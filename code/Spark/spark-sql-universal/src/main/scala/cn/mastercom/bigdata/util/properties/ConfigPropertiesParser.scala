package cn.mastercom.bigdata.util.properties

import org.slf4j.{Logger, LoggerFactory}

import java.io.FileInputStream
import java.util.{Date, Properties}
import scala.collection.mutable
import org.apache.log4j.PropertyConfigurator
import cn.mastercom.bigdata.util._
import cn.mastercom.mtcommon.crypto.Des

import java.text.SimpleDateFormat

/**
 * 解析properties配置
 */
object ConfigPropertiesParser {
  private val log: Logger = LoggerFactory.getLogger(ConfigPropertiesParser.getClass)

  /**
   * 解析核心配置文件
   *
   * @return
   */
  def parsingCoreConfig(args: Array[String]): CoreConfig = {
    // 脚本获取的相对目录
    var userDir = args(0)
    val appName = args(1) // appName从脚本传入
    userDir = userDir.substring(0, userDir.lastIndexOf("/"))

    PropertyConfigurator.configure(loadProperties(s"$userDir/conf/$appName/properties/log4j.properties"))
    log.info("userDir is {}", userDir)
    val coreProperties = loadProperties(s"$userDir/conf/$appName/properties/core.properties")
    val debug = coreProperties.getProperty("debug")

    // 输入参数：日期(通常应该按照6位数来)
    if (args.length == 4) {
      val dateStr = args(2) // 只有在创建天表与数据输入输出的时候解析通配符有效
      val provider = args(3) // 厂商(yd、lt、dt)也是一个很重要的通用配置
      CoreConfig(debug.toBoolean, appName, dateStr, provider, userDir)
    }
    else if (args.length == 3) {
      val dateStr = args(2)
      CoreConfig(debug.toBoolean, appName, dateStr, YD, userDir)
    }
    else {
      val format = new SimpleDateFormat("yyMMdd")
      val dateStr = format.format(new Date())
      CoreConfig(debug.toBoolean, appName, dateStr, YD, userDir)
    }
  }

  /**
   * 解析数据库连接配置
   *
   * @return
   */
  def parsingDBProperties(coreConfig: CoreConfig): mutable.Map[String, DBConnectInfo] = {
    val dbProperties = loadProperties(s"${coreConfig.userDir}/conf/${coreConfig.appName}/properties/database.properties")
    val keySet = dbProperties.stringPropertyNames()
    val dbInfoMap = mutable.Map[String, DBConnectInfo]()
    val iterator = keySet.iterator()
    while (iterator.hasNext) {
      val key = iterator.next()
      // 安全考虑，数据库连接信息需要加密处理；代码里面使用DES解密
      val data: Array[String] = Des.decryptForMt(dbProperties.getProperty(key)).split("-")
      if (data.length < 4)
        log.warn("{}对应的数据库连接配置缺乏必要参数", key)
      else {
        val driver = data(3).toLowerCase match {
          case MYSQL => MYSQL_DRIVER
          case MSSQL => MSSQL_DRIVER
          case _ => data(3)
        }
        dbInfoMap.put(key, DBConnectInfo(data(0), data(1), data(2), driver))
      }
    }
    log.info("dbInfoMap is {}", dbInfoMap)
    dbInfoMap
  }

  /**
   * 配置文件映射为Properties对象
   *
   * @param fileName 配置文件名称
   * @return
   */
  def loadProperties(fileName: String): Properties = {
    val properties = new Properties()
    log.info("load properties , path is {}", fileName)
    properties.load(new FileInputStream(fileName))
    properties
  }

  // 核心配置文件
  case class CoreConfig(debug: Boolean, appName: String, dateStr: String, provider: String, userDir: String)

  // 数据库配置映射
  case class DBConnectInfo(url: String, username: String, password: String, driver: String)
}


