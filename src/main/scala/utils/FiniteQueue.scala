package utils

import scala.collection.mutable


class FiniteQueue[A](val maxSize: Int) extends mutable.Queue[A] {

  override def enqueue(elems: A*): Unit = {
    super.enqueue(elems: _*)
    this.take(maxSize)
  }

}

object FiniteQueue {
  def apply[A](maxSize: Int) = new FiniteQueue[A](maxSize)
}
