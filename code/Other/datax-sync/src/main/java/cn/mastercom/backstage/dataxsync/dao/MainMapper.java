package cn.mastercom.backstage.dataxsync.dao;

import cn.mastercom.backstage.dataxsync.pojo.DataSyncLog;
import cn.mastercom.backstage.dataxsync.pojo.DataSyncRecord;
import cn.mastercom.backstage.dataxsync.pojo.ServerConfigInfo;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 操作主服务器
 */
@Mapper
@Repository
public interface MainMapper {

	/**
	 * 读取数据同步记录 状态为4,6,7的数据不需要重新再同步，需要人工核查 20200617
	 * @return
	 */
	@Select("<script>" +
			"SELECT" +
			" GUID AS guid, " +
			" 数据时间 AS dataTime, " +
			" 源服务器ID AS sourceServerId, " +
			" 源库名 AS sourceDb, " +
			" 源表名 AS sourceTb, " +
			" 源数据SQL AS sqlStr, " +
			" 目标服务器ID AS targetServerId, " +
			" 目标库名 AS targetDb, " +
			" 目标表名 AS targetTb, " +
			" 目标DELETE条件 AS deleteCon, " +
			" 是否分表 AS cutTb, " +
			" 模版表名 AS modelTb, " +
			" 是否bcp方式 AS bcp, " +
			" 状态ID AS statusId,  " +
			" 任务分割ID AS taskId  " +
			"FROM " +
			" TB_数据同步_数据_记录 " +
			"WHERE " +
			" 状态ID in (1) and 任务分割ID in "
			+ "<foreach collection=\"ids\" item=\"id\" index=\"index\" open=\"(\" close=\")\" separator=\",\">"
			+ "'${id}'" + "</foreach>" + "</script>")
	List<DataSyncRecord> getDataSyncRecord(@Param("ids") List<String> taskIdList);

	/**
	 * 获得服务器配置
 	 */
	@Select("select * from TB_数据同步_配置_数据库")
	List<ServerConfigInfo> getServerConfigInfoList();

	@Select("select * from TB_数据同步_配置_数据库 WHERE id = #{id}")
	ServerConfigInfo getServerConfigInfoById(@Param("id") int id);

	/**
	 * 创建SQL_SERVER数据库的表
	 */
	@Select("EXEC MTNOH_AAA_DB.dbo.PROC_数据维护_分表_表_复制 'MTNOH_BASIC_CONF',#{parentTableName},#{formatDate},'天',1, 'MTNOH_BASIC_CONF',''")
	@Options(statementType = StatementType.CALLABLE)
	void createSqlServerDayTable(@Param("parentTableName") String parentTableName, @Param("formatDate") String formatDate);

	/**
	 * 创建MYSQL数据库的表
 	 */
	@Update("create table IF NOT EXISTS ${parentTableName}_DD_${formatDate} like ${parentTableName}")
	void createMysqlDayTable(@Param("parentTableName") String parentTableName, @Param("formatDate") String formatDate);

	@Update("UPDATE TB_数据同步_数据_记录 SET 状态ID = #{stat} WHERE GUID = #{guid}")
	void updateDataSyncRecord(@Param("stat") int stat, @Param("guid") String guid);

	@Insert("INSERT INTO TB_数据同步_日志_记录_DD_${dateFormat} VALUES(#{dataSyncLog.guid},#{dataSyncLog.beginTime},#{dataSyncLog.endTime},#{dataSyncLog.success},#{dataSyncLog.info},#{dataSyncLog.bcpInScript},#{dataSyncLog.bcpOutScript})")
	void insertDataSyncLog(@Param("dataSyncLog") DataSyncLog dataSyncLog, @Param("dateFormat") String format);
}
