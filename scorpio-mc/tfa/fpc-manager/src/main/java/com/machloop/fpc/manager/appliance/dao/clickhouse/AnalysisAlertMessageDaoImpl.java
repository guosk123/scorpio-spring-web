package com.machloop.fpc.manager.appliance.dao.clickhouse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.appliance.dao.AnalysisAlertMessageDao;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年9月17日, fpc-manager
 */
@Repository
public class AnalysisAlertMessageDaoImpl implements AnalysisAlertMessageDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalysisAlertMessageDaoImpl.class);

  private static final int SCALE_COUNTS = 4;

  private static final String DIVIDE_NULL_NAN = "NaN";
  private static final String DIVIDE_NULL_INF = "Infinity";

  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AnalysisAlertMessageDao#analysisAlertMessage(java.util.Date, java.util.Date, int, java.util.Map, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> analysisAlertMessage(Date startTime, Date endTime, int interval,
      Map<String, Object> params, String tableName, List<String> metrics) {
    // 获取字段聚合方式
    Map<String, Tuple2<AggsFunctionEnum, String>> aggFields = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    metrics.forEach(metric -> {
      setAggFields(aggFields, metric);
    });

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') as temp_timestamp ");
    // 聚合字段
    aggFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });

    sql.append(" from ").append(tableName);

    // 过滤时间
    Map<String, Object> keyParams = Maps.newHashMap(params);
    sql.append(" where 1 = 1 ");
    sql.append(" and timestamp >= toDateTime64(:start_time, 3, 'UTC') ");
    sql.append(" and timestamp <= toDateTime64(:end_time, 3, 'UTC') ");
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("interval", interval);

    // 过滤key值
    keyParams.keySet().forEach(param -> {
      sql.append(String.format(" and %s = :%s ", param, param));
    });

    sql.append(" group by temp_timestamp ");
    sql.append(" order by temp_timestamp ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query metric sql: {}, params: {}", sql.toString(), params);
    }

    List<Map<String, Object>> result = jdbcTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());

    result = result.stream().map(item -> {
      item.put("timestamp", MapUtils.getString(item, "temp_timestamp"));
      item.remove("temp_timestamp");
      return item;
    }).collect(Collectors.toList());

    return metricDataConversion(result);
  }

  private void setAggFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggFields, String field) {
    switch (field) {
      case "total_bytes":
        aggFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
        break;
      case "total_packets":
        aggFields.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
        break;
      case "established_sessions":
        aggFields.put("establishedSessions",
            Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
        break;
      case "established_tcp_sessions":
        aggFields.put("establishedTcpSessions",
            Tuples.of(AggsFunctionEnum.SUM, "established_tcp_sessions"));
        break;
      case "concurrent_tcp_sessions":
        aggFields.put("concurrentTcpSessions",
            Tuples.of(AggsFunctionEnum.MAX, "concurrent_tcp_sessions"));
        break;
      case "tcp_syn_packets":
        aggFields.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
        break;
      case "tcp_syn_ack_packets":
        aggFields.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
        break;
      case "tcp_syn_rst_packets":
        aggFields.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));
        break;
      case "tcp_zero_window_packets":
        aggFields.put("tcpZeroWindowPackets",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_zero_window_packets"));
        break;
      case "tcp_established_success_counts":
        aggFields.put("tcpEstablishedSuccessCounts",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_established_success_counts"));
        break;
      case "tcp_established_fail_counts":
        aggFields.put("tcpEstablishedFailCounts",
            Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_counts"));
        break;
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
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "unsupport field.");
    }
  }

  private List<Map<String, Object>> metricDataConversion(List<Map<String, Object>> metricResult) {
    metricResult.forEach(metricData -> {
      if (metricData.containsKey("tcpClientNetworkLatencyAvg")) {
        metricData.put("tcpClientNetworkLatencyAvg",
            StringUtils.equalsAny(String.valueOf(metricData.get("tcpClientNetworkLatencyAvg")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("tcpClientNetworkLatencyAvg")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
      if (metricData.containsKey("tcpServerNetworkLatencyAvg")) {
        metricData.put("tcpServerNetworkLatencyAvg",
            StringUtils.equalsAny(String.valueOf(metricData.get("tcpServerNetworkLatencyAvg")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("tcpServerNetworkLatencyAvg")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
      if (metricData.containsKey("serverResponseLatencyAvg")) {
        metricData.put("serverResponseLatencyAvg",
            StringUtils.equalsAny(String.valueOf(metricData.get("serverResponseLatencyAvg")),
                DIVIDE_NULL_NAN, DIVIDE_NULL_INF) ? 0
                    : new BigDecimal(String.valueOf(metricData.get("serverResponseLatencyAvg")))
                        .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }
    });

    return metricResult;
  }

}
