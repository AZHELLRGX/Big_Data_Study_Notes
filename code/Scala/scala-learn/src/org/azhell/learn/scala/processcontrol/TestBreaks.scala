package org.azhell.learn.scala.processcontrol

import scala.util.control.Breaks._

/**
 * 演示scala中的类似java的循环中断设计
 */
object TestBreaks {
  def main(args: Array[String]): Unit = {
    /*
    在scala中，不存在break与continue关键字，
    官方解释是为了更好的函数式编程与面向对象
     */

    // 使用异常的方式来实现循环的中断
    try {
      for (i <- 1 to 10) {
        if (i == 3) {
          throw new RuntimeException
        } else {
          println(i)
        }
      }
    } catch {
      case _: RuntimeException => // 不做任何处理
    }

    // 可以使用scala中的Breaks与break()实现循环中断
    breakable(
      for (i <- 1 to 10) {
        if (i == 3) {
          break()
        } else {
          println(i)
        }
      }
    )
    println("正常代码块")
  }
}
