package com.github.tkawachi.doctest

import com.google.common.reflect.ClassPath
import org.scalatest.funspec.AnyFunSpec

class DoctestRuntimeCompatSpec extends AnyFunSpec {
  private lazy val allXmlClasses: List[Class[?]] = {
    ClassPath
      .from(classOf[scala.xml.Elem].getClassLoader)
      .getAllClasses
      .asList()
      .toArray(Array.empty[ClassPath.ClassInfo])
      .withFilter(_.getPackageName.startsWith("scala.xml"))
      .map(_.load())
      .toList
      .distinct
      .sortBy(_.getName)
  }

  it("xmlClassNames") {
    // https://github.com/scala/scala/blob/8b9ec50f1b6bcf1c34/src/library/scala/runtime/ScalaRunTime.scala#L213-L214
    val expect = allXmlClasses
      .filter(c => classOf[scala.xml.Node].isAssignableFrom(c) || classOf[scala.xml.MetaData].isAssignableFrom(c))
      .map(_.getName)
      .toSet
    assert(expect == DoctestRuntimeCompat.xmlClassNames)
  }
}
