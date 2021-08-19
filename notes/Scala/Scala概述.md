[toc]

### Scala与JVM的关系

![image-20210531141643470](Scala%E6%A6%82%E8%BF%B0.assets/image-20210531141643470.png)

### Scala语言特点

Scala是一门以Java虚拟机（JVM）为运行环境并将***面向对象和函数式编程***的最佳特性结合在一起的静态类型编程语言（静态语言需要提前编译的如：Java、c、c++等，动态语言如：js）。

- Scala是一门多范式的编程语言，Scala支持面向对象和函数式编程。（多范式，就是多种编程方法的意思。有面向过程、面向对象、泛型、函数式四种程序设计方法。）
- Scala源代码（.scala）会被编译成Java字节码（.class），然后运行于JVM之上，并可以调用现有的Java类库，实现两种语言的无缝对接。
- Scala单作为一门语言来看，非常的简洁高效 。
- Scala在设计时，马丁·奥德斯基是参考了Java的设计思想，可以说Scala是源于Java，同时马丁·奥德斯基也加入了自己的思想，将函数式编程语言的特点融合到JAVA中, 因此，对于学习过Java的同学，只要在学习Scala的过程中，搞清楚Scala和Java相同点和不同点，就可以快速的掌握Scala这门语言。

### HelloWorld

#### 代码解释

```scala
/**
 * 小括号表示参数列表：参数声明方式
 *          java 类型 参数名称
 *			Scala 参数名称:类型
 * public修饰符：scala中没有public关键字、如果不声明访问权限、那么就是公共的
 * static修饰符：scala中没有静态语法，所以没有static关键字
 * void关键字：表示返回值，但是不遵循面向对象原则，所以scala使用Unit类型代替
 * scala中：方法名（参数列表）：返回值类型
 * scala中声明方法必须使用def关键字
 * scala的方法采用了将方法实现赋值给方法声明的方式
 * scala是一门完全面向对象的语言，所以不存在静态语法，为了实现静态语法可以采用伴生对象方式调用方法
 */
object HelloScala {
  def main(args: Array[String]): Unit = {
    print("hello,scala")
  }
}
```

#### 代码反编译

![image-20210531143033323](Scala%E6%A6%82%E8%BF%B0.assets/image-20210531143033323.png)

### 变量和数据类型

#### 变量和常量

- 声明变量时，类型可以省略，编译器自动推导，即类型推导
- 类型确定后，就不能修改，说明 Scala 是强数据类型语言。
- 变量声明时，必须要有初始值
- 在声明/定义一个变量时，可以使用 var 或者 val 来修饰，var 修饰的变量可改变，val 修饰的变量不可改。
- ***能用常量的地方不要使用变量***
- var 修饰的对象引用可以改变，val 修饰的对象则不可改变，但对象的状态（值）却是可以改变的。（比如：自定义对象、数组、集合等等）。

下面是scala与java语法实现常量和变量的区别：

```scala
// var 变量名 [: 变量类型] = 初始值 
var i:Int = 10
// val 常量名 [: 常量类型] = 初始值 
val j:Int = 20
```

```java
// 变量类型 变量名称 = 初始值 
int a = 10
// final 常量类型 常量名称 = 初始值 
final int b = 20
```

#### 标识符的命名规范

Scala 对各种变量、方法、函数等命名时使用的字符序列称为标识符。即：凡是自己可以起名字的地方都叫标识符

Scala 中的标识符声明，基本和 Java 是一致的，但是细节上会有所变化，有以下三种规则：

- 以字母或者下划线开头，后接字母、数字、下划线
- 以操作符开头，且只包含操作符（+ - * / # !等）
- 用反引号包括的任意字符串，即使是 Scala 关键字（39 个）也可以

```scala
package, import, class, object, trait, extends, with, type, for
private, protected, abstract, sealed, final, implicit, lazy, override
try, catch, finally, throw
if, else, match, case, do, while, for, return, yield
def, val, var
this, super
new
true, false, null
```

#### 字符串输出

- 字符串，通过+号连接
- printf 用法：字符串，通过%传值。
- 字符串模板（插值字符串）：通过$获取变量值

```scala
/**
 * 测试scala中的字符串常用操作
 */
object TestString {
  def main(args: Array[String]): Unit = {
    // 字符串可以使用 + 号连接，这一点与java一致
    val name: String = "alice"
    val age: Int = 18
    println("name is " + name + ", age is " + age)

    // 使用*，将一个字符串复制多次并拼接
    println((name + ";") * 3)

    // printf用法：字符串，通过%传值
    printf("%d岁的%s在腾讯任职\n", age, name)

    // 字符串模版（插值字符串），通过$获取变量值，类似shell的语法
    // 字符串前面加上s表示这是一个模版字符串，是scala语法之一
    println(s"${age}岁的${name}在腾讯任职")
    // 看来scala真是兼众家语言之长啊

    val num: Double = 2.3456
    // 可以使用f进行 格式化模版字符串，以下表示：数字不足两位保留两位，小数部分保留两位（会自动四舍五入）
    println(f"num is $num%2.2f")
    // raw只会填充变量，不会处理其他的任何可能有意义的字符串
    println(raw"num is $num%2.2f")

    // 三引号表示字符串，保持多行字符串的原格式输出
    val sql =
      s"""
         |select *
         |from
         |  `student`
         |where
         |  `name` = '$name'
         |and
         |  `age` = $age
         |""".stripMargin
    println(sql)
  }
}
```

#### 键盘输入

在编程中，需要接收用户输入的数据，就可以使用键盘输入语句来获取。
基本语法：StdIn.readLine()、StdIn.readShort()、StdIn.readDouble()等

```scala
/**
 * 测试标准输入
 */
object TestStdIn {

  def main(args: Array[String]): Unit = {
    println("请输出您的姓名：")
    val name = StdIn.readLine()
    println("请输入您的年龄：")
    val age = StdIn.readInt()
    println(s"欢迎${age}的${name}来学习scala语言！")
  }
}
```

#### 数据类型

##### 回顾java数据类型

>Java基本类型：char、byte、short、int、long、float、double、boolean
>Java引用类型：（对象类型）
>由于Java有基本类型，而且基本类型不是真正意义的对象，即使后面产生了基本类型的包装类，但是仍然存在基本数据类型，所以Java语言并不是真正意思的面向对象。
>***注意：Java中基本类型和引用类型没有共同的祖先***
>Java基本类型的包装类：Character、Byte、Short、Integer、Long、Float、Double、Boolean

##### scala数据类型

![image-20210531155850952](Scala%E6%A6%82%E8%BF%B0.assets/image-20210531155850952.png)

- Scala中一切数据都是对象，都是Any的子类。
- Scala中数据类型分为两大类：数值类型（AnyVal）、引用类型（AnyRef），不管是值类型还是引用类型都是对象。
- Scala数据类型仍然遵守，低精度的值类型向高精度值类型，自动转换（隐式转换）
- Scala中的StringOps是对Java中的String增强
- Unit：对应Java中的void，用于方法返回值的位置，表示方法没有返回值。Unit是一个数据类型，只有一个对象就是()。Void不是数据类型，只是一个关键字
- Null是一个类型，只有一个对象就是null。它是所有引用类型（AnyRef）的子类。
- Nothing，是所有数据类型的子类，主要用在一个函数没有明确返回值时使用，因为这样我们可以把抛出的返回值，返回给任何的变量或者函数。

