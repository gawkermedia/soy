package com.kinja.soy

import org.specs2.mutable._
import scala.collection.JavaConversions._
import com.google.template.soy.data.{ SoyListData, SoyMapData }

class SoyWritesSpec extends Specification {

  case class Simple(value: Int)

  case class Other(a: Int)

  case class Complex(a: Int, b: String, c: Long, d: Simple)

  class SimpleSoy extends SoyWrites[Simple] {
    def toSoy(simple: Simple): SoyValue = SoyString(simple.toString)
  }

  class ComplexSoy extends SoyWrites[Complex] {
    def toSoy(complex: Complex): SoyValue = Soy.map(
      "a" -> complex.a,
      "b" -> complex.b,
      "c" -> complex.c,
      "d" -> complex.d)
  }

  implicit val otherSoy = new SoyMapWrites[Other] {
    def toSoy(other: Other): SoyMap = Soy.map("a" -> other.a)
  }

  implicit val simpleSoy = new SimpleSoy
  implicit val complexSoy = new ComplexSoy

  val testData =
    Soy.map(
      "simples" -> Soy.list(Simple(1), Simple(2)),
      "meta" -> Soy.map(
        "title" -> "test title",
        "keywords" -> Soy.list("list", "of", "test", "keywords"),
        "user" -> Soy.map(
          "id" -> 9876543210L,
          "name" -> "test user",
          "posts" -> 250,
          "complex" -> Soy.list(
            Complex(5, "5", 5L, Simple(5)),
            Complex(6, "6", 6L, Simple(6)),
            Complex(7, "7", 7L, Simple(7))),
          "loggedIn" -> true),
        "features" -> Soy.map(
          "feature1" -> true,
          "feature2" -> false,
          "feature3" -> true)),
      "views" -> 1349,
      "footerHtml" -> SoyNull)

