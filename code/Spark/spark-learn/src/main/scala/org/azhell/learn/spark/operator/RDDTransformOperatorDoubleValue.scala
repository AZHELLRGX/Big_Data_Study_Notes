package org.azhell.learn.spark.operator


/**
 * 双Value类型
 * 1、交集
 * 2、并集
 * 3、差集
 * 4、拉链
 *
 * 交集、并集、差集必须保持两个RDD的类型一致
 *
 */
object RDDTransformOperatorDoubleValue {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorDoubleValue")
    val rdd = makeRDD(context, List(1, 2, 3, 4))
    val rdd1 = makeRDD(context, List(3, 4, 5, 6))

    // 交集
    val intersectionRDD = rdd.intersection(rdd1)
    println(intersectionRDD.collect().mkString(";"))

    // 并集
    val unionRDD = rdd.union(rdd1)
    println(unionRDD.collect().mkString(";"))

    // 差集
    val subtractRDD = rdd.subtract(rdd1)
    println(subtractRDD.collect().mkString(";"))

    // 拉链(拉链的两个RDD分区数量需要一致；而且每个分区的数据量也需要一致)，否则会产生异常
    val zipRDD = rdd.zip(rdd1)
    println(zipRDD.collect().mkString(";"))

  }
}
