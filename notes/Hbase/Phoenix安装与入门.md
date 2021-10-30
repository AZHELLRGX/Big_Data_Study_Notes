# Phoenix入门

## 安装

官网现在已经不支持低版本下载了，可以通过其他渠道获取

> [Index of /dist/phoenix (apache.org)](http://archive.apache.org/dist/phoenix/)

### 环境准备

下载安装包后，将包内的所有phoenix开头的jar包拷贝到HBase的目录，需要集群每个节点都拷贝一份

在hbase-site.xml添加两个属性，主要是为了后续HBase新建的命名空间可以作为phoenix的schema使用

```xml
<property>
    <name>phoenix.schema.isNamespaceMappingEnabled</name>
    <value>true</value>
</property>
<property>
    <name>phoenix.schema.mapSystemTablesToNamespace</name>
    <value>true</value>
</property>
```

最后将HBase的hbase-site.xml配置拷贝一份到phoenix的bin目录

当然也可以将加入环境变量/etc/profile

```properties
# phoenix config
export PHOENIX_HOME=/home/hmaster/MyCloudera/APP/apache-phoenix-4.14.1-HBase-1.2-bin
export PHOENIX_CLASSPATH=$PHOENIX_HOME
export PATH=$PHOENIX_HOME/bin:$PATH
```

### 重启HBase集群

```shell
stop-hbase.sh
start-hbase.sh
```

### 进入phoenix

```shell
# 连接zk启动
sqlline.py 192.168.2.101:2181
# 出现如下日式则成功
0: jdbc:phoenix:192.168.2.101:2181>
# 查看所有表
!tables
# 退出
!exit
```



## 测试

### 在HBase建表插入数据

```shell
create 'rgx:phoenix','info'
put 'rgx:phoenix', 'row001','info:name','phoenix'
put 'rgx:phoenix', 'row002','info:name','hbase'
```

### 在phoenix创建Schema

```sql
CREATE SCHEMA IF NOT EXISTS "rgx";
```

### 创建表

```sql
create table "rgx"."phoenix"(
"ROW" varchar primary key,
 "info"."name" varchar)
column_encoded_bytes=0;
# 注意最后一行一定要加上，否则读取数据存在问题
```

### 查询数据

```sql
select * from "rgx"."phoenix";
```

### 插入数据

```sql
UPSERT INTO "rgx"."phoenix" ("ROW","info"."name") values('row003','flink');
# 插入完成HBase也可以看到这个数据了
```

