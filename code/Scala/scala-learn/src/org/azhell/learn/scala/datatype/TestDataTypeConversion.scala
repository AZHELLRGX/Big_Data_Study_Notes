package org.azhell.learn.scala.datatype

/**
 * 测试scala中的类型转换
 */
object TestDataTypeConversion {
  def main(args: Array[String]): Unit = {
    /*
    scala的类型转换原则如下：
    （1）自动提升原则：有多种类型的数据混合运算时，系统首先自动将所有数据转换成
    精度大的那种数据类型，然后再进行计算。
    （2）把精度大的数值类型赋值给精度小的数值类型时，就会报错，反之就会进行自动
    类型转换。
    （3）（byte，short）和 char 之间不会相互自动转换。
    （4）byte，short，char 他们三者可以计算，在计算时首先转换为 int 类型。
     */


  }
}
