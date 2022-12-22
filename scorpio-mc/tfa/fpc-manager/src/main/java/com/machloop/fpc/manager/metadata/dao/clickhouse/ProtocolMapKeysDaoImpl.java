package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.metadata.dao.ProtocolMapKeysDao;

@Repository
public class ProtocolMapKeysDaoImpl implements ProtocolMapKeysDao {


  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  private static final String TABLE_NAME = "t_fpc_protocol_dict";

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolMapKeysDao.class);

  @Override
  public Map<String, Set<String>> queryProtocolMapKeys(String protocol) {
    StringBuilder sql = new StringBuilder();
    sql.append("select field,key ");
    sql.append(" from ").append(TABLE_NAME);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where protocol = :protocol ");
    params.put("protocol", protocol);
    sql.append(whereSql);
    sql.append(" group by field,key ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} log record by id, sql: {}, params: {}", TABLE_NAME, sql, params);
    }
    List<Map<String, Object>> mapKeysList = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    Map<String, Set<String>> result = new HashMap<>();
    for (Map<String, Object> temp : mapKeysList) {
      String field = (String) temp.get("field");
      if (!result.containsKey(field)) {
        Set<String> set = new HashSet<>();
        if (temp.get("key") != null) {
          set.add((String) temp.get("key"));
        }
        result.put(field, set);
      } else {
        Set<String> keySet = result.get(field);
        if (temp.get("key") != null) {
          keySet.add((String) temp.get("key"));
        }
      }

    }
    return result;
  }

  private List<Map<String, Object>> queryWithExceptionHandle(String sql,
      Map<String, Object> paramMap, ColumnMapRowMapper rowMapper) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    try {
      result = jdbcTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query metadata log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata log failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;

  }
}
