package com.github.tkawachi.doctest

import utest._

object TestGenSpec extends TestSuite {

  val tests = utest.Tests {

    val baseName = "MyClass"
    val pkg = Some("com.example.tests")
    val parsed = Seq(
      ParsedDoctest(
        pkg,
        "sumExample",
        Seq(
          Verbatim("import scala.util.Random"),
          Example("List(1,2,3).sum", TestResult("6", Some("Int")), 3),
          Verbatim("val i = 17"),
          Verbatim("val j = 19 + i"),
          Property("""(i: Int) => i + i == i * 2""", 2)
        ),
        37
      )
    )

    "MicroTestGen" - {
      "generates a valid test" - {

        val expectedTest =
          """package com.example.tests
            |
            |import _root_.utest._
            |
            |object `MyClassDoctest` extends TestSuite {
            |
            |  def sbtDoctestTypeEquals[A](a1: => A)(a2: => A): _root_.scala.Unit = {
            |    val _ = () => (a1, a2)
            |  }
            |  def sbtDoctestReplString(any: _root_.scala.Any): _root_.scala.Predef.String = {
            |    val s = _root_.scala.runtime.ScalaRunTime.replStringOf(any, 1000).init
            |    if (s.headOption == Some('\n')) s.tail else s
            |  }
            |
            |
            | val tests = this {
            |  "MyClass.scala:37: sumExample" - {
            |    import scala.util.Random
            |
            |    "example at line 39: List(1,2,3).sum"-{
            |      sbtDoctestTypeEquals(List(1,2,3).sum)((List(1,2,3).sum): Int)
            |      val _actual_   =       sbtDoctestReplString(List(1,2,3).sum)
            |      val _expected_ = "6"
            |      assert( _expected_ == _actual_ )
            |    }
            |
            |    val i = 17
            |
            |    val j = 19 + i
            |
            |    "property at line 38: (i: Int) => i + i == i * 2"-{
            |      sbtDoctestReplString((i: Int) => i + i == i * 2)
            |    }
            |  }
            | }
            |
            |}
            |""".stripMargin

        val generated = MicroTestGen.generate(baseName, pkg, parsed)
        assert(generated == expectedTest)
      }
    }

    "ScalaTest30Gen" - {
      "generates a valid test" - {

        val expectedTest =
          """package com.example.tests
            |
            |import _root_.org.scalacheck.Arbitrary._
            |
            |class `MyClassDoctest`
            |    extends _root_.org.scalatest.FunSpec
            |    with _root_.org.scalatest.prop.Checkers {
            |
            |  def sbtDoctestTypeEquals[A](a1: => A)(a2: => A): _root_.scala.Unit = {
            |    val _ = () => (a1, a2)
            |  }
            |  def sbtDoctestReplString(any: _root_.scala.Any): _root_.scala.Predef.String = {
            |    val s = _root_.scala.runtime.ScalaRunTime.replStringOf(any, 1000).init
            |    if (s.headOption == Some('\n')) s.tail else s
            |  }
            |
            |  describe("MyClass.scala:37: sumExample") {
            |    import scala.util.Random
            |
            |    it("example at line 39: List(1,2,3).sum") {
            |      sbtDoctestTypeEquals(List(1,2,3).sum)((List(1,2,3).sum): Int)
            |      assert(sbtDoctestReplString(List(1,2,3).sum) == "6")
            |    }
            |
            |    val i = 17
            |
            |    val j = 19 + i
            |
            |    it("property at line 38: (i: Int) => i + i == i * 2") {
            |      check {
            |        (i: Int) => i + i == i * 2
            |      }
            |    }
            |  }
            |
            |}
            |""".stripMargin

        val generated = ScalaTest30Gen.generate(baseName, pkg, parsed)

        assert(generated == expectedTest)
      }
    }

    "ScalaTest31Gen" - {
      "generates a valid test" - {

        val expectedTest =
          """package com.example.tests
            |
            |import _root_.org.scalacheck.Arbitrary._
            |
            |class `MyClassDoctest`
            |    extends _root_.org.scalatest.funspec.AnyFunSpec
            |    with _root_.org.scalatestplus.scalacheck.Checkers {
            |
            |  def sbtDoctestTypeEquals[A](a1: => A)(a2: => A): _root_.scala.Unit = {
            |    val _ = () => (a1, a2)
            |  }
            |  def sbtDoctestReplString(any: _root_.scala.Any): _root_.scala.Predef.String = {
            |    val s = _root_.scala.runtime.ScalaRunTime.replStringOf(any, 1000).init
            |    if (s.headOption == Some('\n')) s.tail else s
            |  }
            |
            |  describe("MyClass.scala:37: sumExample") {
            |    import scala.util.Random
            |
            |    it("example at line 39: List(1,2,3).sum") {
            |      sbtDoctestTypeEquals(List(1,2,3).sum)((List(1,2,3).sum): Int)
            |      assert(sbtDoctestReplString(List(1,2,3).sum) == "6")
            |    }
            |
            |    val i = 17
            |
            |    val j = 19 + i
            |
            |    it("property at line 38: (i: Int) => i + i == i * 2") {
            |      check {
            |        (i: Int) => i + i == i * 2
            |      }
            |    }
            |  }
            |
            |}
            |""".stripMargin

        val generated = ScalaTest31Gen.generate(baseName, pkg, parsed)

        assert(generated == expectedTest)
      }
    }

    "Specs2TestGen" - {
      "generates a valid test" - {

        val expectedTest =
          """package com.example.tests
            |
            |import _root_.org.scalacheck.Arbitrary._
            |
            |class `MyClassDoctest`
            |    extends _root_.org.specs2.mutable.Specification
            |    with _root_.org.specs2.ScalaCheck {
            |
            |  def sbtDoctestTypeEquals[A](a1: => A)(a2: => A): _root_.scala.Unit = {
            |    val _ = () => (a1, a2)
            |  }
            |  def sbtDoctestReplString(any: _root_.scala.Any): _root_.scala.Predef.String = {
            |    val s = _root_.scala.runtime.ScalaRunTime.replStringOf(any, 1000).init
            |    if (s.headOption == Some('\n')) s.tail else s
            |  }
            |
            |  implicit def toMatcher[T](t: T): _root_.org.specs2.matcher.Matcher[T] = _root_.org.specs2.matcher.AlwaysMatcher[T]()
            |
            |  "MyClass.scala:37: sumExample" >> {
            |    import scala.util.Random
            |
            |    "example at line 39: List(1,2,3).sum" >> {
            |      sbtDoctestTypeEquals(List(1,2,3).sum)((List(1,2,3).sum): Int)
            |      sbtDoctestReplString(List(1,2,3).sum) must_== "6"
            |    }
            |
            |    val i = 17
            |
            |    val j = 19 + i
            |
            |    "property at line 38: (i: Int) => i + i == i * 2" ! prop {
            |      (i: Int) => i + i == i * 2
            |    }
            |  }
            |
            |}
            |""".stripMargin

        val generated = Specs2TestGen.generate(baseName, pkg, parsed)

        assert(generated == expectedTest)
      }
    }

    "ScalaCheckGen" - {
      "generates a valid test" - {

        val expectedTest =
          """package com.example.tests
            |
            |import _root_.org.scalacheck.Arbitrary._
            |import _root_.org.scalacheck.Prop.{BooleanOperators => _, _}
            |
            |object `MyClassDoctest`
            |    extends _root_.org.scalacheck.Properties("MyClass.scala") {
            |
            |  def sbtDoctestTypeEquals[A](a1: => A)(a2: => A): _root_.scala.Unit = {
            |    val _ = () => (a1, a2)
            |  }
            |  def sbtDoctestReplString(any: _root_.scala.Any): _root_.scala.Predef.String = {
            |    val s = _root_.scala.runtime.ScalaRunTime.replStringOf(any, 1000).init
            |    if (s.headOption == Some('\n')) s.tail else s
            |  }
            |
            |  include(new _root_.org.scalacheck.Properties("MyClass.scala:37: sumExample") {
            |    import scala.util.Random
            |
            |    property("example at line 39: List(1,2,3).sum") = _root_.org.scalacheck.Prop.secure {
            |      sbtDoctestTypeEquals(List(1,2,3).sum)((List(1,2,3).sum): Int)
            |      val actual =       sbtDoctestReplString(List(1,2,3).sum)
            |      val expected = "6"
            |      (actual == expected) :| s"'$actual' is not equal to '$expected'"
            |    }
            |
            |    val i = 17
            |
            |    val j = 19 + i
            |
            |    property("property at line 38: (i: Int) => i + i == i * 2") = _root_.org.scalacheck.Prop.forAll {
            |      (i: Int) => i + i == i * 2
            |    }
            |  })
            |
            |}
            |""".stripMargin

        val generated = ScalaCheckGen.generate(baseName, pkg, parsed)

        assert(generated == expectedTest)
      }
    }

    "MinitestGen" - {
      "generates a valid test" - {

        val expectedTest =
          """package com.example.tests
            |
            |import _root_.minitest._
            |import _root_.minitest.laws.Checkers
            |import _root_.org.scalacheck.Prop.{BooleanOperators => _, _}
            |import _root_.org.scalacheck.Arbitrary._
            |
            |object `MyClassDoctest` extends SimpleTestSuite with Checkers {
            |
            |  def sbtDoctestTypeEquals[A](a1: => A)(a2: => A): _root_.scala.Unit = {
            |    val _ = () => (a1, a2)
            |  }
            |  def sbtDoctestReplString(any: _root_.scala.Any): _root_.scala.Predef.String = {
            |    val s = _root_.scala.runtime.ScalaRunTime.replStringOf(any, 1000).init
            |    if (s.headOption == Some('\n')) s.tail else s
            |  }
            |
            |  implicit def toVoid[A](ref: A)(implicit location: _root_.minitest.api.SourceLocation): _root_.minitest.api.Void =
            |    _root_.minitest.api.Void.UnitRef
            |
            |  test("MyClass.scala:37: sumExample") {
            |    import scala.util.Random
            |
            |    //example at line 39: List(1,2,3).sum
            |    sbtDoctestTypeEquals(List(1,2,3).sum)((List(1,2,3).sum): Int)
            |    assertEquals(      sbtDoctestReplString(List(1,2,3).sum), "6")
            |
            |    val i = 17
            |
            |    val j = 19 + i
            |
            |    //property at line 38: (i: Int) => i + i == i * 2
            |    check(_root_.org.scalacheck.Prop.forAll((i: Int) => i + i == i * 2))
            |  }
            |
            |}
            |""".stripMargin

        val generated = MinitestGen.generate(baseName, pkg, parsed)

        assert(generated == expectedTest)
      }
    }

    "MunitGen" - {
      "generates a valid test" - {
        val expectedTest = """package com.example.tests
                             |
                             |import _root_.munit._
                             |import _root_.org.scalacheck.Prop._
                             |
                             |
                             |class `MyClassDoctest` extends ScalaCheckSuite {
                             |
                             |  def sbtDoctestTypeEquals[A](a1: => A)(a2: => A): _root_.scala.Unit = {
                             |    val _ = () => (a1, a2)
                             |  }
                             |  def sbtDoctestReplString(any: _root_.scala.Any): _root_.scala.Predef.String = {
                             |    val s = _root_.scala.runtime.ScalaRunTime.replStringOf(any, 1000).init
                             |    if (s.headOption == Some('\n')) s.tail else s
                             |  }
                             |
                             |  test("MyClass.scala:37: sumExample") {
                             |    import scala.util.Random
                             |
                             |    //example at line 39: List(1,2,3).sum
                             |    sbtDoctestTypeEquals(List(1,2,3).sum)((List(1,2,3).sum): Int)
                             |      assertEquals(sbtDoctestReplString(List(1,2,3).sum), "6")
                             |
                             |    val i = 17
                             |
                             |    val j = 19 + i
                             |
                             |  property("property at line 38: (i: Int) => i + i == i * 2") {
                             |    forAll((i: Int) => i + i == i * 2)
                             |  }
                             |
                             |  }
                             |
                             |}
                             |""".stripMargin
        val generated = MunitGen.generate(baseName, pkg, parsed)
        assert(generated == expectedTest)
      }
    }
  }
}
