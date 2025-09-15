package com.github.tkawachi.doctest

import com.github.tkawachi.doctest.StringUtil.escape

object ScalaCheckGen extends TestGen {

  override protected def suiteDeclarationLine(basename: String, parsedList: Seq[ParsedDoctest]): String = {
    s"""object `${basename}Doctest`
       |    extends _root_.org.scalacheck.Properties("${escape(basename)}.scala")""".stripMargin
  }

  override protected def importsLine(parsedList: Seq[ParsedDoctest]): String = {
    val importProp =
      if (TestGen.containsExample(parsedList) || TestGen.containsProperty(parsedList))
        "import _root_.org.scalacheck.Prop.{BooleanOperators => _, _}"
      else
        ""
    s"""${TestGen.importArbitrary(parsedList)}
       |$importProp""".stripMargin
  }

  override protected def generateTestCase(caseName: String, caseBody: String): String =
    s"""  include(new _root_.org.scalacheck.Properties("$caseName") {
         |$caseBody
         |  })""".stripMargin

  override protected def generateExample(description: String, assertions: String): String =
    s"""    property("$description") = _root_.org.scalacheck.Prop.secure {
       |      $assertions
       |    }""".stripMargin

  override protected def generatePropertyExample(description: String, property: String): String =
    s"""    property("$description") = _root_.org.scalacheck.Prop.forAll {
       |      $property
       |    }""".stripMargin

  override protected def generateAssert(actual: String, expected: String): String =
    s"""      val actual = $actual
       |      val expected = "$expected"
       |      (actual == expected) :| s"'$$actual' is not equal to '$$expected'"""".stripMargin

}
