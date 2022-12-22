package com.machloop.fpc.cms.center.global.dao.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseBPJdbcTemplate;
import com.machloop.fpc.cms.center.global.dao.SlowQueryDao;

/**
 * @author mazhiyuan
 *
 * create at 2020年10月20日, fpc-manager
 */
@Repository
public class SlowQueryDaoImpl implements SlowQueryDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(SlowQueryDaoImpl.class);

  @Autowired
  private ClickHouseBPJdbcTemplate clickHouseTemplate;

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.SlowQueryDao#cancelQueries(java.util.List)
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
    clickHouseTemplate.getJdbcTemplate().getJdbcOperations().execute(killSql.toString());
    LOGGER.debug("finish to cancel queries, queryIds: [{}], result: [{}]",
        StringUtils.join(queryIds, ", "));
  }
}
