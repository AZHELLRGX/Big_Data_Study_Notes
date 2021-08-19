package org.azhell.learn.spark.operator

/**
 * 相同key的数据Value可以聚合
 * reduceBy与Scala的聚合是一样的，都是两两聚合
 * 分区内的预聚合和分区间的最终聚合的计算规则是一样的
 * 如果分区内与分区间的聚合规则不一样，则需要使用AggregateByKey
 */
object RDDTransformOperatorReduceByKey {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorReduceByKey")
    val rdd = makeRDD(context,List(("a", 1), ("a", 2), ("b", 3), ("b", 4)))

    /**
     * 没有相同key的数据不会进入聚合操作
     */
    val reduceRDD = rdd.reduceByKey(_ + _)
    reduceRDD.collect().foreach(println)
  }
}
