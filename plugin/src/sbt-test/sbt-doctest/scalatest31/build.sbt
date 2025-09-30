import java.nio.charset.StandardCharsets

crossScalaVersions := Seq("2.12.20", "2.13.17")

// Declares scalatest, scalacheck dependencies explicitly.
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.2" % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % "3.1.2.0" % Test
)

doctestTestFramework := DoctestTestFramework.ScalaTest

InputKey[Unit]("check") := {
  val (f1, f2) = sbtBinaryVersion.value match {
    case "2" =>
      (
        file(s"target/out/jvm/scala-2.12.20/${name.value}/src_managed/test/sbt_doctest/MainDoctest.scala"),
        file(s"target/out/jvm/scala-2.13.17/${name.value}/src_managed/test/sbt_doctest/MainDoctest.scala")
      )
    case "1.0" =>
      (
        file("target/scala-2.12/src_managed/test/sbt_doctest/MainDoctest.scala"),
        file("target/scala-2.13/src_managed/test/sbt_doctest/MainDoctest.scala")
      )
  }

  assert(f1.isFile)
  assert(f2.isFile)
  val checkerLine = "with _root_.org.scalatestplus.scalacheck.Checkers"
  assert(IO.read(f1).contains(checkerLine))
  assert(IO.read(f2).contains(checkerLine))
}
