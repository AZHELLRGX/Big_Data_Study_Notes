package org.azhell.learn.spark.operator

/**
 * 演示转换算子flatMap的使用
 * 将数据扁平化
 */
object RDDTransformOperatorFlatMap {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorFlatMap")
    val rdd = context.makeRDD(List(List(1, 2, 3, 4), List(5, 6, 7)))

    rdd.flatMap(
      list => {
        // 返回的只要是一个可迭代的集合就可以
        list
      }
    ).collect().foreach(println)

    val fileRDD = context.textFile("data/part-000")
    fileRDD.flatMap(_.split(" ")).collect().foreach(println)

    val rdd1 = context.makeRDD(List(List(1, 2, 3, 4), 8, "hello", List(5, 6, 7)))
    rdd1.flatMap {
      case list: List[_] => list
      case d => List(d)
    }.collect().foreach(println)
  }
}
