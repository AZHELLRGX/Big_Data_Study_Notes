package org.azhell.learn.spark.operator

import org.apache.spark.rdd.RDD

/**
 * groupByKey会将数据源中相同key的数据分组到一个组中，形成一个元组
 * 这个元组中第一个元素是key，第二个元素是相同key的value的集合
 */
object RDDTransformOperatorGroupByKey {
  def main(args: Array[String]): Unit = {

    val context = createSparkContext("RDDTransformOperatorGroupByKey")
    val rdd = makeRDD(context, List(("a", 1), ("a", 2), ("c", 3), ("b", 4)))


    val groupKeyRDD: RDD[(String, Iterable[Int])] = rdd.groupByKey()

    /**
     * 与groupBy的区别
     * val groupRDD: RDD[(String, Iterable[(String, Int)])] = rdd.groupBy(_._1)
     * 1、返回值不一样，groupByKey返回的RDD中value是源数据的value形成的集合，而groupBy则是源数据整体形成的集合
     * 2、groupByKey固定是按照key是否相同来进行分组的，而groupBy的分组条件不确定
     */

    /**
     * 与reduceByKey的区别
     * 1、reduceByKey对数据有预聚合操作，在shuffle的时候减少了落盘数据量，减少了IO对性能的影响
     * 2、如果是分组聚合的场景推荐reduceByKey，但是reduceByKey功能有限，如果分组后不是聚合，则只能使用groupByKey
     */


    groupKeyRDD.collect().foreach(println)
  }
}
