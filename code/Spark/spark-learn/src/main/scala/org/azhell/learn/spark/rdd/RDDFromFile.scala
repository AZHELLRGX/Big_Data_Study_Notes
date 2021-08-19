package org.azhell.learn.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}

object RDDFromFile {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RDDFromFile")
    val context = new SparkContext(sparkConf)

    // 创建RDD
    // 从文件创建
    /**
     * 1、文件路径可以是绝对路径，也可以是相对路径
     * 2、可以是具体的文件，也可以是一个目录
     * 3、路径名称中可以使用通配符 例如：context.textFile("data/part-*")
     * 4、可以是本地文件系统，也可以是分布式文件存储系统路径，例如HDFS路径
     */

    // 以行为单位来读取数据
    // val rdd = context.textFile("data/part-000")

    // 以文件为单位来读取数据
    // 读取的结果是一个元组，第一个元素表示文件路径，第二个元素表示文件内容
    val rdd = context.wholeTextFiles("data")

    rdd.collect().foreach(println)
  }
}
