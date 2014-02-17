package com.kinja.soy

import scala.language.implicitConversions

/**
 * Provides convenience methods for building SoyValues.
 */
object Soy {

  /**
   * There is an implicit conversion from any type with a SoyWrites to SoyValueWrapper which is an empty trait that
   * shouldn't end into unexpected implicit conversions.
   *
   * Something to note due to `SoyValueWrapper` extending `NotNull`: `null` or `None` will end into compiling error:
   * use SoyNull instead.
   */
  sealed trait SoyValueWrapper extends Any with NotNull

  /**
   * The only implementation of SoyValueWrapper.
   * @param field The wrapped value is already converted to SoyValue.
   */
  private case class SoyValueWrapperImpl(field: SoyValue) extends AnyVal with SoyValueWrapper

  /**
   * The implicit conversion from any type with a SoyWrites to SoyValueWrapper.
   */
  implicit def toSoyFieldSoyValueWrapper[T](field: T)(implicit writes: SoyWrites[T]): SoyValueWrapper = SoyValueWrapperImpl(writes.toSoy(field))

  /**
   * Creates a SoyList from arbitrary items.
   * @param items List items which must have implicit SoyWrites available.
   * @return The soy list.
   */
  def list(items: SoyValueWrapper*): SoyList = SoyList(items.map(_.asInstanceOf[SoyValueWrapperImpl].field))

  /**
   * Creates a SoyMap from key-value pairs.
   * @param items Key-value pairs. The values must have implicit SoyWrites available.
   * @return The soy map.
   */
  def map(items: (String, SoyValueWrapper)*): SoyMap = SoyMap(items.map { case (k, v) => (k, v.asInstanceOf[SoyValueWrapperImpl].field) })

  /**
   * Converts a value of any type to SoyValue using its implicit SoyWrites.
   * @param o The instance to be converted.
   * @return The result of the conversion.
   */
  def toSoy[T](o: T)(implicit writes: SoyWrites[T]): SoyValue = writes.toSoy(o)
}
