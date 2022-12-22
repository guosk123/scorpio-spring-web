package com.machloop.fpc.manager.boot.configuration;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ClickHouseJdbcTemplate {

  private NamedParameterJdbcTemplate jdbcTemplate;

  public ClickHouseJdbcTemplate(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  public NamedParameterJdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }
}
