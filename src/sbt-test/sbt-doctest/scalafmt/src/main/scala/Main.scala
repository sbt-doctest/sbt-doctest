package sbt_doctest

object Main {

  /**
   * {{{
   * scala> {
   *      |   enum A {
   *      |     case B
   *      |     case C
   *      |   }
   *      |   A.values.size
   *      | }
   * res0: Int = 2
   * scala> sbt_doctest.Main.X.values.toList
   * res1: List[Main.X] = List(X1, X2, X3)
   * }}}
   */
  def foo: Int = 0

  enum X {
    case X1
    case X2
    case X3
  }
}
