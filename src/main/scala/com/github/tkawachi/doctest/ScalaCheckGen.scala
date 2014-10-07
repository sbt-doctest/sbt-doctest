package com.github.tkawachi.doctest

import com.github.tkawachi.doctest.StringUtil.{ escapeDoubleQuote => escapeDQ }

object ScalaCheckGen extends TestGen {

  def generate(basename: String, pkg: Option[String], parsedList: Seq[ParsedDoctest]): String = {
    val pkgLine = pkg.fold("")(p => s"package $p")
    s"""$pkgLine
       |
       |import org.scalacheck.Arbitrary._
       |import org.scalacheck.Prop._
       |
       |object ${basename}Doctest
       |    extends org.scalacheck.Properties("${escapeDQ(basename)}.scala") {
       |
       |  def sbtDoctestTypeEquals[A](a1: => A)(a2: => A) = ()
       |  def sbtDoctestReplString(any: Any): String = scala.runtime.ScalaRunTime.replStringOf(any, 1000).init
       |
       |${parsedList.map(generateExample(basename, _)).mkString("\n\n")}
       |}
       |""".stripMargin
  }

  def generateExample(basename: String, parsed: ParsedDoctest): String = {
    s"""  include(new org.scalacheck.Properties("L${parsed.lineNo}:${parsed.symbol}") {
       |${parsed.components.map(gen(parsed.lineNo, _)).mkString("\n\n")}
       |  })""".stripMargin
  }

  def gen(firstLine: Int, component: DoctestComponent): String =
    component match {
      case Example(expr, expected, _) =>
        val typeTest = expected.tpe.fold("")(tpe => genTypeTest(expr, tpe))
        s"""    property("${componentDescription(component, firstLine)}") = {
           |      ${typeTest}sbtDoctestReplString($expr) == ("${escapeDQ(expected.value)}")
           |    }""".stripMargin
      case Property(prop, _) =>
        s"""    property("${componentDescription(component, firstLine)}") = org.scalacheck.Prop.forAll {
           |      $prop
           |    }""".stripMargin
      case Verbatim(code) =>
        StringUtil.indent(code, "    ")
    }

  def genTypeTest(expr: String, expectedType: String): String =
    s"      sbtDoctestTypeEquals($expr)(($expr): $expectedType)\n"

  def componentDescription(comp: DoctestComponent, firstLine: Int): String = {
    def absLine(lineNo: Int): Int = firstLine + lineNo - 1
    def mkStub(s: String): String = escapeDQ(StringUtil.truncate(s))

    comp match {
      case Example(expr, _, lineNo) =>
        s"example at line ${absLine(lineNo)}: ${mkStub(expr)}"
      case Property(prop, lineNo) =>
        s"property at line ${absLine(lineNo)}: ${mkStub(prop)}"
      case _ => ""
    }
  }
}