##### Unit 类型、Null 类型和 Nothing 类型

![image-20210531165741538](Scala%E6%A6%82%E8%BF%B0.assets/image-20210531165741538.png)

- Unit 类型用来标识过程，也就是没有明确返回值的函数
- Null 类只有一个实例对象，Null 类似于 Java 中的 null 引用。***Null 可以赋值给任意引用类型（AnyRef），但是不能赋值给值类型（AnyVal）***
- Nothing，可以作为没有正常返回值的方法的返回类型，非常直观的告诉你这个方法不会正常返回，而且由于 Nothing 是其他任意类型的子类，他还能跟要求返回值的方法兼容。

##### 数值类型自动转换

当 Scala 程序在进行赋值或者运算时，精度小的类型自动转换为精度大的数值类型，这个就是自动类型转换（隐式转换）。数据类型按精度（容量）大小排序为：

![image-20210531170501310](Scala%E6%A6%82%E8%BF%B0.assets/image-20210531170501310.png)

- 自动提升原则：有多种类型的数据混合运算时，系统首先自动将所有数据转换成
  精度大的那种数据类型，然后再进行计算。
- 把精度大的数值类型赋值给精度小的数值类型时，就会报错，反之就会进行自动
  类型转换。
- （byte，short）和 char 之间不会相互自动转换。
- byte，short，char 他们三者可以计算，在计算时首先转换为 int 类型。

##### 强制类型转换

自动类型转换的逆过程，将精度大的数值类型转换为精度小的数值类型。使用时要加上强制转函数，但可能造成精度降低或溢出，格外要注意。

```java
int num = (int)2.5
```

```scala
var num : Int = 2.7.toInt
```

- 将数据由高精度转换为低精度，就需要使用到强制转换
- 强转符号只针对于最近的操作数有效，往往会使用小括号提升优先级

### 运算符

scala大部分运算符的使用与java一致，与java不用的地方如下：

- 不存在i++ 或者 ++i语法，因为这是一个经常引起混淆的语法
- 另外scala中不存在对于 += 与 -= 等赋值运算符的自动类型转换，如果强行赋值编译期间会报错
- scala中的运算符其实都是函数，可以使用`.+()`等方式调用

```scala
object TestOpt {
    def main(args: Array[String]): Unit = {
        // 标准的加法运算
        val i:Int = 1.+(1)
        // （1）当调用对象的方法时，.可以省略
        val j:Int = 1 + (1)
        // （2）如果函数参数只有一个，或者没有参数，()可以省略
        val k:Int = 1 + 1
        println(1.toString())
        println(1 toString())
        println(1 toString)
    }
}
```

- scala中，规避了java中字符串比较的时候`==`与`equals`方法效果不一致的易混淆规则，如果想要比较两个对象的首地址符可以使用`eq()`方法

### 流程控制

#### 分支控制 if-else

- scala中的if-else语句带有返回值；如果if else代码块返回的类型不一致，可以取他们的共同祖先
- 在Scala中没有Switch，而是使用模式匹配来处理

```scala
/**
 * 测试scala中的流程控制语句中的if else
 */
object TestIfElse {
  def main(args: Array[String]): Unit = {
    /*
     * scala的if else的流程控制语句使用方法与java基本相同
     * 但是存在以下差别
     * 1、scala的if else是有返回值的
     * 2、java中的三元表达式可以使用scala的if else实现
     *
     * 注意：在Scala中没有Switch，而是使用模式匹配来处理
     */

    println("input age")
    val age = StdIn.readInt()
    val res: String = if (age < 18) {
      "童年"
    } else if (age >= 18 && age < 30) {
      "中年"
    } else {
      "老年"
    }
    println(res)

    // 以上代码，如果if else代码块返回的类型不一致，可以取他们的共同祖先
    val resAny: Any = if (age < 18) {
      "童年"
    } else if (age >= 18 && age < 30) {
      "中年"
    } else {
      100
    }
    println(resAny)

    // scala中使用三元表达式
    val resX = if (age < 18) "童年" else "成年"
    println(resX)
  }
}
```

#### For 循环控制

Scala 也为 for 循环这一常见的控制结构提供了非常多的特性，这些 for 循环的特性被称为 for 推导式或 for 表达式

##### 范围数据循环（To）

```scala
// 1、范围遍历，这个to也是一个方法，范围是一个闭区间，即[1,10]
for (i <- 1 to 10) {
    println(i + " hello world")
}
for (i: Int <- 1.to(10)) {
    println(i + " hello world")
}
```

##### 集合遍历

```scala
// 2、集合遍历
for (i <- Array(12, 45, 67)) {
    println(i)
}
```

##### 范围数据循环（Until）

```scala
// 不包含边界则使用until关键字,范围是一个前闭后开区间，即[1,10)
for (i <- 1 until 10) {
	println(i + " hello world")
}
```

##### 循环守卫

```scala
// 3、循环守卫
for (i <- 1 to 3 if i != 2) {
    println(i)
}
```

##### 循环步长

```scala
// 4、循环步长  步长不允许为0，否则抛出非法参数异常
for (i <- 1 to 10 by 2) {
    println(i)
}
for (i <- 30 to 13 by -2) {
    println(i)
}
```

##### 反转输出

```scala
// 反转输出 reverse的底层实现其实也是by
for (i <- 1 to 10 reverse) {
  println(i)
}
```

##### 循环嵌套

```scala
// 5、循环嵌套
for (i <- 1 to 4; j <- 1 to 5) {
    println("i = " + i + " j = " + j)
}

// 6、引入变量
for (i <- 1 to 3; j = 4 - i) {
    println("i = " + i + " j = " + j)
}
// 也可以像下面这样写
for {
    i <- 1 to 3
    j = 4 - i
} {
    println("i = " + i + " j = " + j)
}
```

##### 返回值（yield关键字）

```scala
// for循环的返回值是Unit，可以使用yield关键字来将数据封装为集合返回
val arr = for (i <- 1 to 3) yield i
println(arr)
```

#### While 和 do..While 循环控制

While 和 do..While 的使用和 Java 语言中用法相同。

- 循环条件是返回一个布尔值的表达式
- while 循环是先判断再执行语句
- 与 for 语句不同，while 语句没有返回值，即整个 ***while 语句的结果是 Unit 类型()***
- 因为 while 中没有返回值，所以当要用该语句来计算并返回结果时，就不可避免的使用变量，而变量需要声明在 while 循环的外部，那么就等同于循环的内部对外部的变量造成了影响，所以不推荐使用，而是***推荐使用 for 循环。***

#### 循环中断

Scala 内置控制结构特地去掉了 break 和 continue，是为了更好的适应函数式编程，推荐使用函数式的风格解决break和continue的功能，而不是一个关键字。Scala中使用***breakable控制结构***来实现 break 和 continue 功能。

