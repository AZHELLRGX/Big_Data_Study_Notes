package org.azhell.learn.scala.object_oriented

/**
 * 特质叠加
 * 由于一个类可以混入（mixin）多个 trait，且 trait 中可以有具体的属性和方法，若混入
 * 的特质中具有相同的方法（方法名，参数列表，返回值均相同），必然会出现继承冲突问题。
 * 冲突分为以下两种：
 * 1、第一种，一个类（Sub）混入的两个 trait（TraitA，TraitB）中具有相同的具体方法，且
 * 两个 trait 之间没有任何关系，解决这类冲突问题，直接在类（Sub）中重写冲突方法。
 * 2、第二种，一个类（Sub）混入的两个 trait（TraitA，TraitB）中具有相同的具体方法，且
 * 两个 trait 继承自相同的 trait（TraitC），及所谓的“钻石问题”，解决这类冲突问题，Scala
 * 采用了特质叠加的策略。
 * 所谓的特质叠加，就是将混入的多个 trait 中的冲突方法叠加起来，案例如下
 */
object TestTraitOverlying {
  def main(args: Array[String]): Unit = {
    val myBall = new MyBall
    println(myBall.describe())
  }
}

trait Ball {
  def describe(): String = {
    "ball"
  }
}

trait Color extends Ball {
  override def describe(): String = {
    "blue-" + super.describe()
  }
}

trait Category extends Ball {
  override def describe(): String = {
    "foot-" + super.describe()
  }
}

class MyBall extends Category with Color {
  override def describe(): String = {
    // 输出结果：my ball is a blue-foot-ball
    // 从输出结果可以看出，叠加顺序是从右往左叠加的
    "my ball is a " + super.describe() + ";\n" +
      // 如果想要调用某个指定的混入特质中的方法，可以增加约束：super[]
      "my ball is a " + super[Category].describe()
  }
}
