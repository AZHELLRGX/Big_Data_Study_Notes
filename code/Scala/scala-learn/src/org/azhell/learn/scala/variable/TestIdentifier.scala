package org.azhell.learn.scala.variable

/**
 * 测试scala的标识符
 */
object TestIdentifier {
  def main(args: Array[String]): Unit = {
    // 1、scala有和java类似的标识符定义规则，即以字母或者下划线开头，后接字母、数字、下划线
    var _3c: Int = 0

    /*
    scala允许使用操作符【+ - * / # !】，但是如果使用操作符作为标识符
    那么标识符中只能存在操作符，禁止与字母、数字、下划线混用
    todo：我发现如果使用操作符作为标识符，无法指定变量类型，不知道为什么，后面补充
     */
    val +-*/ = "hello"

    /*
    如果使用了scala中的关键字作为标识符，那么需要使用 `` 符号将关键字引用起来
     */
    var `class`: String = "class"

  }

  /**
   * 方法名是关键字，也使用同样的方法
   */
  def `def`(): Unit = {

  }
}
