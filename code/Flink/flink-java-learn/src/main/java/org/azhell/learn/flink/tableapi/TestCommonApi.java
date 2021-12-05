package org.azhell.learn.flink.tableapi;

import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.Row;
import org.azhell.learn.flink.ConnectSQL;

/**
 * 测试table的通用API
 */
public class TestCommonApi {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 新版本默认使用的就是blink

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        // 连接外部系统，读取数据  1.10.1的版本还可以试用connect方法创建，但是最新版本已经将其标记为废除
        // 参考官方文档，现在已经推荐使用sql来操作
        tableEnv.executeSql(ConnectSQL.sourceDataSql());
        Table table = tableEnv.sqlQuery(ConnectSQL.SelectFromSensor());
        tableEnv.toAppendStream(table, Row.class).print();
        DataStreamSource<String> stringDataStreamSource = env.socketTextStream("192.168.18.188", 7777);
        stringDataStreamSource.print();
        env.execute();
    }
}
