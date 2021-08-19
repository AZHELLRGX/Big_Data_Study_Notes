package org.azhell.learn.scala.collections

/**
 * 多维数组，也是不可变数组
 * scala原生支持创建6维数组
 */
object TestMulArray {
  def main(args: Array[String]): Unit = {
    // 创建二维数组
    val array = Array.ofDim[Int](2, 4)

    // 访问元素
    array(0)(2) = 19
    array(1)(0) = 25

    // 双重for循环遍历多维数组
    for (i <- array.indices; j <- array(i).indices) {
      print(array(i)(j) + "\t")
      if (j == array(i).length - 1) {
        println()
      }
    }

    array.foreach(_.foreach(println))
  }
}
