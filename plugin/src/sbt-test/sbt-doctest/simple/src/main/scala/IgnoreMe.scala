package sbt_doctest

object IgnoreMe {

  /**
    * A function.
    *
    * {{{
    * >>> IgnoreMe.f(10)
    * 20
    *
    * prop> (i: Int) =>
    *     |   Main.f(i) == (i *
    *     | 2)
    * }}}
    */
  def f(x: Int) = x + x

}
