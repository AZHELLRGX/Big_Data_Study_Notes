package cn.mastercom.bigdata.util.udf

import org.apache.spark.sql.SparkSession
import cn.mastercom.bigdata.util.gis.GeoUtils
import cn.mastercom.mtcommon.gis.GisFactory
import cn.mastercom.mtcommon.gis.DRect

/**
 * 管理UDF函数
 */
object RegisterUDF {

  def registerUDF(session: SparkSession): Unit = {
    session.udf.register("PointExtendFunction", pointExtendFunction _)
    session.udf.register("PolyGridListFunction", polyGridListFunction _)
    session.udf.register("PointDistanceFunction", pointDistanceFunction _)
  }

  /**
   * 判断经纬度点是否在区域轮廓内
   *
   * @param poly      16进制区域轮廓
   * @param longitude 经度
   * @param latitude  纬度
   * @param radius    半径
   * @return
   */
  def pointExtendFunction(poly: String, longitude: Double, latitude: Double, radius: Double): Boolean = {
    if (longitude > 180 || longitude < -180 || latitude > 90 || latitude < -90)
      return false
    val bytes = GeoUtils.hexToBytes(poly)
    if (bytes != null) {
      val lngExtend = radius * 0.00001
      val latExtend = radius * 0.000009
      val polygon = GisFactory.newPloygon(bytes)
      val dRect = new DRect(longitude - lngExtend, latitude - latExtend, longitude + lngExtend, latitude + latExtend)
      polygon.intersects(dRect)
    } else {
      false
    }
  }

  /**
   * 区域栅格化
   *
   * @param poly     16进制区域轮廓
   * @param gridSize 预定义栅格规格
   * @return
   */
  def polyGridListFunction(poly: String, gridSize: Integer): String = {
    if (gridSize <= 0) return ""
    val bytes = GeoUtils.hexToBytes(poly)
    if (bytes != null) {
      val polygon = GisFactory.newPloygon(bytes)
      GeoUtils.getGridListByPoly(polygon, gridSize)
    } else {
      ""
    }
  }

  /**
   * 获取两个浮点类型经纬度点的距离
   */
  def pointDistanceFunction(lon1: Double, lat1: Double, lon2: Double, lat2: Double): Double = {
    GeoUtils.getDistance(lon1, lat1, lon2, lat2)
  }
}
