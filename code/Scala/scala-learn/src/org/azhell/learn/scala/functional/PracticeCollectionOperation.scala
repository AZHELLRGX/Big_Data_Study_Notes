package org.azhell.learn.scala.functional

/**
 * 使用高阶函数写一段针对数据处理的操作
 */
object PracticeCollectionOperation {
  def main(args: Array[String]): Unit = {
    // 定义一个数组
    val originalArray: Array[Int] = Array(12, 23, 36, 95)

    // 对数组进行处理，将操作抽象出来，处理完毕之后的结果返回一个新的数组
    def arrayOperation(array: Array[Int], opt: Int => Int): Array[Int] = {
      for (element <- array) yield opt(element)
    }

    // 定义具体的操作函数
    def addOne(element: Int): Int = {
      element + 1
    }

    println(arrayOperation(originalArray, addOne).mkString(","))

    // 匿名函数实现
    println(arrayOperation(originalArray, _ * 4).mkString(","))
  }
}
