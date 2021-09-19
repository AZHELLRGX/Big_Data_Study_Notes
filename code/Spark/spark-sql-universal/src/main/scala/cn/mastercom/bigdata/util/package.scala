package cn.mastercom.bigdata


package object util {
  val MYSQL = "mysql"
  val MSSQL = "mssql"
  val MYSQL_DRIVER = "com.mysql.jdbc.Driver"
  val MSSQL_DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
  val SQL = "sql"
  // 数据库连接对应的id
  val DB_ID = "dbId"
  // 数据库表名
  val DB_TABLE_NAME = "dbTableName"
  val DATA_PATH = "dataPath"
  val DELIMITER = "delimiter"
  val FIELD_MAP = "fieldMap"
  val CACHE = "cache"
  val TABLE = "table"
  val OPTION = "option"
  val FIELD = "field"
  val NAME = "name"
  // 数据库表在spark的映射名称
  val SPARK_MAP_NAME = "sparkMapName"
  // 执行完sql以后注册表名
  val REGISTER_TABLE_NAME = "registerTableName"
  val METHOD = "method"
  val INDEX = "index"
  val TYPE = "type"
  val CREATE = "create"
  val YD = "yd"
  val MODEL = "model"
  val MODEL_NAME = "modelName"
  val MULTI_DAYS = "multiDays"
  val PROVIDER_WC = "${provider}"
  val DATE_WC = "${date}"
  val CITY_DISTRIBUTION = "cityDistribution"
  val CITY_ID_FILTER = "cityIdFilter"
  // 四个XML配置文件
  val XML_FILE_NAME_ARRAY: Array[String] = Array[String]("sourceDataFromDB", "sourceDataFromDFS", "dataProcessing", "dataOutPut")

}
