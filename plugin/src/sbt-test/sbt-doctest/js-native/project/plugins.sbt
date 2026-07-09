addSbtPlugin("io.github.sbt-doctest" % "sbt-doctest" % System.getProperty("plugin.version"))
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.22.0")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.12")

libraryDependencies ++= {
  sbtBinaryVersion.value match {
    case "2" =>
      Nil
    case "1.0" =>
      Seq(
        Defaults.sbtPluginExtra(
          "com.eed3si9n" % "sbt-projectmatrix" % "0.11.0",
          (pluginCrossBuild / sbtBinaryVersion).value,
          (update / scalaBinaryVersion).value
        )
      )
  }
}
