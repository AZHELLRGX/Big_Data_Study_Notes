package org.azhell.learn.scala.object_oriented

object TestAccess {
  def main(args: Array[String]): Unit = {
    // 创建对象
    val person : Person = new Person
    println(person.age)
    println(person.sex)
    person.printInfo()

    val worker : Worker = new Worker
    worker.printInfo()
  }
}

// 定义一个子类
class Worker extends Person {
  override def printInfo(): Unit = {
    println("Worker: ")
//    println(idCard) // 无法访问父类的私有属性
    name = "Bob"
    age = 25
    sex = "male"
    println(s"Worker: $name $sex $age")
  }
}
