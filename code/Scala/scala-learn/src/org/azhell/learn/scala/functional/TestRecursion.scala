package org.azhell.learn.scala.functional

/**
 * 递归其实可以完成所有循环操作
 * 递归对程序员更加友好，但是对于机器来讲却不是好事，栈帧就是个大问题
 * 可能因为递归导致StackOverFlow
 * 所以需要进行尾递归优化：就是指某个函数在最后一步调用自身，而且不能存在额外的操作
 * 尾递归的支持得看具体语言的编译器
 */
object TestRecursion {
  def main(args: Array[String]): Unit = {
    println(factorial(5))
    println(factorialByRecursion(5))
    println(factorialByTailRecursion(5))
  }

  // 循环方式实现阶乘
  def factorial(n: Int): Int = {
    var result: Int = 1
    for (i <- 1 to n) {
      result *= i
    }
    result
  }

  // 递归方式实现阶乘
  def factorialByRecursion(n: Int): Int = {
    if (n == 0) 1
    else {
      // 存在额外操作，所以不是尾递归
      factorialByRecursion(n - 1) * n
    }
  }

  // 尾递归优化
  def factorialByTailRecursion(n: Int): Int = {
    @scala.annotation.tailrec // 这个注解确保我们的代码是尾递归的，否则编译器会报错
    def loop(n: Int, currRes: Int): Int = {
      if (n == 0) currRes
      else loop(n - 1, currRes * n)
    }

    loop(n, 1)
  }
}
