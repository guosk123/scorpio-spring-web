package com.machloop.fpc.npm.metric.dao.impl;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.npm.metric.dao.MetricProtocolPortDao;
import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
@Repository
public class MetricProtocolPortDaoImpl implements MetricProtocolPortDao {

  private static final String TABLE_NETFLOW_PROTOCOL_PORT_STATISTICS = "t_netflow_protocol_port_statistics";

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricProtocolPortDaoImpl.class);

  private static final int TOP_SIZE = 10;

  @Autowired
  private ClickHouseStatsJdbcTemplate clickHouseTemplate;

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricProtocolPortDao#queryProtocolportDashboardHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryProtocolPortDashboardHistogram(MetricNetflowQueryVO queryVO,
      List<String> topData) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select protocol, port, protocol_port, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");

    sql.append(" SUM(total_bytes) AS total_bytes, SUM(total_packets) AS total_packets ");

    sql.append(" from ").append(TABLE_NETFLOW_PROTOCOL_PORT_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    whereSql.append(" and protocol_port in (:topData) ");
    params.put("topData", topData);
    sql.append(whereSql);
    sql.append(" group by timeStamp, protocol, port, protocol_port ");
    sql.append(" order by timeStamp ASC ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryProtocolPortHistogramData sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricProtocolPortDao#queryProtocolportTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryProtocolPortTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(
        " select protocol, port, protocol_port, sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_PROTOCOL_PORT_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);
    sql.append(" group by protocol, port, protocol_port ");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryMetricNetflowProtocolPort sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricProtocolPortDao#queryProtocolportTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryProtocolPortTableHistogram(MetricNetflowQueryVO queryVO,
      List<String> topData) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select protocol, port, protocol_port, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");

    sql.append(" SUM(total_bytes) AS total_bytes, SUM(total_packets) AS total_packets ");

    sql.append(" from ").append(TABLE_NETFLOW_PROTOCOL_PORT_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    whereSql.append(" and protocol_port in (:topData) ");
    params.put("topData", topData);
    sql.append(whereSql);
    sql.append(" group by timeStamp, protocol, port, protocol_port ");
    sql.append(" order by timeStamp ASC ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryProtocolPortHistogramData sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    whereSql.append(String.format(" and report_time %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and report_time %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private void whereSqlStatement(MetricNetflowQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where device_name = :device_name ");
    params.put("device_name", queryVO.getDeviceName());
    whereSql.append(" and netif_no = :netif_no ");
    params.put("netif_no", StringUtils.isBlank(queryVO.getNetifNo()) ? "" : queryVO.getNetifNo());

    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getStatisticsIncludeStartTime(), queryVO.getStatisticsIncludeEndTime(), whereSql,
        params);
  }
}
