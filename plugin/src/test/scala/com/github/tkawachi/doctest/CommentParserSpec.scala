package com.github.tkawachi.doctest

import utest._

object CommentParserSpec extends TestSuite {

  val tests = utest.Tests {

    import CommentParser.parse

    "Python style" - {
      "parses a single example" - {
        val comment =
          """ * >>> 1 + 2
            | * 3
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Example("1 + 2", TestResult("3"), 1))
        assert(expected == actual)
      }

      "parses a multi-lines expr" - {
        val comment =
          """ * >>> 1 + 2 +
            | * ... 3 +
            | * ... 4 + 5
            | * 15
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Example(s"1 + 2 +\n3 +\n4 + 5", TestResult("15"), 1))
        assert(expected == actual)
      }

      "doesn't parse a single example for different leading string" - {
        val comment =
          """ * >>> 1 + 2
            |*  3
          """.stripMargin
        val actual = parse(comment).get
        val expected = Nil
        assert(expected == actual)
      }

      "parses multiple examples" - {
        val comment =
          """ * >>> 1 + 2
            | * 3
            | *
            | * >>> "Hello," + " world"
            | * Hello, world
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("1 + 2", TestResult("3"), 1), Example("\"Hello,\" + \" world\"", TestResult("Hello, world"), 4))
        assert(expected == actual)
      }

      "parses a multi-line output" - {
        val comment =
          """ * >>> "abc\ndef"
            | * abc
            | * def
            | *
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("\"abc\\ndef\"", TestResult("abc\ndef", None), 1))
        assert(expected == actual)
      }

