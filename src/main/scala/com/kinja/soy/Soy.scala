package com.kinja.soy

import scala.language.implicitConversions
import scala.language.experimental.macros

/**
 * Provides convenience methods for building SoyValues.
 */
object Soy {

  /**
   * There is an implicit conversion from any type with a SoyWrites to SoyValueWrapper which is an empty trait that
   * shouldn't end into unexpected implicit conversions.
   */
  sealed trait SoyValueWrapper extends Any

  /**
   * The only implementation of SoyValueWrapper.
   * @param field The wrapped value is already converted to SoyValue.
   */
  private case class SoyValueWrapperImpl(field: SoyValue) extends AnyVal with SoyValueWrapper

  /**
   * The implicit conversion from any type with a SoyWrites to SoyValueWrapper.
   */
  implicit def toSoyFieldSoyValueWrapper[T](field: T)(implicit writes: SoyWrites[T]): SoyValueWrapper =
    SoyValueWrapperImpl(writes.toSoy(field))

  /**
   * Creates a SoyList from arbitrary items.
   * @param items List items which must have implicit SoyWrites available.
   * @return The soy list.
   */
  def list(items: SoyValueWrapper*): SoyList = SoyList(items.toSeq.map(unwrap))

  /**
   * Creates a SoyMap from key-value pairs.
   * @param items Key-value pairs. The values must have implicit SoyWrites available.
   * @return The soy map.
   */
  def map(items: (String, SoyValueWrapper)*): SoyMap = SoyMap(items.map { case (k, v) => (k, unwrap(v)) }.toMap)

  // Passed nulls will typecheck without needing the implicit conversion, so they need to be checked at runtime
  private def unwrap(wrapper: SoyValueWrapper) = wrapper match {
    case null => SoyNull
    case SoyValueWrapperImpl(value) => value
  }

  /**
   * Converts a value of any type to SoyValue using its implicit SoyWrites.
   * @param o The instance to be converted.
   * @return The result of the conversion.
   */
  def toSoy[T](o: T)(implicit writes: SoyWrites[T]): SoyValue = writes.toSoy(o)

  /**
   * Converts a value of any type to SoyMap using its implicit SoyMapWrites.
   * @param o The instance to be converted.
   * @return The result of the conversion.
   */
  def toSoyMap[T](o: T)(implicit writes: SoyMapWrites[T]): SoyMap = writes.toSoy(o)

  /**
   * Generates a SoyMapWrites[T] for a given case class T.
   */
  def writes[T]: SoyMapWrites[T] = macro SoyMacroImpl.writesImpl[T]

  def typedWrites[T]: SoyMapWrites[T] = macro SoyMacroImpl.typedWritesImpl[T]

}
