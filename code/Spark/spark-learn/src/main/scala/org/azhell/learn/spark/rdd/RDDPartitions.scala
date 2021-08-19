package org.azhell.learn.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}

object RDDPartitions {
  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RDDPartitions")
    val context = new SparkContext(sparkConf)

    /* makeRDD方法可以传递第二个参数，这个参数表示分区的数量
     * 第二个参数可以不传递，那么分区就使用默认值：defaultParallelism
     * scheduler.conf.getInt("spark.default.parallelism", totalCores)
     * spark在默认情况下会从配置对象中获取配置参数：spark.default.parallelism
     * 如果配置中获取不到，那么会使用totalCores属性，这个属性值表示当前运行环境最大可用核数
     */
    val rdd = context.makeRDD(
      List(1, 2, 3, 4), 2
    )

    /* textFile可以将文件作为数据处理的数据源，默认也可以设定分区
    minPartitions ：最小分区数量
    math.min(defaultParallelism, 2)
    如果不想使用默认分区，也可以通过第二个参数指定分区数

    Spark读取文件底层使用的是hadoop的读取方式
    分区数量的计算方式：
      totalSize、goalSize
     */
    val fileRDD = context.textFile("data/part-000")
    val fileRDD1 = context.textFile("data/part-000",3)


    rdd.saveAsTextFile("output")
  }
}
