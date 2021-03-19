package com.github.inrae.metabohub

import inrae.semantic_web.rdf.URI
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions

object LowestCommonAncestorTest extends TestSuite {
  val tests = Tests {

    test (" chebi - Lowest Common Ancestor ") {
      /*
          Check CHEBI_24663 is the Common Ancestor of CHEBI_16359,CHEBI_36260,CHEBI_43602
       */
      ChebiDiscovery()
        .ancestor_level(List(URI("http://purl.obolibrary.org/obo/CHEBI_16359")),2)
        .map( (response : Seq[URI]) => {
          println(" ================= http://purl.obolibrary.org/obo/CHEBI_16359 ================= ")
          //response.foreach(println)
          assert( response.contains(URI("http://purl.obolibrary.org/obo/CHEBI_24663")))
        })

      ChebiDiscovery()
        .ancestor_level(List(URI("http://purl.obolibrary.org/obo/CHEBI_36260")),1)
        .map( (response : Seq[URI]) => {
          println(" ================= http://purl.obolibrary.org/obo/CHEBI_36260 ================= ")
          //response.foreach(println)
          assert( response.contains(URI("http://purl.obolibrary.org/obo/CHEBI_24663")))
        })

      ChebiDiscovery()
        .ancestor_level(List(URI("http://purl.obolibrary.org/obo/CHEBI_43602")),2)
        .map( (response : Seq[URI]) =>{
          println(" ================= http://purl.obolibrary.org/obo/CHEBI_43602 ================= ")
          //response.foreach(println)
          assert( response.contains(URI("http://purl.obolibrary.org/obo/CHEBI_24663")))
        })

      ChebiDiscovery().lowest_common_ancestor(List("http://purl.obolibrary.org/obo/CHEBI_16359",
        "http://purl.obolibrary.org/obo/CHEBI_36260","http://purl.obolibrary.org/obo/CHEBI_43602"))
        .map( (response : Seq[URI]) =>{
          println("RESULTATS ========================>>>")
          response.foreach(println)
          //assert( response == List("http://purl.obolibrary.org/obo/CHEBI_24663"))
        })

    }
  }
}
