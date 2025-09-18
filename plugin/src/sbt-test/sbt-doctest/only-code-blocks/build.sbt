import java.nio.charset.StandardCharsets

import complete.DefaultParsers._

crossScalaVersions := Seq("3.3.6", "2.13.16", "2.12.20")

scalacOptions := Seq("-Ywarn-dead-code")
Test / scalacOptions -= "-Ywarn-dead-code"

// Declares scalatest, scalacheck, minitest and utest dependencies explicitly.
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.4" % Test,
  "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
  "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
  "org.scalacheck" %% "scalacheck" % "1.19.0" % Test,
  "org.specs2" %% "specs2-scalacheck" % "4.21.0" % Test,
  "io.monix" %% "minitest-laws" % "2.9.6" % Test,
  "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
)

doctestMarkdownEnabled := true
doctestOnlyCodeBlocksMode := true
doctestMarkdownPathFinder := baseDirectory.value ** "*.md"

testFrameworks += new TestFramework("minitest.runner.Framework")

val existsInFile = inputKey[Unit]("Ensure a given string exists in a file")

existsInFile := {
  val Seq(searchKey, fileName) = spaceDelimited("<arg>").parsed
  val fileContent = IO.read(file(fileName), StandardCharsets.UTF_8)
  if (!fileContent.contains(searchKey)) {
    sys.error(s"$searchKey doesn't exist in $fileName")
  }
}
