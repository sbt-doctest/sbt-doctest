package com.github.tkawachi.doctest

import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import scala.meta.Input
import utest._
import scala.meta.dialects

object ScaladocExtractorSpec extends TestSuite {

  val tests = utest.Tests {

    def extractFromFile(path: String) =
      ScaladocExtractor.extractFromFile(
        Paths.get("plugin/" + path),
        StandardCharsets.UTF_8.name(),
        dialects.Scala213Source3
      )

    "extracts from Test.scala" - {
      val actual = extractFromFile("src/test/resources/Test.scala")
      val expected =
        List(
          ScaladocComment(None, "Test", Nil, "/**\n * Test class\n */", 1),
          ScaladocComment(
            None,
            "f",
            Nil,
            """/**
              |   * A function.
              |   *
              |   * >>> new Test().f(10)
              |   * 20
              |   *
              |   * >>> "hello, " + "world!"
              |   * hello, world!
              |   */""".stripMargin,
            5
          ),
          ScaladocComment(
            None,
            "+=",
            Nil,
            """/** Ascii method
              |   * scala> new Test() += 1
              |   * 2
              |   */""".stripMargin,
            16
          ),
          ScaladocComment(
            None,
            "x",
            Nil,
            """/**
              |    * Doc on val
              |    */""".stripMargin,
            22
          ),
          ScaladocComment(
            None,
            "z",
            Nil,
            """/**
              |    * Doc on var
              |    */""".stripMargin,
            27
          )
        )
      assert(expected == actual)
    }

    "extracts from RootPackage.scala" - {
      val actual = extractFromFile("src/test/resources/RootPackage.scala")
      val expected =
        List(
          ScaladocComment(None, "Root1", Nil, "/** Class comment */", 1),
          ScaladocComment(None, "method", Nil, "/** Method comment */", 3),
          ScaladocComment(Some("a1"), "A1", Nil, "/** A1 */", 8),
          ScaladocComment(Some("a1.b1"), "B1", Nil, "/** B1 */", 11),
          ScaladocComment(None, "Root2", Nil, "/** Root2 */", 16),
          ScaladocComment(None, "method2", Nil, "/** Method2 comment */", 20),
          ScaladocComment(None, "IntAlias", Nil, "/** Type alias */", 23)
        )
      assert(expected == actual)
    }

    "extracts from Package.scala" - {
      val actual = extractFromFile("src/test/resources/Package.scala")
      val expected =
        List(
          ScaladocComment(Some("some.pkg"), "Root1", Nil, "/** Class comment */", 3),
          ScaladocComment(Some("some.pkg"), "method", Nil, "/** Method comment */", 5),
          ScaladocComment(Some("some.pkg.a1"), "A1", Nil, "/** A1 */", 10),
          ScaladocComment(Some("some.pkg.a1.b1"), "B1", Nil, "/** B1 */", 13),
          ScaladocComment(Some("some.pkg"), "Root2", Nil, "/** Root2 */", 18)
        )
      assert(expected == actual)
    }

    "extracts only Scaladocs with some parseable text" - {
      val source =
        """
          |package all_scaladocs_without_code
          |
          |/**
          | * @version 123
          | * @since 1953
          | * @constructor aaa
          | */
          |class GotNothing {
          |
          |  /**
          |
          |  */
          |  val a = 0
          |
          |  /**
          |   *
          |   */
          |  var b = 0
          |
          |  /**
          |   */
          |  def c = 0
          |
          |  /**
          |  *
          |  *
          |  *
          |  */
          |  def d = 0
          |
          |  /**
          |   * @param i an Int
          |   * @return an Int
          |   */
          |  def e(i: Int) = 0
          |
          |  /**
          |   * @deprecated for ever
          |   */
          |  def f = 0
          |
          |  /**
          |   * @todo write code example like
          |   *    require(g == 0)
          |   */
          |  def g = 0
          |
          |  /**
          |   *
          |   */
          |  def this(i: Int) = this()
          |}
        """.stripMargin

      val actual = ScaladocExtractor.extract(source, dialects.Scala213Source3)
      val expected = Nil
      assert(expected == actual)
    }

    "parse errors are printed to stdout" - {
      val input = Input.VirtualFile("filename.scala", "object Main {")
      val out = new ByteArrayOutputStream()
      val obtained = Console.withOut(new PrintStream(out)) {
        ScaladocExtractor.extractFromInput(input, dialects.Scala213Source3)
      }
      assert(obtained == Nil)
      val obtainedOut = out.toString(StandardCharsets.UTF_8.name).trim
      val expectedOut =
        """
          |filename.scala:1: error: `}` expected but `end of file` found
          |object Main {
          |             ^
        """.stripMargin.trim
      assert(obtainedOut == expectedOut)
    }

    "extracts from CodeExamples.scala" - {
      val actual = extractFromFile("src/test/resources/CodeExamples.scala")
      val expected =
        List(
          ScaladocComment(
            Some("examples.inda.house"),
            "ff",
            List("val i = ff(5)", "require(i == 5)"),
            """/**
            |    * This method is very nifty and can be used like this:
            |    *
            |    * {{{
            |    *   val i = ff(5)
            |    * }}}
            |    *
            |    * `i` should be equal to 5. Let's check
            |    *
            |    * {{{
            |    *   require(i == 5)
            |    * }}}
            |    *
            |    *
            |    * Works!
            |    */""".stripMargin,
            5
          ),
          ScaladocComment(
            Some("examples.inda.house"),
            "fff",
            List("""val i = fff(5)
                |i match {
                |case 10 => "yep!"
                |case _ => "boo"
                |}""".stripMargin),
            """/**
            |    * Here's a multiline code block
            |    *
            |    * {{{
            |    *   val i = fff(5)
            |    *   i match {
            |    *     case 10 => "yep!"
            |    *     case _ => "boo"
            |    *   }
            |    * }}}
            |    */""".stripMargin,
            23
          ),
          ScaladocComment(
            Some("examples.inda.house"),
            "i_love_py",
            Nil,
            """/**
            |    * call me twice
            |    * >>> i_love_py(1 + 2 +
            |    * ... 3 +
            |    * ... 4 + 5
            |    * 15
            |    */""".stripMargin,
            36
          )
        )
      assert(expected == actual)
    }

    "extracts from package.scala" - {
      val actual = extractFromFile("src/test/resources/package_object/package.scala")
      val expected = List(
        ScaladocComment(
          Some("outer"),
          "package_object",
          Nil,
          """/**
            |  * Package object scaladocs are important as well
            |  */""".stripMargin,
          3
        ),
        ScaladocComment(
          Some("outer.package_object"),
          "five",
          Nil,
          """/**
            |    * package objects should do
            |    */""".stripMargin,
          8
        )
      )
      assert(expected == actual)
    }
  }
}
