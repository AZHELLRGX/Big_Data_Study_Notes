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

- 每一个region维护着StartKey 与 EndKey，如果加入的数据符合某个region维护的Rowkey范围，则该数据交给这个region维护。
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

# Rowkey的设计

HBase中的数据是根据Rowkey的字典顺序来排序的，Rowkey决定了数据将会被分到哪一个分区。一个好的Rowkey方案设计可以避免数据倾斜，让数据均匀的分布到所有的Region中，尽量保证数据访问的时候不会出现热点问题

Rowkey的设计让让Scan操作更加方便，但是也容易出现热点问题，数据倾斜的时候会导致大量的客户端请求访问到少数的几个Region，导致部分RegionServer的负载很高，导致性能下降，甚至不可用

## Rowkey优化

- **长度**：Rowkey可以使任意字符串，最大长度64kb，建议越短越好，最好不要超过16个字节，原因如下:
  - 目前操作系统都是64位系统，内存8字节对齐，控制在16字节，8字节的整数倍利用了操作系统的最佳特性。
  - Hbase将部分数据加载到内存当中，如果Rowkey太长，内存的有效利用率就会下降。
- **唯一**：必须唯一，否则会发生本是插入的新数据，但是却更新了之前的数据
- **散列**
  - **加盐**：在Rowkey的前面增加随机数，散列之后的Rowkey就会根据随机生成的前缀分散到各个Region上，可以有效的避免热点问题。但是加盐这种方式增加了写的吞吐，**但是使得读数据更加困难**
  - **Hash**：Hash算法包含了MD5等算法，可以直接取Rowkey的MD5值作为Rowkey，或者取MD5值拼接原始Rowkey，组成新的Rowkey，由于Rowkey设计不应该太长，所以可以对MD5值进行截取拼接【好像目前用的最多的方法】
- **字符串反转**：时间戳反转、手机号反转等

