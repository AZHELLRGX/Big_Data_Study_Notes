package org.azhell.learn.scala.object_oriented

/**
 * 特质混入
 */
object TestTraitMixin {
  def main(args: Array[String]): Unit = {
    val vulture = new Vulture
    vulture.eat()
    vulture.printInfo()

    // 动态混入特质
    val goshawk = new Goshawk with Wing{
      override var wingSpan: Float = 1.3F

      override def maximumWingspan: Float = 1.5F

      override def printInfo(): Unit = {
        println(s"${name}是${food}的$kind，属于$forms,翼展一般为${wingSpan}m")
      }
    }

    println(goshawk.maximumWingspan)
    goshawk.printInfo()


  }
}


// 翅膀特质
trait Wing {
  // 翼展
  var wingSpan: Float

  // 最大翼展
  def maximumWingspan: Float
}

class Vulture extends Animal with Raptor with Wing {
  override var forms: String = "鹰形目"
  name = "秃鹫"
  kind = "大型猛禽"
  food = "食腐性"
  override var wingSpan: Float = 2F


  override def eat(): Unit = {
    println(s"秃鹫是${food}猛禽，主要以哺乳动物的尸体为食")
  }

  // 重写父类方法
  override def printInfo(): Unit = {
    println(s"${name}是${food}的$kind，属于$forms,翼展一般为${wingSpan}m,最大翼展可达" + maximumWingspan + "m")
  }

  override def maximumWingspan: Float = 2.5F
}