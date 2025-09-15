package com.github.tkawachi.doctest

/**
 * Test generator for Minitest.
 */
object MinitestGen extends TestGen {

  override protected def suiteDeclarationLine(basename: String, parsedList: Seq[ParsedDoctest]): String = {
    val withCheckers = if (TestGen.containsProperty(parsedList)) "with Checkers" else ""
    s"object `${basename}Doctest` extends SimpleTestSuite $withCheckers"
  }

  override protected def importsLine(parsedList: Seq[ParsedDoctest]): String =
    s"""import _root_.minitest._
       |${if (TestGen.containsProperty(parsedList))
        s"""import _root_.minitest.laws.Checkers
           |import _root_.org.scalacheck.Prop.{BooleanOperators => _, _}
           |${TestGen.importArbitrary(parsedList)}""".stripMargin
      else ""}""".stripMargin

  override protected def helperMethodsLine: String = {
    val apiPkg = "_root_.minitest.api"
    s"""${super.helperMethodsLine}
       |
       |  implicit def toVoid[A](ref: A)(implicit location: $apiPkg.SourceLocation): $apiPkg.Void =
       |    $apiPkg.Void.UnitRef""".stripMargin
  }

  override protected def generateTestCase(caseName: String, caseBody: String): String = {
    s"""  test("$caseName") {
       |$caseBody
       |  }""".stripMargin
  }

  override protected def generateExample(description: String, assertions: String): String =
    s"""    //$description
       |    $assertions""".stripMargin

  override protected def generatePropertyExample(description: String, property: String): String = {
    s"""    //$description
       |    check(_root_.org.scalacheck.Prop.forAll($property))""".stripMargin
  }

  override protected def generateAssert(actual: String, expected: String): String =
    s"""    assertEquals($actual, "$expected")""".stripMargin
}
