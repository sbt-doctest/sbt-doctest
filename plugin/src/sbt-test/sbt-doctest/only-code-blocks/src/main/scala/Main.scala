package sbt_doctest

object Main {

  /**
   * A function.
   *
   * {{{
   *  // should return 20
   *  Main.f(10)
   * }}}
   */
  def f(x: Int) = x + x

  /**
   * Comments on variables are also picked up
   *
   * {{{
   * // Import test
   * import sbt_doctest.Main.xyz
   *
   * xyz
   * //123
   * }}}
   */
  val xyz = 123

  /**
   * {{{
   * import sbt_doctest.Main.abc
   *
   * abc
   * // res0: String = Hello, world!
   * }}}
   */
  val abc = "Hello, world!"

  /**
   * {{{
   *   Main.html
   *
   *   //&lt;html&gt;&lt;/html&gt;
   * }}}
   */
  val html = "&lt;html&gt;&lt;/html&gt;"

  /**
   * Escape-character method
   *
   * {{{
   *   Main \ 1 // outputs 2
   * }}}
   */
  def \(x: Int) = x + x
}
