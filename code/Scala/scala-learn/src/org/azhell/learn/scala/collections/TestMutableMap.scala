package org.azhell.learn.scala.collections

import scala.collection.mutable

/**
 * 测试可变map
 */
object TestMutableMap {
  def main(args: Array[String]): Unit = {
    val map: mutable.Map[String, Int] = mutable.Map("a" -> 13, "b" -> 12, "hello" -> 5)
    println(map)
    println(map.getClass)

    // 添加元素
    map.put("c", 15)
    println(map)

    map += (("d", 17)) // 其实添加的是一个元组，注意是双层括号
    println(map)

    // 删除元素
    println(map("c"))
    println(map.remove("c"))
    println(map.getOrElse("c", 15))
    map -= "d"
    println(map)

    val map1: Map[String, Int] = Map("f" -> 3, "e" -> 34, "world" -> 10)
    map ++= map1
    val map2 = map1 ++ map
    println(map)
    println(map2)
  }
}
