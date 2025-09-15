package com.github.tkawachi.doctest

object MunitGen extends TestGen {
  override protected def importsLine(parsedList: Seq[ParsedDoctest]): String = if (
    TestGen.containsProperty(parsedList)
  ) {
    s"""import _root_.munit._
       |import _root_.org.scalacheck.Prop._
       |""".stripMargin
  } else "import _root_.munit._"

  override protected def suiteDeclarationLine(basename: String, parsedList: Seq[ParsedDoctest]): String =
    if (TestGen.containsProperty(parsedList)) s"class `${basename}Doctest` extends ScalaCheckSuite"
    else s"class `${basename}Doctest` extends FunSuite"

  override protected def generateTestCase(caseName: String, caseBody: String): String = {
    s"""  test("$caseName") {
       |$caseBody
       |  }""".stripMargin
  }

  override protected def generateExample(description: String, assertions: String): String = {
    s"""    //$description
       |    $assertions""".stripMargin
  }

  override protected def generatePropertyExample(description: String, property: String): String = {
    s"""  property("$description") {
       |    forAll($property)
       |  }
       |""".stripMargin
  }

  override protected def generateAssert(actual: String, expected: String): String = {
    val ws = actual.takeWhile(_.isWhitespace)
    s"""${ws}assertEquals(${actual.drop(ws.length)}, "$expected")""".stripMargin
  }

}
