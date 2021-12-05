package org.azhell.learn.flink

object ConnectSQL {
  val sourceDataSql: String =
    """
      |CREATE TABLE Sensor (
      |  id STRING,
      |  ts BIGINT,
      |  temperature DOUBLE
      |) WITH (
      |  'connector' = 'filesystem',
      |  'path' = 'file:///E:/GitHub/Big_Data_Study_Notes/code/Flink/flink-java-learn/src/main/resources/sensor.txt',
      |  'format' = 'csv'
      |)
      |""".stripMargin

  val SelectFromSensor = "SELECT id,ts FROM Sensor"
}
