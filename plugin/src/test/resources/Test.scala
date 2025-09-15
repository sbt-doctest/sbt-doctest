/**
 * Test class
 */
class Test {
  /**
   * A function.
   *
   * >>> new Test().f(10)
   * 20
   *
   * >>> "hello, " + "world!"
   * hello, world!
   */
  def f(x: Int) = x + x

  /** Ascii method
   * scala> new Test() += 1
   * 2
   */
  def +=(x: Int) = x + x

  /**
    * Doc on val
    */
  val x = 23

  /**
    * Doc on var
    */
  var z = "zzz"
}
