package org.azhell.learn.scala.object_oriented

/**
 * 测试scala的Class
 */
object TestClass {
  def main(args: Array[String]): Unit = {

  }
}

// 定义一个类
class Student {
  // 定义一个属性
  /*
  所有类和属性默认都是public，但是不能显式声明，但是可以声明为private
   */
  private  var name: String = "alice"
  val age: Int = 18
  val sex: String = "male"
}
