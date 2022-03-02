package org.azhell.learn.flink.wc;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.operators.AggregateOperator;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TupleTypeInfo;

/**
 * 批处理
 */
public class WordCount {
    public static void main(String[] args) throws Exception {
        // 创建执行环境
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        // 从文件中读取数据
        String inputPath = "src/main/resources/data/hello.txt";
        DataSource<String> inputDs = env.readTextFile(inputPath);
        // 对数据集进行处理
        // 在Java使用lambda表达式需要注意声明类型
        //
        // FlatMapFunction需要声明输入数据类型、输出数据类型
        AggregateOperator<Tuple2<String, Integer>> resultDs =
                inputDs.flatMap((FlatMapFunction<String, Tuple2<String, Integer>>) (rowLine, out) -> {
                    for (String word : rowLine.split(" ")) {
                        out.collect(new Tuple2<>(word, 1));
                    }
                })
                        // 这里的returns是显式指定lambda表达式的返回结果
                        .returns(TupleTypeInfo.getBasicTupleTypeInfo(String.class, Integer.class))
                        .groupBy(0).sum(1); // 这里传入的0和1表示的是元组中字段的位置
        // 从以上写法来看还是scala写大数据代码简单易懂，Java代码感觉很重，不够接近人类的自然语言
        resultDs.print();
        
        // Java的代码写起来很复杂，但是业内使用还是较多，所以基于Java学习也是必要的
    }
}
