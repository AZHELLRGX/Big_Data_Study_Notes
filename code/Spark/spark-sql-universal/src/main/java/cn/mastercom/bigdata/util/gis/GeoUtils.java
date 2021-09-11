package cn.mastercom.bigdata.util.gis;

import cn.mastercom.mtcommon.gis.DRect;
import cn.mastercom.mtcommon.gis.IPolygon;

import java.math.BigDecimal;


/**
 * @author yeyingkai
 */
public class GeoUtils {

    private GeoUtils() {
        // do nothing
    }

    private static int gridSizeGlobal = 10;
    public static final double DRATIO = 1.0E7D;
    public static final int LOT_PER_METER = 100;
    public static final int LAT_PER_METER = 90;
    private static final double EARTH_RADIUS = 6371.393;

    private static double rad(double d) {
        return d * Math.PI / 180.0;
    }

    public static double getDistance(double lng1, double lat1, double lng2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lng1) - rad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) +
                Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 1000);
        return s;
    }

    /**
     * 十六进制字符串转byte数组
     */
    public static byte[] hexToBytes(String hex) {
        if (hex.length() < 1) {
            return new byte[]{};
        } else {
            byte[] result = new byte[hex.length() / 2];
            int j = 0;
            for (int i = 0; i < hex.length(); i += 2) {
                result[j++] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            }
            return result;
        }
    }

    public static String getGridListByPoly(IPolygon polygon, Integer gridSize) {
        StringBuilder sb = new StringBuilder();
        gridSizeGlobal = gridSize;
        //获取图元轮廓外边框
        DRect r = polygon.getBounds();
        int x1 = getRatioLot(r.getX1());
        int x2 = getRatioLot(r.getX2());
        int y1 = getRatioLat(r.getY1());
        int y2 = getRatioLat(r.getY2());

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                double lot1 = getLot(x);
                double lat1 = getLat(y);
                double lot2 = getLot(x + 1);
                double lat2 = getLat(y + 1);
                //判断栅格是否与图元真实相交
                if (polygon.intersects(new DRect(lot1, lat1, lot2, lat2))) {
                    sb.append(String.format("%s_%s;",
                            lot1, lat1));
                }
            }
        }
        return sb.toString();
    }

    private static int getRatioLot(double value) {
        return (int) (value / (gridSizeGlobal * LOT_PER_METER / DRATIO));
    }

    private static int getRatioLat(double value) {
        return (int) (value / (gridSizeGlobal * LAT_PER_METER / DRATIO));
    }

    /**
     * 保证double精度计算
     */
    private static double getLot(int ratio) {
        BigDecimal decRatio = new BigDecimal(Integer.toString(ratio));
        BigDecimal decGrid = new BigDecimal(Double.toString(gridSizeGlobal * LOT_PER_METER / DRATIO));
        return decRatio.multiply(decGrid).doubleValue();
    }

    /**
     * 保证double精度计算
     */
    private static double getLat(int ratio) {
        BigDecimal decRatio = new BigDecimal(Integer.toString(ratio));
        BigDecimal decGrid = new BigDecimal(Double.toString(gridSizeGlobal * LAT_PER_METER / DRATIO));
        return decRatio.multiply(decGrid).doubleValue();
    }

}
