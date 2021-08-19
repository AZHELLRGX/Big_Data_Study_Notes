package org.azhell.learn.spark.operator

import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.{PairRDDFunctions, RDD}


/**
 *
 */
object RDDTransformOperatorPartitionBy {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorPartitionBy")
    val rdd = makeRDD(context,List(1, 2, 3, 4, 5, 6))

    //
    /**
     * 这里有一个小知识点，使用到了隐式转换，使用了rddToPairRDDFunctions方法将RDD转换成为PairRDDFunctions
     * [[RDD.rddToPairRDDFunctions]] converts an RDD into a [[PairRDDFunctions]] for key-value-pair RDDs
     * 很多时候，PairRDDFunctions才可以进行键值对操作
     * 方法关键字implicit
     * 隐式转换是在二次编译的时候发生的
     */
    val mapRDD = rdd.map((_, 1))
    mapRDD.partitionBy(new HashPartitioner(2)).saveAsTextFile("output")

    /**
     * 1、如果使用partitionBy的时候，分区器不变，结果也不会再发生变化
     * 2、Spark自带两个可用分区器，HashPartitioner，RangePartitioner(sortBy的时候使用的就是这个分区器)
     * 3、
     */
  }
}
