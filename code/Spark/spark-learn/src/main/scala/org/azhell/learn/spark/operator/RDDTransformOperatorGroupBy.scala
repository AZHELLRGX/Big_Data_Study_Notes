package org.azhell.learn.spark.operator

import org.apache.spark.rdd.RDD

/** groupBy会将数据源中的每一个元素进行分组判断，根据返回的分组key进行分组
 * 相同的key值的数据会放置在一个组中
 */
object RDDTransformOperatorGroupBy {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorGroupBy")
    val rdd = makeRDD(context, List(1, 2, 3, 4), 2)

    val groupRDD: RDD[(Int, Iterable[Int])] = rdd.groupBy(_ % 2)

    groupRDD.collect().foreach(println)

    val wordRDD = context.makeRDD(List("hello", "scala", "spark", "hadoop"), 2)
    wordRDD.groupBy(_.charAt(0)).collect().foreach(println)
  }
}
