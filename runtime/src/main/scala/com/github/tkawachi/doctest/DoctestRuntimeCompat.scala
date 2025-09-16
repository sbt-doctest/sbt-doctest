package com.github.tkawachi.doctest

private[doctest] object DoctestRuntimeCompat {

  /**
    * @see [[https://github.com/scala/scala/blob/8b9ec50f1b6bcf1c34/src/library/scala/runtime/ScalaRunTime.scala#L213-L214]]
    */
  val xmlClassNames: Set[String] = Set(
    "scala.xml.Atom",
    "scala.xml.Comment",
    "scala.xml.Elem",
    "scala.xml.EntityRef",
    "scala.xml.Group",
    "scala.xml.MetaData",
    "scala.xml.Node",
    "scala.xml.Null$",
    "scala.xml.PCData",
    "scala.xml.PrefixedAttribute",
    "scala.xml.ProcInstr",
    "scala.xml.SpecialNode",
    "scala.xml.Text",
    "scala.xml.Unparsed",
    "scala.xml.UnprefixedAttribute"
  )
}
