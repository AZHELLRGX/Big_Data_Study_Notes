package org.azhell.learn.scala.collection_ops

import scala.collection.immutable

/**
 * 测试并行操作
 */
object TestParallel {
  def main(args: Array[String]): Unit = {

    val result: immutable.IndexedSeq[Long] = (1 to 100).map(
      _ => Thread.currentThread.getId
    )
    println(result)
    // 2.13.5貌似已经不支持如下操作了，留待后续回来再看
    //    val result2: ParSeq[Long] = (1 to 100).par.map(
    //      x => Thread.currentThread.getId
    //    )
    //    println(result2)
  }
}
