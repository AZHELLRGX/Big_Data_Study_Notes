package org.azhell.learn.scala.variable

/**
 * 测试scala定义常量与变量的语法
 */
object TestVariable {
  def main(args: Array[String]): Unit = {
    // 声明一个变量的通用语法
    var a: Int = 1
    // 变量声明时，类型可以省略，编译器会自动推导，即类型推导
    var b = 2
    // 类型确定后就不能修改，因为scala是强类型语言
    /* 变量声明时，必须有初始值例如
    java的一些基本数据类型是可以没有初始值的 例如 int i; i的初始值就是0
    但是这在scala中是不允许的，必须显式的指定初始值
     */

    /* 声明、定义一个变量的时候，
    可以使用var或者val来修饰，
    var修饰的变量可以改
    val修饰的变量不可修改，即常量
    和java一样，对于对象类型的常量，对象内部的属性是可以修改的
    */
    // 定义常量的方式
    val i: Int = 10
    // i = 20 这句话就是非法的

    val alice = new Student("alice", 18)
    alice.age = 19
  }
}
