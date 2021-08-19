package org.azhell.learn.scala.functional

/**
 * 演示scala中函数与对象方法的创建与调用
 */
object TestFunctionAndMethod {
  def main(args: Array[String]): Unit = {
    // 定义函数
    def sayHi(name: String): Unit = {
      println("hi, " + name)
    }

    // 函数可以嵌套
    def test(): Unit ={
      def testInner(): Unit ={
        println("嵌套结构函数")
      }
    }

    // 函数没有重写与重载的概念，这里报错
//    def sayHi(name: String, time: String): String = {
//      "hello, " + name + ",good " + time
//    }

    // 调用函数
    sayHi("Alice")

    // 调用对象方法
    TestFunctionAndMethod.sayHi("Alice")

    val str = TestFunctionAndMethod.sayHi("Alice", "afternoon")
    println(str)
  }

  // 定义对象方法
  def sayHi(name: String): Unit = {
    println("Hi, " + name)
  }

  // 带有返回值的对象方法
  def sayHi(name: String, time: String): String = {
    "hello, " + name + ",good " + time
  }
}
