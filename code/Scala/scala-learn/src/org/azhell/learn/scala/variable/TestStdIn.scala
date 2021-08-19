package org.azhell.learn.scala.variable

import scala.io.StdIn

/**
 * 测试标准输入
 */
object TestStdIn {

  def main(args: Array[String]): Unit = {
    println("请输出您的姓名：")
    val name = StdIn.readLine()
    println("请输入您的年龄：")
    val age = StdIn.readInt()
    println(s"欢迎${age}的${name}来学习scala语言！")
  }
}
