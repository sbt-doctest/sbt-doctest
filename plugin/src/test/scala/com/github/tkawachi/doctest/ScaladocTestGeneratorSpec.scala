package com.github.tkawachi.doctest

import utest._

object ScaladocTestGeneratorSpec extends TestSuite {

  import ScaladocTestGenerator.findEncoding

  val tests = utest.Tests {

    "findEncoding should work" - {
      assert(
        findEncoding(Nil).isEmpty,
        findEncoding(Seq("x")).isEmpty,
        findEncoding(Seq("x", "y")).isEmpty,
        findEncoding(Seq("-encoding")).isEmpty,
        findEncoding(Seq("x", "-encoding")).isEmpty,
        findEncoding(Seq("x", "y", "-encoding")).isEmpty
      )

      assert(
        findEncoding(Seq("-encoding", "utf-8")).contains("utf-8"),
        findEncoding(Seq("-encoding", "utf-8", "x")).contains("utf-8"),
        findEncoding(Seq("-encoding", "utf-8", "x", "y")).contains("utf-8")
      )
    }

  }
}
