package com.github.tkawachi.doctest

import com.github.tkawachi.doctest.StringUtil._

/**
 * Interface of a test generator.
 */
trait TestGen {
  def generate(
      basename: String,
      pkg: Option[String],
      parsedList: Seq[ParsedDoctest],
      onlyCodeblocks: Boolean = false
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
       |${testCasesLine(basename, parsedList)}
       |
       |}
       |""".stripMargin
  }

  protected def helperMethodsLine: String = indent(TestGen.helperMethods, "  ")

  protected def importsLine(parsedList: Seq[ParsedDoctest]): String

  protected def suiteDeclarationLine(basename: String, parsedList: Seq[ParsedDoctest]): String

  protected def testCasesLine(basename: String, parsedList: Seq[ParsedDoctest]): String =
    parsedList
      .map { doctest =>
        val testName = escape(s"$basename.scala:${doctest.lineNo}: ${doctest.symbol}")
        val testBody = doctest.components.map(componentLine(doctest.lineNo, _)).mkString("\n\n")
        generateTestCase(testName, testBody)
      }
      .mkString("\n\n")

  private def componentLine(firstLine: Int, component: DoctestComponent): String = {
    def absLine(lineNo: Int): Int = firstLine + lineNo - 1
    def mkStub(s: String): String = escape(truncate(s))

    component match {
      case Example(expr, expected, lineNo) =>
        val description = s"example at line ${absLine(lineNo)}: ${mkStub(expr)}"
        val typeTestLine = expected.tpe.fold("")(tpe => s"sbtDoctestTypeEquals($expr)(($expr): $tpe)")
        val assertTestLine = generateAssert(s"      sbtDoctestReplString($expr)", escape(expected.value))
        // !!! assertTestLine must be last b/c of Specs2 !!!
        generateExample(description, s"$typeTestLine\n$assertTestLine")
      case Property(prop, lineNo) =>
        val description = s"property at line ${absLine(lineNo)}: ${mkStub(prop)}"
        generatePropertyExample(description, prop)
      case Verbatim(code) =>
        indent(code, "    ")
    }
  }

  protected def generateTestCase(caseName: String, caseBody: String): String

  protected def generateExample(description: String, assertions: String): String

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
      |  val s = _root_.com.github.tkawachi.doctest.DoctestRuntime.replStringOf(any).init
      |  if (s.headOption == Some('\n')) s.tail else s
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
