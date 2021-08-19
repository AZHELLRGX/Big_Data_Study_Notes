package org.azhell.learn.spark

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

package object operator {

  def createSparkContext(appName: String): SparkContext = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName(appName)
    new SparkContext(sparkConf)
  }

  def makeRDD(context: SparkContext, data: List[(String, Int)]): RDD[(String, Int)] = {
    context.makeRDD(data)
  }

  def makeRDD(context: SparkContext, data: List[Int], partitionCnt: Int = 1): RDD[Int] = {
    partitionCnt match {
      case 1 => context.makeRDD(data)
      case _ => context.makeRDD(data, partitionCnt)
    }
  }
}
