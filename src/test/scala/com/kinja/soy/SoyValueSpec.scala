package com.kinja.soy

import org.specs2.mutable._
import com.google.template.soy.data.{ SoyMapData, SoyListData }

class SoyValueSpec extends Specification {

  "SoyNull" should {
    "build null" in {
      val value: AnyRef = SoyNull.build
      value must beNull
    }
  }

  "SoyString" should {
    "build the wrapped String" in {
      val value: AnyRef = SoyString("test string").build
      value must beAnInstanceOf[String]
      value must_== "test string"
    }
    "build the wrapped null" in {
      val value: AnyRef = SoyString(null).build
      value must beNull
    }
  }

  "SoyBoolean" should {
    "build the wrapped Boolean(true)" in {
      val value: AnyVal = SoyBoolean(true).build
      value must_== true
    }
    "build the wrapped Boolean(false)" in {
      val value: AnyVal = SoyBoolean(false).build
      value must_== false
    }
  }

  "SoyInt" should {
    "build the wrapped +Int" in {
      val intValue: Int = 12
      val value: Any = SoyInt(intValue).build
      value must_== intValue
    }
    "build the wrapped 0" in {
      val intValue: Int = 0
      val value: Any = SoyInt(intValue).build
      value must_== intValue
    }
    "build the wrapped -Int" in {
      val intValue: Int = -34
      val value: Any = SoyInt(intValue).build
      value must_== intValue
    }
  }

  "SoyFloat" should {
    "build the wrapped +Float" in {
      val floatValue: Float = 12.5f
      val value: Any = SoyFloat(floatValue).build
      value must_== floatValue
    }
    "build the wrapped 0" in {
      val floatValue: Float = 0.0f
      val value: Any = SoyFloat(floatValue).build
      value must_== floatValue
    }
    "build the wrapped -Float" in {
      val floatValue: Float = -34.5f
      val value: Any = SoyFloat(floatValue).build
      value must_== floatValue
    }
  }

  "SoyDouble" should {
    "build the wrapped +Double" in {
      val doubleValue: Double = 12.5980
      val value: Any = SoyDouble(doubleValue).build
      value must_== doubleValue
    }
    "build the wrapped 0" in {
      val doubleValue: Double = 0.0
      val value: Any = SoyDouble(doubleValue).build
      value must_== doubleValue
    }
    "build the wrapped -Double" in {
      val doubleValue: Double = -34.2391
      val value: Any = SoyDouble(doubleValue).build
      value must_== doubleValue
    }
  }

  "SoyList" should {
    "build the wrapped Seq() as SoyListData" in {
      val seq = Seq[SoyValue]()
      val value: Any = SoyList(seq).build
      value must beAnInstanceOf[SoyListData]
      value.toString must_== "[]"
    }
    "build the wrapped Seq[SoyInt](items) as SoyListData" in {
      val seq = Seq[SoyInt](SoyInt(1), SoyInt(2), SoyInt(3))
      val value: Any = SoyList(seq).build
      value must beAnInstanceOf[SoyListData]
      value.toString must_== "[1, 2, 3]"
    }
    "build the wrapped Seq[SoyValue](items) as SoyListData" in {
      val seq = Seq[SoyValue](SoyInt(1), SoyString("a"), SoyNull, SoyBoolean(true))
      val value: Any = SoyList(seq).build
      value must beAnInstanceOf[SoyListData]
      value.toString must_== "[1, a, null, true]"
    }
    "build the wrapped Seq[SoyMap](items) as SoyListData" in {
      val seq = Seq[SoyMap](SoyMap(Seq("a" -> SoyInt(1))), SoyMap(Seq("b" -> SoyInt(2), "c" -> SoyInt(3))))
      val value: Any = SoyList(seq).build
      value must beAnInstanceOf[SoyListData]
      value.toString must_== "[{a: 1}, {b: 2, c: 3}]"
    }
  }

  "SoyMap" should {
    "build the wrapped Seq() as SoyMapData" in {
      val seq = Seq[(String, SoyValue)]()
      val value: Any = SoyMap(seq).build
      value must beAnInstanceOf[SoyMapData]
      value.toString must_== "{}"
    }
    "build the wrapped Seq[String, SoyInt](items) as SoyMapData" in {
      val seq = Seq[(String, SoyInt)]("a" -> SoyInt(1), "b" -> SoyInt(2), "c" -> SoyInt(3))
      val value: Any = SoyMap(seq).build
      value must beAnInstanceOf[SoyMapData]
      value.toString must_== "{a: 1, b: 2, c: 3}"
    }
    "build the wrapped Seq[String, SoyValue](items) as SoyMapData" in {
      val seq = Seq[(String, SoyValue)]("a" -> SoyInt(1), "b" -> SoyString("x"), "c" -> SoyNull, "d" -> SoyBoolean(false))
      val value: Any = SoyMap(seq).build
      value must beAnInstanceOf[SoyMapData]
      value.toString must_== "{a: 1, b: x, c: null, d: false}"
    }
    "build the wrapped Seq[String, SoyList](items) as SoyMapData" in {
      val seq = Seq[(String, SoyList)]("a" -> SoyList(Seq(SoyInt(1), SoyInt(2))), "b" -> SoyList(Seq(SoyInt(4), SoyNull)), "c" -> SoyList(Seq[SoyDouble]()))
      val value: Any = SoyMap(seq).build
      value must beAnInstanceOf[SoyMapData]
      value.toString must_== "{a: [1, 2], b: [4, null], c: []}"
    }
  }
}
