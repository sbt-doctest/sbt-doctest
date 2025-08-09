package com.github.tkawachi.doctest

import java.nio.file.Files
import java.nio.file.Path
import scala.meta.*
import scala.meta.contrib.*
import scala.meta.internal.Scaladoc
import scala.meta.internal.parsers.ScaladocParser
import scala.meta.parsers.Parse

/**
 * Extract examples from scala source.
 */
object ScaladocExtractor {

  def extract(scalaSource: String, dialect: Dialect): List[ScaladocComment] = {
    extractFromInput(Input.String(scalaSource), dialect)
  }
  def extractFromFile(file: Path, encoding: String, dialect: Dialect): List[ScaladocComment] = {
    val text = new String(Files.readAllBytes(file), encoding)
    // Workaround from https://github.com/scalameta/scalameta/issues/443#issuecomment-314797969
    val trimmedText = text.replace("\r\n", "\n")
    val input = Input.VirtualFile(file.toString, trimmedText)
    extractFromInput(input, dialect)
  }
  def extractFromInput(scalaSource: Input, dialect: Dialect): List[ScaladocComment] = {
    implicitly[Parse[Source]].apply(scalaSource, dialect) match {
      case Parsed.Success(tree) =>
        extractFromTree(tree)
      case error @ Parsed.Error(_, _, _) =>
        println(error.toString())
        Nil
    }
  }
  def extractFromTree(code: Tree): List[ScaladocComment] = {
    val comments = AssociatedComments(code)

    object NamedMember {
      def unapply(t: Tree): Option[String] = t match {
        case m: Member => Some(m.name.value)
        case v: Defn.Val => v.pats.collectFirst { case m: Member => m.name.value }
        case v: Defn.Var => v.pats.collectFirst { case m: Member => m.name.value }
        case _ => None
      }
    }

    def pkgOf(t: Tree): Option[String] =
      t.ancestors.collect {
        case pkg: Pkg => pkg.ref.toString
        case pkgObj: Pkg.Object => pkgObj.name.value
      } match {
        case Nil => None
        case names => Some(names.mkString("."))
      }

    def parsedScalaDocComment(t: Tree): Option[ScaladocComment] =
      (t, comments.leading(t).filter(_.isScaladoc).toList) match {
        // take only named members having single scaladoc comment
        case (NamedMember(name), List(scalaDocComment)) if name.nonEmpty =>
          ScaladocParser
            .parse(s"/*${scalaDocComment.value}*/")
            .toSeq
            .flatMap(_.para.flatMap(_.terms))
            .collect {
              case c: Scaladoc.CodeBlock =>
                Seq(c.code.map(_.trim).mkString("\n") -> true)
              case c: Scaladoc.MdCodeBlock =>
                Seq(c.code.map(_.trim).mkString("\n") -> true)
              case c: Scaladoc.Text =>
                c.parts.map(_.part.syntax).map(_ -> false)
            }
            .flatten match {
            case Nil => None
            case docTokens =>
              Some(
                ScaladocComment(
                  pkg = pkgOf(t),
                  symbol = name,
                  codeBlocks = docTokens.collect { case (c, true) if c.trim.nonEmpty => c.trim }.toList,
                  text = scalaDocComment.syntax,
                  lineNo = scalaDocComment.pos.startLine + 1 // startLine is 0 based, so compensating here
                )
              )
          }
        case _ => None
      }

    def extractAllCommentsFrom(t: Tree): List[ScaladocComment] =
      t.children.foldLeft(parsedScalaDocComment(t).toList) { case (extractedSoFar, childTree) =>
        extractedSoFar ::: extractAllCommentsFrom(childTree)
      }

    extractAllCommentsFrom(code)
  }
}
