package com.github.inrae.metabohub

import inrae.semantic_web.rdf.URI
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object BuildSvgTest extends TestSuite {

  val tests = Tests {
    test("ontology_based_matching") {
      ChebiDiscovery().ontology_based_matching(URI("http://purl.obolibrary.org/obo/CHEBI_104011"), List(URI("http://purl.obolibrary.org/obo/CHEBI_15428")))
        .map((response: Seq[(URI, String, Double)]) => {
          response.foreach(println)

          assert(response.length == 1)

          ChebiDiscovery().build_graph(
            URI("http://purl.obolibrary.org/obo/CHEBI_104011"),
            response(0)._1,
            response(0)._2,
            response(0)._3)
            .map((response: Seq[(String,URI)]) => {
              println("1=================================== PATH ==============================")
              println(response)
              ChebiDiscovery().graph_html(response)
            })
        })

      ChebiDiscovery().build_graph(
        URI("http://purl.obolibrary.org/obo/CHEBI_104011"),
        URI("http://purl.obolibrary.org/obo/CHEBI_15428"),
        "has_functional_parent",
        2.1)
        .map((response: Seq[(String,URI)]) => {
          println("2=================================== PATH ==============================")
          val r = ChebiDiscovery().graph_html(response)
          println(r.render.innerHTML)
        })
    }

    test("2") {
      ChebiDiscovery().build_graph(
        URI("http://purl.obolibrary.org/obo/CHEBI_15318"),
        URI("http://purl.obolibrary.org/obo/CHEBI_62207"),
        "has_functional_parent",
        -3.1)
        .map((response: Seq[(String,URI)]) => {
          println("2=================================== PATH ==============================")
          val r = ChebiDiscovery().graph_html(response)
          println(r.render.innerHTML)
        })
    }
    test("3") {
      ChebiDiscovery().build_graph(
        URI("http://purl.obolibrary.org/obo/CHEBI_15318"),
        URI("http://purl.obolibrary.org/obo/CHEBI_28177"),
        "has_functional_parent",
        -2.1)
        .map((response: Seq[(String,URI)]) => {
          println("2=================================== PATH ==============================")
          val r = ChebiDiscovery().graph_html(response)
          println(r.render.innerHTML)
        })
    }

  }
}
