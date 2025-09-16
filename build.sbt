import ReleaseTransformations._
import sbt.Def

def Scala212 = "2.12.20"
def Scala213 = "2.13.16"
def Scala3 = "3.3.6"
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
        Nil
      case "2.13" =>
        Seq("-Wunused")
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

val jsNativeCommon = platformSrcDir(s"${VirtualAxis.js.directorySuffix}-${VirtualAxis.native.directorySuffix}")

lazy val runtime = (projectMatrix in runtimeBase)
  .defaultAxes()
  .jvmPlatform(
    scalaVersions = scalaVersions,
    settings = Def.settings(
      platformSrcDir(VirtualAxis.jvm.directorySuffix),
      libraryDependencies ++= Seq(
        "com.google.guava" % "guava" % "33.4.8-jre" % Test
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
    scalaVersions = Seq(Scala212, "3.7.2")
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
      "org.scalameta" % "scalafmt-interfaces" % "3.9.10",
      "commons-io" % "commons-io" % "2.20.0",
      "org.apache.commons" % "commons-text" % "1.14.0",
      "org.scalameta" %% "scalameta" % "4.13.10",
      "com.lihaoyi" %% "utest" % "0.8.4" % Test,
      "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.21.0" % Test,
      "io.monix" %% "minitest-laws" % "2.9.6" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          sbtVersion.value
        case _ =>
          "2.0.0-RC4"
      }
    },
    name := "sbt-doctest",
    scriptedLaunchOpts ++= {
      Seq("-Xmx4G", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )

commonSettings
publish / skip := true
