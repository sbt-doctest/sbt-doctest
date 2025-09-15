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
   *     |   Main.f(i) == (i *
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
   * scala> val xs = List(1)
   * scala> 0 :: xs
   * res0: List[Int] = List(0, 1)
   * }}}
   */
  def list: List[Int] = List.range(0, 5)

  /**
   * >>> val xs = List(1,
   * ...   2,
   * ...   3)
   * >>> xs.sum
   * 6
   *
   * scala> "Hello World".split(" ").head
   * res0: String = Hello
   */
  type MyInt = Int

  /**
   * scala> Main.square(4)
   * res0: Int = 16
   */
  private[sbt_doctest] def square(x: Int) = x * x

  /**
   * scala> ""
   * res0: String = ""
   *
   * scala> "\"\""
   * res1: String = ""
   *
   * scala> "a a"
   * res2: String = a a
   *
   * scala> "a "
   * res3: String = "a "
   *
   * scala> List("")
   * res0: List[String] = List("")
   *
   * scala> List("a")
   * res1: List[String] = List(a)
   *
   * scala> List("a ")
   * res2: List[String] = List("a ")
   *
   * scala> List("a a")
   * res3: List[String] = List(a a)
   */
  lazy val fortytwo = 21 * 2

  /**
   * {{{
   * >>> Main.helloWorld
   * Hello
   * <BLANKLINE>
   * World
   *
   * scala> Main.helloWorld
   * res0: String =
   * Hello
   * <BLANKLINE>
   * World
   *
   * >>> """Hello
   * ... World"""
   * Hello
   * World
   *
   * scala> """Hello
   *      | World"""
   * res1: String =
   * Hello
   * World
   * }}}
   */
  val helloWorld = "Hello\n\nWorld"

  /**
   * {{{
   * >>> Main.html
   * &lt;html&gt;&lt;/html&gt;
   * }}}
   */
  val html = "&lt;html&gt;&lt;/html&gt;"

  /**
   * Escape-character method
   *
   * {{{
   * >>> Main \ 1
   * 2
   * }}}
   */
  def \(x: Int) = x + x
}
