### Flink程序常见的提交方式有三种 

常用提交方式分为local，standalone，yarn三种。

- local：本地提交项目，可纯粹的在本地单节点运行，也可以将本地代码提交到远端flink集群运行。
- standalone：flink集群自己完成资源调度，不依赖于其他资源调度器，需要手动启动flink集群。
- yarn：依赖于hadoop yarn资源调度器，由yarn负责资源调度，不需要手动启动flink集群。需要先启动yarn和hdfs。又分为yarn-session和yarn-cluster两种方式。提交Flink任务时，所在机器必须要至少设置环境变量YARN_CONF_DIR、HADOOP_CONF_DIR、HADOOP_CONF_PATH中的一个，才能读取YARN和HDFS的配置信息（会按三者从左到右的顺序读取，只要发现一个就开始读取。如果没有正确设置，会尝试使用HADOOP_HOME/etc/hadoop），否则提交任务会失败。

### 以下是两种集群提交模式

```shell
# standalone模式
flink run -c com.atguigu.wc.StreamWordCount -p 2 FlinkTutorial-1.0-SNAPSHOT.jar --host 192.168.2.101 --port 7777
```

```shell
# Per Job Cluster提交
flink run -m yarn-cluster -ys 10 -p 20  -yjm 1024 -ytm 1024  -c com.atguigu.wc.StreamWordCount FlinkTutorial-1.0-SNAPSHOT.jar --host 192.168.2.101 --port 7777
```

### 并行度和TaskManager的关系

- -ys 设置一个taskmanager的slot个数
- -p 设置任务的并行度　

> - taskmanager的数量 = p / ys + 1
> - jobmanager的数量 = 1
> - 消耗的container的数量 = TaskManager的数量+1，+1是因为还有JobManager这个进程。
> - 消耗的vcore的数量 = TaskManager的数量 * solt + 1，+1理由同上
> - 消耗的yarn内存数 = jobmanager的数量 * yjm + taskmanager的数量 * ytm 

### yarn模式提交参数说明

```shell
-m ：yarn-cluster，代表启动单session提交单一job
-yjm：jobmanager的内存占用
-ytm：每个taskmanager的内存占用
-ys: 每个taskmanager可使用的CPU核数
-ynm：application 名称
-yqu：指定job的队列名称
-c： 程序主入口+ jar path
-p: 指定任务的并行度
-yD： 动态参数设置
```

### 总部101环境搭建Flink测试环境进行测试【Flink版本：1.12.1】

```shell
# 方式一
flink run -c com.chinaunicom.app.DecoderCmApp -p 5 lte-wireless-cloud-analysis-1.0.0-alpha.jar --checkpointURI hdfs://master:9000/wcs/liantong/tmp --province zj

# 方式二
# 如果使用到了Hadoop环境，需要先指定hadoop编码
export HADOOP_CLASSPATH=`hadoop classpath`
export HADOOP_CLASSPATH=`${HADOOP_HOME}/bin/hadoop classpath`

flink run -m yarn-cluster -c com.chinaunicom.app.DecoderCmApp lte-wireless-cloud-analysis-1.0.0-alpha.jar --checkpointURI hdfs://master:9000/wcs/liantong/tmp --province zj

flink run -m yarn-cluster -ynm '浙江CM数据处理' -ys 10 -p 30  -yjm 2048 -ytm 2048 -c com.chinaunicom.app.DecoderCmApp lte-wireless-cloud-analysis-1.0.0-alpha.jar --checkpointURI hdfs://master:9000/wcs/liantong/tmp --province zj

# 方式三 参考如下部署模式 使用run-application方式部署
# dependency这个依赖必须将flink目录的lib和plugins下面所有包包含在内
flink run-application -t yarn-application \
-Dyarn.provided.lib.dirs='hdfs://master:9000/usr/rgx/flink/dependency' \
-Dyarn.application.name='MT_CMPM_ZJ_CM' \
-Djobmanager.memory.process.size=1024m \
-Dtaskmanager.memory.process.size=1024m \
-Dtaskmanager.numberOfTaskSlots=2 \
-Dparallelism.default=10 \
-c com.chinaunicom.app.DecoderCmApp lte-wireless-cloud-analysis-1.0.0-alpha.jar --checkpointURI hdfs://master:9000/wcs/liantong/tmp --province zj

flink run-application -t yarn-application \
-Dyarn.provided.lib.dirs='hdfs://master:9000/usr/rgx/flink/dependency' \
-Dyarn.application.name='MT_CMPM_ZJ_PM_RGX' \
-Djobmanager.memory.process.size=1024m \
-Dtaskmanager.memory.process.size=1024m \
-Dtaskmanager.numberOfTaskSlots=2 \
-Dparallelism.default=2 \
-c com.chinaunicom.app.DecoderPmApp lte-wireless-cloud-analysis-1.0.0-alpha.jar --checkpointURI hdfs://master:9000/wcs/liantong/tmp --province zj
```



