package com.github.inrae.metabohub

import inrae.semantic_web.rdf.URI
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object Metabolites2NetworkTest extends TestSuite {
  val tests = Tests {

    test (" chebi - Lowest Common Ancestor ") {
      val data = List("http://purl.obolibrary.org/obo/CHEBI_15756", "http://purl.obolibrary.org/obo/CHEBI_7896").map(s => URI(s))

      ChebiDiscovery().ontology_based_matching_static_level(data,data)
        .map( (response : Map[URI,Map[String,URI]]) =>{
          assert( response == Map(
            URI("http://purl.obolibrary.org/obo/CHEBI_15756") -> Map("is_conjugate_acid_of"->URI("http://purl.obolibrary.org/obo/CHEBI_7896"),
              URI("http://purl.obolibrary.org/obo/CHEBI_7896") -> Map("is_conjugate_base_of"->URI("http://purl.obolibrary.org/obo/CHEBI_15756"))
          )) )
        })

      val data2 = List("http://purl.obolibrary.org/obo/CHEBI_90488", "http://purl.obolibrary.org/obo/CHEBI_57880").map(s => URI(s))
      ChebiDiscovery().ontology_based_matching_static_level(data2,data2,2)
        .map( (response : Map[URI,Map[String,URI]]) =>{
          assert( response == Map(
            URI("http://purl.obolibrary.org/obo/CHEBI_90488") -> Map("is_conjugate_acid_of"->URI("http://purl.obolibrary.org/obo/CHEBI_57880"))
            ))
        })

      val data3 = List("http://purl.obolibrary.org/obo/CHEBI_36023", "http://purl.obolibrary.org/obo/CHEBI_30828").map(s => URI(s))
      ChebiDiscovery().ontology_based_matching_static_level(data3,data3,2)
        .map( (response : Map[URI,Map[String,URI]]) =>{
          println("RESULTATS ========================>>>")
          response.foreach(println)
          assert( response == Map(
            URI("http://purl.obolibrary.org/obo/CHEBI_30828") -> Map("is_conjugate_base_of"->URI("http://purl.obolibrary.org/obo/CHEBI_36023"))
          ))
        })
    }

    test("ontology_based_matching") {
      ChebiDiscovery().ontology_based_matching(List("http://purl.obolibrary.org/obo/CHEBI_36023",
        "http://purl.obolibrary.org/obo/CHEBI_30828"))
        .map( (response : Seq[(URI,URI,String,Double)]) =>{
          response.foreach(println)
        })

      ChebiDiscovery().ontology_based_matching(List("http://purl.obolibrary.org/obo/CHEBI_15756",
        "http://purl.obolibrary.org/obo/CHEBI_7896"))
        .map( (response : Seq[(URI,URI,String,Double)]) =>{
          response.foreach(println)
        })

      ChebiDiscovery().ontology_based_matching(List("http://purl.obolibrary.org/obo/CHEBI_90488",
        "http://purl.obolibrary.org/obo/CHEBI_57880"))
        .map( (response : Seq[(URI,URI,String,Double)]) =>{
          response.foreach(println)
        })
    }
  }
}
