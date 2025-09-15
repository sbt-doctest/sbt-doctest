package com.github.tkawachi.doctest

import java.io.File
import org.apache.commons.io.FilenameUtils
import org.apache.commons.text.StringEscapeUtils.unescapeHtml4
import scala.meta.Dialect

object ScaladocTestGenerator {

  private def decodeHtml(comment: ScaladocComment) =
    comment.copy(codeBlocks = comment.codeBlocks.map(unescapeHtml4), text = unescapeHtml4(comment.text))

  /**
   * Generates test source code from scala source file.
   */
  def apply(
      srcFile: File,
      srcEncoding: String,
      testGen: TestGen,
      decodeHtmlEnabled: Boolean,
      onlyCodeBlocksMode: Boolean,
      dialect: Dialect
  ): Seq[TestSource] = {
    val basename = FilenameUtils.getBaseName(srcFile.getName)
    ScaladocExtractor
      .extractFromFile(srcFile.toPath, srcEncoding, dialect)
      .map(comment => if (decodeHtmlEnabled) decodeHtml(comment) else comment)
      .flatMap { comment =>
        val docTest =
          if (onlyCodeBlocksMode)
            if (comment.codeBlocks.isEmpty)
              None
            else
              Some(ParsedDoctest(comment.pkg, comment.symbol, comment.codeBlocks.map(Verbatim.apply), comment.lineNo))
          else
            CommentParser(comment).right.toOption
        docTest.filter(_.components.nonEmpty)
      }
      .groupBy(_.pkg)
      .map { case (pkg, examples) =>
        TestSource(pkg, basename, testGen.generate(basename, pkg, examples))
      }
      .toSeq
  }

  def findEncoding(scalacOptions: Seq[String]): Option[String] = scalacOptions match {
    case Seq() => None
    case Seq(_, tail @ _*) => scalacOptions.zip(tail).collectFirst { case ("-encoding", enc) => enc }
  }
}
