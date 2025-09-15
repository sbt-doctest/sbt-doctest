package com.github.tkawachi.doctest

import com.github.tkawachi.doctest.TestGen.containsProperty

/**
 * Test generator for ScalaTest.
 */
trait ScalaTestGen extends TestGen {

  override protected def importsLine(parsedList: Seq[ParsedDoctest]): String =
    TestGen.importArbitrary(parsedList)

  override protected def suiteDeclarationLine(basename: String, parsedList: Seq[ParsedDoctest]): String = {
    val withCheckers: String = if (containsProperty(parsedList)) withCheckersString else ""

    s"""class `${basename}Doctest`
       |    extends $funSpecClass
       |    $withCheckers""".stripMargin
  }

  override protected def generateTestCase(caseName: String, caseBody: String): String = {
    s"""  describe("$caseName") {
       |$caseBody
       |  }""".stripMargin
  }

  override protected def generateExample(description: String, assertions: String): String =
    s"""    it("$description") {
       |      $assertions
       |    }""".stripMargin

  override protected def generatePropertyExample(description: String, property: String): String =
    s"""    it("$description") {
       |      check {
       |        $property
       |      }
       |    }""".stripMargin

  override protected def generateAssert(actual: String, expected: String): String = {
    val indent = actual.takeWhile(_.isWhitespace)
    s"""${indent}assert(${actual.trim} == "$expected")"""
  }

  protected def withCheckersString: String

  protected def funSpecClass: String
}

object ScalaTestGen {}
