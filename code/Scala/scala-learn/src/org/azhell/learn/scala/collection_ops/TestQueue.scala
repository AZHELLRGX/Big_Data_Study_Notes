package org.azhell.learn.scala.collection_ops

import scala.collection.immutable.Queue
import scala.collection.mutable

object TestQueue {
  def main(args: Array[String]): Unit = {
    // 创建一个可变队列
    val queue = mutable.Queue[String]()

    // 入队
    queue.enqueue("a", "b", "c")

    println(queue)
    // 出队
    println(queue.dequeue())
    println(queue.dequeue())

    val imQueue = Queue[String]("a", "b", "c")
    val queue1 = imQueue.enqueue("d")
    println(queue1)

    val dequeue = queue1.dequeue // 返回的是一个tuple，兼有出队的值和出队以后生成的新队列
    println(dequeue)
  }
}
