package org.azhell.learn.scala.datatype

import org.azhell.learn.scala.variable.Student

/**
 * 测试scala中的特殊类型Unit、Null、Nothing
 */
object TestSpecialType {

  def main(args: Array[String]): Unit = {
    /*
    1、Unit：表示无值，和其他语言中 void 等同。用作不返回任何结果的方法的结果
类型。 Unit 只有一个实例值，写成 ()
    2、Null：null , Null  类型只有一个实例值 null
    3、Nothing：Nothing 类型在 Scala 的类层级最低端；它是任何其他类型的子类型。
当一个函数，我们确定没有正常的返回值，可以用 Nothing 来指定返回类
型，这样有一个好处，就是我们可以把返回的值（异常）赋给其它的函数
或者变量（兼容性）
     */

    def func(): Unit = {
      println("func方法被调用了")
    }

    val f: Unit = func()
    println(f)

    // null 可以赋值给任意引用类型（AnyRef），但是不能赋值给值类型（AnyVal）
    // var a:Int = null // 所以这段代码是错误的

    var student: Student = null // 而这段代码则是正确的

    // Nothing一般用在异常处理中
    def funcWithException(): Nothing = {
      throw new NullPointerException
    }

    try {
      funcWithException()
    } catch {
      case ex: NullPointerException =>
        println(s"funcWithException方法抛出的异常是$ex")
    } finally {
      println("异常捕获代码块执行完成")
    }

    // 因为Nothing是所有类型的子类，所以下面可能抛出异常的代码可以返回为Int类型
    def funcWithException1(): Int = {
      val a: Boolean = true
      if (a) {
        throw new NullPointerException
      } else {
        1
      }
    }
  }

}
