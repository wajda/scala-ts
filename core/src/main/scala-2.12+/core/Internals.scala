package io.github.scalats.core

import scala.collection.immutable.ListSet

private[core] object Internals {
  /** With predictable order (for emitter). */
  @inline def list[T](set: ListSet[T]): List[T] = set.toList
}
