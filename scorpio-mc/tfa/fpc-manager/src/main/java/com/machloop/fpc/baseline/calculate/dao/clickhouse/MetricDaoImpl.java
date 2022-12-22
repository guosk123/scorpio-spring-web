package com.machloop.fpc.baseline.calculate.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.baseline.calculate.dao.MetricDao;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;

import reactor.util.function.Tuple2;

/**
 * @author fengtianyou
 * 
 * create at 2021年9月13日, fpc-baseline
 */
@Repository
public class MetricDaoImpl implements MetricDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricDaoImpl.class);

  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.baseline.calculate.dao.MetricDao#queryMetrics(java.lang.String, java.lang.String, java.util.Map, java.util.List)
   */
  @Override
  public long[] queryMetrics(String tableName, String field, Map<String, Object> params,
      List<Tuple2<Date, Date>> timeRanges) throws IOException {
    StringBuilder sql = new StringBuilder();

    List<String> fields = getCombinationFields(field);
    sql.append(" select ").append(StringUtils.join(fields, ","));
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
    keyParams.keySet().forEach(param -> {
      sql.append(String.format(" and %s = :%s ", param, param));
    });

    sql.append(" ORDER BY timestamp ");
    sql.append(" LIMIT 10000 ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query metric sql: {}, params: {}", sql, params);
    }

    List<Map<String, Object>> batchresult = jdbcTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    long[] result = {};
    if (CollectionUtils.isNotEmpty(batchresult)) {
      result = new long[batchresult.size()];
      for (int i = 0; i < batchresult.size(); i++) {
        long value = MapUtils.getLongValue(batchresult.get(i), fields.get(0));
        if (fields.size() == 2) {
          long fieldValue2 = MapUtils.getLongValue(batchresult.get(i), fields.get(1));
          value = fieldValue2 == 0 ? 0 : value / fieldValue2;
        }
        result[i] = value;
      }
    }

    return result;
  }

  private List<String> getCombinationFields(String field) {
    List<String> fields = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    switch (field) {
      case "tcp_client_network_latency_avg":
        fields = Lists.newArrayList("tcp_client_network_latency",
            "tcp_client_network_latency_counts");
        break;
      case "tcp_server_network_latency_avg":
        fields = Lists.newArrayList("tcp_server_network_latency",
            "tcp_server_network_latency_counts");
        break;
      case "server_response_latency_avg":
        fields = Lists.newArrayList("server_response_latency", "server_response_latency_counts");
        break;
      case "tcp_client_retransmission_rate":
        fields = Lists.newArrayList("tcp_client_retransmission_packets", "tcp_client_packets");
        break;
      case "tcp_server_retransmission_rate":
        fields = Lists.newArrayList("tcp_server_retransmission_packets", "tcp_server_packets");
        break;
      default:
        fields.add(field);
        break;
    }

    return fields;
  }

}
