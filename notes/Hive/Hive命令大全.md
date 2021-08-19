# Hive 命令大全

## 1. Hive 交互方式

### 1.1 Hive 命令行

- 在任意路径运行hive

```shell
[hadoop@node03 ~]$ hive
```

### 1.2 Hive JDBC服务

- 启动hiveserver2服务，前台启动与后台启动方式二选一

- 前台启动

```shell
[hadoop@node03 ~]$ hive --service hiveserver2
```

  - 后台启动

```shell
[hadoop@node03 ~]$ nohup hive --service hiveserver2 &
```

- beeline连接hiveserver2服务

  若是前台启动hiveserver2，请再开启一个新会话窗口，然后使用beeline连接hive


```shell
[hadoop@node03 ~]$ beeline
beeline> !connect jdbc:hive2://node03:10000
```

  	用户名hadoop，密码为空即可

- 帮助信息

```shell
0: jdbc:hive2://node03:10000> help
```

- 退出

```shell
0: jdbc:hive2://node03:10000> !quit
```

### 1.3  Hive的命令

- hive  ==-e== hql语句
  - 使用 –e 参数来直接执行hql语句

```shell
[hadoop@node03 ~]$ hive -e "show databases"
```

- hive  ==-f==  sql文件

  使用 –f参数执行包含hql语句的文件

  node03执行以下命令准备hive执行脚本

```shell
[hadoop@node03 ~]$ cd /kkb/install/
[hadoop@node03 install]$ vim hive.sql
```

​		文件内容如下

```mysql
create database if not exists myhive;
```

​		通过以下命令来执行我们的hive脚本

```shell
[hadoop@node03 install]$ hive -f /kkb/install/hive.sql 
```

​		查看效果，成功执行hql语句，创建myhive数据库

## 2. Hive的数据类型

### 2.1 基本数据类型

|    类型名称    |              描述               |    举例    |
| :------------: | :-----------------------------: | :--------: |
|    boolean     |           true/false            |    true    |
|    tinyint     |        1字节的有符号整数        |     1      |
|    smallint    |        2字节的有符号整数        |     1      |
|  ==**int**==   |        4字节的有符号整数        |     1      |
| **==bigint==** |        8字节的有符号整数        |     1      |
|     float      |        4字节单精度浮点数        |    1.0     |
| **==double==** |        8字节单精度浮点数        |    1.0     |
| **==string==** |        字符串(不设长度)         |   “abc”    |
|    varchar     | 字符串（1-65355长度，超长截断） |   “abc”    |
|   timestamp    |             时间戳              | 1563157873 |
|      date      |              日期               |  20190715  |

### 2.2 复合数据类型

| 类型名称 |                         描述                          |       举例        |
| :------: | :---------------------------------------------------: | :---------------: |
|  array   | 一组有序的字段，字段类型必须相同 array(元素1，元素2)  |  Array（1,2,3）   |
|   map    |           一组无序的键值对 map(k1,v1,k2,v2)           | Map(‘a’,1,'b',2)  |
|  struct  | 一组命名的字段，字段类型可以不同 struct(元素1，元素2) | Struct('a',1,2,0) |

- array类型的字段的元素访问方式
  - 通过下标获取元素，下标从0开始
  - 如获取第一个元素
    - array[0]

- map类型字段的元素访问方式
  - 通过键获取值
  - 如获取a这个key对应的value
    - map['a']

- struct类型字段的元素获取方式
  - 定义一个字段c的类型为struct{a int, b string}
  - 获取a和b的值
    - 使用c.a 和c.b 获取其中的元素值
  - 这里可以把这种类型看成是一个对象
- 示例：创建一张表，包含了array、map、struct类型的字段

~~~sql
create table complex(
         col1 array<int>,
         col2 map<string,int>,
         col3 struct<a:string,b:int,c:double>
)
~~~

### 2.3 复合类型实战

#### 1、参数说明

~~~
创建表的时候可以指定每行数据的格式,如果使用的是复合数据类型，还需要指定复合数据类型中的元素分割符
ROW FORMAT DELIMITED 
	[FIELDS TERMINATED BY char [ESCAPED BY char]] 
	[COLLECTION ITEMS TERMINATED BY char]
	[MAP KEYS TERMINATED BY char] 
	[LINES TERMINATED BY char]
		
其中这里 
FIELDS TERMINATED BY char 	         指定每一行记录中字段的分割符
COLLECTION ITEMS TERMINATED BY char  指定复合类型中多元素的分割符
MAP KEYS TERMINATED BY char         指定map集合类型中每一个key/value之间的分隔符
LINES TERMINATED BY char            指定每行记录的换行符，一般有默认 就是\n 
~~~

#### 2、Array类型

* array中的数据为相同类型，例如，假如array A中元素['a','b','c']，则A[1]的值为'b'

* 准备数据文件

  *  t_array.txt  (字段空格分割)

  ~~~
  1 zhangsan beijing,shanghai
  2 lisi shanghai,tianjin
  ~~~

* 建表语法

  ~~~sql
  create table t_array(
  id string,
  name string,
  locations array<string>
  ) row format delimited fields terminated by ' ' collection items terminated by ',';
  ~~~

* 加载数据

  ~~~sql
  load data local inpath '/home/hadoop/t_array.txt' into table t_array;
  ~~~

* 查询数据

  ~~~sql
  select id,name,locations[0],locations[1] from t_array;
  ~~~

#### 3、Map类型

* map类型中存储key/value类型的数据，后期可以通过["指定key名称"]访问

* 准备数据文件

  - t_map.txt  (字段空格分割)

    ~~~
    1 name:zhangsan#age:30
    2 name:lisi#age:40
    ~~~

* 建表语法

  ~~~sql
  create table t_map(
  id string,
  info map<string,string>
  ) row format delimited fields terminated by ' ' collection items terminated by '#' map keys terminated by ':';
  
  ~~~

* 加载数据

  ~~~sql
  load data local inpath '/home/hadoop/t_map.txt' into table t_map;
  ~~~

* 查询数据

  ~~~sql
  select id, info['name'], info['age'] from t_map;
  ~~~



#### 4、Struct类型

* 可以存储不同类型的数据

  * 例如c的类型为struct{a INT; b INT}，我们可以通过c.a来访问域a

* 准备数据文件

  - t_struct.txt  (字段空格分割)

    ~~~
    1 zhangsan:30:beijing
    2 lisi:40:shanghai
    ~~~

* 建表语法

  ```sql
  create table t_struct(
  id string,
  info struct<name:string, age:int,address:String>
  ) row format delimited fields terminated by ' ' collection items terminated by ':' ;
  ```

- 加载数据

  ```sql
  load data local inpath '/home/hadoop/t_struct.txt' into table t_struct;
  ```

- 查询数据

  ```sql
  select id,info.name,info.age,info.address from t_struct;
  ```

## 3. 数据库DDL操作

### 3.1 创建数据库

```sql
hive > create database db_hive;
# 或者
hive > create database if not exists db_hive;
```

- 数据库在HDFS上的默认存储路径是==/user/hive/warehouse/数据库名.db==

### 3.2 显示所有数据库

```sql
  hive> show databases;
```

### 3.3 查询数据库	

```sql
hive> show databases like 'db_hive*';
```

### 3.4 查看数据库详情

```sql
hive> desc database db_hive;
```

- 显示数据库详细信息

```sql
hive> desc database extended db_hive;
```

### 3.5 切换当前数据库

```sql
hive > use db_hive;
```

### 3.6 删除数据库

```sql
#删除为空的数据库
hive> drop database db_hive;

#如果删除的数据库不存在，最好采用if exists 判断数据库是否存在
hive> drop database if exists db_hive;

#如果数据库中有表存在，这里需要使用cascade强制删除数据库
hive> drop database if exists db_hive cascade;
```

## 4. 表DDL操作

### 4.0 查看表结构与表属性

```sql
-- 查看表结构
desc table_name
-- 查看表属性
desc formatted table_name;
```

### 4.1 建表语法介绍

```sql
CREATE [EXTERNAL] TABLE [IF NOT EXISTS] table_name 
[(col_name data_type [COMMENT col_comment], ...)] 
[COMMENT table_comment] 
[PARTITIONED BY (col_name data_type [COMMENT col_comment], ...)] 分区
[CLUSTERED BY (col_name, col_name, ...) 分桶
[SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS] 
[ROW FORMAT row_format]  row format delimited fields terminated by "分隔符"
[STORED AS file_format] 
[LOCATION hdfs_path]
```

官网地址：https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL

- 字段解释说明
  - CREATE TABLE	创建一个指定名字的表
  - EXTERNAL       创建一个外部表，在建表的同时指定一个指向实际数据的路径（LOCATION），指定表的数据保存在哪里
  - COMMENT       为表和列添加注释
  - PARTITIONED BY    创建分区表
  - CLUSTERED BY    创建分桶表
  - SORTED BY    按照字段排序（一般不常用）
  - ROW FORMAT    指定每一行中字段的分隔符
    - row format delimited fields terminated by ‘\t’
  - STORED AS    指定存储文件类型
    - 常用的存储文件类型：SEQUENCEFILE（二进制序列文件）、TEXTFILE（文本）、ORCFILE（列式存储格式文件）
    - 如果文件数据是纯文本，可以使用STORED AS TEXTFILE。如果数据需要压缩，使用 STORED AS SEQUENCEFILE
  - LOCATION    指定表在HDFS上的存储位置。

### 4.2 创建内部表

#### 4.2.1 使用标准的建表语句直接建表

```sql
use myhive;
create table stu(id int, name string);

# 可以通过insert into向hive表当中插入数据，但是不建议工作当中这么做；因为每个insert语句转换成mr后会生成一个文件
insert into stu(id,name) values(1,"zhangsan");
insert into stu(id,name) values(2,"lisi");

select * from  stu;
```

#### 4.2.2 查询建表法

- 通过AS 查询语句完成建表：将子查询的结果存入新表里

```sql
create table if not exists myhive.stu1 as select id, name from stu;

# 表中有数据
select * from stu1;
```

#### 4.2.3 like建表法

- 根据已经存在的表结构创建表

```sql
create table if not exists myhive.stu2 like stu;

表中没有数据
select * from stu2;
```

#### 4.2.4 查询表的类型

```sql
hive > desc formatted myhive.stu;
```

- hql示例：创建内部表并指定字段之间的分隔符，指定文件的存储格式，以及数据存放的位置

```sql
create table if not exists myhive.stu3(id int, name string)
row format delimited fields terminated by '\t' 
stored as textfile 
location '/user/stu3';
```

### 4.3 创建外部表

- 外部表因为是指定其他的hdfs路径的数据加载到表当中来
- 所以hive表会认为自己不完全独占这份数据，所以删除hive表的时候，数据仍然存放在hdfs当中，不会删掉

```sql
create external table myhive.teacher (t_id string, t_name string) 
row format delimited fields terminated by '\t';
```

- 创建外部表的时候需要加上**external** 关键字
- location字段可以指定，也可以不指定
  - 指定就是数据存放的具体目录
  - 不指定就是使用默认目录 /user/hive/warehouse

- 向外部表当中加载数据：

  - 我们前面已经看到过通过insert的方式向内部表当中插入数据，外部表也可以通过insert的方式进行插入数据，只不过insert的方式，我们一般都不推荐
  - 实际工作当中我们都是使用load的方式来加载数据到内部表或者外部表

- load数据可以从本地文件系统加载或者也可以从hdfs上面的数据进行加载

  - 从本地文件系统加载数据到teacher表当中去，将我们附件当汇总的数据资料都上传到node03服务器的/kkb/install/hivedatas路径下面去

  ```shell
  mkdir -p /kkb/install/hivedatas
  ```

  - 将数据都上传到/kkb/install/hivedatas路径下
  - 然后在hive客户端下执行以下操作

  ```sql
  load data local inpath '/kkb/install/hivedatas/teacher.csv' into table myhive.teacher;
  ```

