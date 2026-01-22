val scalaVersions = Seq("2.12.21", "2.13.18", "3.8.1")

Global / concurrentRestrictions += Tags.limit(NativeTags.Link, 1)

lazy val jsNativeTest = projectMatrix
  .in(file("core"))
  .settings(
    libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.19.0" % Test
  )
  .defaultAxes()
  .jvmPlatform(
    scalaVersions = scalaVersions
  )
  .jsPlatform(
    scalaVersions = scalaVersions
  )
  .nativePlatform(
    scalaVersions = scalaVersions
  )
