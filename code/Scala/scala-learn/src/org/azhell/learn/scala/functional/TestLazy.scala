package org.azhell.learn.scala.functional

/**
 * 演示scala的懒加载
 */
object TestLazy {
  def main(args: Array[String]): Unit = {
    lazy val result: Int = sum(1, 2)
    val a: Int = sum(1, 3)
    println(s"a = $a")
    println(s"result = $result")
    println(s"result = $result")
  }

  def sum(a: Int, b: Int): Int = {
    println("sum函数被调用了")
    a + b
  }
}
