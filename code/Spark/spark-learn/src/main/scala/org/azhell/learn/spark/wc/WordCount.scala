package org.azhell.learn.spark.wc

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("wordCount")
    val context = new SparkContext(conf)
    val fileRDD = context.textFile("data")
    val wordRDD = fileRDD.flatMap(_.split(" "))
    val wordPairRDD = wordRDD.map((_, 1))
    val wordCountRDD = wordPairRDD.reduceByKey(_ + _)
    wordCountRDD.collect().foreach(println)
  }
}
