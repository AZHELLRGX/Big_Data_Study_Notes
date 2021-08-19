package org.azhell.learn.scala.object_oriented

/**
 * 测试scala的抽象类、抽象方法、抽象属性
 * （1）定义抽象类：abstract class Person{} //通过 abstract 关键字标记抽象类
 * （2）定义抽象属性：val|var name:String //一个属性没有初始化，就是抽象属性
 * （3）定义抽象方法：def hello():String //只声明而没有实现的方法，就是抽象方法
 * 只要存在抽象方法和抽象属性，那么这个类一定是抽象类
 */
object TestAbstractClass {
  def main(args: Array[String]): Unit = {
    val electricFan: Furniture = new ElectricFan
    electricFan.purchase()
    electricFan.sell()
  }
}

abstract class Furniture {
  // 非抽象属性
  val name: String = "Furniture"
  var brand: String = "Haier"

  // 抽象属性
  var price: Int

  // 非抽象方法
  def purchase(): Unit = {
    println(s"买入了${brand}品牌的${name}的家具")
  }

  // 抽象方法
  def sell(): Unit
}

class ElectricFan extends Furniture {

  // 实现抽象属性和方法，override可以不写
  var price: Int = 150

  def sell(): Unit = {
    println(s"卖出一台${brand}品牌的$name，赚取了￥$price")
  }

  // 重写非抽象属性和方法
  override val name: String = "电风扇"
  // var的非抽象属性不能写override，否则编译报错
  brand = "Gree"

  override def purchase(): Unit = {
    super.purchase()
    println(s"买入一台${brand}品牌的$name，花费了￥$price")
  }
}

