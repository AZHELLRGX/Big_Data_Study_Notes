package org.azhell.learn.spark.training

import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

/**
 * 针对AnalyzePopular10Categories实现方式的优化
 * 使用了大量的reduceByKey，会产生大量的shuffle操作
 * reduceByKey聚合算子，spark会提供优化，缓存
 */
object AnalyzePopular10CategoriesOptimization2 {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[*]").setAppName("AnalyzePopular10CategoriesOptimization2")
    val sc = new SparkContext(conf)

    // 读取原始日志数据
    val actionRDD = sc.textFile("D:/尚硅谷课程笔记/spark/2.资料/spark-core数据/user_visit_action.txt")
    actionRDD.cache()

    // 统计品类的点击数量（品类ID、点击数量）
    // 统计品类的下单数量（品类ID、下单数量）
    // 统计品类的支付数量（品类ID、支付数量）
    val actionCountRDD = actionRDD.flatMap(
      action => {
        val data = action.split("_")
        if (data(6) != "-1") {
          // 转换数据类型是为了和下面的数据对齐
          Array((data(6), (1, 0, 0)))
        } else if (data(8) != "null") {
          val orderIDs = data(8).split(",")
          orderIDs.map(orderID => (orderID, (0, 1, 0)))
        } else if (data(10) != "null") {
          val payIDs = data(10).split(",")
          payIDs.map(payID => (payID, (0, 0, 1)))
        } else {
          Nil
        }
      }
    )

    // 将品类进行排序，取前十名（点击数量排序、下单数量排序、支付数量排序）
    val sortRDD = actionCountRDD.reduceByKey(
      (t1, t2) => {
        (t1._1 + t2._1, t1._2 + t2._2, t1._3 + t2._3)
      }
    ).sortBy(_._2, ascending = false)

    // 将结果采集到控制台打印出来
    sortRDD.take(10).foreach(println)

    sc.stop()

  }

}
