package org.azhell.learn.scala.collection_ops

import scala.collection.mutable

/**
 * 合并map应用案例
 */
object PracticeMergeMap {
  def main(args: Array[String]): Unit = {
    val map1 = Map("a" -> 1, "b" -> 3, "c" -> 6)
    val map2 = mutable.Map("a" -> 6, "b" -> 2, "c" -> 9, "d" -> 3)

    // ++操作只能相互覆盖
    println(map1 ++ map2)

    // 现在的场景是相同值相加，实现类似Spark的reduceByKey
    // 这里map2作为初始值，必须是一个可变集合
    val map3 = map1.foldLeft(map2)(
      (mergeMap, kv) => {
        val key = kv._1
        val value = kv._2
        mergeMap(key) = mergeMap.getOrElse(key, 0) + value
        mergeMap
      }
    )
    // 其实此时map2与map3的内容是一致的，甚至它们就是一个对象
    println(map2)
    println(map3)
    println(map2 == map3)
  }
}