```scala
import scala.util.control.Breaks._
/**
 * 演示scala中的类似java的循环中断设计
 */
object TestBreaks {
  def main(args: Array[String]): Unit = {
    /*
    在scala中，不存在break与continue关键字，
    是为了更好的函数式编程与面向对象
     */
    // 1、使用异常的方式来实现循环的中断
    try {
      for (i <- 1 to 10) {
        if (i == 3) {
          throw new RuntimeException
        } else {
          println(i)
        }
      }
    } catch {
      case _: RuntimeException => // 不做任何处理
    }

    // 可以使用scala中的Breaks与break()实现循环中断
    breakable(
      for (i <- 1 to 10) {
        if (i == 3) {
          break()
        } else {
          println(i)
        }
      }
    )
    println("正常代码块")
  }
}
```

### 函数式编程

#### 函数基础

面向对象与函数式编程的对比：

- 面向对象编程
  解决问题，分解对象，行为，属性，然后通过对象的关系以及行为的调用来解决问题。

  - 对象：用户

  - 行为：登录、连接 JDBC、读取数据库

  - 属性：用户名、密码

  Scala 语言是一个完全面向对象编程语言。万物皆对象

  对象的本质：对数据和行为的一个封装

- 函数式编程
  解决问题时，将问题分解成一个一个的步骤，将每个步骤进行封装（函数），通过调用这些封装好的步骤，解决问题。

  - 例如：请求->用户名、密码->连接 JDBC->读取数据库

​         Scala 语言是一个完全函数式编程语言。万物皆函数。
​         函数的本质：函数可以当做一个值进行传递

- 在 Scala 中函数式编程和面向对象编程完美融合在一起了。

##### 函数基本语法

![image-20210601093753074](Scala%E6%A6%82%E8%BF%B0.assets/image-20210601093753074.png)

##### 函数与方法的区别

- 为完成某一功能的程序语句的集合，称为函数。
- 类中的函数称之方法。
- Scala 语言可以在任何的语法结构中声明任何的语法
- 函数没有重载和重写的概念；方法可以进行重载和重写
- Scala 中函数可以嵌套定义

```scala
/**
 * 演示scala中函数与对象方法的创建与调用
 */
object TestFunctionAndMethod {
  def main(args: Array[String]): Unit = {
    // 定义函数
    def sayHi(name: String): Unit = {
      println("hi, " + name)
    }
    
    // 函数可以嵌套
    def test(): Unit ={
      def testInner(): Unit ={
        println("嵌套结构函数")
      }
    }

    // 函数没有重写与重载的概念，这里报错
    //    def sayHi(name: String, time: String): String = {
    //      "hello, " + name + ",good " + time
    //    }
    
    // 调用函数
    sayHi("Alice")

    // 调用对象方法
    TestFunctionAndMethod.sayHi("Alice")

    val str = TestFunctionAndMethod.sayHi("Alice", "afternoon")
    println(str)
  }

  // 定义对象方法
  def sayHi(name: String): Unit = {
    println("Hi, " + name)
  }

  // 方法可以进行重载以及重写，例如下面就是一个带有返回值的对象方法，是对sayHi重载
  def sayHi(name: String, time: String): String = {
    "hello, " + name + ",good " + time
  }
}
```

##### 函数参数

- 可变参数
- 如果参数列表中存在多个参数，那么可变参数一般放置在最后
- 参数默认值，一般将有默认值的参数放置在参数列表的后面
- 带名参数

```scala
/**
 * 演示scala的函数参数使用方式
 */
object TestFunctionParameter {
  def main(args: Array[String]): Unit = {
    // 可变参数与java的使用方式完全一致[放置在函数参数列表的最后]，只是语法有些不一致
    def f1(str: String*): Unit = {
      println(str) // 类型为ArraySeq，ps：java里面是一个数组
    }

    f1("Alice")

    // 参数默认值
    def f2(name: String = "Alice"): Unit = {
      println(name)
    }

    // 不传参数就使用默认值
    f2()
    // 传参覆盖默认值
    f2("Bob")

    // 带名参数
    def f3(name: String, age: Int): Unit = {
      println(s"${name}今年已经${age}了")
    }
	
    f3("Alice", 14) // 按照参数序列使用传参
    f3(age = 13, name = "Bob") // 按名赋值

    // 一般建议将带默认值的参数放置在参数列表的最后
    def f4(age: Int = 15, name: String): Unit = {
      println(s"${name}今年已经${age}了")
    }
	// 当然可以按名复制规避覆盖带有默认值参数的情况
    f4(name = "Lucy")
  }
}
```

##### 函数至简原则（值得反复记忆）

函数至简原则是scala一个很重要的设计，学习至简原则不仅可以提高自己的编程效率和代码整洁度，而且可以提高阅读他人代码或者开源组件源码的能力

至简原则如下：

1. return 可以省略，Scala 会使用函数体的最后一行代码作为返回值
2. 如果函数体只有一行代码，可以省略花括号
3. 返回值类型如果能够推断出来，那么可以省略（:和返回值类型一起省略）
4. 但是如果有 return，则不能省略返回值类型，必须指定
5. 如果函数明确声明 unit，那么即使函数体中使用 return 关键字也不起作用
6. Scala 如果期望是无返回值类型，可以省略等号
7. 如果函数无参，但是声明了参数列表，那么调用时，小括号，可加可不加
8. 如果函数没有参数列表，那么小括号可以省略，调用时小括号必须省略
9. 如果不关心名称，只关心逻辑处理，那么函数名（def）可以省略

```scala
object TestFunction {
    def main(args: Array[String]): Unit = {
        // （0）函数标准写法
        def f( s : String ): String = {
        	return s + " jinlian"
        }
        println(f("Hello"))
        // 至简原则:能省则省
        
        //（1） return 可以省略,Scala 会使用函数体的最后一行代码作为返回值
        def f1( s : String ): String = {
        	s + " jinlian"
        }
        println(f1("Hello"))
        
        //（2）如果函数体只有一行代码，可以省略花括号
        def f2(s:String):String = s + " jinlian"
        
        //（3）返回值类型如果能够推断出来，那么可以省略（:和返回值类型一起省略）
        def f3( s : String ) = s + " jinlian"
        println(f3("Hello3"))
        
        //（4）如果有 return关键字，则不能省略返回值类型，必须指定。
        def f4() :String = {
        	return "ximenqing4"
        }
        println(f4())
        
        //（5）如果函数明确声明 unit，那么即使函数体中使用 return 关键字也不起作用，但是不会像java一样编译期间报错
        def f5(): Unit = {
        	return "dalang5"
        }
        println(f5())
        
        //（6）Scala 如果期望是无返回值类型,可以省略等号
        // 将无返回值的函数称之为过程
        def f6() {
        	"dalang6"
        }
        println(f6())
        
        //（7）如果函数无参，但是声明了参数列表，那么调用时，小括号，可加可不加
        def f7() = "dalang7"
        println(f7())
        println(f7)
        
        //（8）如果函数没有参数列表，那么小括号可以省略,调用时小括号也必须省略
        def f8 = "dalang"
        //println(f8())  // 函数不带括号，调用的时候也不能带括号
        println(f8)
        
        //（9）如果不关心名称，只关心逻辑处理，那么函数名（def）可以省略
        def f9 = (x:String)=>{println("wusong")}
        def f10(f:String=>Unit) = {
        	f("")
        }
        f10(f9)
        // 调用f10传入了一个函数作为参数
        println(f10((x:String)=>{println("wusong")}))
    }
}
```

