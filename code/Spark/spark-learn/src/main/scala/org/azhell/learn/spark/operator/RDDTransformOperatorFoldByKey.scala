package org.azhell.learn.spark.operator

/**
 * foldByKey
 * 前面介绍了aggregateByKey算子
 * 当我们的分区内和分区间的计算规则是一样的时候，其实可以直接使用foldByKey
 */
object RDDTransformOperatorFoldByKey {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorFoldByKey")
    val rdd = makeRDD(context, List(("a", 1), ("a", 2), ("b", 3), ("b", 4)))

    rdd.foldByKey(1)(_ * _).collect().foreach(println)

    // 上面的写法其实就是aggregateByKey算子的简化版本
    rdd.aggregateByKey(1)(_ * _, _ * _).collect().foreach(println)
  }
}
