package org.azhell.learn.flink.wc;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * flink流处理的wordCount
 */
public class StreamWordCount {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // flink提供了解析输入参数的tool方法
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        String host = parameterTool.get("host");
        int port = parameterTool.getInt("port");
        // --host localhost --port 7777

        DataStreamSource<String> socketTextStream = env.socketTextStream(host, port);
        socketTextStream.flatMap((FlatMapFunction<String, Tuple2<String, Integer>>) (rowLine, out) -> {
            for (String word : rowLine.split(" ")) {
                out.collect(new Tuple2<>(word, 1));
            }
        })
                // 这里的returns是显式指定lambda表达式的返回结果
                .returns(TupleTypeInfo.getBasicTupleTypeInfo(String.class, Integer.class))
                // 流处理里面是keyBy 而不是 groupBy
                .keyBy(0).sum(1).print();

        // 保持启动，不要停止
        env.execute();

        // 输出结果分析
        /*
        2> (hello,3)
        4> (flink,1)
        本地线程编号（集群节点分区编号）
         */
    }
}