#### 函数高级

##### 高阶函数

- 函数可以作为值进行传递

```scala
def f(num: Int): Int = {
    println("函数f被调用")
    num
}

// 1、接收函数的执行结果
val r = f(123)
println(r)

// 2、将函数作为值进行传递
val f1: Int => Int = f // 指定变量为函数类型
val f2 = f _ // 不指定变量类型，而使用`函数名 + 下划线`的方式指明这是一个函数，如果不写下划线，则无法判断这是将函数运行结果还是函数本身作为值赋给变量
println(f1)
println(f1(12))
println(f2)
println(f2(35))

// 针对无参函数
def fun(): Int = {
    println("函数f[无参]被调用")
    1
}

val v1 = fun // 虽然不报错，但是不推荐这样写，推荐下面的写法
val v2 = fun()
println(v1)
println(v2)

val f3: () => Int = fun  // 注意这里无参函数的类型，参数列表不存在就写为`()`
val f4 = fun _ // 这里需要注意的是如果方法重载了，这种方式就不好用了
println(f3)
println(f4)
```

- 函数可以作为参数进行传递

```scala
// 3、函数作为参数进行传递
def dualEval(opt: (Int, Int) => Int, a: Int, b: Int): Int = {
    opt(a, b)
}
// add作为参数传递的时候，它的类型是：(Int, Int) => Int
def add(a: Int, b: Int): Int = {
    a + b
}
// 因为这里add的类型可以自动推导出来，所以无需在后面写上下划线
println(dualEval(add, 12, 13))
// 这里使用到了函数至简原则9 以及匿名函数至简原则1
println(dualEval((a, b) => a + b, 12, 13))
// 这里则使用到了匿名函数至简原则4
println(dualEval(_ * _, 12, 3))
```

- 函数可以作为函数返回值返回

```scala
// 4、函数作为函数的返回值进行返回
def f5(): Int => Unit = {
    def f6(num: Int): Unit = {
        println("函数f6被调用" + num)
    }
    f6
}
val f6 = f5()
println(f6) // org.azhell.learn.scala.functional.TestHighOrderFunction$$$Lambda$10/846063400@25618e91
println(f6(23)) // 打印出来是`()`，因为f6的返回值是Unit
```

##### 匿名函数

匿名函数的定义：没有名字的函数就是匿名函数。
匿名函数的形式：(x:Int)=>{函数体}
解释说明：x：表示输入参数类型；Int：表示输入参数类型；函数体：表示具体代码逻辑

> 传递匿名函数至简原则：
> （1）参数的类型可以省略，会根据形参进行自动的推导
> （2）类型省略之后，发现只有一个参数，则圆括号可以省略；其他情况：没有参数和参数超过 1 的永远不能省略圆括号。
> （3）匿名函数如果只有一行，则大括号也可以省略
> （4）如果参数只出现一次，则参数省略且后面参数可以用_代替

```scala
def main(args: Array[String]): Unit = {
    // （1）定义一个函数：参数包含数据和逻辑函数
    def operation(arr: Array[Int], op: Int => Int) = {
    	for (elem <- arr) yield op(elem)  // 使用yield关键字
    }
    // （2）定义逻辑函数
    def op(ele: Int): Int = {
    	ele + 1
    }
    // （3）标准函数调用
    val arr = operation(Array(1, 2, 3, 4), op)
    println(arr.mkString(","))
    // （4）采用匿名函数
    val arr1 = operation(Array(1, 2, 3, 4), (ele: Int) => {
        ele + 1
	})
    println(arr1.mkString(","))
    
    // （4.1）参数的类型可以省略，会根据形参进行自动的推导;
    val arr2 = operation(Array(1, 2, 3, 4), (ele) => {
    	ele + 1
    })
    println(arr2.mkString(","))
    
    // （4.2）类型省略之后，发现只有一个参数，则圆括号可以省略；其他情况：没有参数和参数超过 1 的永远不能省略圆括号。
    val arr3 = operation(Array(1, 2, 3, 4), ele => {
    	ele + 1
    })
    println(arr3.mkString(","))
    
    // (4.3) 匿名函数如果只有一行，则大括号也可以省略
    val arr4 = operation(Array(1, 2, 3, 4), ele => ele + 1)
    println(arr4.mkString(","))
    
    //（4.4）如果参数只出现一次，则参数省略且后面参数可以用_代替
    val arr5 = operation(Array(1, 2, 3, 4), _ + 1)
    println(arr5.mkString(","))
}
```
扩展，当匿名函数有两个参数的时候：
```scala
object TestFunction {
    def main(args: Array[String]): Unit = {
    def calculator(a: Int, b: Int, op: (Int, Int) => Int): Int= {
    	op(a, b)
    }
    // （1）标准版
    println(calculator(2, 3, (x: Int, y: Int) => {x + y}))
        
    // （2）如果只有一行，则大括号也可以省略
    println(calculator(2, 3, (x: Int, y: Int) => x + y))
        
    // （3）参数的类型可以省略，会根据形参进行自动的推导;
    println(calculator(2, 3, (x , y) => x + y))
        
    // （4）如果参数只出现一次，则参数省略且后面参数可以用_代替
    println(calculator(2, 3, _ + _))
    }
}
```

匿名函数练习：

> 定义一个匿名函数，并将它作为值赋给变量 fun。函数有三个参数，类型分别为 Int，String，Char，返回值类型为 Boolean。
> 要求调用函数 fun(0, “”, ‘0’)得到返回值为 false，其它情况均返回 true。

```scala
// 将匿名函数赋值传递给变量fun
val fun = (i: Int, s: String, c: Char) => {
    if (i == 0 && s == "" && c == '0')
    	false
    else
    	true
}
println(fun(0, "", '0'))
println(fun(1, "", '0'))
println(fun(0, "hello", '0'))
println(fun(0, "", '1'))
```

> false
> true
> true
> true

##### 多阶函数

前文提到函数可以作为另外一个函数的返回值，然后这个返回值可以继续作为一个函数被调用，那么在此基础上我们可以完成多阶函数

>定义一个函数 func，它接收一个 Int 类型的参数，返回一个函数（记作 f1）。
>它返回的函数 f1，接收一个 String 类型的参数，同样返回一个函数（记作 f2）。函数 f2 接收一个 Char 类型的参数，返回一个 Boolean 的值。
>要求调用函数 func(0) (“”) (‘0’)得到返回值为 false，其它情况均返回 true。

