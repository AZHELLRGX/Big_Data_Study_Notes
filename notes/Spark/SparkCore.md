### 快速起手

#### WordCount案例

##### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.azhell</groupId>
    <artifactId>spark-learn</artifactId>
    <version>1.0.0</version>

    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.apache.spark</groupId>
            <artifactId>spark-core_2.12</artifactId>
            <version>3.0.0</version>
        </dependency>
    </dependencies>

    <build>
        <sourceDirectory>src/main/scala</sourceDirectory>
        <testSourceDirectory>src/test/scala</testSourceDirectory>

        <plugins>
            <!-- 该插件用于将 Scala 代码编译成 class 文件 -->
            <plugin>
                <groupId>net.alchim31.maven</groupId>
                <artifactId>scala-maven-plugin</artifactId>
                <version>3.2.2</version>
                <executions>
                    <execution>
                        <!-- 声明绑定到 maven 的 compile 阶段 -->
                        <goals>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>single</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

##### WordCount.scala

````scala
package org.azhell.learn.spark.wc

import org.apache.spark.{SparkConf, SparkContext}

object WordCount {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[*]").setAppName("wordCount")
    val context = new SparkContext(conf)
    val fileRDD = context.textFile("data")
    val wordRDD = fileRDD.flatMap(_.split(" "))
    val wordPairRDD = wordRDD.map((_, 1))
    val wordCountRDD = wordPairRDD.reduceByKey(_ + _)
    wordCountRDD.collect().foreach(println)
  }
}
````

### 运行环境

![image-20210530154657109](Spark%20Core.assets/image-20210530154657109.png)

#### Local模式

所谓的 Local 模式，就是不需要其他任何节点资源就可以在本地执行 Spark 代码的环境，一般用于教学，调试，演示等，之前在 IDEA 中运行代码的环境我们称之为开发环境，不太一样。

##### 命令行交互

```shell
# 将spark压缩包解压以后
tar -zxvf spark-3.0.0-bin-hadoop3.2.tgz -C /opt/module
cd /opt/module
mv spark-3.0.0-bin-hadoop3.2 spark-local
bin/spark-shell
# 使用一行代码就可以完成上文的wordCount实例
sc.textFile("data/word.txt").flatMap(_.split("")).map((_,1)).reduceByKey(_+_).collect
# 退出本地模式可以使用按键 Ctrl+C 或输入 Scala 指令
:quit
```

##### 本地模式提交JAR

```shell
bin/spark-submit \
--class org.apache.spark.examples.SparkPi \
--master local[2] \
./examples/jars/spark-examples_2.12-3.0.0.jar \
10
```

#### Standalone模式

local 本地模式毕竟只是用来进行练习演示的，真实工作中还是要将应用提交到对应的集群中去执行，这里我们来看看只使用 Spark 自身节点运行的集群模式，也就是我们所谓的独立部署（Standalone）模式。Spark 的 Standalone 模式体现了经典的 master-slave 模式。

Standalone也可以构建HA集群，通过增加一个备份Master的方式来完成

#### 提交参数说明

```shell
bin/spark-submit \
--class <main-class>
--master <master-url> \
... # other options
<application-jar> \
[application-arguments]
```

![image-20210530160453528](Spark%20Core.assets/image-20210530160453528.png)

#### Yarn模式

### Spark运行架构

#### 核心组件

![image-20210530194910422](Spark%20Core.assets/image-20210530194910422.png)





#### 核心概念

##### Executor 与 与 Core

Spark Executor 是集群中运行在工作节点（Worker）中的一个 JVM 进程，是整个集群中的专门用于计算的节点。在提交应用中，可以提供参数指定计算节点的个数，以及对应的资源。这里的资源一般指的是工作节点 Executor 的内存大小和使用的虚拟 CPU 核（Core）数量。

相关配置参数如下：

![image-20210530195345740](Spark%20Core.assets/image-20210530195345740.png)

##### 并行度（Parallelism ）
在分布式计算框架中一般都是多个任务同时执行，由于任务分布在不同的计算节点进行计算，所以能够真正地实现多任务并行执行，记住，这里是并行，而不是并发。这里我们将整个集群并行执行任务的数量称之为并行度。

##### 有向无环图DAG

![image-20210530211527357](Spark%20Core.assets/image-20210530211527357.png)

