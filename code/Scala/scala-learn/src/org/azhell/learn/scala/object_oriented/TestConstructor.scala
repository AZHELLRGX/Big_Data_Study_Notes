package org.azhell.learn.scala.object_oriented

object TestConstructor {
  def main(args: Array[String]): Unit = {
    var teacher: Teacher = new Teacher()
    teacher.Teacher()

    teacher = new Teacher("Lucy")
    teacher = new Teacher("Allen", 26)
  }
}

// 定义一个类，其实下面的类声明就是一个主构造器
class Teacher() {
  // 定义属性
  var name: String = _
  var age: Int = _

  println("主构造方法被调用")

  // 声明辅助构造方法
  def this(name: String) {
    this() // 直接调用主构造器
    println("辅助构造方法1被调用")
    this.name = name
    println(s"name: $name age: $age")
  }

  def this(name: String, age: Int) {
    this(name) // 调用前一个辅助构造器
    println("辅助构造方法2被调用")
    this.age = age
    println(s"name: $name age: $age")
  }

  def Teacher(): Unit = {
    println("不是构造方法而是一般方法被调用")
  }
}
