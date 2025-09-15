package com.github.tkawachi.doctest

case class ScaladocComment(pkg: Option[String], symbol: String, codeBlocks: List[String], text: String, lineNo: Int)
