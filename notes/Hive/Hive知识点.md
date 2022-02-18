## hive partition by 和 group by 的区别

- partition by不会改变数据行数，而group by则会改变
- partition by用在窗口函数上，group by则是一种分组聚类操作

## 分区和分桶的区别

分区主要是为了方便数据的快速查询，从文件结构来看，分区就是HDFS的一个目录，可以指定多层分区，这样HDFS就会产生多级目录；经常用来作为分区的字段是日期【年月日等】、地区等；分区字段是一个伪列，可以查询，但不实际存储；分区也分为静态分区和动态分区，但是在Hive的默认严格模式下，要求至少存在一个静态分区

但是分区无法解决一切问题，因为不能无限制的创建分区，这样HDFS的namenode会撑不住，所以分桶表的出现，是将一个完整的数据集分成若干部分。它存在的意义是：一是提高 join 查询的效率；二是利于抽样。

```sql
create table bck_student(   
id int,
name string,
sex string,
age int,
department string)
-- 按照sex字段分两个桶
clustered by(sex) into 2 buckets
row format delimited
fields terminated  by ",";
```

## left semi join和left join区别

LEFT SEMI JOIN （左半连接）是 IN/EXISTS 子查询的一种更高效的实现。

- left semi join中右表的过滤条件只能写在on条件中，where中不生效，而left join都会生效
- left semi join的结果中只有左表的内容，而left join可以保留右边的字段，这也是导致上一条问题的原因
- left semi join工作在Map阶段，当找到可以关联的值的时候，就停止查找，而left join则可能会产生多条记录

## Hive中的数据取样

### 数据块抽样（tablesample()函数）

- tablesample(n percent) 根据hive表数据的大小按比例抽取数据，并保存到新的hive表中。如：抽取原hive表中10%的数据（注意：测试过程中发现，select语句不能带where条件且不支持子查询，可通过新建中间表或使用随机抽样解决）

```sql
create table xxx_new as select * from xxx tablesample(10 percent)
```

- tablesample(n M) 指定抽样数据的大小，单位为M。
- tablesample(n rows) 指定抽样数据的行数，其中n代表每个map任务均取n行数据，map数量可通过hive表的简单查询语句确认（关键词：number of mappers: x)

### 分桶抽样

hive中分桶其实就是根据某一个字段Hash取模，放入指定数据的桶中，比如将表table_1按照ID分成100个桶，其算法是hash(id) % 100，这样，hash(id) % 100 = 0的数据被放到第一个桶中，hash(id) % 100 = 1的记录被放到第二个桶中。创建分桶表的关键语句为：CLUSTER BY语句。

分桶抽样语法

```sql
TABLESAMPLE (BUCKET x OUT OF y [ON colname])
```

其中x是要抽样的桶编号，桶编号从1开始，colname表示抽样的列，y表示桶的数量。

例如：将表随机分成10组，抽取其中的第一个桶的数据

```sql
select * from table_01 tablesample(bucket 1 out of 10 on rand())
```

### 随机抽样（rand()函数）

- 使用rand()函数进行随机抽样，limit关键字限制抽样返回的数据，其中rand函数前的distribute和sort关键字可以保证数据在mapper和reducer阶段是随机分布的，案例如下：

```sql
select * from table_name where col=xxx distribute by rand() sort by rand() limit num;
```

- 使用order 关键词；案例如下：

```plsql
select * from table_name where col=xxx order by rand() limit num;
```

经测试对比，千万级数据中进行随机抽样 order by方式耗时更长，大约多30秒左右。

## 开窗函数中row number， rank， dense rank区别

三个都是开窗函数，开窗函数由分析函数（sum()，max()等）和窗口函数（over()）组成

- ROW_NUMBER() 从1开始，按照顺序，生成分组内记录的序列。

- RANK() 生成数据项在分组中的排名，排名相等会在名次中留下空位

- DENSE_RANK() 生成数据项在分组中的排名，排名相等会在名次中不会留下空位

> 关于更加详细的介绍和使用可以参考：[Hive开窗函数总结与实践 ](https://cloud.tencent.com/developer/article/1780007)

### Hive的四个by说一下

> [深入探究order by,sort by,distribute by,cluster by的区别](https://mp.weixin.qq.com/s?__biz=MzI2MDQzOTk3MQ==&mid=2247485089&idx=1&sn=1224bc187e02e0d725487f6ed85752ee&chksm=ea68ec6ddd1f657bc39f01dc68c726ce4879848a65211ce2a2d3215a549a2e589c2eac3a5bef&scene=21)

## Hive的复杂数据类型

先复习一下基本数据类型；Hive的基本数据类型与Mysql完全一致

​                 <img src="Hive%E7%9F%A5%E8%AF%86%E7%82%B9.assets/pesM1BAKdoKljptJh_zSMg.png" alt="img" style="zoom:80%;" />        

复杂数据类型主要包含：

- Struts【结构类型使用点(.)方式获取属性数据】

- Maps【key-value类型，使用M['key']方式获取数据】

- Arrays【数组类型，可以通过A[下标]方式获取数据】

```sql
-- 使用三种复杂数据类型创建表
create table students(
id INT,
name STRING,
scores STRUCT<english:INT,math:INT,chinese:INT> COMMENT '英语分数、数学分数、语文分数',
reward MAP<STRING,STRING> COMMENT '奖励',
goods ARRAY<STRING> COMMENT '持有物品'
);

-- 使用
select reward['二等奖'] from students where name='admin';
select scores.chinese from students where name='admin';
select goods[0] from students where name='admin';
```

## udf、udtf、udaf

- UDF：用户定义（普通）函数，只对单行数值产生作用；
- UDAF：User- Defined Aggregation Funcation；用户定义聚合函数，可对多行数据产生作用；等同与SQL中常用的SUM()，AVG()，也是聚合函数；
- UDTF：User-Defined Table-Generating Functions，用户定义表生成函数，用来解决输入一行输出多行；

需要分别实现一些接口，然后注册成临时函数或者永久函数，在SQL中使用

## 其它

> - [什么情况下，Hive 只会产生一个reduce任务，没有maptask-Hive](https://www.aboutyun.com/thread-30604-1-1.html)

