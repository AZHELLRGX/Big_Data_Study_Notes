package org.azhell.learn.spark.operator

import org.apache.spark.rdd.RDD

/**
 * cogroup的使用方法
 */
object RDDTransformOperatorCogroup {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorCogroup")
    val rdd1 = makeRDD(context, List(("a", 4), ("b", 8)))
    val rdd2 = makeRDD(context, List(("a", 1), ("b", 2), ("c", 3), ("c", 9)))

    // cogroup = connect + group 但是是先分组后连接
    val cogroupRDD: RDD[(String, (Iterable[Int], Iterable[Int]))] = rdd1.cogroup(rdd2)
    cogroupRDD.collect().foreach(println)
    /*
    输出结果:
    (a,(CompactBuffer(4),CompactBuffer(1)))
    (b,(CompactBuffer(8),CompactBuffer(2)))
    (c,(CompactBuffer(),CompactBuffer(3, 9)))
     */
  }
}
