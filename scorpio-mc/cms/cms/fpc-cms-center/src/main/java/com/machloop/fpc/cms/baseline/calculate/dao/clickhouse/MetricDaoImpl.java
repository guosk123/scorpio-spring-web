package com.machloop.fpc.cms.baseline.calculate.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.baseline.calculate.dao.MetricDao;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseBPJdbcTemplate;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * 
 * @author guosk
 *
 * create at 2021年9月16日, fpc-cms-center
 */
@Repository
public class MetricDaoImpl implements MetricDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricDaoImpl.class);

  private static final String DIVIDE_NULL_NAN = "NaN";
  private static final String DIVIDE_NULL_INF = "Infinity";

  @Autowired
  private ClickHouseBPJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.baseline.calculate.dao.MetricDao#queryMetrics(java.lang.String, java.lang.String, java.util.Map, java.util.List)
   */
  @Override
  public long[] queryMetrics(String tableName, String field, Map<String, Object> params,
      List<Tuple2<Date, Date>> timeRanges) throws IOException {
    StringBuilder sql = new StringBuilder();
    sql.append(" select ");
    Iterator<Entry<String, Tuple2<AggsFunctionEnum, String>>> iterator = getCombinationFields(field)
        .entrySet().iterator();
    while (iterator.hasNext()) {
      Entry<String, Tuple2<AggsFunctionEnum, String>> next = iterator.next();
      sql.append(next.getValue().getT1().getOperation()).append("(").append(next.getValue().getT2())
          .append(") AS ").append(next.getKey());

      if (iterator.hasNext()) {
        sql.append(", ");
      }
    }

    sql.append(" from ").append(tableName);
    sql.append(" where 1 = 1 ");

    // 过滤时间
    Map<String, Object> keyParams = Maps.newHashMap(params);
    List<String> timeFilterSql = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    int tag = 0;
    for (Tuple2<Date, Date> timeRange : timeRanges) {
      if (timeRange != null) {
        timeFilterSql.add(String.format(
            "(timestamp > toDateTime64(:start_time%s, 3, 'UTC') and timestamp <= toDateTime64(:end_time%s, 3, 'UTC'))",
            tag, tag));
        params.put("start_time" + tag,
            DateUtils.toStringFormat(timeRange.getT1(), "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
        params.put("end_time" + tag,
            DateUtils.toStringFormat(timeRange.getT2(), "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

        tag++;
      }
    }
    sql.append(" and (").append(StringUtils.join(timeFilterSql, " or ")).append(") ");

    // 过滤key值
    keyParams.forEach((paramName, paramValue) -> {
      if (paramValue instanceof List) {
        sql.append(String.format(" and %s in (:%s) ", paramName, paramName));
      } else {
        sql.append(String.format(" and %s = :%s ", paramName, paramName));
      }
    });

    sql.append(" GROUP BY timestamp ");
    sql.append(" ORDER BY timestamp ");
    sql.append(" LIMIT 10000 ");
    sql.append(" settings max_execution_time = 6 ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query metric sql: {}, params: {}", sql, params);
    }

    List<Map<String, Object>> batchresult = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    long[] result = {};
    field = TextUtils.underLineToCamel(field);
    if (CollectionUtils.isNotEmpty(batchresult)) {
      result = new long[batchresult.size()];
      for (int i = 0; i < batchresult.size(); i++) {
        Object object = batchresult.get(i).get(field);
        result[i] = StringUtils.equalsAny(String.valueOf(object), DIVIDE_NULL_NAN, DIVIDE_NULL_INF)
            ? 0
            : object instanceof Double ? ((Double) object).longValue() : (long) object;
      }
    }

    return result;
  }

  private Map<String, Tuple2<AggsFunctionEnum, String>> getCombinationFields(String field) {
    Map<String, Tuple2<AggsFunctionEnum, String>> aggFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    switch (field) {
      case "tcp_client_network_latency_avg":
        aggFields.put("tcpClientNetworkLatency",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
        aggFields.put("tcpClientNetworkLatencyCounts",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts"));
        aggFields.put("tcpClientNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
            "tcpClientNetworkLatency, tcpClientNetworkLatencyCounts"));
        break;
      case "tcp_server_network_latency_avg":
        aggFields.put("tcpServerNetworkLatency",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
        aggFields.put("tcpServerNetworkLatencyCounts",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts"));
        aggFields.put("tcpServerNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
            "tcpServerNetworkLatency, tcpServerNetworkLatencyCounts"));
        break;
      case "server_response_latency_avg":
        aggFields.put("serverResponseLatency",
            Tuples.of(AggsFunctionEnum.SUM, "server_response_latency"));
        aggFields.put("serverResponseLatencyCounts",
            Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_counts"));
        aggFields.put("serverResponseLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
            "serverResponseLatency, serverResponseLatencyCounts"));
        break;
      case "tcp_client_retransmission_rate":
        aggFields.put("tcpClientRetransmissionPackets",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
        aggFields.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
        aggFields.put("tcpClientRetransmissionRate",
            Tuples.of(AggsFunctionEnum.DIVIDE, "tcpClientRetransmissionPackets, tcpClientPackets"));
        break;
      case "tcp_server_retransmission_rate":
        aggFields.put("tcpServerRetransmissionPackets",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
        aggFields.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
        aggFields.put("tcpServerRetransmissionRate",
            Tuples.of(AggsFunctionEnum.DIVIDE, "tcpServerRetransmissionPackets, tcpServerPackets"));
        break;
      default:
        aggFields.put(TextUtils.underLineToCamel(field), Tuples.of(AggsFunctionEnum.SUM, field));
        break;
    }

    return aggFields;
  }

  private <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = jdbcTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("caculate baseline has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("caculate baseline failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);
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
        LOGGER.warn("found other error.", e);
      }
    }

    return result;
  }

}
