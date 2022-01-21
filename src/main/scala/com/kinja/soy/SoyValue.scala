package com.kinja.soy

import com.google.template.soy.data._
import scala.jdk.CollectionConverters._

/**
 * Base trait of types which can be used in Google Cloure Templatates.
 */
sealed trait SoyValue extends Any {

  /**
   * Converts the SoyValue to a (Java) type which can be used directly in Google Closure Templates.
   */
  def build: Any
}

case object SoyNull extends SoyValue {
  @inline def build = null
}

case class SoyString(value: String) extends AnyVal with SoyValue {
  @inline def build = value
}

case class SoyBoolean(value: Boolean) extends AnyVal with SoyValue {
  @inline def build = value
}

case class SoyInt(value: Int) extends AnyVal with SoyValue {
  @inline def build = value
}

case class SoyFloat(value: Double) extends AnyVal with SoyValue {
  @inline def build = value
}

case class SoyList(value: Seq[SoyValue]) extends AnyVal with SoyValue {
  def build = value.map(_.build).asJava
}

case class SoyMap(value: Map[String, SoyValue]) extends AnyVal with SoyValue {

  def build = value.map { case (k, v) => k -> v.build }.asJava

  /**
   * All distinct keys of the map.
   */
  def keys: Set[String] = value.map(_._1).toSet

  /**
   * All distinct values of the map.
   */
  def values: Set[SoyValue] = value.map(_._2).toSet

  /**
   * Merge this map with an other one. Values from other override value of the current map.
   */
  def ++(other: SoyMap): SoyMap = {
    SoyMap(value ++ other.value)
  }

  /**
   * Removes a key from the map.
   */
  def -(key: String): SoyMap = SoyMap(value.filterNot(_._1 == key))

  /**
   * Appends a key-value pair to the map.
   */
  def +(field: (String, SoyValue)): SoyMap = SoyMap(value + field)

}

/**
 * A pre-escaped HTML fragment. By constructing this you are guaranteeing that the value
 * passed is safe for display.
 */
case class SoyHtml(value: String) extends AnyVal with SoyValue {
  def build = UnsafeSanitizedContentOrdainer.ordainAsSafe(value, SanitizedContent.ContentKind.HTML)
}

/**
 * A pre-escaped URI. By constructing this you are guaranteeing that the value
 * passed is safe for display.
 */
case class SoyUri(value: String) extends AnyVal with SoyValue {
  def build = UnsafeSanitizedContentOrdainer.ordainAsSafe(value, SanitizedContent.ContentKind.URI)
}

/**
 * A pre-escaped CSS fragment. By constructing this you are guaranteeing that the value
 * passed is safe for display.
 */
case class SoyCss(value: String) extends AnyVal with SoyValue {
  def build = UnsafeSanitizedContentOrdainer.ordainAsSafe(value, SanitizedContent.ContentKind.CSS)
}

/**
 * A pre-escaped Javascript fragment or JSON data. By constructing this you are guaranteeing
 * that the value passed is safe for display.
 */
case class SoyJs(value: String) extends AnyVal with SoyValue {
  def build = UnsafeSanitizedContentOrdainer.ordainAsSafe(value, SanitizedContent.ContentKind.JS)
}

