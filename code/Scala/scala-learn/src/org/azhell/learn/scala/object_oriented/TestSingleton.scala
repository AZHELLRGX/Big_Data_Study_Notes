package org.azhell.learn.scala.object_oriented

/**
 * 饿汉式与懒汉式实现单例模式
 */
object TestSingleton {
  def main(args: Array[String]): Unit = {
    val lion: Lion = Lion.getInstance()
    val lion1: Lion = Lion.getInstance()
    println(lion == lion1)

    val lion2: Lion = Lion.getInstance1
    val lion3: Lion = Lion.getInstance1
    println(lion2 == lion3)
    println(lion2)
    println(lion3)
  }
}

// 定义类
class Lion private(var name: String) {
  def printInfo(): Unit = {
    println(s"$name is a ${Lion.kind}")
  }
}

object Lion {
  val kind: String = "Asian Lion"
  // 饿汉式
  private val lion: Lion = new Lion("辛巴")

  def getInstance(): Lion = lion

  // 懒汉式
  private var lion1: Lion = _

  def getInstance1: Lion = {
    if (lion1 == null) {
      lion1 = new Lion("辛巴")
    }
    lion
  }
}
