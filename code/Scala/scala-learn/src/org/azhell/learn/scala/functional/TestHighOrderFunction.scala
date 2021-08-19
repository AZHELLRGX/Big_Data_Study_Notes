package org.azhell.learn.scala.functional

/**
 * 函数的高阶用法
 */
object TestHighOrderFunction {
  def main(args: Array[String]): Unit = {
    def f(num: Int): Int = {
      println("函数f被调用")
      num
    }

    // 1、接收函数的执行结果
    val r = f(123)
    println(r)

    // 2、将函数作为值进行传递
    val f1: Int => Int = f // 指定变量为函数类型
    val f2 = f _ // 不指定变量类型，而使用`函数名 + 下划线`的方式指明这是一个函数
    println(f1)
    println(f1(12))
    println(f2)
    println(f2(35))

    // 针对无参函数
    def fun(): Int = {
      println("函数f[无参]被调用")
      1
    }

    val v1 = fun // 虽然不报错，但是不推荐这样写，推荐下面的写法
    val v2 = fun()
    println(v1)
    println(v2)

    val f3: () => Int = fun
    val f4 = fun _ // 这里需要注意的是如果方法重载了，这种方式就不好用了
    println(f3)
    println(f4)

    // 3、函数作为参数进行传递
    def dualEval(opt: (Int, Int) => Int, a: Int, b: Int): Int = {
      opt(a, b)
    }

    def add(a: Int, b: Int): Int = {
      a + b
    }

    println(dualEval(add, 12, 13))
    println(dualEval((a, b) => a + b, 12, 13))
    println(dualEval(_ * _, 12, 3))

    // 4、函数作为函数的返回值进行返回
    def f5(): Int => Unit = {
      def f6(num: Int): Unit = {
        println("函数f6被调用" + num)
      }

      f6
    }
    val f6 = f5()
    println(f6)
    println(f6(23))
  }
}
