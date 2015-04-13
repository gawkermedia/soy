package com.kinja.soy.util

/**
 * Lazy is useful for creating SoyWrites for recursive data structures.
 */
trait Lazy[A] {
  def lazyVal: A
}
object Lazy {
  def apply[A](a: => A) = new Lazy[A] {
    override lazy val lazyVal = a
  }
}
