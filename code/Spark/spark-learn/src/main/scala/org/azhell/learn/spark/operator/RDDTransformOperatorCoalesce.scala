package org.azhell.learn.spark.operator



/**
 * 根据数据量缩减分区，用于大数据集过滤后，提高小数据集的执行效率
 * 当 spark 程序中，存在过多的小任务的时候，可以通过 coalesce 方法，收缩合并分区，
 * 减少分区的个数，减小任务调度成本
 */
object RDDTransformOperatorCoalesce {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorCoalesce")
    val rdd = makeRDD(context, List(1, 2, 3, 4, 5, 6), partitionCnt = 4)

    // coalesce默认是不会将数据打散的，数据很可能会存在不均衡的情况，从而出现数据倾斜的情况
    val coalesceRDD = rdd.coalesce(2)

    // coalesce可以设置多个参数，第二个参数指定数据是否混淆
    rdd.coalesce(2, shuffle = true)

    // coalesce可以用来扩大分区，但是如果不设置shuffle = true，则扩大分区既没有效果，也没有意义
    rdd.coalesce(6, shuffle = true)
    // 扩大分区可以使用repartition方法，
    rdd.repartition(6) // 底层就是调用 coalesce(numPartitions, shuffle = true)
    coalesceRDD.saveAsTextFile("output")
  }
}
