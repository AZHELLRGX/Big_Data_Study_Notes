package org.azhell.learn.spark.streaming

import org.apache.spark.SparkConf
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.util.Random

/**
 * 自定义数据采集器 演示
 */
object StreamingReceiver {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("StreamingReceiver")
    val context = new StreamingContext(conf, Seconds(5))

    // 使用自定义采集器采集数据
    val msgDStream = context.receiverStream(new MyReceiver)
    msgDStream.print()

    context.start()


    context.awaitTermination()
  }

  /**
   * 自定义数据采集器
   * 1、继承Receiver，定义泛型，传递参数
   * 2、重写方法
   */
  class MyReceiver extends Receiver[String](storageLevel = StorageLevel.MEMORY_ONLY) {
    private var flag = true

    override def onStart(): Unit = {
      new Thread(() => {
        while (flag) {
          val msg = "采集的数据为：" + new Random().nextInt(10).toString
          store(msg)
          Thread.sleep(500)
        }
      }).start()
    }

    override def onStop(): Unit = {
      flag = false
    }
  }
}
