package org.azhell.learn.spark.sql

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SparkSqlDemo {
  def main(args: Array[String]): Unit = {
    val conf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("SparkSQLDemo")
    //创建 SparkSession 对象
    val spark: SparkSession = SparkSession.builder().config(conf).getOrCreate()
    val sc = spark.sparkContext
    //RDD=>DataFrame=>DataSet 转换需要引入隐式转换规则，否则无法转换
    //spark 不是包名，是上下文环境对象名
    val dataFrame: DataFrame = spark.read.text("data/users")
    dataFrame.show()

    val rdd = sc.textFile("data/users")
    val mapRDD: RDD[User] = rdd.map(line => {
      val data = line.split("\t")
      User(data(0).toInt, data(1), data(2).toInt)
    })
    // This import is needed
    import spark.implicits._
    // Dataset是强类型
    val userDS: Dataset[User] = mapRDD.toDS() // RDD转换Dataset
    userDS.show()
    // 将DataFrame转换为Dataset
    val df = sc.makeRDD(List((1, "zhangsan", 30), (2, "lisi", 49)))
      .toDF("id", "name", "age")
    val ds = df.as[User]
    ds.show()

    // 注册udf函数
    spark.udf.register("addName", (x: String) => {
      "name is " + x
    })

    // 使用udf函数
    spark.sql("select addName('Lucy') as name").show()
  }


  case class User(id: Int, name: String, age: Int)
}


