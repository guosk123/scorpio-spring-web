package com.machloop.fpc.cms.npm.analysis.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.npm.analysis.dao.SuricataAlertStatisticsDao;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 10:55 AM,cms
 * @version 1.0
 */
@Repository
public class SuricataAlertStatisticsDaoImpl implements SuricataAlertStatisticsDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SuricataAlertStatisticsDaoImpl.class);

  private static final String TABLE_ANALYSIS_SURICATA_ALERT_STATISTICS = "d_fpc_analysis_suricata_alert_statistics";

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Override
  public List<Map<String, Object>> queryAlertStatistics(String dsl, int topCount) {
    StringBuilder sql = new StringBuilder();
    sql.append("select key, sum(value) as count ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT_STATISTICS);

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(dsl, null, null, null, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by key ");
    sql.append(" order by count desc ");
    if (!params.values()
        .contains(FpcCmsConstants.SURICATA_ALERT_STATISTICS_TYPE_CLASSIFICATION_PROPORTION)
        && !params.values()
            .contains(FpcCmsConstants.SURICATA_ALERT_STATISTICS_TYPE_MITRE_TACTIC_PROPORTION)) {
      sql.append(" limit ").append(topCount);
    }

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  @Override
  public List<Map<String, Object>> queryAlertStatistics(String type, Date startTime, Date endTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select key, sum(value) as count ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT_STATISTICS);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(null, type, startTime, endTime, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by key ");
    sql.append(" order by count desc ");

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  @Override
  public List<Map<String, Object>> queryAlertDateHistogram(String dsl, int interval) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select toStartOfInterval(timestamp, INTERVAL :interval second) as temp_timestamp, ");
    sql.append(" sum(value) as count ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT_STATISTICS);

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", interval);
    whereSql(dsl, null, null, null, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by temp_timestamp ");
    sql.append(" order by temp_timestamp ");

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      item.put("timestamp", item.get("temp_timestamp"));
      item.remove("temp_timestamp");
    });

    return result;
  }


  private void whereSql(String dsl, String type, Date startTime, Date endTime,
      StringBuilder whereSql, Map<String, Object> params) {
    SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
    queryVO.setDsl(dsl);

    whereSql.append(" where 1=1 ");

    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> filterTuple = dslConverter.converte(queryVO.getDsl(), false,
                queryVO.getTimePrecision(), queryVO.getIncludeStartTime(),
                queryVO.getIncludeEndTime());
        whereSql.append(" and ");
        whereSql.append(filterTuple.getT1());
        params.putAll(filterTuple.getT2());
        return;
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(startTime, endTime, queryVO.getIncludeStartTime(),
        queryVO.getIncludeEndTime(), whereSql, params);

    if (StringUtils.isNotBlank(type)) {
      whereSql.append(" and type = :type ");
      params.put("type", type);
    }
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(String.format(" and timestamp %s toDateTime64(:start_time, 3, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and timestamp %s toDateTime64(:end_time, 3, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("queryFlowLogs has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryFlowLogs failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else if (errorCode == 60 || errorCode == 102) {
        // 60(UNKNOWN_TABLE，表不存在)：集群内没有探针节点，仅存在默认节点，默认节点会从本地查找数据表，此时就会返回表不存在的错误码
        LOGGER.info("not found sensor node.");
      } else if (errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        LOGGER.warn("connection sensor clikhousenode error.", e.getMessage());
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }
}
