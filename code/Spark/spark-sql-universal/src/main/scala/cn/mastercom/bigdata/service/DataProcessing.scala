package cn.mastercom.bigdata.service

import cn.mastercom.bigdata.util.xml.SQLXMLParser.SqlXmlEntity
import org.apache.spark.sql.SparkSession

import scala.collection.mutable.ListBuffer

/**
 * 数据交互
 */
object DataProcessing {
  def controlDataInteraction(sqlXmlEntities: ListBuffer[SqlXmlEntity], session: SparkSession): Unit = {
    for (sqlXmlEntity <- sqlXmlEntities) {
      session.sql(sqlXmlEntity.sql).createOrReplaceTempView(sqlXmlEntity.registerTableName)
      // 检查sparkMapName和registerTableName是否需要缓存
      cacheTable(sqlXmlEntity, session)
    }
  }
}