```scala
// 多阶函数的使用
def func(i: Int): String => Char => Boolean = {
    def func1(s: String): Char => Boolean = {
        def func2(c: Char): Boolean = {
            if (i == 0 && s == "" && c == '0')
            	false
            else
            	true
        }
		
        // 作为函数func1的返回值返回
        func2
    }

    // 作为函数func的返回值返回
    func1
}
// 多阶函数的调用方式
println(func(0)("")('0'))
println(func(1)("")('0'))
println(func(0)("hello")('0'))
println(func(0)("")('1'))

// 上面的方法可以使用匿名函数原则进行简化
// 匿名函数简化
/*
     * 简化原则
     * 1、内部函数的匿名化
     * 2、因为最外层指定了输入参数，所以参数类型可以省略
     * 3、单行代码，去除大括号
     *
     */
def func3(i: Int): String => Char => Boolean = {
    s =>
        c =>
            if (i == 0 && s == "" && c == '0')
                false
            else
                true
}

println(func3(0)("")('0'))
println(func3(1)("")('0'))
println(func3(0)("hello")('0'))
println(func3(0)("")('1'))
```

##### 函数柯里化&闭包

***闭包***：如果一个函数，访问到了它的外部（局部）变量的值，那么这个函数和他所处的环境，称为闭包
***函数柯里化***：把一个参数列表的多个参数，变成多个参数列表。

在上面的多阶函数中，可以看到本来应该在外层函数执行完成就释放掉的参数`i`和`s`并没有随着栈空间释放掉，而是包含在了最内层函数内部，形成了闭包的效果。

柯里化就是对闭包的简单化，将复杂的逻辑改造的简单易懂，所以可以说函数柯里化的时候一定存在闭包，对前面多阶函数的柯里化：

```scala
// 柯里化
def func4(i: Int)(s: String)(c: Char): Boolean = {
    if (i == 0 && s == "" && c == '0')
    	false
    else
    	true
}
// 函数柯里化以后，调用方式并未改变
println(func4(0)("")('0'))
println(func4(1)("")('0'))
println(func4(0)("hello")('0'))
println(func4(0)("")('1'))
```

##### 递归

定义：一个函数/方法在函数/方法体内又调用了本身，我们称之为递归调用

>递归其实可以完成所有循环操作
>递归对程序员更加友好，但是对于机器来讲却不是好事，栈帧就是个大问题，JVM可能因为递归导致StackOverFlow
>所以需要进行尾递归优化：尾递归就是指某个函数在最后一步调用自身，而且不能存在额外的操作：其实可以理解为将上一个结果当作参数传给下一次递归，这个时候上一步计算的栈帧就可以释放了，不会导致StackOverFlow
>尾递归的支持得看具体语言的编译器

```scala
// 循环方式实现阶乘
def factorial(n: Int): Int = {
    var result: Int = 1
    for (i <- 1 to n) {
        result *= i
    }
    result
}

// 递归方式实现阶乘
def factorialByRecursion(n: Int): Int = {
    if (n == 0) 1
    else {
        // 存在额外操作，所以不是尾递归
        factorialByRecursion(n - 1) * n
    }
}

// 尾递归优化
def factorialByTailRecursion(n: Int): Int = {
    @scala.annotation.tailrec // 这个注解确保编译器会检查是否是尾递归的，如果不是编译器会报错
    def loop(n: Int, currRes: Int): Int = {
        if (n == 0) 
        	currRes
        else 
        	loop(n - 1, currRes * n) // 不存在额外操作，而且在函数的最后一步，标准的尾递归
    }

    loop(n, 1)
}
```

##### 控制抽象

Java只存在传值调用，而scala既支持传值调用，也支持传名调用，传名调用的时候，代码块只有返回值，而没有参数，所以代码块的类型一般写为`=> Int`，如果代码块没有返回值，则写为`=> Unit`

```scala
/**
 * 演示scala的控制抽象
 */
object TestControlAbstraction {
  def main(args: Array[String]): Unit = {
    // 1、传值参数
    def f1(): Int = {
      println("函数f1被调用")
      12
    }

    def f2(a: Int): Unit = {
      println(s"函数f2被调用,param is $a")
    }
	// 将f1的执行结果传递给了f2
    f2(f1())

    // 2、传名参数，=> Int指定了代码块a的返回类型
    def f3(a: => Int): Unit = {
      println(s"a => $a") // 调用第一次
      println(s"a => $a") // 调用第二次
    }
	// 将f1的执行过程传递给f3
    f3(f1())
    f3(23) // 等同于 f3({23})
    // 传递一个匿名代码块
    f3({
      println("代码块执行")
      24
    })
  }
}
```

>函数f1被调用
>函数f2被调用,param is 12
>函数f1被调用
>a => 12
>函数f1被调用
>a => 12
>a => 23
>a => 23
>代码块执行
>a => 24
>代码块执行
>a => 24

案例实操：实现一个自己的While循环逻辑

```scala
/**
 * 使用控制抽象完成自定义while
 * 这里不是太明白，还是等scala使用了一段时间再搞吧
 */
object PracticeCustomWhile {

  def main(args: Array[String]): Unit = {
    var n = 10

    // 1. 常规的while循环
    while (n >= 1) {
      println(n)
      n -= 1
    }

    // 2. 用闭包实现一个函数，将代码块作为参数传入，递归调用
    def myWhile(condition: => Boolean): (=> Unit) => Unit = {
      // 内层函数需要递归调用，参数就是循环体
      def doLoop(op: => Unit): Unit = {
        if (condition) {
          op
          myWhile(condition)(op)
        }
      }

      doLoop _
    }

    println("=================")
    n = 10
    myWhile(n >= 1) {
      println(n)
      n -= 1
    }

    // 3. 用匿名函数实现
    def myWhile2(condition: => Boolean): (=> Unit) => Unit = {
      // 内层函数需要递归调用，参数就是循环体
      op => {
        if (condition) {
          op
          myWhile2(condition)(op)
        }
      }
    }

    println("=================")
    n = 10
    myWhile2(n >= 1) {
      println(n)
      n -= 1
    }

    // 3. 用柯里化实现
    def myWhile3(condition: => Boolean)(op: => Unit): Unit = {
      if (condition) {
        op
        myWhile3(condition)(op)
      }
    }

    println("=================")
    n = 10
    myWhile3(n >= 1) {
      println(n)
      n -= 1
    }
  }
}
```

##### 惰性加载

当函数返回值被声明为 lazy 时，函数的执行将被推迟，直到我们首次对此取值，该函数才会执行。这种函数我们称之为惰性函数。

```scala
/**
 * 演示scala的懒加载
 */
object TestLazy {
  def main(args: Array[String]): Unit = {
    lazy val result: Int = sum(1, 2)
    val a: Int = sum(1, 3)
    println(s"a = $a")
    println(s"result = $result")
    println(s"result = $result")
  }

  def sum(a: Int, b: Int): Int = {
    println("sum函数被调用了")
    a + b
  }
}
```

### 面向对象

#### Scala包

scala包的应用与java基本一致

##### 包说明（包语句）

- Scala 有两种包的管理风格，一种方式和 Java 的包管理风格相同，每个源文件一个包（包名和源文件所在路径不要求必须一致），包名用“.”进行分隔以表示包的层级关系，如`com.atguigu.scala`
- 通过嵌套的风格表示层级关系；这种风格有以下特点：
  - 一个源文件中可以声明多个 package
  - 子包中的类可以直接访问父包中的内容，而无需导包

