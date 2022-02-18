## hive调优概括

- 考虑是否是模型设计不合理，调整模型，提高模型质量和复用度，尽量减少Join的使用

- 可以先使用explain查看SQL的执行计划，看看是否符合预期
- 具体SQL编写方面需要注意：
  - Join之前左右两个表先看看能不能做过滤，比如分区过滤
  - 尽量使用用in来代替join，但是低版本中可能不支持in语法，可以试用left semi join来代替in【[转载:left join和left semi join的联系和区别](https://www.cnblogs.com/zzhangyuhang/p/9792794.html)】
  - 避免使用COUNT(DISTINCT col)操作，DISTINCT会将该列数据全部放到内存中去，使用类似HashSet的结构去达到去重目的，性能较差;
- 根据当前数据量和资源合理设置Properties配置进行优化
  - 小文件太多的时候，使用`mapred.max.split.size`配置【其实是一个MR配置】设置每个map读取的数据量
  - 使用map join应对大表join小表的情况【当然高版本的hive已经自动实现优化，只需要配置判断小表的门限即可】
- 发生了数据倾斜，可以使用下面的优化手段

## 处理数据倾斜

> 参考文章：
>
> - [深入浅出Hive数据倾斜](https://www.getui.com/college/2021010444)
> -  [hive.groupby.skewindata及数据倾斜优化](https://zhuanlan.zhihu.com/p/345770553) 

数倾斜的一般性原因：key分布不均匀、业务数据本身的特性、建表时考虑不周、某些SQL语句本身就有数据倾斜

__1、数据倾斜很少发生在Map端__

Map端的数据倾斜一般是HDFS数据存储不均匀导致的，而Reduce端则更容易发生数据倾斜，而Reduce阶段最容易出现数据倾斜的两个场景分别是Join和Count Distinct；如果只是使用Group By进行Sum或者Count操作，发生了数据倾斜可以先使用如下两个参数优化：

 ```sql
 set hive.map.aggr=true;(默认为true) （map端的Combiner ）
 set hive.groupby.skewindata=true; （默认为false）
 ```

​		解决数据倾斜首先要进行负载均衡操作，将上面两个参数设定为 true

第一个参数还可以搭配两个辅助配置

```sql
set hive.groupby.mapaggr.checkinterval=100000 (默认)执行聚合的条数
set hive.map.aggr.hash.min.reduction=0.5(默认)
#如果hash表的容量与输入行数之比超过这个数，那么map端的hash聚合将被关闭，默认是0.5，
#设置为1可以保证hash聚合永不被关闭；
```

 参数开启后， 在map中会做部分聚集操作，效率更高但需要更多的内存。开启map之后使用combiner，这样基本上是对各记录比较同质的数据效果比较好，相反，则没有什么意义。通用的做法是设置下面两个参数： ![preview](Hive%E8%B0%83%E4%BC%98.assets/v2-af3a7ed4ef0176fc5ddf45c5c078ff0b_r.jpg) 

第二个参数，MapReduce进程则会生成两个额外的 MR Job，这两个任务的主要操作如下【面试高频】：

 - 第一步：MR Job 中Map 输出的结果集合首先会随机分配到 Reduce 中，然后每个 Reduce 做局部聚合操作并输出结果，这样处理的原因是相同的Group By Key有可能被分发到不同的 Reduce Job中，从而达到负载均衡的目的。

 - 第二步：MR Job 再根据预处理的数据结果按照 Group By Key 分布到 Reduce 中（这个过程可以保证相同的 Group By Key 被分布到同一个 Reduce 中），最后完成聚合操作。

   在设置好这两个参数之后，我们仍然需要针对不同的SQL语句进行SQL优化，主要分为两个场景， join操作发生的数据倾斜以及Count Distinct操作带来的数据倾斜。

    ![preview](Hive%E8%B0%83%E4%BC%98.assets/v2-26b17fbbcd11b1ca20a396c2be904346_r.jpg) 

**2、在Join场景下：**

- 方案一：使用Mapjoin进行关联

 ```properties
 set hive.auto.convert.join= ture; 设置自动化开启(默认为false)
 set hive.mapjoin.smalltable.filesize=25000000; 设置小表大小的上限（默认为25M）
 ```

- 方案二：将小表放入子查询 ，比如将与小表的Join改成in操作，但前提是小表的数据不会特别大

- 方案三：关联字段去重。很多时候发生数据倾斜是因为两表的关联字段有大量的重复值（大量Null也是一种情况）；

- 方案四：部分数据参数Join操作，过滤掉的不需要处理的数据后面再union回来

**3、在Count Distinct场景下：**

​		由于SQL中的Distinct操作本身会有一个全局排序的过程，一般情况下，不建议采用Count Distinct方式进行去重计数，除非表的数量比较小。当SQL中不存在分组字段时，Count Distinct操作仅生成一个Reduce 任务，该任务会对全部数据进行去重统计；当SQL中存在分组字段时，可能某些Reduce 任务需要去重统计的数量非常大。在这种情况下，我们可以通过以下方式替换：

 ​                 ![img](Hive%E8%B0%83%E4%BC%98.assets/gG1wUi_2N5xwTovB7YP23A.png)        

如果语句中存在多个Distinct命令，开发者需要评估下用空间换时间的方法是否能够提升效率，具体的评估语法如下： ​           ![img](Hive%E8%B0%83%E4%BC%98.assets/h9R_ruMuWzniJfqjAUtYhw.png)        

>综上，可以得到一些通用方法
>
>1、如果任务长时间卡在99%则基本可以认为是发生了数据倾斜，建议开发者调整参数以实现负载均衡：set hive.groupby.skewindata=true；
>
>2、小表关联大表操作，需要先看能否使用子查询，再看能否使用Map join；
>
>3、Join操作注意关联字段不能出现大量的重复值或者空值；
>
>4、Count(distinct id ) 去重统计要慎用，尽量通过其他方式替换。

## Map Join原理

> 参考文章：
>
> -  [Hive中MapJoin的原理和机制](https://ych0112xzz.github.io/2018/07/23/Hive中MapJoin的原理和机制/) 

### 1 Hive Common Join

如果不指定MapJoin或者不符合MapJoin的条件，那么Hive解析器会将Join操作转换成Common Join,即：在Reduce阶段完成join.
整个过程包含Map、Shuffle、Reduce阶段。

#### Map阶段

- 读取源表的数据，Map输出时候以Join on条件中的列为key，如果Join有多个关联键，则以这些关联键的组合作为key;
- Map输出的value为join之后所关心的(select或者where中需要用到的)列；同时在value中还会包含表的Tag信息，用于标明此value对应哪个表；
- 按照key进行排序

#### Shuffle阶段

根据key的值进行**hash**,并将key/value按照hash值推送至不同的reduce中，这样确保两个表中相同的key位于同一个reduce中

#### Reduce阶段

根据key的值完成join操作，期间通过Tag来识别不同表中的数据。
以下面的HQL为例，图解其过程：

```sql
SELECTa.id,a.dept,b.ageFROM a join bON (a.id = b.id);
```

![](Hive%E8%B0%83%E4%BC%98.assets/HQL%E6%B5%81%E7%A8%8B.png)

### 2 Hive Map Join

- MapJoin通常用于一个很小的表和一个大表进行join的场景，具体小表有多小，由参数 **hive.mapjoin.smalltable.filesize**来决定，该参数表示小表的总大小，默认值为**25000000字节，**即25M.
- Hive0.7之前，需要使用hint提示 **/\*+ mapjoin(table)\* /**才会执行MapJoin,否则执行Common Join，但在0.7版本之后，默认自动会转换Map Join，由数 **hive.auto.convert.join**来控制，默认为true.
- 仍然以9.1中的HQL来说吧，假设a表为一张大表，b为小表，并且`hive.auto.convert.join=true`,那么Hive在执行时候会自动转化为MapJoin。

![](Hive%E8%B0%83%E4%BC%98.assets/Hive%E7%9A%84MapJoin%E6%B5%81%E7%A8%8B.png)

- 执行流程如下：
  - 如图中的流程，首先是Task A，它是一个Local Task（在客户端本地执行的Task），负责扫描小表b的数据，将其转换成一个HashTable的数据结构，并写入本地的文件中，之后将该文件加载到**DistributeCache**中，该HashTable的数据结构可以抽象为：

> |key| value|
> | 1 | 26 |
> | 2 | 34 |

![](Hive%E8%B0%83%E4%BC%98.assets/LocalTask%E4%BF%A1%E6%81%AF.png)

图中红框圈出了执行Local Task的信息。

- 接下来是Task B，该任务是一个没有Reduce的MR，启动MapTasks扫描大表a,在Map阶段，根据a的每一条记录去和DistributeCache中b表对应的HashTable关联，并直接输出结果。
- 由于MapJoin没有Reduce，所以由Map直接输出结果文件，有多少个Map Task，就有多少个结果文件

## 长尾调优

> 对前文处理数据倾斜的一个补充，二者结合一起看
>
> 参考文章： 
>
> - [三问Hive](https://danzhuibing.github.io/Hive_basic.html) 
> - [HiveSql调优经验](https://www.cnblogs.com/duanxingxing/p/6874318.html) 

### Join长尾调优

#### 情形1：小表长尾

Join倾斜时，如果某路输入比较小，可以采用Mapjoin避免倾斜。Mapjoin的原理是将Join操作提前到Map端执行，这样可以避免因为分发Key不均匀导致数据倾斜。但是Mapjoin的使用有限制，必须是Join中的从表比较小才可用。所谓从表，即Left Outer Join中的右表，或者Right Outer Join中的左表。

#### 情形2：空值长尾

做left outer join时，左表存在大量空值，聚集到了一个reducer处。通过`coalesce(left_table.key, rand()*9999)`将key为空的情况下赋予随机值，来避免空值集中。

#### 情形3：热点值长尾

从连接表a中取出热点值，建立临时表t。 a表mapjoin表t，取出非热点值的部分，与表b连接输出r1。此时因为无热点值，这部分不再存在长尾问题。

用mapjoin取出表a的热点值部分得到c，用mapjoin取出表b的热点值部分得到d，c再mapjoin表d，得到热点值的输出r2.

r1和r2 union all得到最后的结果。

### Map端长尾

Map端读数据时，由于文件大小分布不均匀，一些map任务读取并处理的数据特别多，一些map任务处理的数据特别少，造成map端长尾。这种情形没有特别好的方法，只能调节splitsize来增加mapper数量，让数据分片更小，以期望获得更均匀的分配。

### Reduce端长尾

典型case是Multi Distinct。一个distinct的实现是将group by的key和distinct的字段合并在一起作为mapreduce的key，将group by的key作为partition的key，然后在reduce端去重计数即可。而Multi Distinct，map端的一行数据就会输出n行。

可以将multi distinct改为两层group by，第一层的group by的key为multi distinct group by的key与distinct的key，第二层为multi distinct的group by的key。

## 小文件优化

> 参考文章： [Hive优化之小文件问题及其解决方案](https://blog.csdn.net/lzm1340458776/article/details/43567209) 

### 小文件是如何产生的
- 动态分区插入数据，产生大量的小文件，从而导致map数量剧增。
- reduce数量越多，小文件也越多(reduce的个数和输出文件是对应的)。
- 数据源本身就包含大量的小文件。

### 小文件问题的影响

- 从Hive的角度看，小文件会开很多map，一个map开一个JVM去执行，所以这些任务的初始化，启动，执行会浪费大量的资源，严重影响性能。
- 在HDFS中，每个小文件对象约占150byte，如果小文件过多会占用大量内存。这样NameNode内存容量严重制约了集群的扩展。

### 小文件问题的解决方案
从小文件产生的途经就可以从源头上控制小文件数量，方法如下：

- 使用Sequencefile作为表存储格式，不要用textfile，在一定程度上可以减少小文件。
- 减少reduce的数量(可以使用参数进行控制)。
- 少用动态分区，用时记得按distribute by分区。




对于已有的小文件，我们可以通过以下几种方案解决：

- 使用hadoop archive命令把小文件进行归档。

- 重建表，建表时减少reduce数量。

- 通过参数进行调节，设置map/reduce端的相关参数，如下：



设置map输入合并小文件的相关参数：

```sql
//每个Map最大输入大小(这个值决定了合并后文件的数量)
set mapred.max.split.size=256000000;  
//一个节点上split的至少的大小(这个值决定了多个DataNode上的文件是否需要合并)
set mapred.min.split.size.per.node=100000000;
//一个交换机下split的至少的大小(这个值决定了多个交换机上的文件是否需要合并)  
set mapred.min.split.size.per.rack=100000000;
//执行Map前进行小文件合并
set hive.input.format=org.apache.hadoop.hive.ql.io.CombineHiveInputFormat; 
```

设置map输出和reduce输出进行合并的相关参数：

```sql
//设置map端输出进行合并，默认为true
set hive.merge.mapfiles = true
//设置reduce端输出进行合并，默认为false
set hive.merge.mapredfiles = true
//设置合并文件的大小
set hive.merge.size.per.task = 256*1000*1000
//当输出文件的平均大小小于该值时，启动一个独立的MapReduce任务进行文件merge。
set hive.merge.smallfiles.avgsize=16000000
```

