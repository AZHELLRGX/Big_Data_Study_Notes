package org.azhell.learn.flink.sql

object ConnectSQL {
  val fileDataSql: String =
    """
      |CREATE TABLE Sensor (
      |  id STRING,
      |  ts BIGINT,
      |  temperature DOUBLE
      |) WITH (
      |  'connector' = 'filesystem',
      |  'path' = 'file:///D:/work-space/github/Big_Data_Study_Notes/code/Flink/flink-java-learn/src/main/resources/sensor.txt',
      |  'format' = 'csv'
      |)
      |""".stripMargin

  val kafkaDataSql: String =
    """
      |CREATE TABLE KafkaTestSink (
      |  `msg` STRING
      |) WITH (
      |  'connector' = 'kafka',
      |  'topic' = 'test',
      |  'properties.bootstrap.servers' = '192.168.2.102:9092',
      |  'format' = 'csv'
      |)
      |""".stripMargin
}