```scala
package com{
    package atguigu{
        package scala{
        }
    }
}
```

```scala
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
```

##### 包对象

在 Scala 中可以为每个包定义一个同名的包对象，定义在包对象中的成员，作为其对应包下所有 class 和 object 的共享变量，可以被直接访问。所有的包对象的文件名称都是`Package`，但是包对象的命名却应该使用包的名字，例如下面的`object_oriented`所在包应该就是叫做`object_oriented`

```scala
/**
 * scala可以定义包对象，如下object即为一个包对象
 * 包对象的定义必须和包是同级的才能被包下面的其他对象访问到
 */
package object object_oriented {
  // 可以定义一些当前包共享的属性和方法
  val commonValue = "天猫"
  def commonMethod(): Unit ={
    println(s"${commonValue}是一家大型电商平台")
  }
}

/**
 * 测试访问包对象的公共属性与方法
 */
object TestPackageObject {
  def main(args: Array[String]): Unit = {
    commonMethod()
  }
}
```

##### 导包说明

- 和 Java 一样，可以在顶部使用 import 导入，在这个文件中的所有类都可以使用。
- 局部导入：什么时候使用，什么时候导入。在其作用范围内都可以使用
- 通配符导入：`import java.util._`
- 给类起名：`import java.util.{ArrayList=>JL}`
- 导入相同包的多个类：`import java.util.{HashSet, ArrayList}`
- 屏蔽类：`import java.util.{ArrayList =>_,_}`
- 导入包的绝对路径：`new _root_.java.util.HashMap`

![image-20210601201614730](Scala%E6%A6%82%E8%BF%B0.assets/image-20210601201614730.png)

Scala中存在三个默认的导入，分别是

- import java.lang._
- import scala._
- import scala.Predef._

#### 类和对象

类：可以看成一个模板；对象：表示具体的事物

##### 定义类

在java中，如果类是public的，就必须和文件名称一致，一个.java文件只允许存在一个public对象，但是scala中没有public、一个.scala文件可以写多个类

- Scala 语法中，类并不声明为 public，所有这些类都具有公有可见性（即默认就是public）
- 一个 Scala 源文件可以包含多个类

```scala
//（1）Scala 语法中，类并不声明为 public，所有这些类都具有公有可见性（即默认就是 public）
class Person {
}
//（2）一个 Scala 源文件可以包含多个类
class Teacher{
}
```

##### 属性

**`[修饰符] var|val 属性名称 [：类型] = 属性值`**
注：Bean 属性（`@BeanPropetry`），可以自动生成规范的 setXxx/getXxx 方法

```scala
class Person {
    var name: String = "bobo" //定义属性
    var age: Int = _ // _表示给属性一个默认值
    //Bean 属性（@BeanProperty）
    @BeanProperty 
    var sex: String = "男"
    //val 修饰的属性不能赋默认值，必须显示指定
}
object Person {
    def main(args: Array[String]): Unit = {
        var person = new Person()
        println(person.name)
        person.setSex("女")
        println(person.getSex)
    }
}
```

####  封装

封装就是把抽象出的数据和对数据的操作封装在一起，数据被保护在内部，程序的其它部分只有通过被授权的操作（成员方法），才能对数据进行操作。Java 封装操作如下，

- 将属性进行私有化

- 提供一个公共的 set 方法，用于对属性赋值

- 提供一个公共的 get 方法，用于获取属性的值

Scala 中的 public 属性，底层实际为 private，并通过 get 方法（`obj.field()`）和 set 方法（`obj.field_=(value)`）对其进行操作。所以 Scala 并不推荐将属性设为 private，再为其设置public 的 get 和 set 方法的做法。但由于很多 Java 框架都利用反射调用 getXXX 和 setXXX 方法，有时候为了和这些框架兼容，也会为 Scala 的属性设置 getXXX 和 setXXX 方法（通过`@BeanProperty` 注解实现）。

##### 访问权限

在 Java 中，访问权限分为：public，private，protected 和默认。在 Scala 中，你可以通过类似的修饰符达到同样的效果。但是使用上有区别。

- Scala 中属性和方法的默认访问权限为 public，***但 Scala 中无 public 关键字***。
- `private` 为私有权限，只在类的内部和伴生对象中可用。
- `protected` 为受保护权限，Scala 中受保护权限比 Java 中更严格，同类、子类可以访问，同包无法访问。
- `private[包名]`增加包访问权限，包名下的其他类也可以使用

```scala
object TestClassForAccess {

}

// 定义一个父类
class Person {
  // 只有自己可以访问
  private val idCard: String = "3523566"
  // 只有当前类以及子类可以访问
  protected var name: String = "alice"
  var sex: String = "female"
  // 包object_oriented内部的其他类都可以访问到这个属性
  private[object_oriented] var age: Int = 18

  def printInfo(): Unit = {
    println(s"Person: $idCard $name $sex $age")
  }
}
```

##### 方法

类内部的函数就是类的方法

##### 创建对象

- val 修饰对象，不能改变对象的引用（即：内存地址），可以改变对象属性的值。
- var 修饰对象，可以修改对象的引用和修改对象的属性值
- 自动推导变量类型不能多态，所以多态需要显示声明

##### 构造器

基本语法

```scala
class 类名(形参列表) { // 主构造器
    // 类体
    def this(形参列表) { // 辅助构造器
    }
    def this(形参列表) { //辅助构造器可以有多个...
    }
}
```

- 辅助构造器，函数的名称 this，可以有多个，编译器通过参数的个数及类型来区分。
- 辅助构造方法不能直接构建对象，***必须直接或者间接调用主构造方法***。
- 构造器调用其他另外的构造器，要求被调用构造器必须提前声明。
- 如果主构造器无参数，小括号可省略，构建对象时调用的构造方法的小括号也可以省略。

```scala
object TestConstructor {
  def main(args: Array[String]): Unit = {
    var teacher: Teacher = new Teacher()
    teacher.Teacher()

    teacher = new Teacher("Lucy")
    teacher = new Teacher("Allen", 26)
  }
}

// 定义一个类，其实下面的类声明就是一个主构造器
class Teacher() { // 主构造器无参数可以将小括号省略
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
```

##### 构造器参数

Scala 类的***主构造器函数***的形参包括三种类型：未用任何修饰、var 修饰、val 修饰

- 未用任何修饰符修饰，这个参数就是一个局部变量
- var 修饰参数，作为类的成员属性使用，可以修改
- val 修饰参数，作为类只读属性使用，不能修改

```scala
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

// 上面的定义等价于,但是创建对象的时候不能等价
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
    this(name, age)  // 在辅助构造器调用主构造器
    this.school = school
  }

  def printInfo(): Unit = {
    println(s"Professor4: name : $name, age : $age school : $school")
  }
}
```

#### 继承与多态

`class 子类名 extends 父类名 { 类体 }`

