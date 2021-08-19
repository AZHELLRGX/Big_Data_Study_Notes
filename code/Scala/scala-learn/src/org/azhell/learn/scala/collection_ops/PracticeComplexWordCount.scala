package org.azhell.learn.scala.collection_ops

import scala.collection.MapView

/**
 * 应用案例，复杂的wordCount示例
 */
object PracticeComplexWordCount {
  def main(args: Array[String]): Unit = {
    val tupleList: List[(String, Int)] = List(
      ("hello", 1),
      ("hello world", 2),
      ("hello scala", 3),
      ("hello spark from scala", 1),
      ("hello flink from scala", 2)
    )

    // 1、直接展开为普通版本，可以实现，但是不推荐
    val newStringList: List[String] = tupleList.map(
      kv => {
        (kv._1.trim + " ") * kv._2
      }
    )
    println(newStringList)

    // 接下来操作与普通版本完全一致
    val wordCountList: List[(String, Int)] = newStringList
      .flatMap(_.split(" ")) // 空格分词
      .groupBy(word => word) // 按照单词分组
      .map(kv => (kv._1, kv._2.size)) // 统计出每个单词的个数
      .toList
      .sortBy(_._2)(Ordering[Int].reverse)
      .take(3)

    println(wordCountList)

    println("================================")

    // 2、直接基于预统计的结果进行的转换
    val preCountList = tupleList.flatMap(
      tuple => {
        val strings = tuple._1.split(" ")
        strings.map((_, tuple._2))
      }
    )
    println(preCountList)

    val preCountMap = preCountList.groupBy(_._1)
    println(preCountMap)

    val countMap: Map[String, Int] = preCountMap.view.mapValues(
      tempTupleList => {
        tempTupleList.map(_._2).sum
      }
    ).toMap
    println(countMap.toString())
    val countList = countMap.toList.sortWith(_._2 > _._2).take(3)
    println(countList)
  }
}
