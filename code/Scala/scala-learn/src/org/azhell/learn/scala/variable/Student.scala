package org.azhell.learn.scala.variable

// 如果变量的类型设置为val，那么这个属性也不可以修改了
class Student(var name: String, var age: Int) {
  def printInfo(): Unit = {
    println(name + " " + age + " " + Student.school)
  }
}

/**
 * 引入伴生对象
 */
object Student {
  val school: String = "ctgu"

  def main(args: Array[String]): Unit = {
    val alice = new Student("alice", 18)
    val bob = new Student("bob", 20)

    alice.printInfo()
    bob.printInfo()
  }
}
