package org.azhell.learn.scala.collections

/**
 * 测试不可变Set
 */
object TestImmutableSet {
  def main(args: Array[String]): Unit = {
    // 创建Set
    val set = Set(13, 23, 53, 12, 13, 45, 23)
    println(set)

    // 添加元素
    val set1 = set + 20 // 添加元素不需要冒号，因为set无序，不关心是头部还是尾部添加元素
    println(set)
    println(set1)

    // 合并Set
    val set2 = Set(56, 53, 97)
    //    val set3 = set + set2  // error
    val set3 = set ++ set2
    println(set3)

    // 删除元素
    val set4 = set3 - 13
    println(set4)
  }
}
