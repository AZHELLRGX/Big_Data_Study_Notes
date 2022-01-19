package org.azhell.learn.flink.tableapi;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.azhell.learn.flink.sql.ConnectSQL;
import org.azhell.learn.flink.sql.CrudSQL;

/**
 * 测试table的通用API
 */
public class TestCommonApi {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 新版本默认使用的就是blink
        // 使用blink、流式
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        // 连接外部系统，读取数据  1.10.1的版本还可以试用connect方法创建，但是最新版本已经将其标记为废除
        // 参考官方文档，现在已经推荐使用sql来操作
        // 目前最新的1.14.0提供了tableEnv.createTemporaryTable用来创立connector
        // file-source
        tableEnv.executeSql(ConnectSQL.fileDataSql());
        Table table = tableEnv.sqlQuery(CrudSQL.selectFromSensor());
        // Table table = tableEnv.from("Sensor").select()
        tableEnv.toAppendStream(table, Row.class).print();
        DataStreamSource<String> stringDataStreamSource = env.socketTextStream("192.168.2.101", 7777);
        // kafka-sink
        tableEnv.executeSql(ConnectSQL.kafkaDataSql());
        tableEnv.fromDataStream(stringDataStreamSource).executeInsert("KafkaTestSink");
        env.execute();
    }
}
