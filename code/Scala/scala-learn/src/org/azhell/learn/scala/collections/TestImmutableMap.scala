package org.azhell.learn.scala.collections

/**
 * 测试不可变map
 */
object TestImmutableMap {
  def main(args: Array[String]): Unit = {
    // 创建map
    val map: Map[String, Int] = Map("a" -> 13, "b" -> 12, "hello" -> 5)
    println(map)
    println(map.getClass)
    // 遍历元素
    map.foreach(println)

    map.keys.foreach(println)
    println(map.keySet.mkString(","))
    map.values.foreach(println)

    // 访问元素
    println(s"hello -> ${map("hello")}")  // 获取不到元素值抛出异常
    println(s"hello -> ${map.getOrElse("hello",0)}")  // 获取不到元素值则返回默认值
  }
}
