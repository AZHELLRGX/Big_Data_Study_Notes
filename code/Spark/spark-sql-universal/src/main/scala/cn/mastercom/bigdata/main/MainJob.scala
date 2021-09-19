package cn.mastercom.bigdata.main

import cn.mastercom.bigdata.service.{DataPersistent, DataProcessing, ReadDataFromDB, ReadDataFromDFS}
import cn.mastercom.bigdata.util.jdbc.CityDBHandleUtil
import cn.mastercom.bigdata.util.properties.ConfigPropertiesParser
import cn.mastercom.bigdata.util.properties.ConfigPropertiesParser.DBConnectInfo
import cn.mastercom.bigdata.util.udf.{RegisterUDAF, RegisterUDF}
import cn.mastercom.bigdata.util.xml.SQLXMLParser
import cn.mastercom.bigdata.util.xml.SQLXMLParser.SqlXmlEntity
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.slf4j.{Logger, LoggerFactory}

import collection.JavaConverters._
import scala.collection.mutable

/**
 * 通用SparkSQL的启动项
 */
object MainJob {

  private val log: Logger = LoggerFactory.getLogger(MainJob.getClass)

  def main(args: Array[String]): Unit = {
    // 1、加载启动项配置文件
    val coreConfig = ConfigPropertiesParser.parsingCoreConfig(args)

    val dBProperties = ConfigPropertiesParser.parsingDBProperties(coreConfig)

    // 2、加载SQL配置文件
    val sqlXmlMap = SQLXMLParser.getSqlXmlMap(coreConfig)

    val conf = new SparkConf().setAppName(coreConfig.appName) // 后续需要配置一些通用参数
    if (coreConfig.debug) {
      conf.setMaster("local[*]")
    }
    val session = SparkSession.builder().config(conf).getOrCreate()

    // 3、注册UDF函数
    RegisterUDF.registerUDF(session)
    RegisterUDAF.registerUDAF()

    // 必须存在输出结果，才会进行读取数据和交互操作
    if (sqlXmlMap.contains("dataOutPut") && sqlXmlMap("dataOutPut").nonEmpty) {

      // Main库与地市库的映射关系
      val mainDbSettingMap = mutable.Map[String, mutable.Map[String, DBConnectInfo]]()

      // 判断是否有需要输出数据需要在地市库创建天表
      for (sqlXmlEntity <- sqlXmlMap("dataOutPut")) {
        createCityTables(sqlXmlEntity, dBProperties, mainDbSettingMap, coreConfig.cityIdFilter)
      }

      // 判断是否有需要输出数据需要在地市库创建天表
      for (sqlXmlEntity <- sqlXmlMap("sourceDataFromDB")) {
        createCityTables(sqlXmlEntity, dBProperties, mainDbSettingMap, coreConfig.cityIdFilter)
      }

      if (sqlXmlMap.contains("sourceDataFromDB")) {
        ReadDataFromDB.readDataFromDB(sqlXmlMap("sourceDataFromDB"), coreConfig, dBProperties, mainDbSettingMap, session)
      }
      // 4、根据原始数据相关配置文件从数据库和HDFS加载数据
      if (sqlXmlMap.contains("sourceDataFromDFS")) {
        ReadDataFromDFS.readDataFromDFS(sqlXmlMap("sourceDataFromDFS"), session, coreConfig)
      }
      // 5、串行SQL执行代码
      if (sqlXmlMap.contains("dataProcessing")) {
        DataProcessing.controlDataInteraction(sqlXmlMap("dataProcessing"), session)
      }
      // 6、数据输出
      DataPersistent.writeData(sqlXmlMap("dataOutPut"), dBProperties, mainDbSettingMap, session)

      for (elem <- mainDbSettingMap) {
        writeInputDBSuccessLog(coreConfig.dateStr, elem._2)
      }
    }

  }

  /**
   * 判断是否需要创建天表
   *
   * @param sqlXmlEntity 输出输出配置
   * @param dBProperties 数据库连接配置
   * @return
   */
  def createCityTables(sqlXmlEntity: SqlXmlEntity, dBProperties: mutable.Map[String, DBConnectInfo],
                       mainDbSettingMap: mutable.Map[String, mutable.Map[String, DBConnectInfo]], cityIdFilter: String): Unit = {
    if (sqlXmlEntity.cityDistribution) {
      val dBConnectInfo = dBProperties.getOrElse(sqlXmlEntity.dbId, null)
      if (dBConnectInfo == null) {
        val dbId = sqlXmlEntity.dbId
        log.warn("[{}]的数据库连接配置不存在，无法获取地市库连接信息", dbId)
      } else {
        val integerToInfo = mainDbSettingMap.getOrElseUpdate(sqlXmlEntity.dbId, CityDBHandleUtil.getDBSetting(dBConnectInfo, cityIdFilter).asScala)
        // 在地市库创建天表
        if (sqlXmlEntity.createSQL.nonEmpty) {
          // 如果是输出配置，则可能需要创建天表
          CityDBHandleUtil.createTable(sqlXmlEntity, integerToInfo.asJava)
        }
      }
    }
    // 不需要在地市库创建的数据也需要建表
    else {
      if (sqlXmlEntity.createSQL.nonEmpty) {
        CityDBHandleUtil.createTable(sqlXmlEntity, dBProperties.asJava)
      }
    }
  }

  // 如果是入库程序，需要在程序执行完成的时候输出入库完成日志
  def writeInputDBSuccessLog(dateStr: String, dbSettingMap: mutable.Map[String, DBConnectInfo]): Unit = {
    CityDBHandleUtil.writeInputDBSuccessLog(dateStr, dbSettingMap.asJava)
  }

}
