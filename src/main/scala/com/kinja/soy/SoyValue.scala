package com.kinja.soy

import com.google.template.soy.data.{ SoyMapData, SoyListData }
import scala.collection.JavaConversions._

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

case class SoyFloat(value: Float) extends AnyVal with SoyValue {
  @inline def build = value
}

case class SoyDouble(value: Double) extends AnyVal with SoyValue {
  @inline def build = value
}

case class SoyList(value: Seq[SoyValue]) extends AnyVal with SoyValue {
  def build = new SoyListData(asJavaIterable(value.map(_.build)))
}

case class SoyMap(value: Seq[(String, SoyValue)]) extends AnyVal with SoyValue {
  def build = new SoyMapData(mapAsJavaMap(Map(value.map { case (k, v) => (k, v.build) }: _*)))
}
