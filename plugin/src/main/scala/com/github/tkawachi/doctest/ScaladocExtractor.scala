package com.github.tkawachi.doctest

import java.nio.file.Files
import java.nio.file.Path
import scala.meta._
import scala.meta.contrib._
import scala.meta.parsers.Parse

/**
 * Extract examples from scala source.
 */
object ScaladocExtractor {

  private val meaningfulDocTokenKinds: Set[DocToken.Kind] =
    Set(DocToken.CodeBlock, DocToken.Description, DocToken.Example)

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
          scalaDocComment.docTokens.flatMap(_.filter(dt => meaningfulDocTokenKinds(dt.kind)) match {
            case Nil => None
            case docTokens =>
              Some(
                ScaladocComment(
                  pkg = pkgOf(t),
                  symbol = name,
                  codeBlocks = docTokens.collect {
                    case DocToken(DocToken.CodeBlock, _, Some(body)) if body.trim.nonEmpty => body
                  },
                  text = scalaDocComment.syntax,
                  lineNo = scalaDocComment.pos.startLine + 1 // startLine is 0 based, so compensating here
                )
              )
          })
        case _ => None
      }

    def extractAllCommentsFrom(t: Tree): List[ScaladocComment] =
      t.children.foldLeft(parsedScalaDocComment(t).toList) { case (extractedSoFar, childTree) =>
        extractedSoFar ::: extractAllCommentsFrom(childTree)
      }

    extractAllCommentsFrom(code)
  }
}
