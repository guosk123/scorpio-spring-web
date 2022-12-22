package com.machloop.fpc.manager.boot.configuration;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ClickHouseStatsJdbcTemplate {

  private NamedParameterJdbcTemplate jdbcTemplate;

  public ClickHouseStatsJdbcTemplate(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  public NamedParameterJdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }
}

