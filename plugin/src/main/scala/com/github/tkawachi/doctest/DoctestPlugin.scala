package com.github.tkawachi.doctest

import java.nio.file.Path

import org.apache.commons.io.FilenameUtils
import org.scalafmt.interfaces.{Scalafmt, ScalafmtSession}
import sbt.Keys._
import sbt.{given, _}
import sbt.internal.io.Source
import sbt.io.{AllPassFilter, NothingFilter}
import sbt.plugins.JvmPlugin
import scala.meta.Dialect
import scala.meta.dialects

/**
 * Sbt plugin for doctest.
 *
 * It enables doctest like following.
 *
 * {{{
 * # In Python style
 * >>> 1 + 1
 * 2
 *
 * # In scala repl style
 * scala> 1 + 10
 * res0: Int = 11
 * }}}
 */
object DoctestPlugin extends AutoPlugin with DoctestCompat {
  self =>

  sealed abstract class DoctestTestFramework

  object DoctestTestFramework {
    case object Specs2 extends DoctestTestFramework
    case object ScalaTest extends DoctestTestFramework
    case object ScalaCheck extends DoctestTestFramework
    case object MicroTest extends DoctestTestFramework
    case object Minitest extends DoctestTestFramework
    case object Munit extends DoctestTestFramework
  }
  import DoctestTestFramework._

  override def trigger: PluginTrigger = allRequirements

  override def projectSettings: Seq[Setting[?]] = doctestSettings

  object autoImport {
    val doctestTestFramework = settingKey[DoctestTestFramework](
      "Test framework. Specify MicroTest, Minitest, Munit, ScalaTest, ScalaCheck or Specs2."
    )
    val doctestScalaTestVersion =
      settingKey[Option[String]]("Explicitly specify ScalaTest version to generate test files (ex. Some(\"3.2.0\")).")
    val doctestMarkdownEnabled = settingKey[Boolean]("Whether to compile markdown into doctests.")
    val doctestMarkdownPathFinder = settingKey[PathFinder]("PathFinder to find markdown to test.")
    val doctestGenTests = taskKey[Seq[File]]("Generates test files.")
    val doctestDecodeHtmlEntities = settingKey[Boolean]("Whether to decode HTML entities.")
    val doctestIgnoreRegex =
      settingKey[Option[String]]("All sources that match the regex will not be used for tests generation")
    val doctestOnlyCodeBlocksMode = settingKey[Boolean]("Whether to treat all code in Scaladocs as pure code blocks.")
    val doctestDialect = settingKey[Dialect]("dialect")
    val doctestScalafmt = settingKey[Boolean]("format generated code by scalafmt")

    val DoctestTestFramework = self.DoctestTestFramework
  }

  import autoImport._

  private val createScalafmtInstance: Def.Initialize[Task[Option[ScalafmtSession]]] =
    Def.task {
      if (doctestScalafmt.value) {
        // https://github.com/scalameta/sbt-scalafmt/blob/e59fc02237374e6/plugin/src/main/scala/org/scalafmt/sbt/ScalafmtPlugin.scala#L42-L45
        TaskKey[File]("scalafmtConfig").?.value.filter(_.isFile).map { conf =>
          Scalafmt
            .create(this.getClass.getClassLoader)
            .createSession(conf.toPath)
        }
      } else {
        None
      }
    }

  private def doctestScaladocGenTests(
      sources: Seq[File],
      testGen: TestGen,
      decodeHtml: Boolean,
      onlyCodeBlocksMode: Boolean,
      scalacOptions: Seq[String],
      dialect: Dialect
  ) = {
    val srcEncoding = ScaladocTestGenerator.findEncoding(scalacOptions).getOrElse("UTF-8")
    sources
      .filter(_.ext == "scala")
      .flatMap(ScaladocTestGenerator(_, srcEncoding, testGen, decodeHtml, onlyCodeBlocksMode, dialect))
  }

  private def doctestMarkdownGenTests(finder: PathFinder, baseDirectoryPath: Path, testGen: TestGen) = {
    finder
      .filter(!_.isDirectory)
      .get()
      .sortBy(_.getCanonicalPath)
      .zipWithIndex
      .flatMap { case (file, disambiguatingIdx) =>
        MarkdownTestGenerator(file, baseDirectoryPath, testGen, disambiguatingIdx.toString)
      }
  }

