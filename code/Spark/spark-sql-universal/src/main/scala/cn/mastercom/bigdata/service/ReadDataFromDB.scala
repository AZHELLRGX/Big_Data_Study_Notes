package cn.mastercom.bigdata.service

import cn.mastercom.bigdata.util.properties.ConfigPropertiesParser.{CoreConfig, DBConnectInfo}
import cn.mastercom.bigdata.util.time.DateUtil
import cn.mastercom.bigdata.util.xml.SQLXMLParser.SqlXmlEntity
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

object ReadDataFromDB {
  private val log: Logger = LoggerFactory.getLogger(ReadDataFromDB.getClass)

  def readDataFromDB(sqlXmlEntities: ListBuffer[SqlXmlEntity], coreConfig: CoreConfig, dBProperties: mutable.Map[String, DBConnectInfo]
                     , mainDbSettingMap: mutable.Map[String, mutable.Map[String, DBConnectInfo]], session: SparkSession): Unit = {
    for (sqlXmlEntity <- sqlXmlEntities) {
      if (sqlXmlEntity.dbId.isEmpty || sqlXmlEntity.dbTableName.isEmpty) {
        log.warn("属性dbId或者dbTableName为空, 判定为非法配置, 其他信息为{}", sqlXmlEntity)
      } else if (sqlXmlEntity.sql.nonEmpty && sqlXmlEntity.registerTableName.isEmpty) {
        log.warn("SQL语句: {}, 未指定对应的registerTableName", sqlXmlEntity.sql)
      } else if (!dBProperties.contains(sqlXmlEntity.dbId)) {
        log.warn("dbId属性:{}在database.properties配置文件中未配置", sqlXmlEntity.dbId)
      } else {
        // 前面已经判断了是否包含
        val info = dBProperties(sqlXmlEntity.dbId)

        // 判断是否需要读取多个地市库的数据，还需关注是否需要读取多天数据
        if (sqlXmlEntity.cityDistribution) {
          readCityData(sqlXmlEntity, mainDbSettingMap, coreConfig, session)
        } else {
          session.read.format("jdbc")
            .option("url", info.url)
            .option("user", info.username)
            .option("password", info.password)
            .option("driver", info.driver)
            .option("dbtable", sqlXmlEntity.dbTableName)
            .load().createOrReplaceTempView(sqlXmlEntity.sparkMapName)
        }
        if (sqlXmlEntity.sql.nonEmpty) {
          session.sql(sqlXmlEntity.sql).createOrReplaceTempView(sqlXmlEntity.registerTableName)
        }
      }
      // 检查sparkMapName和registerTableName是否需要缓存
      cacheTable(sqlXmlEntity, session)
    }
  }

  private def readCityData(sqlXmlEntity: SqlXmlEntity, mainDbSettingMap: mutable.Map[String, mutable.Map[String, DBConnectInfo]],
                           coreConfig: CoreConfig, session: SparkSession): Unit = {
    // 存放表与地市库连接信息的元组列表
    val tupleList = new ListBuffer[(String, DBConnectInfo)]()

    val cityConnectionInfo = mainDbSettingMap.getOrElse(sqlXmlEntity.dbId, null)
    if (cityConnectionInfo == null) {
      log.warn("[{}]对应的地市库配置不存在", sqlXmlEntity.dbId)
      return
    }
    if (sqlXmlEntity.multiDays > 0) {
      for (i <- 0 to sqlXmlEntity.multiDays) {
        val dateStr = DateUtil.getBeforeDay(coreConfig.dateStr, -i)
        val realTableName = sqlXmlEntity.dbTableName.replace(MULTI_DATE, dateStr)
        for (elem <- cityConnectionInfo) {
          // cityId大于0的才是地市库
          if (elem._1.toInt > 0) {
            tupleList.append((realTableName, elem._2))
          }
        }
      }
    }

    var dfAll: DataFrame = null
    for (tuple <- tupleList) {
      val df = session.read.format("jdbc")
        .option("url", tuple._2.url)
        .option("user", tuple._2.username)
        .option("password", tuple._2.password)
        .option("driver", tuple._2.driver)
        .option("dbtable", tuple._1)
        .load()
      dfAll = if (dfAll == null) {
        df
      } else {
        dfAll.union(df)
      }
    }
    dfAll.createOrReplaceTempView(sqlXmlEntity.sparkMapName)
  }
}
