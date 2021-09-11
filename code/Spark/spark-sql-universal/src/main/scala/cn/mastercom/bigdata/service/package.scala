package cn.mastercom.bigdata


import cn.mastercom.bigdata.util.xml.SQLXMLParser.SqlXmlEntity
import org.apache.spark.sql.SparkSession

package object service {

  /**
   * 缓存指定的表数据
   *
   * @param sqlXmlEntity SQL标签实体
   * @param session      SparkSession
   */
  def cacheTable(sqlXmlEntity: SqlXmlEntity, session: SparkSession): Unit = {
    if (sqlXmlEntity.sparkMapName.nonEmpty && sqlXmlEntity.cacheSet.contains(sqlXmlEntity.sparkMapName)) {
      // cacheTable默认是lazy加载, 缓存级别是MEMORY_AND_DISK
      // todo 可以通过session.catalog.cacheTable(sqlXmlEntity.sparkMapName,storageLevel = StorageLevel.MEMORY_AND_DISK)来指定缓存类型
      session.sqlContext.cacheTable(sqlXmlEntity.sparkMapName)
    } else if (sqlXmlEntity.registerTableName.nonEmpty && sqlXmlEntity.cacheSet.contains(sqlXmlEntity.registerTableName)) {
      session.sqlContext.cacheTable(sqlXmlEntity.registerTableName)
    }
  }
}
