package org.azhell.learn.spark.operator


object RDDTransformOperatorSortBy {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorSortBy")
    val rdd = makeRDD(context, List(6, 5, 3, 4, 1, 10), 2)
    // 默认是从小到大排序
    val sortRDD = rdd.sortBy(num => num)
    sortRDD.collect().foreach(println)

    val rdd1 = context.makeRDD(List(("1", 1), ("11", 11), ("2", 2)), 2)
    // 对元组进行排序，设置倒序输出
    val sortRDD1 = rdd1.sortBy(tuple => tuple._1.toInt, ascending = false)
    sortRDD1.collect().foreach(println)
  }
}
