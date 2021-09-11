# sparksql-universal

问题记录

- [x] 多个同名数据库数据需要去重

- [x] ```sql
  -- 支持类似create语句的hdfs数据字段映射
  tb_tablename
  (
  citid int 1;
  
  )
  ```

- [x] properties和xml配置可以按照APP name 划分文件夹，进而读取
- [x] 多天数据【脚本、xml支持】（范围、或者枚举）、多地市支持
- [x] 数据库连接串密文

- [x] 打通脉络，实现基本的数据读取、交互、输出
- [x] 绘制详细的程序脉络图、架构图
- [x] 添加对udf函数的支持、缓存支持等功能
- [x] driver 阶段根据输出配置文件中配置的天表创建规则创建天表、对多地市数据读取的支持、通配符替换
- [x] 逐步添加其他特性支持
- [x] 程序日志
- [x] 脚本参数配置 --deploy-mode
- [x] 按照任务来划分XML文件
- [ ] 支持yaml配置



一些基本不需要调整的参数可以预先配置

```properties
# 该参数默认为10M,在进行join等聚合操作时，将小于该值的表broadcast到每台worker，消除了大量的shuffle操作
spark.sql.autoBroadcastJoinThreshold
# 将rdd存入mem或disk前再进行一次压缩，效果显著，我使用cacheTable了一张表，没有开启该参数前总共cache了54G数据,开启这个参数后只34G,可是执行速度并没有收到太大的影响。
spark.rdd.compress=true
# 这个参数默认为200，是join等聚合操作的并行度，如果有大量的数据进行操作，造成单个任务比较重,运行时间过长的时候，会报如下的错误:
# > org.apache.spark.shuffle.FetchFailedException: Connection from /192.168.xx.xxx:53450 closed
# 这个时候需要提高该值。
spark.sql.shuffle.partitions
```

### 暂时认定为遗弃的代码备份

#### 缓存DataFrame

缓存DataFrame可能和直接缓存表的效果不一样，目前想达到的目的还是直接缓存表

```scala
// 缓存执行完sql以后的结果
def cacheSqlResult(df: DataFrame, sqlXmlEntity: SqlXmlEntity): Unit = {
    if (sqlXmlEntity.cacheMap.contains(sqlXmlEntity.registerTableName)) {
        cacheSetting(df, sqlXmlEntity.cacheMap(sqlXmlEntity.registerTableName))
    }
}

// 缓存原始数据
def cacheLoadData(df: DataFrame, sqlXmlEntity: SqlXmlEntity): Unit = {
    if (sqlXmlEntity.cacheMap.contains(sqlXmlEntity.dbTableName)) {
        cacheSetting(df, sqlXmlEntity.cacheMap(sqlXmlEntity.dbTableName))
    } else if (sqlXmlEntity.cacheMap.contains(sqlXmlEntity.sparkMapName)) {
        cacheSetting(df, sqlXmlEntity.cacheMap(sqlXmlEntity.sparkMapName))
    }
}

/**
   * 根据缓存配置设置缓存等级
   *
   * @param df     需要缓存的DataFrame
   * @param option 缓存类型配置
   */
private def cacheSetting(df: DataFrame, option: String): Unit = {
    option match {
        case "md" => df.persist(StorageLevel.MEMORY_AND_DISK)
        case "m" => df.persist(StorageLevel.MEMORY_ONLY)
        case "d" => df.persist(StorageLevel.DISK_ONLY)
    }
}
```

#### 缓存测试代码

```scala
// todo 测试
session.sql("select * from aoiDbTemp").show()
session.sql("select * from addressTemp").show()
session.sql("select * from tb_aaa_zc_point_temp").show()
// todo 测试
session.sql("SELECT * FROM simplify WHERE imsi = '262325294610128'").show()
// todo 测试
session.sql("SELECT * FROM simplify_relate_cell").show()
// todo 测试udf函数
session.sql("select imsi as imsiName,strLen(imsi) as imsiNameLen from simplify_relate_cell").show()
//    import org.apache.spark.sql.functions
//    dataframe.withColumn("a2", functions.callUDF("add_one", functions.col("a")))
```

### 性能优化

#### 数据库

- 最好让数据库表存在自增id字段，这样数据读取的时候可以依赖这个自增ID完成较均匀的数据分区
- 数据库尽量分天表存放，这样过滤数据的配置相对简单

#### 代码优化

