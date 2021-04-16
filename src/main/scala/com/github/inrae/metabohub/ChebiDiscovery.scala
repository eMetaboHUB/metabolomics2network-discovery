/**
 *
 */
package com.github.inrae.metabohub

import inrae.semantic_web.rdf.{QueryVariable, SparqlBuilder, URI}
import inrae.semantic_web.{SWDiscovery, StatementConfiguration}
import org.scalajs.dom
import scalatags.JsDom
import scalatags.JsDom.all._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.scalajs.js
import scala.scalajs.js.Dynamic
import scala.scalajs.js.JSConverters._
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

                         ,groupBySize : Int = 1000) {
  /**
   *  implementation based of the ontology based matching describe in the
   * "Improving lipid mapping in Genome Scale Metabolic Networks using ontologies." using the
   *  Chemical Entities of Biological Interest (ChEBI)
   *
   *
   * - output : list of object `{uri: <string>, property: <string> , score: <double>}`
   * - `uri` uri from the input list
   * - `property` possible value : `"is_conjugate_acid_of", "is_conjugate_base_of",
   * "has_functional_parent","is_tautomer_of","chas_parent_hydride","is_substituent_group_from",
   * "is_enantiomer_of"`
   * - `score` distance between `uri` and `chebid` . distance is positive (1.0) when `chebid` matches a more generic class
   * in the list. The distance is slightly increased (by 0.1) when a relation different tha `is_a` of the lipid
   * is present in the `listChebIds`
   *
   * @param chebiIdRef a reference ChEBI id : `String`
   * @param chebiIds a list of CheBid : `Array[String]`
   * @param maxScore (optional) max score to reach : `Double` (default : `4.5`)
   * @return list of object `{uri: <string>, property: <string> , score: <double>}`
   */

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


  /**
   * Get complete Path (from the most specific to the most generic term)
   * with function (edge name : is_a, is_conjugate_acid_of, ...) and
   * intermediate ChEBIIb (node) from uriIn to uriOut
   * @param uriIn
   * @param uriOut
   * @param property last name property to get uriOut
   * @param score deep, if positive uriIn is the most specific
   * @return a promise containing the path
   */
  @JSExport("build_graph")
  def build_graph_js(uriIn: String, uriOut : String, property : String, score : Double): js.Promise[js.Array[(String,String)]] =
    build_graph(URI(uriIn),URI(uriOut),property,score).map( _.map( v => (v._1,v._2.localName) ).toJSArray ).toJSPromise


  /**
   * Get UL/LI Html tag representation of a CHEBI path
   * @param path : sequence (function/chebid) resulting from build_graph_js
   * @return String : html representation
   */
  @JSExport("graph_html")
  def graph_html_js(path : js.Array[(String,String)]) : String =
    graph_html(path.map(v => (v._1,URI(v._2))).toSeq).render.innerHTML


  def ontology_based_matching(
                               chebiIdRef : URI ,
                               chebiIds: Seq[URI],
                               maxScore: Double = 4.5) : Future[Seq[(URI,String,Double)]] =
    Future.sequence(chebiIds.grouped(groupBySize).map(
      chebIdSublist => {
        ontology_based_matching_internal(chebiIdRef,chebIdSublist,maxScore)
      }
    )).map(l => l.reduce( (x,y) => x ++ y ))

  val instDiscovery =
    SWDiscovery(StatementConfiguration.setConfigString(config_discovery))
      .prefix("c","http://purl.obolibrary.org/obo/")
      .prefix("cp", "http://purl.obolibrary.org/obo/chebi#")


  def shortenUri( uri : URI) : URI = uri.nameSpace match {
    case "" => URI(uri.localName.replace("http://purl.obolibrary.org/obo/","c:"))
    case _ => uri
  }

  val listOwlOnProperties: List[URI] =
    List(
      "cp:is_conjugate_acid_of",
      "cp:is_conjugate_base_of",
      "cp:has_functional_parent",
      "cp:is_tautomer_of",
      "cp:has_parent_hydride",
      "cp:is_substituent_group_from",
      "cp:is_enantiomer_of")

  def ontology_based_matching_static_level(
                                            chebiStartIds: Seq[URI],
                                            chebiEndIds: Seq[URI],
                                            deepLevel: Int = 1
                                          )
  : Future[Map[URI, Map[String, URI]]] = {
    if (deepLevel <= 0) throw ChebiDiscoveryException("level must be strictly positive.")

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
       // .console
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
    )/*
        .map( x => {

        println("==============================   ontology_based_matching_static_level ===================================")
        println(s"INPUT LSTART:${chebiStartIds}")
        println(s"INPUT LEND:${chebiEndIds}")
        println(s"DEEP:${deepLevel}")
        println(" --------------- RESULTS --------- ")
        println(x)
        println(" ====================================================================")
        x
      }) */
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


  def build_graph(uriIn: URI, uriOut : URI, property : String, score : Double): Future[Seq[(String,URI)]] = {

    val deepestLength = Math.ceil(Math.abs(score)).toInt

    val (chebiStart,chebiEnd) = score match {
      case s if s>0 => (uriIn,uriOut)
      case _ => (uriOut,uriIn)
    }

    val queryStart: SWDiscovery = instDiscovery
      .something("ac_1")
      .set(chebiStart)


    val q1 = 2.to(deepestLength).foldLeft(queryStart) {
      (query: SWDiscovery, ind: Int) => {
        query.isSubjectOf(URI("rdfs:subClassOf"), "ac_" + ind)
      }
    }

    (if ( property != "is_a") {
          q1.isSubjectOf(URI("rdfs:subClassOf"), "ac_" + (deepestLength + 1))
            .isSubjectOf(URI("owl:onProperty"))
              .set(URI("cp:" + property))
            .focus("ac_" + (deepestLength + 1))
              .isSubjectOf(URI("owl:someValuesFrom"))
                .set(chebiEnd)
      } else {
        q1.isSubjectOf(URI("rdfs:subClassOf"), "ac_" + (deepestLength + 1))
          .set(chebiEnd)
      })
      .select(1.to(deepestLength).map("ac_"+_))
      .commit()
      .raw
      .map(json => {
        println(json("results")("bindings"))
        val rows = json("results")("bindings").arr
        if ( rows.length<=0 ) throw ChebiDiscoveryException(s"Impossible to find the way between $chebiStart and $chebiEnd with property=$property,score=$score")
        Seq( ("",chebiEnd) ) ++
          Seq( (property,SparqlBuilder.createUri(rows(0)("ac_"+deepestLength))) )  ++
          (deepestLength-1).to(1,-1).map("ac_"+_).map( chebiInterm => ("is_a",SparqlBuilder.createUri(rows(0)(chebiInterm))) )
      })
  }

  def graph_html(path : Seq[(String,URI)]) : JsDom.TypedTag[dom.html.UList] = {
    if ( path.length == 0 ) {
        ul()
    } else {
      val function = path(0)._1
      val chebiId = path(0)._2

      val tagImg = function match {
        case _ if function == "is_a" || listOwlOnProperties.filter( _.localName == function).length>0 =>
          img(src := "https://www.ebi.ac.uk/chebi/images/ontology/checked/" + function + ".gif", alt := function)
        case _ => span()
      }

      if (path.length > 1) {
        ul(
          li(
            span(tagImg,
            a(href := chebiId.localName)(" "+chebiId.localName.replace("http://purl.obolibrary.org/obo/CHEBI_", "CHEBI:")),
            graph_html(path.drop(1)))
          )
        )
      } else {
        ul(
          li(
            span(tagImg,
            a(href := chebiId.localName)(" "+chebiId.localName.replace("http://purl.obolibrary.org/obo/CHEBI_", "CHEBI:"))),
        ))
      }
    }
  }

}