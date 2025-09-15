package com.github.tkawachi.doctest

import scala.util.matching.Regex

class MarkdownCodeblocksExtractor {

  private val regex = """(?ms)^```scala(.*?)```""".r

  def extract(source: String): Seq[MarkdownCodeblock] = {
    val blocks =
      for {
        code <- regex.findAllMatchIn(source)
      } yield MarkdownCodeblock(code.toString, line(source, code))

    blocks.toSeq
  }

  private def line(source: String, m: Regex.Match): Int =
    source.substring(m.start).split("\r\n|\r|\n").length

}
