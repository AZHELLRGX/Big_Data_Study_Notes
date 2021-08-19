package org.azhell.learn.scala.object_oriented

/**
 * scala的自身类型可以实现依赖注入的功能
 */
object TestTraitSelfType {
  def main(args: Array[String]): Unit = {
    val myApp = new MyApp
    myApp.login()
  }
}

class User(val name: String, val age: Int)

trait Dao {
  def insert(user: User): Unit = {
    println("insert into database :" + user.name)
  }
}

trait APP {
  // 实现了类似Spring框架的依赖注入功能
  _: Dao =>
  def login(user: User): Unit = {
    println("login :" + user.name)
    insert(user)
  }
}

class MyApp extends APP with Dao {
  def login(): Unit = {
    login(new User("Alice", 23))
  }
}
