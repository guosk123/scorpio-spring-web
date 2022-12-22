package com.machloop.fpc.manager.global.dao.clickhouse;

import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.clickhouse.jdbc.ClickHouseDataSource;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.fpc.manager.global.dao.SlowQueryDao;
import com.zaxxer.hikari.HikariDataSource;

/**
 * @author mazhiyuan
 *
 * create at 2020年10月20日, fpc-manager
 */
@Repository
public class SlowQueryDaoImpl implements SlowQueryDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(SlowQueryDaoImpl.class);

  @Value("${clickhouse.url}")
  private String url;
  @Value("${clickhouse.username}")
  private String username;
  @Value("${clickhouse.password}")
  private String password;
  @Value("${clickhouse.driver-class-name}")
  private String driverClassName;

  private static HikariDataSource dataSource;
  private static NamedParameterJdbcTemplate jdbcTemplate;

  @PostConstruct
  private void initJdbcTemplate() throws SQLException {

    dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
    dataSource.setPoolName("slow_query_manager");
    dataSource.setDataSource(new ClickHouseDataSource(url));
    dataSource.setJdbcUrl(url);
    dataSource.setUsername(username);
    dataSource.setPassword(password);
    dataSource.setDriverClassName(driverClassName);
    dataSource.setMaximumPoolSize(1);

    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  @PreDestroy
  private void destroyDataSource() {
    if (dataSource != null && !dataSource.isClosed()) {
      dataSource.close();
    }
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.SlowQueryDao#cancelQueries(java.util.List)
   */
  @Override
  public void cancelQueries(List<String> queryIds) {

    if (CollectionUtils.isEmpty(queryIds)) {
      throw new IllegalArgumentException("queryIds must not be empty");
    }

    StringBuilder killSql = new StringBuilder();
    killSql.append("KILL QUERY WHERE query_id IN ");
    killSql.append("(SELECT query_id FROM system.processes ");
    killSql.append(" WHERE 1=2 ");
    for (String queryId : queryIds) {
      killSql.append(" or query LIKE '/*" + Base64Utils.encode(queryId) + "*/%' ");
    }
    killSql.append(" ) ASYNC");
    jdbcTemplate.getJdbcOperations().execute(killSql.toString());
    LOGGER.debug("finish to cancel queries, queryIds: [{}], result: [{}]",
        StringUtils.join(queryIds, ", "));
  }
}
