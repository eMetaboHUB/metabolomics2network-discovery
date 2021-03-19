package com.github.inrae.metabohub

import inrae.semantic_web.rdf.URI
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object AncestorTest extends TestSuite {
  val tests = Tests {

    test("chebi - get ancestor / level") {
        ChebiDiscovery().ancestor_level(List(URI("http://purl.obolibrary.org/obo/CHEBI_16359")),5)
        .map( (response : Seq[URI]) => response.foreach(println))
        .recover(err => {
          println(err)
        })
        }
  }
}
