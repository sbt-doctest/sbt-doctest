import java.nio.charset.StandardCharsets

crossScalaVersions := Seq("3.3.7", "3.8.1", "2.12.21")

InputKey[Unit]("check") := {
  val expect = sbtBinaryVersion.value match {
    case "1.0" =>
      Set(
        s"target/scala-2.12/src_managed/test/DocREADMEmd1Doctest.scala",
        s"target/scala-2.12/src_managed/test/READMEmd0Doctest.scala",
        s"target/scala-2.12/src_managed/test/sbt_doctest/MainDoctest.scala",
        s"target/scala-2.13/src_managed/test/DocREADMEmd1Doctest.scala",
        s"target/scala-2.13/src_managed/test/READMEmd0Doctest.scala",
        s"target/scala-2.13/src_managed/test/sbt_doctest/MainDoctest.scala",
        s"target/scala-3.3.7/src_managed/test/DocREADMEmd1Doctest.scala",
        s"target/scala-3.3.7/src_managed/test/READMEmd0Doctest.scala",
        s"target/scala-3.3.7/src_managed/test/sbt_doctest/MainDoctest.scala"
      )
    case "2" =>
      Set(
        s"target/out/jvm/scala-2.12.21/${name.value}/src_managed/test/DocREADMEmd1Doctest.scala",
        s"target/out/jvm/scala-2.12.21/${name.value}/src_managed/test/READMEmd0Doctest.scala",
        s"target/out/jvm/scala-2.12.21/${name.value}/src_managed/test/sbt_doctest/MainDoctest.scala",
        s"target/out/jvm/scala-3.8.1/${name.value}/src_managed/test/DocREADMEmd1Doctest.scala",
        s"target/out/jvm/scala-3.8.1/${name.value}/src_managed/test/READMEmd0Doctest.scala",
        s"target/out/jvm/scala-3.8.1/${name.value}/src_managed/test/sbt_doctest/MainDoctest.scala",
        s"target/out/jvm/scala-3.3.7/${name.value}/src_managed/test/DocREADMEmd1Doctest.scala",
        s"target/out/jvm/scala-3.3.7/${name.value}/src_managed/test/READMEmd0Doctest.scala",
        s"target/out/jvm/scala-3.3.7/${name.value}/src_managed/test/sbt_doctest/MainDoctest.scala"
      )
  }
  val actual = sbt.io
    .PathFinder(file("target"))
    .allPaths
    .filter(_.getName.endsWith(".scala"))
    .get()
    .flatMap(
      IO.relativize(baseDirectory.value, _)
    )
    .toSet
  assert(actual == expect, s"${actual.toSeq.sorted} != ${expect.toSeq.sorted}")
}

// Declares scalatest, scalacheck, minitest and utest dependencies explicitly.
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.4" % Test,
  "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
  "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
  "org.scalacheck" %% "scalacheck" % "1.19.0" % Test,
  "org.specs2" %% "specs2-scalacheck" % "4.23.0" % Test,
  "io.monix" %% "minitest-laws" % "2.9.6" % Test,
  "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
)

doctestMarkdownEnabled := true
doctestOnlyCodeBlocksMode := true
doctestMarkdownPathFinder := baseDirectory.value ** "*.md"

testFrameworks += new TestFramework("minitest.runner.Framework")

InputKey[Unit]("cleanFull") := {
  clean.value
  // https://github.com/sbt/sbt/blob/0cef94b1d534cf3f6/main/src/main/scala/sbt/Keys.scala#L442
  SettingKey[File]("localCacheDirectory").?.value.foreach(IO.delete)
}
