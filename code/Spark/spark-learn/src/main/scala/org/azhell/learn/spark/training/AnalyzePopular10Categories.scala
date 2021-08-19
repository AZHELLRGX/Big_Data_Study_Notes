package org.azhell.learn.spark.training

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 需求：分析热门品类前10
 */
object AnalyzePopular10Categories {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[*]").setAppName("AnalyzePopular10Categories")
    val sc = new SparkContext(conf)

    // 读取原始日志数据
    val actionRDD = sc.textFile("D:/尚硅谷课程笔记/spark/2.资料/spark-core数据/user_visit_action.txt")

    // 统计品类的点击数量（品类ID、点击数量）
    val clickActionRDD = actionRDD.filter(
      action => {
        val data = action.split("_")
        // 过滤点击类型的日志
        data(6) != "-1"
      }
    )

    val clickActionCountRDD: RDD[(String, Int)] = clickActionRDD.map(
      clickAction => {
        val data = clickAction.split("_")
        (data(6), 1)
      }
    ).reduceByKey(_ + _)

    // 统计品类的下单数量（品类ID、下单数量）
    val orderActionRDD = actionRDD.filter(
      action => {
        val data = action.split("_")
        // 过滤订单类型的日志
        data(8) != "null"
      }
    )

    val orderActionCountRDD = orderActionRDD.flatMap(
      orderAction => {
        val data = orderAction.split("_")
        val orderIDs = data(8).split(",")
        orderIDs.map(orderID => (orderID, 1))
      }
    ).reduceByKey(_ + _)

    // 统计品类的支付数量（品类ID、支付数量）
    val payActionRDD = actionRDD.filter(
      action => {
        val data = action.split("_")
        // 过滤支付类型的日志
        data(10) != "null"
      }
    )

    val payActionCountRDD = payActionRDD.flatMap(
      payAction => {
        val data = payAction.split("_")
        val payIDs = data(10).split(",")
        payIDs.map(payID => (payID, 1))
      }
    ).reduceByKey(_ + _)


    // 将品类进行排序，取前十名（点击数量排序、下单数量排序、支付数量排序）
    val coGroupRDD: RDD[(String, (Iterable[Int], Iterable[Int], Iterable[Int]))] =
      clickActionCountRDD.cogroup(orderActionCountRDD, payActionCountRDD)
    val actionCountRDD: RDD[(String, (Int, Int, Int))] = coGroupRDD.mapValues {
      case (clickIterable, orderIterable, payIterable) =>
        var clickCount = 0
        var orderCount = 0
        var payCount = 0
        val clickIterator = clickIterable.iterator
        if (clickIterator.hasNext) {
          clickCount = clickIterator.next()
        }
        val orderIterator = orderIterable.iterator
        if (orderIterator.hasNext) {
          orderCount = orderIterator.next()
        }
        val payIterator = payIterable.iterator
        if (payIterator.hasNext) {
          payCount = payIterator.next()
        }
        (clickCount, orderCount, payCount)
    }

    // 思路：使用scala的元组排序
    val sortRDD = actionCountRDD.sortBy(_._2, ascending = false)

    // 将结果采集到控制台打印出来
    sortRDD.take(10).foreach(println)

    sc.stop()

  }
}
