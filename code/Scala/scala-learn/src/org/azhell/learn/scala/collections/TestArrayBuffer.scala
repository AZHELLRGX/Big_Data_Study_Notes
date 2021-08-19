package org.azhell.learn.scala.collections

import scala.collection.mutable.ArrayBuffer

/**
 * 测试可变数组
 */
object TestArrayBuffer {
  def main(args: Array[String]): Unit = {
    // 创建可变数组
    val ab1: ArrayBuffer[Int] = new ArrayBuffer[Int]()
    val ab2 = ArrayBuffer(12, 45, 78) // 还是apply方法的语法糖
    println(ab1.mkString(","))
    println(ab2) // 隐式的调用了toString方法

    // 访问元素
    println(ab2(0))
    ab2(0) = 1
    println(ab2(0))

    // 添加元素
    val ab3 = ab1 :+ 15 // 同样需要赋值给新的数组
    println(ab3)
    // 针对可变数组
    ab1 += 19 // 不推荐将这个操作的结果再复制给新的对象，很容易出错
    println(ab1)
    28 +=: ab1 // 在开头追加元素
    println(ab1)

    // 可变集合不推荐使用符号操作，而推荐使用方法来操作
    ab1.append(78)
    ab1.prepend(90)
    println(ab1)
    // 插入方法
    ab1.insert(3, 89) // 2.13.0版本以后不再支持同时插入多个元素
    println(ab1)
    ab1.insertAll(2, ab2)
    println(ab1)

    // 删除元素
    ab1.remove(1)
    println(ab1)
    ab1.remove(2, 3) // 从2开始，删除3个元素
    println(ab1)

    ab1.append(89)
    ab1.prepend(89)
    println(ab1)
    ab1 -= 89 // 删除指定值的元素，但是只会删除找到的第一个元素
    println(ab1)

    // 可变数组转换为不可变数组
    val array = ab2.toArray
    println(array.mkString(","))

    // 不可变数组转换为可变数组
    val buffer = array.toBuffer
    println(buffer)
  }
}
