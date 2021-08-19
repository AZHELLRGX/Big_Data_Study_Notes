package org.azhell.learn.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}

object RDDFromMemoryCollections {
  def main(args: Array[String]): Unit = {
    // 准备环境
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RDDFromMemoryCollections")
    val context = new SparkContext(sparkConf)

    // 创建RDD
    // 1、从内存集合创建
    val seq = Seq[Int](1, 2, 3, 4)
    // parallelize 并行
    // val rdd: RDD[Int] = context.parallelize(seq)
    // makeRDD底层实现就是调用了RDD对象的parallelize方法，makeRDD只是一种更好记忆的方法名
    val rdd = context.makeRDD(seq)

    // 使用RDD
    rdd.collect().foreach(println)
  }
}