```scala
// 下面的组合操作虽然复杂，但是对于分区设置可以更加细致 
// 但是问题也是相对的，这会导致XML的配置异常复杂
import java.util.Properties
 
val readConnProperties3 = new Properties()
readConnProperties3.put("driver", "com.mysql.jdbc.Driver")
readConnProperties3.put("user", "test")
readConnProperties3.put("password", "123456")
readConnProperties3.put("fetchsize", "2")
 
val arr = Array(
  (1, 50),
  (2, 60))
 
// 此处的条件，既可以分割数据用作并行度，也可以过滤数据
val predicates = arr.map {
  case (gender, age) =>
    s" gender = $gender " + s" AND age < $age "
}
 
val predicates1 =
  Array(
    "2017-05-01" -> "2017-05-20",
    "2017-06-01" -> "2017-06-05").map {
      case (start, end) =>
        s"cast(create_time as date) >= date '$start' " + s"AND cast(create_time as date) <= date '$end'"
    }
 
val jdbcDF3 = spark.read.jdbc(
  "jdbc:mysql://ip:3306",
  "db.user_test",
  predicates,
  readConnProperties3)
 
 
 
jdbcDF3.show
 
// 并行度为2，对应arr数组中元素的个数
jdbcDF3.rdd.partitions.size
Int = 2
```

> +---+------+---+
> |uid|gender|age|
> +---+------+---+
> |  3|     1| 30|
> |  7|     1| 25|
> |  1|     1| 18|
> |  2|     2| 20|
> |  4|     2| 40|
> |  8|     2| 35|
> +---+------+---+

```scala
object JDBCSource {
	def main(args: Array[String]): Unit = {
	    val conf = new SparkConf().setAppName("Greenplum_test").setMaster("local[*]")
	    val sc = new SparkContext(conf)
	    sc.setLogLevel("WARN")
	    val spark = SparkSession.builder().config(conf).getOrCreate()
	    
	    //由于dbtable被用作SELECT语句的源。如果要填入子查询语句，则应提供别名：
	    val tablename = "(select id,name,gender from test.info where gender='man') temp"
	    
	    val data = spark.sqlContext.read
	      .format("jdbc")
	      .option("driver", "com.mysql.jdbc.Driver")
	      .option("url", "jdbc:mysql://localhost:3306/test")
	      .option("dbtable", tablename)			//将查询语句传入
	      .option("user", "username")
	      .option("password", "password")
	      .load()

	    data.show()
  }
}
```

### 配置简化

#### 简化标签fieldMap的配置难度

下面是原来的代码备份

```xml
<!-- HDFS数据字段映射示例 -->
<fieldMap name="simplifyDataMapping">
    <field index="0" type="int">cityId</field>
    <field index="1" type="String">imsi</field>
    <field index="6" type="bigint">eci</field>
</fieldMap>
```

下面原来的解析代码备份

```scala
// 这里需要简化配置
val fieldTags = fieldMapTag \ FIELD
    for (fieldTag <- fieldTags) {
      val field = Field(fieldTag.attribute(INDEX).getOrElse(-1).toString.toInt,
      fieldTag.attribute(TYPE).getOrElse("String").toString, fieldTag.text)
      fieldList.append(field)
}
```

### UDF函数配置[废弃]

```##scala
/**
   * 解析udf函数配置
   * UDF以及UDAF函数后续统一交给大数据研发人员维护，不再配置
   *
   * @return
   */
def parsingUDFConfig(userDir: String): mutable.Map[String, UdfConfig] = {
    val udfProperties = loadProperties(s"$userDir/conf/properties/udf.properties")
    val keySet = udfProperties.stringPropertyNames()
    val udfInfoMap = mutable.Map[String, UdfConfig]()
    val iterator = keySet.iterator()
    while (iterator.hasNext) {
        val key = iterator.next()
        val data: Array[String] = udfProperties.getProperty(key).split(";")
        if (data.length < 2)
        log.warn("{}对应udf相关配置信息不全", key)
        else {
            udfInfoMap.put(key, UdfConfig(key, data(0), data(1)))
        }
    }
    // todo scala2.11版本不支持如下操作
    //    keySet.forEach(key => {
    //
    //    })
    udfInfoMap
}

// udf函数配置文件 todo udf目前暂时没找到类似hive那种使用jar创建函数的方式,如果使用支持Hive语法的SparkSession应该支持，后续可以测试一下
case class UdfConfig(funcName: String, className: String, jarName: String)
```

## 构建测试环境

