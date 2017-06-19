package com.github.tkawachi.doctest
import sbt._, Keys._
import sbt.plugins.JvmPlugin

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
object DoctestPlugin extends AutoPlugin {
  self =>

  sealed abstract class DoctestTestFramework

  object DoctestTestFramework {
    case object Specs2 extends DoctestTestFramework
    case object ScalaTest extends DoctestTestFramework
    case object ScalaCheck extends DoctestTestFramework
    case object utest extends DoctestTestFramework
  }
  import DoctestTestFramework._

  override def trigger: PluginTrigger = allRequirements

  // Currently not working with ScalaJS. See #52.
  override def requires = JvmPlugin

  override def projectSettings: Seq[Setting[_]] = doctestSettings

  object autoImport {
    val doctestTestFramework = settingKey[DoctestTestFramework]("Test framework. Specify utest, ScalaTest, ScalaCheck or Specs2.")
    val doctestMarkdownEnabled = settingKey[Boolean]("Whether to compile markdown into doctests.")
    val doctestMarkdownPathFinder = settingKey[PathFinder]("PathFinder to find markdown to test.")
    val doctestGenTests = taskKey[Seq[File]]("Generates test files.")
    val doctestDecodeHtmlEntities = settingKey[Boolean]("Whether to decode HTML entities.")

    val DoctestTestFramework = self.DoctestTestFramework
  }

  import autoImport._

  private def doctestScaladocGenTests(sources: Seq[File], framework: DoctestTestFramework, decodeHtml: Boolean, scalacOptions: Seq[String]) = {
    val srcEncoding = ScaladocTestGenerator.findEncoding(scalacOptions).getOrElse("UTF-8")
    sources
      .filter(_.ext == "scala")
      .flatMap(ScaladocTestGenerator(_, srcEncoding, framework, decodeHtml))
  }

  private def doctestMarkdownGenTests(
                                       finder: PathFinder,
                                       framework: DoctestTestFramework) = {
      finder.filter(!_.isDirectory).get
        .flatMap(MarkdownTestGenerator(_, framework))
  }


  /**
   * Settings for test Generation.
   */
  val doctestGenSettings = Seq(
    doctestTestFramework := (doctestTestFramework ?? ScalaCheck).value,
    doctestDecodeHtmlEntities := (doctestDecodeHtmlEntities ?? false).value,
    doctestMarkdownEnabled := (doctestMarkdownEnabled ?? false).value,
    doctestMarkdownPathFinder := baseDirectory.value * "*.md",
    testFrameworks            += new TestFramework("utest.runner.Framework"),
    doctestGenTests := {
      (managedSourceDirectories in Test).value.headOption match {
        case None =>
          streams.value.log.warn("managedSourceDirectories in Test is empty. Failed to generate tests")
          Seq()
        case Some(testDir) =>

          val scaladocTests = doctestScaladocGenTests(
            (unmanagedSources in Compile).value ++ (managedSources in Compile).value,
            doctestTestFramework.value,
            doctestDecodeHtmlEntities.value,
            (scalacOptions in Compile).value
          )

          val markdownTests = if (doctestMarkdownEnabled.value) {
            doctestMarkdownGenTests(doctestMarkdownPathFinder.value, doctestTestFramework.value)
          } else {
            Seq()
          }

          (scaladocTests ++ markdownTests)
            .groupBy(r => r.pkg -> r.basename)
            .flatMap {
              case ((pkg, basename), results) =>
                results.zipWithIndex.map {
                  case (result, idx) =>
                    val writeBasename = if (idx == 0) basename else basename + idx
                    val writeDir = pkg.fold(testDir)(_.split("\\.").foldLeft(testDir) { (a: File, e: String) => new File(a, e) })
                    val writeFile = new File(writeDir, writeBasename + "Doctest.scala")
                    IO.write(writeFile, result.testSource)
                    writeFile
                }
            }.toSeq
      }
    },
    sourceGenerators in Test += doctestGenTests.taskValue,
    watchSources ++= {
      if (doctestMarkdownEnabled.value) {
        doctestMarkdownPathFinder.value.get
      } else {
        Seq.empty
      }
    }
  )

  val doctestSettings = doctestGenSettings
}
