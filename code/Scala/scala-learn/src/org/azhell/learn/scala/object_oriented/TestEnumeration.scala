package org.azhell.learn.scala.object_oriented

object TestEnumeration {
  def main(args: Array[String]): Unit = {
    println(WorkDay.MONDAY)
  }
}

// 定义枚举对象
object WorkDay extends Enumeration {
  val MONDAY: WorkDay.Value = Value(1, "周一")
  val TUESDAY: WorkDay.Value = Value(2, "周二")
}

// 定义应用类
object TestApp extends App {
  // 相当于自带main方法
  println("app Start...")

  // 类型别名
  type MyString = String
  val a: MyString = "abc"
  println(a)
}
