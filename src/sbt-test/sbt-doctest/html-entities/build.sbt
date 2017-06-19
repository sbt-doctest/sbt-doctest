crossScalaVersions := Seq("2.10.6", "2.11.11", "2.12.2")

javacOptions ++= (scalaVersion.value match {
  case v if v startsWith "2.13." => Seq("-source", "1.8", "-target", "1.8")
  case v if v startsWith "2.12." => Seq("-source", "1.8", "-target", "1.8")
  case v if v startsWith "2.11." => Seq("-source", "1.8", "-target", "1.6")
  case v if v startsWith "2.10." => Seq("-source", "1.8", "-target", "1.6")
})

scalacOptions         := Seq("-Ywarn-dead-code")
scalacOptions in Test -= "-Ywarn-dead-code"
scalacOptions        ++= (scalaVersion.value match {
  case v if v startsWith "2.13." => Seq("-target:jvm-1.8")
  case v if v startsWith "2.12." => Seq("-target:jvm-1.8", "-opt:l:method")
  case v if v startsWith "2.11." => Seq("-target:jvm-1.6")
  case v if v startsWith "2.10." => Seq("-target:jvm-1.6")
})

// Declares scalatest, scalacheck and utest dependencies explicitly.
libraryDependencies ++= Seq(
  "com.lihaoyi"    %% "utest"             % "0.4.7"  % "test",
  "org.scalatest"  %% "scalatest"         % "3.0.1"  % "test",
  "org.scalacheck" %% "scalacheck"        % "1.13.4" % "test",
  "org.specs2"     %% "specs2-core"       % "3.8.7"  % "test",
  "org.specs2"     %% "specs2-scalacheck" % "3.8.7"  % "test"
)

doctestDecodeHtmlEntities := true