#### 提交流程

![image-20210530211801352](Spark%20Core.assets/image-20210530211801352.png)

Spark 应用程序提交到 Yarn 环境中执行的时候，一般会有两种部署执行的方式：Client和 Cluster。两种模式主要区别在于：Driver 程序的运行节点位置。

### Spark核心编程

Spark 计算框架为了能够进行高并发和高吞吐的数据处理，封装了三大数据结构，用于处理不同的应用场景。三大数据结构分别是：

- RDD : 弹性分布式数据集
- 累加器 : 分布式共享只写变量
- 广播变量 : 分布式共享只读变量

#### 什么是 RDD
RDD（Resilient Distributed Dataset）叫做弹性分布式数据集，是 Spark 中最基本的数据处理模型。代码中是一个抽象类，它代表一个弹性的、不可变、可分区、里面的元素可并行计算的集合。

- 弹性
  - 存储的弹性：内存与磁盘的自动切换；
  - 容错的弹性：数据丢失可以自动恢复；
  - 计算的弹性：计算出错重试机制；
  - 分片的弹性：可根据需要重新分片。
- 分布式：数据存储在大数据集群不同节点上
- 数据集：RDD 封装了计算逻辑，并不保存数据
- 数据抽象：RDD 是一个抽象类，需要子类具体实现
- 不可变：RDD 封装了计算逻辑，是不可以改变的，想要改变，只能产生新的 RDD，在新的 RDD 里面封装计算逻辑
- 可分区、并行计算

RDD其实也是一种装饰器设计模式，上面的WordCount示例图解如下：

![image-20210530224320815](Spark%20Core.assets/image-20210530224320815.png)

结合Driver与Executor计算模型，可以将Spark的运行架构简化为：

![image-20210530225631024](Spark%20Core.assets/image-20210530225631024.png)

#### 核心属性

> ```scala
> /* Internally, each RDD is characterized by five main properties:
> *
> *  - A list of partitions
> *  - A function for computing each split
> *  - A list of dependencies on other RDDs
> *  - Optionally, a Partitioner for key-value RDDs (e.g. to say that the RDD is hash-partitioned)
> *  - Optionally, a list of preferred locations to compute each split on (e.g. block locations for
> *    an HDFS file)
> */
> ```

##### 分区列表

RDD 数据结构中存在分区列表，用于执行任务时并行计算，是实现分布式计算的重要属性。

```scala
/**
   * Implemented by subclasses to return the set of partitions in this RDD. This method will only
   * be called once, so it is safe to implement a time-consuming computation in it.
   *
   * The partitions in this array must satisfy the following property:
   *   `rdd.partitions.zipWithIndex.forall { case (partition, index) => partition.index == index }`
   */
 protected def getPartitions: Array[Partition]
```

##### 分区计算函数

Spark 在计算时，是使用分区函数对每一个分区进行计算

```scala
/**
   * :: DeveloperApi ::
   * Implemented by subclasses to compute a given partition.
   */
@DeveloperApi
def compute(split: Partition, context: TaskContext): Iterator[T]
```

##### RDD 之间的依赖关系

RDD 是计算模型的封装，当需求中需要将多个计算模型进行组合时，就需要将多个 RDD 建立依赖关系

```scala
/**
   * Implemented by subclasses to return how this RDD depends on parent RDDs. This method will only
   * be called once, so it is safe to implement a time-consuming computation in it.
   */
protected def getDependencies: Seq[Dependency[_]] = deps
```

##### 分区器（可选）

当数据为 KV 类型数据时，可以通过设定分区器自定义数据的分区

```scala
/** Optionally overridden by subclasses to specify how they are partitioned. */
@transient val partitioner: Option[Partitioner] = None
```

##### 首选位置（可选）

计算数据时，可以根据计算节点的状态选择不同的节点位置进行计算

```scala
 /**
   * Optionally overridden by subclasses to specify placement preferences.
   */
 protected def getPreferredLocations(split: Partition): Seq[String] = Nil
```

#### 分区的设定和分区数据的分配

##### 集合数据源

##### 文件数据源



#### RDD算子

RDD方法大致可以分为两类：

- 转换算子：功能的补充和封装，将旧的RDD包装成新的RDD  ； 例如flatMap、map
- 行动算子：触发任务的调度和作业的执行 ； 例如 collect



