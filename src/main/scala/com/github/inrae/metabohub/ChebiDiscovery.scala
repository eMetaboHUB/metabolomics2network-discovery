/**
 *
 */
package com.github.inrae.metabohub

import inrae.semantic_web.rdf.{QueryVariable, SparqlBuilder, URI}
import inrae.semantic_web.{SWDiscovery, StatementConfiguration}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.scalajs.js.JSConverters.{JSRichFutureNonThenable, JSRichIterableOnce}
import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}


@JSExportTopLevel(name="ChebiDiscovery")
case class ChebiDiscovery(
                           config_discovery : String = """{
             "sources" : [{
               "id"  : "Forum-Desease-Chem",
               "url" : "https://forum.semantic-metabolomics.fr/sparql"
             }],
             "settings" : {
               "logLevel" : "off"
             }
        }""".stripMargin

                         ) {
  /* send batch request by lot */
  val groupBySize = 1000

  val instDiscovery =
    SWDiscovery(StatementConfiguration.setConfigString(config_discovery))
      .prefix("c","http://purl.obolibrary.org/obo/")
      .prefix("cp", "http://purl.obolibrary.org/obo/chebi#")

  /**
   * Give all level ancestor
   *
   * @param chebiIds  : List of ChEBI
   * @param deepLevel : branch level
   * @return
   */

  def ancestor_level(
                      chebiIds: Seq[URI],
                      deepLevel: Int = 1,
                      allAncestor: Boolean = false
                    )
  : Future[Seq[URI]] = {

    if (deepLevel <= 0) throw ChebiDiscoveryException("level must be strictly positive.")

    val listVariableAncestor: List[String] = allAncestor match {
      case false => List("ac_1")
      case true => deepLevel.to(1, -1).foldLeft(List[String]())((acc: List[String], ind: Int) => {
        acc ++ List("ac_" + ind)
      })
    }

    val listPossibleProperties: List[URI] =
      List("rdfs:subClassOf")

    val queryStart: SWDiscovery = instDiscovery
      .something("listOfInterest")
      .setList(chebiIds.distinct)

    deepLevel.to(1, -1).foldLeft(queryStart) {
      (query: SWDiscovery, ind: Int) => {
        query.isSubjectOf(QueryVariable("prop_" + ind), "ac_" + ind)
          .root
          .something("prop_" + ind)
          .setList(listPossibleProperties)
          .focus("ac_" + ind)
      }
    }
     // .console
      .select(listVariableAncestor)
      .commit()
      .raw
      .map(json => json("results")("bindings")
        .arr.flatMap(v => listVariableAncestor.map(variable => SparqlBuilder.createUri(v(variable)))).toSeq)
  }

  @JSExport("ancestor_level")
  def ancestor_level_js(chebiIds: js.Array[String],
                        deepLevel: Int = 1,
                        allAncestor: Boolean = false): js.Promise[js.Array[String]] = {

    ancestor_level(
      chebiIds.toList.map(st => URI(st)),
      deepLevel,
      allAncestor)
      .map(lUris => lUris.map(uri => uri.localName).toJSArray).toJSPromise
  }

  def lowest_common_ancestor(
                              chebiIds: Seq[URI],
                              startNbLevel: Int = 1,
                              stepNbLevel: Int = 1,
                              maxDeepLevel: Int = 8,
                            ): Future[Seq[URI]] = {

    if (startNbLevel > maxDeepLevel) throw ChebiDiscoveryException(
      s"""
         the search depth has reached its maximum.
         startNbLevel -> $startNbLevel,
         stepNbLevel  -> $stepNbLevel,
         maxDeepLevel -> $maxDeepLevel,
         """.stripMargin)

    Future.sequence(chebiIds.map(chebid => {
      ancestor_level(chebiIds = List(chebid), deepLevel = startNbLevel, true)
    })).map(
      ancestorsListByChebId => {
        ancestorsListByChebId.foldLeft(ancestorsListByChebId(0))(
          (accumulator: Seq[URI], ancestorFromAChebId: Seq[URI]) => {
            /* keep only common uri */
            ancestorFromAChebId.filter(uri => accumulator.contains(uri))
          }
        )
      }
    ) map {
      case empty if empty.length <= 0 => lowest_common_ancestor(chebiIds, startNbLevel + stepNbLevel, stepNbLevel, maxDeepLevel)
      case lca => Future {
        lca
      }
    }
  }.flatten

  @JSExport("lowest_common_ancestor")
  def lowest_common_ancestor_js(
                                 chebiIds: js.Array[String],
                                 startNbLevel: Int = 1,
                                 stepNbLevel: Int = 1,
                                 maxDeepLevel: Int = 8,
                               ): js.Promise[js.Array[String]] =
    lowest_common_ancestor(
      chebiIds.toList.map(s => URI(s)),
      startNbLevel,
      stepNbLevel,
      maxDeepLevel
    ).map(lUris => lUris.map(uri => uri.localName).toJSArray).toJSPromise

  def shortenUri( uri : URI) : URI = uri.nameSpace match {
    case "" => URI(uri.localName.replace("http://purl.obolibrary.org/obo/","c:"))
    case _ => uri
  }

  def ontology_based_matching_static_level(
                                            chebiStartIds: Seq[URI],
                                            chebiEndIds: Seq[URI],
                                            deepLevel: Int = 1
                                          )
  : Future[Map[URI, Map[String, URI]]] = {
    if (deepLevel <= 0) throw ChebiDiscoveryException("level must be strictly positive.")

    val listOwlOnProperties: List[URI] =
      List(
        "cp:is_conjugate_acid_of",
        "cp:is_conjugate_base_of",
        "cp:has_functional_parent",
        "cp:is_tautomer_of",
        "cp:has_parent_hydride",
        "cp:is_substituent_group_from",
        "cp:is_enantiomer_of")

    val queryStart: SWDiscovery = instDiscovery
      .something("chebi_start")
      .setList(chebiStartIds.map( uri => shortenUri(uri) ).distinct)

      Future.sequence(List(
      deepLevel.to(2, -1).foldLeft(queryStart) {
        (query: SWDiscovery, ind: Int) => {
          query.isSubjectOf(URI("rdfs:subClassOf"), "ac_" + ind)
        }
      }
        .isSubjectOf(URI("rdfs:subClassOf"), "chebi_end")
        .setList(chebiEndIds.distinct)
        //.console
        .select(List("chebi_start", "chebi_end"))
        .commit()
        .raw
        .map(json =>
          json("results")("bindings").arr.map(row => {
            val chebi_start = SparqlBuilder.createUri(row("chebi_start"))
            val chebi_end = SparqlBuilder.createUri(row("chebi_end"))
            chebi_start -> Map("is_a" -> chebi_end)
          }).toMap
        )
      ,

      deepLevel.to(2, -1).foldLeft(queryStart) {
        (query: SWDiscovery, ind: Int) => {
          query.isSubjectOf(URI("rdfs:subClassOf"), "ac_" + ind)
        }
      }
        .isSubjectOf(URI("rdfs:subClassOf"), "ac_1")
        .isSubjectOf(URI("owl:onProperty"), "prop_1")
        .focus("ac_1")
        .isSubjectOf(URI("owl:someValuesFrom"), "chebi_end")
        .setList(chebiEndIds.map( uri => shortenUri(uri)).distinct)
        .filter.not.equal(QueryVariable("chebi_start"))
        .root
        .something("prop_1")
        .setList(listOwlOnProperties)
        //.console
        .select(List("chebi_start", "chebi_end", "prop_1"))
        .commit()
        .raw
        .map(json =>
          json("results")("bindings").arr.map(row => {
              val chebi_start = SparqlBuilder.createUri(row("chebi_start"))
              val chebi_end = SparqlBuilder.createUri(row("chebi_end"))
              val prop = SparqlBuilder.createUri(row("prop_1")).localName.split("#")(1)
              chebi_start -> Map(prop -> chebi_end)
          }).toMap
        )
    ))
        .map(ll => ll.reduce((x, y) => (x ++ y))
    ).map( x => {
        /*
        println("==============================   ontology_based_matching_static_level ===================================")
        println(s"INPUT LSTART:${chebiStartIds}")
        println(s"INPUT LEND:${chebiEndIds}")
        println(s"DEEP:${deepLevel}")
        println(" --------------- RESULTS --------- ")
        println(x)
        println(" ====================================================================")*/
        x
      })

  }

  def ontology_based_matching_internal(
                              chebiIdRef : URI ,
                              chebiIds: Seq[URI],
                              maxScore: Double = 3.0)
  : Future[Seq[(URI,String,Double)]] = {

    if (maxScore <= 0.0) throw ChebiDiscoveryException(
      s"""
         maxScore must be positive .
         maxScore -> $maxScore

         rules:
         ------
         1.0 -> is_a relation
         0.1 -> is_conjugate_acid_of,is_conjugate_base_of,
                has_functional_parent,is_tautomer_of,
                has_parent_hydride,is_substituent_group_from,
                is_enantiomer_of

         """.stripMargin)

    val deepestLength = Math.ceil(maxScore).toInt
    val LL1=deepestLength.to(1, -1).map(
      deepLevel => {

        val res1 = ontology_based_matching_static_level(List(chebiIdRef),chebiIds, deepLevel)
        val res2 = ontology_based_matching_static_level(chebiIds,List(chebiIdRef), deepLevel)
          (deepLevel-1,res1,res2)
      })
      .map(
        deppLevelRes1Res2 => {
          val deep = deppLevelRes1Res2._1
          val chefLeft =  deppLevelRes1Res2._2
          val chefRight =  deppLevelRes1Res2._3



          val L1 : Future[Seq[(URI,String,Double)]] =

            chefLeft.map(fm => {
              fm.flatMap(v1 => {
                v1._2.map(v2=> {
                  (v2._2,v2._1, v2._1 match {
                    case "is_a" => 1.0 + deep
                    case _      => 0.1 + deep
                  })
               })
            }).toSeq})

          val L2: Future[Seq[(URI, String, Double)]] =
            chefRight.map(fm => {
                  fm.flatMap(v1 => {
                    v1._2.map(v2=> {
                      (v1._1,v2._1, v2._1 match {
                        case "is_a" => - 1.0 - deep
                        case _      => - 0.1 - deep
                      })
                    }).toSeq
                  }).toSeq})

          Future.sequence(List(L1,L2)).map( _.flatten )

      })

    Future.sequence(LL1).map(_.flatten)
    }

  def ontology_based_matching(
                               chebiIdRef : URI ,
                               chebiIds: Seq[URI],
                               maxScore: Double = 4.5) : Future[Seq[(URI,String,Double)]] =
    Future.sequence(chebiIds.grouped(groupBySize).map(
      chebIdSublist => {
        ontology_based_matching_internal(chebiIdRef,chebIdSublist,maxScore)
      }
    )).map(l => l.reduce( (x,y) => x ++ y ))

  @JSExport("ontology_based_matching")
  def ontology_based_matching_js(
                                  chebiIdRef : String ,
                                  chebiIds: js.Array[String],
                                  maxScore: Double = 4.5
                               ): js.Promise[js.Array[js.Object with js.Dynamic]] = {

    /* patch to check uri well formed */
    ontology_based_matching(
      URI(js.URIUtils.encodeURI(chebiIdRef)),
      chebiIds.toList.map(s => URI(js.URIUtils.encodeURI(s))),
      maxScore
    ).map(lTuples => lTuples.map(tuple =>  Dynamic.literal(
      "uri" -> tuple._1.localName,
      "property" -> tuple._2,
      "score" -> tuple._3)
    ).toJSArray).toJSPromise
  }
}