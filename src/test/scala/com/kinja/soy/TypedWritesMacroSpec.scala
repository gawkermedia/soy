package com.kinja.soy

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

case class TypedClass(i: Int)

class TypedWritesMacroSpec extends AnyFlatSpec with Matchers {

  implicit val tw = Soy.typedWrites[TypedClass]

  it should "correctly add the type field to the result" in {
    Soy.toSoy(TypedClass(1)) should be(Soy.map("i" -> 1, "type" -> "TypedClass"))
  }

}
