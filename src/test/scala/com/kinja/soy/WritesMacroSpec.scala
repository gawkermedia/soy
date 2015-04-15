package com.kinja.soy

import org.scalatest._

case class EmptyClass()
case class OptionClass(i: Int, s: Option[String])
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

class WritesMacroSpec extends FlatSpec with Matchers {
  implicit val z: SoyWrites[OptionClass] = Soy.writes[OptionClass]
  implicit val b: SoyWrites[RecOptionClass] = Soy.writes[RecOptionClass]
  implicit val c: SoyWrites[RecListClass] = Soy.writes[RecListClass]
  implicit val d: SoyWrites[RecSetClass] = Soy.writes[RecSetClass]
  implicit val f: SoyWrites[RecMapClass] = Soy.writes[RecMapClass]
  implicit val g: SoyWrites[Single] = Soy.writes[Single]
  implicit val h: SoyWrites[Pair] = Soy.writes[Pair]
  implicit val i: SoyWrites[Triple] = Soy.writes[Triple]
  implicit val j: SoyWrites[Quad] = Soy.writes[Quad]
  implicit val k: SoyWrites[Quint] = Soy.writes[Quint]
  implicit val l: SoyWrites[Sext] = Soy.writes[Sext]
  implicit val m: SoyWrites[Sept] = Soy.writes[Sept]
  implicit val n: SoyWrites[Oct] = Soy.writes[Oct]
  implicit val o: SoyWrites[Non] = Soy.writes[Non]
  implicit val p: SoyWrites[Dec] = Soy.writes[Dec]
  implicit val q: SoyWrites[EmptyClass] = Soy.writes[EmptyClass]
  implicit val r: SoyWrites[Inner] = Soy.writes[Inner]
  implicit val s: SoyWrites[Outer] = Soy.writes[Outer]

  "Soy.writes" should "support empty case classes" in {
    Soy.toSoy(EmptyClass()) should be(Soy.map())
  }

  it should "support nested case classes" in {
    val inner = Inner(1)
    val outer = Outer(2, inner)
    Soy.toSoy(outer) should be(Soy.map("i" -> 2, "inner" -> Soy.map("i" -> 1)))
  }

  it should "support case classes with optional members" in {
    Soy.toSoy(OptionClass(5, None)) should be(Soy.map("i" -> 5, "s" -> SoyNull))
    Soy.toSoy(OptionClass(5, Some("foo"))) should be(Soy.map("i" -> 5, "s" -> "foo"))
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
}
