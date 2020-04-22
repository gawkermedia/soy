package com.kinja.soy

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class Id[A](key: Int)

case class HasId(id: Id[HasId])

case class EmptyClass()
case class OptionClass(i: Int, s: Option[String])
case class OptionClass2(i: Int, s: Option[OptionClass])
case class RecOptionClass(i: Int, b: Option[RecOptionClass])
case class RecListClass(i: Int, b: List[RecListClass])
case class RecSetClass(i: Int, b: Set[RecSetClass])
case class RecMapClass(i: Int, b: Map[String, RecMapClass])
case class Single(i: Int)
case class Pair(i: Int, d: Double)
case class Triple(i: Int, d: Double, l: Long)
case class Quad(i: Int, d: Double, l: Long, s: String)
case class Quint(a: Int, b: Int, c: Int, d: Int, e: Int)
case class Sext(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int)
case class Sept(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int)
case class Oct(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int)
case class Non(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int, i: Int)
case class Dec(a: Int, b: Int, c: Int, d: Int, e: Int, f: Int, g: Int, h: Int, i: Int, j: Int)
case class Inner(i: Int)
case class Outer(i: Int, inner: Inner)
case class ParamClass[A](i: Int, a: A)
case class TwoParamClass[A, B](a: A, b: B)
case class GenericOptionClass[A](oa: Option[A])
case class MultipleApplies(i: Int)
object MultipleApplies {
  def apply(s: String): MultipleApplies = apply(s.length)
}

class WritesMacroSpec extends AnyFlatSpec with Matchers {
  implicit def Id_Soy[A] = new SoyWrites[Id[A]] {
    def toSoy(id: Id[A]): SoyValue = SoyInt(id.key)
  }
  implicit val z = Soy.writes[OptionClass]
  implicit val y = Soy.writes[OptionClass2]
  implicit val b = Soy.writes[RecOptionClass]
  implicit val c = Soy.writes[RecListClass]
  implicit val d = Soy.writes[RecSetClass]
  implicit val f = Soy.writes[RecMapClass]
  implicit val g = Soy.writes[Single]
  implicit val h = Soy.writes[Pair]
  implicit val i = Soy.writes[Triple]
  implicit val j = Soy.writes[Quad]
  implicit val k = Soy.writes[Quint]
  implicit val l = Soy.writes[Sext]
  implicit val m = Soy.writes[Sept]
  implicit val n = Soy.writes[Oct]
  implicit val o = Soy.writes[Non]
  implicit val p = Soy.writes[Dec]
  implicit val q = Soy.writes[EmptyClass]
  implicit val r = Soy.writes[Inner]
  implicit val s = Soy.writes[Outer]
  implicit def t[A: SoyWrites]: SoyWrites[ParamClass[A]] = Soy.writes[ParamClass[A]]
  implicit def u[A: SoyWrites, B: SoyWrites]: SoyWrites[TwoParamClass[A, B]] = Soy.writes[TwoParamClass[A, B]]
  implicit def v[A: SoyWrites]: SoyWrites[GenericOptionClass[A]] = Soy.writes[GenericOptionClass[A]]
  implicit val w = Soy.writes[MultipleApplies]
  implicit val x = Soy.writes[HasId]

  "Soy.writes" should "support empty case classes" in {
    Soy.toSoy(EmptyClass()) should be(Soy.map())
  }

  it should "support phantom types" in {
    val id = Id[HasId](1)
    val hasId = HasId(id)
    Soy.toSoy(hasId) should be(Soy.map("id" -> 1))
  }

  it should "support nested case classes" in {
    val inner = Inner(1)
    val outer = Outer(2, inner)
    Soy.toSoy(outer) should be(Soy.map("i" -> 2, "inner" -> Soy.map("i" -> 1)))
  }

  it should "support case classes with optional members" in {
    val o1 = OptionClass(5, None)
    val o2 = OptionClass(5, Some("foo"))
    val oo1 = OptionClass2(5, None)
    val oo2 = OptionClass2(5, Some(o1))
    Soy.toSoy(o1) should be(Soy.map("i" -> 5, "s" -> SoyNull))
    Soy.toSoy(o2) should be(Soy.map("i" -> 5, "s" -> "foo"))
    Soy.toSoy(oo1) should be(Soy.map("i" -> 5, "s" -> SoyNull))
    Soy.toSoy(oo2) should be(Soy.map("i" -> 5, "s" -> Soy.map("i" -> 5, "s" -> SoyNull)))
  }

  it should "support recursive case classes with optional members" in {
    val b1 = RecOptionClass(1, None)
    val b2 = RecOptionClass(2, Some(b1))
    Soy.toSoy(b1) should be(Soy.map("i" -> 1, "b" -> SoyNull))
    Soy.toSoy(b2) should be(Soy.map("i" -> 2, "b" -> Soy.map("i" -> 1, "b" -> SoyNull)))
  }

  it should "support recursive case classes with list members" in {
    val b1 = RecListClass(1, Nil)
    val b2 = RecListClass(2, List(b1))
    Soy.toSoy(b1) should be(Soy.map("i" -> 1, "b" -> Soy.list()))
    Soy.toSoy(b2) should be(Soy.map("i" -> 2, "b" -> Soy.list(Soy.map("i" -> 1, "b" -> Soy.list()))))
  }