  /**
   * Settings for test Generation.
   */
  val doctestGenSettings = Seq(
    libraryDependencies ++= {
      // https://github.com/portable-scala/sbt-platform-deps/blob/1b9d7ef512546e8/src/main/scala/org/portablescala/sbtplatformdeps/PlatformDepsPlugin.scala#L20-L21
      scalaBinaryVersion.value match {
        case "2.12" | "2.13" | "3" =>
          SettingKey[CrossVersion]("platformDepsCrossVersion").?.value match {
            case Some(c) =>
              Seq(
                ("io.github.sbt-doctest" %% "doctest-runtime" % DoctestBuildInfo.version).cross(c)
              )
            case None =>
              Seq(
                "io.github.sbt-doctest" %% "doctest-runtime" % DoctestBuildInfo.version
              )
          }
        case _ =>
          Nil
      }
    },
    doctestScalafmt := true,
    doctestTestFramework := (doctestTestFramework ?? ScalaCheck).value,
    doctestScalaTestVersion := (doctestScalaTestVersion ?? None).value,
    doctestDecodeHtmlEntities := (doctestDecodeHtmlEntities ?? false).value,
    doctestOnlyCodeBlocksMode := (doctestOnlyCodeBlocksMode ?? false).value,
    doctestMarkdownEnabled := (doctestMarkdownEnabled ?? false).value,
    doctestMarkdownPathFinder := baseDirectory.value * "*.md",
    testFrameworks += new TestFramework("utest.runner.Framework"),
    doctestIgnoreRegex := None,
    doctestDialect := {
      scalaBinaryVersion.value match {
        case "2.10" =>
          dialects.Scala210
        case "2.11" =>
          dialects.Scala211
        case "2.12" =>
          dialects.Scala212Source3
        case "2.13" =>
          dialects.Scala213Source3
        case "3" =>
          dialects.Scala3
        case _ =>
          dialects.Scala213Source3
      }
    },
    doctestGenTests := Def.uncached {
      (Test / managedSourceDirectories).value.headOption match {
        case None =>
          streams.value.log.warn("DocTest: `Test/managedSourceDirectories` is empty. Failed to generate tests")
          Seq.empty
        case Some(testDir) =>
          val scalaTestVersion = doctestScalaTestVersion.value
            .orElse(
              TestGenResolver.findScalaTestVersionFromScalaBinaryVersion(
                managedJars(Test, classpathTypes.value, update.value, fileConverter.value),
                scalaBinaryVersion.value
              )
            )
          val testGen = TestGenResolver.resolve(doctestTestFramework.value, scalaTestVersion)

          val sourceFiles = (Compile / unmanagedSources).value ++ (Compile / managedSources).value
          val log = streams.value.log
          log.debug(s"DocTest: Applying ignore pattern [${doctestIgnoreRegex.value}] to exclude matching sources...")
          val filteredSourceFiles =
            doctestIgnoreRegex.value.fold(sourceFiles) { regex =>
              val IgnoreRegex = regex.r
              sourceFiles.filterNot { f =>
                FilenameUtils.normalize(f.getCanonicalPath, true) match {
                  case ign @ IgnoreRegex(_*) =>
                    log.debug(s"DocTest: Excluding source file: $ign")
                    true
                  case use =>
                    log.debug(s"DocTest: Using source file: $use")
                    false
                }
              }
            }
          val scaladocTests = doctestScaladocGenTests(
            filteredSourceFiles,
            testGen,
            doctestDecodeHtmlEntities.value,
            doctestOnlyCodeBlocksMode.value,
            (Compile / scalacOptions).value,
            doctestDialect.value
          )

          val pathFinder = doctestMarkdownPathFinder.value
          val baseDirectoryPath = baseDirectory.value.toPath
          val markdownTests = if (doctestMarkdownEnabled.value) {
            doctestMarkdownGenTests(pathFinder, baseDirectoryPath, testGen)
          } else {
            Seq.empty
          }

          (scaladocTests ++ markdownTests)
            .groupBy(r => r.pkg -> r.basename)
            .flatMap { case ((pkg, basename), results) =>
              if (results.nonEmpty) {
                log.debug(s"format ${results.size} files")
              }
              results.zipWithIndex.map { case (result, idx) =>
                val writeBasename = if (idx == 0) basename else basename + idx
                val writeDir = pkg.fold(testDir)(_.split("\\.").foldLeft(testDir) { (a: File, e: String) =>
                  new File(a, e)
                })
                val writeFile = new File(writeDir, writeBasename + "Doctest.scala")
                IO.write(writeFile, result.testSource)
                createScalafmtInstance.value.foreach { fmt =>
                  log.debug(s"format ${writeFile.getAbsolutePath}")
                  IO.write(
                    writeFile,
                    fmt.format(
                      writeFile.toPath,
                      IO.read(writeFile)
                    )
                  )
                }
                writeFile
              }
            }
            .toSeq
      }
    },
    Test / sourceGenerators += doctestGenTests.taskValue,
    watchSources ++= Def.uncached {
      val pathFinder = doctestMarkdownPathFinder.value
      if (doctestMarkdownEnabled.value) {
        pathFinder.get().map(new Source(_, AllPassFilter, NothingFilter))
      } else {
        Seq.empty
      }
    }
  )

  val doctestSettings = doctestGenSettings
}
