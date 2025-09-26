package com.github.tkawachi.doctest

import com.github.tkawachi.doctest.StringUtil.escape

object MunitGen extends TestGen {
  override protected def importsLine(parsedList: Seq[ParsedDoctest]): String = if (
    TestGen.containsProperty(parsedList)
  ) {
    s"""import _root_.munit._
       |import _root_.org.scalacheck.Prop._
       |""".stripMargin
  } else "import _root_.munit._"

  override protected def testCasesLine(basename: String, parsedList: Seq[ParsedDoctest]): String =
    parsedList
      .map { doctest =>
        val testName = escape(s"$basename.scala:${doctest.lineNo}: ${doctest.symbol}")
        val testBody = doctest.components.map {
          case x: Verbatim =>
            Right(
              componentLineVerbatim(doctest.lineNo, x)
            )
          case x: Example =>
            Right(
              componentLineExample(doctest.lineNo, x)
            )
          case x: Property =>
            Left(
              componentLineProperty(doctest.lineNo, x)
            )
        }
        val properties = testBody.collect { case Left(x) => x }.mkString("\n\n")
        val other = testBody.collect { case Right(x) => x }.mkString("\n\n")
        Seq(
          properties,
          generateTestCase(testName, other)
        ).mkString("\n\n")
      }
      .mkString("\n\n")

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
