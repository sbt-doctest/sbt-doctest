# sbt-doctest

Plugin for [sbt](https://www.scala-sbt.org) that generates tests from examples
in ScalaDoc.

## Install

To use this plugin, add it to your `project/plugins.sbt`.

```scala
addSbtPlugin("io.github.sbt-doctest" % "sbt-doctest" % "0.12.0")
```

This plugin supports sbt 1.x and 2.x.

It's automatically enabled.

sbt-doctest allows you to choose which test library to use by `doctestTestFramework`.
By default, the tests are generated for ScalaCheck.
The test libraries need to be added separately to libraryDependencies.

### Using ScalaCheck

If you are using [``ScalaCheck``](https://github.com/typelevel/scalacheck), add the following lines to your ``build.sbt``:

```scala
libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.19.0" % Test
)

doctestTestFramework := DoctestTestFramework.ScalaCheck // Default value for doctestTestFramework
```

### Using ScalaTest

If you are using [``ScalaTest``](https://github.com/scalatest/scalatest), add the following lines to your ``build.sbt``:

```scala
// ScalaTest 3.2
// ScalaTest 3.2 has been modularized. sbt-doctest generates tests using FunSpec.
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest-funspec" % "3.2.19" % Test,
  "org.scalatestplus" %% "scalacheck-1-18" % "3.2.19.0" % Test
)

// ScalaTest 3.1
libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.1.2" % Test,
  "org.scalatestplus" %% "scalacheck-1-14" % "3.1.2.0" % Test
)

// ScalaTest 3.0
libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"  % "3.0.9"  % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
)

doctestTestFramework := DoctestTestFramework.ScalaTest
```

Due to changes in the ScalaTest API, the test code generated will be slightly different depending on the version of
ScalaTest. sbt-doctest automatically determines which test code to generate by looking at `libraryDependencies`.

If you want to explicitly specify the version of ScalaTest to be generated, you can specify `doctestScalaTestVersion`.

```scala
doctestScalaTestVersion := Some("3.2.15")
```

### Using Specs2

If you are using [``Specs2``](https://github.com/etorreborre/specs2), add the following lines to your ``build.sbt``:

```scala
libraryDependencies ++= Seq(
  "org.specs2" %% "specs2-scalacheck" % "4.21.0" % Test
)

doctestTestFramework := DoctestTestFramework.Specs2
```

### Using Minitest

If you are using [``Minitest``](https://github.com/monix/minitest), add the following lines to your ``build.sbt``:
```scala
libraryDependencies ++= Seq(
  "io.monix" %% "minitest" % "2.9.5" % Test,
  "io.monix" %% "minitest-laws" % "2.9.6" % Test
)

doctestTestFramework := DoctestTestFramework.Minitest
```

### Using µTest

If you are using [``µTest``](https://github.com/com-lihaoyi/utest), add the following lines to your ``build.sbt``:
```scala
libraryDependencies ++= Seq(
  "com.lihaoyi" %% "utest" % "0.8.4" % Test
)

doctestTestFramework := DoctestTestFramework.MicroTest
```

### Using MUnit

If you are using [``MUnit``](https://scalameta.org/munit/), add the following lines to your ``build.sbt``:
```scala
libraryDependencies ++= Seq(
  "org.scalameta" %% "munit" % "0.7.29" % Test
)

doctestTestFramework := DoctestTestFramework.Munit
```

In case you are [configuring µTest or using a custom test framework](https://github.com/com-lihaoyi/utest#configuring-utest), you can do something like this below in your ``build.sbt``:
```scala
testFrameworks -= new TestFramework("utest.runner.Framework")
testFrameworks += new TestFramework("test.utest.MyCustomFramework")
```
which means that you are removing ``utest.runner.Framework`` inserted automatically for you by ``sbt-doctest`` and you are inserting your own custom test framework, provided by class _test.utest.MyCustomFramework_, in this example.

#### Caveats

There are still dependencies from ``ScalaTest`` and/or ``ScalaCheck`` when property checks are employed.

The difficulty can be circumvented for the time being by providing all dependencies in ``build.sbt``, like
shown in the example below which uses ``uTest`` with property checks, which require ``ScalaTest`` and ``ScalaCheck`` as well:

```scala
libraryDependencies ++= Seq(
  "com.lihaoyi"    %% "utest"      % "0.8.4"  % Test,
  "org.scalatest"  %% "scalatest"  % "3.0.9"  % Test,
  "org.scalacheck" %% "scalacheck" % "1.19.0" % Test
)
      
doctestTestFramework := DoctestTestFramework.MicroTest
```

## Usage

sbt-doctest will generate tests from
doctests in ScalaDoc comments. These tests are automatically generated and
run when sbt's `test` task is invoked.

Here is an example that shows the different doctest styles that are supported
by the plugin:

```scala
object Test {

  /**
   * A sample function.
   *
   * {{{
   * # Python style
   * >>> Test.f(10)
   * 20
   *
   * # Scala REPL style
   * scala> Test.f(20)
   * res1: Int = 40
   *
   * # Property based test
   * prop> (i: Int) => Test.f(i) == (i * 2)
   * }}}
   */
  def f(x: Int) = x + x
}
```

It also supports multi-line inputs:

```scala
/**
 * {{{
 * # Python style
 * >>> Test.f(
 * ...   10
 * ... )
 * 20
 *
 * # Scala REPL style
 * scala> Test.f(
 *      |   20
 *      | )
 * res1: Int = 40
 *
 * # Property based test
 * prop> (i: Int) =>
 *     |   Test.f(i) == (i * 2)
 * }}}
 */
def f(x: Int) = x + x
```

Please use `<BLANKLINE>` when an output contains blank lines.

```scala
/**
 * {{{
 * # Python style
 * >>> Test.helloWorld
 * Hello
 * <BLANKLINE>
 * World
 *
 * # Scala REPL style
 * scala> Test.helloWorld
 * res0: String =
 * Hello
 * <BLANKLINE>
 * World
 * }}}
 */
def helloWorld = "Hello\n\nWorld"
```

## Ignoring Some Files

If you don't want to generate doctests for some of your sources, then specify a regex pattern:

```scala
doctestIgnoreRegex := Some(".*SomeClass.scala")
```

Source files with canonical paths (using UNIX separator - `/`) matching the regex, will not be used for doctest generation.

## Only Code Blocks Mode

If you all you need is to check that code in Scaladoc code blocks (text inside `{{{}}}`) compiles),
you can enable only code blocks mode by setting `doctestOnlyCodeBlocksMode` to `true`:

```scala
doctestOnlyCodeBlocksMode := true
```

Generated tests won't have any assertions, unless they are present in your Scaladocs.

## HTML Entities

Often when documenting libraries that work with HTML you need to encode HTML entities so that they will be displayed in browsers.

However, `sbt-doctest` ignores these and attempts to compare encoded HTML with unencoded HTML entities. You can fix this by enabling decoding of HTML entities. Just add the following setting to your `build.sbt`:

```scala
doctestDecodeHtmlEntities := true
```

Now the following should pass:

```scala
  /**
   * {{{
   * >>> Main.html
   * &lt;html&gt;&lt;/html&gt;
   * }}}
   */
  val html = "<html></html>"
```

## Markdown

Also supports code examples in Markdown documentation. To enable add the following to your `build.sbt`:

```scala
doctestMarkdownEnabled := true
```

Any code blocks that start with the ```` ```scala```` markdown directive will be parsed.
It searches `*.md` under `baseDirectory` by default. It can be configured by
`doctestMarkdownPathFinder`.

```scala
// default
doctestMarkdownPathFinder := baseDirectory.value * "*.md"

// search doc/ recursively
doctestMarkdownPathFinder := baseDirectory.value * "doc" ** "*.md" 
```

See [an example markdown](https://github.com/sbt-doctest/sbt-doctest/blob/master/src/test/resources/ScalaText.md).

## Compatibility with other sbt plugins

If you happen to have other plugins that use [scalameta](https://github.com/scalameta/scalameta)
 (e.g. [sbt-scalafmt](https://github.com/scalameta/sbt-scalafmt))
please make sure those plugins don't bring conflicting version of scalameta.

At this moment sbt-scalafmt need to be of version 1.6.x at least.

## License

MIT
