scalaVersion := "3.3.7"

libraryDependencies += "org.scalameta" %% "munit-scalacheck" % "0.7.29" % Test

doctestTestFramework := DoctestTestFramework.Munit
