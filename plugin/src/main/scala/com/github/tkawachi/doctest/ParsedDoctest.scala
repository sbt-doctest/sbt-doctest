package com.github.tkawachi.doctest

case class ParsedDoctest(pkg: Option[String], symbol: String, components: Seq[DoctestComponent], lineNo: Int)
