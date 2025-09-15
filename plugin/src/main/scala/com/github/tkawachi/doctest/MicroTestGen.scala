package com.github.tkawachi.doctest

/**
 * Test generator for µTest.
 */
object MicroTestGen extends TestGen {

  override protected def importsLine(parsedList: Seq[ParsedDoctest]): String =
    "import _root_.utest._"

  override protected def suiteDeclarationLine(basename: String, parsedList: Seq[ParsedDoctest]): String =
    s"object `${basename}Doctest` extends TestSuite"

  override protected def testCasesLine(basename: String, parsedList: Seq[ParsedDoctest]): String =
    s"""
       | val tests = this {
       |${super.testCasesLine(basename, parsedList)}
       | }""".stripMargin

  override protected def generateTestCase(caseName: String, caseBody: String): String = {
    s"""  "$caseName" - {
       |$caseBody
       |  }""".stripMargin
  }

  override protected def generateExample(description: String, assertions: String): String = {
    s"""    "$description"-{
       |      $assertions
       |    }""".stripMargin
  }

  override protected def generatePropertyExample(description: String, property: String): String = {
    s"""    "$description"-{
       |      sbtDoctestReplString($property)
       |    }""".stripMargin
  }

  override protected def generateAssert(actual: String, expected: String): String =
    s"""      val _actual_   = $actual
       |      val _expected_ = "$expected"
       |      assert( _expected_ == _actual_ )""".stripMargin
}
