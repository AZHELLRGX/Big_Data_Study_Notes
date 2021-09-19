package cn.mastercom.bigdata.service

import cn.mastercom.bigdata.util.properties.ConfigPropertiesParser.CoreConfig
import cn.mastercom.bigdata.util.time.DateUtil
import cn.mastercom.bigdata.util.xml.SQLXMLParser.{FieldMap, SqlXmlEntity}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.types.{DataType, DataTypes, StructField, StructType}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

/**
 * 从文件系统中读取数据
 * Spark3.0版本后，Row对象没有自动序列化，需要手动序列化
 */
object ReadDataFromDFS extends Serializable {
  private val log: Logger = LoggerFactory.getLogger(ReadDataFromDFS.getClass)

  def readDataFromDFS(sqlXmlEntities: ListBuffer[SqlXmlEntity], session: SparkSession, coreConfig: CoreConfig): Unit = {
    for (sqlXmlEntity <- sqlXmlEntities) {

      // 加载数据
      val rdd = if (sqlXmlEntity.multiDays > 0 || pathIsValid(session, sqlXmlEntity.dataPath)) {
        // 需要注意多天数据的判断
        readData(sqlXmlEntity, session, coreConfig)
      } else {
        session.sparkContext.emptyRDD[Row]
      }

      // 获取类型
      val schema = getStructType(sqlXmlEntity.fieldMap)

      // 创建DataFrame
      val df = session.createDataFrame(rdd, schema)
      df.createOrReplaceTempView(sqlXmlEntity.sparkMapName)

      // 如果sql不为空，则执行数据
      if (sqlXmlEntity.sql.nonEmpty) {
        val sqlResult = session.sql(sqlXmlEntity.sql)
        log.info("{}对应的sql语句执行成功", sqlXmlEntity.sparkMapName)
        // 注册表
        sqlResult.createOrReplaceTempView(sqlXmlEntity.registerTableName)
      }
      // 检查sparkMapName和registerTableName是否需要缓存
      cacheTable(sqlXmlEntity, session)

      // log.info("{}数据读取成功，经过预处理，注册为表{}", sqlXmlEntity.dataPath, sqlXmlEntity.registerTableName)
    }
  }

  private def readData(sqlXmlEntity: SqlXmlEntity, session: SparkSession, coreConfig: CoreConfig): RDD[Row] = {
    val dataPathList = new ListBuffer[String]()
    if (sqlXmlEntity.multiDays > 0) {
      // 读取HDFS多天目录数据
      for (i <- 0 to sqlXmlEntity.multiDays) {
        val dateStr = DateUtil.getBeforeDay(coreConfig.dateStr, -i)
        val dataPath = sqlXmlEntity.dataPath.replace(MULTI_DATE, dateStr)
        // 多天数据也需要判空
        if (pathIsValid(session, dataPath))
          dataPathList.append(dataPath)
      }
    } else {
      dataPathList.append(sqlXmlEntity.dataPath)
    }
    if (dataPathList.nonEmpty) {
      // 可以一次性读取多天数据
      session.sparkContext.textFile(dataPathList.mkString(",")).map(
        line => {
          val data = line.split(sqlXmlEntity.delimiter)
          val array = ListBuffer[Any]()
          for (field <- sqlXmlEntity.fieldMap.fields) {
            // 数据需要根据类型提前转化
            array.append(typeConverter(data(field.index), field.fieldType))
          }
          Row.fromSeq(array)
        }
      )
    } else {
      // 多天数据均不存在也返回空RDD
      session.sparkContext.emptyRDD[Row]
    }
  }

  /**
   * 根据配置文件的fieldMap生成表的schema
   *
   * @param fieldMap 字段类型映射配置
   * @return
   */
  private def getStructType(fieldMap: FieldMap): StructType = {
    val fields = ListBuffer[StructField]()
    for (field <- fieldMap.fields) {
      fields.append(StructField(field.fieldName, typeConverter(field.fieldType), nullable = true))
    }
    StructType(fields)
  }

  /**
   * String数据转换为scala数据类型
   *
   * @param fieldValue 字符串
   * @param fieldType  需要转换的目标类型
   * @return
   */
  private def typeConverter(fieldValue: String, fieldType: String): Any = {
    fieldType match {
      // todo 测试阶段先使用这几种类型转换
      case "int" => fieldValue.toInt
      case "bigint" => fieldValue.toLong
      case "long" => fieldValue.toLong
      case "double" => fieldValue.toDouble
      case "float" => fieldValue.toFloat
      case _ => fieldValue
    }
  }

  /**
   * 类型转换为Spark的DataType
   *
   * @param fieldType 需要转换的目标类型
   * @return
   */
  private def typeConverter(fieldType: String): DataType = {
    fieldType match {
      // todo 测试阶段先使用这几种类型转换
      case "int" => DataTypes.IntegerType
      case "bigint" => DataTypes.LongType
      case "long" => DataTypes.LongType
      case "double" => DataTypes.DoubleType
      case "float" => DataTypes.FloatType
      case _ => DataTypes.StringType
    }
  }
}
