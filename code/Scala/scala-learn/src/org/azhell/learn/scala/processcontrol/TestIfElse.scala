package org.azhell.learn.scala.processcontrol

import scala.io.StdIn

/**
 * 测试scala中的流程控制语句中的if else
 */
object TestIfElse {
  def main(args: Array[String]): Unit = {
    /*
     * scala的if else的流程控制语句使用方法与java基本相同
     * 但是存在以下差别
     * 1、scala的if else是有返回值的
     * 2、java中的三元表达式可以使用scala的if else实现
     *
     * 注意：在Scala中没有Switch，而是使用模式匹配来处理
     */

    println("input age")
    val age = StdIn.readInt()
    val res: String = if (age < 18) {
      "童年"
    } else if (age >= 18 && age < 30) {
      "中年"
    } else {
      "老年"
    }
    println(res)

    // 以上代码，如果if else代码块返回的类型不一致，可以取他们的共同祖先
    val resAny: Any = if (age < 18) {
      "童年"
    } else if (age >= 18 && age < 30) {
      "中年"
    } else {
      100
    }
    println(resAny)

    // scala中使用三元表达式
    val resX = if (age < 18) "童年" else "成年"
    println(resX)
  }
}
