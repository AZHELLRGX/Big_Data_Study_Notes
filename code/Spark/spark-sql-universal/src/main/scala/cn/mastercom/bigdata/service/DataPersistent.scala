package cn.mastercom.bigdata.service

import cn.mastercom.bigdata.util.properties.ConfigPropertiesParser.DBConnectInfo
import cn.mastercom.bigdata.util.xml.SQLXMLParser.SqlXmlEntity
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object DataPersistent {
  private val log: Logger = LoggerFactory.getLogger(DataPersistent.getClass)

  def writeData(sqlXmlEntities: ListBuffer[SqlXmlEntity], dBProperties: mutable.Map[String, DBConnectInfo],
                mainDbSettingMap: mutable.Map[String, mutable.Map[String, DBConnectInfo]], session: SparkSession): Unit = {
    for (sqlXmlEntity <- sqlXmlEntities) {

      if (sqlXmlEntity.sparkMapName.nonEmpty || sqlXmlEntity.sql.nonEmpty) {
        // 指定了数据库连接信息，即认为数据需要写进数据库
        if (sqlXmlEntity.dbId.nonEmpty && sqlXmlEntity.dbTableName.nonEmpty) {
          if (sqlXmlEntity.cityDistribution) {
            // 需要将数据输出到地市库
            writeDataToCityDB(sqlXmlEntity, mainDbSettingMap(sqlXmlEntity.dbId), session)
          } else {
            // 只需要写出到单一数据库
            val connectInfo = dBProperties.getOrElse(sqlXmlEntity.dbId, null)
            if (connectInfo == null) {
              log.warn("[{}]对应的数据库连接不存在", sqlXmlEntity.dbId)
            }
            writeDataToDB(getDataFrame(sqlXmlEntity, session), sqlXmlEntity, connectInfo)
          }
        } else if (sqlXmlEntity.dataPath.nonEmpty) {
          writeDataToDFS(sqlXmlEntity, session)
        }
      }
    }
  }

  /**
   * 数据输出到地市库
   *
   * @param sqlXmlEntity 任务
   * @param dbSettingMap 地市库连接配置合集
   * @param session      SparkSQL上下文
   */
  private def writeDataToCityDB(sqlXmlEntity: SqlXmlEntity, dbSettingMap: mutable.Map[String, DBConnectInfo], session: SparkSession): Unit = {
    val df = getDataFrame(sqlXmlEntity, session)
    for (elem <- dbSettingMap) {
      val cityDF = df.filter(s"CityId=${elem._1.toInt}")
      writeDataToDB(cityDF, sqlXmlEntity, elem._2)
    }
  }


  /**
   * 输出输出到数据库指定的表
   *
   * @param df           需要写出的DataFrame
   * @param sqlXmlEntity 任务
   * @param connectInfo  数据库连接对象
   */
  private def writeDataToDB(df: DataFrame, sqlXmlEntity: SqlXmlEntity, connectInfo: DBConnectInfo): Unit = {
    val writer = df.write
      .format("jdbc")
      .option("url", connectInfo.url)
      .option("driver", connectInfo.driver)
      .option("user", connectInfo.username)
      .option("password", connectInfo.password)
      .option("dbtable", sqlXmlEntity.dbTableName)

    sqlXmlEntity.method match {
      case "append" => writer.mode(SaveMode.Append)
      case "overwrite" => writer.mode(SaveMode.Overwrite)
    }
    writer.save()
  }

  private def getDataFrame(sqlXmlEntity: SqlXmlEntity, session: SparkSession): DataFrame = {
    if (sqlXmlEntity.sql.nonEmpty) {
      session.sql(sqlXmlEntity.sql)
      // 虽然是输出结果的时候执行的SQL，但是这部分数据也有可能会缓存
      // cacheSqlResult(sqlResult, sqlXmlEntity)  // 暂时不支持数据结果配置中的数据缓存和表注册
    } else {
      session.table(sqlXmlEntity.sparkMapName)
    }
  }

  private def writeDataToDFS(sqlXmlEntity: SqlXmlEntity, session: SparkSession): Unit = {
    val df = getDataFrame(sqlXmlEntity, session)
    // todo 为什么从XML查询出来的分隔符变成了'\\t'暂时未查询出原因
    val delimiter = sqlXmlEntity.delimiter.replace("\\t", "\t")
    val resultRDD = df.rdd.map(row => {
      // 数据按照指定的分隔符重新组织拼接输出
      row.mkString(delimiter)
    })
    resultRDD.saveAsTextFile(sqlXmlEntity.dataPath)
  }
}
