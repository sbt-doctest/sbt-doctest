import ReleaseTransformations._
import sbt.Def

def Scala212 = "2.12.20"
def Scala213 = "2.13.17"
def Scala3 = "3.3.7"
val scalaVersions = Seq(Scala212, Scala213, Scala3)

val commonSettings = Def.settings(
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommandAndRemaining("publishSigned"),
    releaseStepCommandAndRemaining("sonaRelease"),
    setNextVersion,
    commitNextVersion,
    pushChanges
  ),
  publishTo := (if (isSnapshot.value) None else localStaging.value),
  pomExtra := {
    <url>https://github.com/sbt-doctest/sbt-doctest/</url>
    <developers>
      <developer>
        <id>kawachi</id>
        <name>Takashi Kawachi</name>
        <url>https://github.com/tkawachi</url>
      </developer>
      <developer>
        <id>fthomas</id>
        <name>Frank S. Thomas</name>
        <url>https://github.com/fthomas</url>
      </developer>
      <developer>
        <id>jozic</id>
        <name>Eugene Platonov</name>
        <url>https://github.com/jozic</url>
      </developer>
    </developers>
  },
  sbtPluginPublishLegacyMavenStyle := false,
  organization := "io.github.sbt-doctest",
  licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/sbt-doctest/sbt-doctest/"),
      "scm:git:github.com:sbt-doctest/sbt-doctest.git"
    )
  ),
  javacOptions ++= Seq("-encoding", "UTF-8"),
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "2.12" =>
        Seq("-Xsource:3")
      case "2.13" =>
        Seq("-Wunused", "-Xsource:3-cross")
      case _ =>
        Seq("-Wunused:all")
    }
  },
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "3" =>
        Nil
      case _ =>
        Seq("-Xlint")
    }
  },
  scalacOptions ++= Seq(
    "-release:8",
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked"
  )
)

val runtimeBase = file("runtime")

def platformSrcDir(x: String): Seq[Def.Setting[?]] = {
  Def.settings(
    Seq(Compile, Test).map { c =>
      c / unmanagedSourceDirectories ++= {
        val base = runtimeBase / x / "src" / Defaults.nameForSrc(c.name)

        Seq(
          base / "scala",
          scalaBinaryVersion.value match {
            case "3" | "2.13" =>
              base / "scala-2.13+"
            case "2.12" =>
              base / "scala-2.12"
          }
        ).map(_.getAbsoluteFile)
      }
    }
  )
}

val jsNativeCommon = Def.settings(
  platformSrcDir(s"${VirtualAxis.js.directorySuffix}-${VirtualAxis.native.directorySuffix}"),
  scalacOptions ++= {
    scalaBinaryVersion.value match {
      case "3" =>
        Seq("-Wconf:msg=reflectiveSelectableFromLangReflectiveCalls:silent")
      case _ =>
        Nil
    }
  }
)

lazy val runtime = (projectMatrix in runtimeBase)
  .defaultAxes()
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = Def.settings(
      platformSrcDir(VirtualAxis.jvm.directorySuffix),
      libraryDependencies ++= Seq(
        "com.google.guava" % "guava" % "33.5.0-jre" % Test
      )
    )
  )
  .jsPlatform(
    scalaVersions = scalaVersions,
    settings = jsNativeCommon
  )
  .nativePlatform(
    scalaVersions = scalaVersions,
    settings = jsNativeCommon
  )
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest-funspec" % "3.2.19" % Test,
      "org.scala-lang.modules" %%% "scala-xml" % "2.4.0" % Test
    ),
    name := "doctest-runtime"
  )

lazy val plugin = (projectMatrix in file("plugin"))
  .enablePlugins(SbtPlugin)
  .jvmPlatform(
    scalaVersions = Seq(Scala212, "3.7.3")
  )
  .settings(
    commonSettings,
    Compile / sourceGenerators += task {
      val dir = (Compile / sourceManaged).value
      val f = dir / "DoctestBuildInfo.scala"
      IO.write(
        f,
        s"""|package com.github.tkawachi.doctest
            |
            |private[doctest] object DoctestBuildInfo {
            |  def version: String = "${version.value}"
            |}
            |""".stripMargin
      )
      Seq(f)
    },
    libraryDependencies ++= Seq(
      "org.scalameta" % "scalafmt-interfaces" % "3.10.1",
      "commons-io" % "commons-io" % "2.21.0",
      "org.apache.commons" % "commons-text" % "1.14.0",
      "org.scalameta" %% "scalameta" % "4.14.1",
      "com.lihaoyi" %% "utest" % "0.8.4" % Test,
      "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.23.0" % Test,
      "io.monix" %% "minitest-laws" % "2.9.6" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          sbtVersion.value
        case _ =>
          "2.0.0-RC6"
      }
    },
    name := "sbt-doctest",
    scriptedDependencies := {
      val s = state.value
      Project.extract(s).runAggregated(LocalRootProject / publishLocal, s)
    },
    TaskKey[Unit]("scriptedTestSbt2") := Def.taskDyn {
      val values = sbtTestDirectory.value
        .listFiles(_.isDirectory)
        .flatMap { dir1 =>
          dir1.listFiles(_.isDirectory).map { dir2 =>
            dir1.getName -> dir2.getName
          }
        }
        .toList
      val log = streams.value.log
      val exclude: Set[(String, String)] = Set(
        "js-native"
      ).map("sbt-doctest" -> _)
      val args = values.filterNot(exclude).map { case (x1, x2) => s"${x1}/${x2}" }
      val arg = args.mkString(" ", " ", "")
      log.info("scripted" + arg)
      scripted.toTask(arg)
    }.value,
    scriptedLaunchOpts ++= {
      Seq("-Xmx4G", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )

commonSettings
publish / skip := true
