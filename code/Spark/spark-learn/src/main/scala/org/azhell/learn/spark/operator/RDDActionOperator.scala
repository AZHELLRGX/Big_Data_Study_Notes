package org.azhell.learn.spark.operator
/**
 * 所有行动算子介绍
 * 所谓行动算子，其实底层就是调用了runJob方法，根据DAG图创建ActiveJob，并提交执行
 *
 */
object RDDActionOperator {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDActionOperator")
    val rdd = makeRDD(context, List(1, 2, 3, 4))
    val rdd1 = makeRDD(context, List(("a", 4), ("b", 8)))

    val result: Int = rdd.reduce(_ + _)
    println(result)

    // 会将不同分区的数据按照分区顺序采集数据到driver端的内存中
    val array: Array[Int] = rdd.collect()

    rdd.count()

    rdd.first()

    rdd.take(3)

    // 数据排序以后再取前三，默认是升序排列
    rdd.takeOrdered(3)

    /*
    与[[PairRDDFunctions.aggregateByKey]]存在区别
    aggregateByKey的初始值只对分区内计算产生作用
    但是aggregate会对分区内和分区间计算都起作用
     */
    val aggregateResult: Int = rdd.aggregate(0)(_ + _, _ + _)

    // 当让上面的写法也可以使用fold方法简化
    val foldResult: Int = rdd.fold(0)(_ + _)

    // 统计每个元素出现的次数
    val intToLong: collection.Map[Int, Long] = rdd.countByValue()

    // 根据key统计出现的次数，只有Pair类型的RDD才可以进行这样的操作
    val stringToLong: collection.Map[String, Long] = rdd1.countByKey()

    // 写出到文件的一些行动算子
    rdd.saveAsTextFile("output")
    rdd.saveAsObjectFile("output")
    // RDD数据类型必须要是键值类型
    rdd1.saveAsSequenceFile("output")

    // 数据先按照分区顺序采集到driver端，然后再打印输出
    rdd.collect().foreach(println)
    // 数据是在executor端直接打印的
    rdd.foreach(println)

    //
  }
}
