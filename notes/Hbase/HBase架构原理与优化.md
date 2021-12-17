# 架构原理

## 行锁设计

- HBase有一些客户端API，如put()、delete()、checkAndPut()等操作都是独立执行的，这意味着在一个串行方式执行中，对于每一行必须保证行级别的操作是原子性的

- 行锁”的特性保证了只有一个客户端能获取一行数据相应的锁，同时对该行进行修改，其它客户端则需要等到锁释放后才能修改同一行。
- “行锁”机制一般是隐式的，当然客户端也可以显式地对单行数据的多次操作进行加锁，使用**lockRow()**加锁，使用**unlockRow()**释放锁。
- get()获取数据是不需要加锁的，而是应用一个多版本的并发控制(MVCC)机制来保证行级读操作。

## 集群级别的高可用

[HBase高可用原理与实践 - 云+社区 - 腾讯云 (tencent.com)](https://cloud.tencent.com/developer/article/1074535)

## 读写流程

[Hbase的数据读写流程 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/65513466)

# 优化

## 高可用

## 预分区

### Region自动拆分原理

- 每一个region维护着StartKey 与 EndKey，如果加入的数据符合某个region维护的RowKey范围，则该数据交给这个region维护。
- 默认情况下，HBase建表的时候会默认为表分配一个Region，当数据量到达一定的阈值，HBase就会拆分这个Region
- Region的拆分是不可见的，Master 不会参与其中。RegionServer 拆分 Region的步骤是：先将该 Region 下线，然后拆分，将其子 Region 加入到 META 元信息中，再将他们加入到原本的 RegionServer 中，最后汇报 Master。执行 split 的线程是 CompactSplitThread。

### Region 自动拆分策略

到2.0.5版本后，存在7种拆分策略

![img](HBase%E6%9E%B6%E6%9E%84%E5%8E%9F%E7%90%86%E4%B8%8E%E4%BC%98%E5%8C%96.assets/webp0.webp)

![img](HBase%E6%9E%B6%E6%9E%84%E5%8E%9F%E7%90%86%E4%B8%8E%E4%BC%98%E5%8C%96.assets/webp.webp)

#### 三种拆分策略设置方法

#### hbase-site.xml

```xml
<property> 
  <name>hbase.regionserver.region.split.policy</name> 
  <value>org.apache.hadoop.hbase.regionserver.IncreasingToUpperBoundRegionSplitPolicy</value> 
</property>
```

#### Java客户端设置

```java
private static Configuration conf = HBaseConfiguration.create();
conf.set("hbase.regionserver.region.split.policy", "org.apache.hadoop.hbase.regionserver.SteppingSplitPolicy");
```

#### 建表的时候单独设置

Region 的拆分策略需要根据表的属性来合理的配置， 所以在建表的时候不建议用前两种方式配置，而是针对不同的表设置不同的策略，每种策略在建表时具体使用在解释每种策略的时候说明。

- 手动设定预分区

```shell
create 'test_shell:user','info','partition1',SPLITS => ['a','c','f','h']
# 从HBase的监控界面可以看到具体的分区效果
```

![image-20211023141506916](HBase%E6%9E%B6%E6%9E%84%E5%8E%9F%E7%90%86%E4%B8%8E%E4%BC%98%E5%8C%96.assets/image-20211023141506916.png)

- 生成16进制序列预分区

```shell
create 'test_shell:user2','info','partition2',{NUMREGIONS => 15, SPLITALGO => 'HexStringSplit'}
# 分成15个分区，以下是部分截图
```

![image-20211023142035883](HBase%E6%9E%B6%E6%9E%84%E5%8E%9F%E7%90%86%E4%B8%8E%E4%BC%98%E5%8C%96.assets/image-20211023142035883.png)

- 按照文件中设置的规则预分区

```shell
create 'user3','partition3',SPLITS_FILE => 'splits.txt'
# splits.txt文件内容如下：
```

> 10
>
> 20
>
> 30
>
> 40

- Java客户端设置

```java
//自定义算法，产生一系列Hash散列值存储在二维数组中
byte[][] splitKeys = 某个散列值函数
//创建HBaseAdmin实例
HBaseAdmin hAdmin = new HBaseAdmin(HBaseConfiguration.create());
//创建HTableDescriptor实例
HTableDescriptor tableDesc = new HTableDescriptor(tableName);
//通过HTableDescriptor实例和散列值二维数组创建带有预分区的HBase表
hAdmin.createTable(tableDesc, splitKeys);
```

## 内存

-  HBase操作过程中需要大量的内存开销，毕竟Table是可以缓存在内存中的，一般会分配整个可用内存的70%给HBase的Java堆。
- 但是不建议分配非常大的堆内存，因为GC过程持续太久会导致RegionServer处于长期不可用状态，一般16~48G内存就可以了，如果因为框架占用内存过高导致系统内存不足，框架一样会被系统服务拖死。

## 压缩

写入压缩过程

![img](HBase%E6%9E%B6%E6%9E%84%E5%8E%9F%E7%90%86%E4%B8%8E%E4%BC%98%E5%8C%96.assets/20200722100615598.jpg)

读取解压缩过程

![img](HBase%E6%9E%B6%E6%9E%84%E5%8E%9F%E7%90%86%E4%B8%8E%E4%BC%98%E5%8C%96.assets/20200722100633751.jpg)

由此可见：压缩会缩小磁盘上的数据。当它在内存中（例如，在MemStore中）或在线上（例如，在RegionServer和Client之间传输）时，它会膨胀。因此，虽然使用ColumnFamily压缩是最佳做法，但它不会完全消除过大的Keys，过大的ColumnFamily名称或过大的列名称的影响。

## Family数控制

[带你快速上手HBase | HBase列族优化 - 随性i - 博客园 (cnblogs.com)](https://www.cnblogs.com/sx66/p/14239769.html)

一个列族在数据底层是一个文件，所以将经常一起查询的列放到一个列族中，列族尽量少，减少文件的寻址时间。

## Column数控制

没有限制，但是需要考虑到行锁，如果业务场景是频繁的更新同一行数据，可能会导致吞吐量降低

## 开启布隆过滤器

[详解布隆过滤器的原理，使用场景和注意事项 - 知乎 (zhihu.com)](https://zhuanlan.zhihu.com/p/43263751)

## RowKey的设计原则

### RowKey在查询中的作用

- 通过 **get** 方式，指定 **RowKey** 获取唯一一条记录
- 通过 **scan** 方式，设置 **startRow** 和 **stopRow** 参数进行范围匹配
- **全表扫描**，即直接扫描整张表中所有行记录

### RowKey在Region中的作用

-  读写数据时通过 RowKey 找到对应的 Region 
-  MemStore 中的数据是按照 RowKey 的字典序排序 
-  HFile 中的数据是按照 RowKey 的字典序排序 

### RowKey设计原则

####  **长度原则** 

RowKey是一个二进制码流，可以是任意字符串，最大长度为64kb，实际应用中一般为10-100byte，以byte[]形式保存，一般设计成定长。建议越短越好，不要超过16个字节，原因如下：

- 数据的持久化文件HFile中时按照Key-Value存储的，如果RowKey过长，例如超过100byte，那么1000w行的记录，仅RowKey就需占用近1GB的空间。这样会极大影响HFile的存储效率。
- MemStore会缓存部分数据到内存中，若RowKey字段过长，内存的有效利用率就会降低，就不能缓存更多的数据，从而降低检索效率。
- 目前操作系统都是64位系统，内存8字节对齐，控制在16字节，8字节的整数倍利用了操作系统的最佳特性。

####  **唯一原则** 

#### **排序原则**

HBase的RowKey是按照ASCII有序排序的，因此我们在设计RowKey的时候要充分利用这点。

#### 散列原则

设计的RowKey应均匀的分布在各个HBase节点上。

### 避免数据热点的方案

####  **Reversing** 【翻转】

如果正序设计的RowKey在数据分布上不均匀，但是尾部却呈现了良好的随机性，此时，可以考虑将RowKey的信息翻转，或者直接将尾部的bytes提前到RowKey的开头。Reversing可以有效的使RowKey随机分布，但是牺牲了RowKey的有序性。  利于Get操作，但不利于Scan操作，因为数据在原RowKey上的自然顺序已经被打乱 。

####  **Salting** 【盐值】

 Salting（加盐）的原理是在原RowKey的前面添加固定长度的随机数，也就是给RowKey分配一个随机前缀使它和之间的RowKey的开头不同。随机数能保障数据在所有Regions间的负载均衡。 

但是因为添加的是随机数，基于原RowKey查询时无法知道随机数是什么，那样在查询的时候就需要去各个可能的Regions中查找，Salting对于读取是利空的。并且加盐这种方式增加了读写时的吞吐量。 

####  **Hashing** 【哈希】

 基于 RowKey 的完整或部分数据进行 Hash，而后将Hashing后的值完整替换或部分替换原RowKey的前缀部分。这里说的 hash 包含 MD5、sha1、sha256 或 sha512 等算法。 但是与 Reversing 类似，Hashing 也不利于 Scan，因为打乱了原RowKey的自然顺序。 

