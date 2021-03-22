package com.github.inrae.metabohub

import inrae.semantic_web.rdf.URI
import utest._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.implicitConversions
import scala.scalajs.js

object Metabolites2NetworkPeakforestTest extends TestSuite {

  val lChebIdPeakforest = List(
    "http://purl.obolibrary.org/obo/CHEBI_73888","http://purl.obolibrary.org/obo/CHEBI_100246","http://purl.obolibrary.org/obo/CHEBI_10072","http://purl.obolibrary.org/obo/CHEBI_10276","http://purl.obolibrary.org/obo/CHEBI_104011","http://purl.obolibrary.org/obo/CHEBI_107667","http://purl.obolibrary.org/obo/CHEBI_10782","http://purl.obolibrary.org/obo/CHEBI_1148","http://purl.obolibrary.org/obo/CHEBI_116314","http://purl.obolibrary.org/obo/CHEBI_116735","http://purl.obolibrary.org/obo/CHEBI_1189","http://purl.obolibrary.org/obo/CHEBI_11909","http://purl.obolibrary.org/obo/CHEBI_12387","http://purl.obolibrary.org/obo/CHEBI_1296","http://purl.obolibrary.org/obo/CHEBI_1301","http://purl.obolibrary.org/obo/CHEBI_13172","http://purl.obolibrary.org/obo/CHEBI_131924","http://purl.obolibrary.org/obo/CHEBI_132750","http://purl.obolibrary.org/obo/CHEBI_132983","http://purl.obolibrary.org/obo/CHEBI_13332","http://purl.obolibrary.org/obo/CHEBI_137077","http://purl.obolibrary.org/obo/CHEBI_138000","http://purl.obolibrary.org/obo/CHEBI_1387","http://purl.obolibrary.org/obo/CHEBI_1427","http://purl.obolibrary.org/obo/CHEBI_14750","http://purl.obolibrary.org/obo/CHEBI_15318","http://purl.obolibrary.org/obo/CHEBI_15346","http://purl.obolibrary.org/obo/CHEBI_15347","http://purl.obolibrary.org/obo/CHEBI_15350","http://purl.obolibrary.org/obo/CHEBI_15351","http://purl.obolibrary.org/obo/CHEBI_15354","http://purl.obolibrary.org/obo/CHEBI_15355","http://purl.obolibrary.org/obo/CHEBI_15356","http://purl.obolibrary.org/obo/CHEBI_15365","http://purl.obolibrary.org/obo/CHEBI_15366","http://purl.obolibrary.org/obo/CHEBI_15367","http://purl.obolibrary.org/obo/CHEBI_15368","http://purl.obolibrary.org/obo/CHEBI_15375","http://purl.obolibrary.org/obo/CHEBI_15382","http://purl.obolibrary.org/obo/CHEBI_15399","http://purl.obolibrary.org/obo/CHEBI_15409","http://purl.obolibrary.org/obo/CHEBI_15422","http://purl.obolibrary.org/obo/CHEBI_15428","http://purl.obolibrary.org/obo/CHEBI_15430","http://purl.obolibrary.org/obo/CHEBI_15468","http://purl.obolibrary.org/obo/CHEBI_1547","http://purl.obolibrary.org/obo/CHEBI_15473","http://purl.obolibrary.org/obo/CHEBI_15473","http://purl.obolibrary.org/obo/CHEBI_15487","http://purl.obolibrary.org/obo/CHEBI_15517","http://purl.obolibrary.org/obo/CHEBI_15524","http://purl.obolibrary.org/obo/CHEBI_15533","http://purl.obolibrary.org/obo/CHEBI_15544","http://purl.obolibrary.org/obo/CHEBI_15545","http://purl.obolibrary.org/obo/CHEBI_15551",
    "http://purl.obolibrary.org/obo/CHEBI_15554","http://purl.obolibrary.org/obo/CHEBI_15570","http://purl.obolibrary.org/obo/CHEBI_15573","http://purl.obolibrary.org/obo/CHEBI_15600","http://purl.obolibrary.org/obo/CHEBI_15603","http://purl.obolibrary.org/obo/CHEBI_15611","http://purl.obolibrary.org/obo/CHEBI_15633","http://purl.obolibrary.org/obo/CHEBI_15670","http://purl.obolibrary.org/obo/CHEBI_15671","http://purl.obolibrary.org/obo/CHEBI_15672","http://purl.obolibrary.org/obo/CHEBI_15676","http://purl.obolibrary.org/obo/CHEBI_15688","http://purl.obolibrary.org/obo/CHEBI_15694","http://purl.obolibrary.org/obo/CHEBI_15695","http://purl.obolibrary.org/obo/CHEBI_15698","http://purl.obolibrary.org/obo/CHEBI_15699","http://purl.obolibrary.org/obo/CHEBI_15700","http://purl.obolibrary.org/obo/CHEBI_15702","http://purl.obolibrary.org/obo/CHEBI_15713","http://purl.obolibrary.org/obo/CHEBI_15714","http://purl.obolibrary.org/obo/CHEBI_15721","http://purl.obolibrary.org/obo/CHEBI_15724","http://purl.obolibrary.org/obo/CHEBI_15725","http://purl.obolibrary.org/obo/CHEBI_15727","http://purl.obolibrary.org/obo/CHEBI_15728","http://purl.obolibrary.org/obo/CHEBI_15729","http://purl.obolibrary.org/obo/CHEBI_15732","http://purl.obolibrary.org/obo/CHEBI_15741","http://purl.obolibrary.org/obo/CHEBI_15743","http://purl.obolibrary.org/obo/CHEBI_15746","http://purl.obolibrary.org/obo/CHEBI_15751","http://purl.obolibrary.org/obo/CHEBI_15756","http://purl.obolibrary.org/obo/CHEBI_15760","http://purl.obolibrary.org/obo/CHEBI_15765","http://purl.obolibrary.org/obo/CHEBI_15767","http://purl.obolibrary.org/obo/CHEBI_15768","http://purl.obolibrary.org/obo/CHEBI_15793","http://purl.obolibrary.org/obo/CHEBI_15811","http://purl.obolibrary.org/obo/CHEBI_15816","http://purl.obolibrary.org/obo/CHEBI_15820",
    "http://purl.obolibrary.org/obo/CHEBI_15830","http://purl.obolibrary.org/obo/CHEBI_15842","http://purl.obolibrary.org/obo/CHEBI_15843","http://purl.obolibrary.org/obo/CHEBI_15882","http://purl.obolibrary.org/obo/CHEBI_15887","http://purl.obolibrary.org/obo/CHEBI_15891","http://purl.obolibrary.org/obo/CHEBI_15901","http://purl.obolibrary.org/obo/CHEBI_15918","http://purl.obolibrary.org/obo/CHEBI_15940","http://purl.obolibrary.org/obo/CHEBI_15956","http://purl.obolibrary.org/obo/CHEBI_15960","http://purl.obolibrary.org/obo/CHEBI_15963","http://purl.obolibrary.org/obo/CHEBI_15966","http://purl.obolibrary.org/obo/CHEBI_15971","http://purl.obolibrary.org/obo/CHEBI_15996","http://purl.obolibrary.org/obo/CHEBI_16000","http://purl.obolibrary.org/obo/CHEBI_16002","http://purl.obolibrary.org/obo/CHEBI_16010","http://purl.obolibrary.org/obo/CHEBI_16015","http://purl.obolibrary.org/obo/CHEBI_16020","http://purl.obolibrary.org/obo/CHEBI_16024","http://purl.obolibrary.org/obo/CHEBI_16027","http://purl.obolibrary.org/obo/CHEBI_16027","http://purl.obolibrary.org/obo/CHEBI_16032","http://purl.obolibrary.org/obo/CHEBI_16039","http://purl.obolibrary.org/obo/CHEBI_16039","http://purl.obolibrary.org/obo/CHEBI_16040","http://purl.obolibrary.org/obo/CHEBI_16048","http://purl.obolibrary.org/obo/CHEBI_16057","http://purl.obolibrary.org/obo/CHEBI_1606","http://purl.obolibrary.org/obo/CHEBI_16069","http://purl.obolibrary.org/obo/CHEBI_16070","http://purl.obolibrary.org/obo/CHEBI_16087","http://purl.obolibrary.org/obo/CHEBI_16108","http://purl.obolibrary.org/obo/CHEBI_16112","http://purl.obolibrary.org/obo/CHEBI_16113","http://purl.obolibrary.org/obo/CHEBI_16118","http://purl.obolibrary.org/obo/CHEBI_16119","http://purl.obolibrary.org/obo/CHEBI_16125","http://purl.obolibrary.org/obo/CHEBI_16135","http://purl.obolibrary.org/obo/CHEBI_16153")

  val tests = Tests {
    test("ontology_based_matching") {
      ChebiDiscovery().ontology_based_matching(URI("http://purl.obolibrary.org/obo/CHEBI_34247"), lChebIdPeakforest.map(uri => URI(uri)))
        .map((response: Seq[(URI, String, Double)]) => {
          println("OK")
          response.foreach(println)
        })
    }

    test("ontology_based_matching") {
      ChebiDiscovery().ontology_based_matching_js(" ://purl.oboli 584", js.Array(" ://purl.oboli 584"))
    }
  }
}