package com.github.tkawachi.doctest

import com.github.tkawachi.doctest.StringUtil.*

/**
 * Interface of a test generator.
 */
trait TestGen {
  def generate(
      basename: String,
      pkg: Option[String],
      parsedList: Seq[ParsedDoctest],
      onlyCodeblocks: Boolean
  ): String = {
    val pkgLine = pkg.fold("")(p => s"package $p")
    s"""$pkgLine
       |
       |${importsLine(parsedList)}
       |
       |${suiteDeclarationLine(basename, parsedList)} {
       |
       |$helperMethodsLine
       |
       |${testCasesLine(basename, parsedList, onlyCodeblocks)}
       |
       |}
       |""".stripMargin
  }

  protected def helperMethodsLine: String = indent(TestGen.helperMethods, "  ")

  protected def importsLine(parsedList: Seq[ParsedDoctest]): String

  protected def suiteDeclarationLine(basename: String, parsedList: Seq[ParsedDoctest]): String

  protected def testCasesLine(basename: String, parsedList: Seq[ParsedDoctest], onlyCodeblocks: Boolean): String =
    parsedList
      .map { doctest =>
        val testName = escape(s"$basename.scala:${doctest.lineNo}: ${doctest.symbol}")
        val testBody =
          doctest.components.map(componentLine(doctest.lineNo, _, onlyCodeblocks: Boolean)).mkString("\n\n")
        generateTestCase(testName, testBody, onlyCodeblocks)
      }
      .mkString("\n\n")

  private def absLine(firstLine: Int, lineNo: Int): Int = firstLine + lineNo - 1
  private def mkStub(s: String): String = escape(truncate(s))

  protected def componentLineExample(firstLine: Int, component: Example, onlyCodeblocks: Boolean): String = {
    val description = s"example at line ${absLine(firstLine, component.lineNo)}: ${mkStub(component.expr)}"
    val typeTestLine =
      component.expected.tpe.fold("")(tpe => s"sbtDoctestTypeEquals(${component.expr})((${component.expr}): $tpe)")
    val assertTestLine =
      generateAssert(s"      sbtDoctestReplString(${component.expr})", escape(component.expected.value))
    // !!! assertTestLine must be last b/c of Specs2 !!!
    generateExample(description, s"$typeTestLine\n$assertTestLine", onlyCodeblocks)
  }

  protected def componentLineProperty(firstLine: Int, component: Property): String = {
    val description = s"property at line ${absLine(firstLine, component.lineNo)}: ${mkStub(component.prop)}"
    generatePropertyExample(description, component.prop)
  }

  protected def componentLineVerbatim(firstLine: Int, component: Verbatim): String = {
    indent(component.code, "    ")
  }

  protected def componentLine(firstLine: Int, component: DoctestComponent, onlyCodeblocks: Boolean): String = {
    component match {
      case x: Example =>
        componentLineExample(firstLine, x, onlyCodeblocks)
      case x: Property =>
        componentLineProperty(firstLine, x)
      case x: Verbatim =>
        componentLineVerbatim(firstLine, x)
    }
  }

  protected def generateTestCase(caseName: String, caseBody: String, onlyCodeblocks: Boolean): String

  protected def generateExample(description: String, assertions: String, onlyCodeblocks: Boolean): String

  protected def generatePropertyExample(description: String, property: String): String

  protected def generateAssert(actual: String, expected: String): String

}

object TestGen {

  /**
   * Helper methods which will be embedded in generated tests.
   */
  val helperMethods =
    """def sbtDoctestTypeEquals[A](a1: => A)(a2: => A): _root_.scala.Unit = {
      |  val _ = () => (a1, a2)
      |}
      |def sbtDoctestReplString(any: _root_.scala.Any): _root_.scala.Predef.String = {
      |  _root_.com.github.tkawachi.doctest.DoctestRuntime.replStringOf(any)
      |}""".stripMargin

  def importArbitrary(examples: Seq[ParsedDoctest]): String =
    if (containsProperty(examples)) "import _root_.org.scalacheck.Arbitrary._" else ""

  def containsExample(examples: Seq[ParsedDoctest]): Boolean =
    examples.exists(_.components.exists {
      case _: Example => true
      case _ => false
    })

  def containsProperty(examples: Seq[ParsedDoctest]): Boolean =
    examples.exists(_.components.exists {
      case _: Property => true
      case _ => false
    })
}