- 从hdfs上面加载文件到teacher表里面去(将teacher.csv文件上传到hdfs的/kkb/hdfsload/hivedatas路径下)

  ```shell
  cd /kkb/install/hivedatas
  hdfs dfs -mkdir -p /kkb/hdfsload/hivedatas
  hdfs dfs -put teacher.csv /kkb/hdfsload/hivedatas
  # 在hive的客户端当中执行
  load data inpath '/kkb/hdfsload/hivedatas' overwrite into table myhive.teacher;
  ```

### 4.4 内部表与外部表的互相转换

#### 4.4.1 内部表转换为外部表

```sql
#将stu内部表改为外部表
alter table stu set tblproperties('EXTERNAL'='TRUE');
```

#### 4.4.2 外部表转换为内部表

```sql
#把teacher外部表改为内部表
alter table teacher set tblproperties('EXTERNAL'='FALSE');
```

### 4.5 内部表与外部表的区别

- 1、建表语法的区别
  - 外部表在创建的时候需要加上external关键字
- 2、删除表之后的区别
  - 内部表删除后，表的元数据和真实数据都被删除了
  - 外部表删除后，仅仅只是把该表的元数据删除了，真实数据还在，后期还是可以恢复出来

### 4.6 内部表与外部表的使用时机

- 内部表由于删除表的时候会同步删除HDFS的数据文件，所以确定如果一个表仅仅是你独占使用，其他人不使用的时候就可以创建内部表，如果一个表的文件数据，其他人也要使用，那么就创建外部表
- 一般外部表都是用在数据仓库的ODS层
- 内部表都是用在数据仓库的DW层

### 4.7 hive的分区表

- 如果hive当中所有的数据都存入到一个文件夹下面，那么在使用MR计算程序的时候，读取一整个目录下面的所有文件来进行计算，就会变得特别慢，因为数据量太大了
- 实际工作当中一般都是计算前一天的数据，所以我们只需要将前一天的数据挑出来放到一个文件夹下面即可，专门去计算前一天的数据。
- 这样就可以使用hive当中的分区表，通过分文件夹的形式，将每一天的数据都分成为一个文件夹，然后我们计算数据的时候，通过指定前一天的文件夹即可只计算前一天的数据。
- 在大数据中，最常用的一种思想就是分治，我们可以把大的文件切割划分成一个个的小的文件，这样每次操作一个小的文件就会很容易了，同样的道理，在hive当中也是支持这种思想的，就是我们可以把大的数据，按照每天，或者每小时进行切分成一个个的小的文件，这样去操作小的文件就会容易得多了

- 在文件系统上建立文件夹，把表的数据放在不同文件夹下面，加快查询速度。

#### 4.7.1 创建分区表语法

```sql
hive (myhive)> create table score(s_id string, c_id string, s_score int) partitioned by (month string) row format delimited fields terminated by '\t';
```

#### 4.7.2 创建一个表带多个分区

```sql
hive (myhive)> create table score2 (s_id string,c_id string, s_score int) partitioned by (year string, month string, day string) row format delimited fields terminated by '\t';
```

#### 4.7.3 加载数据到分区表当中去

```sql
 hive (myhive)>load data local inpath '/kkb/install/hivedatas/score.csv' into table score partition (month='201806');
```

#### 4.7.4 加载数据到多分区表当中去

```sql
hive (myhive)> load data local inpath '/kkb/install/hivedatas/score.csv' into table score2 partition(year='2018', month='06', day='01');
```

#### 4.7.5查看分区

```sql
hive (myhive)> show  partitions  score;
```

#### 4.7.6 添加一个分区

```sql
hive (myhive)> alter table score add partition(month='201805');
```

#### 4.7.7 同时添加多个分区

```sql
hive (myhive)> alter table score add partition(month='201804') partition(month = '201803');
```

- 注意：添加分区之后就可以在hdfs文件系统当中看到表下面多了一个文件夹

#### 4.7.8 删除分区

```sql
hive (myhive)> alter table score drop partition(month = '201806');
```



- 外部分区表综合练习：

- 需求描述：

  - 现在有一个文件score.csv文件，里面有三个字段，分别是s_id string, c_id string, s_score int
  - 字段都是使用 \t进行分割
  - 存放在集群的这个目录下/scoredatas/day=20180607，这个文件每天都会生成，存放到对应的日期文件夹下面去
  - 文件别人也需要公用，不能移动
  - 请创建hive对应的表，并将数据加载到表中，进行数据统计分析，且删除表之后，数据不能删除

- 需求实现:

- 数据准备:

- node03执行以下命令，将数据上传到hdfs上面去

  将我们的score.csv上传到node03服务器的/kkb/install/hivedatas目录下，然后将score.csv文件上传到HDFS的/scoredatas/day=20180607目录上

```
cd /kkb/install/hivedatas/
hdfs dfs -mkdir -p /scoredatas/day=20180607
hdfs dfs -put score.csv /scoredatas/day=20180607/
```

- 创建外部分区表，并指定文件数据存放目录

```
hive (myhive)> create external table score4(s_id string, c_id string, s_score int) partitioned by (day string) row format delimited fields terminated by '\t' location '/scoredatas';
```

- 进行表的修复，说白了就是建立我们表与我们数据文件之间的一个关系映射()

```
hive (myhive)> msck repair table score4;
```

- 修复成功之后即可看到数据已经全部加载到表当中去了

### 4.8 Hive的分桶表

#### 4.8.1 分桶表原理

- 分桶是相对分区进行更细粒度的划分

  - Hive表或分区表可进一步的分桶

  - ==分桶将整个数据内容按照某列取hash值，对桶的个数取模的方式决定该条记录存放在哪个桶当中；具有相同hash值的数据进入到同一个文件中==

  - 比如按照name属性分为3个桶，就是对name属性值的hash值对3取摸，按照取模结果对数据分桶。
    - 取模结果为==0==的数据记录存放到一个文件

    - 取模结果为==1==的数据记录存放到一个文件

    - 取模结果为==2==的数据记录存放到一个文件

#### 4.8.2 作用

- 1、取样sampling更高效。没有分区的话需要扫描整个数据集。

- 2、提升某些查询操作效率，例如map side join

#### 4.8.3 案例演示：创建分桶表

- 在创建分桶表之前要执行的命令
- ==set hive.enforce.bucketing=true;==  开启对分桶表的支持
- ==set mapreduce.job.reduces=4;==      设置与桶相同的reduce个数（默认只有一个reduce）

- 进入hive客户端然后执行以下命令

```sql
use myhive;
set hive.enforce.bucketing=true; 
set mapreduce.job.reduces=4;  

-- 创建分桶表
create table myhive.user_buckets_demo(id int, name string)
clustered by(id) 
into 4 buckets 
row format delimited fields terminated by '\t';

-- 创建普通表
create table user_demo(id int, name string)
row format delimited fields terminated by '\t';
```

- 准备数据文件 buckets.txt

```shell
#在linux当中执行以下命令
cd /kkb/install/hivedatas/
vim user_bucket.txt

1	anzhulababy1
2	anzhulababy2
3	anzhulababy3
4	anzhulababy4
5	anzhulababy5
6	anzhulababy6
7	anzhulababy7
8	anzhulababy8
9	anzhulababy9
10	anzhulababy10
```

- 加载数据到普通表 user_demo 中

```sql
load data local inpath '/kkb/install/hivedatas/user_bucket.txt'  overwrite into table user_demo; 
```

##### 4.8.3.1 加载数据到桶表中

```sql
insert into table user_buckets_demo select * from user_demo;
```

- hdfs上查看表的数据目录

```bash
[hadoop@node01 ~]$ hdfs dfs -ls /user/hive/warehouse/myhive.db/user_buckets_demo
Found 4 items
-rwxrwxrwx   2 hadoop supergroup         30 2020-02-25 17:58 /user/hive/warehouse/myhive.db/user_buckets_demo/000000_0
-rwxrwxrwx   2 hadoop supergroup         45 2020-02-25 17:59 /user/hive/warehouse/myhive.db/user_buckets_demo/000001_1
-rwxrwxrwx   2 hadoop supergroup         47 2020-02-25 17:58 /user/hive/warehouse/myhive.db/user_buckets_demo/000002_0
-rwxrwxrwx   2 hadoop supergroup         30 2020-02-25 17:59 /user/hive/warehouse/myhive.db/user_buckets_demo/000003_0
```



##### 4.8.3.2 抽样查询桶表的数据

- tablesample抽样语句语法：tablesample(bucket  x  out  of  y)
  - x表示从第几个桶开始取数据
  - y与进行采样的桶数的个数、每个采样桶的采样比例有关；

```sql
select * from user_buckets_demo tablesample(bucket 1 out of 2);
-- 需要采样的总桶数=4/2=2个
-- 先从第1个桶中取出数据
-- 1+2=3，再从第3个桶中取出数据
```

### 4.9 表结构修改

#### 4.9.1 更改表名

```sql
ALTER TABLE table_name RENAME TO new_table_name;
```

#### 4.9.2 增加列

```sql
ALTER TABLE table_name ADD COLUMNS (
  new_column1 STRING COMMENT '注释1',
  new_column2 INT COMMENT '注释2'
);
```

#### 4.9.3 删除列

删除列只能用列名替换来进行操作

```sql
ALTER TABLE name REPLACE COLUMNS (
  new_column1 STRING COMMENT '注释1',
  new_column2 INT COMMENT '注释2'
)
```

> ⚠️注意：由于`ALTER`操作只是更改Hive的元数据，对应数据的列并没有改变，因此在删除列的时候，需要将列名和数据列一一对应起来。

#### 4.9.4 更改列名

```sql
ALTER TABLE table_name CHANGE 
	column_name new_column_name new_type
	COMMENT 'new comment';
```



## 5. Hive数据导入

### 5.1 直接向表中插入数据（强烈不推荐使用）

  ```sql
hive (myhive)> create table score3 like score;
hive (myhive)> insert into table score3 partition(month ='201807') values ('001','002','100');
  ```

### 5.2 通过load加载数据（必须掌握）

- 语法：

```sql
 hive> load data [local] inpath 'dataPath' overwrite into table student [partition (partcol1=val1,…)]; 
```

- 通过load方式加载数据

```sql
hive (myhive)> load data local inpath '/kkb/install/hivedatas/score.csv' overwrite into table score3 partition(month='201806');
```

### 5.3 通过查询加载数据（必须掌握）

- 通过查询方式加载数据

```sql
hive (myhive)> create table score5 like score;
hive (myhive)> insert overwrite table score5 partition(month = '201806') select s_id,c_id,s_score from score;
```

### 5.4 查询语句中创建表并加载数据（as select）

- 将查询的结果保存到一张表当中去

```sql
hive (myhive)> create table score6 as select * from score;
```

### 5.5 创建表时指定location

- 创建表，并指定在hdfs上的位置

```sql
hive (myhive)> create external table score7 (s_id string,c_id string,s_score int) row format delimited fields terminated by '\t' location '/myscore7';
```

- 上传数据到hdfs上，我们也可以直接在hive客户端下面通过dfs命令来进行操作hdfs的数据

```sql
hive (myhive)> dfs -mkdir -p /myscore7;
hive (myhive)> dfs -put /kkb/install/hivedatas/score.csv /myscore7;
```

- 查询数据

```sql
hive (myhive)> select * from score7;
```

### 5.6 export导出与import 导入 hive表数据（内部表操作）

```sql
hive (myhive)> create table teacher2 like teacher;
hive (myhive)> export table teacher to  '/kkb/teacher';
hive (myhive)> import table teacher2 from '/kkb/teacher';
```

## 6. Hive数据导出

### 6.1 insert 导出

- 将查询的结果导出到本地

