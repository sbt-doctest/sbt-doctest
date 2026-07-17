import ReleaseTransformations._
import sbt.Def

def Scala212 = "2.12.21"
def Scala213 = "2.13.18"
def Scala3 = "3.3.8"
val scalaVersions = Seq(Scala212, Scala213, Scala3)
val sbt2 = {
  val p = new java.util.Properties
  p.load(new java.io.FileInputStream("project/build.properties"))
  p.getProperty("sbt.version").trim
}
def sbt1 = "1.12.13"

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
        Seq("-Xlint", "-release:8")
    }
  },
  scalacOptions ++= Seq(
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
      scalacOptions ++= {
        if (scalaVersion.value.startsWith("3.3.")) {
          Seq(
            "-Yfuture-lazy-vals",
            "-release:11"
          )
        } else {
          Nil
        }
      },
      libraryDependencies ++= Seq(
        "com.google.guava" % "guava" % "33.6.0-jre" % Test
      )
    )
  )
  .jsPlatform(
    scalaVersions = scalaVersions,
    settings = jsNativeCommon
  )
  .nativePlatform(
    scalaVersions = scalaVersions,
    settings = Def.settings(
      jsNativeCommon,
      evictionErrorLevel := Level.Warn
    )
  )
  .settings(
    commonSettings,
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest-funspec" % "3.2.20" % Test,
      "org.scala-lang.modules" %% "scala-xml" % "2.4.0" % Test
    ),
    name := "doctest-runtime"
  )

lazy val plugin = (projectMatrix in file("plugin"))
  .enablePlugins(SbtPlugin)
  .defaultAxes(VirtualAxis.jvm)
  .jvmPlatform(
    scalaVersions = Seq(
      Scala212,
      scala_version_from_sbt_version.ScalaVersionFromSbtVersion(sbt2)
    )
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
      "org.scalameta" % "scalafmt-interfaces" % "3.11.4",
      "commons-io" % "commons-io" % "2.22.0",
      "org.apache.commons" % "commons-text" % "1.15.0",
      "org.scalameta" %% "scalameta" % "4.17.2",
      "com.lihaoyi" %% "utest" % "0.9.5" % Test,
      "org.scalatest" %% "scalatest-funspec" % "3.2.20" % Test,
      "org.scalatestplus" %% "scalacheck-1-19" % "3.2.20.0" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.23.0" % Test,
      "io.monix" %% "minitest-laws" % "2.9.6" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          sbt1
        case "3" =>
          sbt2
      }
    },
    name := "sbt-doctest",
    scriptedDependencies := Def.uncached {
      val s = state.value
      Project.extract(s).runAggregated(LocalRootProject / publishLocal, s)
    },
    scriptedLaunchOpts ++= {
      Seq("-Xmx4G", "-Dplugin.version=" + version.value)
    },
    scriptedBufferLog := false
  )

lazy val sbtDoctestRoot = rootProject.autoAggregate.settings(
  commonSettings,
  publish / skip := true
)
