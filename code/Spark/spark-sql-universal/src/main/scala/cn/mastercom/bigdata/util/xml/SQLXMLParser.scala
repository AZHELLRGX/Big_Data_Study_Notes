package cn.mastercom.bigdata.util.xml

import org.slf4j.{Logger, LoggerFactory}

import java.io.{FileInputStream, FileNotFoundException, IOException, InputStreamReader}
import scala.xml.{Elem, XML}
import cn.mastercom.bigdata.util._
import cn.mastercom.bigdata.util.properties.ConfigPropertiesParser.CoreConfig

import scala.collection.mutable.ListBuffer
import scala.collection.mutable

/**
 * 解析SQL所在XML文件的工具类
 * 配置文件应该分为四类配置
 * 1、数据库加载原始数据
 * 2、HDFS加载原始数据
 * 3、计算交互
 * 4、写出数据
 */
object SQLXMLParser {
  private val log: Logger = LoggerFactory.getLogger(SQLXMLParser.getClass)

  /**
   * 解析四种类型的XML配置文件并返回
   *
   * @param coreConfig 核心配置
   * @return
   */
  def getSqlXmlMap(coreConfig: CoreConfig): mutable.Map[String, ListBuffer[SqlXmlEntity]] = {
    val sqlXmlMap = mutable.Map[String, ListBuffer[SqlXmlEntity]]()
    for (xmlFileName <- XML_FILE_NAME_ARRAY) {
      sqlXmlMap.put(xmlFileName, parsingSqlXml(coreConfig, xmlFileName))
    }
    sqlXmlMap
  }

  /**
   * 解析XML配置文件，返回解析结果List
   *
   * @param coreConfig 核心配置
   * @param fileName   XML文件名称
   * @return
   */
  private def parsingSqlXml(coreConfig: CoreConfig, fileName: String): ListBuffer[SqlXmlEntity] = {
    val sqlXmlEntityList = ListBuffer[SqlXmlEntity]()
    val isr = new InputStreamReader(new FileInputStream(s"${coreConfig.userDir}/conf/${coreConfig.appName}/xml/$fileName.xml"))
    try {
      val xml = XML.load(isr)
      val fieldMap = getFieldMapFromTags(xml)
      val cacheSet = getCacheSetFromTags(xml)
      val modelMap = getCreateMapFromTags(xml, coreConfig)

      // sql标签是所有xml配置中均存在的
      val sqlTags = xml \ SQL
      if (sqlTags.nonEmpty) {
        for (sqlTag <- sqlTags) {
          var dbTableName = sqlTag.attribute(DB_TABLE_NAME).getOrElse("").toString

          dbTableName = replaceWildcard(dbTableName, coreConfig)

          val fieldName = sqlTag.attribute(FIELD_MAP).getOrElse("").toString
          val modelName = sqlTag.attribute(MODEL_NAME).getOrElse("").toString
          val createSQL = modelMap.getOrElse(modelName, "")
          val sqlXmlEntity = SqlXmlEntity(sqlTag.attribute(DB_ID).getOrElse("").toString,
            dbTableName,
            replaceWildcard(sqlTag.attribute(DATA_PATH).getOrElse("").toString, coreConfig),
            // 数据的默认分隔符是\t，在从DFS读取或者写出数据的时候生效
            sqlTag.attribute(DELIMITER).getOrElse("\t").toString,
            FieldMap(fieldName, fieldMap.getOrElse(fieldName, null)),
            // 如果没有指定映射表名，则使用默认数据库表名
            sqlTag.attribute(SPARK_MAP_NAME).getOrElse(dbTableName).toString,
            sqlTag.attribute(REGISTER_TABLE_NAME).getOrElse("").toString,
            cacheSet,
            sqlTag.attribute(METHOD).getOrElse("").toString,
            // 目前只有在输出配置中才存在模版表配置
            if (createSQL.nonEmpty) createSQL.replace(modelName, dbTableName) else createSQL, // 还需要将建表语句中的模版表名称替换为实际天表名称
            // 是否需要地市分发，默认是false
            sqlTag.attribute(CITY_DISTRIBUTION).getOrElse("false").toString.toBoolean,
            sqlTag.attribute(MULTI_DAYS).getOrElse("0").toString.toInt,
            sqlTag.text.trim
          )
          sqlXmlEntityList.append(sqlXmlEntity)
        }
      }
    }
    catch {
      case ex: FileNotFoundException => log.error("sourceDataFromDB.xml not found", ex)
      case ex: IOException => log.error("sourceDataFromDB.xml read error", ex)
    }
    finally {
      isr.close()
    }
    sqlXmlEntityList
  }

