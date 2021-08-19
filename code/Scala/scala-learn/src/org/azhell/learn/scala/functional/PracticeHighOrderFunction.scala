package org.azhell.learn.scala.functional

/**
 * 针对scala高阶函数部分的两个练习
 */
object PracticeHighOrderFunction {
  def main(args: Array[String]): Unit = {
    // 练习1 匿名函数
    val fun = (i: Int, s: String, c: Char) => {
      if (i == 0 && s == "" && c == '0')
        false
      else
        true
    }
    println(fun(0, "", '0'))
    println(fun(1, "", '0'))
    println(fun(0, "hello", '0'))
    println(fun(0, "", '1'))

    // 练习2 多阶函数的使用
    def func(i: Int): String => Char => Boolean = {
      def func1(s: String): Char => Boolean = {
        def func2(c: Char): Boolean = {
          if (i == 0 && s == "" && c == '0')
            false
          else
            true
        }

        func2
      }

      func1
    }

    println(func(0)("")('0'))
    println(func(1)("")('0'))
    println(func(0)("hello")('0'))
    println(func(0)("")('1'))

    // 匿名函数简化
    /*
     * 简化原则
     * 1、内部函数的匿名化
     * 2、因为最外层指定了输入参数，所以参数类型可以省略
     * 3、单行代码，去除大括号
     *
     */
    def func3(i: Int): String => Char => Boolean = {
      s =>
        c =>
          if (i == 0 && s == "" && c == '0')
            false
          else
            true
    }

    println(func3(0)("")('0'))
    println(func3(1)("")('0'))
    println(func3(0)("hello")('0'))
    println(func3(0)("")('1'))

    // 柯里化
    def func4(i: Int)(s: String)(c: Char): Boolean = {
      if (i == 0 && s == "" && c == '0')
        false
      else
        true
    }

    println(func4(0)("")('0'))
    println(func4(1)("")('0'))
    println(func4(0)("hello")('0'))
    println(func4(0)("")('1'))
  }
}
