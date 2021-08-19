package org.azhell.learn.spark.operator

/**
 * 演示转换算子map的使用
 */
object RDDTransformOperatorMap {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorMap")
    val rdd = makeRDD(context, List(1, 2, 3, 4))

    val mapRDD = rdd.map(_ * 2)
    mapRDD.collect().foreach(println)

    // 演示map针对文件处理
    val fileRDD = context.textFile("data/spark.log")
    fileRDD.map(
      line => {
        val array = line.split(" ")
        // 打印时间戳
        array(0) + "-" + array(1)
      }
    ).collect().foreach(println)

    // map并行计算
    /**
     * 1、RDD的计算一个分区内的数据是一个一个执行逻辑
     * 只有前面的一个数据全部的逻辑执行完成完毕后，才会执行下一个数据
     * 分区内数据的执行是有序的
     * 2、不同的分区数据之间的计算是无序的
     */
  }
}
