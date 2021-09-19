package cn.mastercom.bigdata


import cn.mastercom.bigdata.util.xml.SQLXMLParser.SqlXmlEntity
import org.apache.spark.sql.SparkSession

package object service {

  val MULTI_DATE = "${multiDate}";

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

  /**
   * 检测路径是否存在且不为空
   */
  def pathIsValid(spark: SparkSession, path: String): Boolean = {
    pathIsExist(spark, path) && get_path_size(spark, path) > 0
  }


  /**
   * 判断目录是否存在,注意：只能在driver端使用，可以多线程来提速。
   */
  def pathIsExist(spark: SparkSession, path: String): Boolean = {
    //取文件系统
    val filePath = new org.apache.hadoop.fs.Path(path)
    val fileSystem = filePath.getFileSystem(spark.sparkContext.hadoopConfiguration)

    // 判断路径是否存在
    fileSystem.exists(filePath)
  }

  /**
   * 获取某个目录的大小(单位b字节),注意：只能在driver端使用，可以多线程来提速。
   */
  def get_path_size(spark: SparkSession, path: String): Long = {
    //取文件系统
    val filePath = new org.apache.hadoop.fs.Path(path)
    val fileSystem = filePath.getFileSystem(spark.sparkContext.hadoopConfiguration)

    // 获取该目录的大小，单位是字节
    if (fileSystem.exists(filePath)) {
      fileSystem.getContentSummary(filePath).getLength
    } else {
      0
    }
  }
}
