package org.azhell.learn.scala.object_oriented

/**
 * 测试scala的特征
 * Scala 语言中，采用特质 trait（特征）来代替接口的概念，也就是说，多个类具有相同
 * 的特质（特征）时，就可以将这个特质（特征）独立出来，采用关键字 trait 声明。
 * Scala 中的 trait 中即可以有抽象属性和方法，也可以有具体的属性和方法，一个类可
 * 以混入（ mixin）多个特质。这种感觉类似于 Java 中的抽象类。
 * Scala 引入 trait 特征，第一可以替代 Java 的接口，第二个也是对单继承机制的一种
 * 补充。
 */
object TestTrait {
  def main(args: Array[String]): Unit = {
    val goshawk: Goshawk = new Goshawk
    goshawk.eat()
    goshawk.printInfo()
  }
}

// 猛禽特质
trait Raptor {
  // 鹰形目、隼形目和鸮形目
  var forms: String
  // 除了秃鹫是食腐性
  var food: String = "掠食性"

  def fly(): Unit = {
    println("猛禽都善于飞翔")
  }

  // 猛禽的具体食谱不同
  def eat(): Unit
}

class Goshawk extends Animal with Raptor {
  override var forms: String = "鹰形目"
  name = "苍鹰"
  kind = "中小型猛禽"

  override def eat(): Unit = {
    println(s"苍鹰是${food}猛禽，主要以森林鼠类、野兔、雉类、榛鸡、鸠鸽类和其他小型鸟类为食")
  }

  // 重写父类方法
  override def printInfo(): Unit = {
    println(s"${name}是${food}的$kind，属于${forms}")
  }

}
