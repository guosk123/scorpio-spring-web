package com.machloop.fpc.manager.boot.configuration;

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

  private static final int DEFAULT_POOL_SIZE = 10;

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

  @Value("${clickhouse.stats.url}")
  private String statsUrl;
  @Value("${clickhouse.stats.driver-class-name}")
  private String statsDriverClassName;
  @Value("${clickhouse.stats.connection.timeout.ms}")
  private int statsConnectionTimeoutMs;
  @Value("${clickhouse.stats.connection.MaximumPoolSize}")
  private int statsMaximumPoolSize;
  @Value("${clickhouse.stats.username}")
  private String statsUsername;
  @Value("${clickhouse.stats.password}")
  private String statsPassword;

  @Bean
  public ClickHouseJdbcTemplate metadataJdbcTemplate() throws SQLException {

    HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
    dataSource.setPoolName("ch_metadata_pool");
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
  public ClickHouseStatsJdbcTemplate statsJdbcTemplate() throws SQLException {

    HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
    dataSource.setPoolName("ch_stats_pool");
    dataSource.setDataSource(new ClickHouseDataSource(statsUrl));
    dataSource.setJdbcUrl(statsUrl);
    dataSource.setUsername(statsUsername);
    dataSource.setPassword(statsPassword);
    dataSource.setDriverClassName(statsDriverClassName);
    dataSource.setConnectionTimeout(statsConnectionTimeoutMs);
    dataSource
        .setMaximumPoolSize(statsMaximumPoolSize == 0 ? DEFAULT_POOL_SIZE : statsMaximumPoolSize);
    dataSource.setMetricsTrackerFactory(new HikariCPMetricsTrackerFactory());

    return new ClickHouseStatsJdbcTemplate(dataSource);
  }

}
