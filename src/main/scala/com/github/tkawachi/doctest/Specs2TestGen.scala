package com.github.tkawachi.doctest

/**
 * Test generator for specs2.
 */
object Specs2TestGen extends TestGen {

  val BasePackage = "_root_.org.specs2"

  override protected def importsLine(parsedList: Seq[ParsedDoctest]): String =
    TestGen.importArbitrary(parsedList)

  override protected def suiteDeclarationLine(basename: String, parsedList: Seq[ParsedDoctest]): String = {
    s"""class `${basename}Doctest`
       |    extends $BasePackage.mutable.Specification
       |    with $BasePackage.ScalaCheck""".stripMargin
  }

  override protected def helperMethodsLine: String = {
    val MatcherPackage = s"$BasePackage.matcher"
    s"""${super.helperMethodsLine}
       |
       |  implicit def toMatcher[T](t: T): $MatcherPackage.Matcher[T] = $MatcherPackage.AlwaysMatcher[T]()""".stripMargin
  }

  override protected def generateTestCase(caseName: String, caseBody: String): String =
    s"""  "$caseName" >> {
       |$caseBody
       |  }""".stripMargin

  override protected def generateExample(description: String, assertions: String): String =
    s"""    "$description" >> {
       |      $assertions
       |    }""".stripMargin

  override protected def generatePropertyExample(description: String, property: String): String =
    s"""    "$description" ! prop {
       |      $property
       |    }""".stripMargin

  override protected def generateAssert(actual: String, expected: String): String =
    s"""$actual must_== "$expected""""
}
