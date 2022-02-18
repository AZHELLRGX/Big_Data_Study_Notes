> 参考文章： [Hive底层原理](https://zhuanlan.zhihu.com/p/352076174) 

## 理论

**HIVE提供了EXPLAIN命令来展示一个查询的执行计划**,这个执行计划对于我们了解底层原理，hive 调优，排查数据倾斜等很有帮助

使用语法如下：

```text
EXPLAIN [EXTENDED|CBO|AST|DEPENDENCY|AUTHORIZATION|LOCKS|VECTORIZATION|ANALYZE] query
```

explain 后面可以跟以下可选参数，注意：这几个可选参数不是 hive 每个版本都支持的

1. **EXTENDED**：加上 extended 可以输出有关计划的额外信息。这通常是物理信息，例如文件名。这些额外信息对我们用处不大
2. **CBO**：输出由Calcite优化器生成的计划。CBO 从 hive 4.0.0 版本开始支持
3. **AST**：输出查询的抽象语法树。AST 在hive 2.1.0 版本删除了，存在bug，转储AST可能会导致OOM错误，将在4.0.0版本修复
4. **DEPENDENCY**：dependency在EXPLAIN语句中使用会产生有关计划中输入的额外信息。它显示了输入的各种属性
5. **AUTHORIZATION**：显示所有的实体需要被授权执行（如果存在）的查询和授权失败
6. **LOCKS**：这对于了解系统将获得哪些锁以运行指定的查询很有用。LOCKS 从 hive 3.2.0 开始支持
7. **VECTORIZATION**：将详细信息添加到EXPLAIN输出中，以显示为什么未对Map和Reduce进行矢量化。从 Hive 2.3.0 开始支持
8. **ANALYZE**：用实际的行数注释计划。从 Hive 2.2.0 开始支持

在 hive cli 中输入以下命令(hive 2.3.7)：

```text
explain select sum(id) from test1;
```

得到结果：

```text
STAGE DEPENDENCIES:
  Stage-1 is a root stage
  Stage-0 depends on stages: Stage-1

STAGE PLANS:
  Stage: Stage-1
    Map Reduce
      Map Operator Tree:
          TableScan
            alias: test1
            Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
            Select Operator
              expressions: id (type: int)
              outputColumnNames: id
              Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
              Group By Operator
                aggregations: sum(id)
                mode: hash
                outputColumnNames: _col0
                Statistics: Num rows: 1 Data size: 8 Basic stats: COMPLETE Column stats: NONE
                Reduce Output Operator
                  sort order:
                  Statistics: Num rows: 1 Data size: 8 Basic stats: COMPLETE Column stats: NONE
                  value expressions: _col0 (type: bigint)
      Reduce Operator Tree:
        Group By Operator
          aggregations: sum(VALUE._col0)
          mode: mergepartial
          outputColumnNames: _col0
          Statistics: Num rows: 1 Data size: 8 Basic stats: COMPLETE Column stats: NONE
          File Output Operator
            compressed: false
            Statistics: Num rows: 1 Data size: 8 Basic stats: COMPLETE Column stats: NONE
            table:
                input format: org.apache.hadoop.mapred.SequenceFileInputFormat
                output format: org.apache.hadoop.hive.ql.io.HiveSequenceFileOutputFormat
                serde: org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe

  Stage: Stage-0
    Fetch Operator
      limit: -1
      Processor Tree:
        ListSink
```

> **一个HIVE查询被转换为一个由一个或多个stage组成的序列（有向无环图DAG）。这些stage可以是MapReduce stage，也可以是负责元数据存储的stage，也可以是负责文件系统的操作（比如移动和重命名）的stage**。

将上述结果拆分看，先从最外层开始，包含两个大的部分：

1. stage dependencies： 各个stage之间的依赖性
2. stage plan： 各个stage的执行计划

先看第一部分 stage dependencies ，包含两个 stage，Stage-1 是根stage，说明这是开始的stage，Stage-0 依赖 Stage-1，Stage-1执行完成后执行Stage-0。

再看第二部分 stage plan，里面有一个 Map Reduce，一个MR的执行计划分为两个部分：

1. Map Operator Tree： MAP端的执行计划树
2. Reduce Operator Tree： Reduce端的执行计划树

这两个执行计划树里面包含这条sql语句的 operator：

1. map端第一个操作肯定是加载表，所以就是 **TableScan 表扫描操作**，常见的属性：

- alias： 表名称
- Statistics： 表统计信息，包含表中数据条数，数据大小等

**Select Operator： 选取操作**，常见的属性 ：

- expressions：需要的字段名称及字段类型
- outputColumnNames：输出的列名称
- Statistics：表统计信息，包含表中数据条数，数据大小等

**Group By Operator：分组聚合操作**，常见的属性：

- aggregations：显示聚合函数信息
- mode：聚合模式，值有 hash：随机聚合，就是hash partition；partial：局部聚合；final：最终聚合
- keys：分组的字段，如果没有分组，则没有此字段
- outputColumnNames：聚合之后输出列名
- Statistics： 表统计信息，包含分组聚合之后的数据条数，数据大小等

**Reduce Output Operator：输出到reduce操作**，常见属性：

- sort order：值为空 不排序；值为 + 正序排序，值为 - 倒序排序；值为 +- 排序的列为两列，第一列为正序，第二列为倒序

**Filter Operator：过滤操作**，常见的属性：

- predicate：过滤条件，如sql语句中的where id>=1，则此处显示(id >= 1)

**Map Join Operator：join 操作**，常见的属性：

- condition map：join方式 ，如Inner Join 0 to 1 Left Outer Join0 to 2
- keys: join 的条件字段
- outputColumnNames： join 完成之后输出的字段
- Statistics： join 完成之后生成的数据条数，大小等

**File Output Operator：文件输出操作**，常见的属性

- compressed：是否压缩
- table：表的信息，包含输入输出文件格式化方式，序列化方式等

**Fetch Operator 客户端获取数据操作**，常见的属性：

- limit，值为 -1 表示不限制条数，其他值为限制的条数

好，学到这里再翻到上面 explain 的查询结果，是不是感觉基本都能看懂了。

## 实践

## 1. join 语句会过滤 null 的值吗？

现在，我们在hive cli 输入以下查询计划语句

```mysql
select a.id,b.user_name from test1 a join test2 b on a.id=b.id;
```

问：**上面这条 join 语句会过滤 id 为 null 的值吗**

执行下面语句：

```mysql
explain select a.id,b.user_name from test1 a join test2 b on a.id=b.id;
```

我们来看结果 (为了适应页面展示，仅截取了部分输出信息)：

```text
TableScan
 alias: a
 Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
 Filter Operator
    predicate: id is not null (type: boolean)
    Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
    Select Operator
        expressions: id (type: int)
        outputColumnNames: _col0
        Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
        HashTable Sink Operator
           keys:
             0 _col0 (type: int)
             1 _col0 (type: int)
 ...
```

从上述结果可以看到 **predicate: id is not null** 这样一行，**说明 join 时会自动过滤掉关联字段为 null 值的情况，但 left join 或 full join 是不会自动过滤的**，大家可以自行尝试下。

## 2. group by 分组语句会进行排序吗？

看下面这条sql

```mysql
select id,max(user_name) from test1 group by id;
```

问：**group by 分组语句会进行排序吗**

直接来看 explain 之后结果 (为了适应页面展示，仅截取了部分输出信息)

```text
 TableScan
    alias: test1
    Statistics: Num rows: 9 Data size: 108 Basic stats: COMPLETE Column stats: NONE
    Select Operator
        expressions: id (type: int), user_name (type: string)
        outputColumnNames: id, user_name
        Statistics: Num rows: 9 Data size: 108 Basic stats: COMPLETE Column stats: NONE
        Group By Operator
           aggregations: max(user_name)
           keys: id (type: int)
           mode: hash
           outputColumnNames: _col0, _col1
           Statistics: Num rows: 9 Data size: 108 Basic stats: COMPLETE Column stats: NONE
           Reduce Output Operator
             key expressions: _col0 (type: int)
             sort order: +
             Map-reduce partition columns: _col0 (type: int)
             Statistics: Num rows: 9 Data size: 108 Basic stats: COMPLETE Column stats: NONE
             value expressions: _col1 (type: string)
 ...
```

我们看 Group By Operator，里面有 keys: id (type: int) 说明按照 id 进行分组的，再往下看还有 sort order: + ，**说明是按照 id 字段进行正序排序的**。

## 3. 哪条sql执行效率高呢？

观察两条sql语句

```mysql
SELECT
    a.id,
    b.user_name
FROM
    test1 a
JOIN test2 b ON a.id = b.id
WHERE
    a.id > 2;
    
SELECT
    a.id,
    b.user_name
FROM
    (SELECT * FROM test1 WHERE id > 2) a
JOIN test2 b ON a.id = b.id;
```

**这两条sql语句输出的结果是一样的，但是哪条sql执行效率高呢**
有人说第一条sql执行效率高，因为第二条sql有子查询，子查询会影响性能
有人说第二条sql执行效率高，因为先过滤之后，在进行join时的条数减少了，所以执行效率就高了

到底哪条sql效率高呢，我们直接在sql语句前面加上 explain，看下执行计划不就知道了嘛

在第一条sql语句前加上 explain，得到如下结果

```text
hive (default)> explain select a.id,b.user_name from test1 a join test2 b on a.id=b.id where a.id >2;
OK
Explain
STAGE DEPENDENCIES:
  Stage-4 is a root stage
  Stage-3 depends on stages: Stage-4
  Stage-0 depends on stages: Stage-3

STAGE PLANS:
  Stage: Stage-4
    Map Reduce Local Work
      Alias -> Map Local Tables:
        $hdt$_0:a
          Fetch Operator
            limit: -1
      Alias -> Map Local Operator Tree:
        $hdt$_0:a
          TableScan
            alias: a
            Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
            Filter Operator
              predicate: (id > 2) (type: boolean)
              Statistics: Num rows: 2 Data size: 25 Basic stats: COMPLETE Column stats: NONE
              Select Operator
                expressions: id (type: int)
                outputColumnNames: _col0
                Statistics: Num rows: 2 Data size: 25 Basic stats: COMPLETE Column stats: NONE
                HashTable Sink Operator
                  keys:
                    0 _col0 (type: int)
                    1 _col0 (type: int)

  Stage: Stage-3
    Map Reduce
      Map Operator Tree:
          TableScan
            alias: b
            Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
            Filter Operator
              predicate: (id > 2) (type: boolean)
              Statistics: Num rows: 2 Data size: 25 Basic stats: COMPLETE Column stats: NONE
              Select Operator
                expressions: id (type: int), user_name (type: string)
                outputColumnNames: _col0, _col1
                Statistics: Num rows: 2 Data size: 25 Basic stats: COMPLETE Column stats: NONE
                Map Join Operator
                  condition map:
                       Inner Join 0 to 1
                  keys:
                    0 _col0 (type: int)
                    1 _col0 (type: int)
                  outputColumnNames: _col0, _col2
                  Statistics: Num rows: 2 Data size: 27 Basic stats: COMPLETE Column stats: NONE
                  Select Operator
                    expressions: _col0 (type: int), _col2 (type: string)
                    outputColumnNames: _col0, _col1
                    Statistics: Num rows: 2 Data size: 27 Basic stats: COMPLETE Column stats: NONE
                    File Output Operator
                      compressed: false
                      Statistics: Num rows: 2 Data size: 27 Basic stats: COMPLETE Column stats: NONE
                      table:
                          input format: org.apache.hadoop.mapred.SequenceFileInputFormat
                          output format: org.apache.hadoop.hive.ql.io.HiveSequenceFileOutputFormat
                          serde: org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe
      Local Work:
        Map Reduce Local Work

  Stage: Stage-0
    Fetch Operator
      limit: -1
      Processor Tree:
        ListSink
```

在第二条sql语句前加上 explain，得到如下结果

```text
hive (default)> explain select a.id,b.user_name from(select * from  test1 where id>2 ) a join test2 b on a.id=b.id;
OK
Explain
STAGE DEPENDENCIES:
  Stage-4 is a root stage
  Stage-3 depends on stages: Stage-4
  Stage-0 depends on stages: Stage-3

STAGE PLANS:
  Stage: Stage-4
    Map Reduce Local Work
      Alias -> Map Local Tables:
        $hdt$_0:test1
          Fetch Operator
            limit: -1
      Alias -> Map Local Operator Tree:
        $hdt$_0:test1
          TableScan
            alias: test1
            Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
            Filter Operator
              predicate: (id > 2) (type: boolean)
              Statistics: Num rows: 2 Data size: 25 Basic stats: COMPLETE Column stats: NONE
              Select Operator
                expressions: id (type: int)
                outputColumnNames: _col0
                Statistics: Num rows: 2 Data size: 25 Basic stats: COMPLETE Column stats: NONE
                HashTable Sink Operator
                  keys:
                    0 _col0 (type: int)
                    1 _col0 (type: int)

  Stage: Stage-3
    Map Reduce
      Map Operator Tree:
          TableScan
            alias: b
            Statistics: Num rows: 6 Data size: 75 Basic stats: COMPLETE Column stats: NONE
            Filter Operator
              predicate: (id > 2) (type: boolean)
              Statistics: Num rows: 2 Data size: 25 Basic stats: COMPLETE Column stats: NONE
              Select Operator
                expressions: id (type: int), user_name (type: string)
                outputColumnNames: _col0, _col1
                Statistics: Num rows: 2 Data size: 25 Basic stats: COMPLETE Column stats: NONE
                Map Join Operator
                  condition map:
                       Inner Join 0 to 1
                  keys:
                    0 _col0 (type: int)
                    1 _col0 (type: int)
                  outputColumnNames: _col0, _col2
                  Statistics: Num rows: 2 Data size: 27 Basic stats: COMPLETE Column stats: NONE
                  Select Operator
                    expressions: _col0 (type: int), _col2 (type: string)
                    outputColumnNames: _col0, _col1
                    Statistics: Num rows: 2 Data size: 27 Basic stats: COMPLETE Column stats: NONE
                    File Output Operator
                      compressed: false
                      Statistics: Num rows: 2 Data size: 27 Basic stats: COMPLETE Column stats: NONE
                      table:
                          input format: org.apache.hadoop.mapred.SequenceFileInputFormat
                          output format: org.apache.hadoop.hive.ql.io.HiveSequenceFileOutputFormat
                          serde: org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe
      Local Work:
        Map Reduce Local Work

  Stage: Stage-0
    Fetch Operator
      limit: -1
      Processor Tree:
        ListSink
```

大家有什么发现，除了表别名不一样，其他的执行计划完全一样，都是先进行 where 条件过滤，在进行 join 条件关联。**说明 hive 底层会自动帮我们进行优化，所以这两条sql语句执行效率是一样的**。