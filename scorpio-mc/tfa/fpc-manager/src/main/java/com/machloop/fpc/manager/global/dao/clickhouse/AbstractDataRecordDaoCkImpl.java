package com.machloop.fpc.manager.global.dao.clickhouse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.DataRecordDaoCk;
import com.machloop.fpc.manager.global.data.AbstractDataRecordDO;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisSubTaskDO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年6月11日, fpc-manager
 */
public abstract class AbstractDataRecordDaoCkImpl implements DataRecordDaoCk {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataRecordDaoCkImpl.class);

  private static final int SCALE_COUNTS = 4;

  protected static final int COMPOSITE_BATCH_SIZE = 10000;

  protected static final String DEFAULT_SORT_FIELD = "total_bytes";
  protected static final String DEFAULT_SORT_DIRECTION = "desc";

  private static final String DIVIDE_NULL_NAN = "NaN";
  private static final String DIVIDE_NULL_INF = "Infinity";

  protected static final String QUERY_RESULT_DATE_PATTERN_OLD = "yyyy-MM-dd HH:mm:ss.SSS";
  protected static final String QUERY_RESULT_DATE_PATTERN_NEW = "yyyy-MM-dd'T'HH:mm:ssXXX";

  private static final Map<String,
      String> metricFields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
  static {
    metricFields.put("tcpClientNetworkLatencyAvg", "客户端网络时延迟均值");
    metricFields.put("tcpClientNetworkLatencyAvgInsideService", "客户端网络时延均值-内网服务");
    metricFields.put("tcpClientNetworkLatencyAvgOutsideService", "客户端网络时延均值-外网服务");
    metricFields.put("tcpServerNetworkLatencyAvg", "服务端网络时延均值");
    metricFields.put("tcpServerNetworkLatencyAvgInsideService", "服务端网络时延均值-内网服务");
    metricFields.put("tcpServerNetworkLatencyAvgOutsideService", "服务端网络时延均值-外网服务");
    metricFields.put("serverResponseLatencyAvg", "服务端响应时延均值");
    metricFields.put("serverResponseLatencyAvgInsideService", "服务端响应时延均值-内网服务");
    metricFields.put("serverResponseLatencyAvgOutsideService", "服务端响应时延均值-外网服务");
    metricFields.put("tcpClientRetransmissionRate", "客户端重传率");
    metricFields.put("tcpClientRetransmissionRateInsideService", "客户端重传率-内网服务");
    metricFields.put("tcpClientRetransmissionRateOutsideService", "客户端重传率-外网服务");
    metricFields.put("tcpServerRetransmissionRate", "服务端重传率");
    metricFields.put("tcpServerRetransmissionRateInsideService", "服务端重传率-内网服务");
    metricFields.put("tcpServerRetransmissionRateOutsideService", "服务端重传率-外网服务");
    metricFields.put("tcpEstablishedFailCountsRate", "建连失败率");
    metricFields.put("tcpEstablishedFailCountsInsideServiceRate", "建连失败率-内网服务");
    metricFields.put("tcpEstablishedFailCountsOutsideServiceRate", "建连失败率-外网服务");
    metricFields.put("tcpClientEstablishedFailCountsRate", "客户端建连失败率");
    metricFields.put("tcpClientEstablishedFailCountsInsideServiceRate", "客户端建连失败率-内网服务");
    metricFields.put("tcpClientEstablishedFailCountsOutsideServiceRate", "客户端建连失败率-外网服务");
    metricFields.put("tcpServerEstablishedFailCountsRate", "服务端建连失败率");
    metricFields.put("tcpServerEstablishedFailCountsInsideServiceRate", "服务端建连失败率-内网服务");
    metricFields.put("tcpServerEstablishedFailCountsOutsideServiceRate", "服务端建连失败率-外网服务");
    metricFields.put("tcpClientRecvRetransmissionPacketsRate", "客户端接收重传率");
    metricFields.put("tcpClientRecvRetransmissionPacketsInsideServiceRate", "客户端接收重传率-内网服务");
    metricFields.put("tcpClientRecvRetransmissionPacketsOutsideServiceRate", "客户端接收重传率-外网服务");
    metricFields.put("tcpClientSendRetransmissionPacketsRate", "客户端发送重传率");
    metricFields.put("tcpClientSendRetransmissionPacketsInsideServiceRate", "客户端发送重传率-内网服务");
    metricFields.put("tcpClientSendRetransmissionPacketsOutsideServiceRate", "客户端发送重传率-外网服务");
    metricFields.put("tcpServerRecvRetransmissionPacketsRate", "服务端接收重传率");
    metricFields.put("tcpServerRecvRetransmissionPacketsInsideServiceRate", "服务端接收重传率-内网服务");
    metricFields.put("tcpServerRecvRetransmissionPacketsOutsideServiceRate", "服务端接收重传率-外网服务");
    metricFields.put("tcpServerSendRetransmissionPacketsRate", "服务端发送重传率");
    metricFields.put("tcpServerSendRetransmissionPacketsInsideServiceRate", "服务端发送重传率-内网服务");
    metricFields.put("tcpServerSendRetransmissionPacketsOutsideServiceRate", "服务端发送重传率-外网服务");

  }

  /**
   * @see com.machloop.fpc.manager.global.dao.DataRecordDaoCk#rollup(java.util.Date, java.util.Date)
   */
  @Override
  public int rollup(final Date startTime, final Date endTime) throws IOException {

    int success = 0;

    String inputTableName;
    String outputTableName;

    long intervalSeconds = (endTime.getTime() - startTime.getTime()) / 1000;
    if (intervalSeconds == Constants.FIVE_MINUTE_SECONDS) {
      // 60秒汇总到5分钟
      inputTableName = getTableName();
      outputTableName = getTableName() + "_5m";
    } else if (intervalSeconds == Constants.ONE_HOUR_SECONDS) {
      // 5分钟汇总到1小时
      inputTableName = getTableName() + "_5m";
      outputTableName = getTableName() + "_1h";
    } else {
      return success;
    }

    // 聚合前先查询是否存在当前聚合时间的脏数据，有则清除, 防止重复统计
    StringBuilder countSql = new StringBuilder();
    countSql.append("select count(1) from ");
    countSql.append(outputTableName);
    countSql.append(" where timestamp > toDateTime64(:timestamp, 3, 'UTC') ");

    Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(1);
    paramMap.put("timestamp",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    long dirtyCount = getClickHouseJdbcTemplate().getJdbcTemplate()
        .queryForObject(countSql.toString(), paramMap, Long.class);
    if (dirtyCount > 0) {
      StringBuilder deleteSql = new StringBuilder();
      deleteSql.append(" alter table ").append(outputTableName);
      deleteSql.append(" delete where timestamp > toDateTime64(:timestamp, 3, 'UTC') ");

      getClickHouseJdbcTemplate().getJdbcTemplate().update(deleteSql.toString(), paramMap);
      LOGGER.info("found dirty rollup record in [{}], start to clean, total: [{}].",
          outputTableName, dirtyCount);
    }

    // 聚合统计inputTableName
    success += aggregate(startTime, endTime, (int) intervalSeconds, inputTableName,
        outputTableName);

    LOGGER.info(
        "finish to roll up, input tableName: [{}], out tableName: [{}], startTime: [{}], endTime: [{}], "
            + "total roll up sucess count: [{}]",
        inputTableName, outputTableName, startTime, endTime, success);

    return success;
  }

  protected abstract Spl2SqlHelper getSpl2SqlHelper();

  protected abstract ClickHouseStatsJdbcTemplate getClickHouseJdbcTemplate();

  protected abstract PacketAnalysisSubTaskDao getOfflineAnalysisSubTaskDao();

  protected abstract String getTableName();

  protected abstract int aggregate(final Date startTime, final Date endTime, final int interval,
      final String inputTableName, final String outputTableName) throws IOException;

  /**
   * 查询基础数据
   * @param queryVO
   * @return
   * @throws IOException
   */
  protected List<Map<String, Object>> queryMetricDataRecord(MetricQueryVO queryVO)
      throws IOException {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    if (StringUtils.isBlank(queryVO.getServiceId())) {
      whereSql.append(" and service_id = '' ");
    }

    StringBuilder sql = new StringBuilder();
    sql.append("select * from ").append(convertTableName(queryVO.getSourceType(),
        queryVO.getInterval(), queryVO.getPacketFileId()));
    sql.append(whereSql.toString());
    sql.append(" order by timestamp ");
    sql.append(" limit ").append(
        Integer.parseInt(HotPropertiesHelper.getProperty("rest.metric.result.query.max.count")));

    List<Map<String, Object>> result = getClickHouseJdbcTemplate().getJdbcTemplate()
        .query(sql.toString(), params, new ColumnMapRowMapper());

    return result.stream().map(item -> {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(item.size());
      item.forEach((k, v) -> {
        map.put(TextUtils.underLineToCamel(k), v);
      });
      map.put("timestamp", DateUtils.toStringISO8601((OffsetDateTime) item.get("timestamp"),
          ZoneId.systemDefault()));

      return map;
    }).collect(Collectors.toList());
  }

  /**
   * 流量分析聚合统计
   * @param tableName 表名
   * @param queryVO 过滤条件
   * @param additionalConditions 附加条件
   * @param keys <分组字段名， 别名>
   * @param aggFields <聚合字段名，<聚合类型，别名>>
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @return 统计结果
   */
  protected List<Map<String, Object>> termMetricAggregate(String tableName, MetricQueryVO queryVO,
      List<Map<String, Object>> additionalConditions, Map<String, String> keys,
      Map<String, Tuple2<AggsFunctionEnum, String>> aggFields, String sortProperty,
      String sortDirection) throws IOException {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    // 添加附加条件
    fillAdditionalConditions(additionalConditions, whereSql, params);

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    // 分组字段
    List<String> terms = keys.entrySet().stream()
        .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
    sql.append(" select ").append(StringUtils.join(terms, ","));
    // 聚合字段
    if (!StringUtils.equals(queryVO.getColumns(), "*")) {
      // 过滤要查询的列
      Set<String> fields = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      CsvUtils.convertCSVToList(queryVO.getColumns()).forEach(
          field -> fields.addAll(getCombinationAggsFields(TextUtils.camelToUnderLine(field))));
      fields.addAll(getCombinationAggsFields(sortProperty));

      aggFields = aggFields.entrySet().stream()
          .filter(aggsField -> fields.contains(aggsField.getKey()))
          .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }
    aggFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(tableName);
    sql.append(whereSql.toString());
    sql.append(" group by ").append(StringUtils.join(keys.values(), ","));
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    if (queryVO.getCount() > 0) {
      sql.append(" limit ").append(queryVO.getCount());
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("termMetricAggregate sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = metricDataConversion(
        queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
    sortMetricResult(result, sortProperty, sortDirection);

    return result;
  }

  /**
   * 数据预聚合使用（总查询量大于10000，分页查询）
   * @param tableName 表名
   * @param queryVO 过滤条件
   * @param additionalConditions 附加条件
   * @param keys <分组字段名， 别名>
   * @param aggFields <聚合字段名，<聚合类型，别名>>
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @param limit 限制数量
   * @param offset 偏移
   * @return 统计结果
   * @throws IOException
   */
  protected List<Map<String, Object>> termMetricAggregate(String tableName, MetricQueryVO queryVO,
      List<Map<String, Object>> additionalConditions, Map<String, String> keys,
      Map<String, Tuple2<AggsFunctionEnum, String>> aggFields, String sortProperty,
      String sortDirection, int limit, int offset) throws IOException {
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    // 添加附加条件
    fillAdditionalConditions(additionalConditions, whereSql, params);

    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    // 分组字段
    List<String> terms = keys.entrySet().stream()
        .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
    sql.append(" select ").append(StringUtils.join(terms, ","));
    // 聚合字段
    aggFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(tableName);
    sql.append(whereSql.toString());
    sql.append(" group by ").append(StringUtils.join(keys.values(), ","));
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    sql.append(" limit ").append(limit).append(" offset ").append(offset);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("termMetricAggregate sql : [{}], param: [{}] ", sql.toString(), params);
    }

    return queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
  }

  /**
   * 流量分析趋势图（分组）
   * @param tableName 表名
   * @param queryVO 前端过滤条件
   * @param additionalConditions 附加条件
   * @param keys <分组字段名， 别名>
   * @param aggFields <聚合字段名，<聚合类型，别名>>
   * @return
   */
  protected List<Map<String, Object>> dateHistogramTermMetricAggregate(String tableName,
      MetricQueryVO queryVO, List<Map<String, Object>> additionalConditions,
      Map<String, String> keys, Map<String, Tuple2<AggsFunctionEnum, String>> aggFields)
      throws IOException {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    enrichWhereSql(queryVO, whereSql, params);
    // 添加附加条件
    fillAdditionalConditions(additionalConditions, whereSql, params);

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') as temp_timestamp, ");
    // 分组字段
    List<String> terms = keys.entrySet().stream()
        .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
    sql.append(StringUtils.join(terms, ","));
    // 聚合字段
    aggFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });

    sql.append(" from ").append(tableName);
    sql.append(whereSql.toString());
    sql.append(" group by temp_timestamp, ").append(StringUtils.join(keys.values(), ","));
    sql.append(" order by temp_timestamp ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("dateHistogramTermMetricAggregate sql : [{}], param: [{}] ", sql.toString(),
          params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      item.put("timestamp", item.get("temp_timestamp"));
      item.remove("temp_timestamp");
    });

    return metricDataConversion(result);
  }

  /**
   * 流量分析趋势图（不分组）
   * @param tableName 表名
   * @param queryVO 前端过滤条件
   * @param additionalConditions 附加条件
   * @param aggFields <聚合字段名，<聚合类型，别名>>
   * @return
   */
  protected List<Map<String, Object>> dateHistogramMetricAggregate(String tableName,
      MetricQueryVO queryVO, List<Map<String, Object>> additionalConditions,
      Map<String, Tuple2<AggsFunctionEnum, String>> aggFields) throws IOException {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    enrichWhereSql(queryVO, whereSql, params);
    // 添加附加条件
    fillAdditionalConditions(additionalConditions, whereSql, params);

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') as temp_timestamp");
    // 聚合字段
    aggFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });

    sql.append(" from ").append(tableName);
    sql.append(whereSql.toString());
    sql.append(" group by temp_timestamp ");
    sql.append(" order by temp_timestamp ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("dateHistogramMetricAggregate sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      item.put("timestamp", item.get("temp_timestamp"));
      item.remove("temp_timestamp");
    });

    return metricDataConversion(result);
  }

  protected <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = getClickHouseJdbcTemplate().getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query flow metric has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query flow metric failed, error msg: {}",
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

  /**
   * 关键指标
   * @param kpiAggs 关键指标
   */
  protected void kpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> kpiAggs) {
    // 总字节数
    kpiAggs.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    // 总包数
    kpiAggs.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
    // 新建会话数
    kpiAggs.put("establishedSessions", Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
    // TCP客户端网络总时延
    kpiAggs.put("tcpClientNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
    // TCP客户端网络时延统计次数
    kpiAggs.put("tcpClientNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts"));
    // TCP服务端网络总时延
    kpiAggs.put("tcpServerNetworkLatency",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
    // TCP服务端网络时延统计次数
    kpiAggs.put("tcpServerNetworkLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts"));
    // 服务端响应总时延
    kpiAggs.put("serverResponseLatency",
        Tuples.of(AggsFunctionEnum.SUM, "server_response_latency"));
    // 服务端响应时延统计次数
    kpiAggs.put("serverResponseLatencyCounts",
        Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_counts"));
    // TCP客户端总包数
    kpiAggs.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
    // TCP客户端重传包数
    kpiAggs.put("tcpClientRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
    // TCP服务端总包数
    kpiAggs.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
    // TCP服务端重传包数
    kpiAggs.put("tcpServerRetransmissionPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
    // 客户端零窗口包数
    kpiAggs.put("tcpClientZeroWindowPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_client_zero_window_packets"));
    // 服务端零窗口包数
    kpiAggs.put("tcpServerZeroWindowPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_server_zero_window_packets"));
    // TCP建连成功数
    kpiAggs.put("tcpEstablishedSuccessCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_established_success_counts"));
    // TCP建连失败数
    kpiAggs.put("tcpEstablishedFailCounts",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_counts"));
  }

  /**
   * 关键指标
   * @param kpiAggs 关键指标
   */
  protected void specialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> kpiAggs) {
    // TCP客户端时延均值
    kpiAggs.put("tcpClientNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpClientNetworkLatency, tcpClientNetworkLatencyCounts"));
    // TCP服务端时延均值
    kpiAggs.put("tcpServerNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
        "tcpServerNetworkLatency, tcpServerNetworkLatencyCounts"));
    // 服务端响应时延均值
    kpiAggs.put("serverResponseLatencyAvg",
        Tuples.of(AggsFunctionEnum.DIVIDE, "serverResponseLatency, serverResponseLatencyCounts"));
    // TCP客户端重传率
    kpiAggs.put("tcpClientRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpClientRetransmissionPackets, tcpClientPackets"));
    // TCP服务端重传率
    kpiAggs.put("tcpServerRetransmissionRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpServerRetransmissionPackets, tcpServerPackets"));
  }

  /**
   * 流日志基础字段聚合方式
   * @param aggFields
   * @param
   * @param field
   * @return
   */
  protected void aggregateFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggFields,
      String field) {

    // 总字节数
    if (StringUtils.equals(field, "total_bytes")) {
      aggFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    }
    // 上行字节数
    if (StringUtils.equals(field, "upstream_bytes")) {
      aggFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    }
    // 下行字节数
    if (StringUtils.equals(field, "downstream_bytes")) {
      aggFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    }
    // 总负载字节数
    if (StringUtils.equals(field, "total_payload_bytes")) {
      aggFields.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    }
    // 上行负载字节数
    if (StringUtils.equals(field, "upstream_payload_bytes")) {
      aggFields.put("upstreamPayloadBytes",
          Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));
    }
    // 下行负载字节数
    if (StringUtils.equals(field, "downstream_payload_bytes")) {
      aggFields.put("downstreamPayloadBytes",
          Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));
    }
    // 总包数
    if (StringUtils.equals(field, "total_packets")) {
      aggFields.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
    }
    // 上行包数
    if (StringUtils.equals(field, "upstream_packets")) {
      aggFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    }
    // 下行包数
    if (StringUtils.equals(field, "downstream_packets")) {
      aggFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    }
    // 总负载包数
    if (StringUtils.equals(field, "total_payload_packets")) {
      aggFields.put("totalPayloadPackets",
          Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    }
    // 上行负载包数
    if (StringUtils.equals(field, "upstream_payload_packets")) {
      aggFields.put("upstreamPayloadPackets",
          Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));
    }
    // 下行负载包数
    if (StringUtils.equals(field, "downstream_payload_packets")) {
      aggFields.put("downstreamPayloadPackets",
          Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));
    }
    // tcp同步数据包
    if (StringUtils.equals(field, "tcp_syn_packets")) {
      aggFields.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    }
    // tcp同步确认数据包
    if (StringUtils.equals(field, "tcp_syn_ack_packets")) {
      aggFields.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    }
    // tcp同步重置数据包
    if (StringUtils.equals(field, "tcp_syn_rst_packets")) {
      aggFields.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));
    }
    // 新建会话数
    if (StringUtils.equals(field, "established_sessions")) {
      aggFields.put("establishedSessions", Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));
    }
    // 主动新建会话数
    if (StringUtils.equals(field, "active_established_sessions")) {
      aggFields.put("activeEstablishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "active_established_sessions"));
    }
    // 被动新建会话数
    if (StringUtils.equals(field, "passive_established_sessions")) {
      aggFields.put("passiveEstablishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "passive_established_sessions"));
    }
    // 客户端网络总时延
    if (StringUtils.equals(field, "tcp_client_network_latency")) {
      aggFields.put("tcpClientNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
    }
    // 客户端网络时延均值
    if (StringUtils.equals(field, "tcp_client_network_latency_avg")) {
      aggFields.put("tcpClientNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency"));
      aggFields.put("tcpClientNetworkLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_network_latency_counts"));
    }
    // 服务端网络总时延
    if (StringUtils.equals(field, "tcp_server_network_latency")) {
      aggFields.put("tcpServerNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
    }
    // 服务端网络时延均值
    if (StringUtils.equals(field, "tcp_server_network_latency_avg")) {
      aggFields.put("tcpServerNetworkLatency",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency"));
      aggFields.put("tcpServerNetworkLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_network_latency_counts"));
    }
    // 服务端响应总时延
    if (StringUtils.equals(field, "server_response_latency")) {
      aggFields.put("serverResponseLatency",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency"));
    }
    // 服务端响应时延均值
    if (StringUtils.equals(field, "server_response_latency_avg")) {
      aggFields.put("serverResponseLatency",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency"));
      aggFields.put("serverResponseLatencyCounts",
          Tuples.of(AggsFunctionEnum.SUM, "server_response_latency_counts"));
    }
    // TCP客户端总包数
    if (StringUtils.equals(field, "tcp_client_packets")) {
      aggFields.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
    }
    // TCP客户端重传包数
    if (StringUtils.equals(field, "tcp_client_retransmission_packets")) {
      aggFields.put("tcpClientRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
    }
    // TCP客户端重传率
    if (StringUtils.equals(field, "tcp_client_retransmission_rate")) {
      aggFields.put("tcpClientRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_retransmission_packets"));
      aggFields.put("tcpClientPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_client_packets"));
    }
    // TCP服务端总包数
    if (StringUtils.equals(field, "tcp_server_packets")) {
      aggFields.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
    }
    // TCP服务端重传包数
    if (StringUtils.equals(field, "tcp_server_retransmission_packets")) {
      aggFields.put("tcpServerRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
    }
    // TCP服务端重传率
    if (StringUtils.equals(field, "tcp_server_retransmission_rate")) {
      aggFields.put("tcpServerRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_retransmission_packets"));
      aggFields.put("tcpServerPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_server_packets"));
    }
    // TCP零窗口包数
    if (StringUtils.equals(field, "tcp_zero_window_packets")) {
      aggFields.put("tcpZeroWindowPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_zero_window_packets"));
    }
    // TCP客户端零窗口包数
    if (StringUtils.equals(field, "tcp_client_zero_window_packets")) {
      aggFields.put("tcpClientZeroWindowsPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_zero_window_packets"));
    }
    // TCP服务端零窗口包数
    if (StringUtils.equals(field, "tcp_server_zero_window_packets")) {
      aggFields.put("tcpServerZeroWindowsPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_zero_window_packets"));
    }
    // TCP建连成功数
    if (StringUtils.equals(field, "tcp_established_success_counts")) {
      aggFields.put("tcpEstablishedSuccessCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_success_counts"));
    }
    // TCP建连失败数
    if (StringUtils.equals(field, "tcp_established_fail_counts")) {
      aggFields.put("tcpEstablishedFailCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_counts"));
    }
  }

  /**
   * 流日志特殊字段聚合方式
   * @param specialAggFields
   * @param field
   * @return
   */
  protected void specialAggregateFields(
      Map<String, Tuple2<AggsFunctionEnum, String>> specialAggFields, String field) {
    // 客户端网络时延均值
    if (StringUtils.equals(field, "tcp_client_network_latency_avg")) {
      specialAggFields.put("tcpClientNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientNetworkLatency, tcpClientNetworkLatencyCounts"));
    }
    // 服务端网络时延均值
    if (StringUtils.equals(field, "tcp_server_network_latency_avg")) {
      specialAggFields.put("tcpServerNetworkLatencyAvg", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerNetworkLatency, tcpServerNetworkLatencyCounts"));
    }
    // 服务端响应时延均值
    if (StringUtils.equals(field, "server_response_latency_avg")) {
      specialAggFields.put("serverResponseLatencyAvg",
          Tuples.of(AggsFunctionEnum.DIVIDE, "serverResponseLatency, serverResponseLatencyCounts"));
    }
    // 客户端重传率
    if (StringUtils.equals(field, "tcp_client_retransmission_rate")) {
      specialAggFields.put("tcpClientRetransmissionRate",
          Tuples.of(AggsFunctionEnum.DIVIDE, "tcpClientRetransmissionPackets, tcpClientPackets"));
    }
    // 服务端重传率
    if (StringUtils.equals(field, "tcp_server_retransmission_rate")) {
      specialAggFields.put("tcpServerRetransmissionRate",
          Tuples.of(AggsFunctionEnum.DIVIDE, "tcpServerRetransmissionPackets, tcpServerPackets"));
    }
  }

  /**
   * 个别特殊计算字段处理
   * @param metricResult
   * @return
   */
  protected List<Map<String, Object>> metricDataConversion(List<Map<String, Object>> metricResult) {
    if (CollectionUtils.isEmpty(metricResult)) {
      return metricResult;
    }

    metricResult = metricResult.stream().map(map -> map.entrySet().parallelStream()
        .<HashMap<String, Object>>collect(HashMap::new, (m, n) -> m.put(n.getKey(),
            metricFields.containsKey(n.getKey()) ? (StringUtils
                .equalsAny(String.valueOf(n.getValue()), DIVIDE_NULL_NAN, DIVIDE_NULL_INF)
                    ? 0
                    : new BigDecimal(String.valueOf(n.getValue())).setScale(SCALE_COUNTS,
                        RoundingMode.HALF_UP))
                : n.getValue()),
            HashMap::putAll))
        .collect(Collectors.toList());
    return metricResult;
  }

  protected void sortMetricResult(List<Map<String, Object>> result, String sortProperty,
      String sortDirection) {
    if (CollectionUtils.isEmpty(result)) {
      return;
    }

    result.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        Long o1Value = MapUtils.getLongValue(o1, TextUtils.underLineToCamel(sortProperty), 0);
        Long o2Value = MapUtils.getLongValue(o2, TextUtils.underLineToCamel(sortProperty), 0);

        return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
            ? o1Value.compareTo(o2Value)
            : o2Value.compareTo(o1Value);
      }
    });
  }

  protected void enrichWhereSql(MetricQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where 1 = 1 ");

    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> dsl = getSpl2SqlHelper().converte(queryVO.getDsl(), false,
                queryVO.getTimePrecision(), queryVO.getIncludeStartTime(),
                queryVO.getIncludeEndTime());
        whereSql.append(" and ");
        whereSql.append(dsl.getT1());
        params.putAll(dsl.getT2());
        return;
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    // 网络
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and network_id = :networkId ");
      params.put("networkId", queryVO.getNetworkId());
    }
    // 业务
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      whereSql.append(" and service_id = :serviceId ");
      params.put("serviceId", queryVO.getServiceId());
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

  /**
   * 增加额外过滤条件，格式：[{"W":xx},{"A":x1, "B":y1},{"A":x2, "B":y2}]，</br>
   * 单个过滤组合内条件用 "and" 拼接，</br>
   * 过滤组合相同的项将通过 "or" 连接，作为一整项过滤，</br>
   * 过滤组合不同的项用 "and" 连接；</br>
   * 最终过滤语句如下：where xxx and W = xx and ((A = x1 and B = y1) or (A = x2 and B = y2))
   * @param additionalConditions
   * @param whereSql
   * @param params
   */
  protected void fillAdditionalConditions(List<Map<String, Object>> additionalConditions,
      StringBuilder whereSql, Map<String, Object> params) {
    // 添加附加条件
    if (CollectionUtils.isNotEmpty(additionalConditions)) {
      Map<String,
          List<String>> conditions = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      int index = 0;
      for (Map<String, Object> condition : additionalConditions) {
        StringBuilder conditionSql = new StringBuilder("(");
        Iterator<Entry<String, Object>> iterator = condition.entrySet().iterator();
        while (iterator.hasNext()) {
          Entry<String, Object> entry = iterator.next();
          if (entry.getValue() == null) {
            conditionSql.append("isNull(").append(entry.getKey()).append(")");
          } else {
            conditionSql.append(entry.getKey()).append(" = :").append(entry.getKey()).append(index);
            params.put(entry.getKey() + index, entry.getValue());
          }

          if (iterator.hasNext()) {
            conditionSql.append(" and ");
          }
        }
        conditionSql.append(")");
        String conditionKey = StringUtils.join(condition.keySet(), "_");
        List<String> list = conditions.getOrDefault(conditionKey,
            Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
        list.add(conditionSql.toString());
        conditions.put(conditionKey, list);

        index++;
      }

      conditions.values().forEach(conditionSqlList -> {
        whereSql.append(" and (").append(StringUtils.join(conditionSqlList, " or ")).append(")");
      });
    }
  }

  /**
   * 根据时间间隔获取表名
   * @param sourceType
   * @param interval
   * @param packetFileId
   * @return
   */
  protected String convertTableName(String sourceType, int interval, String packetFileId) {
    if (StringUtils.equals(sourceType, FpcConstants.SOURCE_TYPE_PACKET_FILE)) {
      PacketAnalysisSubTaskDO offlineAnalysisSubTask = getOfflineAnalysisSubTaskDao()
          .queryPacketAnalysisSubTask(packetFileId);
      if (StringUtils.isBlank(offlineAnalysisSubTask.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "未查询到离线分析子任务");
      }

      String packetFileTableName = String.join("_", getTableName(),
          offlineAnalysisSubTask.getTaskId());

      // 判断离线数据包文件的分析结果是否存在
      List<Map<String, Object>> existResult = getClickHouseJdbcTemplate().getJdbcTemplate()
          .query(String.format("show tables from %s where name = '%s'",
              ManagerConstants.FPC_DATABASE, packetFileTableName), new ColumnMapRowMapper());
      if (CollectionUtils.isEmpty(existResult)) {
        LOGGER.warn("search failed, offline packet file metric data not found: {}",
            packetFileTableName);
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "离线分析结果异常，未查找到流量分析数据");
      }

      return packetFileTableName;
    }

    String tableName = getTableName();
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      tableName = getTableName() + "_5m";
    } else if (interval >= Constants.ONE_HOUR_SECONDS) {
      tableName = getTableName() + "_1h";
    }

    return tableName;
  }

  /**
   * 获取计算关键字段
   * @param aggsField
   * @return
   */
  protected List<String> getCombinationAggsFields(String aggsField) {
    List<String> aggsFields;
    switch (aggsField) {
      case "tcp_client_network_latency_avg":
        aggsFields = Lists.newArrayList("tcpClientNetworkLatency", "tcpClientNetworkLatencyCounts",
            "tcpClientNetworkLatencyAvg");
        break;
      case "tcp_server_network_latency_avg":
        aggsFields = Lists.newArrayList("tcpServerNetworkLatency", "tcpServerNetworkLatencyCounts",
            "tcpServerNetworkLatencyAvg");
        break;
      // 性能接口中上周同期响应时间新增字段查询
      case "server_response_latency_avg_inside_service":
        aggsFields = Lists.newArrayList("serverResponseLatencyInsideService",
            "serverResponseLatencyCountsInsideService", "serverResponseLatencyAvgInsideService");
        break;
      case "server_response_latency_avg_outside_service":
        aggsFields = Lists.newArrayList("serverResponseLatencyOutsideService",
            "serverResponseLatencyCountsOutsideService", "serverResponseLatencyAvgOutsideService");
        break;
      case "server_response_latency_avg":
        aggsFields = Lists.newArrayList("serverResponseLatency", "serverResponseLatencyCounts",
            "serverResponseLatencyAvg");
        break;

      case "tcp_client_retransmission_rate":
        aggsFields = Lists.newArrayList("tcpClientRetransmissionPackets", "tcpClientPackets",
            "tcpClientRetransmissionRate");
        break;
      case "tcp_server_retransmission_rate":
        aggsFields = Lists.newArrayList("tcpServerRetransmissionPackets", "tcpServerPackets",
            "tcpServerRetransmissionRate");
        break;
      case "tcp_established_fail_rate":
        aggsFields = Lists.newArrayList("tcpEstablishedFailCounts", "tcpEstablishedSuccessCounts",
            "tcpEstablishedCounts", "tcpEstablishedFailRate");
        break;


      case "tcp_server_established_fail_counts_rate":
        aggsFields = Lists.newArrayList("tcpServerEstablishedFailCountsRate",
            "tcpServerEstablishedFailCounts", "tcpServerEstablishedCounts");
        break;

      case "tcp_client_established_fail_counts_rate":
        aggsFields = Lists.newArrayList("tcpClientEstablishedFailCountsRate",
            "tcpClientEstablishedFailCounts", "tcpClientEstablishedCounts");
        break;

      case "tcp_server_recv_retransmission_packets_rate":
        aggsFields = Lists.newArrayList("tcpServerRecvRetransmissionPacketsRate",
            "tcpServerRecvRetransmissionPackets", "tcpServerRecvPackets");
        break;

      case "tcp_server_send_retransmission_packets_rate":
        aggsFields = Lists.newArrayList("tcpServerSendRetransmissionPacketsRate",
            "tcpServerSendRetransmissionPackets", "tcpServerSendPackets");
        break;

      case "tcp_client_recv_retransmission_packets_rate":
        aggsFields = Lists.newArrayList("tcpClientRecvRetransmissionPacketsRate",
            "tcpClientRecvRetransmissionPackets", "tcpClientRecvPackets");
        break;

      case "tcp_client_send_retransmission_packets_rate":
        aggsFields = Lists.newArrayList("tcpClientSendRetransmissionPacketsRate",
            "tcpClientSendRetransmissionPackets", "tcpClientSendPackets");
        break;

      case "tcp_server_established_fail_counts_inside_service_rate":
        aggsFields = Lists.newArrayList("tcpServerEstablishedFailCountsInsideServiceRate",
            "tcpServerEstablishedFailCountsInsideService",
            "tcpServerEstablishedCountsInsideService");
        break;

      case "tcp_client_established_fail_counts_inside_service_rate":
        aggsFields = Lists.newArrayList("tcpClientEstablishedFailCountsInsideServiceRate",
            "tcpClientEstablishedFailCountsInsideService",
            "tcpClientEstablishedCountsInsideService");
        break;

      case "tcp_server_recv_retransmission_packets_inside_service_rate":
        aggsFields = Lists.newArrayList("tcpServerRecvRetransmissionPacketsInsideServiceRate",
            "tcpServerRecvRetransmissionPacketsInsideService", "tcpServerRecvPacketsInsideService");
        break;

      case "tcp_server_send_retransmission_packets_inside_service_rate":
        aggsFields = Lists.newArrayList("tcpServerSendRetransmissionPacketsInsideServiceRate",
            "tcpServerSendRetransmissionPacketsInsideService", "tcpServerSendPacketsInsideService");
        break;

      case "tcp_client_recv_retransmission_packets_inside_service_rate":
        aggsFields = Lists.newArrayList("tcpClientRecvRetransmissionPacketsInsideServiceRate",
            "tcpClientRecvRetransmissionPacketsInsideService", "tcpClientRecvPacketsInsideService");
        break;

      case "tcp_client_send_retransmission_packets_inside_service_rate":
        aggsFields = Lists.newArrayList("tcpClientSendRetransmissionPacketsInsideServiceRate",
            "tcpClientSendRetransmissionPacketsInsideService", "tcpClientSendPacketsInsideService");
        break;


      case "tcp_server_established_fail_counts_outside_service_rate":
        aggsFields = Lists.newArrayList("tcpServerEstablishedFailCountsOutsideServiceRate",
            "tcpServerEstablishedFailCountsOutsideService",
            "tcpServerEstablishedCountsOutsideService");
        break;

      case "tcp_client_established_fail_counts_outside_service_rate":
        aggsFields = Lists.newArrayList("tcpClientEstablishedFailCountsOutsideServiceRate",
            "tcpClientEstablishedFailCountsOutsideService",
            "tcpClientEstablishedCountsOutsideService");
        break;

      case "tcp_server_recv_retransmission_packets_outside_service_rate":
        aggsFields = Lists.newArrayList("tcpServerRecvRetransmissionPacketsOutsideServiceRate",
            "tcpServerRecvRetransmissionPacketsOutsideService",
            "tcpServerRecvPacketsOutsideService");
        break;

      case "tcp_server_send_retransmission_packets_outside_service_rate":
        aggsFields = Lists.newArrayList("tcpServerSendRetransmissionPacketsOutsideServiceRate",
            "tcpServerSendRetransmissionPacketsOutsideService",
            "tcpServerSendPacketsOutsideService");
        break;

      case "tcp_client_recv_retransmission_packets_outside_service_rate":
        aggsFields = Lists.newArrayList("tcpClientRecvRetransmissionPacketsOutsideServiceRate",
            "tcpClientRecvRetransmissionPacketsOutsideService",
            "tcpClientRecvPacketsOutsideService");
        break;

      case "tcp_client_send_retransmission_packets_outside_service_rate":
        aggsFields = Lists.newArrayList("tcpClientSendRetransmissionPacketsOutsideServiceRate",
            "tcpClientSendRetransmissionPacketsOutsideService",
            "tcpClientSendPacketsOutsideService");
        break;

      // TCP
      case "tcp_client_retransmission_rate_inside_service":
        aggsFields = Lists.newArrayList("tcpClientRetransmissionPacketsInsideService",
            "tcpClientPacketsInsideService", "tcpClientRetransmissionRateInsideService");
        break;

      case "tcp_server_retransmission_rate_inside_service":
        aggsFields = Lists.newArrayList("tcpServerRetransmissionPacketsInsideService",
            "tcpServerPacketsInsideService", "tcpServerRetransmissionRateInsideService");
        break;

      case "tcp_client_retransmission_rate_outside_service":
        aggsFields = Lists.newArrayList("tcpClientRetransmissionPacketsOutsideService",
            "tcpClientPacketsOutsideService", "tcpClientRetransmissionRateOutsideService");
        break;

      case "tcp_server_retransmission_rate_outside_service":
        aggsFields = Lists.newArrayList("tcpServerRetransmissionPacketsOutsideService",
            "tcpServerPacketsOutsideService", "tcpServerRetransmissionRateOutsideService");
        break;

      default:
        aggsFields = Lists.newArrayList(TextUtils.underLineToCamel(aggsField));
        break;
    }
    return aggsFields;
  }

  protected void tranKPIMapToDateRecord(Map<String, Object> item,
      AbstractDataRecordDO abstractDataRecordDO) {
    if (item.get("timestamp") != null) {
      OffsetDateTime timestamp = (OffsetDateTime) item.get("timestamp");
      abstractDataRecordDO.setTimestamp(Date.from(timestamp.toInstant()));
    }
    abstractDataRecordDO.setNetworkId(MapUtils.getString(item, "networkId", null));
    String serviceId = MapUtils.getString(item, "serviceId", null);
    abstractDataRecordDO.setServiceId(StringUtils.equals(serviceId, "null") ? null : serviceId);
    abstractDataRecordDO.setTotalBytes(MapUtils.getLongValue(item, "totalBytes"));
    abstractDataRecordDO.setTotalPackets(MapUtils.getLongValue(item, "totalPackets"));
    abstractDataRecordDO.setEstablishedSessions(MapUtils.getLongValue(item, "establishedSessions"));
    abstractDataRecordDO
        .setTcpClientNetworkLatency(MapUtils.getLongValue(item, "tcpClientNetworkLatency"));
    abstractDataRecordDO.setTcpClientNetworkLatencyCounts(
        MapUtils.getLongValue(item, "tcpClientNetworkLatencyCounts"));
    abstractDataRecordDO
        .setTcpClientNetworkLatencyAvg(MapUtils.getDoubleValue(item, "tcpClientNetworkLatencyAvg"));
    abstractDataRecordDO
        .setTcpServerNetworkLatency(MapUtils.getLongValue(item, "tcpServerNetworkLatency"));
    abstractDataRecordDO.setTcpServerNetworkLatencyCounts(
        MapUtils.getLongValue(item, "tcpServerNetworkLatencyCounts"));
    abstractDataRecordDO
        .setTcpServerNetworkLatencyAvg(MapUtils.getDoubleValue(item, "tcpServerNetworkLatencyAvg"));
    abstractDataRecordDO
        .setServerResponseLatency(MapUtils.getLongValue(item, "serverResponseLatency"));
    abstractDataRecordDO
        .setServerResponseLatencyCounts(MapUtils.getLongValue(item, "serverResponseLatencyCounts"));
    abstractDataRecordDO
        .setServerResponseLatencyAvg(MapUtils.getDoubleValue(item, "serverResponseLatencyAvg"));
    abstractDataRecordDO.setTcpClientRetransmissionPackets(
        MapUtils.getLongValue(item, "tcpClientRetransmissionPackets"));
    abstractDataRecordDO.setTcpClientPackets(MapUtils.getLongValue(item, "tcpClientPackets"));
    abstractDataRecordDO.setTcpClientRetransmissionRate(
        MapUtils.getDoubleValue(item, "tcpClientRetransmissionRate"));
    abstractDataRecordDO.setTcpServerRetransmissionPackets(
        MapUtils.getLongValue(item, "tcpServerRetransmissionPackets"));
    abstractDataRecordDO.setTcpServerPackets(MapUtils.getLongValue(item, "tcpServerPackets"));
    abstractDataRecordDO.setTcpServerRetransmissionRate(
        MapUtils.getDoubleValue(item, "tcpServerRetransmissionRate"));
    abstractDataRecordDO
        .setTcpClientZeroWindowPackets(MapUtils.getLongValue(item, "tcpClientZeroWindowPackets"));
    abstractDataRecordDO
        .setTcpServerZeroWindowPackets(MapUtils.getLongValue(item, "tcpServerZeroWindowPackets"));
    abstractDataRecordDO
        .setTcpEstablishedFailCounts(MapUtils.getLongValue(item, "tcpEstablishedFailCounts"));
    abstractDataRecordDO
        .setTcpEstablishedSuccessCounts(MapUtils.getLongValue(item, "tcpEstablishedSuccessCounts"));
  }

}
