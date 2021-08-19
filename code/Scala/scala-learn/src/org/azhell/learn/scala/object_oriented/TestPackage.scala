


/**
 * 测试scala的包设计原则
 */
// package org.azhell.learn.scala.object_oriented 这段语句可以写成下面的嵌套形式
package org {
  package azhell {
    package learn {

      // 必须进行导包操作

      import org.azhell.learn.scala.object_oriented.TestPackage

      object Outer {
        var out: String = "out"

        def main(args: Array[String]): Unit = {
          println(TestPackage.in)
          TestPackage.in = "inner"
          println(TestPackage.in)
        }
      }
      package scala {
        package object_oriented {

          object TestPackage {
            var in: String = "in"

            def main(args: Array[String]): Unit = {
              println(Outer.out)
              Outer.out = "outer"
              println(Outer.out)
            }
          }

        }

      }

    }

  }

}

// 甚至可以在一个文件中包含多个包的定义


