val scalaVersions = Seq("2.13.16", "3.7.3")

lazy val jsNativeTest = projectMatrix
  .in(file("core"))
  .settings(
    libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.19.0" % Test
  )
  .defaultAxes()
  .jvmPlatform(
    scalaVersions = scalaVersions :+ "2.12.20"
  )
  .jsPlatform(
    scalaVersions = scalaVersions
  )
  .nativePlatform(
    scalaVersions = scalaVersions
  )
