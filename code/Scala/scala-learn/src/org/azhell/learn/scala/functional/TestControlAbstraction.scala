package org.azhell.learn.scala.functional

/**
 * 演示scala的控制抽象
 */
object TestControlAbstraction {
  def main(args: Array[String]): Unit = {
    // 1、传值参数
    def f1(): Int = {
      println("函数f1被调用")
      12
    }

    def f2(a: Int): Unit = {
      println(s"函数f2被调用,param is $a")
    }

    f2(f1())

    // 2、传名参数，=> Int指定了代码块a的返回类型
    def f3(a: => Int): Unit = {
      println(s"a => $a") // 调用第一次
      println(s"a => $a") // 调用第二次
    }

    f3(f1())
    f3(23)
    f3({
      println("代码块执行")
      24
    })
  }
}