```sql
insert overwrite local directory '/kkb/install/hivedatas/stu' select * from stu;
```

- 将查询的结果**格式化**导出到本地

```sql
insert overwrite local directory '/kkb/install/hivedatas/stu2' row format delimited fields terminated by ',' select * from stu;
```

- 将查询的结果导出到HDFS上==(没有local)==

```sql
insert overwrite directory '/kkb/hivedatas/stu' row format delimited fields terminated by  ','  select * from stu;
```

### 6.2 Hive Shell 命令导出

- 基本语法：

  - hive -e "sql语句" >   file
  - hive -f  sql文件   >    file

```shell
hive -e 'select * from myhive.stu;' > /kkb/install/hivedatas/student1.txt
```

### 6.3 export导出到HDFS上

```sql
export table  myhive.stu to '/kkb/install/hivedatas/stuexport';
```

## 7. Hive的静态分区和动态分区

### 7.1 静态分区

- 表的分区字段的值需要开发人员手动给定

- 创建分区表

```sql
use myhive;
create table order_partition(
order_number string,
order_price  double,
order_time string
)
partitioned BY(month string)
row format delimited fields terminated by '\t';
```


- 准备数据

```shell
cd /kkb/install/hivedatas
vim order.txt 

10001	100	2019-03-02
10002	200	2019-03-02
10003	300	2019-03-02
10004	400	2019-03-03
10005	500	2019-03-03
10006	600	2019-03-03
10007	700	2019-03-04
10008	800	2019-03-04
10009	900	2019-03-04
```

- 加载数据到分区表

```sql
load data local inpath '/kkb/install/hivedatas/order.txt' overwrite into table order_partition partition(month='2019-03');
```

  - 4、查询结果数据	

```sql
select * from order_partition where month='2019-03';
结果为：
  
10001   100.0   2019-03-02      2019-03
10002   200.0   2019-03-02      2019-03
10003   300.0   2019-03-02      2019-03
10004   400.0   2019-03-03      2019-03
10005   500.0   2019-03-03      2019-03
10006   600.0   2019-03-03      2019-03
10007   700.0   2019-03-04      2019-03
10008   800.0   2019-03-04      2019-03
10009   900.0   2019-03-04      2019-03
```


### 7.2 动态分区

- 按照需求实现把数据自动导入到表的不同分区中，==不需要手动指定==

- **需求：根据分区字段不同的值，自动将数据导入到分区表不同的分区中** 

- 创建表

```sql
--创建普通表
create table t_order(
    order_number string,
    order_price  double, 
    order_time   string
)row format delimited fields terminated by '\t';

--创建目标分区表
create table order_dynamic_partition(
    order_number string,
    order_price  double    
)partitioned BY(order_time string)
row format delimited fields terminated by '\t';
```

- 准备数据

```shell
cd /kkb/install/hivedatas
vim order_partition.txt

10001	100	2019-03-02 
10002	200	2019-03-02
10003	300	2019-03-02
10004	400	2019-03-03
10005	500	2019-03-03
10006	600	2019-03-03
10007	700	2019-03-04
10008	800	2019-03-04
10009	900	2019-03-04
```

- 向普通表t_order加载数据

```sql
load data local inpath '/kkb/install/hivedatas/order_partition.txt' overwrite into table t_order;
```

- 动态加载数据到分区表中

```sql
-- 要想进行动态分区，需要设置参数
-- 开启动态分区功能
hive> set hive.exec.dynamic.partition=true; 
-- 设置hive为非严格模式
hive> set hive.exec.dynamic.partition.mode=nonstrict; 
hive> insert into table order_dynamic_partition partition(order_time) select order_number, order_price, order_time from t_order;
```

- 查看分区

```sql
hive> show partitions order_dynamic_partition;
```

## 8. Hive的查询语法DQL

### 8.1 基本查询

- 注意
  - SQL 语言==大小写不敏感==
  - SQL 可以写在一行或者多行
  - ==关键字不能被缩写也不能分行==
  - 各子句一般要分行写
  - 使用缩进提高语句的可读性

#### 8.1.1 查询全表和特定列

- 全表查询

```sql
select * from stu;
```

- 选择特定列查询

```sql
select id,name from stu;
```

#### 8.1.2 列起别名

- 重命名一个列

  - 紧跟列名，也可以在列名和别名之间加入关键字 ‘as’ 

- 案例实操

```sql
select id,name as stuName from stu;
```

#### 8.1.3 常用函数

- 求总行数（count）

```sql
 select count(*) cnt from score;
```

- 求分数的最大值（max）

```sql
select max(s_score) from score;
```

- 求分数的最小值（min）

```sql
select min(s_score) from score;
```

- 求分数的总和（sum）

```sql
select sum(s_score) from score;
```

- 求分数的平均值（avg）

```sql
select avg(s_score) from score;
```

#### 8.1.4 limit 语句

- imit子句用于限制返回的行数。

```sql
 select  * from score limit 5;
```

#### 8.1.5 where 语句

- 使用 where 子句，将不满足条件的行过滤掉
- ==where 子句紧随from子句==
- 案例实操

```sql
select * from score where s_score > 60;
```

#### 8.1.6 算术运算符

| 运算符 | 描述           |
| ------ | -------------- |
| A+B    | A和B 相加      |
| A-B    | A减去B         |
| A*B    | A和B 相乘      |
| A/B    | A除以B         |
| A%B    | A对B取余       |
| A&B    | A和B按位取与   |
| A\|B   | A和B按位取或   |
| A^B    | A和B按位取异或 |
| ~A     | A按位取反      |

#### 8.1.7 比较运算符

|         操作符          | 支持的数据类型 |                             描述                             |
| :---------------------: | :------------: | :----------------------------------------------------------: |
|           A=B           |  基本数据类型  |             如果A等于B则返回true，反之返回false              |
|          A<=>B          |  基本数据类型  | 如果A和B都为NULL，则返回true，其他的和等号（=）操作符的结果一致，如果任一为NULL则结果为NULL |
|       A<>B, A!=B        |  基本数据类型  | A或者B为NULL则返回NULL；如果A不等于B，则返回true，反之返回false |
|           A<B           |  基本数据类型  | A或者B为NULL，则返回NULL；如果A小于B，则返回true，反之返回false |
|          A<=B           |  基本数据类型  | A或者B为NULL，则返回NULL；如果A小于等于B，则返回true，反之返回false |
|           A>B           |  基本数据类型  | A或者B为NULL，则返回NULL；如果A大于B，则返回true，反之返回false |
|          A>=B           |  基本数据类型  | A或者B为NULL，则返回NULL；如果A大于等于B，则返回true，反之返回false |
| A [NOT] BETWEEN B AND C |  基本数据类型  | 如果A，B或者C任一为NULL，则结果为NULL。如果A的值大于等于B而且小于或等于C，则结果为true，反之为false。如果使用NOT关键字则可达到相反的效果。 |
|        A IS NULL        |  所有数据类型  |           如果A等于NULL，则返回true，反之返回false           |
|      A IS NOT NULL      |  所有数据类型  |          如果A不等于NULL，则返回true，反之返回false          |
|    IN(数值1, 数值2)     |  所有数据类型  |                  使用 IN运算显示列表中的值                   |
|     A [NOT] LIKE B      |  STRING 类型   | B是一个SQL下的简单正则表达式，如果A与其匹配的话，则返回true；反之返回false。B的表达式说明如下：‘x%’表示A必须以字母‘x’开头，‘%x’表示A必须以字母’x’结尾，而‘%x%’表示A包含有字母’x’,可以位于开头，结尾或者字符串中间。如果使用NOT关键字则可达到相反的效果。like不是正则，而是通配符 |
|  A RLIKE B, A REGEXP B  |  STRING 类型   | B是一个正则表达式，如果A与其匹配，则返回true；反之返回false。匹配使用的是JDK中的正则表达式接口实现的，因为正则也依据其中的规则。例如，正则表达式必须和整个字符串A相匹配，而不是只需与其字符串匹配。 |

#### 8.1.8 逻辑运算符

|  操作符  |  操作  |                   描述                    |
| :------: | :----: | :---------------------------------------: |
| A AND  B | 逻辑并 |    如果A和B都是true则为true，否则false    |
| A  OR  B | 逻辑或 | 如果A或B或两者都是true则为true，否则false |
|  NOT  A  | 逻辑否 |      如果A为false则为true,否则false       |

### 8.2 分组

#### 8.2.1 Group By 语句

- Group By语句通常会和==聚合函数==一起使用，按照一个或者多个列对结果进行分组，然后对每个组执行聚合操作。

- 案例实操：

  - 计算每个学生的平均分数

  ```sql
  select s_id, avg(s_score) from score group by s_id;
  ```

  - 计算每个学生最高的分数

  ```sql
  select s_id, max(s_score) from score group by s_id;
  ```

#### 8.2.2 Having语句

- having 与 where 不同点

  - where针对==表中的列发挥作用==，查询数据；==having针对查询结果中的列==发挥作用，筛选数据
  - where后面==不能写分组函数==，而having后面可以==使用分组函数==
  - having只用于group by分组统计语句

- 案例实操

  - 求每个学生的平均分数

  ```sql
  select s_id, avg(s_score) from score group by s_id;
  ```

  - 求每个学生平均分数大于60的人

  ```sql
  select s_id, avg(s_score) as avgScore from score group by s_id having avgScore > 60;
  ```

### 8.3 join语句

#### 8.3.1 等值 join

- Hive支持通常的SQL JOIN语句，但是只支持等值连接，==不支持非等值连接==。

- 案例实操

  - 根据学生和成绩表，查询学生姓名对应的成绩

  ```sql
  select * from stu left join score on stu.id = score.s_id
  ```

#### 8.3.2 表的别名

- 好处

  - 使用别名可以简化查询。
  - 使用表名前缀可以提高执行效率。

- 案例实操：合并老师与课程表

  ```sql
  -- hive当中创建course表并加载数据
  create table course (c_id string, c_name string, t_id string) 
  row format delimited fields terminated by '\t';
  
  load data local inpath '/kkb/install/hivedatas/course.csv' overwrite into table course;
  
  select * from teacher t join course c on t.t_id = c.t_id;
  ```

#### 8.3.3 内连接 inner join

- 内连接：只有进行连接的两个表中都存在与连接条件相匹配的数据才会被保留下来。

  - join默认是inner  join

- 案例实操

  ```sql
  select * from teacher t inner join course c  on t.t_id = c.t_id;
  ```

#### 8.3.4 左外连接 left outer join

- 左外连接：

  - join操作符==左边表中==符合where子句的所有记录将会被返回。
  - 右边表的指定字段没有符合条件的值的话，那么就使用null值替代。

- 案例实操：查询老师对应的课程

  ```sql
   select * from teacher t left outer join course c on t.t_id = c.t_id;
  ```

#### 8.3.5 右外连接 right outer join

- 右外连接：

  - join操作符==右边表中==符合where子句的所有记录将会被返回。
  - 左边表的指定字段没有符合条件的值的话，那么就使用null值替代。

- 案例实操

  ```sql
   select * from teacher t right outer join course c on t.t_id = c.t_id;
  ```

#### 8.3.6 满外连接 full outer join

- 满外连接：

  - 将会返回==所有表中==符合where语句条件的所有记录。
  - 如果任一表的指定字段没有符合条件的值的话，那么就使用null值替代。

- 案例实操

  ```sql
  select * from teacher t full outer join course c on t.t_id = c.t_id;
  ```

#### 8.3.7 多表连接 

- **多个表使用join进行连接**

- ==注意：==连接 n个表，至少需要n-1个连接条件。例如：连接三个表，至少需要两个连接条件。

- 案例实操

  - 多表连接查询，查询老师对应的课程，以及对应的分数，对应的学生

  ```sql
  select * from teacher t 
  left join course c on t.t_id = c.t_id 
  left join score s on c.c_id = s.c_id 
  left join stu on s.s_id = stu.id;
  ```

