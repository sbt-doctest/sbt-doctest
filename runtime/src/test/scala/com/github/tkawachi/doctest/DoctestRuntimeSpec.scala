package com.github.tkawachi.doctest

import org.scalatest.funspec.AnyFunSpec

class DoctestRuntimeSpec extends AnyFunSpec {
  it("List") {
    assert(DoctestRuntime.replStringOf(List(1, 2, 3)) == "List(1, 2, 3)")
  }
  it("Range") {
    assert(DoctestRuntime.replStringOf(1 to 3) == "Range 1 to 3")
  }
  it("Array") {
    assert(DoctestRuntime.replStringOf(Array(1, 2, 3)) == "Array(1, 2, 3)")
  }
  it("Map") {
    assert(DoctestRuntime.replStringOf(Map(1 -> 2, 3 -> 4)) == "Map(1 -> 2, 3 -> 4)")
  }
  it("String") {
    assert(DoctestRuntime.replStringOf("abc") == "abc")
  }
  it("null") {
    assert(DoctestRuntime.replStringOf(null) == "null")
  }
  it("Tuple") {
    assert(DoctestRuntime.replStringOf((1, Array(2), 3)) == "(1,Array(2),3)")
  }
  it("xml") {
    assert(DoctestRuntime.replStringOf(<a>b</a>) == "<a>b</a>")
  }
}