### 联通集团发包

```shell
export HADOOP_CLASSPATH=`hadoop classpath`
kinit zb_zhw_cmpm_mt@ODATA.NCMP.UNICOM.LOCAL -kt /home/zb_zhw_cmpm_mt/zb_zhw_cmpm_mt.keytab
# 方式三 参考如下部署模式 使用run-application方式部署
flink run-application -t yarn-application \
-Dyarn.application.queue=default \
-Dyarn.provided.lib.dirs='hdfs://hdfsunity/user/zb_zhw_cmpm_mt/flink/dependency;hdfs://hdfsunity/user/zb_zhw_cmpm_mt/flink/lib;hdfs://hdfsunity/user/zb_zhw_cmpm_mt/flink/plugins' \
-Dyarn.application.name='MT_CMPM_ZJ_CM' \
-Djobmanager.memory.process.size=2048m \
-Dtaskmanager.memory.process.size=4096m \
-Dtaskmanager.numberOfTaskSlots=2 \
-Dparallelism.default=20 \
-Dsecurity.kerberos.login.keytab=/home/zb_zhw_cmpm_mt/zb_zhw_cmpm_mt.keytab \
-Dsecurity.kerberos.login.principal=zb_zhw_cmpm_mt@ODATA.NCMP.UNICOM.LOCAL \
-Dsecurity.kerberos.login.contexts=Client,KafkaClient \
-Djava.security.krb5.conf=/etc/krb5.conf \
-c com.chinaunicom.app.DecoderCmApp lte-wireless-cloud-analysis-1.0.0-release.jar --checkpointURI hdfs://hdfsunity/user/zb_zhw_cmpm_mt/flink/checkpoint --province zj
```



