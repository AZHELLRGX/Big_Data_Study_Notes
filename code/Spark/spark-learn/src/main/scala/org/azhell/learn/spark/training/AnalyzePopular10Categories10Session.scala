package org.azhell.learn.spark.training

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * 需求2
 * Top10  热门品类中 每个品类的 的 Top10  活跃 Session 【判断标准就是点击次数】 统计
 */
object AnalyzePopular10Categories10Session {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[*]").setAppName("AnalyzePopular10Categories10Session")
    val sc = new SparkContext(conf)


    // 读取原始日志数据
    val actionRDD = sc.textFile("D:/尚硅谷课程笔记/spark/2.资料/spark-core数据/user_visit_action.txt")
    actionRDD.cache()

    val popularCategories = getPopular10Categories(sc, actionRDD)

    val categoryIds = popularCategories.map(_.cid)

    // 过滤原始数据
    val filterBy10CategoryIdsRDD = actionRDD.filter(
      action => {
        val data = action.split("_")
        // 数据量不大，不需要广播
        categoryIds.contains(data(6))
      }
    )

    // 先构造称为((cid,session),1)的结构，然后reduce
    val sessionReduceRDD = filterBy10CategoryIdsRDD.map(
      action => {
        val data = action.split("_")
        ((data(6), data(2)), 1)
      }
    ).reduceByKey(_ + _)

    // 转换数据结构，准备排序
    val sessionRDD = sessionReduceRDD.map {
      case ((categoryId, session), count) => (categoryId, (session, count))
    }

    // 分组后组内排序，并且保留前十
    val sortByClickCountRDD: RDD[(String, List[(String, Int)])] = sessionRDD.groupByKey().map {
      case (categoryId, iterable: Iterable[(String, Int)]) =>
        (categoryId, iterable.toList.sortBy(_._2)(Ordering.Int.reverse).take(10))
    }

    // 每个categoryId输出前十的信息
    sortByClickCountRDD.collect().foreach(println)

    // 将结果采集到控制台打印出来

    sc.stop()

  }

  def getPopular10Categories(sc: SparkContext, actionRDD: RDD[String]): List[PopularCategory] = {
    val categoriesAccumulator = new PopularCategoriesAccumulator
    sc.register(categoriesAccumulator, "PopularCategoriesAccumulator")

    // 统计品类的点击数量（品类ID、点击数量）
    // 统计品类的下单数量（品类ID、下单数量）
    // 统计品类的支付数量（品类ID、支付数量）
    actionRDD.foreach(
      action => {
        val data = action.split("_")
        if (data(6) != "-1") {
          // 转换数据类型是为了和下面的数据对齐
          categoriesAccumulator.add(data(6), "click")
        } else if (data(8) != "null") {
          val orderIDs = data(8).split(",")
          orderIDs.map(orderID => categoriesAccumulator.add(orderID, "order"))
        } else if (data(10) != "null") {
          val payIDs = data(10).split(",")
          payIDs.map(payID => categoriesAccumulator.add(payID, "pay"))
        } else {
          Nil
        }
      }
    )

    // 将品类进行排序，取前十名（点击数量排序、下单数量排序、支付数量排序）
    categoriesAccumulator.value.values.toList.sortWith(
      (left, right) => {
        if (left.clickCnt > right.clickCnt) {
          true
        } else if (left.clickCnt == right.clickCnt) {
          if (left.orderCnt > right.orderCnt) {
            true
          } else if (left.orderCnt == right.orderCnt) {
            left.payCnt > right.payCnt
          } else {
            false
          }
        } else {
          false
        }
      }
    ).take(10)
  }

}

