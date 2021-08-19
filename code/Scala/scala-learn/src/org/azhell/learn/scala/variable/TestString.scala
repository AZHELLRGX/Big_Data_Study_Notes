package org.azhell.learn.scala.variable

/**
 * 测试scala中的字符串常用操作
 */
object TestString {
  def main(args: Array[String]): Unit = {
    // 字符串可以使用 + 号连接，这一点与java一致
    val name: String = "alice"
    val age: Int = 18
    println("name is " + name + ", age is " + age)

    // 使用*，将一个字符串复制多次并拼接
    println((name + ";") * 3)

    // printf用法：字符串，通过%传值
    printf("%d岁的%s在腾讯任职\n", age, name)

    // 字符串模版（插值字符串），通过$获取变量值，类似shell的语法
    // 字符串前面加上s表示这是一个模版字符串，是scala语法之一
    println(s"${age}岁的${name}在腾讯任职")
    // 看来scala真是兼众家语言之长啊

    val num: Double = 2.3456
    // 可以使用f进行 格式化模版字符串，以下表示：数字不足两位保留两位，小数部分保留两位（会自动四舍五入）
    println(f"num is $num%2.2f")
    // raw只会填充变量，不会处理其他的任何可能有意义的字符串
    println(raw"num is $num%2.2f")

    // 三引号表示字符串，保持多行字符串的原格式输出
    val sql =
      s"""
         |select *
         |from
         |  `student`
         |where
         |  `name` = '$name'
         |and
         |  `age` = $age
         |""".stripMargin
    println(sql)
  }
}
