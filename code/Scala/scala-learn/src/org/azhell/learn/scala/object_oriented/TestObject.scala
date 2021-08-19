package org.azhell.learn.scala.object_oriented

object TestObject {
  def main(args: Array[String]): Unit = {
    // val tiger:Tiger = new Tiger("胖虎")  // 私有化构造器以后不能访问类的私有构造器
    var tiger: Tiger = Tiger.newTiger("胖虎")
    tiger.printInfo()

    // apply方法
    tiger = Tiger("胖虎")
    tiger.printInfo()
  }
}

// 定义类
class Tiger private(var name: String) {
  def printInfo(): Unit = {
    // 使用伴生对象的常量【等同于java的类静态属性】
    println(s"$name is a ${Tiger.kind}")
  }
}

// 类的伴生对象
object Tiger {
  val kind: String = "Siberian Tiger"

  // 创建对象的工厂方法，伴生对象可以方法类的私有构造器
  def newTiger(name: String): Tiger = new Tiger(name)

  // 伴生对象的一个特殊方法，与上述方法达到的效果一致，scala底层存在一个优惠政策，使用的时候可以省略apply
  def apply(name: String): Tiger = new Tiger(name)
}
