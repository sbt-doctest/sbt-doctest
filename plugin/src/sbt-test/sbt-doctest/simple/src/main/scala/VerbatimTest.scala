package sbt_doctest

object VerbatimTest {

  /**
   * scala> abstract class A {
   *      |   def name: String
   *      | }
   * scala> class B extends A {
   *      |   def name = "B"
   *      | }
   * scala> (new B).name
   * res0: String = B
   */
  abstract class A1

  /**
   * {{{
   * scala> case class C(name: String)
   * scala> C("Hello").name
   * res0: String = Hello
   * }}}
   */
  case class C1()

  /**
   * >>> class C { def name = "clazz" }
   * >>> (new C).name
   * clazz
   */
  class C2

  /**
   * >>> implicit val y: Int = 23
   * >>> def f(implicit ev: Int) = ev
   * >>> f
   * 23
   */
  implicit val x: Int = 42

  /**
   * scala> object xy { val z = 123 }
   * scala> xy.z
   * res0: Int = 123
   */
  object obj

  /**
   * >>> sealed trait ST1
   * >>> case object ST2 extends ST1 { val x = 9 }
   * >>> ST2.x
   * 9
   */
  sealed trait T3

  /**
   * >>> trait T1
   * >>> object T2 extends T1 { val x = 19 }
   * >>> T2.x
   * 19
   */
  trait T4

  /**
   * scala> type
   *      |   MyInt = Int
   * scala> val x: MyInt = 4
   * scala> x: Int
   * res0: Int = 4
   */
  type MyInt = Int

  /**
    * scala> @deprecated("a", "b") case class Foo(x: Int)
    * scala> Foo(1)
    * res0: Foo = Foo(1)
    */
  case class Foo(x: Int)
}
