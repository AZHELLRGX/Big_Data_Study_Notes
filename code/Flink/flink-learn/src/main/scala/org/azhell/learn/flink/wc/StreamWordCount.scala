package org.azhell.learn.flink.wc

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._

/**
 * 流处理的WordCount
 *
 */
object StreamWordCount {
  def main(args: Array[String]): Unit = {
    // 创建流处理的执行环境
    val environment = StreamExecutionEnvironment.getExecutionEnvironment

    // 设置一下并行度
    environment.setParallelism(4) // 如果不设置默认就是本机的核心数

    // 解析输入参数--host localhost --port 7777
    val parameterTool = ParameterTool.fromArgs(args)
    val host = parameterTool.get("host")
    val port = parameterTool.get("port").toInt

    // 监听一个接口，接收一个socket文本流
    val inputDataStream: DataStream[String] = environment.socketTextStream(host, port)
    // 进行转换处理统计
    val resultDS: DataStream[(String, Int)] = inputDataStream.flatMap(_.split(" "))
      .filter(_.nonEmpty)
      .map((_, 1))
      .keyBy(0)
      .sum(1)
    resultDS.print().setParallelism(1) // 写出的时候也可以修改并行度

    // 启动任务执行
    environment.execute("stream word count")
  }
}
