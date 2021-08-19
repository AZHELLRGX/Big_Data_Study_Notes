package org.azhell.learn.spark.operator

import org.apache.spark.rdd.RDD

/**
 * aggregateByKey
 * 分区间计算规则和分区内计算规则可以分别单独指定
 */
object RDDTransformOperatorAggregateByKey {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorAggregateByKey")
    val rdd = context.makeRDD(List(("a", 1), ("a", 2), ("b", 3), ("b", 4)), 4)

    /*
    有两个参数列表：scala的函数柯里化
    第一个参数列表：
      需要传递一个参数，表示为初始值
      主要用于当遇见第一个key的时候，和value进行分区内计算
    第二个参数列表：传递两个参数
      第一个参数表示分区内计算规则
      第二个参数表示分区间计算规则
     */
    rdd.aggregateByKey(0)(
      (x, y) => math.max(x, y),
      (x, y) => x + y
    ).collect().foreach(println)

    // 以上代码可以使用最简化原则简化为下面的写法
    rdd.aggregateByKey(0)(
      math.max,
      _ + _
    ).collect().foreach(println)

    // aggregateByKey的结果类型取决于zeroValue的类型，例如
    val aggregateRDD: RDD[(String, String)] = rdd.aggregateByKey("")(_ + _, _ + _)

    // 使用以上特性可以完成 求平均值 的功能
    val sumCountRDD: RDD[(String, (Int, Int))] = rdd.aggregateByKey((1, 0))(
      (t, v) => {
        // 分区内计算的时候，使用tuple类型记录 相同key的value和以及key出现的次数
        (t._1 + v, t._2 + 1)
      },
      (t1, t2) => {
        // 分区间计算的时候，只需要将sum和count加起来
        (t1._1 + t2._1, t1._2 + t2._2)
      }
    )
    val avgRDD: RDD[(String, Int)] = sumCountRDD.mapValues(
      tuple => {
        tuple._1 / tuple._2
      }
    )
    avgRDD.collect().foreach(println)
    // 也可以使用模式匹配的写法，更加直观
    val mapRDD: RDD[(String, Double)] = sumCountRDD.mapValues {
      case (sum, cnt) =>
        sum.toDouble / cnt
    }
    mapRDD.collect().foreach(println)
  }
}
