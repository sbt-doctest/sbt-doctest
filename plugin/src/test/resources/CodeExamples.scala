package examples.inda.house

class HasCodeExamples {

  /**
    * This method is very nifty and can be used like this:
    *
    * {{{
    *   val i = ff(5)
    * }}}
    *
    * `i` should be equal to 5. Let's check
    *
    * {{{
    *   require(i == 5)
    * }}}
    *
    *
    * Works!
    */
  def ff(i: Int) = i

  /**
    * Here's a multiline code block
    *
    * {{{
    *   val i = fff(5)
    *   i match {
    *     case 10 => "yep!"
    *     case _ => "boo"
    *   }
    * }}}
    */
  def fff(i: Int) = i + i

  /**
    * call me twice
    * >>> i_love_py(1 + 2 +
    * ... 3 +
    * ... 4 + 5
    * 15
    */
  def i_love_py(i: Int): Int = i
}