package com.github.tkawachi.doctest

import sbt.*

private[doctest] trait DoctestCompat {
  self: DoctestPlugin.type =>

  def findScalaTestVersionFromScalaBinaryVersion(
      testClasspath: Def.Classpath,
      scalaBinaryVersion: String
  ): Option[String] = {
    testClasspath.flatMap { entry =>
      entry.get(sbt.Keys.moduleIDStr).flatMap { str =>
        val moduleId = Classpaths.moduleIdJsonKeyFormat.read(str)
        TestGenResolver.detectScalaTestVersion(moduleId, scalaBinaryVersion)
      }
    }.headOption
  }

  def managedJars(
      config: Configuration,
      jarTypes: Set[String],
      up: UpdateReport,
      converter: FileConverter
  ): Def.Classpath = Classpaths.managedJars(
    config = config,
    jarTypes = jarTypes,
    up = up,
    converter = converter
  )
}