  it should "support recursive case classes with set members" in {
    val b1 = RecSetClass(1, Set.empty)
    val b2 = RecSetClass(2, Set(b1))
    Soy.toSoy(b1) should be(Soy.map("i" -> 1, "b" -> Soy.list()))
    Soy.toSoy(b2) should be(Soy.map("i" -> 2, "b" -> Soy.list(Soy.map("i" -> 1, "b" -> Soy.list()))))
  }

  it should "support recursive case classes with map members" in {
    val b1 = RecMapClass(1, Map.empty)
    val b2 = RecMapClass(2, Map("b1" -> b1))
    Soy.toSoy(b1) should be(Soy.map("i" -> 1, "b" -> Soy.map()))
    Soy.toSoy(b2) should be(Soy.map("i" -> 2, "b" -> Soy.map("b1" -> Soy.map("i" -> 1, "b" -> Soy.map()))))
  }

  it should "support generic case classes" in {
    val b1 = Single(1)
    val b2 = Pair(1, 2.0)
    val b3 = ParamClass[Single](2, b1)
    val b4 = ParamClass(2, b2)
    Soy.toSoy(b3) should be(Soy.map("i" -> 2, "a" -> Soy.map("i" -> 1)))
    Soy.toSoy(b4) should be(Soy.map("i" -> 2, "a" -> Soy.map("i" -> 1, "d" -> 2.0)))
  }

  it should "support generic case classes with two parameters" in {
    val b1 = Single(1)
    val b2 = Pair(1, 2.0)
    val b3 = TwoParamClass(b1, b2)
    val b4 = TwoParamClass(b2, b1)
    Soy.toSoy(b3) should be(Soy.map("a" -> Soy.map("i" -> 1), "b" -> Soy.map("i" -> 1, "d" -> 2.0)))
    Soy.toSoy(b4) should be(Soy.map("a" -> Soy.map("i" -> 1, "d" -> 2.0), "b" -> Soy.map("i" -> 1)))
  }

  it should "support case classes with 1 member" in {
    Soy.toSoy(Single(5)) should be(Soy.map("i" -> 5))
  }

  it should "support case classes with 2 members" in {
    Soy.toSoy(Pair(1, 2.0)) should be(Soy.map("i" -> 1, "d" -> 2.0))
  }

  it should "support case classes with 3 members" in {
    Soy.toSoy(Triple(1, 2.0, 3L)) should be(Soy.map("i" -> 1, "d" -> 2.0, "l" -> 3L))
  }

  it should "support case classes with 4 members" in {
    Soy.toSoy(Quad(1, 2.0, 3L, "4")) should be(Soy.map("i" -> 1, "d" -> 2.0, "l" -> 3L, "s" -> "4"))
  }

  it should "support case classes with 5 members" in {
    val clazz = Quint(1, 2, 3, 4, 5)
    val map = Soy.map(
      "a" -> 1,
      "b" -> 2,
      "c" -> 3,
      "d" -> 4,
      "e" -> 5)
    Soy.toSoy(clazz) should be(map)
  }

  it should "support case classes with 6 members" in {
    val clazz = Sext(1, 2, 3, 4, 5, 6)
    val map = Soy.map(
      "a" -> 1,
      "b" -> 2,
      "c" -> 3,
      "d" -> 4,
      "e" -> 5,
      "f" -> 6)
    Soy.toSoy(clazz) should be(map)
  }

  it should "support case classes with 7 members" in {
    val clazz = Sept(1, 2, 3, 4, 5, 6, 7)
    val map = Soy.map(
      "a" -> 1,
      "b" -> 2,
      "c" -> 3,
      "d" -> 4,
      "e" -> 5,
      "f" -> 6,
      "g" -> 7)
    Soy.toSoy(clazz) should be(map)
  }

  it should "support case classes with 8 members" in {
    val clazz = Oct(1, 2, 3, 4, 5, 6, 7, 8)
    val map = Soy.map(
      "a" -> 1,
      "b" -> 2,
      "c" -> 3,
      "d" -> 4,
      "e" -> 5,
      "f" -> 6,
      "g" -> 7,
      "h" -> 8)
    Soy.toSoy(clazz) should be(map)
  }

  it should "support case classes with 9 members" in {
    val clazz = Non(1, 2, 3, 4, 5, 6, 7, 8, 9)
    val map = Soy.map(
      "a" -> 1,
      "b" -> 2,
      "c" -> 3,
      "d" -> 4,
      "e" -> 5,
      "f" -> 6,
      "g" -> 7,
      "h" -> 8,
      "i" -> 9)
    Soy.toSoy(clazz) should be(map)
  }

  it should "support case classes with 10 members" in {
    val clazz = Dec(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val map = Soy.map(
      "a" -> 1,
      "b" -> 2,
      "c" -> 3,
      "d" -> 4,
      "e" -> 5,
      "f" -> 6,
      "g" -> 7,
      "h" -> 8,
      "i" -> 9,
      "j" -> 10)
    Soy.toSoy(clazz) should be(map)
  }

  it should "support case classes with generic members" in {
    val clazz = GenericOptionClass(Option(5))
    Soy.toSoy(clazz) should be(Soy.map("oa" -> 5))
  }
}
