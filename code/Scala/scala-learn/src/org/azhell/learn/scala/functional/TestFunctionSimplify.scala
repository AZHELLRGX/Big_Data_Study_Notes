package org.azhell.learn.scala.functional

/**
 * 演示scala的至简原则
 */
object TestFunctionSimplify {
  def main(args: Array[String]): Unit = {
    /*
     * （1）return 可以省略，Scala 会使用函数体的最后一行代码作为返回值
     * （2）如果函数体只有一行代码，可以省略花括号
     * （3）返回值类型如果能够推断出来，那么可以省略（:和返回值类型一起省略）
     * （4）如果有 return，则不能省略返回值类型，必须指定
     * （5）如果函数明确声明 unit，那么即使函数体中使用 return 关键字也不起作用
     * （6）Scala 如果期望是无返回值类型，可以省略等号
     * （7）如果函数无参，但是声明了参数列表，那么调用时，小括号，可加可不加
     * （8）如果函数没有参数列表，那么小括号可以省略，调用时小括号必须省略
     * （9）如果不关心名称，只关心逻辑处理，那么函数名（def）可以省略
     */
    // （0）函数标准写法
    def f(s: String): String = {
      return s + " jinlian"
    }

    println(f("Hello"))
    // 至简原则:能省则省

    //（1） return 可以省略,Scala 会使用函数体的最后一行代码作为返回值
    def f1(s: String): String = {
      s + " jinlian"
    }

    println(f1("Hello"))

    //（2）如果函数体只有一行代码，可以省略花括号
    def f2(s: String): String = s + " jinlian"

    //（3）返回值类型如果能够推断出来，那么可以省略（:和返回值类型一起省略）
    def f3(s: String) = s + " jinlian"

    println(f3("Hello3"))

    //（4）如果有 return关键字，则不能省略返回值类型，必须指定。
    def f4(): String = {
      return "ximenqing4"
    }

    println(f4())

    //（5）如果函数明确声明 unit，那么即使函数体中使用 return 关键字也不起作用，但是不会像java一样报语法错误
    def f5(): Unit = {
      return "dalang5"
    }

    println(f5())

    //（6）Scala 如果期望是无返回值类型,可以省略等号
    // 将无返回值的函数称之为过程
    def f6() {
      "dalang6"
    }

    println(f6())

    //（7）如果函数无参，但是声明了参数列表，那么调用时，小括号，可加可不加
    def f7() = "dalang7"

    println(f7())
    println(f7)

    //（8）如果函数没有参数列表，那么小括号可以省略,调用时小括号也必须省略
    def f8 = "dalang"
    //println(f8())  // 函数不带括号，调用的时候也不能带括号
    println(f8)

    //（9）如果不关心名称，只关心逻辑处理，那么函数名（def）可以省略
    def f9 = (x: String) => {
      println("wusong")
    }

    def f10(f: String => Unit) = {
      f("")
    }

    f10(f9)
    // 调用f10传入了一个函数作为参数
    println(f10((x: String) => {
      println("wusong")
    }))

  }
}
