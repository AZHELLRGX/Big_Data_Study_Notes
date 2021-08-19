package org.azhell.learn.scala.collections

import scala.collection.mutable.ListBuffer

/**
 * 测试可变列表
 */
object TestListBuffer {
  def main(args: Array[String]): Unit = {
    // 创建可变列表
    val list = new ListBuffer[Int]()
    val list1 = ListBuffer(12, 45, 67)
    println(list)
    println(list1)

    // 添加元素
    list.append(68)
    list1.prepend(20)
    list.insert(1, 19)
    println(list)
    println(list1)

    // 使用符号添加元素
    31 +=: 96 +=: list += 25 += 11
    println(list)

    // 连接两个list
    val list2 = list ++ list1 // 返回的是一个新的list
    println(list)
    println(list1)
    println(list2)

    list ++= list1
    // 如上操作改变的是list，不是list1
    println(list)
    println(list1)
    list ++=: list1
    // 如上操作改变的是list1，不是list，list1的元素被追加到了list
    println(list)
    println(list1)

    // 修改元素
    list2(3) = 20
    list2.update(4, 39) // 这两个操作是同样的效果
    println(list2)

    // 删除元素
    list2.remove(2)
    list2 -= 11
    println(list2)
  }
}
