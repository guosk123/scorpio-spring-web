package com.machloop.fpc.cms.center.boot.configuration;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class ClickHouseBPJdbcTemplate {

  private NamedParameterJdbcTemplate jdbcTemplate;

  public ClickHouseBPJdbcTemplate(DataSource dataSource) {
    jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
  }

  public NamedParameterJdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }
}