  /**
   * 从XML中解析数据模版表配置
   * 主要配置的是模版表名称和建表语句SQL的对应关系
   *
   * @param xml        XML映射实体
   * @param coreConfig 核心配置
   * @return
   */
  private def getCreateMapFromTags(xml: Elem, coreConfig: CoreConfig): mutable.Map[String, String] = {
    val modelMap = mutable.Map[String, String]()
    // 模版表创建相关
    val createTags = xml \ CREATE
    for (createTag <- createTags) {
      val tableTags = createTag \ TABLE
      for (tableTag <- tableTags) {
        // table标签内容是模版表建表语句
        val createSql = replaceWildcard(tableTag.text, coreConfig)
        val model = tableTag.attribute(MODEL).getOrElse("").toString
        if (model.nonEmpty)
          modelMap.put(tableTag.attribute(MODEL).getOrElse("").toString, createSql)
        else
          log.warn("{}对应的模版表配置为空", createSql)
      }
    }
    modelMap
  }

  /**
   * 从XML中解析缓存配置
   *
   * @param xml XML映射实体
   * @return
   */
  private def getCacheSetFromTags(xml: Elem): mutable.Set[String] = {
    // cache缓存相关
    val cacheSet = mutable.Set[String]()
    val cacheTags = xml \ CACHE
    for (cacheTag <- cacheTags) {
      val tableTags = cacheTag \ TABLE
      for (tableTag <- tableTags) {
        cacheSet.add(tableTag.text)
      }
    }
    cacheSet
  }

  /**
   * 从XML中解析HDFS文件映射配置
   *
   * @param xml XML映射实体
   * @return
   */
  private def getFieldMapFromTags(xml: Elem): mutable.Map[String, ListBuffer[Field]] = {
    val fieldMap = mutable.Map[String, ListBuffer[Field]]()
    val fieldMapTags = xml \ FIELD_MAP
    if (fieldMapTags.nonEmpty) {
      for (fieldMapTag <- fieldMapTags) {
        val fieldMapName = fieldMapTag.attribute(NAME).getOrElse("").toString
        val fieldList = ListBuffer[Field]()
        val columnConfigsString = fieldMapTag.text
        val columnConfigs = columnConfigsString.split("\n")
        columnConfigs.foreach(columnConfig => {
          val columnTrim = columnConfig.trim
          if (columnTrim.nonEmpty) {
            val columns = columnTrim.split("\t")
            // 这里需要直接报错,否则HDFS字段配置错误,会直接影响读取数据,进而影响后续计算
            val field = Field(columns(0).toInt, columns(2), columns(1))
            fieldList.append(field)
          }
        })
        fieldMap.put(fieldMapName, fieldList)
      }
    }
    fieldMap
  }

  /**
   * 替换字符串中的通配符
   *
   * @param str        需要替换通配符的字符串
   * @param coreConfig 核心配置
   * @return
   */
  private def replaceWildcard(str: String, coreConfig: CoreConfig): String = {
    str.replace(PROVIDER_WC, coreConfig.provider).replace(DATE_WC, coreConfig.dateStr)
  }

  /**
   * hdfs字段映射配置
   *
   * @param index     字段在文件中的序列
   * @param fieldType 字段映射到Spark时候的数据类型
   * @param fieldName 字段名称
   */
  case class Field(index: Int, fieldType: String, fieldName: String)

  /**
   * FieldMap
   *
   * @param name   FieldMap名称
   * @param fields 字段映射合集
   */
  case class FieldMap(name: String, fields: ListBuffer[Field])

  /**
   * sql标签实体
   *
   * @param dbId              数据库连接ID
   * @param dbTableName       数据库表名
   * @param dataPath          HDFS数据路径
   * @param sparkMapName      Spark映射表名
   * @param registerTableName 执行SQL完成后注册表名
   * @param sql               SQL语句
   */
  case class SqlXmlEntity( // 数据库相关配置
                           dbId: String, dbTableName: String,
                           // HDFS相关配置
                           dataPath: String, delimiter: String, fieldMap: FieldMap,
                           // 公共属性
                           sparkMapName: String,
                           registerTableName: String,
                           // 缓存配置
                           cacheSet: mutable.Set[String],
                           // 写出的时候指定数据的写出方式
                           method: String,
                           // 数据输出的时候，是否需要在driver端预先创建天表
                           createSQL: String,
                           // 出入库的时候，是否需要地市分发
                           cityDistribution: Boolean,
                           // 扫描指定天数内部的天表
                           multiDays: Int,
                           sql: String
                         )
}
