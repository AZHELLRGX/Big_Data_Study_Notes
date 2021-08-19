package org.azhell.learn.scala.collection_ops

/**
 * 应用案例，普通的wordCount示例
 */
object PracticeCommonWordCount {
  def main(args: Array[String]): Unit = {
    val stringList: List[String] = List(
      "hello",
      "hello world",
      "hello scala",
      "hello spark from scala",
      "hello flink from scala"
    )

    // 对字符串进行切分
    val flatList = stringList.flatMap(_.split(" "))

    // 扁平化后的数据进行分组，返回的数据类型是Map[String, List[String]]
    /* 注意一个问题：这里不能简写为groupBy(_)，因为这里需要传入的是一个操作，
    _ + _，甚至是_._1这种操作都是可以简写的
    但是类似word => word这种简写的为 _ 的时候，编译器无法预知这是一个操作还是一个参数，
    从而会引起编译混淆
    * */
    val groupMap = flatList.groupBy(word => word)

    // 统计各组单词个数
    val wordCountMap = groupMap.map(kv => (kv._1, kv._2.length))
    println(wordCountMap)
    // 将map转换为list
    val wordCountList = wordCountMap.toList.sortWith(_._2 > _._2).take(3)
    println(wordCountList)
  }
}
