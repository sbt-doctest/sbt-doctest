package com.github.tkawachi.doctest

import com.github.tkawachi.doctest.DoctestPlugin.DoctestTestFramework
import com.github.tkawachi.doctest.DoctestPlugin.DoctestTestFramework._
import sbt.Keys.{Classpath, moduleID}
import sbt.internal.util.MessageOnlyException
import sbt.librarymanagement.ModuleID
import sbt.librarymanagement.CrossVersion

import scala.util.Try

object TestGenResolver {
  def resolve(framework: DoctestTestFramework, scalaTestVersion: Option[String]): TestGen = {
    framework match {
      case MicroTest => MicroTestGen
      case Minitest => MinitestGen
      case ScalaTest =>
        scalaTestVersion
          .flatMap {
            _.split('.').take(2).flatMap(s => Try(s.toInt).toOption) match {
              case Array(major, minor) =>
                if (major < 3 || major == 3 && minor == 0) {
                  Some(ScalaTest30Gen)
                } else {
                  Some(ScalaTest31Gen)
                }
              case _ =>
                None
            }
          }
          .getOrElse(throw new MessageOnlyException(s"Unexpected doctestScalaTestVersion version: $scalaTestVersion"))
      case Specs2 => Specs2TestGen
      case ScalaCheck => ScalaCheckGen
      case Munit => MunitGen
    }
  }

  @deprecated(message = "use findScalaTestVersionFromScalaBinaryVersion", since = "0.9.8")
  def findScalaTestVersion(testClasspath: Classpath, scalaVersion: String): Option[String] = {
    val scalaBinaryVersion = CrossVersion.binaryScalaVersion(scalaVersion)
    findScalaTestVersionFromScalaBinaryVersion(testClasspath, scalaBinaryVersion)
  }

  def findScalaTestVersionFromScalaBinaryVersion(
      testClasspath: Classpath,
      scalaBinaryVersion: String
  ): Option[String] =
    DoctestPlugin.findScalaTestVersionFromScalaBinaryVersion(
      testClasspath = testClasspath,
      scalaBinaryVersion = scalaBinaryVersion
    )

  private[doctest] def detectScalaTestVersion(moduleID: ModuleID, scalaBinVersion: String): Option[String] = {
    // From ScalaTest 3.2, scalatest-core is used.
    if (
      moduleID.organization == "org.scalatest" &&
      (moduleID.name == s"scalatest_$scalaBinVersion" || moduleID.name == s"scalatest-core_$scalaBinVersion")
    )
      Some(moduleID.revision)
    else None
  }
}
