package org.azhell.learn.spark.training

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 针对AnalyzePopular10Categories实现方式的优化
 * actionRDD重复使用
 * cogoup性能问题
 */
object AnalyzePopular10CategoriesOptimization1 {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[*]").setAppName("AnalyzePopular10CategoriesOptimization1")
    val sc = new SparkContext(conf)

    // 读取原始日志数据
    val actionRDD = sc.textFile("D:/尚硅谷课程笔记/spark/2.资料/spark-core数据/user_visit_action.txt")
    actionRDD.cache()

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
    val rdd1 = clickActionCountRDD.map {
      case (cid, count) => (cid, (count, 0, 0))
    }
    val rdd2 = orderActionCountRDD.map {
      case (oid, count) => (oid, (0, count, 0))
    }
    val rdd3 = payActionCountRDD.map {
      case (pid, count) => (pid, (0, 0, count))
    }

    // 将三个数据合并在一起，统一进行聚合计算
    val reduceRDD = rdd1.union(rdd2).union(rdd3).reduceByKey(
      (t1, t2) => {
        (t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3)
      }
    )

    // 思路：使用scala的元组排序
    val sortRDD = reduceRDD.sortBy(_._2, ascending = false)

    // 将结果采集到控制台打印出来
    sortRDD.take(10).foreach(println)

    sc.stop()

  }
}
