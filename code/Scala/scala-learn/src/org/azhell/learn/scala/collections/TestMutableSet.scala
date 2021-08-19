package org.azhell.learn.scala.collections

import scala.collection.mutable

/**
 * 测试可变Set
 */
object TestMutableSet {
  def main(args: Array[String]): Unit = {
    // 创建Set
    val set: mutable.Set[Int] = mutable.Set[Int](13, 23, 53, 12, 13, 45, 23)
    println(set)

    // 添加元素
    set.add(24) // 返回值是Boolean类型，表示添加的元素是否已经在set集合中，true表示添加成功，也就是不存在，false则相反
    println(set)

    set += 11
    println(set)

    // 删除元素
    set.remove(11) // 返回值的含义与add函数相同
    println(set)

    // 合并两个set
    val set1: mutable.Set[Int] = mutable.Set[Int](14, 24, 54)
    set ++= set1
    println(set)
  }
}
