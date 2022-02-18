## 架构原理

> 参考文章： 
>
> - [Hive架构原理](https://zhuanlan.zhihu.com/p/87545980) 
> - [Hive SQL的编译过程](https://tech.meituan.com/2014/02/12/hive-sql-to-mapreduce.html) 
> - [Hive on Spark设计原则及架构 ](https://cloud.tencent.com/developer/article/1779286) 

 <img src="Hive%E6%9E%B6%E6%9E%84.assets/v2-7d929c41583c29665e960a27751ead7d_r.jpg" alt="preview" style="zoom: 50%;" /> 

### 用户接口: Client 

 CLI（command-line interface）、JDBC/ODBC(jdbc访问hive)、WEBUI（浏览器访问hive） 

###  元数据：Metastore 

元数据包括：表名、表所属的数据库（默认是default）、表的拥有者、列/分区字段、表的类型（是否是外部表）、表的数据所在目录等；

默认存储在自带的derby数据库中，推荐使用MySQL存储Metastore

###  Hadoop 

使用HDFS进行存储，使用MapReduce进行计算。 

###  驱动器：Driver 

（1）解析器（SQL Parser）：将SQL字符串转换成抽象语法树AST，这一步一般都用第三方工具库完成，比如antlr；对AST进行语法分析，比如表是否存在、字段是否存在、SQL语义是否有误。

（2）编译器（Physical Plan）：将AST编译生成逻辑执行计划。

（3）优化器（Query Optimizer）：对逻辑执行计划进行优化。

（4）执行器（Execution）：把逻辑执行计划转换成可以运行的物理计划。对于Hive来说，就是MR/Spark。

## 任务转化流程

 ![preview](Hive%E6%9E%B6%E6%9E%84.assets/v2-81d1271cd9d269f14531cae220865de4_r.jpg) 

### 编译流程

整个编译过程分为六个阶段：

1. Antlr定义SQL的语法规则，完成SQL词法，语法解析，将SQL转化为抽象语法树AST Tree
2. 遍历AST Tree，抽象出查询的基本组成单元QueryBlock
3. 遍历QueryBlock，翻译为执行操作树OperatorTree
4. 逻辑层优化器进行OperatorTree变换，合并不必要的ReduceSinkOperator，减少shuffle数据量
5. 遍历OperatorTree，翻译为MapReduce任务
6. 物理层优化器进行MapReduce任务的变换，生成最终的执行计划

 ![img](Hive%E6%9E%B6%E6%9E%84.assets/v2-4f7272ba9812f78cbb2b1f7917d23ed7_720w.jpg) 

### Hive编译器组成

 ![img](Hive%E6%9E%B6%E6%9E%84.assets/v2-877198e40dbb1baebeea2ff471a16ee4_720w.jpg) 

## Hive优缺点

### 优点

- 简单容易上手：提供了类SQL查询语言HQL
- 可扩展：为超大数据集设计了计算/扩展能力（MR\Tez\Spark作为计算引擎，HDFS作为存储系统）
  一般情况下不需要重启服务Hive可以自由的扩展集群的规模。
- 提供统一的元数据管理
- 延展性：Hive支持用户自定义函数，用户可以根据自己的需求来实现自己的函数
- 容错：良好的容错性，节点出现问题SQL仍可完成执行 

### 缺点

- hive的HQL表达能力有限
  - 迭代式算法无法表达，比如pagerank
  - 数据挖掘方面，比如kmeans
- hive的效率比较低
    - hive自动生成的mapreduce作业，通常情况下不够智能化
    - hive调优比较困难，粒度较粗
    - hive可控性差 

### Hive工作原理

需要注意：下图中Mapreduce部分还是1.0的模式，2.0中资源管理已经交给Yarn负责

 ![img](Hive%E6%9E%B6%E6%9E%84.assets/v2-14fee99a0ec40fd03bee7020db37bc6a_720w.jpg) 