### 8.4 排序

#### 8.4.1 order by 全局排序

- 全局排序，只有一个reduce

- 使用 ORDER BY 子句排序

  - asc ( ascend) 升序 (默认)
  - desc (descend) 降序

- order by 子句在select语句的结尾

- 案例实操

  - 查询学生的成绩，并按照分数降序排列

  ```sql
  select * from score s order by s_score desc ;
  ```

#### 8.4.2 按照别名排序

- 按照学生分数的平均值排序

  ```sql
  select s_id, avg(s_score) avgscore from score group by s_id order by avgscore desc; 
  ```

#### 8.4.3 每个MapReduce内部排序（Sort By）局部排序

- sort by：每个reducer内部进行排序，对全局结果集来说不是排序。

- 设置reduce个数

  ```
  set mapreduce.job.reduces=3;
  ```

- 查看reduce的个数

  ```sql
  set mapreduce.job.reduces;
  ```

- 查询成绩按照成绩降序排列

  ```sql
  select * from score s sort by s.s_score;
  ```

- 将查询结果导入到文件中（按照成绩降序排列）

  ```sql
  insert overwrite local directory '/kkb/install/hivedatas/sort' select * from score s sort by s.s_score;
  ```

#### 8.4.4 distribute by 分区排序

- distribute by：

  - 类似MR中partition，==采集hash算法，在map端将查询的结果中hash值相同的结果分发到对应的reduce文件中==。
  - 结合sort by使用。

- 注意

  - Hive要求 **distribute by** 语句要写在 **sort by** 语句之前。

- 案例实操

  - 先按照学生 sid 进行分区，再按照学生成绩进行排序

  - 设置reduce的个数

  ```sql
  set mapreduce.job.reduces=3;
  ```

    - 通过distribute by  进行数据的分区,，将不同的sid 划分到对应的reduce当中去

    ```sql
  insert overwrite local directory '/kkb/install/hivedatas/distribute' select * from score distribute by s_id sort by s_score;
    ```

#### 8.4.5 cluster by

- 当distribute by和sort by字段相同时，可以使用cluster by方式代替

- 除了distribute by 的功能外，还会对该字段进行排序，所以cluster by = distribute by + sort by

  ```sql
  --以下两种写法等价
  insert overwrite local directory '/kkb/install/hivedatas/distribute_sort' select * from score distribute by s_score sort by s_score;
  
  insert overwrite local directory '/kkb/install/hivedatas/cluster' select * from score  cluster by s_score;
  ```

## 9. Hive的参数传递

### 9.1 Hive命令行

- 查看hive命令的参数

```shell
[hadoop@node03 ~]$ hive -help
```

语法结构

- `hive [-hiveconf x=y]* [<-i filename>]* [<-f filename>|<-e query-string>] [-S]`

- 说明：
  - -i 从文件初始化HQL。
  - -e从命令行执行指定的HQL 
  - -f 执行HQL脚本 
  - -v 输出执行的HQL语句到控制台 
  - -p <port> connect to Hive Server on port number 
  - -hiveconf x=y Use this to set hive/hadoop configuration variables.  设置hive运行时候的参数配置

### 9.2 Hive参数配置方式

