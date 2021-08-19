[toc]

## 命名空间

### 介绍
在HBase中，namespace命名空间指对一组表的逻辑分组，类似RDBMS中的database，方便对表在业务上划分。Apache HBase从0.98.0, 0.95.2两个版本开始支持namespace级别的授权操作，HBase全局管理员可以创建、修改和回收namespace的授权。

### namespace

HBase系统默认定义了两个缺省的namespace

- **hbase**：系统内建表，包括namespace和meta表
- **default**：用户建表时未指定namespace的表都创建在此

### 操作namespace

#### 创建

```shell
create_namespace 'rgx'
```

#### 删除

```shell
drop_namespace 'rgx'
# 删除之前需要保证namespace为empty（现将表下线删除）
```

#### 查看

```shell
describe_namespace 'rgx'
```

#### 在命名空间下面创建表

```shell
# 格式大致为 create '<namespace>:<table name>' '<column family name>'
create 'rgx:student' 'info'
```

#### 查看命名空间下面所有的表

```shell
list_namespace_tables 'rgx'
```

### 授权

#### 授权用户对命名空间的写权限

```shell
grant 'user-A' 'W' '@rgx'
```

#### 回收用户对命名空间的所有权限
```shell
revoke 'user-A' '@rgx'
```

#### 授权操作示例

> **当前用户：hbase**
>
> ```shell
> create_namespace 'hbase_perf'
> grant 'mike', 'W', '@hbase_perf'
> ```
>
> **当前用户：mike**
>
> ```shell
> create 'hbase_perf.table20', 'family1'
> create 'hbase_perf.table50', 'family1'
> ```
>
> > mike创建了两张表table20和table50，同时成为这两张表的owner，意味着有'RWXCA'权限；此时，mike团队的另一名成员alice也需要获得hbase_perf下的权限，hbase管理员操作如下
>
> **当前用户：hbase**
>
> ```shell
> grant 'alice', 'W', '@hbase_perf'
> ```
>
> > 此时alice可以在hbase_perf下创建表，但是无法读、写、修改和删除hbase_perf下已存在的表
>
> **当前用户：alice**
>
> ```shell
> scan 'hbase_perf:table20'
> ```
>
> > 报错AccessDeniedException
> >
> > 如果希望alice可以访问已经存在的表，则hbase管理员操作如下
>
> **当前用户：hbase**
>
> ```shell
> grant 'alice', 'RW', 'hbase_perf.table20'
> grant 'alice', 'RW', 'hbase_perf.table50'
> ```

### 在HBase中启用授权机制

> **hbase-site.xml**
>
> ```xml
> <property>
>      <name>hbase.security.authorization</name>
>      <value>true</value>
> </property>
> <property>
>      <name>hbase.coprocessor.master.classes</name>
>      <value>org.apache.hadoop.hbase.security.access.AccessController</value>
> </property>
> <property>
>      <name>hbase.coprocessor.region.classes</name><value>org.apache.hadoop.hbase.security.token.TokenProvider,org.apache.hadoop.hbase.security.access.AccessController</value>
> </property>
> ```
>
> 配置完成后需要重启HBase集群

## 表的管理

### 操作表

#### 查看有哪些表

```shell
list
```

#### 创建表

```shell
# 语法：create <table>, {NAME => <family>, VERSIONS => <VERSIONS>}
# 例如：创建表t1，有两个family name：f1，f2，且版本数均为2
create 't1',{NAME => 'f1', VERSIONS => 2},{NAME => 'f2', VERSIONS => 2}
```

> **创建表t1,列族为f1,列族版本号为5,命令如下**
>
> ```shell
> create 't1',{NAME=>'f1',VERSIONS=>5}
> ```
>
> **或者使用如下等价的命令**
>
> ```shell
> create 't2','f1','f2','f3'
> ```
>
> **创建表t1,将表依据分割算法HexStringSplit分布在15个Region里,命令如下:**
>
> ```shell
> create 't1','f1',{NUMREGIONS=>15,SPLITALGO=>'HexStringSplit'}
> ```
>
> **创建表t1,指定切分点,命令如下:**
>
> ```shell
> create 't1','f1',{SPLITS=>['10','20','30','40']}
> ```
>
> **put:向表/行/列指定的单元格添加数据**
>
> **向表t1中行row1,列f1:1,添加数据value1,时间戳为1625916578,命令如下:**
>
> ```shell
> put 't1','row1','f1:1','value1',1625916578
> ```

#### 删除表

分两步：首先disable，然后drop,例如：删除表t1

