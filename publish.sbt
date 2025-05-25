import ReleaseTransformations._

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+ publishSigned"),
  releaseStepCommandAndRemaining("sonaUpload"),
  releaseStepCommandAndRemaining("sonaRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

publishTo := if(isSnapshot.value) None else localStaging.value

pomExtra := {
  <url>https://github.com/sbt-doctest/sbt-doctest/</url>
  <developers>
    <developer>
      <id>kawachi</id>
      <name>Takashi Kawachi</name>
      <url>https://github.com/tkawachi</url>
    </developer>
    <developer>
      <id>fthomas</id>
      <name>Frank S. Thomas</name>
      <url>https://github.com/fthomas</url>
    </developer>
    <developer>
      <id>jozic</id>
      <name>Eugene Platonov</name>
      <url>https://github.com/jozic</url>
    </developer>
  </developers>
}

sbtPluginPublishLegacyMavenStyle := false
