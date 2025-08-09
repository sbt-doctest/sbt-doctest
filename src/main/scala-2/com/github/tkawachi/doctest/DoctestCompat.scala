package com.github.tkawachi.doctest

import sbt._

private[doctest] trait DoctestCompat { self: DoctestPlugin.type =>
  def findScalaTestVersionFromScalaBinaryVersion(
      testClasspath: Def.Classpath,
      scalaBinaryVersion: String
  ): Option[String] = {
    testClasspath.flatMap { entry =>
      entry.get(Keys.moduleID.key).flatMap(TestGenResolver.detectScalaTestVersion(_, scalaBinaryVersion))
    }.headOption
  }

  def managedJars(
      config: Configuration,
      jarTypes: Set[String],
      up: UpdateReport,
      converter: xsbti.FileConverter
  ): Def.Classpath = Classpaths.managedJars(
    config = config,
    jarTypes = jarTypes,
    up = up
  )

  implicit class DefOps(self: Def.type) {
    def uncached[A](a: A): A = a
  }
}
