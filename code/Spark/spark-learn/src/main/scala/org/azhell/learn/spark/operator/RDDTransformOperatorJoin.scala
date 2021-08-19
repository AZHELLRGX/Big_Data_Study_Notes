package org.azhell.learn.spark.operator

/**
 * join、leftOuterJoin、rightOuterJoin以及fullOuterJoin的使用方法
 * 与sql的join系列函数的使用方法与含义一致
 * 两个不同数据源的数据，相同key的value会连接再一起，形成一个元组
 * 如果两个数据源的key无法匹配到数据，那么结果中不会出现结果
 * 如果数据源中存在多个key一样的元组，会出现笛卡尔积，需要注意内存爆炸的问题；尽量保证key的唯一性
 */
object RDDTransformOperatorJoin {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorJoin")
    val rdd1 = makeRDD(context, List(("a", 4), ("b", 8), ("c", 16)))
    val rdd2 = makeRDD(context, List(("a", 1), ("b", 2), ("c", 3)))

    // RDD[(String, (Int, Int))]
    rdd1.join(rdd2).collect().foreach(println)

    val rdd3 = makeRDD(context, List(("a", 5), ("b", 10), ("b", 40), ("d", 20)))
    // RDD[(String, (Int, Option[Int]))]，右表无数据，用None表示
    rdd1.leftOuterJoin(rdd3).collect().foreach(println)
    // RDD[(String, (Option[Int], Int))]，左表无数据，用None表示
    rdd1.rightOuterJoin(rdd3).collect().foreach(println)
    // RDD[(String, (Option[Int], Option[Int]))]
    rdd1.fullOuterJoin(rdd3).collect().foreach(println)
  }
}
