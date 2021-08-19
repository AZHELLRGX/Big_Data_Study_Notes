package org.azhell.learn.scala.object_oriented

object TestClassForAccess {

}

// 定义一个父类
class Person {
  // 只有自己可以访问
  private val idCard: String = "3523566"
  // 只有当前类以及子类可以访问
  protected var name: String = "alice"
  var sex: String = "female"
  private[object_oriented] var age: Int = 18

  def printInfo(): Unit = {
    println(s"Person: $idCard $name $sex $age")
  }
}
