package org.azhell.learn.spark.training

import org.apache.spark.util.AccumulatorV2
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable

/**
 * 针对AnalyzePopular10Categories实现方式的优化
 * 使用累加器彻底替代reduceByKey
 */
object AnalyzePopular10CategoriesOptimization3 {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[*]").setAppName("AnalyzePopular10CategoriesOptimization3")
    val sc = new SparkContext(conf)

    // 读取原始日志数据
    val actionRDD = sc.textFile("D:/尚硅谷课程笔记/spark/2.资料/spark-core数据/user_visit_action.txt")
    actionRDD.cache()

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
    ).take(10).foreach(println)

    // 将结果采集到控制台打印出来

    sc.stop()

  }

}

case class PopularCategory(cid: String, var clickCnt: Int, var orderCnt: Int, var payCnt: Int)

/**
 * 自定义累加器
 * 泛型
 * IN (品类ID，行为类型)
 * Out Map[String,PopularCategory]
 */
class PopularCategoriesAccumulator extends AccumulatorV2[(String, String), mutable.Map[String, PopularCategory]] {
  private val popularCategoriesMap = mutable.Map[String, PopularCategory]()

  override def isZero: Boolean = {
    popularCategoriesMap.isEmpty
  }

  override def copy(): AccumulatorV2[(String, String), mutable.Map[String, PopularCategory]] = {
    new PopularCategoriesAccumulator()
  }

  override def reset(): Unit = {
    popularCategoriesMap.clear()
  }

  override def add(v: (String, String)): Unit = {
    val cid = v._1
    val actionType = v._2
    val popularCategory = popularCategoriesMap.getOrElse(cid, PopularCategory(cid, 0, 0, 0))
    if (actionType == "click") {
      popularCategory.clickCnt += 1
    } else if (actionType == "order") {
      popularCategory.orderCnt += 1
    } else if (actionType == "pay") {
      popularCategory.payCnt += 1
    }
    popularCategoriesMap.update(cid, popularCategory)
  }

  override def merge(other: AccumulatorV2[(String, String), mutable.Map[String, PopularCategory]]): Unit = {
    val otherMap = other.value
    otherMap.foreach {
      case (cid, pc) =>
        val popularCategory = popularCategoriesMap.getOrElse(cid, PopularCategory(cid, 0, 0, 0))
        popularCategory.clickCnt += pc.clickCnt
        popularCategory.orderCnt += pc.orderCnt
        popularCategory.payCnt += pc.payCnt
        popularCategoriesMap.update(cid, popularCategory)
    }
  }

  override def value: mutable.Map[String, PopularCategory] = {
    popularCategoriesMap
  }
}

