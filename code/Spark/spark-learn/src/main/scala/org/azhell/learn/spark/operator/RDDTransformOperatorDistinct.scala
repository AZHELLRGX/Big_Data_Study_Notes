package org.azhell.learn.spark.operator



object RDDTransformOperatorDistinct {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorDistinct")
    val rdd = makeRDD(context, List(1, 2, 3, 4, 4, 5, 6, 1))

    // 底层其实是reduceByKey，将数据转换为(1,null),(2,null)结构
    rdd.distinct().collect().foreach(println)
  }
}