### 打包方式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
		 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		 xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>org.example</groupId>
	<groupId>com.chinaunicom</groupId>
	<artifactId>lte-wireless-cloud-analysis</artifactId>
	<version>1.0-SNAPSHOT</version>

	<version>1.0.0</version>
	<name>联通集团4G PMCM项目</name>
	<description>
		中国联通集团4G PMCM实时计算Flink代码工程
	</description>
	<properties>
		<maven.compiler.source>8</maven.compiler.source>
		<maven.compiler.target>8</maven.compiler.target>
		<hadoop.version>2.8.2</hadoop.version>
		<flink.version>1.12.0</flink.version>
		<kafka.version>0.10.1.1</kafka.version>
		<scala.binary.version>2.11</scala.binary.version>

	</properties>

	<profiles>
		<profile>
			<id>prod</id>
			<properties>
				<profiles.active>prod</profiles.active>
				<version.tag>release</version.tag>
			</properties>
		</profile>
		<profile>
			<id>test</id>
			<properties>
				<profiles.active>test</profiles.active>
				<version.tag>beta</version.tag>
			</properties>
		</profile>
		<profile>
			<id>dev</id>
			<properties>
				<profiles.active>dev</profiles.active>
				<version.tag>alpha</version.tag>
			</properties>
			<activation>
				<activeByDefault>true</activeByDefault>
			</activation>
		</profile>
	</profiles>

	<dependencies>
		<!-- flink依赖 -->
	</dependencies>

    <!-- 打包插件，最终会出现一个dependency包，将其上传hdfs集群，使用run application方式运行 -->
	<build>
		<finalName>${artifactId}-${version}-${version.tag}</finalName>
		<resources>
			<resource>
				<directory>src/main/resources</directory>
				<excludes>
					<exclude>dev/*</exclude>
					<exclude>prod/*</exclude>
					<exclude>test/*</exclude>
				</excludes>
			</resource>
			<resource>
				<directory>src/main/resources/${profiles.active}</directory>
			</resource>
		</resources>

		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-dependency-plugin</artifactId>
				<version>3.1.1</version>
				<executions>
					<execution>
						<id>copy-dependencies</id>
						<phase>package</phase>
						<goals>
							<goal>copy-dependencies</goal>
						</goals>
						<configuration>
							<excludeTransitive>false</excludeTransitive>
							<stripVersion>true</stripVersion>
							<includeScope>runtime</includeScope>
						</configuration>
					</execution>
				</executions>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-jar-plugin</artifactId>
				<executions>
					<execution>
						<id>app-jar</id>
						<goals>
							<goal>jar</goal>
						</goals>
					</execution>
					<execution>
						<id>app-test-jar</id>
						<goals>
							<goal>test-jar</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>

</project>
```

另外一种打包方式

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.chinaunicom</groupId>
    <artifactId>lte-wireless-cloud-analysis</artifactId>
    <version>1.0.0</version>
    <name>联通集团4G PMCM项目</name>
    <description>
        中国联通集团4G PMCM实时计算Flink代码工程
    </description>
    <properties>
        <maven.compiler.source>8</maven.compiler.source>
        <maven.compiler.target>8</maven.compiler.target>
        <!--        <hadoop.version>2.8.2</hadoop.version>-->
        <!-- 联通集团hadoop版本 -->
        <hadoop.version>3.3.0</hadoop.version>
        <flink.version>1.12.1</flink.version>
        <scala.binary.version>2.11</scala.binary.version>
        <!-- 测试组件版本 -->
        <junit.version>4.12</junit.version>
        <mockito.version>2.21.0</mockito.version>
        <powermock.version>2.0.2</powermock.version>
        <hbase.version>1.2.5</hbase.version>
        <!-- 文件拷贝时的编码 -->
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <!-- 编译时的编码 -->
        <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
    </properties>

    <profiles>
        <profile>
            <id>prod</id>
            <properties>
                <profiles.active>prod</profiles.active>
                <version.tag>release</version.tag>
            </properties>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
        </profile>
        <profile>
            <id>test</id>
            <properties>
                <profiles.active>test</profiles.active>
                <version.tag>beta</version.tag>
            </properties>
        </profile>
        <profile>
            <id>dev</id>
            <properties>
                <profiles.active>dev</profiles.active>
                <version.tag>alpha</version.tag>
            </properties>
        </profile>
    </profiles>

    <dependencies>

        <!-- ================================================================================ -->
        <!--                                   Flink 依赖                                      -->
        <!-- ================================================================================ -->

        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-java</artifactId>
            <version>${flink.version}</version>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-clients_${scala.binary.version}</artifactId>
            <version>${flink.version}</version>
            <scope>provided</scope>
        </dependency>
        <!-- 以上属于flink的核心依赖，无需打包，而下面属于是连接器类型的依赖，必须打包 -->
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-statebackend-rocksdb_${scala.binary.version}</artifactId>
            <version>${flink.version}</version>
        </dependency>
        <!-- flink connector -->
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-connector-kafka_${scala.binary.version}</artifactId>
            <version>${flink.version}</version>
        </dependency>
        <!-- ================================================================================ -->
        <!--                                  Hadoop 依赖                                      -->
        <!-- ================================================================================ -->
        <!-- 联通集团的Flink目录有3.3版本的common包，不知道是否可以不打进去，后续试试 -->
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-common</artifactId>
            <version>${hadoop.version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>${hadoop.version}</version>
        </dependency>

        <!-- ================================================================================ -->
        <!--                                  HBase 依赖                                      -->
        <!-- ================================================================================ -->
        <dependency>
            <groupId>org.apache.hbase</groupId>
            <artifactId>hbase-client</artifactId>
            <version>${hbase.version}</version>
        </dependency>

        <!-- ================================================================================ -->
        <!--                                   测试依赖                                        -->
        <!-- ================================================================================ -->

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>${junit.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>${mockito.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.powermock</groupId>
            <artifactId>powermock-module-junit4</artifactId>
            <version>${powermock.version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.powermock</groupId>
            <artifactId>powermock-api-mockito2</artifactId>
            <version>${powermock.version}</version>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.mockito</groupId>
                    <artifactId>mockito-core</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.apache.flink</groupId>
            <artifactId>flink-tests</artifactId>
            <version>${flink.version}</version>
            <type>test-jar</type>
            <scope>test</scope>
        </dependency>

    </dependencies>

    <!-- 直接将依赖的源代码编译在jar里面，这种打包出来会比较大 -->
    <build>
        <finalName>${artifactId}-${version}-${version.tag}</finalName>
        <resources>
            <resource>
                <directory>src/main/resources</directory>
                <excludes>
                    <exclude>dev/*</exclude>
                    <exclude>prod/*</exclude>
                    <exclude>test/*</exclude>
                </excludes>
            </resource>
            <resource>
                <directory>src/main/resources/${profiles.active}</directory>
            </resource>
        </resources>
		
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-dependency-plugin</artifactId>
                <version>3.1.1</version>
                <executions>
                    <execution>
                        <id>copy-dependencies</id>
                        <phase>package</phase>
                        <goals>
                            <goal>copy-dependencies</goal>
                        </goals>
                        <configuration>
                            <excludeTransitive>false</excludeTransitive>
                            <stripVersion>true</stripVersion>
                            <includeScope>runtime</includeScope>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>2.4</version>
                <executions>
                    <execution>
                        <id>app-jar</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>app-test-jar</id>
                        <goals>
                            <goal>test-jar</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
            <!-- maven 打包插件 -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>2.4.3</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer
                                        implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <!-- 指定main的位置 -->
                                    <mainClass>*</mainClass>
                                </transformer>
                            </transformers>
                            <filters>
                                <filter>
                                    <!-- 过滤不需要的jar包，本地调试的时候需要将这个注释掉 -->
                                    <artifact>*:*</artifact>

                                    <excludes>
                                        <exclude>META-INF/*.SF</exclude>
                                        <exclude>META-INF/*.DSA</exclude>
                                        <exclude>META-INF/*.RSA</exclude>
                                    </excludes>
                                </filter>
                            </filters>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

</project>
```

### 启动错误如何排查

监控界面往往存在一些问题，看不到全量的日志，可以使用如下命令查看完整日志

```shell
yarn logs -applicationId application_1633658820683_0126
```

监控界面

```
http://x86-gx-0013.odata.ncmp.unicom.local:23188/cluster/apps/RUNNING
```

### HBase建表与删除

```shell
create 'zb_zhw_cmpm_mt:ods_cm_lte_zj_hw_d',{NAME => 'f'}
create 'zb_zhw_cmpm_mt:dim_lte_cell',{NAME => 'f'}
create 'zb_zhw_cmpm_mt:dim_lte_enodeb',{NAME => 'f'}
create 'zb_zhw_cmpm_mt:ads_dama_lte_log',{NAME => 'f'}


truncate 'zb_zhw_cmpm_mt:ods_cm_lte_zj_hw_d'
truncate 'zb_zhw_cmpm_mt:dim_lte_cell'
truncate 'zb_zhw_cmpm_mt:dim_lte_enodeb'
truncate 'zb_zhw_cmpm_mt:ads_dama_lte_log'
```

