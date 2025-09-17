package com.github.tkawachi.doctest

import java.lang.Class as jClass
import scala.collection.SortedOps
import scala.collection.StrictOptimizedIterableOps
import scala.collection.StringOps
import scala.collection.StringView
import scala.collection.View
import scala.collection.immutable.NumericRange
import scala.collection.mutable.StringBuilder
import scala.language.reflectiveCalls
import scala.math.min
import scala.runtime.BoxedUnit

object DoctestRuntime {
  private type AnyConstr[X] = Any

  private def array_length(xs: AnyRef): Int = java.lang.reflect.Array.getLength(xs)

  private implicit class IterableOps[A](private val self: Iterable[A]) extends AnyVal {
    def collectionClassName: String =
      self.asInstanceOf[{ def collectionClassName: String }].collectionClassName
  }

  private def isArray(x: Any, atLevel: Int = 1): Boolean =
    x != null && isArrayClass(x.getClass, atLevel)

  private def isArrayClass(clazz: jClass[?], atLevel: Int): Boolean =
    clazz.isArray && (atLevel == 1 || isArrayClass(clazz.getComponentType, atLevel - 1))

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
      case _: SortedOps[?, ?] => true
      // StringBuilder(a, b, c) and similar not so attractive
      case _: StringView | _: StringOps | _: StringBuilder => true
      // Don't want to evaluate any elements in a view
      case _: View[?] => true
      // Node extends NodeSeq extends Seq[Node] and MetaData extends Iterable[MetaData]
      // -> catch those by isXmlNode and isXmlMetaData.
      // Don't want to a) traverse infinity or b) be overly helpful with peoples' custom
      // collections which may have useful toString methods - ticket #3710
      // or c) print AbstractFiles which are somehow also Iterable[AbstractFile]s.
      case x: Iterable[?] =>
        (!x.isInstanceOf[StrictOptimizedIterableOps[?, AnyConstr, ?]]) || !isScalaClass(x) || isScalaCompilerClass(
          x
        ) || isXml(x.getClass)
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
        (0 until min(array_length(x), maxElements)).map(_ => "()").mkString("Array(", ", ", ")")
      else
        x.asInstanceOf[Array[?]].iterator.take(maxElements).map(inner).mkString("Array(", ", ", ")")
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
        x.iterator.take(maxElements).map(mapInner).mkString(x.collectionClassName + "(", ", ", ")")
      case x: Iterable[?] => x.iterator.take(maxElements).map(inner).mkString(x.collectionClassName + "(", ", ", ")")
      case x: Product1[?] if isTuple(x) => "(" + inner(x._1) + ",)" // that special trailing comma
      case x: Product if isTuple(x) => x.productIterator.map(inner).mkString("(", ",", ")")
      case x => x.toString
    }

    // The try/catch is defense against iterables which aren't actually designed
    // to be iterated, such as some scala.tools.nsc.io.AbstractFile derived classes.
    try inner(arg)
    catch {
      case _: UnsupportedOperationException | _: AssertionError => "" + arg
    }
  }

  def replStringOf(arg: Any): String = {
    val res = stringOf(arg, 1000) match {
      case null => "null toString"
      case s if s.indexOf('\n') >= 0 => "\n" + s
      case s => s
    }
    if (res.headOption.contains('\n')) res.drop(1) else res
  }
}
