package com.github.tkawachi.doctest

object StringUtil {

  /**
   * Escape a string.
   *
   * {{{
   * // In scala repl style
   * scala> import com.github.tkawachi.doctest.StringUtil._
   * import com.github.tkawachi.doctest.StringUtil._
   *
   * scala> escapeDoubleQuote("""aaa"bbb\cc"""")
   * res2: String = aaa\"bbb\\cc\"
   *
   * // In Python style
   * >>> escapeDoubleQuote("""aaa"bbb\cc"""")
   * aaa\"bbb\\cc\"
   *
   * // Number of double quotes will be not changed.
   * prop> (s: String) =>
   *     |   escapeDoubleQuote(s).filter(_ == '"').size >= s.filter(_ == '"').size
   * }}}
   *
   * @param s An original string.
   * @return An escaped string.
   */
  def escapeDoubleQuote(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")

  /**
   * {{{
   * >>> import StringUtil._
   * >>> escapeLineSep("Hello,\nWorld")
   * Hello,\nWorld
   *
   * >>> escapeLineSep("Hello,\rWorld")
   * Hello,\rWorld
   * }}}
   */
  def escapeLineSep(s: String): String = s.replace("\n", "\\n").replace("\r", "\\r")

  def escape(s: String): String = escapeLineSep(escapeDoubleQuote(s))

  /**
   * {{{
   * >>> StringUtil.indent("Hello\nWorld", "[indent]")
   * [indent]Hello
   * [indent]World
   * }}}
   */
  def indent(target: String, indentation: String): String = {
    val pattern = """(\n\r?)""".r
    indentation + pattern.replaceAllIn(target, _.group(0) + indentation)
  }

  /**
   * {{{
   * prop> (s: String) => s.startsWith(StringUtil.truncate(s).dropRight(4))
   * }}}
   */
  def truncate(s: String): String = {
    val maxLength = 60
    val lines = s.split("[\\r\\n]")
    val suffix =
      if (lines.size > 1 || lines.headOption.exists(_.length > maxLength)) " ..."
      else ""

    lines.headOption.fold("")(_.take(maxLength) + suffix)
  }
}