```shell
disable 't1' # 下线
drop 't1' # 删除
```

#### 查看表的结构

```shell
# 语法：describe <table>
# 例如：查看表t1的结构
describe 't1'
```

#### 修改表结构

```shell
# 修改表结构必须先disable
# 语法示例：alter 't1', {NAME => 'f1'}, {NAME => 'f2', METHOD => 'delete'}
# 例如：修改表test1的cf的TTL为180天
disable 'test1'
alter 'test1',{NAME=>'body',TTL=>'15552000'},{NAME=>'meta', TTL=>'15552000'}
enable 'test1'
```

### 权限管理

#### 分配权限

```shell
# 语法 : grant <user> <permissions> <table> <column family> <column qualifier> 
# 参数后面用逗号分隔权限用五个字母表示： "RWXCA".
# READ('R'), WRITE('W'), EXEC('X'), CREATE('C'), ADMIN('A')
# 例如，给用户‘user-A'分配对表t1有读写的权限，
grant 'user-A','RW','t1'
```

#### 查看权限

```shell
# 语法：user_permission <table>
# 例如，查看表t1的权限列表
user_permission 't1'
```

#### 收回权限

```shell
# 与分配权限类似，语法：revoke <user> <table> <column family> <column qualifier>
# 例如，收回test用户在表t1上的权限
revoke 'test','t1'
```

### 表数据的增删改查

#### 添加数据

```shell
# 语法：put <table>,<rowkey>,<family:column>,<value>,<timestamp>
# 例如：给表t1的添加一行记录：rowkey是rowkey001，family name：f1，column name：col1，value：value01，timestamp：系统默认
put 't1','rowkey001','f1:col1','value01'
```

#### 查询某行记录

```shell
# 语法：get <table>,<rowkey>,[<family:column>,....]
# 例如：查询表t1，rowkey001中的f1下的col1的值
get 't1','rowkey001', 'f1:col1'
# 或者：
get 't1','rowkey001', {COLUMN=>'f1:col1'}
# 查询表t1，rowke002中的f1下的所有列值
get 't1','rowkey001'
```

#### 扫描表

```shell
# 语法：scan <table>, {COLUMNS => [ <family:column>,.... ], LIMIT => num}
# 另外，还可以添加STARTROW、TIMERANGE和FITLER等高级功能
# 例如：扫描表t1的前5条数据
scan 't1',{LIMIT=>5}
# 查询区间内的数据
scan 't1',{STARTROW=>1001,STOPROW=>1004} # 这是一个左闭右开的范围
# 数据的输出顺序是按照rowKey的字典顺序
```

#### 查询表中的数据行数

```shell
# 语法：count <table>, {INTERVAL => intervalNum, CACHE => cacheNum}
# INTERVAL设置多少行显示一次及对应的rowkey，默认1000；CACHE每次去取的缓存区大小，默认是10，调整该参数可提高查询速度
# 例如，查询表t1中的行数，每100条显示一次，缓存区为500
count 't1', {INTERVAL => 100, CACHE => 500}
```

### 删除数据

#### 删除行中的某个列值

```shell
# 语法：delete <table>, <rowkey>,  <family:column> , <timestamp>,必须指定列名
# 例如：删除表t1，rowkey001中的f1:col1的数据
delete 't1','rowkey001','f1:col1'
# 注：将删除该行f1:col1列所有版本的数据
```

#### 删除行

```shell
# 语法：deleteall <table>, <rowkey>,  <family:column> , <timestamp>，可以不指定列名，删除整行数据
# 例如：删除表t1，rowk001的数据
deleteall 't1','rowkey001'
```

#### 删除表中的所有数据

```shell
# 语法： truncate <table>
# 其具体过程是：disable table -> drop table -> create table
# 例如：删除表t1的所有数据
truncate 't1'
```

### Region管理

#### 移动region

```shell
# 语法：move 'encodeRegionName', 'ServerName'
# encodeRegionName指的regioName后面的编码，ServerName指的是master-status的Region Servers列表
# 示例
move '4343995a58be8e5bbc739af1e91cd72d', 'db-41.xxx.xxx.org,60020,1390274516739'
```

#### 开启/关闭region

```shell
# 语法：balance_switch true|false
balance_switch
```

#### 手动split

> 语法：split 'regionName', 'splitKey'

#### 手动触发major compaction

```shell
# Compact all regions in a table:
major_compact 't1'
# Compact an entire region:
major_compact 'r1'
# Compact a single column family within a region:
major_compact 'r1', 'c1'
# Compact a single column family within a table:
major_compact 't1', 'c1'
```
