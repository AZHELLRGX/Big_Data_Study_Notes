package org.azhell.learn.scala.object_oriented

/**
 * 测试scala的继承
 */
object TestInherit {
  def main(args: Array[String]): Unit = {
    val cat: Animal = new Cat("Persian cat", "Cat family", "Fish")
    animalInfo(cat)

    val dog: Animal = new Dog("Husky", "Canine", "Large")
    animalInfo(dog)
  }

  // 动态绑定
  // scala的动态绑定属性和方法都可以动态，而java只能是方法可以动态绑定
  def animalInfo(animal: Animal) {
    animal.printInfo()
  }
}


// 定义一个父类
class Animal() {
  var name: String = _
  var kind: String = _

  println("Animal的主构造器被调用")

  def this(name: String, kind: String) {
    this()
    println("Animal的辅助构造器被调用")
    this.name = name
    this.kind = kind
  }

  def printInfo(): Unit = {
    println(s"Animal: name=$name kind=$kind")
  }
}

// 定义子类，这里的属性不能再使用var声明属性，因为父类已经存在同名参数
// 如果这里的继承写为 extends Animal(name,kind) 则还会调用父类Animal的辅助构造器
class Cat(name: String, kind: String) extends Animal() {
  var food: String = _

  println("Cat的主构造器被调用")

  def this(name: String, age: String, food: String) {
    this(name, age)
    println("Cat的辅助构造器被调用")
    this.food = food
  }

  override def printInfo(): Unit = {
    println(s"Animal: name=$name kind=$kind food=$food")
  }
}

// 测试多态
class Dog(var size: String, name: String, kind: String) extends Animal {

  override def printInfo(): Unit = {
    println(s"Animal: name=$name kind=$kind size=$size")
  }

}

