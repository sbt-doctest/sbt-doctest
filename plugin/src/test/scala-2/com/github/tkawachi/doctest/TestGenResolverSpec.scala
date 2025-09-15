package com.github.tkawachi.doctest

import com.github.tkawachi.doctest.DoctestPlugin.DoctestTestFramework
import sbt.Keys.moduleID
import sbt._
import utest._

object TestGenResolverSpec extends TestSuite {
  val tests = utest.Tests {
    "findScalaTestVersion()" - {
      val result = TestGenResolver.findScalaTestVersion(
        Seq(
          Attributed(file("."))(
            AttributeMap(AttributeEntry(moduleID.key, ModuleID("org.scalatest", "scalatest_2.11", "3.0.0")))
          )
        ),
        "2.11.12"
      )
      assert(result.contains("3.0.0"))
    }

    "findScalaTestVersion() scalaVersion mismatch" - {
      val result = TestGenResolver.findScalaTestVersion(
        Seq(
          Attributed(file("."))(
            AttributeMap(AttributeEntry(moduleID.key, ModuleID("org.scalatest", "scalatest_2.11", "3.1.0")))
          )
        ),
        "2.12.5"
      )
      assert(result.isEmpty)
    }

    "resolve(ScalaTest, 3.0.9)" - {
      val result = TestGenResolver.resolve(DoctestTestFramework.ScalaTest, Some("3.0.9"))
      assert(result == ScalaTest30Gen)
    }
  }
}
