package sbt_doctest

object Main {

  /**
   * A function.
   *
   * {{{
   * prop> (i: Int) =>
   *     |   Main.f(i) == (i *
   *     | 2)
   * }}}
   */
  def f(x: Int) = x + x
}
