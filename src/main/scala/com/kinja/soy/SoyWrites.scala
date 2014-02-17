package com.kinja.soy

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

/**
 * Implement this trait for an arbitrary type to allow it being converted for use in Google Closure Templates.
 */
@implicitNotFound("No Soy writer found for type ${T}. Try to implement an implicit SoyWrites for this type.")
trait SoyWrites[-T] extends DefaultSoyWrites {

  /**
   * Implement this method to define how the instance can be converted to SoyValue.
   */
  def toSoy(t: T): SoyValue
}

/**
 * Provides conversion from base types to SoyValue.
 */
trait DefaultSoyWrites {

  /**
   * Converter for Int types.
   */
  implicit object IntSoy extends SoyWrites[Int] {
    def toSoy(o: Int) = SoyInt(o)
  }

  /**
   * Converter for Short types.
   */
  implicit object ShortSoy extends SoyWrites[Short] {
    def toSoy(o: Short) = SoyInt(o)
  }

  /**
   * Converter for Long types.
   */
  implicit object LongSoy extends SoyWrites[Long] {
    def toSoy(o: Long) = SoyString(o.toString)
  }

  /**
   * Converter for Char types.
   */
  implicit object CharSoy extends SoyWrites[Char] {
    def toSoy(o: Char) = SoyString(o.toString)
  }

  /**
   * Converter for Byte types.
   */
  implicit object ByteSoy extends SoyWrites[Byte] {
    def toSoy(o: Byte) = SoyInt(o)
  }

  /**
   * Converter for Float types.
   */
  implicit object FloatSoy extends SoyWrites[Float] {
    def toSoy(o: Float) = SoyFloat(o)
  }

  /**
   * Converter for Double types.
   */
  implicit object DoubleSoy extends SoyWrites[Double] {
    def toSoy(o: Double) = SoyDouble(o)
  }

  /**
   * Converter for BigInt types.
   */
  implicit object BigIntSoy extends SoyWrites[BigInt] {
    def toSoy(o: BigInt) = SoyString(o.toString)
  }

  /**
   * Converter for BigDecimal types.
   */
  implicit object BigDecimalSoy extends SoyWrites[BigDecimal] {
    def toSoy(o: BigDecimal) = SoyString(o.toString)
  }

  /**
   * Converter for Boolean types.
   */
  implicit object BooleanSoy extends SoyWrites[Boolean] {
    def toSoy(o: Boolean) = SoyBoolean(o)
  }

  /**
   * Converter for String types.
   */
  implicit object StringSoy extends SoyWrites[String] {
    def toSoy(o: String) = SoyString(o)
  }

  /**
   * Converter for Array[T] types.
   */
  implicit def arraySoy[T: ClassTag](implicit writes: SoyWrites[T]): SoyWrites[Array[T]] = new SoyWrites[Array[T]] {
    def toSoy(ts: Array[T]) = SoyList((ts.map(t => Soy.toSoy(t)(writes))).toSeq)
  }

  /**
   * Converter for Map[String,V] types.
   */
  implicit def mapSoy[V](implicit writes: SoyWrites[V]): SoyWrites[collection.immutable.Map[String, V]] = new SoyWrites[collection.immutable.Map[String, V]] {
    def toSoy(ts: collection.immutable.Map[String, V]) = SoyMap(ts.map { case (k, v) => (k, Soy.toSoy(v)(writes)) }.toSeq)
  }

  /**
   * Converter for Traversables types.
   */
  implicit def traversableSoy[A: SoyWrites] = new SoyWrites[Traversable[A]] {
    def toSoy(as: Traversable[A]) = SoyList(as.map(Soy.toSoy(_)).toSeq)
  }

  /**
   * Converter for SoyValues.
   */
  implicit object SoyValueSoy extends SoyWrites[SoyValue] {
    def toSoy(o: SoyValue) = o
  }

  /**
   * Converter for Option.
   */
  implicit def OptionSoy[T](implicit writes: SoyWrites[T]): SoyWrites[Option[T]] = new SoyWrites[Option[T]] {
    def toSoy(o: Option[T]) = o match {
      case Some(value) => writes.toSoy(value)
      case None => SoyNull
    }
  }
}
