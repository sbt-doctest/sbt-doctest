package com.github.tkawachi.doctest

import utest._

object MarkdownCodeblocksExtractorSpec extends TestSuite {

  val tests = utest.Tests {

    val extractor = new MarkdownCodeblocksExtractor

    "extracts Markdown code block" - {
      val src = """
                  | # Header
                  |
                  |```scala
                  | scala> println("Hello, World!")
                  | Hello, World!
                  | ```""".stripMargin

      val actual = extractor.extract(src)
      val expected =
        Seq(
          MarkdownCodeblock(
            """```scala
              | scala> println("Hello, World!")
              | Hello, World!
              | ```""".stripMargin,
            4
          )
        )
      assert(expected == actual)
    }

    "extracts multiple Markdown code blocks" - {
      val src = """
                  | # Header
                  |
                  |```scala
                  | scala> println("Hello, World!")
                  | Hello, World!
                  | ```
                  |
                  |```scala
                  | scala> println("Good night, World!")
                  | Good night, World!
                  | ```""".stripMargin

      val actual = extractor.extract(src).size
      val expected = 2
      assert(expected == actual)
    }

  }

}
