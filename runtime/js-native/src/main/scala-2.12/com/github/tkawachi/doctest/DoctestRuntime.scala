package com.github.tkawachi.doctest

import java.lang.Class as jClass
import scala.collection.GenIterable
import scala.collection.TraversableView
import scala.collection.generic.Sorted
import scala.collection.immutable.NumericRange
import scala.collection.immutable.StringLike
import scala.collection.mutable.WrappedArray
import scala.runtime.BoxedUnit

object DoctestRuntime {
  private def isArray(x: Any, atLevel: Int = 1): Boolean =
    x != null && isArrayClass(x.getClass, atLevel)

  private def isArrayClass(clazz: jClass[?], atLevel: Int): Boolean =
    clazz.isArray && (atLevel == 1 || isArrayClass(clazz.getComponentType, atLevel - 1))

  /** Get generic array length */
  private def array_length(xs: AnyRef): Int = java.lang.reflect.Array.getLength(xs)

  /** Given any Scala value, convert it to a String.
   *
   * The primary motivation for this method is to provide a means for
   * correctly obtaining a String representation of a value, while
   * avoiding the pitfalls of naively calling toString on said value.
   * In particular, it addresses the fact that (a) toString cannot be
   * called on null and (b) depending on the apparent type of an
   * array, toString may or may not print it in a human-readable form.
   *
   * @param   arg   the value to stringify
   * @return        a string representation of arg.
   */
  private def stringOf(arg: Any, maxElements: Int): String = {
    def isScalaClass(x: AnyRef) = x.getClass.getName startsWith "scala."
    def isScalaCompilerClass(x: AnyRef) = x.getClass.getName startsWith "scala.tools.nsc."

    // includes specialized subclasses and future proofed against hypothetical TupleN (for N > 22)
    def isTuple(x: Any) = x != null && x.getClass.getName.startsWith("scala.Tuple")

    def isXml(potentialSubClass: Class[?]) = DoctestRuntimeCompat.xmlClassNames(potentialSubClass.getName)

    // When doing our own iteration is dangerous
    def useOwnToString(x: Any) = x match {
      // Range/NumericRange have a custom toString to avoid walking a gazillion elements
      case _: Range | _: NumericRange[?] => true
      // Sorted collections to the wrong thing (for us) on iteration - ticket #3493
      case _: Sorted[?, ?] => true
      // StringBuilder(a, b, c) and similar not so attractive
      case _: StringLike[?] => true
      // Don't want to evaluate any elements in a view
      case _: TraversableView[?, ?] => true
      // Node extends NodeSeq extends Seq[Node] and MetaData extends Iterable[MetaData]
      // -> catch those by isXmlNode and isXmlMetaData.
      // Don't want to a) traverse infinity or b) be overly helpful with peoples' custom
      // collections which may have useful toString methods - ticket #3710
      // or c) print AbstractFiles which are somehow also Iterable[AbstractFile]s.
      case x: Traversable[?] =>
        !x.hasDefiniteSize || !isScalaClass(x) || isScalaCompilerClass(x) || isXml(x.getClass)
      // Otherwise, nothing could possibly go wrong
      case _ => false
    }

    // A variation on inner for maps so they print -> instead of bare tuples
    def mapInner(arg: Any): String = arg match {
      case (k, v) => inner(k) + " -> " + inner(v)
      case _ => inner(arg)
    }

    // Special casing Unit arrays, the value class which uses a reference array type.
    def arrayToString(x: AnyRef) = {
      if (x.getClass.getComponentType == classOf[BoxedUnit])
        0 until (array_length(x) min maxElements) map (_ => "()") mkString ("Array(", ", ", ")")
      else
        WrappedArray make x take maxElements map inner mkString ("Array(", ", ", ")")
    }

    // The recursively applied attempt to prettify Array printing.
    // Note that iterator is used if possible and foreach is used as a
    // last resort, because the parallel collections "foreach" in a
    // random order even on sequences.
    def inner(arg: Any): String = arg match {
      case null => "null"
      case "" => "\"\""
      case x: String => if (x.head.isWhitespace || x.last.isWhitespace) "\"" + x + "\"" else x
      case x if useOwnToString(x) => x.toString
      case x: AnyRef if isArray(x) => arrayToString(x)
      case x: scala.collection.Map[?, ?] =>
        x.iterator take maxElements map mapInner mkString (x.stringPrefix + "(", ", ", ")")
      case x: GenIterable[?] => x.iterator take maxElements map inner mkString (x.stringPrefix + "(", ", ", ")")
      case x: Traversable[?] => x take maxElements map inner mkString (x.stringPrefix + "(", ", ", ")")
      case x: Product1[?] if isTuple(x) => "(" + inner(x._1) + ",)" // that special trailing comma
      case x: Product if isTuple(x) => x.productIterator map inner mkString ("(", ",", ")")
      case x => x.toString
    }

    // The try/catch is defense against iterables which aren't actually designed
    // to be iterated, such as some scala.tools.nsc.io.AbstractFile derived classes.
    try inner(arg)
    catch {
      case _: UnsupportedOperationException | _: AssertionError => "" + arg
    }
  }

  /** stringOf formatted for use in a repl result. */
  def replStringOf(arg: Any): String = {
    val s = stringOf(arg, 1000)
    val nl = if (s contains "\n") "\n" else ""

    val res = nl + s
    if (res.headOption.contains('\n')) res.drop(1) else res
  }
}