- Hive参数大全：
- [官网地址](<https://cwiki.apache.org/confluence/display/Hive/Configuration+Properties>)
- 开发Hive应用时，不可避免地需要设定Hive的参数。设定Hive的参数可以调优HQL代码的执行效率，或帮助定位问题。然而实践中经常遇到的一个问题是，为什么设定的参数没有起作用？这通常是错误的设定方式导致的。

- **对于一般参数，有以下三种设定方式：**
  - 配置文件  hive-site.xml

  - 命令行参数  启动hive客户端的时候可以设置参数

  - 参数声明   进入客户端以后设置的一些参数  set  

- **配置文件**
  - Hive的配置文件包括
    - 用户自定义配置文件：$HIVE_CONF_DIR/hive-site.xml 
    - 默认配置文件：$HIVE_CONF_DIR/hive-default.xml 
  - 用户自定义配置会覆盖默认配置。
  - 另外，Hive也会读入Hadoop的配置，因为Hive是作为Hadoop的客户端启动的，Hive的配置会覆盖Hadoop的配置。配置文件的设定对本机启动的所有Hive进程都有效。

- **命令行参数**：启动Hive（客户端或Server方式）时，可以在命令行添加-hiveconf param=value来设定参数，例如：

```shell
bin/hive --hiveconf hive.root.logger=INFO,console
```

​		这一设定只对本次启动的Session（对于Server方式启动，则是所有请求的Sessions）有效。

- **参数声明**：可以在HQL中使用SET关键字设定参数，例如：

```sql
-- 设置mr中reduce个数
set mapreduce.job.reduces=100;
```

​		这一设定的作用域也是session级的。

- 上述三种设定方式的优先级依次递增。
  - 即参数声明覆盖命令行参数，命令行参数覆盖配置文件设定。
  - 注意某些系统级的参数，例如log4j相关的设定，必须用前两种方式设定，因为那些参数的读取在Session建立以前已经完成了。

```
参数声明  >   命令行参数   >  配置文件参数（hive）
```

### 9.3 使用变量传递参数

- 实际工作当中，我们一般都是将hive的hql语法开发完成之后，就写入到一个脚本里面去，然后定时的通过命令 hive  -f  去执行hive的语法即可
- 然后通过定义变量来传递参数到hive的脚本当中去，那么我们接下来就来看看如何使用hive来传递参数。

- hive0.9以及之前的版本是不支持传参
- hive1.0版本之后支持  hive -f 传递参数

- 在hive当中我们一般可以使用==hivevar==或者==hiveconf==来进行参数的传递

#### 9.3.1 hiveconf使用说明

- hiveconf用于定义HIVE**执行上下文的属性**(配置参数)，可覆盖覆盖hive-site.xml（hive-default.xml）中的参数值，如用户执行目录、日志打印级别、执行队列等。例如我们可以使用hiveconf来覆盖我们的hive属性配置，

- hiveconf变量取值必须要使用hiveconf作为前缀参数，具体格式如下:

```sql
${hiveconf:key} 
bin/hive --hiveconf "mapred.job.queue.name=root.default"
```

#### 9.3.2 hivevar使用说明

- hivevar用于定义HIVE**运行时的变量**替换，类似于JAVA中的“PreparedStatement”，与\${key}配合使用或者与 ${hivevar:key}

- 对于hivevar取值可以不使用前缀hivevar，具体格式如下：

```sql
-- 使用前缀:
 ${hivevar:key}
-- 不使用前缀:
 ${key}
hive --hivevar  name=zhangsan    

${hivevar:name}  
也可以这样取值  ${name}
```

#### 9.3.3 define使用说明

- define与hivevar用途完全一样，还有一种简写“-d

```sql
hive --hiveconf "mapred.job.queue.name=root.default" -d my="201912" --database myhive

-- 执行SQL
hive > select * from myhive.score2 where concat(year, month) = ${my} limit 5;
```

#### 9.3.4 hiveconf与hivevar使用实战

- 需求：hive当中执行以下hql语句，并将'201807'、'80'、'03'用参数的形式全部都传递进去

```sql
select * from student left join score on student.s_id = score.s_id where score.month = '201807' and score.s_score > 80 and score.c_id = 03;
```

##### 第一步：创建student表并加载数据

```sql
hive (myhive)> create external table student
(s_id string, s_name string, s_birth string, s_sex string) row format delimited
fields terminated by '\t';

hive (myhive)> load data local inpath '/kkb/install/hivedatas/student.csv' overwrite into table student;
```

##### 第二步：定义hive脚本

- 开发hql脚本，并使用hiveconf和hivevar进行参数传入

- node03执行以下命令定义hql脚本

```sql
cd /kkb/instal/hivedatas

vim hivevariable.hql

use myhive;
select * from student left join score on student.s_id = score.s_id where score.month = ${hiveconf:month} and score.s_score > ${hivevar:s_score} and score.c_id = ${c_id};   
```

##### 第三步：调用hive脚本并传递参数

- student、score表内容如下

```
hive (myhive)> select * from student;
OK
+---------------+-----------------+------------------+-------------------+--+
| student.s_id  | student.s_name  | student.s_birth  | student.s_gender  |
+---------------+-----------------+------------------+-------------------+--+
| 1             | 赵雷             | 1990-01-01       | 男                |
| 2             | 钱电             | 1990-12-21       | 男                |
| 3             | 孙风             | 1990-05-20       | 男                |
| 4             | 李云             | 1990-08-06       | 男                |
| 5             | 周梅             | 1991-12-01       | 女                |
| 6             | 吴兰             | 1992-03-01       | 女                |
| 7             | 郑竹             | 1989-07-01       | 女                |
| 8             | 王菊             | 1990-01-20       | 女                |
+---------------+-----------------+------------------+-------------------+--+
Time taken: 0.048 seconds, Fetched: 8 row(s)
hive (myhive)> select * from score;
OK
+-------------+-------------+-------------+--+
| score.s_id  | score.c_id  | score.s_score  |
+-------------+-------------+----------------+--+
| 1           | 1           | 80             |
| 1           | 2           | 90             |
| 1           | 3           | 99             |
| 2           | 1           | 70             |
| 2           | 2           | 60             |
| 2           | 3           | 80             |
| 3           | 1           | 80             |
| 3           | 2           | 80             |
| 3           | 3           | 80             |
| 4           | 1           | 50             |
| 4           | 2           | 30             |
| 4           | 3           | 20             |
| 5           | 1           | 76             |
| 5           | 2           | 87             |
| 6           | 1           | 31             |
| 6           | 3           | 34             |
| 7           | 2           | 89             |
| 7           | 3           | 98             |
+-------------+-------------+----------------+--+
```

- node03执行以下命令并

```sql
hive --hiveconf month=201912 --hivevar s_score=80 --hivevar c_id=03  -f /kkb/install/hivedatas/hivevariable.hql

+-----------------+-------------+-----------+-------+-------+----------+--+
| s_id  | s_name  | s_birth     | s_gender  | s_id  | c_id  | s_score  |
+-----------------+-------------+-----------+-------+-------+----------+--+
| 1     | 赵雷     | 1990-01-01  | 男        | 1     | 3     | 99       |
| 7     | 郑竹     | 1989-07-01  | 女        | 7     | 3     | 98       |
+-------+---------+--------------+----------+-------+-------+----------+--+
```



## 10. Hive的常用函数

### 10.1 系统内置函数

```sql
1．查看系统自带的函数
hive> show functions;
2．显示自带的函数的用法
hive> desc function upper;
3．详细显示自带的函数的用法
hive> desc function extended upper;
```

### 10.2 数值计算

#### 1、取整函数: round 

- **语法**: round(double a)
- **返回值**: BIGINT
- **说明**: 返回double类型的整数值部分 （遵循四舍五入）

```sql
hive> select round(3.1415926) from tableName;
3
hive> select round(3.5) from tableName;
4
hive> create table tableName as select round(9542.158) from tableName;
```

#### 2、指定精度取整函数: round 

- **语法**: round(double a, int d)
- **返回值**: DOUBLE
- **说明**: 返回指定精度d的double类型

```sql
hive> select round(3.1415926, 4) from tableName;
3.1416
```

#### 3、向下取整函数: floor 

- **语法**: floor(double a)
- **返回值**: BIGINT
- **说明**: 返回等于或者小于该double变量的最大的整数

```sql
hive> select floor(3.1415926) from tableName;
3
hive> select floor(25) from tableName;
25
```

#### 4、向上取整函数: ceil 

- **语法**: ceil(double a)
- **返回值**: BIGINT
- **说明**: 返回等于或者大于该double变量的最小的整数

```sql
hive> select ceil(3.1415926) from tableName;
4
hive> select ceil(46) from tableName;
46
```

#### 5、向上取整函数: ceiling 

- **语法**: ceiling(double a)
- **返回值**: BIGINT
- **说明**: 与ceil功能相同

```sql
hive> select ceiling(3.1415926) from tableName;
4
hive> select ceiling(46) from tableName;
46
```

#### 6、取随机数函数: rand

- **语法**: rand(), rand(int seed)
- **返回值**: double
- **说明**: 返回一个0到1范围内的随机数。如果指定种子seed，则会等到一个稳定的随机数序列

```sql
hive> select rand() from tableName;
0.5577432776034763
hive> select rand() from tableName;
0.6638336467363424
hive> select rand(100) from tableName;
0.7220096548596434
hive> select rand(100) from tableName;
0.7220096548596434
```

### 10.3 日期函数

#### 1、UNIX时间戳转日期函数: from_unixtime  

- **语法**: from_unixtime(bigint unixtime[, string format])
- **返回值**: string
- **说明**: 转化UNIX时间戳（从1970-01-01 00:00:00 UTC到指定时间的秒数）到当前时区的时间格式

```sql
hive> select from_unixtime(1323308943, 'yyyyMMdd') from tableName;
20111208
```

#### 2、获取当前UNIX时间戳函数: unix_timestamp

- **语法**: unix_timestamp()
- **返回值**: bigint
- **说明**: 获得当前时区的UNIX时间戳

```sql
hive> select unix_timestamp() from tableName;
1323309615
```

#### 3、日期转UNIX时间戳函数: unix_timestamp 

- **语法**: unix_timestamp(string date)
- **返回值**: bigint
- **说明**: 转换格式为"yyyy-MM-dd HH:mm:ss"的日期到UNIX时间戳。如果转化失败，则返回0。

```sql
hive> select unix_timestamp('2011-12-07 13:01:03') from tableName;
1323234063
```

#### 4、指定格式日期转UNIX时间戳函数: unix_timestamp 

- **语法**: unix_timestamp(string date, string pattern)
- **返回值**: bigint
- **说明**: 转换pattern格式的日期到UNIX时间戳。如果转化失败，则返回0。

```sql
hive> select unix_timestamp('20111207 13:01:03','yyyyMMdd HH:mm:ss') from tableName;
1323234063
```

#### 5、日期时间转日期函数: to_date  

- **语法**: to_date(string datetime)
- **返回值**: string
- **说明**: 返回日期时间字段中的日期部分。

```sql
hive> select to_date('2011-12-08 10:03:01') from tableName;
2011-12-08
```

#### 6、日期转年函数: year 

- **语法**: year(string date)
- **返回值**: int
- **说明**: 返回日期中的年。

```sql
hive> select year('2011-12-08 10:03:01') from tableName;
2011
hive> select year('2012-12-08') from tableName;
2012
```

#### 7、日期转月函数: month 

- **语法**: month (string date)
- **返回值**: int
- **说明**: 返回date或datetime中的月份。

```sql
hive> select month('2011-12-08 10:03:01') from tableName;
12
hive> select month('2011-08-08') from tableName;
8
```

#### 8、日期转天函数: day 

- **语法**: day (string date)
- **返回值**: int
- **说明**: 返回日期中的天。

```sql
hive> select day('2011-12-08 10:03:01') from tableName;
8
hive> select day('2011-12-24') from tableName;
24
```

#### 9、日期转小时函数: hour 

- **语法**: hour (string date)
- **返回值**: int
- **说明**: 返回日期中的小时。

```sql
hive> select hour('2011-12-08 10:03:01') from tableName;
10
```

#### 10、日期转分钟函数: minute

- **语法**: minute (string date)
- **返回值**: int
- **说明**: 返回日期中的分钟。

```sql
hive> select minute('2011-12-08 10:03:01') from tableName;
3

-- second 返回秒
hive> select second('2011-12-08 10:03:01') from tableName;
1
```

#### 12、日期转周函数: weekofyear

- **语法**: weekofyear (string date)
- **返回值**: int
- **说明**: 返回日期在当前的周数。

```sql
hive> select weekofyear('2011-12-08 10:03:01') from tableName;
49
```

#### 13、日期比较函数: datediff 

- **语法**: datediff(string enddate, string startdate)
- **返回值**: int
- **说明**: 返回结束日期减去开始日期的天数。

```sql
hive> select datediff('2012-12-08','2012-05-09') from tableName;
213
```

#### 14、日期增加函数: date_add 

- **语法**: date_add(string startdate, int days)
- **返回值**: string
- **说明**: 返回开始日期startdate增加days天后的日期。

```sql
hive> select date_add('2012-12-08',10) from tableName;
2012-12-18
```

#### 15、日期减少函数: date_sub 

- **语法**: date_sub (string startdate, int days)
- **返回值**: string
- **说明**: 返回开始日期startdate减少days天后的日期。

```sql
hive> select date_sub('2012-12-08',10) from tableName;
2012-11-28
```

### 10.4 条件函数（重点）

#### 1、If函数: if 

- **语法**: if(boolean testCondition, T valueTrue, T valueFalseOrNull)
- **返回值**: T
- **说明**: 当条件testCondition为TRUE时，返回valueTrue；否则返回valueFalseOrNull

```sql
hive> select if(1=2,100,200) from tableName;
200
hive> select if(1=1,100,200) from tableName;
100
```

#### 2、非空查找函数: COALESCE

- **语法**: COALESCE(T v1, T v2, …)
- **返回值**: T
- **说明**: 返回参数中的第一个非空值；如果所有值都为NULL，那么返回NULL

```sql
hive> select COALESCE(null,'100','50') from tableName;
100
```

#### 3、条件判断函数：CASE 

- **语法**: CASE a WHEN b THEN c [WHEN d THEN e]* [ELSE f] END
- **返回值**: T
- **说明**：如果a等于b，那么返回c；如果a等于d，那么返回e；否则返回f

```sql
hive> select case 100 when 50 then 'tom' when 100 then 'mary' else 'tim' end from tableName;
mary
hive> Select case 200 when 50 then 'tom' when 100 then 'mary' else 'tim' end from tableName;
tim
```

#### 4、条件判断函数：CASE

- **语法**: CASE WHEN a THEN b [WHEN c THEN d]* [ELSE e] END
- **返回值**: T
- **说明**：如果a为TRUE,则返回b；如果c为TRUE，则返回d；否则返回e

```sql
hive> select case when 1=2 then 'tom' when 2=2 then 'mary' else 'tim' end from tableName;
mary
hive> select case when 1=1 then 'tom' when 2=2 then 'mary' else 'tim' end from tableName;
tom
```

### 10.5 字符串函数

#### 1、字符串长度函数：length

- **语法**: length(string A)
- **返回值**: int
- **说明**：返回字符串A的长度

```sql
hive> select length('abcedfg') from tableName;
```

#### 2、字符串反转函数：reverse

- **语法**: reverse(string A)
- **返回值**: string
- **说明**：返回字符串A的反转结果

```sql
hive> select reverse('abcedfg') from tableName;
gfdecba
```

#### 3、字符串连接函数：concat

- **语法**: concat(string A, string B…)
- **返回值**: string
- **说明**：返回输入字符串连接后的结果，支持任意个输入字符串

```sql
hive> select concat('abc','def','gh') from tableName;
abcdefgh
```

#### 4、字符串连接并指定字符串分隔符：concat_ws

- **语法**: concat_ws(string SEP, string A, string B…)
- **返回值**: string
- **说明**：返回输入字符串连接后的结果，SEP表示各个字符串间的分隔符

```sql
hive> select concat_ws(',','abc','def','gh') from tableName;
abc,def,gh
```

#### 5、字符串截取函数：substr

- **语法**: substr(string A, int start), substring(string A, int start)
- **返回值**: string
- **说明**：返回字符串A从start位置到结尾的字符串

```sql
hive> select substr('abcde',3) from tableName;
cde
hive> select substring('abcde',3) from tableName;
cde
hive> select substr('abcde',-1) from tableName;  （和ORACLE相同）
e
```

#### 6、字符串截取函数：substr, substring 

- **语法**: substr(string A, int start, int len),substring(string A, int start, int len)
- **返回值**: string
- **说明**：返回字符串A从start位置开始，长度为len的字符串

```sql
hive> select substr('abcde',3,2) from tableName;
cd
hive> select substring('abcde',3,2) from tableName;
cd
hive>select substring('abcde',-3,2) from tableName;
cd
```

#### 7、字符串转大写函数：upper, ucase  

- **语法**: upper(string A) ucase(string A)
- **返回值**: string
- **说明**：返回字符串A的大写格式

```sql
hive> select upper('abSEd') from tableName;
ABSED
hive> select ucase('abSEd') from tableName;
ABSED
```

#### 8、字符串转小写函数：lower, lcase  

- **语法**: lower(string A) lcase(string A)
- **返回值**: string
- **说明**：返回字符串A的小写格式

```sql
hive> select lower('abSEd') from tableName;
absed
hive> select lcase('abSEd') from tableName;
absed
```

#### 9、去空格函数：trim 

- **语法**: trim(string A)
- **返回值**: string
- **说明**：去除字符串两边的空格

```sql
hive> select trim(' ab c ') from tableName;
ab c
```

#### 10、url解析函数  parse_url

- **语法**:
  parse_url(string urlString, string partToExtract [, string keyToExtract])
- **返回值**: string
- **说明**：返回URL中指定的部分。partToExtract的有效值为：HOST, PATH,
  QUERY, REF, PROTOCOL, AUTHORITY, FILE, and USERINFO.

```sql
hive> select parse_url
('https://www.tableName.com/path1/p.php?k1=v1&k2=v2#Ref1', 'HOST') 
from tableName;
www.tableName.com 
hive> select parse_url
('https://www.tableName.com/path1/p.php?k1=v1&k2=v2#Ref1', 'QUERY', 'k1')
 from tableName;
v1
```

#### 11、json解析  get_json_object 

- **语法**: get_json_object(string json_string, string path)
- **返回值**: string
- **说明**：解析json的字符串json_string,返回path指定的内容。如果输入的json字符串无效，那么返回NULL。

```sql
hive> select  get_json_object('{"store":{"fruit":\[{"weight":8,"type":"apple"},{"weight":9,"type":"pear"}], "bicycle":{"price":19.95,"color":"red"} },"email":"amy@only_for_json_udf_test.net","owner":"amy"}','$.owner') from tableName;
```

```json
{
  "store": {
    "fruit": [{
      "weight": 8,
      "type": "apple"
    }, {
      "weight": 9,
      "type": "pear"
    }], 
    "bicycle": {
      "price": 19.95,
      "color":"red"
    }
  },
  "email": "amy@only_for_json_udf_test.net",
  "owner": "amy"
}
```



#### 12、重复字符串函数：repeat 

- **语法**: repeat(string str, int n)
- **返回值**: string
- **说明**：返回重复n次后的str字符串

```sql
hive> select repeat('abc', 5) from tableName;
abcabcabcabcabc
```

#### 13、分割字符串函数: split   

- **语法**: split(string str, string pat)
- **返回值**: array
- **说明**: 按照pat字符串分割str，会返回分割后的字符串数组

```sql
hive> select split('abtcdtef','t') from tableName;
["ab","cd","ef"]
```

### 10.6 集合统计函数

#### 1、个数统计函数: count  

- **语法**: count(*), count(expr), count(DISTINCT expr[, expr_.])
- **返回值**：Int
- **说明**: count(*)统计检索出的行的个数，包括NULL值的行；count(expr)返回指定字段的非空值的个数；count(DISTINCT
  expr[, expr_.])返回指定字段的不同的非空值的个数

```sql
hive> select count(*) from tableName;
20
hive> select count(distinct t) from tableName;
10
```

#### 2、总和统计函数: sum 

- **语法**: sum(col), sum(DISTINCT col)
- **返回值**: double
- **说明**: sum(col)统计结果集中col的相加的结果；sum(DISTINCT col)统计结果中col不同值相加的结果

```sql
hive> select sum(t) from tableName;
100
hive> select sum(distinct t) from tableName;
70
```

#### 3、平均值统计函数: avg   

- **语法**: avg(col), avg(DISTINCT col)
- **返回值**: double
- **说明**: avg(col)统计结果集中col的平均值；avg(DISTINCT col)统计结果中col不同值相加的平均值

```sql
hive> select avg(t) from tableName;
50
hive> select avg (distinct t) from tableName;
30
```

#### 4、最小值统计函数: min 

- **语法**: min(col)
- **返回值**: double
- **说明**: 统计结果集中col字段的最小值

```sql
hive> select min(t) from tableName;
20
```

#### 5、最大值统计函数: max  

- **语法**: max(col)
- **返回值**: double
- **说明**: 统计结果集中col字段的最大值

```sql
hive> select max(t) from tableName;
120
```

### 10.7 复合类型构建函数

#### 1、Map类型构建: map  

- **语法**: map (key1, value1, key2, value2, …)
- **说明**：根据输入的key和value对构建map类型

```sql
-- 建表
create table score_map(name string, score map<string, int>)
row format delimited fields terminated by '\t' 
collection items terminated by ',' 
map keys terminated by ':';

-- 创建数据内容如下并加载数据
cd /kkb/install/hivedatas/
vim score_map.txt

zhangsan	数学:80,语文:89,英语:95
lisi	语文:60,数学:80,英语:99

-- 加载数据到hive表当中去
load data local inpath '/kkb/install/hivedatas/score_map.txt' overwrite into table score_map;

-- map结构数据访问：
-- 获取所有的value：
select name,map_values(score) from score_map;

-- 获取所有的key：
select name,map_keys(score) from score_map;

-- 按照key来进行获取value值
select name,score["数学"]  from score_map;

-- 查看map元素个数
select name,size(score) from score_map;

-- 构建一个map
select map(1, 'zs', 2, 'lisi');
```

```bash
0: jdbc:hive2://node03:10000> select map(1, 'zs', 2, 'lisi');
INFO  : Compiling command(queryId=hadoop_20200225183232_dfcdbb55-6e14-46c3-ab4f-956831e196dc): select map(1, 'zs', 2, 'lisi')
INFO  : Semantic Analysis Completed
INFO  : Returning Hive schema: Schema(fieldSchemas:[FieldSchema(name:_c0, type:map<int,string>, comment:null)], properties:null)
INFO  : Completed compiling command(queryId=hadoop_20200225183232_dfcdbb55-6e14-46c3-ab4f-956831e196dc); Time taken: 0.158 seconds
INFO  : Concurrency mode is disabled, not creating a lock manager
INFO  : Executing command(queryId=hadoop_20200225183232_dfcdbb55-6e14-46c3-ab4f-956831e196dc): select map(1, 'zs', 2, 'lisi')
INFO  : Completed executing command(queryId=hadoop_20200225183232_dfcdbb55-6e14-46c3-ab4f-956831e196dc); Time taken: 0.001 seconds
INFO  : OK
+--------------------+--+
|        _c0         |
+--------------------+--+
| {1:"zs",2:"lisi"}  |
+--------------------+--+
1 row selected (0.254 seconds)
```



#### 2、Struct类型构建: struct

- **语法**: struct(val1, val2, val3, …)
- **说明**：根据输入的参数构建结构体struct类型，似于C语言中的结构体，内部数据通过X.X来获取，假设我
- 数据格式是这样的，电影ABC，有1254人评价过，打分为7.4分

```sql
-- 创建struct表
hive> create table movie_score(name string, info struct<number:int,score:float>)
row format delimited fields terminated by "\t"  
collection items terminated by ":"; 

-- 加载数据
cd /kkb/install/hivedatas/
vim struct.txt

-- 电影ABC，有1254人评价过，打分为7.4分
ABC	1254:7.4  
DEF	256:4.9  
XYZ	456:5.4

-- 加载数据
load data local inpath '/kkb/install/hivedatas/struct.txt' overwrite into table movie_score;

-- hive当中查询数据
hive> select * from movie_score;  
hive> select info.number, info.score from movie_score;  
OK  
1254    7.4  
256     4.9  
456     5.4  

-- 构建一个struct
select struct(1, 'anzhulababy', 'moon', 1.68);
```

```bash
0: jdbc:hive2://node03:10000> select struct(1, 'anzhulababy', 'moon', 1.68);
INFO  : Compiling command(queryId=hadoop_20200225183434_6d15e1c1-fc75-4112-98f8-18116d7e1018): select struct(1, 'anzhulababy', 'moon', 1.68)
INFO  : Semantic Analysis Completed
INFO  : Returning Hive schema: Schema(fieldSchemas:[FieldSchema(name:_c0, type:struct<col1:int,col2:string,col3:string,col4:double>, comment:null)], properties:null)
INFO  : Completed compiling command(queryId=hadoop_20200225183434_6d15e1c1-fc75-4112-98f8-18116d7e1018); Time taken: 0.234 seconds
INFO  : Concurrency mode is disabled, not creating a lock manager
INFO  : Executing command(queryId=hadoop_20200225183434_6d15e1c1-fc75-4112-98f8-18116d7e1018): select struct(1, 'anzhulababy', 'moon', 1.68)
INFO  : Completed executing command(queryId=hadoop_20200225183434_6d15e1c1-fc75-4112-98f8-18116d7e1018); Time taken: 0.001 seconds
INFO  : OK
+-----------------------------------------------------------+--+
|                        _c0                                |
+-----------------------------------------------------------+--+
| {"col1":1,"col2":"anzhulababy","col3":"moon","col4":1.68} |
+-----------------------------------------------------------+--+
1 row selected (0.277 seconds)
```



#### 3、Array类型构建: array

- **语法**: array(val1, val2, …)
- **说明**：根据输入的参数构建数组array类型

```sql
hive> create table person(name string, work_locations array<string>)
ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t'
COLLECTION ITEMS TERMINATED BY ',';

-- 加载数据到person表当中去
cd /kkb/install/hivedatas/
vim person.txt

-- 数据内容格式如下
biansutao	beijing,shanghai,tianjin,hangzhou
linan	changchun,chengdu,wuhan

-- 加载数据
hive > load  data local inpath '/kkb/install/hivedatas/person.txt' overwrite into table person;

-- 查询所有数据数据
hive > select * from person;

-- 按照下表索引进行查询
hive > select work_locations[0] from person;

-- 查询所有集合数据
hive  > select work_locations from person; 

-- 查询元素个数
hive >  select size(work_locations) from person;   

-- 构建array
select array(1, 2, 1);
select array(1, 'a', 1.0);
select array(1, 2, 1.0);
```

### 10.8 复杂类型长度统计函数

#### 1、Map类型长度函数: size(Map<k .V>)

- **语法**: size(Map<k .V>)
- **返回值**: int
- **说明**: 返回map类型的长度

```sql
hive> select size(map(1, 'zs', 2, 'anzhulababy')) from tableName;
2
```

#### 2、array类型长度函数: size(Array<T>)

- **语法**: size(Array<T>)
- **返回值**: int
- **说明**: 返回array类型的长度

```sql
hive> select size(t) from arr_table2;
4
```

#### 3、类型转换函数  

- **类型转换函数**: cast
- **语法**: cast(expr as <type>)
- **返回值**: Expected "=" to follow "type"
- **说明**: 返回转换后的数据类型

```sql
hive> select cast('1' as bigint) from tableName;
1
```

### 10.9 行转列

#### 1、相关函数说明

- CONCAT(string A/col, string B/col…)：返回输入字符串连接后的结果，支持任意个输入字符串;

- CONCAT_WS(separator, str1, str2,...)：它是一个特殊形式的 CONCAT()。
  - 第一个参数剩余参数间的分隔符。分隔符可以是与剩余参数一样的字符串。如果分隔符是 NULL，返回值也将为 NULL。
  - 这个函数会跳过分隔符参数后的任何 NULL 和空字符串。分隔符将被加到被连接的字符串之间;

- COLLECT_SET(col)：函数只接受基本数据类型，它的主要作用是将某字段的值进行去重汇总，产生**array**类型字段。

#### 2、数据准备

- 数据准备

| name       | constellation | blood_type |
| ---------- | ------------- | ---------- |
| 孙悟空     | 白羊座        | A          |
| 老王       | 射手座        | A          |
| 宋宋       | 白羊座        | B          |
| 猪八戒     | 白羊座        | A          |
| 按住啦baby | 射手座        | A          |

#### 3、需求

- 把星座和血型一样的人归类到一起。结果如下：

```
射手座,A            老王|按住啦baby
白羊座,A            孙悟空|猪八戒
白羊座,B            宋宋
```

#### 4、创建表数据文件

- node03服务器执行以下命令创建文件，注意数据使用\t进行分割

```shell
cd /kkb/install/hivedatas
vim constellation.txt
```

```
孙悟空	白羊座	A
老王	射手座	A
宋宋	白羊座	B       
猪八戒	白羊座	A
凤姐	射手座	A
```

#### 5、创建hive表并导入数据

- 创建hive表并加载数据

```sql
hive (hive_explode)> create table person_info(name string, constellation string,  blood_type string) row format delimited fields terminated by "\t";
```

- 加载数据

```sql
hive (hive_explode)> load data local inpath '/kkb/install/hivedatas/constellation.txt' into table person_info;
```

#### 6、按需求查询数据

```sql
hive (hive_explode)> select t1.base, concat_ws('|', collect_set(t1.name)) name 
from    
(select name, concat(constellation, "," , blood_type) base from person_info) t1 
group by t1.base;
```

### 10.10 列转行

#### 1、函数说明

- EXPLODE(col)：将hive一列中复杂的array或者map结构拆分成多行。

- LATERAL VIEW
  - 用法：LATERAL VIEW udtf(expression) tableAlias AS columnAlias
  - 解释：用于和split, explode等UDTF一起使用，它能够将一列数据拆成多行数据，在此基础上可以对拆分后的数据进行聚合。

#### 2、数据准备

- 数据内容如下，字段之间都是使用\t进行分割

```shell
cd /kkb/install/hivedatas

vim movie.txt

《疑犯追踪》	悬疑,动作,科幻,剧情
《Lie to me》	悬疑,警匪,动作,心理,剧情
《战狼2》	战争,动作,灾难
```

#### 3、需求

- 将电影分类中的数组数据展开。结果如下：

```
《疑犯追踪》	悬疑
《疑犯追踪》	动作
《疑犯追踪》	科幻
《疑犯追踪》	剧情
《Lie to me》	悬疑
《Lie to me》	警匪
《Lie to me》	动作
《Lie to me》	心理
《Lie to me》	剧情
《战狼2》	战争
《战狼2》	动作
《战狼2》	灾难
```

#### 4、创建hive表并导入数据

- 创建hive表

```sql
hive (hive_explode)> create table movie_info(movie string, category array<string>) 
row format delimited fields terminated by "\t" 
collection items terminated by ",";
```

- 加载数据

```sql
load data local inpath "/kkb/install/hivedatas/movie.txt" into table movie_info;
```

#### 5、按需求查询数据

```sql
hive (hive_explode)> select movie, category_name from movie_info 
lateral view explode(category) table_tmp as category_name;
```

### 10.11 lateral view、explode练习

#### 1、explode函数将Map和Array字段数据进行拆分

- explode还可以用于将hive一列中复杂的array或者map结构拆分成多行

```sql
-- 需求：现在有数据格式如下
zhangsan	child1,child2,child3,child4	k1:v1,k2:v2
lisi	child5,child6,child7,child8	 k3:v3,k4:v4

-- 字段之间使用\t分割，需求将所有的child进行拆开成为一列
+----------+--+
| mychild  |
+----------+--+
| child1   |
| child2   |
| child3   |
| child4   |
| child5   |
| child6   |
| child7   |
| child8   |
+----------+--+

-- 将map的key和value也进行拆开，成为如下结果
+-----------+-------------+--+
| mymapkey  | mymapvalue  |
+-----------+-------------+--+
| k1        | v1          |
| k2        | v2          |
| k3        | v3          |
| k4        | v4          |
+-----------+-------------+--+
```

##### 第一步：创建hive数据库

- 创建hive数据库

```
hive (default)> create database hive_explode;
hive (default)> use hive_explode;
```

##### 第二步：创建hive表

```sql
hive (hive_explode)> create table hive_explode.t3(name string, children array<string>, address Map<string, string>) row format delimited fields terminated by '\t' collection items terminated by ','  map keys terminated by ':' stored as textFile;
```

##### 第三步：加载数据

- node03执行以下命令创建表数据文件

```sql
cd  /kkb/install/hivedatas/

vim maparray

-- 数据内容格式如下
zhangsan	child1,child2,child3,child4	k1:v1,k2:v2
lisi	child5,child6,child7,child8	k3:v3,k4:v4
```

- hive表当中加载数据

```sql
hive (hive_explode)> load data local inpath '/kkb/install/hivedatas/maparray' into table hive_explode.t3;
```

##### 第四步：使用explode将hive当中数据拆开

- 将array当中的数据拆分开

```sql
hive (hive_explode)> SELECT explode(children) AS myChild FROM hive_explode.t3;
```

- 将map当中的数据拆分开

```sql
hive (hive_explode)> SELECT explode(address) AS (myMapKey, myMapValue) FROM hive_explode.t3;
```

#### 2、使用explode拆分json字符串

- 需求：现在有一些数据格式如下：

```
a:shandong,b:beijing,c:hebei|1,2,3,4,5,6,7,8,9|[{"source":"7fresh","monthSales":4900,"userCount":1900,"score":"9.9"},{"source":"jd","monthSales":2090,"userCount":78981,"score":"9.8"},{"source":"jdmart","monthSales":6987,"userCount":1600,"score":"9.0"}]
```

- 其中字段与字段之间的分隔符是 | 

- 我们要解析得到所有的monthSales对应的值为以下这一列（行转列）

```
4900
2090
6987
```

##### 第一步：创建hive表

```sql
hive (hive_explode)> create table hive_explode.explode_lateral_view (area string, goods_id string, sale_info string) ROW FORMAT DELIMITED FIELDS TERMINATED BY '|' STORED AS textfile;
```

##### 第二步：准备数据并加载数据

- 准备数据如下

```shell
cd /kkb/install/hivedatas

vim explode_json

a:shandong,b:beijing,c:hebei|1,2,3,4,5,6,7,8,9|[{"source":"7fresh","monthSales":4900,"userCount":1900,"score":"9.9"},{"source":"jd","monthSales":2090,"userCount":78981,"score":"9.8"},{"source":"jdmart","monthSales":6987,"userCount":1600,"score":"9.0"}]
```

- 加载数据到hive表当中去

```sql
hive (hive_explode)> load data local inpath '/kkb/install/hivedatas/explode_json' overwrite into table hive_explode.explode_lateral_view;
```

##### 第三步：使用explode拆分Array

```sql
hive (hive_explode)> select explode(split(goods_id, ',')) as goods_id from hive_explode.explode_lateral_view;
```

##### 第四步：使用explode拆解Map

```sql
hive (hive_explode)> select explode(split(area, ',')) as area from hive_explode.explode_lateral_view;
```

##### 第五步：拆解json字段

```sql
hive (hive_explode)> select explode(split(regexp_replace(regexp_replace(sale_info,'\\[\\{',''),'}]',''),'},\\{')) as sale_info from hive_explode.explode_lateral_view;
```

```json
"source":"7fresh","monthSales":4900,"userCount":1900,"score":"9.9"
"source":"jd","monthSales":2090,"userCount":78981,"score":"9.8"
"source":"jdmart","monthSales":6987,"userCount":1600,"score":"9.0"
```

- 然后我们想用get_json_object来获取key为monthSales的数据：

```sql
hive (hive_explode)> select get_json_object(explode(split(regexp_replace(regexp_replace(sale_info,'\\[\\{',''),'}]',''),'},\\{')),'$.monthSales') as sale_info from hive_explode.explode_lateral_view;

-- 然后出现异常FAILED: SemanticException [Error 10081]: UDTF's are not supported outside the SELECT clause, nor nested in expressions
-- UDTF explode不能写在别的函数内

-- 如果你这么写，想查两个字段，select explode(split(area,',')) as area,good_id from explode_lateral_view;
-- 会报错FAILED: SemanticException 1:40 Only a single expression in the SELECT clause is supported with UDTF's. Error encountered near token 'good_id'
-- 使用UDTF的时候，只支持一个字段，这时候就需要LATERAL VIEW出场了
```

#### 3、配合LATERAL  VIEW使用

- lateral view用于和split、explode等UDTF一起使用的，能将一行数据拆分成多行数据
- 在此基础上可以对拆分的数据进行聚合
- lateral view首先为原始表的每行调用UDTF，UDTF会把一行拆分成一行或者多行，lateral view在把结果组合，产生一个支持别名表的**虚拟表**。

- 配合lateral view查询多个字段

```sql
hive (hive_explode)> select goods_id2, sale_info from explode_lateral_view 
LATERAL VIEW explode(split(goods_id, ','))goods as goods_id2;
```

- 其中LATERAL VIEW explode(split(goods_id,','))goods相当于一个虚拟表，与原表explode_lateral_view**笛卡尔积关联**。

- 也可以多重使用，如下，也是三个表笛卡尔积的结果

```sql
hive (hive_explode)> select goods_id2, sale_info, area2 from explode_lateral_view 
LATERAL VIEW explode(split(goods_id, ','))goods as goods_id2 
LATERAL VIEW explode(split(area,','))area as area2;
```

- 最终，我们可以通过下面的句子，把这个json格式的一行数据，完全转换成二维表的方式展现

```sql
hive (hive_explode)> select 
get_json_object(concat('{',sale_info_1,'}'),'$.source') as source, get_json_object(concat('{',sale_info_1,'}'),'$.monthSales') as monthSales, get_json_object(concat('{',sale_info_1,'}'),'$.userCount') as userCount,  get_json_object(concat('{',sale_info_1,'}'),'$.score') as score 
from explode_lateral_view   
LATERAL VIEW explode(split(regexp_replace(regexp_replace(sale_info,'\\[\\{',''),'}]',''),'},\\{'))sale_info as sale_info_1;
```

- 总结：
  - Lateral View通常和UDTF一起出现，为了解决UDTF不允许在select字段的问题。 
  - Multiple Lateral View可以实现类似笛卡尔乘积。 
  - Outer关键字可以把不输出的UDTF的空结果，输出成NULL，防止丢失数据。

### 10.12 reflect函数

- reflect函数可以支持在sql中调用java中的自带函数，秒杀一切udf函数。

#### 1、使用java.lang.Math当中的Max求两列中最大值

- 创建hive表

```sql
hive (hive_explode)> create table test_udf(col1 int,col2 int) 
row format delimited fields terminated by ',';
```

- 准备数据并加载数据

```shell
cd /kkb/install/hivedatas
vim test_udf

1,2
4,3
6,4
7,5
5,6
```

- 加载数据

```sql
hive (hive_explode)> load data local inpath '/kkb/install/hivedatas/test_udf' overwrite into table test_udf;
```

- 使用java.lang.Math当中的Max求两列当中的最大值

```sql
hive (hive_explode)> select reflect("java.lang.Math","max", col1, col2) from test_udf;
```

#### 2、不同记录执行不同的java内置函数

- 创建hive表

```sql
hive (hive_explode)> create table test_udf2(class_name string, method_name string, col1 int, col2 int) row format delimited fields terminated by ',';
```

- 准备数据

```shell
cd /export/servers/hivedatas
vim test_udf2

java.lang.Math,min,1,2
java.lang.Math,max,2,3
```

- 加载数据

```sql
hive (hive_explode)> load data local inpath '/kkb/install/hivedatas/test_udf2' overwrite into table test_udf2;
```

- 执行查询

```sql
hive (hive_explode)> select reflect(class_name, method_name, col1, col2) from test_udf2;
```

#### 3、判断是否为数字

- 使用apache commons中的函数，commons下的jar已经包含在hadoop的classpath中，所以可以直接使用。

- 使用方式如下：

```sql
hive (hive_explode)> select reflect("org.apache.commons.lang.math.NumberUtils", "isNumber", "123");
```

### 10.13 分析函数—分组求topN

#### 1、分析函数的作用

- 对于一些比较复杂的数据求取过程，我们可能就要用到分析函数
- 分析函数主要用于==分组求topN或者求取百分比，或者进行数据的切片==等等，我们都可以使用分析函数来解决

#### 2、常用的分析函数

1、ROW_NUMBER()：

- 从1开始，按照顺序，给分组内的记录加序列；
  - 比如，按照pv降序排列，生成分组内每天的pv名次,ROW_NUMBER()的应用场景非常多
  - 再比如，获取分组内排序第一的记录;
  - 获取一个session中的第一条refer等。 

2、RANK() ：

- 生成数据项在分组中的排名，排名相等会在名次中留下空位 

3、DENSE_RANK() ：

- 生成数据项在分组中的排名，排名相等会在名次中不会留下空位 

4、CUME_DIST ：

- 小于等于当前值的行数/分组内总行数。比如，统计小于等于当前薪水的人数，所占总人数的比例 

5、PERCENT_RANK ：

- 分组内当前行的RANK值/分组内总行数

6、NTILE(n) ：

- 用于将分组数据按照顺序切分成n片，返回当前切片值
- 如果切片不均匀，默认增加第一个切片的分布。
- NTILE不支持ROWS BETWEEN，比如 NTILE(2) OVER(PARTITION BY cookieid ORDER BY createtime ROWS BETWEEN 3 PRECEDING AND CURRENT ROW)

#### 3、需求描述

- 现有数据内容格式如下，分别对应三个字段，cookieid，createtime ，pv
- 求取每个cookie访问pv前三名的数据记录，其实就是分组求topN，求取每组当中的前三个值

```
cookie1,2015-04-10,1
cookie1,2015-04-11,5
cookie1,2015-04-12,7
cookie1,2015-04-13,3
cookie1,2015-04-14,2
cookie1,2015-04-15,4
cookie1,2015-04-16,4
cookie2,2015-04-10,2
cookie2,2015-04-11,3
cookie2,2015-04-12,5
cookie2,2015-04-13,6
cookie2,2015-04-14,3
cookie2,2015-04-15,9
cookie2,2015-04-16,7
```

##### 第一步：创建数据库表

- 在hive当中创建数据库表

```sql
CREATE EXTERNAL TABLE cookie_pv (
cookieid string,
createtime string, 
pv INT
) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' ;
```

##### 第二步：准备数据并加载

- node03执行以下命令，创建数据，并加载到hive表当中去

```shell
cd /kkb/install/hivedatas
vim cookiepv.txt

cookie1,2015-04-10,1
cookie1,2015-04-11,5
cookie1,2015-04-12,7
cookie1,2015-04-13,3
cookie1,2015-04-14,2
cookie1,2015-04-15,4
cookie1,2015-04-16,4
cookie2,2015-04-10,2
cookie2,2015-04-11,3
cookie2,2015-04-12,5
cookie2,2015-04-13,6
cookie2,2015-04-14,3
cookie2,2015-04-15,9
cookie2,2015-04-16,7
```

- 加载数据到hive表当中去

```sql
load data local inpath '/kkb/install/hivedatas/cookiepv.txt' overwrite into table  cookie_pv;
```

##### 第三步：使用分析函数来求取每个cookie访问PV的前三条记录

```sql
select * from (
SELECT 
cookieid,
createtime,
pv,
RANK() OVER(PARTITION BY cookieid ORDER BY pv desc) AS rn1,
DENSE_RANK() OVER(PARTITION BY cookieid ORDER BY pv desc) AS rn2,
ROW_NUMBER() OVER(PARTITION BY cookieid ORDER BY pv DESC) AS rn3 
FROM cookie_pv 
) temp where temp.rn1 <= 3;
```

### 10.14 hive自定义函数

#### 1、自定义函数的基本介绍

- Hive 自带了一些函数，比如：max/min等，但是数量有限，自己可以通过自定义UDF来方便的扩展。

- 当Hive提供的内置函数无法满足你的业务处理需要时，此时就可以考虑使用用户自定义函数（UDF：user-defined function）

- 根据用户自定义函数类别分为以下三种：
  - UDF（User-Defined-Function） 一进一出
  - UDAF（User-Defined Aggregation Function）  聚集函数，多进一出，类似于：count/max/min

  - UDTF（User-Defined Table-Generating Functions） 一进多出，如lateral view explode()

​                如lateral view explode()

- 官方文档地址

  https://cwiki.apache.org/confluence/display/Hive/HivePlugins

- 编程步骤：

​        （1）继承org.apache.hadoop.hive.ql.UDF

​        （2）需要实现evaluate函数；evaluate函数支持重载；

- 注意事项

​        （1）UDF必须要有返回类型，可以返回null，但是返回类型不能为void；

​        （2）UDF中常用Text/LongWritable等类型，不推荐使用java类型；

####  2、自定义函数开发

##### 第一步：创建maven java 工程，并导入jar包

```xml
	<repositories>
        <repository>
            <id>cloudera</id>
            <url>https://repository.cloudera.com/artifactory/cloudera-repos/</url>
        </repository>
    </repositories>
    
    <dependencies>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>2.6.0-mr1-cdh5.14.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-common</artifactId>
            <version>2.6.0-cdh5.14.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-hdfs</artifactId>
            <version>2.6.0-cdh5.14.2</version>
        </dependency>

        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-mapreduce-client-core</artifactId>
            <version>2.6.0-cdh5.14.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hive</groupId>
            <artifactId>hive-exec</artifactId>
            <version>1.1.0-cdh5.14.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hive</groupId>
            <artifactId>hive-jdbc</artifactId>
            <version>1.1.0-cdh5.14.2</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hive</groupId>
            <artifactId>hive-cli</artifactId>
            <version>1.1.0-cdh5.14.2</version>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.0</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                    <encoding>UTF-8</encoding>
                    <!--    <verbal>true</verbal>-->
                </configuration>
            </plugin>
        </plugins>
    </build>
```

##### 第二步：开发java类继承UDF，并重载evaluate 方法

```java
package com.kkb.udf.MyUDF;

public class MyUDF extends UDF {
     public Text evaluate(final Text s) {
         if (null == s) {
             return null;
         }
         //返回大写字母         
         return new Text(s.toString().toUpperCase());
     }
 }
```

##### 第三步：项目打包

- 使用maven的package进行打包
- 将我们打包好的jar包上传到node03服务器的/kkb/install/hive-1.1.0-cdh5.14.2/lib 这个路径下

##### 第四步：添加我们的jar包

- 重命名我们的jar包名称

```shell
cd /kkb/install/hive-1.1.0-cdh5.14.2/lib
mv original-day_hive_udf-1.0-SNAPSHOT.jar udf.jar
```

- hive的客户端添加我们的jar包

```bash
0: jdbc:hive2://node03:10000> add jar /kkb/install/hive-1.1.0-cdh5.14.2/lib/udf.jar;
```

##### 第五步：设置函数与我们的自定义函数关联

```sql
0: jdbc:hive2://node03:10000> create temporary function touppercase as 'com.kkb.udf.MyUDF';
```

##### 第六步：使用自定义函数

```sql
0: jdbc:hive2://node03:10000>select tolowercase('abc');
```

- hive当中如何创建永久函数

- 在hive当中添加临时函数，需要我们每次进入hive客户端的时候都需要添加以下，退出hive客户端临时函数就会失效，那么我们也可以创建永久函数来让其不会失效
- 创建永久函数

```sql
-- 1、指定数据库，将我们的函数创建到指定的数据库下面
0: jdbc:hive2://node03:10000>use myhive;

-- 2、使用add jar添加我们的jar包到hive当中来
0: jdbc:hive2://node03:10000>add jar /kkb/install/hive-1.1.0-cdh5.14.2/lib/udf.jar;

-- 3、查看我们添加的所有的jar包
0: jdbc:hive2://node03:10000>list jars;

-- 4、创建永久函数，与我们的函数进行关联
0: jdbc:hive2://node03:10000>create function myuppercase as 'com.kkb.udf.MyUDF';

-- 5、查看我们的永久函数
0: jdbc:hive2://node03:10000>show functions like 'my*';

-- 6、使用永久函数
0: jdbc:hive2://node03:10000>select myhive.myuppercase('helloworld');

-- 7、删除永久函数
0: jdbc:hive2://node03:10000>drop function myhive.myuppercase;

-- 8、查看函数
 show functions like 'my*';
```

## 11. 数据操纵DML

### 11.1 插入操作

```sql
INSERT INTO table_name(col1, col2, ...) VALUES(val1, val2, ...);
```

> ⚠️注意：使用这种操作每次都会生成一个小文件。

### 11.2 更新操作

Hive作为一个数据仓库，本身就不应该做 UPDATE 和 DELETE 操作的，应该把数据全部清洗完成后再存入数据仓库。

为了使Hive支持 UPDATE 和 DELETE 操作，必须满足以下前提条件：

1. 服务端必须配置

```xml
<property>
	<name>hive.compactor.initiator.on</name>
	<value>true</value>
</property>
<property>
	<name>hive.compactor.worker.threads</name>
	<value>1</value>
</property>
<property>
	<name>hive.in.test</name>
	<value>true</value>
</property>
```

2. 客户端必须配置

```xml
<property>
	<name>hive.support.concurrency</name>
	<value>true</value>
</property>
<property>
	<name>hive.enforce.bucketing</name>
	<value>true</value>
</property>
<property>
	<name>hive.exec.dynamic.partition.mode</name>
	<value>nonstrict</value>
</property>
<property>
	<name>hive.txn.manager</name>
	<value>org.apache.hadoop.hive.ql.lockmgr.DbTxnManager</value>
</property>
```

3. 表必须分桶，且保存为ORC格式，同时开启事务

```sql
create table if not exists user_acid_demo (
  id int,
  name string 
)
clustered by (id) into 4 buckets
row format delimited fields terminated by '\t'
stored as orc TBLPROPERTIES('transactional'='true');
```

4. 操作实战

```sql
use myhive;

set mapreduce.job.reduces=4; 

insert into table user_acid_demo select * from user_demo;

select * from user_acid_demo;
```
查询结果如下：

```log
+--------------------+----------------------+--+
| user_acid_demo.id  | user_acid_demo.name  |
+--------------------+----------------------+--+
| 8                  | anzhulababy8         |
| 4                  | anzhulababy4         |
| 9                  | anzhulababy9         |
| 5                  | anzhulababy5         |
| 1                  | anzhulababy1         |
| 10                 | anzhulababy10        |
| 6                  | anzhulababy6         |
| 2                  | anzhulababy2         |
| 7                  | anzhulababy7         |
| 3                  | anzhulababy3         |
+--------------------+----------------------+--+
```
执行update操作：

```sql
update user_acid_demo set name = 'baby5' where id = 1;
```

结果如下：

```log
+--------------------+----------------------+--+
| user_acid_demo.id  | user_acid_demo.name  |
+--------------------+----------------------+--+
| 8                  | anzhulababy8         |
| 4                  | anzhulababy4         |
| 9                  | anzhulababy9         |
| 5                  | anzhulababy5         |
| 1                  | baby5                |
| 10                 | anzhulababy10        |
| 6                  | anzhulababy6         |
| 2                  | anzhulababy2         |
| 7                  | anzhulababy7         |
| 3                  | anzhulababy3         |
+--------------------+----------------------+--+
```

> ⚠️注意：每次执行update或delete操作都会启动一个MapReduce任务，效率极其低下，生产环境中不应该使用。

### 11.3 删除操作

```sql
DELETE FROM user_acid_demo WHERE id=10;
```

结果如下：

```log
+--------------------+----------------------+--+
| user_acid_demo.id  | user_acid_demo.name  |
+--------------------+----------------------+--+
| 8                  | anzhulababy8         |
| 4                  | anzhulababy4         |
| 9                  | anzhulababy9         |
| 5                  | anzhulababy5         |
| 1                  | baby5                |
| 6                  | anzhulababy6         |
| 2                  | anzhulababy2         |
| 7                  | anzhulababy7         |
| 3                  | anzhulababy3         |
+--------------------+----------------------+--+
```

## 12. 增强聚合（多维分析）

### 12.1 grouping sets

在一个GROUP BY查询中，根据不同的维度组合进行聚合，等价于将不同维度的GROUP BY结果集进行UNION ALL，例如：

```sql
select a,b, … group by a,b grouping sets((a,b),(a),(b));
```

上面这条语句等价于：

```sql
select a,b, … group by a,b
union all
select a,null as b, … group by a
union all
select null as a,b, … group by b
```

再比如：

```sql
select a,b, … group by a,b grouping sets((a),(b));
```

等价于：

```sql
select a,null as b, … group by a
union all
select null as a,b, … group by b
```

> ⚠️==注意==：grouping sets 语法中含有一个隐含的字段`grouping__id`来区分属于哪个组：
>
> ```sql
> select c_id, g_id, sum(s_id), grouping__id from t_student group by c_id, g_id grouping set(c_id, g_id)
> ```
>
> 可得到结果如下：
>
> ```log
> +---------+--------+----------+---------------+--+
> |   c_id  |  g_id  | sum(sid) | groupping__id |
> +---------+--------+----------+---------------+--+
> |  class1 |  NULL  |    108   |       1       |
> |  class2 |  NULL  |    92    |       1       |
> |   NULL  | grade7 |    70    |       2       |
> |   NULL  | grade8 |    68    |       2       |
> |   NULL  | grade9 |    62    |       2       |
> +---------+--------+----------+---------------+--+
> ```
>
> 并且，如果要用`grouping__id`进行排序，那么 select 语句中必须出现`grouping__id`，即：
>
> ```sql
> select a,b, grouping__id from t_table group by a, b grouping set(a, b) order by grouping__id;
> # 下面这种写法会报错
> select a,b from t_table group by a, b grouping set(a, b) order by grouping__id;
> ```

### 12.2 cube

cube 会将GROUP BY查询中的维度，进行排列组合，然后把结果全部 UNION ALL，例如：

```sql
select a,b, … group by a,b with cube;
```

等价于

```sql
select a,b, … group by a,b grouping set((a,b), (a), (b), ());
```

等价于

```sql
select a,b, … group by a,b
union all
select a,null as b, … group by a
union all
select null as a,b, … group by b
union all
select null as a,null as b, … 
```

> ⚠️==注意==：同样的，cube 语法同样隐含`grouping__id`

### 12.3 rollup

rollup是CUBE的子集，以最左侧的维度为主，从该维度进行层级聚合，例如

```sql
select a,b,c, … group by a,b,c with rollup;
```

等价于

```sql
select a,b,c, … group by a,b,c grouping set((a,b,c), (a,b), (a), ());
```

等价于

```sql
select a,b,c, … group by a,b,c
union all
select a,b,null as c, … group by a,b
union all
select a,null as b,null as c … group by a
union all
select null as a,null as b,null as c, … 
```

