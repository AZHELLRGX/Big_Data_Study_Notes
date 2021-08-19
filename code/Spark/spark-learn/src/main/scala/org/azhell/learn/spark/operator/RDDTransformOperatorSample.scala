package org.azhell.learn.spark.operator


object RDDTransformOperatorSample {
  def main(args: Array[String]): Unit = {
    val context = createSparkContext("RDDTransformOperatorSample")
    val rdd = makeRDD(context,List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

    /**
     * sample算子传递三个参数
     * 1、第一个参数表示，抽取数据后是否将数据返回，true（放回），false（丢弃）
     * 2、第二个参数表示，数据源中每条数据被抽取的概率
     * 如果抽取不放回，数据源中每条数据被抽取的概率，例如0.4
     * 如果抽取放回，表示每条数据可能被抽取的次数，例如2
     * 3、第三个参数表示，抽取数据时随机算法的种子
     * 如果不传递第三个参数，默认使用当前系统时间
     */
    println(rdd.sample(withReplacement = false, 0.4, 1).collect().mkString(";"))
  }
}
