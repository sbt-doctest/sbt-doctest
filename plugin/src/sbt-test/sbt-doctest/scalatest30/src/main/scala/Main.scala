package sbt_doctest

object Main {

  /**
   * A function.
   *
   * {{{
   * prop> (i: Int) =>
   *     |   Main.f(i) == (i *
   *     | 2)
   *
   * scala> Main.f(20)
   * res1: Int = 40
   * }}}
   */
  def f(x: Int) = x + x
}
