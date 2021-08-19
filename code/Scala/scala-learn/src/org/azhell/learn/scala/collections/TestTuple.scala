package org.azhell.learn.scala.collections

object TestTuple {
  def main(args: Array[String]): Unit = {
    // 创建元组
    val tuple = ("hello", 13, 'a', true)

    // 访问元素
    println(tuple._1)

    println(tuple.productElement(1))

    // 遍历元组
    for (element <- tuple.productIterator){
      println(element)
    }

    // 嵌套元组
    val mulTuple = ("world",3,tuple)
    println(mulTuple._3._1)
  }
}
