/**
 * 此包用来演示Flink的流Join
 * 另外展示相关Api的使用方式
 */
package org.azhell.learn.flink.jointest;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

class PkgConst{
    private PkgConst(){}
    static StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
}