package com.github.inrae.metabohub

import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel(name="ChebiDiscoveryException")
case class ChebiDiscoveryException(s: String)  extends Exception(s)
