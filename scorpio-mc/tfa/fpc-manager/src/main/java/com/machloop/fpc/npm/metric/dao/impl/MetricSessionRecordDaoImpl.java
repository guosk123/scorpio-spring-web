package com.machloop.fpc.npm.metric.dao.impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao;
import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
@Repository
public class MetricSessionRecordDaoImpl implements MetricSessionRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricSessionRecordDaoImpl.class);

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  private static final int TOP_SIZE = 1000;

  private static final int SESSION_RECORD_SIZE = 10000;

  private static final String TABLE_NETFLOW_SESSION_RECORD = "t_netflow_session_record";

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#queryIpTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryIpTable(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    sql.append(
        " select ipv4, ipv6, sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ");
    sql.append(" (select src_ipv4 as ipv4, src_ipv6 as ipv6, ");
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);
    sql.append(" group by ipv4, ipv6 ");
    sql.append(" union all ");
    sql.append(" select dest_ipv4 as ipv4, dest_ipv6 as ipv6, ");
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql);
    enrichIpWhereSql(queryVO, params, sql);
    sql.append(" group by ipv4, ipv6) ");
    sql.append(" group by ipv4, ipv6 ");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);
    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#queryIpTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());

    sql.append(
        " select ipv4, ipv6, timeStamp, sum(total_bytes) as total_bytes , sum(total_packets) as total_packets ");
    sql.append(" from ");
    sql.append(" (select src_ipv4 as ipv4, src_ipv6 as ipv6, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second ) as timeStamp, ");
    params.put("interval", queryVO.getInterval());
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);

    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");

    sql.append(" group by ipv4, ipv6, timeStamp ");
    sql.append(" union all ");
    sql.append(" select dest_ipv4 as ipv4, dest_ipv6 as ipv6, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second ) as timeStamp, ");
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);

    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");

    sql.append(" group by ipv4, ipv6, timeStamp) ");
    sql.append(" group by ipv4, ipv6, timeStamp ");
    sql.append(" order by timeStamp asc ");

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#queryTransmitIpTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryTransmitIpTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    sql.append(
        " select ipv4, ipv6, sum(transmit_bytes) as transmit_bytes, sum(transmit_packets) as transmit_packets ");
    sql.append(" from ");
    sql.append(" (select src_ipv4 as ipv4, src_ipv6 as ipv6, ");
    sql.append(
        " sum(transmit_bytes) as transmit_bytes, sum(transmit_packets) as transmit_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);
    sql.append(" group by ipv4, ipv6 ");
    sql.append(" union all ");
    sql.append(" select dest_ipv4 as ipv4, dest_ipv6 as ipv6, ");
    sql.append(" sum(ingest_bytes) as transmit_bytes, sum(ingest_packets) as transmit_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);
    sql.append(" group by ipv4, ipv6) ");
    sql.append(" group by ipv4, ipv6 ");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#queryTransmitIpTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryTransmitIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());

    sql.append(
        " select ipv4, ipv6, timeStamp, sum(transmit_bytes) as transmit_bytes, sum(transmit_packets) as transmit_packets ");
    sql.append(" from ");
    sql.append(" (select src_ipv4 as ipv4, src_ipv6 as ipv6, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second ) as timeStamp, ");
    params.put("interval", queryVO.getInterval());

    sql.append(
        " sum(transmit_bytes) as transmit_bytes, sum(transmit_packets) as transmit_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);

    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");

    sql.append(" group by ipv4, ipv6, timeStamp ");
    sql.append(" union all ");
    sql.append(" select dest_ipv4 as ipv4, dest_ipv6 as ipv6, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second ) as timeStamp, ");
    sql.append(" sum(ingest_bytes) as transmit_bytes, sum(ingest_packets) as transmit_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);

    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");

    sql.append(" group by ipv4, ipv6, timeStamp) ");
    sql.append(" group by ipv4, ipv6, timeStamp ");
    sql.append(" order by timeStamp asc ");

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#queryIngestIpTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryIngestIpTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    sql.append(
        " select ipv4, ipv6, sum(ingest_bytes) as ingest_bytes, sum(ingest_packets) as ingest_packets ");
    sql.append(" from ");
    sql.append(" (select src_ipv4 as ipv4, src_ipv6 as ipv6, ");
    sql.append(" sum(ingest_bytes) as ingest_bytes, sum(ingest_packets) as ingest_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);
    sql.append(" group by ipv4, ipv6 ");
    sql.append(" union all ");
    sql.append(" select dest_ipv4 as ipv4, dest_ipv6 as ipv6, ");
    sql.append(" sum(transmit_bytes) as ingest_bytes, sum(transmit_packets) as ingest_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);
    sql.append(" group by ipv4, ipv6) ");
    sql.append(" group by ipv4, ipv6 ");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#queryIngestIpTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryIngestIpTableHistogram(MetricNetflowQueryVO queryVO,
      List<Map<String, Object>> topData) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    List<String> ipAddressList = topData.stream().map(item -> {
      Inet4Address ipv4Address = (Inet4Address) item.get("ipv4");
      Inet6Address ipv6Address = (Inet6Address) item.get("ipv6");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (ipv4Address != null) {
        sb.append(" and ipv4=toIPv4('").append(ipv4Address.getHostAddress()).append("')");
      }
      if (ipv6Address != null) {
        sb.append(" and ipv6=toIPv6('").append(ipv6Address.getHostAddress()).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());

    sql.append(
        " select ipv4, ipv6, timeStamp, sum(ingest_bytes) as ingest_bytes, sum(ingest_packets) as ingest_packets ");
    sql.append(" from ");
    sql.append(" (select src_ipv4 as ipv4, src_ipv6 as ipv6, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second ) as timeStamp, ");
    params.put("interval", queryVO.getInterval());

    sql.append(" sum(ingest_bytes) as ingest_bytes, sum(ingest_packets) as ingest_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);

    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");

    sql.append(" group by ipv4, ipv6, timeStamp ");
    sql.append(" union all ");
    sql.append(" select dest_ipv4 as ipv4, dest_ipv6 as ipv6, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second ) as timeStamp, ");
    sql.append(" sum(transmit_bytes) as ingest_bytes, sum(transmit_packets) as ingest_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichIpWhereSql(queryVO, params, sql);

    sql.append(" and (");
    sql.append(StringUtils.join(ipAddressList, " or "));
    sql.append(" ) ");

    sql.append(" group by ipv4, ipv6, timeStamp) ");
    sql.append(" group by ipv4, ipv6, timeStamp ");
    sql.append(" order by timeStamp asc ");


    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#queryProtocolportTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryProtocolportTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    sql.append(" select port, protocol, total_bytes, total_packets ");
    sql.append(" from ");
    sql.append(" (select src_port as port, protocol, ");
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichProtocolPortWhereSql(queryVO, params, sql);
    sql.append(" group by port, protocol ");
    sql.append(" union all ");
    sql.append(" select dest_port as port, protocol, ");
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichProtocolPortWhereSql(queryVO, params, sql);
    sql.append(" group by port, protocol) ");
    sql.append(" group by port, protocol, total_bytes, total_packets");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#queryProtocolportTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryProtocolportTableHistogram(MetricNetflowQueryVO queryVO,
      List<String> topData) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> condition = Lists.newArrayListWithCapacity(topData.size());
    StringBuilder whereSql = whereSqlStatement(queryVO, params);
    int index = 0;
    for (String protocolPort : topData) {
      String protocol = StringUtils.substringBefore(protocolPort, "_");
      String port = StringUtils.substringAfter(protocolPort, "_");
      condition.add(String.format("protocol = :protocol%s and port = :port%s", index, index));
      params.put("protocol" + index, protocol);
      params.put("port" + index, port);
      index += 1;
    }

    sql.append(" select port, protocol, timeStamp, total_bytes, total_packets ");
    sql.append(" from ");
    sql.append(" (select src_port as port, protocol, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second ) as timeStamp, ");
    params.put("interval", queryVO.getInterval());
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichProtocolPortWhereSql(queryVO, params, sql);
    sql.append(" and (").append(StringUtils.join(condition, " ) or (")).append(")");
    sql.append(" group by port, protocol, timeStamp ");
    sql.append(" union all ");
    sql.append(" select dest_port as port, protocol, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second ) as timeStamp, ");
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichProtocolPortWhereSql(queryVO, params, sql);
    sql.append(" and (").append(StringUtils.join(condition, " ) or (")).append(")");
    sql.append(" group by port, protocol, timeStamp) ");
    sql.append(" group by port, protocol, timeStamp, total_bytes, total_packets ");
    sql.append(" order by timeStamp asc ");

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#querySessionTable(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionTable(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    sql.append(
        " select toString(session_id) as session_id, protocol, src_ipv4, src_ipv6, src_port, dest_ipv4, dest_ipv6, dest_port, ");
    sql.append(" sum(total_bytes) as total_bytes, sum(total_packets) as total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    // 添加查询条件
    sql.append(whereSql.toString());
    enrichSessionWhereSql(queryVO, params, sql);
    sql.append(
        " group by session_id, protocol, src_ipv4, src_ipv6, src_port, dest_ipv4, dest_ipv6, dest_port ");
    sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);
    sql.append(" limit ").append(TOP_SIZE);

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#querySessionTableHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public List<Map<String, Object>> querySessionTableHistogram(MetricNetflowQueryVO queryVO,
      List<String> topData) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = whereSqlStatement(queryVO, params);

    params.put("interval", queryVO.getInterval());
    sql.append(
        " select toString(session_id) as session_id, src_ipv4, src_ipv6, dest_ipv4, dest_ipv6, src_port, dest_port, ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) AS timeStamp, ");
    sql.append(" SUM(total_bytes) AS total_bytes, SUM(total_packets) AS total_packets ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    sql.append(whereSql.toString());
    enrichSessionWhereSql(queryVO, params, sql);
    sql.append(" and session_id in (:topData) ");
    params.put("topData", topData);
    sql.append(
        " group by timeStamp, session_id, src_ipv4, src_ipv6, dest_ipv4, dest_ipv6, src_port, dest_port ");
    sql.append(" order by timeStamp ASC ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("querySessionRecordHistogramData sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());
    return result;
  }

  // 会话详单页面使用
  private void enrichWhereSql(MetricNetflowQueryVO queryVO, Map<String, Object> params,
      StringBuilder whereSql) {

    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> dsl = dslConverter.converte(queryVO.getDsl(), false,
                queryVO.getTimePrecision(), queryVO.getSessionIncludeStartTime(),
                queryVO.getSessionIncludeEndTime());
        whereSql.append(" and ");
        whereSql.append(dsl.getT1());
        params.putAll(dsl.getT2());
        return;
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getSessionIncludeStartTime(), queryVO.getSessionIncludeEndTime(), whereSql, params);
  }

  private void enrichIpWhereSql(MetricNetflowQueryVO queryVO, Map<String, Object> params,
      StringBuilder whereSql) {
    // 使用dsl表达式查询
    List<Map<String, Object>> filterContents = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        filterContents = spl2SqlHelper.getFilterContent(queryVO.getDsl());
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getStatisticsIncludeStartTime(), queryVO.getStatisticsIncludeEndTime(), whereSql, params);

    for (Map<String, Object> fieldMap : filterContents) {
      String field = MapUtils.getString(fieldMap, "field");
      String fieldType = MapUtils.getString(fieldMap, "fieldType");
      String operator = MapUtils.getString(fieldMap, "operator");

      Object operand = fieldMap.get("operand");

      if (StringUtils.equals(fieldType, "IPv4")) {

        if (StringUtils.equals(field, "src_ip")) {
          whereSql.append(" and ipv4 " + operator + " toIPv4(:src_ip) ");
          params.put("src_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "dest_ip")) {
          whereSql.append(" and ipv4 " + operator + " toIPv4(:dest_ip) ");
          params.put("dest_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "ip_address")) {
          whereSql.append(" and ipv4 " + operator + " toIPv4(:ip_address) ");
          params.put("ip_address", operand);
          continue;
        }
      }

      if (StringUtils.equals(fieldType, "IPv6")) {

        if (StringUtils.equals(field, "src_ip")) {
          whereSql.append(" and ipv6 " + operator + " toIPv6(:src_ip) ");
          params.put("src_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "dest_ip")) {
          whereSql.append(" and ipv6 " + operator + " toIPv6(:dest_ip) ");
          params.put("dest_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "ip_address")) {
          whereSql.append(" and ipv6 " + operator + " toIPv6(:ip_address) ");
          params.put("ip_address", operand);
          continue;
        }
      }

      if (StringUtils.equals(field, "port")) {
        whereSql
            .append(" and (src_port " + operator + " :port or dest_port " + operator + " :port) ");
        params.put("port", operand);
        continue;
      }

      if (StringUtils.equals(field, "src_port")) {
        whereSql.append(" and (src_port " + operator + " :src_port) ");
        params.put("src_port", operand);
        continue;
      }

      if (StringUtils.equals(field, "dest_port")) {
        whereSql.append(" and (dest_port " + operator + " :dest_port) ");
        params.put("dest_port", operand);
        continue;
      }

      if (StringUtils.equals(field, "protocol")) {
        whereSql.append(" and (protocol " + operator + " :protocol) ");
        params.put("protocol", operand);
        continue;
      }
    }
  }

  private void enrichProtocolPortWhereSql(MetricNetflowQueryVO queryVO, Map<String, Object> params,
      StringBuilder whereSql) {
    // 使用dsl表达式查询
    List<Map<String, Object>> filterContents = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        filterContents = spl2SqlHelper.getFilterContent(queryVO.getDsl());
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getStatisticsIncludeStartTime(), queryVO.getStatisticsIncludeEndTime(), whereSql, params);

    for (Map<String, Object> fieldMap : filterContents) {
      String field = MapUtils.getString(fieldMap, "field");
      String fieldType = MapUtils.getString(fieldMap, "fieldType");
      String operator = MapUtils.getString(fieldMap, "operator");

      Object operand = fieldMap.get("operand");

      if (StringUtils.equals(fieldType, "IPv4")) {

        if (StringUtils.equals(field, "src_ip")) {
          whereSql.append(" and src_ipv4 " + operator + " toIPv4(:src_ip) ");
          params.put("src_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "dest_ip")) {
          whereSql.append(" and dest_ipv4 " + operator + " toIPv4(:dest_ip) ");
          params.put("dest_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "ip_address")) {
          whereSql.append(" and (src_ipv4 " + operator + " toIPv4(:ip_address) or dest_ipv4 "
              + operator + " toIPv4(:ip_address)) ");
          params.put("ip_address", operand);
          continue;
        }
      }

      if (StringUtils.equals(fieldType, "IPv6")) {

        if (StringUtils.equals(field, "src_ip")) {
          whereSql.append(" and src_ipv6 " + operator + " toIPv6(:src_ip) ");
          params.put("src_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "dest_ip")) {
          whereSql.append(" and dest_ipv6 " + operator + " toIPv6(:dest_ip) ");
          params.put("dest_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "ip_address")) {
          whereSql.append(" and (src_ipv6 " + operator + " toIPv6(:ip_address) or dest_ipv6 "
              + operator + " toIPv6(:ip_address)) ");
          params.put("ip_address", operand);
          continue;
        }
      }

      if (StringUtils.equals(field, "port")) {
        whereSql.append(" and port " + operator + " :port ");
        params.put("port", operand);
        continue;
      }

      if (StringUtils.equals(field, "src_port")) {
        whereSql.append(" and (src_port " + operator + " :src_port) ");
        params.put("src_port", operand);
        continue;
      }

      if (StringUtils.equals(field, "dest_port")) {
        whereSql.append(" and (dest_port " + operator + " :dest_port) ");
        params.put("dest_port", operand);
        continue;
      }

      if (StringUtils.equals(field, "protocol")) {
        whereSql.append(" and (protocol " + operator + " :protocol) ");
        params.put("protocol", operand);
        continue;
      }
    }
  }

  private void enrichSessionWhereSql(MetricNetflowQueryVO queryVO, Map<String, Object> params,
      StringBuilder whereSql) {
    // 使用dsl表达式查询
    List<Map<String, Object>> filterContents = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        filterContents = spl2SqlHelper.getFilterContent(queryVO.getDsl());
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getStatisticsIncludeStartTime(), queryVO.getStatisticsIncludeEndTime(), whereSql, params);

    for (Map<String, Object> fieldMap : filterContents) {
      String field = MapUtils.getString(fieldMap, "field");
      String fieldType = MapUtils.getString(fieldMap, "fieldType");
      String operator = MapUtils.getString(fieldMap, "operator");

      Object operand = fieldMap.get("operand");

      if (StringUtils.equals(fieldType, "IPv4")) {

        if (StringUtils.equals(field, "src_ip")) {
          whereSql.append(" and src_ipv4 " + operator + " toIPv4(:src_ip) ");
          params.put("src_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "dest_ip")) {
          whereSql.append(" and dest_ipv4 " + operator + " toIPv4(:dest_ip) ");
          params.put("dest_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "ip_address")) {
          whereSql.append(" and (src_ipv4 " + operator + " toIPv4(:ip_address) or dest_ipv4 "
              + operator + " toIPv4(:ip_address)) ");
          params.put("ip_address", operand);
          continue;
        }
      }

      if (StringUtils.equals(fieldType, "IPv6")) {

        if (StringUtils.equals(field, "src_ip")) {
          whereSql.append(" and src_ipv6 " + operator + " toIPv6(:src_ip) ");
          params.put("src_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "dest_ip")) {
          whereSql.append(" and dest_ipv6 " + operator + " toIPv6(:dest_ip) ");
          params.put("dest_ip", operand);
          continue;
        }
        if (StringUtils.equals(field, "ip_address")) {
          whereSql.append(" and (src_ipv6 " + operator + " toIPv6(:ip_address) or dest_ipv6 "
              + operator + " toIPv6(:ip_address)) ");
          params.put("ip_address", operand);
          continue;
        }
      }

      if (StringUtils.equals(field, "port")) {
        whereSql
            .append(" and (src_port " + operator + " :port or dest_port " + operator + " :port) ");
        params.put("port", operand);
        continue;
      }

      if (StringUtils.equals(field, "src_port")) {
        whereSql.append(" and (src_port " + operator + " :src_port) ");
        params.put("src_port", operand);
        continue;
      }

      if (StringUtils.equals(field, "dest_port")) {
        whereSql.append(" and (dest_port " + operator + " :dest_port) ");
        params.put("dest_port", operand);
        continue;
      }

      if (StringUtils.equals(field, "protocol")) {
        whereSql.append(" and (protocol " + operator + " :protocol) ");
        params.put("protocol", operand);
        continue;
      }
    }
  }


  private StringBuilder whereSqlStatement(MetricNetflowQueryVO queryVO,
      Map<String, Object> params) {
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where device_name = :device_name ");
    params.put("device_name", queryVO.getDeviceName());
    if (StringUtils.isNotBlank(queryVO.getNetifNo())) {
      whereSql.append(" and (in_netif = :netif_no or out_netif = :netif_no) ");
      params.put("netif_no", queryVO.getNetifNo());
    }
    return whereSql;
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

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#querySessionRecord(java.lang.String, com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.util.List)
   */
  @Override
  public Page<Map<String, Object>> querySessionRecords(String queryId, Pageable page,
      MetricNetflowQueryVO queryVO, List<String> sessionIds) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 查询符合条件的记录report_time, session_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder(securityQueryId);
    innerSelectSql.append(" select report_time, toString(session_id) as session_id from ")
        .append(TABLE_NETFLOW_SESSION_RECORD);
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = whereSqlStatement(queryVO, innerParams);
    enrichWhereSql(queryVO, innerParams, whereSql);

    // 会话ID集合
    if (CollectionUtils.isNotEmpty(sessionIds)) {
      whereSql.append(" and session_id in (:sessionIds) ");
      innerParams.put("sessionIds", sessionIds);
    }

    innerSelectSql.append(whereSql);

    PageUtils.appendPage(innerSelectSql, page, MetricNetflowQueryVO.class);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query session record page, inner sql: {}, params: {}",
          innerSelectSql.toString(), innerParams);
    }

    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    // 使用已查到的report_time,session_id查找对应记录的全部字段
    List<String> rowIds = innerResult.stream()
        .map(r -> DateUtils.toStringYYYYMMDDHHMMSS((OffsetDateTime) r.get("report_time"),
            ZoneId.of("UTC")) + "_" + MapUtils.getString(r, "session_id"))
        .collect(Collectors.toList());

    List<Map<String, Object>> result = querySessionRecordByIds(queryId, page.getSort(), rowIds,
        queryVO);
    return new PageImpl<>(result, page, 0);
  }

  /**
   * @see com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao#countSessionRecords(java.lang.String, com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO)
   */
  @Override
  public long countSessionRecords(String queryId, MetricNetflowQueryVO queryVO) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    StringBuilder totalSql = new StringBuilder(securityQueryId);
    totalSql.append(" select count(1) from ");
    totalSql.append(TABLE_NETFLOW_SESSION_RECORD);

    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = whereSqlStatement(queryVO, innerParams);
    enrichWhereSql(queryVO, innerParams, whereSql);
    // 会话ID集合
    if (StringUtils.isNotEmpty(queryVO.getSessionId())) {
      totalSql.append(" and sessionId in (:sessionIds) ");
      innerParams.put("sessionIds", queryVO.getSessionId());
    }

    totalSql.append(whereSql);

    return queryForLongWithExceptionHandle(totalSql.toString(), innerParams);
  }

  private List<Map<String, Object>> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<Map<String, Object>> rowMapper) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    try {
      result = clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("queryNetflows has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryNetflows failed, error msg: {}",
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

  private List<Map<String, Object>> querySessionRecordByIds(String queryId, Sort sort,
      List<String> ids, MetricNetflowQueryVO queryVO) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 使用id查询记录，id的格式为reportTime_sessionId
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> reportTimeCondition = Lists.newArrayListWithCapacity(ids.size());
    List<String> sessionIdCondition = Lists.newArrayListWithCapacity(ids.size());
    int index = 0;
    for (String id : ids) {
      String[] filters = StringUtils.split(id, "_");
      // report_time
      reportTimeCondition.add(String.format("toDateTime64(:report_time%s, 9, 'UTC')", index));
      params.put("report_time" + index, filters[0]);
      // session_id
      sessionIdCondition.add(String.format(":session_id%s", index));
      params.put("session_id" + index, filters[1]);

      index += 1;
    }
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append("select * from (");

    // 嵌套子查询
    StringBuilder innerSql = buildSelectStatement(securityQueryId);

    // report_time过滤
    StringBuilder whereSql = whereSqlStatement(queryVO, params);
    innerSql.append(whereSql);
    innerSql.append(" and report_time in (");
    innerSql.append(StringUtils.join(reportTimeCondition, ",")).append(")");
    sql.append(innerSql).append(")");

    // session_id过滤
    sql.append(" where session_id in (");
    sql.append(StringUtils.join(sessionIdCondition, ",")).append(")");

    PageUtils.appendSort(sql, sort, MetricNetflowQueryVO.class);
    sql.append(" limit ").append(SESSION_RECORD_SIZE);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query session record page by ids, ids: {}, sql: {}, params: {}",
          StringUtils.join(ids), sql.toString(), params);
    }

    List<Map<String, Object>> allResult = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    List<Map<String, Object>> filterResult = allResult.stream().filter(item -> ids.contains(
        DateUtils.toStringYYYYMMDDHHMMSS((OffsetDateTime) item.get("report_time"), ZoneId.of("UTC"))
            + "_" + MapUtils.getString(item, "session_id")))
        .collect(Collectors.toList());

    return filterResult;
  }

  private StringBuilder buildSelectStatement(String queryId) {
    StringBuilder sql = new StringBuilder(queryId);
    sql.append(" select report_time, src_ipv4, src_ipv6, dest_ipv4, dest_ipv6, toString(session_id) as session_id, ");
    sql.append(" src_port, dest_port, protocol, total_bytes, total_packets, ");
    sql.append(" transmit_bytes, transmit_packets, ingest_bytes, ingest_packets, ");
    sql.append(" tcp_flag, dscp_flag, duration, device_name, in_netif, out_netif, ");
    sql.append(" start_time, end_time ");
    sql.append(" from ").append(TABLE_NETFLOW_SESSION_RECORD);
    return sql;
  }

  private Long queryForLongWithExceptionHandle(String sql, Map<String, ?> paramMap) {
    Long result = 0L;
    try {
      result = clickHouseTemplate.getJdbcTemplate().queryForObject(sql, paramMap, Long.class);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("querySessionRecords has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("querySessionRecords failed, error msg: {}",
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
