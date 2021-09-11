package cn.mastercom.bigdata.util.jdbc;


import cn.mastercom.bigdata.util.properties.ConfigPropertiesParser.DBConnectInfo;
import cn.mastercom.bigdata.util.xml.SQLXMLParser.SqlXmlEntity;
import cn.mastercom.mtcommon.crypto.Des;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * 地市库操作
 * 1、地市库建表
 */
public class CityDBHandleUtil {
    private static final Logger log = LoggerFactory.getLogger(CityDBHandleUtil.class);

    private static final String MSSQL_JDBC_URL = "jdbc:sqlserver://${ServeIP};DataBaseName=${DBName}";
    private static final String GET_DB_SETTING = "SELECT [CityID],[DBName],[LogonName],[Password],[ServeIP] FROM [MBD2_CITY_MAIN].[dbo].[tb_cfg_dbsetting] WHERE [DBType] = 'CITY'";


    private CityDBHandleUtil() {
    }

    public static void createTable(SqlXmlEntity sqlXmlEntity, Map<Integer, DBConnectInfo> dbSettingMap) {
        // 是否需要在地市库创建天表
        if (sqlXmlEntity.cityDistribution()) {
            String createTableSQL = sqlXmlEntity.createSQL();
            for (Map.Entry<Integer, DBConnectInfo> entry : dbSettingMap.entrySet()) {
                if (createTable(createTableSQL, entry.getValue())) {
                    String dbTableName = sqlXmlEntity.dbTableName();
                    log.info("[{}]库创建天表[{}]成功", entry.getKey(), dbTableName);
                }
            }
        }
    }

    /**
     * 通过Main库的dbSetting配置，连接地市库创建天表
     *
     * @param dbConnectInfo 数据库连接信息
     */
    public static Map<Integer, DBConnectInfo> getDBSetting(DBConnectInfo dbConnectInfo) {
        Map<Integer, DBConnectInfo> dbSettingMap = new HashMap<>();
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            //1.加载驱动程序【其实可以默认都是SqlServer数据库】
            Class.forName(dbConnectInfo.driver());
            //2.获得数据库的连接
            conn = DriverManager.getConnection(dbConnectInfo.url(), dbConnectInfo.username(), dbConnectInfo.password());
            conn.setAutoCommit(true); // 这个连接可能被fetchSize查询使用过
            statement = conn.createStatement();
            resultSet = statement.executeQuery(GET_DB_SETTING);
            while (resultSet.next()) {
                int cityId = resultSet.getInt("CityID"); // 废弃库数据依然需要保留,因为数据写出需要废弃库配置
                String logonName = desDecrypt(resultSet.getString("LogonName"));
                String password = desDecrypt(resultSet.getString("Password"));
                String serveIP = desDecrypt(resultSet.getString("ServeIP"));
                String dbName = desDecrypt(resultSet.getString("DBName"));
                // 组装连接串
                String url = MSSQL_JDBC_URL.replace("${ServeIP}", serveIP).replace("${DBName}", dbName);
                dbSettingMap.put(cityId, new DBConnectInfo(url, logonName, password, dbConnectInfo.driver()));

            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("查询Main库dbSetting错误", e);
        } finally {
            close(resultSet, statement, conn);
        }
        return dbSettingMap;
    }

    /**
     * 依次关闭JDBC对象
     */
    private static void close(ResultSet resultSet, Statement statement, Connection conn) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                log.error("resultSet关闭错误", e);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                log.error("statement关闭错误", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("关系连接失败", e);
            }
        }
    }

    /**
     * 尝试是否可以DES解密
     *
     * @param str 加密串
     * @return 解密后的串
     */
    private static String desDecrypt(String str) {
        try {
            return Des.decryptForMt(str);
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * @param sql           需要执行的SQL
     * @param dbConnectInfo 数据库连接信息
     */
    private static boolean createTable(String sql, DBConnectInfo dbConnectInfo) {
        Connection conn = null;
        Statement statement = null;
        try {
            //1.加载驱动程序【其实可以默认都是SqlServer数据库】
            Class.forName(dbConnectInfo.driver());
            //2.获得数据库的连接
            conn = DriverManager.getConnection(dbConnectInfo.url(), dbConnectInfo.username(), dbConnectInfo.password());
            conn.setAutoCommit(true); // 这个连接可能被fetchSize查询使用过
            statement = conn.createStatement();
            statement.execute(sql);
            return true;
        } catch (Exception e) {
            log.error("创建表存在异常", e);
            return false;
        } finally {
            close(null, statement, conn);
        }
    }
}
