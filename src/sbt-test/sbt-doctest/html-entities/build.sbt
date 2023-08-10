crossScalaVersions := Seq("3.3.0", "2.13.11", "2.12.18")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalacOptions         := Seq("-Ywarn-dead-code")
Test / scalacOptions  -= "-Ywarn-dead-code"

// Declares scalatest, scalacheck, minitest and utest dependencies explicitly.
libraryDependencies ++= Seq(
  "com.lihaoyi"       %% "utest"             % "0.8.1"    % Test,
  "org.scalatest"     %% "scalatest-funspec" % "3.2.16"   % Test,
  "org.scalatestplus" %% "scalacheck-1-17"   % "3.2.16.0" % Test,
  "org.scalacheck"    %% "scalacheck"        % "1.17.0"   % Test,
  "io.monix"          %% "minitest-laws"     % "2.9.6"    % Test,
  "org.specs2"        %% "specs2-scalacheck" % "4.20.2"   % Test,
  "org.scalameta"     %% "munit-scalacheck"  % "0.7.29"   % Test
)

testFrameworks += new TestFramework("minitest.runner.Framework")

doctestDecodeHtmlEntities := true
