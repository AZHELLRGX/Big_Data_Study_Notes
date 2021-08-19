package org.azhell.learn.spark.operator

import org.apache.spark.rdd.RDD

/**
 * 演示转换算子glom的使用
 * 将同一个分区的数据转换成内存数组进行处理，分区不变
 */
object RDDTransformOperatorGlom {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorGlom")
    val rdd = makeRDD(context, List(1, 2, 3, 4), 2)

    /**
     * List => Int
     * Int => Array
     */
    val glomRDD: RDD[Array[Int]] = rdd.glom()
    glomRDD.collect().foreach(data => {
      println(data.mkString(","))
    })

    /**
     * 实现一个小功能：
     * 每个分区最大值求和计算
     */
    val maxRDD = glomRDD.map(_.max)
    println(maxRDD.collect().sum)
  }
}
