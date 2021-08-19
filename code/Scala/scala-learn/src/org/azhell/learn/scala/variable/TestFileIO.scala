package org.azhell.learn.scala.variable

import java.io.{File, PrintWriter}

import scala.io.Source

/**
 * 测试scala的简单写入写出文件操作
 */
object TestFileIO {
  def main(args: Array[String]): Unit = {
    val source = Source.fromFile("resources/input.txt")
    source.foreach(print)
    // 记得关闭输入流
    source.close()

    // scala没有自带的写出方法，需要使用java的类库
    val writer = new PrintWriter(new File("resources/output.txt"))
    writer.write("hello scala from java writer")
    writer.close()
  }
}
