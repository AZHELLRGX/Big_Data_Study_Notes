## OLAP平台都有哪些？

Hive、SparkSQL、FlinkSQL、Clickhouse、Elasticsearch、Druid、Kylin、Presto、Impala等；

根据分类的话，大致分为：

- __MOLAP__【Multi-dimensional OLAP】（基于直接支持多维数据和操作的本机逻辑模型，一般都存在数据预处理或者预计算，典型代表就是Kylin和Druid）

- __ROLAP__【Relational OLAP】（收到Query请求时，会先解析Query，生成执行计划，扫描数据，执行关系型算子，然后直接对元数据进行一系列过滤聚合操作，不存在数据预处理，所以性能稍差，但是灵活性很高，Presto，Impala，GreenPlum，Clickhouse，Elasticsearch，Hive，Spark SQL，Flink SQL这些都是ROLAP）

- __混合OLAP__ 【Hybrid OLAP 】（是 MOLAP 和 ROLAP 的一种融合。当查询聚合性数据的时候，使用MOLAP 技术；当查询明细数据时，使用 ROLAP 技术）