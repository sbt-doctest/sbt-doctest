package com.github.tkawachi.doctest

object DoctestRuntime {
  def replStringOf(arg: Any): String = {
    val res = scala.runtime.ScalaRunTime.replStringOf(arg, 1000).dropRight(1)
    if (res.headOption.contains('\n')) res.drop(1) else res
  }
}
