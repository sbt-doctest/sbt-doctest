scalaVersion := "3.7.4"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.18.1" % Test

TaskKey[Unit]("checkFormat") := {
  val Seq(src) = (Test / sources).value
  assert(IO.read(src) == IO.read(file("expect")))
}

TaskKey[Unit]("checkNoFormat") := {
  val Seq(src) = (Test / sources).value
  assert(IO.read(src) != IO.read(file("expect")))
}
