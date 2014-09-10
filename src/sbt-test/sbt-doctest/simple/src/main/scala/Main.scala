package sbt_doctest

object Main {
  /**
   * A function.
   *
   * {{{
   * >>> Main.f(10)
   * 20
   *
   * prop> (i: Int) =>
   *     |   Main.f(i) should === (i *
   *     | 2)
   * }}}
   */
  def f(x: Int) = x + x

  /**
   * Comments on variables are also picked up
   *
   * {{{
   * # Import test
   * >>> import sbt_doctest.Main.xyz
   *
   * >>> xyz
   * 123
   * }}}
   */
  val xyz = 123

  /**
   * {{{
   * scala> import sbt_doctest.Main.abc
   * import sbt_doctest.Main.abc
   *
   * scala> abc
   * res0: String = Hello, world!
   * }}}
   */
  val abc = "Hello, world!"

  /**
   * {{{
   * scala> Main.list.take(2)
   * res0: List[Int] = List(0, 1)
   *
   * scala> val xs = List(2)
   * scala> 1 :: xs
   * res0: List[Int] = List(1, 2)
   * }}}
   */
  def list: List[Int] = List.range(0, 5)
}

