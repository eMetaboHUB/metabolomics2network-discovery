package com.github.inrae.metabohub

import inrae.semantic_web.rdf.URI
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object GetDescriptionTest extends TestSuite {

  val tests = Tests {
    test("get_description CHEBI_104011") {
      ChebiDiscovery().get_description(URI("http://purl.obolibrary.org/obo/CHEBI_104011"))
        .map((response: Map[URI,String]) => {
          response.foreach( chebiRole => println("CHEBI_104011 -> "+chebiRole))
          assert(response.toSeq.length>0)
        })
    }

    test("get_description CHEBI_15428") {
      ChebiDiscovery().get_description(URI("http://purl.obolibrary.org/obo/CHEBI_15428"))
        .map((response: Map[URI,String]) => {
          response.foreach( chebiRole => println("CHEBI_15428 -> "+chebiRole))
          assert(response.toSeq.length>0)
        })
    }

    test("get_description CHEBI_15318") {
      ChebiDiscovery().get_description(URI("http://purl.obolibrary.org/obo/CHEBI_15318"))
        .map((response: Map[URI,String]) => {
          response.foreach( chebiRole => println("CHEBI_15318 -> "+chebiRole))
          assert(response.toSeq.length>0)
        })
    }

    test("get_description CHEBI_62207") {
      ChebiDiscovery().get_description(URI("http://purl.obolibrary.org/obo/CHEBI_62207"))
        .map((response: Map[URI,String]) => {
          response.foreach( chebiRole => println("CHEBI_62207 -> "+chebiRole))
          assert(response.toSeq.length>0)
        })
    }
  }
}
