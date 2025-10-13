package sbt_doctest

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption

object Main {

  /**
   * ```
   * scala> sbt_doctest.Main.foo()
   * res0: Int = 2
   * ```
   */
  def foo(): Int = {
    Files.write(
      new File("x1.txt").toPath,
      "a\n".getBytes("UTF-8"),
      StandardOpenOption.APPEND
    )
    2
  }
}
