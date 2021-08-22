package org.azhell.learn.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingWordCount {
  def main(args: Array[String]): Unit = {
    // 创建环境对象
    /* StreamingContext创建的时候需要传递两个参数：
    1、环境配置
    2、批量处理的周期（采集周期）
     */
    val conf = new SparkConf().setMaster("local[*]").setAppName("StreamingWordCount")
    val ssc = new StreamingContext(conf,Seconds(3))

    // 逻辑处理
    val lines: ReceiverInputDStream[String] = ssc.socketTextStream("localhost", 9999)

    val words = lines.flatMap(_.split(" "))

    val wordToOne = words.map((_, 1))

    val wordToCount = wordToOne.reduceByKey(_ + _)

    wordToCount.print()

    // 关闭环境
    // 因为采集器是长期执行的任务，所以不能直接关闭
    // 如果main方法，应用程序也会自动结束，所以不能让main方法执行完毕
    // ssc.stop()

    // 1、启动采集器
    ssc.start()
    // 2、等待采集器的关闭
    ssc.awaitTermination()
  }
}