- 子类继承父类的属性和方法
- scala 是单继承
- 继承的调用顺序：***父类构造器->子类构造器***
- 动态绑定：scala的动态绑定属性和方法都可以动态，而java只能是方法可以动态绑定

```scala
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
  // 重写方法需要加上override关键字
  override def printInfo(): Unit = {
    println(s"Animal: name=$name kind=$kind food=$food")
  }
}

// 测试多态
class Dog(var size: String, name: String, kind: String) extends Animal {
  // 重写方法需要加上override关键字
  override def printInfo(): Unit = {
    println(s"Animal: name=$name kind=$kind size=$size")
  }

}
```

演示Scala中的动态绑定

```scala
class Person {
    val name: String = "person"
    def hello(): Unit = {
        println("hello person")
    }
}
class Teacher extends Person {
    // 重写属性
    override val name: String = "teacher"
    // 重写方法
    override def hello(): Unit = {
    	println("hello teacher")
    }
}
object Test {
    def main(args: Array[String]): Unit = {
        val teacher: Teacher = new Teacher()
        println(teacher.name)
        teacher.hello()
        val teacher1: Person = new Teacher
        println(teacher1.name)
        teacher1.hello()
    }
}
```

```java
class Person {
    public String name = "person";
    public void hello() {
    	System.out.println("hello person");
    }
}
class Teacher extends Person {
    public String name = "teacher";
    @Override
    public void hello() {
        System.out.println("hello teacher");
    }
}
public class TestDynamic {
    public static void main(String[] args) {
        Teacher teacher = new Teacher();
        Person teacher1 = new Teacher();
        System.out.println(teacher.name);
        teacher.hello();
        System.out.println(teacher1.name);
        teacher1.hello();
    }
}
```

![image-20210602153619925](Scala%E6%A6%82%E8%BF%B0.assets/image-20210602153619925.png)

#### 抽象类

##### 抽象属性和抽象方法

基本语法：

- 定义抽象类：`abstract class Person{}` //通过 abstract 关键字标记抽象类
- 定义抽象属性：`val|var name:String` //一个属性没有初始化，就是抽象属性
- 定义抽象方法：`def hello():String` //只声明而没有实现的方法，就是抽象方法

```scala
abstract class Furniture {
  // 非抽象属性
  val name: String = "Furniture"
  var brand: String = "Haier"

  // 抽象属性
  var price: Int

  // 非抽象方法
  def purchase(): Unit = {
    println(s"买入了${brand}品牌的${name}的家具")
  }

  // 抽象方法
  def sell(): Unit
}
```

继承&重写：

- 如果父类为抽象类，那么子类需要将抽象的属性和方法实现，否则子类也需声明为抽象类
- 重写非抽象方法需要用 `override` 修饰，重写抽象方法则可以不加 `override`。
- 子类中调用父类的方法使用 `super `关键字
- 子类对抽象属性进行实现，父类抽象属性可以用 var 修饰；子类对非抽象属性重写，父类非抽象属性只支持 val 类型，而不支持 var：因为 var 修饰的为可变变量，子类继承之后就可以直接使用，没有必要重写

##### 匿名子类

和 Java 一样，可以通过包含带有定义或重写的代码块的方式创建一个匿名的子类

```scala
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
```

#### 单例对象（伴生对象）

Scala语言是完全面向对象的语言，所以并没有静态的操作（即在Scala中没有静态的概念）。但是为了能够和Java语言交互（因为Java中有静态概念），就产生了一种特殊的对象来模拟类对象，该对象为单例对象。若单例对象名与类名一致，则称该单例对象这个类的伴生对象，这个类的所有“静态”内容都可以放置在它的伴生对象中声明。

##### 单例对象用法

```scala
object Person{
	val country:String="China"
}
```

- 单例对象采用 object 关键字声明。
- 单例对象对应的类称之为伴生类，伴生对象的名称应该和伴生类名一致。
- 单例对象中的属性和方法都可以通过伴生对象名（类名）直接调用访问。

```scala
object TestObject {
  def main(args: Array[String]): Unit = {
    // val tiger:Tiger = new Tiger("胖虎")  // 私有化构造器以后不能访问类的私有构造器
    var tiger: Tiger = Tiger.newTiger("胖虎")
    tiger.printInfo()

    // apply方法
    tiger = Tiger("胖虎")
    tiger.printInfo()
  }
}

// 定义类
class Tiger private(var name: String) {
  def printInfo(): Unit = {
    // 使用伴生对象的常量【等同于java的类静态属性】
    println(s"$name is a ${Tiger.kind}")
  }
}

// 类的伴生对象
object Tiger {
  val kind: String = "Siberian Tiger"

  // 创建对象的工厂方法，伴生对象可以方法类的私有构造器
  def newTiger(name: String): Tiger = new Tiger(name)

  // 伴生对象的一个特殊方法，与上述方法达到的效果一致，scala底层存在一个优惠政策，使用的时候可以省略apply
  def apply(name: String): Tiger = new Tiger(name)
}
```

##### apply 方法

- 通过伴生对象的 apply 方法，实现不使用 new 方法创建对象。
- 如果想让主构造器变成私有的，可以在()之前加上 private。
- apply 方法可以重载。
- Scala 中 obj(arg)的语句实际是在调用该对象的 apply 方法，即 obj.apply(arg)。用以统一面向对象编程和函数式编程的风格。
- 当使用 new 关键字构建对象时，调用的其实是类的构造方法，当直接使用类名构建对象时，调用的其实时伴生对象的 apply 方法。

演示示例在上面的代码块已经涉及

#### 特质

- Scala 语言中，采用特质 trait（特征）来代替接口的概念，也就是说，多个类具有相同的特质（特征）时，就可以将这个特质（特征）独立出来，采用关键字 trait 声明。

- Scala 中的 trait 中即可以有抽象属性和方法，也可以有具体的属性和方法，一个类可以混入（ mixin）多个特质。这种感觉类似于 Java 中的抽象类。

- Scala 引入 trait 特征，第一可以替代 Java 的接口，第二个也是对单继承机制的一种补充。

##### 特质声明
从特质的字节码可以看出，scala的特质=抽象类+接口

```scala
trait 特质名 {
	// trait的主体
}
```

特质示例

```scala
trait PersonTrait {
    // 声明属性
    var name:String = _
    // 声明方法
    def eat():Unit={
        // 有方法体
    }
    // 抽象属性
    var age:Int
    // 抽象方法
    def say():Unit
}
```

特质的用法示例

```scala
object TestTrait {
  def main(args: Array[String]): Unit = {
    val goshawk: Goshawk = new Goshawk
    goshawk.eat()
    goshawk.printInfo()
  }
}

// 猛禽特质
trait Raptor {
  // 鹰形目、隼形目和鸮形目
  var forms: String
  // 除了秃鹫是食腐性
  var food: String = "掠食性"

  def fly(): Unit = {
    println("猛禽都善于飞翔")
  }

  // 猛禽的具体食谱不同
  def eat(): Unit
}

// Animal的实现在前文的例子中可以找到
class Goshawk extends Animal with Raptor {
  override var forms: String = "鹰形目"
  name = "苍鹰"
  kind = "中小型猛禽"

  override def eat(): Unit = {
    println(s"苍鹰是${food}猛禽，主要以森林鼠类、野兔、雉类、榛鸡、鸠鸽类和其他小型鸟类为食")
  }

  // 重写父类方法
  override def printInfo(): Unit = {
    println(s"${name}是${food}的$kind，属于${forms}")
  }
}
```

