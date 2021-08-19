package org.azhell.learn.scala.datatype

/**
 * 测试scala基本的数据类型
 * scala与java不一样，没有所谓的基本数据类型和对象数据类型之分
 * 全部的数据类型都是对象，不用装包拆包
 */
object TestBaseDataType {
  def main(args: Array[String]): Unit = {
    /*
        * byte：8位补码，取值范围是[-128,127]
        * short：16位补码
        * int：32位补码
        * long：64位补码
        */

    val b: Byte = 1
    // var b1:Byte = 128 // 编译期间直接error

    /**
     * scala中默认整型数据类型是Int
     * 默认浮点型数据类型是Double
     */
    // var i = 1234567890123 // 大于int表示范围的整型数据，需要指定具体类型
    var i = 1234567890123L
    // 就算指定了数据类型，还是需要在数据后面加上符号，因为数据是先判断类型，再赋值给变量的
    var l: Long = 1234567890123L

    // var d:Float = 1.2345 // 默认浮点型数据是Double，这段代码会编译异常
    var f: Float = 1.2345F

    /* 如果编译期间可以算出来的数据，不用显示的指定类型；
    下面这段代码其实没有问题，scala代码插件提示错误是不需要关注的
    当然可以显式的加上toByte等
    */
    var a: Byte = (10 + 20).toByte // 没有toByte也不会报错

    /*
     * 但是如果是有变量参与计算的赋值，编译期间就会出现错误
     * 当然这个只会针对比默认数据类型表示范围低的数据，比如Byte与Short
     * 因为编译器无法感知计算结果【int类型】是否超出了Byte或者Short或者表示的范围，
     * 为了避免再运行期间报错，直接在编译期间认定为错误
     * 可以使用类型转换来避免
     */
    var c: Byte = (b + 20).toByte
    val s: Short = 10
    var l1: Short = (s + s).toShort

    // char类型的存储其实是ASCII码
  }
}
