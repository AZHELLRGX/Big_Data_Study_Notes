package org.azhell.learn.flink.wc

import org.apache.flink.api.scala.{AggregateDataSet, DataSet, ExecutionEnvironment}
import org.apache.flink.streaming.api.scala.createTypeInformation

/**
 * 模拟批处理的WordCount Demo
 */
object WordCount {
  def main(args: Array[String]): Unit = {
    // 创建上下文执行环境
    val environment: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment
    // 从文件中读取数据
    val inputPath: String = "src/main/resources/hello.txt"
    val inputDS: DataSet[String] = environment.readTextFile(inputPath)

    // 统计单词数量
    val resultDS: AggregateDataSet[(String, Int)] = inputDS
      .flatMap(_.split(" "))
      .map((_, 1))
      .groupBy(0)
      .sum(1)

    // 输出结果
    resultDS.print()
  }
}
