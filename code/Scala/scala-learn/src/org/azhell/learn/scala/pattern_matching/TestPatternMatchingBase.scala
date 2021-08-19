package org.azhell.learn.scala.pattern_matching

object TestPatternMatchingBase {
  def main(args: Array[String]): Unit = {
    // 基本定义语法
    val x = 5
    val y = x match {
      case 1 => "one"
      case 2 => "two"
      case _ => "other"
    }
    println(y)

    // 示例，用模式匹配实现简单的二元运算
    val a = 25
    val b = 13

    def matchDualOp(op: Char): Int = op match {
      case '+' => a + b
      case '-' => a - b
      case '*' => a * b
      case '/' => a / b
      case '%' => a % b
      case _ => -1
    }

    println(matchDualOp('+'))
    println(matchDualOp('/'))
    println(matchDualOp('\\'))

    println("=========================")

    // 3. 模式守卫，类似之前的循环守卫的设计
    // 求一个整数的绝对值
    def abs(num: Int): Int = {
      num match {
        case i if i >= 0 => i
        case i if i < 0 => -i
      }
    }

    println(abs(67))
    println(abs(0))
    println(abs(-24))
  }
}
