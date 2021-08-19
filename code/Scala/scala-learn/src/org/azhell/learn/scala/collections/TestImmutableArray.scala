package org.azhell.learn.scala.collections

/**
 * 测试scala的不可变数组
 * 不可变集合只能进行改查，但是不能进行增删[针对原数组本身]
 */
object TestImmutableArray {
  def main(args: Array[String]): Unit = {
    // 1、创建数组
    val arr: Array[Int] = new Array[Int](5)

    // 2、使用伴生对象创建数组，类似抽象工厂，下面的创建方式其实调用的apply方法
    val arr2 = Array(1, 2, 3, 4, 5)

    // 访问数组中的元素，使用小括号
    println(arr(0))
    println(arr2(3))

    // 修改数组中的元素
    arr(0) = 1
    println(arr(0))

    // 遍历数组
    // 普通for循环
    for (i <- 0 until arr2.length) {
      println(arr(i))
    }
    // 上面是不推荐写法，可以直接使用scala自带的indices方法，底层实现其实都是Range
    for (i <- arr.indices) {
      println(arr(i))
    }
    // 增强for循环
    for (element <- arr) {
      println(element)
    }
    // 迭代器
    val iterator = arr2.iterator
    while (iterator.hasNext) {
      println(iterator.next())
    }

    arr2.foreach(println)

    // 数组数据拼接为字符串输出
    println(arr2.mkString(","))

    // 添加元素，生成新的数组
    val array = arr2.:+(73) // 尾插
    println(array.mkString(","))

    val array1 = array.+:(45) // 头插
    println(array1.mkString(","))

    // 以上的写法可以简化为运算符写法
    val array2 = array :+ 15
    println(array2.mkString(","))
    val array3 = 29 +: array1 // 注意是元素在前，数组在后
    println(array3.mkString(","))

  }

}
