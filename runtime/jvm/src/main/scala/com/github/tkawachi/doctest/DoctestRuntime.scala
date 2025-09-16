package com.github.tkawachi.doctest

object DoctestRuntime {
  def replStringOf(arg: Any): String = scala.runtime.ScalaRunTime.replStringOf(arg, 1000)
}
