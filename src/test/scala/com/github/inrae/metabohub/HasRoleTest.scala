package com.github.inrae.metabohub

import inrae.semantic_web.rdf.URI
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object HasRoleTest extends TestSuite {

  val tests = Tests {
    test("get_has_role CHEBI_104011") {
      ChebiDiscovery().get_has_role(URI("http://purl.obolibrary.org/obo/CHEBI_104011"))
        .map((response: Seq[URI]) => {
          response.foreach( chebiRole => println("CHEBI_104011 -> role:"+chebiRole.localName))
          assert(response.length>0)
        })
    }

    test("get_has_role CHEBI_15428") {
      ChebiDiscovery().get_has_role(URI("http://purl.obolibrary.org/obo/CHEBI_15428"))
        .map((response: Seq[URI]) => {
          response.foreach( chebiRole => println("CHEBI_15428 -> role:"+chebiRole.localName))
          assert(response.length>0)
        })
    }

    test("get_has_role CHEBI_15318") {
      ChebiDiscovery().get_has_role(URI("http://purl.obolibrary.org/obo/CHEBI_15318"))
        .map((response: Seq[URI]) => {
          response.foreach( chebiRole => println("CHEBI_15318 -> role:"+chebiRole.localName))
          assert(response.length>0)
        })
    }

    test("get_has_role CHEBI_62207") {
      ChebiDiscovery().get_has_role(URI("http://purl.obolibrary.org/obo/CHEBI_62207"))
        .map((response: Seq[URI]) => {
          response.foreach( chebiRole => println("CHEBI_62207 -> role:"+chebiRole.localName))
          assert(response.length>0)
        })
    }
  }
}
