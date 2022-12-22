package com.machloop.fpc.npm.metric.dao.impl;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.machloop.fpc.npm.metric.dao.MetricIpDao;
import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
@Repository
public class MetricIpDaoImpl implements MetricIpDao {

  private static final int TOP_SIZE = 1000;

  private static final String TABLE_NETFLOW_IP_STATISTICS = "t_netflow_ip_statistics";

  private static final String TABLE_NETFLOW_SOURCE = "t_netflow_source_statistics";

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricIpDaoImpl.class);

  @Autowired
  private ClickHouseStatsJdbcTemplate clickHouseTemplate;

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryIpDashboardHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryIpDashboardHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select ipv4_address, ipv6_address, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");

    sql.append(" SUM(total_bytes) AS total_bytes, SUM(total_packets) AS total_packets ");

    sql.append(" from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4_address");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6_address");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4_address=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6_address=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());
    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");
    sql.append(" ) group by timeStamp, ipv4_address, ipv6_address ");
    sql.append(" order by timeStamp ASC ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryHistogramData sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryIpTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryIpTable(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection) {
    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(
        " select ipv4_address, ipv6_address, sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append("from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);
    sql.append(" group by ipv4_address, ipv6_address ");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryIpTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select ipv4_address, ipv6_address, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");

    sql.append(" SUM(total_bytes) AS total_bytes, SUM(total_packets) AS total_packets ");

    sql.append(" from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4_address");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6_address");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4_address=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6_address=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());
    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");
    sql.append(" group by timeStamp, ipv4_address, ipv6_address ");
    sql.append(" order by timeStamp ASC");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryHistogramData sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryTransmitIpDashboardHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryTransmitIpDashboardHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select ipv4_address, ipv6_address, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");

    sql.append(
        " SUM(transmit_bytes) AS transmit_bytes, SUM(transmit_packets) AS transmit_packets ");

    sql.append(" from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4_address");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6_address");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4_address=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6_address=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());
    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");
    sql.append(" group by timeStamp, ipv4_address, ipv6_address ");
    sql.append(" order by timeStamp ASC ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryHistogramData sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryTransmitIpTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryTransmitIpTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(
        " select ipv4_address, ipv6_address, sum(transmit_bytes) as transmit_bytes, sum(transmit_packets) as transmit_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);
    sql.append(" group by ipv4_address, ipv6_address ");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryTransmitIpTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryTransmitIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select ipv4_address, ipv6_address, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");

    sql.append(
        " SUM(transmit_bytes) AS transmit_bytes, SUM(transmit_packets) AS transmit_packets ");

    sql.append(" from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4_address");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6_address");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4_address=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6_address=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());
    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");
    sql.append(" group by timeStamp, ipv4_address, ipv6_address ");
    sql.append(" order by timeStamp ASC");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryHistogramData sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryIngestIpDashboardHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryIngestIpDashboardHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select ipv4_address, ipv6_address, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");

    sql.append(" SUM(ingest_bytes) AS ingest_bytes, SUM(ingest_packets) AS ingest_packets ");

    sql.append(" from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4_address");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6_address");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4_address=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6_address=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());
    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");
    sql.append(" group by timeStamp, ipv4_address, ipv6_address ");
    sql.append(" order by timeStamp ASC");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryHistogramData sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryIngestIpTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryIngestIpTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(
        " select ipv4_address, ipv6_address, sum(ingest_bytes) as ingest_bytes, sum(ingest_packets) as ingest_packets ");
    sql.append("from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);
    sql.append(" group by ipv4_address, ipv6_address ");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryIngestIpTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryIngestIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select ipv4_address, ipv6_address, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");

    sql.append(" SUM(ingest_bytes) AS ingest_bytes, SUM(ingest_packets) AS ingest_packets ");

    sql.append(" from ").append(TABLE_NETFLOW_IP_STATISTICS);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4_address");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6_address");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4_address=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6_address=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());
    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");
    sql.append(" group by timeStamp, ipv4_address, ipv6_address ");
    sql.append(" order by timeStamp ASC");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryHistogramData sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#queryMetricNetflowIndex(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricNetflowIndex(MetricNetflowQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(" select SUM(total_bytes) AS total_bytes, SUM(total_packets) AS total_packets ");
    if (StringUtils.isNotBlank(queryVO.getNetifNo())) {
      sql.append(", SUM(transmit_bytes) AS transmit_bytes, SUM(ingest_bytes) AS ingest_bytes ");
    }
    sql.append(" from ").append(TABLE_NETFLOW_SOURCE);
    whereSqlStatement(queryVO, whereSql, params);

    sql.append(whereSql);
    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricIpDao#transmitIngestBytesDataAggregate(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO)
   */
  @Override
  public List<Map<String, Object>> querytransmitIngestBytesDataAggregate(
      MetricNetflowQueryVO queryVO) {
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");
    params.put("interval", queryVO.getInterval());

    sql.append(" sum(transmit_bytes) as transmit_bytes, sum(ingest_bytes) as ingest_bytes ");

    sql.append(" from ").append(TABLE_NETFLOW_SOURCE);
    whereSqlStatement(queryVO, whereSql, params);
    sql.append(whereSql);
    sql.append(" group by timeStamp ");
    sql.append(" order by timeStamp ASC");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("transmitIngestHistogramDataAggregate sql : [{}], param: [{}] ", sql.toString(),
          params);
    }
    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());
    return result;
  }

  private void whereSqlStatement(MetricNetflowQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where device_name = :device_name");
    params.put("device_name", queryVO.getDeviceName());
    whereSql.append(" and netif_no = :netif_no ");
    params.put("netif_no", StringUtils.isBlank(queryVO.getNetifNo()) ? "" : queryVO.getNetifNo());

    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getStatisticsIncludeStartTime(), queryVO.getStatisticsIncludeEndTime(), whereSql,
        params);
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

}
