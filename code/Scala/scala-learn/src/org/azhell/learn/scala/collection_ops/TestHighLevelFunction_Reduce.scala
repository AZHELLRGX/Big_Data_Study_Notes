package org.azhell.learn.scala.collection_ops

/**
 * 高阶函数之规约计算
 * 这部分函数在spark中也有很多类似实现
 */
object TestHighLevelFunction_Reduce {
  def main(args: Array[String]): Unit = {
    val list = List(1, 2, 3, 4)

    // 1. reduce
    println(list.reduce(_ + _)) // 这里只是演示，实际使用还是推荐使用sum函数
    println(list.reduceLeft(_ + _))
    println(list.reduceRight(_ + _))

    println("===========================")

    val list2 = List(3, 4, 5, 8, 10)
    println(list2.reduce(_ - _)) // -24
    println(list2.reduceLeft(_ - _))
    // 因为底层实现是递归调用，所以真正的减法实现是注释部分的逻辑
    println(list2.reduceRight(_ - _)) // 3 - (4 - (5 - (8 - 10))), 6

    println("===========================")
    // 2. fold  具有初始值的reduce
    println(list.fold(10)(_ + _)) // 10 + 1 + 2 + 3 + 4
    println(list.foldLeft(10)(_ - _)) // 10 - 1 - 2 - 3 - 4
    println(list2.foldRight(11)(_ - _)) // 3 - (4 - (5 - (8 - (10 - 11)))),  -5
  }
}
