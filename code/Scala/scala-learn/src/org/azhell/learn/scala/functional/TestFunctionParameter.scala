package org.azhell.learn.scala.functional

/**
 * 演示scala的函数参数使用方式
 */
object TestFunctionParameter {
  def main(args: Array[String]): Unit = {
    /*
     * （1）可变参数
     * （2）如果参数列表中存在多个参数，那么可变参数一般放置在最后
     * （3）参数默认值，一般将有默认值的参数放置在参数列表的后面
     * （4）带名参数
     */
    // 可变参数与java的使用方式完全一致，只是语法有些不一致
    def f1(str: String*): Unit = {
      println(str) // 类型为ArraySeq
    }

    f1("Alice")

    // 参数默认值
    def f2(name: String = "Alice"): Unit = {
      println(name)
    }

    f2()
    f2("Bob")

    // 带名参数
    def f3(name: String, age: Int): Unit = {
      println(s"${name}今年已经${age}了")
    }

    f3("Alice", 14)
    f3(age = 13, name = "Bob")

    // 带名参数的一种使用场景
    def f4(age: Int = 15, name: String): Unit = {
      println(s"${name}今年已经${age}了")
    }

    f4(name = "Lucy")
  }
}
