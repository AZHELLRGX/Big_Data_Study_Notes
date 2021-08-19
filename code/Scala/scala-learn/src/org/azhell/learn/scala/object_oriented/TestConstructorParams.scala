package org.azhell.learn.scala.object_oriented

/**
 * 测试scala的构造器参数
 * （1）未用任何修饰符修饰，这个参数就是一个局部变量
 * （2）var 修饰参数，作为类的成员属性使用，可以修改
 * （3）val 修饰参数，作为类只读属性使用，不能修改
 */
object TestConstructorParams {
  def main(args: Array[String]): Unit = {
    val professor: Professor = new Professor
    println(professor.name + ";" + professor.age)

    val professor1: Professor1 = new Professor1("Alice", 24)
    println(professor1.name + ";" + professor1.age)

    val professor2: Professor2 = new Professor2("Bob", 25)
    println(professor2.name + ";" + professor2.age)

    val professor3: Professor3 = new Professor3("Allen", 23)
    // professor3.name = "Lucy" // error 不允许修改
    println(professor3.name + ";" + professor3.age)

    val professor4: Professor4 = new Professor4("Lucy", 23)
    professor4.printInfo()

    val professor4_1: Professor4 = new Professor4("Lucy", 23, "CTGU")
    professor4_1.printInfo()
  }
}

// 定义类
// 无参构造器
class Professor {
  // 直接声明属性
  var name: String = _
  var age: Int = _
}

// 上面的定义等价于
class Professor1(var name: String, var age: Int)

// 主构造器参数无修饰，java方式，不推荐，太臃肿
class Professor2(_name: String, _age: Int) {
  var name: String = _name
  var age: Int = _age
}

// val修饰的属性不允许修改
class Professor3(val name: String, val age: Int)

class Professor4(var name: String, var age: Int) {
  var school: String = _

  def this(name: String, age: Int, school: String) {
    this(name, age)
    this.school = school
  }

  def printInfo(): Unit = {
    println(s"Professor4: name : $name, age : $age school : $school")
  }
}