  "There" should {
    "be an implicit SoyWrites from String to SoyString" in {
      val stringValue: String = "test string"
      val soyValue = Soy.toSoy(stringValue)
      soyValue must beAnInstanceOf[SoyString]
      soyValue.build must_== stringValue
    }
    "be an implicit SoyWrites from Int to SoyInt" in {
      val intValue: Int = 12
      val soyValue = Soy.toSoy(intValue)
      soyValue must beAnInstanceOf[SoyInt]
      soyValue.build must_== intValue
    }
    "be an implicit SoyWrites from Short to SoyInt" in {
      val shortValue: Short = 12
      val soyValue = Soy.toSoy(shortValue)
      soyValue must beAnInstanceOf[SoyInt]
      soyValue.build must_== shortValue
    }
    "be an implicit SoyWrites from Byte to SoyInt" in {
      val byteValue: Byte = 12
      val soyValue = Soy.toSoy(byteValue)
      soyValue must beAnInstanceOf[SoyInt]
      soyValue.build must_== byteValue
    }
    "be an implicit SoyWrites from Double to SoyFloat" in {
      val doubleValue: Double = 12
      val soyValue = Soy.toSoy(doubleValue)
      soyValue must beAnInstanceOf[SoyFloat]
      soyValue.build must_== doubleValue
    }
    "be an implicit SoyWrites from Char to SoyString" in {
      val charValue: Char = '@'
      val soyValue = Soy.toSoy(charValue)
      soyValue must beAnInstanceOf[SoyString]
      soyValue.build must_== charValue.toString
    }
    "be an implicit SoyWrites from BigInt to SoyString" in {
      val bigIntValue: BigInt = 12
      val soyValue = Soy.toSoy(bigIntValue)
      soyValue must beAnInstanceOf[SoyString]
      soyValue.build must_== bigIntValue.toString
    }
    "be an implicit SoyWrites from BigDecimal to SoyString" in {
      val bigDecimalValue: BigDecimal = 12
      val soyValue = Soy.toSoy(bigDecimalValue)
      soyValue must beAnInstanceOf[SoyString]
      soyValue.build must_== bigDecimalValue.toString
    }
    "be an implicit SoyWrites from Boolean to SoyBoolean" in {
      val booleanValue: Boolean = true
      val soyValue = Soy.toSoy(booleanValue)
      soyValue must beAnInstanceOf[SoyBoolean]
      soyValue.build must_== booleanValue
    }

    "be an implicit SoyWrites from empty Array[Int] to SoyList" in {
      val listValue: Array[Int] = Array()
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[]"
    }
    "be an implicit SoyWrites from Array[Int] to SoyList" in {
      val listValue: Array[Int] = Array(1, 2, 3, 4, 5, 6)
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[1, 2, 3, 4, 5, 6]"
    }
    "be an implicit SoyWrites from Array[String] to SoyList" in {
      val listValue: Array[String] = Array("a", "b", "c")
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[a, b, c]"
    }
    "be an implicit SoyWrites from List[Int] to SoyList" in {
      val listValue: List[Int] = List(1, 2, 3, 4, 5, 6)
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[1, 2, 3, 4, 5, 6]"
    }
    "be an implicit SoyWrites from Seq[Int] to SoyList" in {
      val listValue: Seq[Int] = Seq(1, 2, 3, 4, 5, 6)
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[1, 2, 3, 4, 5, 6]"
    }
    "be an implicit SoyWrites from Set[Int] to SoyList" in {
      val listValue: Set[Int] = Set(1, 2, 3, 4, 5, 6)
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString.sorted must_== "[1, 2, 3, 4, 5, 6]".sorted
    }
    "be an implicit SoyWrites from Vector[Int] to SoyList" in {
      val listValue: Vector[Int] = Vector(1, 2, 3, 4, 5, 6)
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[1, 2, 3, 4, 5, 6]"
    }
    "be an implicit SoyWrites from Traversable[Int] to SoyList" in {
      val listValue: Traversable[Int] = List(1, 2, 3, 4, 5, 6)
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[1, 2, 3, 4, 5, 6]"
    }
    "be an implicit SoyWrites from Traversable[Simple] to SoyList" in {
      val listValue: Traversable[Simple] = List(Simple(1), Simple(2), Simple(3))
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[Simple(1), Simple(2), Simple(3)]"
    }
    "be an implicit SoyWrites from Traversable[Complex] to SoyList" in {
      val listValue: Traversable[Complex] = List(
        Complex(1, "a", 11L, Simple(111)),
        Complex(2, "b", 22L, Simple(222)),
        Complex(3, "c", 33L, Simple(333)))
      val soyValue = Soy.toSoy(listValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyList]
      built must beAnInstanceOf[SoyListData]
      built.toString must_== "[{a: 1, b: a, c: 11, d: Simple(111)}, {a: 2, b: b, c: 22, d: Simple(222)}, {a: 3, b: c, c: 33, d: Simple(333)}]"
    }

    "be an implicit SoyWrites from Map[String, Int] to SoyMap" in {
      val mapValue: Map[String, Int] = Map("a" -> 1, "b" -> 2, "c" -> 3)
      val soyValue = Soy.toSoy(mapValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyMap]
      built must beAnInstanceOf[SoyMapData]
      built.toString must_== "{a: 1, b: 2, c: 3}"
    }
    "be an implicit SoyWrites from Map[String, String] to SoyMap" in {
      val mapValue: Map[String, String] = Map("a" -> "1", "b" -> "2", "c" -> "3")
      val soyValue = Soy.toSoy(mapValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyMap]
      built must beAnInstanceOf[SoyMapData]
      built.toString must_== "{a: 1, b: 2, c: 3}"
    }
    "be an implicit SoyWrites from Map[String, Simple] to SoyMap" in {
      val mapValue: Map[String, Simple] = Map("a" -> Simple(1), "b" -> Simple(2), "c" -> Simple(3))
      val soyValue = Soy.toSoy(mapValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyMap]
      built must beAnInstanceOf[SoyMapData]
      built.toString must_== "{a: Simple(1), b: Simple(2), c: Simple(3)}"
    }
    "be an implicit SoyWrites from Map[String, Complex] to SoyMap" in {
      val mapValue: Map[String, Complex] = Map(
        "c1" -> Complex(1, "a", 11L, Simple(111)),
        "c2" -> Complex(2, "b", 22L, Simple(222)),
        "c3" -> Complex(3, "c", 33L, Simple(333)))
      val soyValue = Soy.toSoy(mapValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyMap]
      built must beAnInstanceOf[SoyMapData]
      built.toString must_== "{c1: {a: 1, b: a, c: 11, d: Simple(111)}, c2: {a: 2, b: b, c: 22, d: Simple(222)}, c3: {a: 3, b: c, c: 33, d: Simple(333)}}"
    }

    "be an implicit SoyWrites from Option[Int] with None to SoyNull" in {
      val optionValue: Option[Int] = None
      val soyValue = Soy.toSoy(optionValue)
      val built = soyValue.build
      soyValue must_== SoyNull
      built must beNull
    }
    "be an implicit SoyWrites from Option[Int] with value to Int" in {
      val optionValue: Option[Int] = Some(12)
      val soyValue = Soy.toSoy(optionValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyInt]
      built must_== optionValue.get
    }
    "be an implicit SoyWrites from Option[String] with None to SoyNull" in {
      val optionValue: Option[String] = None
      val soyValue = Soy.toSoy(optionValue)
      val built = soyValue.build
      soyValue must_== SoyNull
      built must beNull
    }
    "be an implicit SoyWrites from Option[String] with value to String" in {
      val optionValue: Option[String] = Some("test string")
      val soyValue = Soy.toSoy(optionValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyString]
      built must_== optionValue.get
    }
    "be an implicit SoyWrites from Option[Simple] with value to Simple" in {
      val simpleValue: Simple = Simple(12)
      val optionValue: Option[Simple] = Some(simpleValue)
      val soyValue = Soy.toSoy(optionValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyString]
      built must_== simpleValue.toString
    }
    "be an implicit SoyWrites from Option[Complex] with value to Complex" in {
      val complexValue: Complex = Complex(1, "a", 11L, Simple(111))
      val optionValue: Option[Complex] = Some(complexValue)
      val soyValue = Soy.toSoy(optionValue)
      val built = soyValue.build
      soyValue must beAnInstanceOf[SoyMap]
      built must beAnInstanceOf[SoyMapData]
      built.toString must_== "{a: 1, b: a, c: 11, d: Simple(111)}"
    }
  }

