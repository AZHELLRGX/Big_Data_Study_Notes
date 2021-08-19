package org.azhell.learn.spark.operator

/**
 * 将数据根据指定的规则进行筛选过滤，符合规则的数据保留，不符合规则的数据丢弃
 * 当数据进行筛选过滤后，分区不变，但是分区内的数据可能不均衡，
 * 生产环境下，可能会出现数据倾斜
 * 数据倾斜的解决后面会讲到
 */
object RDDTransformOperatorFilter {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorFilter")
    val rdd = makeRDD(context,List(1, 2, 3, 4))

    rdd.filter(_ % 2 == 0).collect().foreach(println)
  }
}
