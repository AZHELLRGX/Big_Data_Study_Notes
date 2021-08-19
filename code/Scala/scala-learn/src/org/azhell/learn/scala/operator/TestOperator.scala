package org.azhell.learn.scala.operator

import scala.language.postfixOps
/**
 * 测试scala中的运算符
 */
object TestOperator {
  def main(args: Array[String]): Unit = {
    /*
    大部分的scala运算符的使用方法与效果都和java一致
    1、算术运算符的精度等问题与java一致
    2、比较运算符存在一定的差别
    3、逻辑运算符的短路设计与java一致（只有&&与||存在短路运算，&不是）
    4、算术运算符的使用与java也基本一致，
    但是去除了 i++ 与 ++i的操作，因为这也会经常引起混淆
    另外scala中不存在对于 += 与 -= 等赋值运算符的自动类型转换，如果强行赋值编译期间会报错
    5、位运算符与java的符号和产生的效果也是完全一致的


    有一个很重要的语言设计上的知识点
    在scala中，所有的运算符都是方法，可以使用`.`进行调用
    这是一个与java等其他语言很不同的一个设计
    当然为了与java兼容，直接使用运算符号也是ok的，也很方便

    部分地方存在细节上的不同，记录在下面
     */

    // scala中，规避了java中字符串比较的时候`==`与equals方法效果不一致的易混淆规则
    val a: String = "hello"
    val b: String = new String("hello")
    println(a == b)
    println(a.equals(b))
    // 如果有需要比较对象地址符的必要，可以使用eq方法
    println(a.eq(b))

    // 甚至在scala中支持如下语法，这个设计对于函数式编程是非常方便的
    // println(7.5 toInt toString)  // 目前在2.13.5版本中尝试失败，不知道是否是语法设计变更了
    println(7.5.+(1))
    var s = 1
    s += 1
  }
}
