package org.azhell.learn.spark.operator

/**
 * mapPartitions可以以分区为单位进行数据转换操作
 * 但是会将整个分区的数据加载到内存进行引用，
 * 处理完成的数据不会马上释放掉内存，必须等待整个分区的数据处理完成
 * 所以在内存较小，数据量较大的场合下，容易产生内存溢出
 */
object RDDTransformOperatorMapPartitions {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorMapPartitions")
    val rdd = makeRDD(context,List(1, 2, 3, 4), 2)

    rdd.mapPartitions(
      // 迭代器
      iterator => {
        // 只会按照分区数量2执行两次
        println("mapPartitions......")
        iterator.map(_ * 2)
      }
    ).collect().foreach(println)

    // 统计每个分区数据的最大值
    rdd.mapPartitions(
      iterator => {
        // 返回的也需要是一个迭代器
        List(iterator.max, 1, 2).iterator
      }
    ).collect().foreach(println)
  }
}
