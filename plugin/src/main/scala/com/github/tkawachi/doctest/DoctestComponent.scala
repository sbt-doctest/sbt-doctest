package com.github.tkawachi.doctest

sealed trait DoctestComponent
case class Verbatim(code: String) extends DoctestComponent
case class Example(expr: String, expected: TestResult, lineNo: Int) extends DoctestComponent
case class Property(prop: String, lineNo: Int) extends DoctestComponent
