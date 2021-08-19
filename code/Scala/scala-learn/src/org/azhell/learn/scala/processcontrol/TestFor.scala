package org.azhell.learn.scala.processcontrol

import scala.language.postfixOps

/**
 * 测试scala中的for循环语法
 */
object TestFor {
  def main(args: Array[String]): Unit = {
    // 1、范围遍历，这个to也是一个方法
    for (i <- 1 to 10) {
      println(i + " hello world")
    }
    for (i: Int <- 1.to(10)) {
      println(i + " hello world")
    }
    // 不包含边界则使用until关键字
    for (i <- 1 until 10) {
      println(i + " hello world")
    }
    // 2、集合遍历
    for (i <- Array(12, 45, 67)) {
      println(i)
    }
    // 3、循环守卫
    for (i <- 1 to 3 if i != 2) {
      println(i)
    }
    // 4、循环步长  步长不允许为0，否则抛出非法参数异常
    for (i <- 1 to 10 by 2) {
      println(i)
    }
    for (i <- 30 to 13 by -2) {
      println(i)
    }
    // 反转输出 reverse的底层实现其实也是by
    for (i <- 1 to 10 reverse) {
      println(i)
    }

    // 5、循环嵌套
    for (i <- 1 to 4; j <- 1 to 5) {
      println("i = " + i + " j = " + j)
    }

    // 6、引入变量
    for (i <- 1 to 3; j = 4 - i) {
      println("i = " + i + " j = " + j)
    }
    // 也可以像下面这样写
    for {
      i <- 1 to 3
      j = 4 - i
    } {
      println("i = " + i + " j = " + j)
    }
    // for循环的返回值是Unit，可以使用yield关键字来将数据封装为集合返回
    val arr = for (i <- 1 to 3) yield i
    println(arr)
  }
}