  "SoyMapWrites" should {
    "function as SoyWrites" in {
      val other = Other(5)
      val soyValue = Soy.toSoy(other)
      soyValue must beAnInstanceOf[SoyValue]
      soyValue must_== Soy.map("a" -> 5)
    }
  }

  "Soy.list()" should {
    "create an empty list" in {
      val soyValue = Soy.list()
      soyValue must_== SoyList(Seq())
    }
    "allow building complex lists using implicit writers" in {
      val none: Option[Int] = None
      val soyValue = Soy.list(1, 2, "a", "b", 0, 12.5, SoyList(Seq(SoyInt(6), SoyFloat(5))), 37.802, 'h', SoyMap(Map("a" -> SoyNull)), Some("hello"), none, 444L)
      val expected = SoyList(Seq(
        SoyInt(1),
        SoyInt(2),
        SoyString("a"),
        SoyString("b"),
        SoyInt(0),
        SoyFloat(12.5),
        SoyList(Seq(SoyInt(6), SoyFloat(5))),
        SoyFloat(37.802),
        SoyString("h"),
        SoyMap(Map(("a", SoyNull))),
        SoyString("hello"),
        SoyNull,
        SoyString("444")))
      soyValue must_== expected
    }
  }

  "Soy.map()" should {
    "create an empty map" in {
      val soyValue = Soy.map()
      soyValue must_== SoyMap(Map())
    }
    "allow building complex maps using implicit writers" in {
      val none: Option[Int] = None
      val soyValue = Soy.map(
        "a" -> 1,
        "b" -> 2,
        "c" -> 0,
        "d" -> 452.5,
        "e" -> 905.438098023,
        "f" -> SoyList(Seq(SoyFloat(56), SoyInt(4))),
        "g" -> SoyNull,
        "h" -> '&',
        "i" -> none,
        "i" -> SoyMap(Map("aa" -> SoyString("aa1"), "bb" -> SoyString("bb1"))),
        "j" -> Some(SoyList(Seq())))
      val expected = SoyMap(Map(
        "a" -> SoyInt(1),
        "b" -> SoyInt(2),
        "c" -> SoyInt(0),
        "d" -> SoyFloat(452.5f),
        "e" -> SoyFloat(905.438098023),
        "f" -> SoyList(Seq(SoyFloat(56.0), SoyInt(4))),
        "g" -> SoyNull,
        "h" -> SoyString("&"),
        "i" -> SoyNull,
        "i" -> SoyMap(Map("aa" -> SoyString("aa1"), "bb" -> SoyString("bb1"))),
        "j" -> SoyList(Seq())))
      soyValue must_== expected
    }
  }

  "Soy.list() and Soy.map()" should {
    "allow composing complex data structures" in {
      val soyValue = testData
      val expected = SoyMap(Map(
        "simples" -> SoyList(Seq(SoyString("Simple(1)"), SoyString("Simple(2)"))),
        "meta" -> SoyMap(Map(
          "title" -> SoyString("test title"),
          "keywords" -> SoyList(Seq(SoyString("list"), SoyString("of"), SoyString("test"), SoyString("keywords"))),
          "user" -> SoyMap(Map(
            "id" -> SoyString("9876543210"),
            "name" -> SoyString("test user"),
            "posts" -> SoyInt(250),
            "complex" -> SoyList(Seq(
              SoyMap(Map("a" -> SoyInt(5), "b" -> SoyString("5"), "c" -> SoyString("5"), "d" -> SoyString("Simple(5)"))),
              SoyMap(Map("a" -> SoyInt(6), "b" -> SoyString("6"), "c" -> SoyString("6"), "d" -> SoyString("Simple(6)"))),
              SoyMap(Map("a" -> SoyInt(7), "b" -> SoyString("7"), "c" -> SoyString("7"), "d" -> SoyString("Simple(7)"))))),
            "loggedIn" -> SoyBoolean(true))),
          "features" -> SoyMap(Map(
            "feature1" -> SoyBoolean(true),
            "feature2" -> SoyBoolean(false),
            "feature3" -> SoyBoolean(true))))),
        "views" -> SoyInt(1349),
        "footerHtml" -> SoyNull))
      soyValue must_== expected
    }
    "build the composed complex data structure correctly" in {
      val soyValue = testData
      val built = soyValue.build
      built must beAnInstanceOf[SoyMapData]
      built.toString must_== "{simples: [Simple(1), Simple(2)], meta: {title: test title, keywords: [list, of, test, keywords], user: {name: test user, posts: 250, id: 9876543210, loggedIn: true, complex: [{a: 5, b: 5, c: 5, d: Simple(5)}, {a: 6, b: 6, c: 6, d: Simple(6)}, {a: 7, b: 7, c: 7, d: Simple(7)}]}, features: {feature1: true, feature2: false, feature3: true}}, views: 1349, footerHtml: null}"
    }
  }
}