##### 特质基本语法（特质混入）

一个类具有某种特质（特征），就意味着这个类满足了这个特质（特征）的所有要素，所以在使用时，也采用了 extends 关键字，如果有多个特质或存在父类，那么需要采用 with关键字连接。

- 类和特质的关系：使用继承的关系。
- 当一个类去继承特质时，第一个连接词是 extends，后面是 with。
- 如果一个类在同时继承特质和父类时，应当把父类写在 extends 后。
- 特质可以同时拥有抽象方法和具体方法
- 一个类可以混入（mixin）多个特质
- 所有的 Java 接口都可以当做 Scala 特质使用
- 动态混入：可灵活的扩展类的功能
  - 动态混入：创建对象时混入 trait，而无需使类混入该 trait
  - 如果混入的 trait 中有未实现的方法，则需要实现

```scala
/**
 * 特质混入
 */
object TestTraitMixin {
  def main(args: Array[String]): Unit = {
    val vulture = new Vulture
    vulture.eat()
    vulture.printInfo()

    // 动态混入特质
    val goshawk = new Goshawk with Wing{
      override var wingSpan: Float = 1.3F

      override def maximumWingspan: Float = 1.5F

      override def printInfo(): Unit = {
        println(s"${name}是${food}的$kind，属于$forms,翼展一般为${wingSpan}m")
      }
    }

    println(goshawk.maximumWingspan)
    goshawk.printInfo()
  }
}

// 翅膀特质
trait Wing {
  // 翼展
  var wingSpan: Float

  // 最大翼展
  def maximumWingspan: Float
}

class Vulture extends Animal with Raptor with Wing {
  override var forms: String = "鹰形目"
  name = "秃鹫"
  kind = "大型猛禽"
  food = "食腐性"
  override var wingSpan: Float = 2F

  override def eat(): Unit = {
    println(s"秃鹫是${food}猛禽，主要以哺乳动物的尸体为食")
  }

  // 重写父类方法
  override def printInfo(): Unit = {
    println(s"${name}是${food}的$kind，属于$forms,翼展一般为${wingSpan}m,最大翼展可达" + maximumWingspan + "m")
  }

  override def maximumWingspan: Float = 2.5F
}
```

##### 特质叠加

由于一个类可以混入（mixin）多个 trait，且 trait 中可以有具体的属性和方法，若混入的特质中具有相同的方法（方法名，参数列表，返回值均相同），必然会出现继承冲突问题。冲突分为以下两种：

第一种，一个类（Sub）混入的两个 trait（TraitA，TraitB）中具有相同的具体方法，且两个 trait 之间没有任何关系，解决这类冲突问题，直接在类（Sub）中重写冲突方法。

![image-20210602173735978](Scala%E6%A6%82%E8%BF%B0.assets/image-20210602173735978.png)

第二种，一个类（Sub）混入的两个 trait（TraitA，TraitB）中具有相同的具体方法，且两个 trait 继承自相同的 trait（TraitC），及所谓的“钻石问题”，解决这类冲突问题，Scala采用了***特质叠加***的策略。

![image-20210602173741015](Scala%E6%A6%82%E8%BF%B0.assets/image-20210602173741015.png)

```scala
object TestTraitOverlying {
  def main(args: Array[String]): Unit = {
    val myBall = new MyBall
    println(myBall.describe())
  }
}

trait Ball {
  def describe(): String = {
    "ball"
  }
}

trait Color extends Ball {
  override def describe(): String = {
    "blue-" + super.describe()
  }
}

trait Category extends Ball {
  override def describe(): String = {
    "foot-" + super.describe()
  }
}

class MyBall extends Category with Color {
  override def describe(): String = {
    // 输出结果：my ball is a blue-foot-ball
    // 从输出结果可以看出，叠加顺序是从右往左叠加的
    "my ball is a " + super.describe() + ";\n" +
      // 如果想要调用某个指定的混入特质中的方法，可以增加约束：super[]
      "my ball is a " + super[Category].describe()
  }
}
```

>my ball is a blue-foot-ball;
>my ball is a foot-ball

##### 特质自身类型

自身类型可实现依赖注入的功能

```scala
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
```

##### 特质和抽象类的区别
- 优先使用特质。一个类扩展多个特质是很方便的，但却只能扩展一个抽象类。
- 如果你需要构造函数参数，使用抽象类。因为抽象类可以定义带参数的构造函数，而特质不行（有无参构造）。

#### 扩展

##### 类型检查和转换

- obj.isInstanceOf[T]：判断 obj 是不是 T 类型。
- obj.asInstanceOf[T]：将 obj 强转成 T 类型。
- classOf 获取对象的类名。

```scala
class Person{
}
object Person {
    def main(args: Array[String]): Unit = {
    val person = new Person
    //（1）判断对象是否为某个类型的实例
    val bool: Boolean = person.isInstanceOf[Person]
    if ( bool ) {
        //（2）将对象转换为某个类型的实例
        val p1: Person = person.asInstanceOf[Person]
        println(p1)
    }
    //（3）获取类的信息
    val pClass: Class[Person] = classOf[Person]
    	println(pClass)
    }
}
```

##### 枚举类和应用类

- 枚举类：需要继承 Enumeration
- 应用类：需要继承 App

```scala
object TestEnumeration {
  def main(args: Array[String]): Unit = {
    println(WorkDay.MONDAY)
  }
}

// 定义枚举对象
object WorkDay extends Enumeration {
  val MONDAY: WorkDay.Value = Value(1, "周一")
  val TUESDAY: WorkDay.Value = Value(2, "周二")
}

// 定义应用类
object TestApp extends App {
  // 相当于自带main方法
  println("app Start...")
}
```

##### Type 定义新类型

使用 type 关键字可以定义新的数据数据类型名称，本质上就是类型的一个别名

```scala
// 类型别名
type MyString = String
val a: MyString = "abc"
println(a)
```

### 集合

#### 集合简介

- Scala 的集合有三大类：***序列 Seq、集 Set、映射 Map***，所有的集合都扩展自 Iterable特质。
- 对于几乎所有的集合类，Scala 都同时提供了可变和不可变的版本，分别位于以下两个包
  - 不可变集合：`scala.collection.immutable`
  - 可变集合：` scala.collection.mutable`
- Scala 不可变集合，就是指该集合对象不可修改，每次修改就会返回一个新对象，而不会对原对象进行修改。类似于 java 中的 String 对象
- 可变集合，就是这个集合可以直接对原对象进行修改，而不会返回新的对象。类似于 java 中 StringBuilder 对象

***建议：在操作集合的时候，不可变用符号，可变用方法***

##### 不可变集合继承图

![image-20210603133640364](Scala%E6%A6%82%E8%BF%B0.assets/image-20210603133640364.png)

