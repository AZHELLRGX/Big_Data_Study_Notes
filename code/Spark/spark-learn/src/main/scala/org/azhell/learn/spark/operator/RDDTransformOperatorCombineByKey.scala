package org.azhell.learn.spark.operator

import org.apache.spark.rdd.RDD

/**
 * combineByKey
 * 主要功能就是可以将分区的数据转换数据结构，然后再进行分区内以及分区间计算
 */
object RDDTransformOperatorCombineByKey {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorCombineByKey")
    val rdd = makeRDD(context, List(("a", 1), ("a", 2), ("b", 3), ("b", 4)))

    /*
    参数列表有三个参数
    第一个参数表示  将源数据转换为其他数据结构
    第二个参数表示  分区内计算规则
    第三个参数表示  分区间计算规则
     */

    // 使用以上特性可以完成 求平均值 的功能
    val sumCountRDD: RDD[(String, (Int, Int))] = rdd.combineByKey(
      v => (v, 2), // 转换数据value部分的数据结构
      (t, v) => {
        // 分区内计算的时候，使用tuple类型记录 相同key的value和以及key出现的次数
        (t._1 + v, t._2 + 1)
      },
      (t1, t2) => {
        // 分区间计算的时候，只需要将sum和count加起来
        (t1._1 + t2._1, t1._2 + t2._2)
      }
    )
    val avgRDD: RDD[(String, Double)] = sumCountRDD.mapValues(tuple => tuple._1.toDouble / tuple._2)
    avgRDD.collect().foreach(println)
  }
}