      "parses a <BLANKLINE>" - {
        val comment =
          """ * >>> "abc\n\ndef"
            | * abc
            | * <BLANKLINE>
            | * def
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("\"abc\\n\\ndef\"", TestResult("abc\n\ndef", None), 1))
        assert(expected == actual)
      }

      "parses }}} as an end of example" - {
        val comment =
          """ * >>> 1 + 1
            | * 2
            | * }}}
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("1 + 1", TestResult("2", None), 1))
        assert(expected == actual)
      }

      "parses }}} as an end of multiline example" - {
        val comment =
          """ * >>> "Hello\nWorld"
            | * Hello
            | * World
            | * }}}
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("\"Hello\\nWorld\"", TestResult("Hello\nWorld", None), 1))
        assert(expected == actual)
      }

      "parses multi-line outputs" - {
        val comment =
          """ * >>> "abc\ndef"
            | * abc
            | * def
            | *
            | * >>> " abc\ndef"
            | * " abc
            | * def"
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(
            Example("\"abc\\ndef\"", TestResult("abc\ndef", None), 1),
            Example("\" abc\\ndef\"", TestResult("\" abc\ndef\"", None), 5)
          )
        assert(expected == actual)
      }

      "parses an import line" - {
        val comment =
          """ * >>> import abc.def
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim("import abc.def"))
        assert(expected == actual)
      }

      "parses a single-line assignment" - {
        val comment =
          """ * >>> val xs = List(1, 2, 3)
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim("val xs = List(1, 2, 3)"))
        assert(expected == actual)
      }

      "parses a multi-line assignment" - {
        val comment =
          """ * >>> val xs = List(
            | * ... 1,
            | * ... 2,
            | * ... 3)
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim(s"val xs = List(\n1,\n2,\n3)"))
        assert(expected == actual)
      }
    }

    "Scala repl style" - {
      "parses a single example" - {
        val comment =
          """ * scala> 1 + 2
            | * res0: Int = 3
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Example("1 + 2", TestResult("3", Some("Int")), 1))
        assert(expected == actual)
      }

      "parses a multi-lines expr" - {
        val comment =
          """ * scala> 1 + 2 +
            | *      | 3 +
            | *      | 4 + 5
            | * res0: Int = 15
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Example(s"1 + 2 +\n3 +\n4 + 5", TestResult("15", Some("Int")), 1))
        assert(expected == actual)
      }

      "doesn't parse a single example for different leading string" - {
        val comment =
          """ * scala> 1 + 2
            |*  3
          """.stripMargin
        val actual = parse(comment).get
        val expected = Nil
        assert(expected == actual)
      }

      "parses multiple examples" - {
        val comment =
          """ * scala> 1 + 2
            | * res1: Int = 3
            | *
            | * scala> "Hello," + " world"
            | * res2: String = Hello, world
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(
            Example("1 + 2", TestResult("3", Some("Int")), 1),
            Example("\"Hello,\" + \" world\"", TestResult("Hello, world", Some("String")), 4)
          )
        assert(expected == actual)
      }

      "parses a multi-line output" - {
        val comment =
          """ * scala> "abc\ndef"
            | * res0: String =
            | * abc
            | * def
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("\"abc\\ndef\"", TestResult("abc\ndef", Some("String")), 1))
        assert(expected == actual)
      }

      "parses a <BLANKLINE>" - {
        val comment =
          """ * scala> "abc\n\ndef"
            | * res0: String =
            | * abc
            | * <BLANKLINE>
            | * def
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("\"abc\\n\\ndef\"", TestResult("abc\n\ndef", Some("String")), 1))
        assert(expected == actual)
      }

      "parses }}} as an end of example" - {
        val comment =
          """ * scala> 1 + 1
            | * res0: Int = 2
            | * }}}
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("1 + 1", TestResult("2", Some("Int")), 1))
        assert(expected == actual)
      }

      "parses }}} as an end of multiline example" - {
        val comment =
          """ * scala> "Hello\nWorld"
            | * res0: String =
            | * Hello
            | * World
            | * }}}
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("\"Hello\\nWorld\"", TestResult("Hello\nWorld", Some("String")), 1))
        assert(expected == actual)
      }

      "parses multi-line outputs" - {
        val comment =
          """ * scala> "abc\ndef"
            | * res0: String =
            | * abc
            | * def
            | *
            | * scala> " abc\ndef"
            | * res2: String =
            | * " abc
            | * def"
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(
            Example("\"abc\\ndef\"", TestResult("abc\ndef", Some("String")), 1),
            Example("\" abc\\ndef\"", TestResult("\" abc\ndef\"", Some("String")), 6)
          )
        assert(expected == actual)
      }

      "parses an import line" - {
        val comment =
          """ * scala> import abc.def
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim("import abc.def"))
        assert(expected == actual)
      }

      "parses a result with a parametric type" - {
        val comment =
          """ * scala> List(1)
            | * res1: List[Int] = List(1)
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Example("List(1)", TestResult("List(1)", Some("List[Int]")), 1))
        assert(expected == actual)
      }

      "parses a type with a colon" - {
        val comment =
          """ * scala> =:=
            | * res0: =:=.type = scala.Predef...
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Example("=:=", TestResult("scala.Predef...", Some("=:=.type")), 1))
        assert(expected == actual)
      }

      "parses a type with equal signs" - {
        val comment =
          """ * scala> ==>>.empty[Int, String]
            | * res0: ==>>[Int, String] = ???
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("==>>.empty[Int, String]", TestResult("???", Some("==>>[Int, String]")), 1))
        assert(expected == actual)
      }

      "parses a type which only contains equal signs" - {
        // This example would compile with this alias definition:
        // type == = String
        val comment =
          """ * scala> "Hello\nWorld": ==
          | * res0: == =
          | * Hello
          | * World
        """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Example("\"Hello\\nWorld\": ==", TestResult("Hello\nWorld", Some("==")), 1))
        assert(expected == actual)
      }

      "parses a single-line assignment" - {
        val comment =
          """ * scala> var xs = List(1, 2, 3)
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim("var xs = List(1, 2, 3)"))
        assert(expected == actual)
      }

      "parses a multi-line assignment" - {
        val comment =
          """ * scala> var ys = List(
            | *      |   1,
            | *      |   2,
            | *      |   3)
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim(s"var ys = List(\n  1,\n  2,\n  3)"))
        assert(expected == actual)
      }

      "parses class definitions with annotations" - {
        val comment =
          """ * scala> @deprecated("foo", "bar") case class Foo(x: Int)
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim("@deprecated(\"foo\", \"bar\") case class Foo(x: Int)"))
        assert(expected == actual)
      }

      "parses an example that starts with a verbatim keyword" - {
        val comment =
          """ * scala> val value = 1
            | * scala> value + 1
            | * res0: Int = 2
          """.stripMargin
        val actual = parse(comment).get
        val expected =
          List(Verbatim("val value = 1"), Example("value + 1", TestResult("2", Some("Int")), 2))
        assert(expected == actual)
      }
    }

    "Property based" - {
      "parses a single property" - {
        val comment =
          """ * prop> (i: Int) => i + i == i * 2
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Property("""(i: Int) => i + i == i * 2""", 1))
        assert(expected == actual)
      }

      "parses a muti-lines property" - {
        val comment =
          """ * prop> (i: Int) =>
            | *     | i + i == (i *
            | *     | 2)
            | *
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Property(s"(i: Int) =>\ni + i == (i *\n2)", 1))
        assert(expected == actual)
      }

      "parses an import line" - {
        val comment =
          """ * prop> import abc.def
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim("import abc.def"))
        assert(expected == actual)
      }

      "parses a single-line assignment" - {
        val comment =
          """ * prop> var xs = List(1, 2, 3)
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim("var xs = List(1, 2, 3)"))
        assert(expected == actual)
      }

      "parses a multi-line assignment" - {
        val comment =
          """ * prop> var ys = List(
            | *     |  1,
            | *     |   2,
            | *     |    3)
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim(s"var ys = List(\n 1,\n  2,\n   3)"))
        assert(expected == actual)
      }

      "parses a multi-line assignment whose first line is only a keyword" - {
        val comment =
          """ * prop> def
            | *     | x = 1
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Verbatim(s"def\nx = 1"))
        assert(expected == actual)
      }
    }

    "No examples" - {
      "returns None" - {
        val comment =
          """/**
            | * Test comment.
            | */
          """.stripMargin
        val actual = parse(comment).get
        val expected = List.empty
        assert(expected == actual)
      }
    }

    "Markdown comments" - {
      "python style" - {
        val comment =
          """```scala
            |>>> 1 + 2
            |3
            |```
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Example("1 + 2", TestResult("3"), 2))
        assert(expected == actual)
      }

      "repl style" - {
        val comment =
          """```scala
            |scala> 1 + 2
            |res0: Int = 3
            |```
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Example("1 + 2", TestResult("3", Some("Int")), 2))
        assert(expected == actual)
      }

      "repl style with import and example" - {
        val comment =
          """```scala
            |scala> import scala.util.Success
            |import scala.util.Success
            |
            |scala> Success(1 + 2)
            |res0: scala.util.Try[Int] = Success(3)
            |```
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(
          Verbatim("import scala.util.Success"),
          Example("Success(1 + 2)", TestResult("Success(3)", Some("scala.util.Try[Int]")), 5)
        )
        assert(expected == actual)
      }

      "property style" - {
        val comment =
          """```scala
            |prop> (i: Int) => i + i == i * 2
            |```
          """.stripMargin
        val actual = parse(comment).get
        val expected = List(Property("""(i: Int) => i + i == i * 2""", 2))
        assert(expected == actual)
      }
    }

  }

}
