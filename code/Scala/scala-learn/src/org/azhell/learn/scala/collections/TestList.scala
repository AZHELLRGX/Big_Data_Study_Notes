package org.azhell.learn.scala.collections

/**
 * 不可变链表
 */
object TestList {
  def main(args: Array[String]): Unit = {
    // 创建一个list
    val list = List(16, 28, 89)
    println(list)

    // 访问以及遍历list元素
    println(list(1))
    list.foreach(println)

    // 列表添加元素
    val list1 = list :+ 10
    val list2 = 18 +: list1
    println(list)
    println(list1)
    println(list2)

    // 可以使用双冒号在列表开头添加元素
    val list3 = list2.::(51)
    println(list3)

    // 但是双冒号基本不会拿来添加元素，而是拿来创建一个list，如下操作
    val list4 = Nil.::(13)
    println(list4)
    val list5 = 32 :: Nil
    println(list5)
    // 所以创建的时候可以如下操作
    val list6 = 12 :: 45 :: 90 :: 67 :: Nil
    println(list6)

    val list7 = list5 :: list6
    println(list7) // list的第一个元素是list类型

    // 如果想要合并两个list
    val list8 = list5 ::: list6
    println(list8)
    // 使用`++`符号可以实现与上面一样的效果，其实底层就是三冒号
    val list9 = list5 ++ list6
    println(list9)
  }
}
