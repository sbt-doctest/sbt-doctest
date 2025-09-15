lazy val root = (project in file("."))
  .settings(
    organization := "io.github.sbt-doctest",
    crossScalaVersions += "3.7.2",
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" =>
          sbtVersion.value
        case _ =>
          "2.0.0-RC4"
      }
    },
    name := "sbt-doctest",
    licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/sbt-doctest/sbt-doctest/"),
        "scm:git:github.com:sbt-doctest/sbt-doctest.git"
      )
    ),
    javacOptions ++= Seq("-encoding", "UTF-8"),
    scalacOptions ++= Seq(
      "-release:8",
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-Xlint:-unused,_"
    ),
    libraryDependencies ++= Seq(
      "org.scalameta" % "scalafmt-interfaces" % "3.9.9",
      "commons-io" % "commons-io" % "2.20.0",
      "org.apache.commons" % "commons-text" % "1.14.0",
      "org.scalameta" %% "scalameta" % "4.13.10",
      "com.lihaoyi" %% "utest" % "0.8.4" % Test,
      "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
      "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.21.0" % Test,
      "io.monix" %% "minitest-laws" % "2.9.6" % Test
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .enablePlugins(SbtPlugin)
