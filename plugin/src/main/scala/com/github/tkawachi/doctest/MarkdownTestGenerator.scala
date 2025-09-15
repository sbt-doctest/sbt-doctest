package com.github.tkawachi.doctest

import java.io.File
import java.nio.file.Path

import scala.io.Source

import scala.annotation.tailrec

object MarkdownTestGenerator {

  val extractor = new MarkdownCodeblocksExtractor

  private def getPathComponents(path: Path): List[String] = {
    getPathComponentsRec(path, List.empty)
  }

  @tailrec
  private def getPathComponentsRec(path: Path, componentsSoFar: List[String]): List[String] = {
    val currentNameCount = path.getNameCount
    val newComponents = path.getName(currentNameCount - 1).toString :: componentsSoFar
    Option(path.getParent) match {
      case None =>
        newComponents
      case Some(parentPath) =>
        getPathComponentsRec(parentPath, newComponents)
    }
  }

  def apply(source: File, relativeTo: Path, testGen: TestGen, disambiguatingSuffix: String): Seq[TestSource] = {
    val contents = Source.fromFile(source).mkString
    val generatedClassName = getPathComponents(relativeTo.relativize(source.toPath))
      .map(_.capitalize)
      .mkString("")
      .filterNot(
        _ == '.'
      ) // This is for getting rid of periods in extensions that can mess up class names such as ".md"
      .++(disambiguatingSuffix)
    extractor
      .extract(contents)
      .flatMap(codeblock => CodeblockParser(codeblock).right.toOption.filter(_.components.nonEmpty))
      .groupBy(_.pkg)
      .map { case (pkg, examples) =>
        TestSource(pkg, generatedClassName, testGen.generate(generatedClassName, pkg, examples))
      }
      .toSeq
  }

}
