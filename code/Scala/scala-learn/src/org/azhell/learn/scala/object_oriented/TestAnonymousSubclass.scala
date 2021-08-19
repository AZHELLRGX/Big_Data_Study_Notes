package org.azhell.learn.scala.object_oriented

/**
 * 匿名子类的用法
 */
object TestAnonymousSubclass {
  def main(args: Array[String]): Unit = {
    val refrigerator: Furniture = new Furniture {
      var price: Int = 2100
      override val name:String = "冰箱"

      def sell(): Unit = {
        println(s"卖出一台${brand}品牌的$name，赚取了￥$price")
      }
    }
    refrigerator.purchase()
    refrigerator.sell()
  }
}
