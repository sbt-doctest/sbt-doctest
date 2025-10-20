scalaVersion := "3.3.7"

libraryDependencies += "org.specs2" %% "specs2-scalacheck" % "4.23.0" % Test

doctestTestFramework := DoctestTestFramework.Specs2

InputKey[Unit]("check") := {
  val actual = IO.read(file("x1.txt"))
  assert(actual == "a\n", actual)
}
