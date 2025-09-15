package com.github.tkawachi.doctest

import scala.collection.{
  AbstractIterator,
  AnyConstr,
  SortedOps,
  StrictOptimizedIterableOps,
  StringOps,
  StringView,
  View
}
import scala.collection.generic.IsIterable
import scala.collection.immutable.{ArraySeq, NumericRange}
import scala.collection.mutable.StringBuilder
import scala.math.min
import scala.reflect.{ClassTag, classTag}
import java.lang.{Class => jClass}
import java.lang.reflect.{Method => JMethod}
import scala.language.reflectiveCalls
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

  private def isArrayClass(clazz: jClass[_], atLevel: Int): Boolean =
    clazz.isArray && (atLevel == 1 || isArrayClass(clazz.getComponentType, atLevel - 1))

  private def stringOf(arg: Any, maxElements: Int): String = {
    def packageOf(x: AnyRef) = ""

    def isScalaClass(x: AnyRef) = packageOf(x) startsWith "scala."
    def isScalaCompilerClass(x: AnyRef) = packageOf(x) startsWith "scala.tools.nsc."

    // includes specialized subclasses and future proofed against hypothetical TupleN (for N > 22)
    def isTuple(x: Any) = x != null && x.getClass.getName.startsWith("scala.Tuple")

    // We use reflection because the scala.xml package might not be available
    def isSubClassOf(potentialSubClass: Class[_], ofClass: String) = false

    def isXmlNode(potentialSubClass: Class[_]) = isSubClassOf(potentialSubClass, "scala.xml.Node")
    def isXmlMetaData(potentialSubClass: Class[_]) = isSubClassOf(potentialSubClass, "scala.xml.MetaData")

    // When doing our own iteration is dangerous
    def useOwnToString(x: Any) = x match {
      // Range/NumericRange have a custom toString to avoid walking a gazillion elements
      case _: Range | _: NumericRange[_] => true
      // Sorted collections to the wrong thing (for us) on iteration - ticket #3493
      case _: SortedOps[_, _] => true
      // StringBuilder(a, b, c) and similar not so attractive
      case _: StringView | _: StringOps | _: StringBuilder => true
      // Don't want to evaluate any elements in a view
      case _: View[_] => true
      // Node extends NodeSeq extends Seq[Node] and MetaData extends Iterable[MetaData]
      // -> catch those by isXmlNode and isXmlMetaData.
      // Don't want to a) traverse infinity or b) be overly helpful with peoples' custom
      // collections which may have useful toString methods - ticket #3710
      // or c) print AbstractFiles which are somehow also Iterable[AbstractFile]s.
      case x: Iterable[_] =>
        (!x.isInstanceOf[StrictOptimizedIterableOps[_, AnyConstr, _]]) || !isScalaClass(x) || isScalaCompilerClass(
          x
        ) || isXmlNode(x.getClass) || isXmlMetaData(x.getClass)
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
        x.asInstanceOf[Array[_]].iterator.take(maxElements).map(inner).mkString("Array(", ", ", ")")
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
      case x: scala.collection.Map[_, _] =>
        x.iterator.take(maxElements).map(mapInner).mkString(x.collectionClassName + "(", ", ", ")")
      case x: Iterable[_] => x.iterator.take(maxElements).map(inner).mkString(x.collectionClassName + "(", ", ", ")")
      case x: Product1[_] if isTuple(x) => "(" + inner(x._1) + ",)" // that special trailing comma
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

  def replStringOf(arg: Any): String =
    stringOf(arg, 1000) match {
      case null => "null toString"
      case s if s.indexOf('\n') >= 0 => "\n" + s + "\n"
      case s => s + "\n"
    }
}
