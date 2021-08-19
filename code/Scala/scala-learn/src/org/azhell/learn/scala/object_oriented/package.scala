package org.azhell.learn.scala

/**
 * scala可以定义包对象，如下object即为一个包对象
 * 包对象的定义必须和包是同级的才能被包下面的其他对象访问到
 */
package object object_oriented {
  // 可以定义一些当前包共享的属性和方法
  val commonValue = "天猫"
  def commonMethod(): Unit ={
    println(s"${commonValue}是一家大型电商平台")
  }
}
