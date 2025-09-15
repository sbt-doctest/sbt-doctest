import java.nio.charset.StandardCharsets

import complete.DefaultParsers._

crossScalaVersions := Seq("2.12.20", "2.13.16")

// Declares scalatest, scalacheck dependencies explicitly.
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.2" % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % "3.1.2.0" % Test
)

doctestTestFramework := DoctestTestFramework.ScalaTest

val existsInFile = inputKey[Unit]("Ensure a given string exists in a file")

existsInFile := {
  val Seq(searchKey, fileName) = spaceDelimited("<arg>").parsed
  val fileContent = IO.read(file(fileName), StandardCharsets.UTF_8)
  if (!fileContent.contains(searchKey)) {
    sys.error(s"$searchKey doesn't exist in $fileName")
  }
}
