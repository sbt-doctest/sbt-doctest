crossScalaVersions := Seq("3.3.7", "3.8.0", "2.12.21")

// Declares scalatest, scalacheck, minitest and utest dependencies explicitly.
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.4" % Test,
  "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
  "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test,
  "org.scalacheck" %% "scalacheck" % "1.19.0" % Test,
  "io.monix" %% "minitest-laws" % "2.9.6" % Test,
  "org.specs2" %% "specs2-scalacheck" % "4.23.0" % Test,
  "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test
)

testFrameworks += new TestFramework("minitest.runner.Framework")

doctestDecodeHtmlEntities := true

InputKey[Unit]("check") := {
  sbtBinaryVersion.value match {
    case "1.0" =>
      Seq(
        "target/scala-3.3.7/src_managed/test/sbt_doctest/MainDoctest.scala",
        "target/scala-2.13/src_managed/test/sbt_doctest/MainDoctest.scala",
        "target/scala-2.12/src_managed/test/sbt_doctest/MainDoctest.scala"
      ).foreach(f => assert(file(f).isFile, f))
    case "2" =>
      Seq(
        s"target/out/jvm/scala-2.12.21/${name.value}/src_managed/test/sbt_doctest/MainDoctest.scala",
        s"target/out/jvm/scala-3.8.0/${name.value}/src_managed/test/sbt_doctest/MainDoctest.scala",
        s"target/out/jvm/scala-3.3.7/${name.value}/src_managed/test/sbt_doctest/MainDoctest.scala"
      ).foreach(f => assert(file(f).isFile, f))
  }
}

InputKey[Unit]("cleanFull") := {
  clean.value
  // https://github.com/sbt/sbt/blob/0cef94b1d534cf3f6/main/src/main/scala/sbt/Keys.scala#L442
  SettingKey[File]("localCacheDirectory").?.value.foreach(IO.delete)
}
