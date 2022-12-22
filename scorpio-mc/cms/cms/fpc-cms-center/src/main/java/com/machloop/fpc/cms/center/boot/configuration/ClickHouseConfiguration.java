package com.machloop.fpc.cms.center.boot.configuration;

import java.sql.SQLException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.clickhouse.jdbc.ClickHouseDataSource;
import com.machloop.alpha.common.indicator.hikari.HikariCPMetricsTrackerFactory;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class ClickHouseConfiguration {

  private static final int DEFAULT_POOL_SIZE = 20;

  @Value("${clickhouse.url}")
  private String url;
  @Value("${clickhouse.driver-class-name}")
  private String driverClassName;
  @Value("${clickhouse.connection.timeout.ms}")
  private int connectionTimeoutMs;
  @Value("${clickhouse.connection.MaximumPoolSize}")
  private int maximumPoolSize;
  @Value("${clickhouse.username}")
  private String username;
  @Value("${clickhouse.password}")
  private String password;

  @Bean
  public ClickHouseJdbcTemplate clickhouseJdbcTemplate() throws SQLException {

    HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
    dataSource.setPoolName("ch_search_pool");
    dataSource.setDataSource(new ClickHouseDataSource(url));
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setDriverClassName(driverClassName);
    dataSource.setConnectionTimeout(connectionTimeoutMs);
    dataSource.setMaximumPoolSize(maximumPoolSize == 0 ? DEFAULT_POOL_SIZE : maximumPoolSize);
    dataSource.setMetricsTrackerFactory(new HikariCPMetricsTrackerFactory());

    return new ClickHouseJdbcTemplate(dataSource);
  }

  @Bean
  public ClickHouseBPJdbcTemplate clickhouseBPJdbcTemplate() throws SQLException {

    HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
    dataSource.setPoolName("ch_background_process_pool");
    dataSource.setDataSource(new ClickHouseDataSource(url));
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setDriverClassName(driverClassName);
    dataSource.setMetricsTrackerFactory(new HikariCPMetricsTrackerFactory());

    return new ClickHouseBPJdbcTemplate(dataSource);
  }

}
