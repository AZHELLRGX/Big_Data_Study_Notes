package org.azhell.learn.spark.operator


/**
 * mapPartitionsWithIndex算子，
 * 比mapPartitions算子多了一个索引选择的功能
 */
object RDDTransformOperatorMapPartitionsWithIndex {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorMapPartitionsWithIndex")
    val rdd = makeRDD(context, List(1, 2, 3, 4), 2)

    rdd.mapPartitionsWithIndex(
      (index, iterator) => {
        if (index == 1) {
          iterator
        } else {
          Nil.iterator
        }
      }
    ).collect().foreach(println)

    rdd.mapPartitionsWithIndex(
      (index, iterator) => {
        iterator.map(
          num => {
            (index, num)
          }
        )
      }
    ).collect().foreach(println)
  }
}
