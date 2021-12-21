package cn.mastercom.backstage.dataxsync.config;

import cn.mastercom.backstage.dataxsync.util.CommonUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.datasource")
public class DbClientConfig {

	private String url;

	private String username;

	private String password;

	private String driverClassName;

	@Bean(destroyMethod = "close")
	public HikariDataSource getDataSource() {
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driverClassName);
		config.setUsername(CommonUtil.desDecrypt(username));
		config.setPassword(CommonUtil.desDecrypt(password));
		config.setJdbcUrl(CommonUtil.desDecrypt(url));
		config.addDataSourceProperty("cachePrepStmts", true);
		config.addDataSourceProperty("prepStmtCacheSize", 500);
		config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
		config.setConnectionTestQuery("SELECT 1");
		config.setIdleTimeout(60000);
		config.setConnectionTimeout(30000);
		config.setMaxLifetime(60000);
		config.setValidationTimeout(3000);
		config.setAutoCommit(true);
		config.setLeakDetectionThreshold(0);
		// 池中最小空闲链接数量
		config.setMinimumIdle(10);
		// 池中最大链接数量
		config.setMaximumPoolSize(35);
		return new HikariDataSource(config);
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDriverClassName() {
		return driverClassName;
	}

	public void setDriverClassName(String driverClassName) {
		this.driverClassName = driverClassName;
	}
}