package org.azhell.learn.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable

object StreamingQueue {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("StreamingQueue")
    val context = new StreamingContext(conf, Seconds(5))

    // 模拟手动往DStream中添加数据
    val queue = mutable.Queue[RDD[Int]]()

    val inputStream = context.queueStream(queue, oneAtATime = false)

    val mapRDD = inputStream.map((_, 1))

    val reduceRDD = mapRDD.reduceByKey(_ + _)

    reduceRDD.print()

    context.start()

    // 不断插入数据
    for (i <- 1 to 5) {
      queue += context.sparkContext.makeRDD(1 + i to 300, 10)
      Thread.sleep(2)
    }

    context.awaitTermination()
  }
}
