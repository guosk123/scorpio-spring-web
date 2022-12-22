package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.dao.MetricL3DeviceDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricL3DeviceDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * create at 2021年8月17日, fpc-manager
 */
@Repository
public class MetricL3DeviceDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricL3DeviceDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricL3DeviceDataRecordDaoImpl.class);

  private static final Map<String, String> PRIMARY_TERM_KEY;
  private static final Map<String, String> AGGS_TERM_KEY;

  static {
    PRIMARY_TERM_KEY = new HashMap<String, String>();
    AGGS_TERM_KEY = new HashMap<String, String>();
    PRIMARY_TERM_KEY.put("ip_address", "ipAddress");
    PRIMARY_TERM_KEY.put("ip_locality", "ipLocality");
    AGGS_TERM_KEY.put("network_id", "networkId");
    AGGS_TERM_KEY.put("service_id", "serviceId");
    AGGS_TERM_KEY.put("ip_address", "ipAddress");
    AGGS_TERM_KEY.put("ip_locality", "ipLocality");
    AGGS_TERM_KEY.put("mac_address", "macAddress");
  }

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;

  @Autowired
  private PacketAnalysisSubTaskDao offlineAnalysisSubTaskDao;

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#getSpl2SqlHelper()
   */
  @Override
  protected Spl2SqlHelper getSpl2SqlHelper() {
    return spl2SqlHelper;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseStatsJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#getOfflineAnalysisSubTaskDao()
   */
  @Override
  protected PacketAnalysisSubTaskDao getOfflineAnalysisSubTaskDao() {
    return offlineAnalysisSubTaskDao;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return ManagerConstants.TABLE_METRIC_L3DEVICE_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricL3DeviceDataRecordDao#queryMetricL3DeviceRawdatas(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3DeviceRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      result = queryMetricDataRecord(queryVO);
    } catch (IOException e) {
      LOGGER.warn("failed to query L3Device metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricL3DeviceDataRecordDao#queryMetricL3Devices(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricL3DeviceDataRecordDO> queryMetricL3Devices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<MetricL3DeviceDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 分组字段
    Map<String, String> termKeys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    termKeys.put("ip_address", "ipAddress");
    termKeys.put("ip_locality", "ipLocality");
    try {
      if (spl2SqlHelper.getFilterFields(queryVO.getDsl()).contains("mac_address")) {
        termKeys.put("mac_address", TextUtils.underLineToCamel("mac_address"));
      }
    } catch (V8ScriptExecutionException | IOException e) {
      LOGGER.warn("failed to query l3-device metric.", e);
      return result;
    }

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> metric = Lists.newArrayList("original");
    setAggsFields(aggsFields, metric);
    setSpecialKpiAggs(aggsFields, metric);

    try {
      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, termKeys, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query L3Device metric.", e);
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> queryMetricL3DevicesEstablishedFail(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 分组字段
    Map<String, String> termKeys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    termKeys.put("ip_address", "ipAddress");
    termKeys.put("ip_locality", "ipLocality");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> metric = Lists.newArrayList(queryVO.getServiceType(), "original");
    setAggsFields(allAggsFields, metric);
    setSpecialKpiAggs(allAggsFields, metric);
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (queryVO.getServiceType().equals("totalService")) {
      aggsFields.put("tcpEstablishedFailCounts", allAggsFields.get("tcpEstablishedFailCounts"));

      aggsFields.put("tcpServerEstablishedFailCounts",
          allAggsFields.get("tcpServerEstablishedFailCounts"));

      aggsFields.put("tcpClientEstablishedFailCounts",
          allAggsFields.get("tcpClientEstablishedFailCounts"));

      aggsFields.put("tcpEstablishedCounts", allAggsFields.get("tcpEstablishedCounts"));

      aggsFields.put("tcpServerEstablishedCounts", allAggsFields.get("tcpServerEstablishedCounts"));

      aggsFields.put("tcpClientEstablishedCounts", allAggsFields.get("tcpClientEstablishedCounts"));

      aggsFields.put("tcpEstablishedFailCountsRate",
          allAggsFields.get("tcpEstablishedFailCountsRate"));

      aggsFields.put("tcpServerEstablishedFailCountsRate",
          allAggsFields.get("tcpServerEstablishedFailCountsRate"));

      aggsFields.put("tcpClientEstablishedFailCountsRate",
          allAggsFields.get("tcpClientEstablishedFailCountsRate"));

    } else if (queryVO.getServiceType().equals("intranetService")) {
      aggsFields.put("tcpEstablishedFailCountsInsideService",
          allAggsFields.get("tcpEstablishedFailCountsInsideService"));

      aggsFields.put("tcpServerEstablishedFailCountsInsideService",
          allAggsFields.get("tcpServerEstablishedFailCountsInsideService"));

      aggsFields.put("tcpClientEstablishedFailCountsInsideService",
          allAggsFields.get("tcpClientEstablishedFailCountsInsideService"));

      aggsFields.put("tcpEstablishedCountsInsideService",
          allAggsFields.get("tcpEstablishedCountsInsideService"));

      aggsFields.put("tcpServerEstablishedCountsInsideService",
          allAggsFields.get("tcpServerEstablishedCountsInsideService"));

      aggsFields.put("tcpClientEstablishedCountsInsideService",
          allAggsFields.get("tcpClientEstablishedCountsInsideService"));

      aggsFields.put("tcpEstablishedFailCountsInsideServiceRate",
          allAggsFields.get("tcpEstablishedFailCountsInsideServiceRate"));

      aggsFields.put("tcpServerEstablishedFailCountsInsideServiceRate",
          allAggsFields.get("tcpServerEstablishedFailCountsInsideServiceRate"));

      aggsFields.put("tcpClientEstablishedFailCountsInsideServiceRate",
          allAggsFields.get("tcpClientEstablishedFailCountsInsideServiceRate"));
    } else {
      aggsFields.put("tcpEstablishedFailCountsOutsideService",
          allAggsFields.get("tcpEstablishedFailCountsOutsideService"));

      aggsFields.put("tcpServerEstablishedFailCountsOutsideService",
          allAggsFields.get("tcpServerEstablishedFailCountsOutsideService"));

      aggsFields.put("tcpClientEstablishedFailCountsOutsideService",
          allAggsFields.get("tcpClientEstablishedFailCountsOutsideService"));

      aggsFields.put("tcpEstablishedCountsOutsideService",
          allAggsFields.get("tcpEstablishedCountsOutsideService"));

      aggsFields.put("tcpServerEstablishedCountsOutsideService",
          allAggsFields.get("tcpServerEstablishedCountsOutsideService"));

      aggsFields.put("tcpClientEstablishedCountsOutsideService",
          allAggsFields.get("tcpClientEstablishedCountsOutsideService"));

      aggsFields.put("tcpEstablishedFailCountsOutsideServiceRate",
          allAggsFields.get("tcpEstablishedFailCountsOutsideServiceRate"));

      aggsFields.put("tcpServerEstablishedFailCountsOutsideServiceRate",
          allAggsFields.get("tcpServerEstablishedFailCountsOutsideServiceRate"));

      aggsFields.put("tcpClientEstablishedFailCountsOutsideServiceRate",
          allAggsFields.get("tcpClientEstablishedFailCountsOutsideServiceRate"));
    }
    try {
      String tableName = convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
          queryVO.getPacketFileId());
      String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
          ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
          : "";

      // 构造过滤条件
      StringBuilder whereSql = new StringBuilder();
      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      enrichWhereSql(queryVO, whereSql, params);

      // 构造查询语句
      StringBuilder sql = new StringBuilder(securityQueryId);
      // 分组字段
      List<String> terms = termKeys.entrySet().stream()
          .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
      sql.append(" select ").append(StringUtils.join(terms, ","));
      // 聚合字段
      aggsFields.entrySet().forEach(entry -> {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      });
      sql.append(" from ").append(tableName);
      sql.append(whereSql);

      sql.append(" group by ").append(StringUtils.join(termKeys.values(), ","));
      if (queryVO.getServiceType().equals("totalService")) {
        sql.append(" having tcpEstablishedFailCounts > 0 ");
      } else if (queryVO.getServiceType().equals("intranetService")) {
        sql.append(" having tcpEstablishedFailCountsInsideService > 0 ");
      } else {
        sql.append(" having tcpEstablishedFailCountsOutsideService > 0 ");
      }
      sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
          .append(sortDirection);
      if (queryVO.getCount() > 0) {
        sql.append(" limit ").append(queryVO.getCount());
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("termMetricAggregate sql : [{}], param: [{}] ", sql.toString(), params);
      }

      result = metricDataConversion(
          queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
      sortMetricResult(result, sortProperty, sortDirection);
    } catch (Exception e) {
      LOGGER.warn("failed to query L3Device metric.", e);
    }

    return result;
  }


  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricL3DeviceDataRecordDao#countMetricL3Devices(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> countMetricL3Devices(MetricQueryVO queryVO, String aggsField,
      String sortProperty, String sortDirection, List<String> networkIds) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 分组字段
    Map<String, String> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    termKey.put("ip_address", "ipAddress");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> metric = Lists.newArrayList(queryVO.getServiceType(), "original");
    setAggsFields(allAggsFields, metric);
    setSpecialKpiAggs(allAggsFields, metric);
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    getCombinationAggsFields(aggsField).forEach(item -> {
      if (allAggsFields.containsKey(item)) {
        aggsFields.put(item, allAggsFields.get(item));
      }
    });
    aggsFields.put("upstreamBytes", allAggsFields.get("upstreamBytes"));
    aggsFields.put("downstreamBytes", allAggsFields.get("downstreamBytes"));

    // 附加过滤条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(networkIds)) {
      for (String item : networkIds) {
        Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        itemMap.put("network_id", item);
        itemMap.put("service_id", "");
        combinationConditions.add(itemMap);
      }
    } else {
      Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      String networkId = StringUtils.equals(queryVO.getSourceType(),
          FpcConstants.SOURCE_TYPE_PACKET_FILE) ? queryVO.getPacketFileId()
              : queryVO.getNetworkId();
      itemMap.put("network_id", networkId);
      itemMap.put("service_id", StringUtils.defaultIfBlank(queryVO.getServiceId(), ""));
      combinationConditions.add(itemMap);
    }

    try {
      // 标识查询，用于取消查询
      String tableName = convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
          queryVO.getPacketFileId());
      String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
          ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
          : "";

      // 构造过滤条件
      StringBuilder whereSql = new StringBuilder();
      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      enrichWhereSql(queryVO, whereSql, params);
      // 添加附加条件
      fillAdditionalConditions(combinationConditions, whereSql, params);

      // 构造查询语句
      StringBuilder sql = new StringBuilder(securityQueryId);
      // 分组字段
      List<String> terms = termKey.entrySet().stream()
          .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
      sql.append(" select ").append(StringUtils.join(terms, ","));
      // 聚合字段
      Map<String, Tuple2<AggsFunctionEnum, String>> finalAggsFields = aggsFields;
      finalAggsFields.entrySet().forEach(entry -> {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      });
      sql.append(" from ").append(tableName);
      sql.append(whereSql);
      sql.append(" group by ").append(StringUtils.join(termKey.values(), ","));
      if (sortProperty.contains("rate")) {
        int len = sortProperty.length();
        String s = sortProperty.substring(0, len - 5);
        sql.append(" having ");
        sql.append(TextUtils.underLineToCamel(s)).append(" >0 ");
      } else {
        sql.append(" having ");
        sql.append(TextUtils.underLineToCamel(sortProperty)).append(" >0 ");
      }
      sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
          .append(sortDirection);
      if (queryVO.getCount() > 0) {
        sql.append(" limit ").append(queryVO.getCount());
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("termMetricAggregate sql : [{}], param: [{}] ", sql.toString(), params);
      }
      result = metricDataConversion(
          queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper()));
      sortMetricResult(result, sortProperty, sortDirection);
    } catch (Exception e) {
      LOGGER.warn("failed to count all L3Device metric.", e);
    }

    return result;
  }


  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricL3DeviceDataRecordDao#queryMetricL3DeviceHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3DeviceHistograms(MetricQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> metric = Lists.newArrayList("original");
    setAggsFields(allAggsFields, metric);
    setSpecialKpiAggs(allAggsFields, metric);
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    getCombinationAggsFields(aggsField).forEach(item -> {
      if (allAggsFields.containsKey(item)) {
        aggsFields.put(item, allAggsFields.get(item));
      }
    });
    if (MapUtils.isEmpty(aggsFields)) {
      return result;
    }

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, PRIMARY_TERM_KEY, aggsFields);
      result.addAll(batchResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query L3Device histograms.", e);
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationL3Devices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(" select network_id as networkId ");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    kpiAggs(aggsFields);
    if (!StringUtils.equals(queryVO.getColumns(), "*")) {
      // 过滤要查询的列
      Set<String> fields = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      CsvUtils.convertCSVToList(queryVO.getColumns()).forEach(
          field -> fields.addAll(getCombinationAggsFields(TextUtils.camelToUnderLine(field))));
      fields.addAll(getCombinationAggsFields(sortProperty));

      aggsFields = aggsFields.entrySet().stream()
          .filter(aggsField -> fields.contains(aggsField.getKey()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    aggsFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
        queryVO.getPacketFileId()));
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    List<String> networkIdList = CsvUtils.convertCSVToList(queryVO.getNetworkId());
    if (CollectionUtils.isNotEmpty(networkIdList)) {
      whereSql.append(" and network_id in (:networkIds)");
      params.put("networkIds", networkIdList);
    }
    sql.append(whereSql);
    sql.append(" group by networkId ");
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    sortMetricResult(result, sortProperty, sortDirection);

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#aggregate(java.util.Date, java.util.Date, int, java.lang.String, java.lang.String)
   */
  @Override
  protected int aggregate(Date startTime, Date endTime, int interval, String inputTableName,
      String outputTableName) throws IOException {
    MetricQueryVO queryVO = new MetricQueryVO();
    queryVO.setStartTimeDate(startTime);
    queryVO.setEndTimeDate(endTime);
    queryVO.setInterval(interval);

    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> metric = Lists.newArrayList("all");
    setAggsFields(aggsFields, metric);

    int totalSize = 0;
    int offset = 0;
    int currentSize = 0;
    List<Map<String, Object>> batchList = null;
    do {
      batchList = termMetricAggregate(inputTableName, queryVO, null, AGGS_TERM_KEY, aggsFields,
          DEFAULT_SORT_FIELD, DEFAULT_SORT_DIRECTION, COMPOSITE_BATCH_SIZE, offset);
      batchList.forEach(item -> item.put("timestamp", endTime));
      totalSize += saveMetricDataRecord(batchList, outputTableName);

      currentSize = batchList.size();
      offset += currentSize;
      batchList = null;
    } while (currentSize == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());

    return totalSize;
  }

  private int saveMetricDataRecord(List<Map<String, Object>> batchList, String outputTableName) {
    if (CollectionUtils.isEmpty(batchList)) {
      return 0;
    }

    batchList.forEach(item -> {
      item.put("timestamp", DateUtils.toStringFormat((Date) item.get("timestamp"),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(batchList);
    return Arrays.stream(
        jdbcTemplate.getJdbcTemplate().batchUpdate(batchUpdatesql(outputTableName), batchSource))
        .sum();
  }

  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields,
      List<String> metric) {
    if (metric.contains("original") || metric.contains("all")) {
      kpiAggs(aggsFields);
      aggsFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
      aggsFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
      aggsFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
      aggsFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
      aggsFields.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
      aggsFields.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
      aggsFields.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));
      aggsFields.put("activeEstablishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "active_established_sessions"));
      aggsFields.put("passiveEstablishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "passive_established_sessions"));
      aggsFields.put("tcpZeroWindowPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_zero_window_packets"));
    }
    if (metric.contains("totalService") || metric.contains("all")) {
      // 总体服务-建连失败分析-表中字段
      aggsFields.put("tcpEstablishedFailCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_established_fail_counts"));
      aggsFields.put("tcpServerEstablishedFailCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_established_fail_counts"));
      aggsFields.put("tcpClientEstablishedFailCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_established_fail_counts"));
      aggsFields.put("tcpServerEstablishedCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_established_counts"));
      aggsFields.put("tcpClientEstablishedCounts",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_established_counts"));

      // 总体服务-重传分析-表中字段
      aggsFields.put("tcpClientRecvRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_recv_retransmission_packets"));
      aggsFields.put("tcpClientSendRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_send_retransmission_packets"));
      aggsFields.put("tcpServerRecvRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_recv_retransmission_packets"));
      aggsFields.put("tcpServerSendRetransmissionPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_send_retransmission_packets"));
      aggsFields.put("tcpClientRecvPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_recv_packets"));
      aggsFields.put("tcpClientSendPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_send_packets"));
      aggsFields.put("tcpServerRecvPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_recv_packets"));
      aggsFields.put("tcpServerSendPackets",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_send_packets"));
    }
    if (metric.contains("intranetService") || metric.contains("all")) {
      // 内网服务-建连失败分析-表中字段
      aggsFields.put("tcpServerEstablishedFailCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_established_fail_counts_inside_service"));
      aggsFields.put("tcpClientEstablishedFailCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_established_fail_counts_inside_service"));
      aggsFields.put("tcpServerEstablishedCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_established_counts_inside_service"));
      aggsFields.put("tcpClientEstablishedCountsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_established_counts_inside_service"));

      // 内网服务-重传分析-表中字段
      aggsFields.put("tcpClientRecvRetransmissionPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_recv_retransmission_packets_inside_service"));
      aggsFields.put("tcpClientSendRetransmissionPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_send_retransmission_packets_inside_service"));
      aggsFields.put("tcpServerRecvRetransmissionPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_recv_retransmission_packets_inside_service"));
      aggsFields.put("tcpServerSendRetransmissionPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_send_retransmission_packets_inside_service"));
      aggsFields.put("tcpClientRecvPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_recv_packets_inside_service"));
      aggsFields.put("tcpClientSendPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_send_packets_inside_service"));
      aggsFields.put("tcpServerRecvPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_recv_packets_inside_service"));
      aggsFields.put("tcpServerSendPacketsInsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_send_packets_inside_service"));
    }
    if (metric.contains("internetService") || metric.contains("all")) {
      // 外网服务-建连失败分析-表中字段
      aggsFields.put("tcpServerEstablishedFailCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_established_fail_counts_outside_service"));
      aggsFields.put("tcpClientEstablishedFailCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_established_fail_counts_outside_service"));
      aggsFields.put("tcpServerEstablishedCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_established_counts_outside_service"));
      aggsFields.put("tcpClientEstablishedCountsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_established_counts_outside_service"));

      // 外网服务-重传分析-表中字段
      aggsFields.put("tcpClientRecvRetransmissionPacketsOutsideService", Tuples
          .of(AggsFunctionEnum.SUM, "tcp_client_recv_retransmission_packets_outside_service"));
      aggsFields.put("tcpClientSendRetransmissionPacketsOutsideService", Tuples
          .of(AggsFunctionEnum.SUM, "tcp_client_send_retransmission_packets_outside_service"));
      aggsFields.put("tcpServerRecvRetransmissionPacketsOutsideService", Tuples
          .of(AggsFunctionEnum.SUM, "tcp_server_recv_retransmission_packets_outside_service"));
      aggsFields.put("tcpServerSendRetransmissionPacketsOutsideService", Tuples
          .of(AggsFunctionEnum.SUM, "tcp_server_send_retransmission_packets_outside_service"));
      aggsFields.put("tcpClientRecvPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_recv_packets_outside_service"));
      aggsFields.put("tcpClientSendPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_client_send_packets_outside_service"));
      aggsFields.put("tcpServerRecvPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_recv_packets_outside_service"));
      aggsFields.put("tcpServerSendPacketsOutsideService",
          Tuples.of(AggsFunctionEnum.SUM, "tcp_server_send_packets_outside_service"));
    }

  }

  private void setSpecialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields,
      List<String> metric) {
    if (metric.contains("original")) {
      specialKpiAggs(aggsFields);
    }
    if (metric.contains("totalService")) {
      // 总体服务-建连失败分析-计算字段

      // TCP建连总次数
      aggsFields.put("tcpEstablishedCounts", Tuples.of(AggsFunctionEnum.PLUS,
          "tcpServerEstablishedCounts, tcpClientEstablishedCounts"));
      // TCP建连失败率
      aggsFields.put("tcpEstablishedFailCountsRate",
          Tuples.of(AggsFunctionEnum.DIVIDE, "tcpEstablishedFailCounts, tcpEstablishedCounts"));
      // TCP服务端建连失败率
      aggsFields.put("tcpServerEstablishedFailCountsRate", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerEstablishedFailCounts, tcpServerEstablishedCounts"));
      // TCP客户端建连失败率
      aggsFields.put("tcpClientEstablishedFailCountsRate", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientEstablishedFailCounts, tcpClientEstablishedCounts"));

      // 总体服务-重传分析-计算字段

      // TCP服务端接收重传率
      aggsFields.put("tcpServerRecvRetransmissionPacketsRate", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerRecvRetransmissionPackets, tcpServerRecvPackets"));
      // TCP服务端发送重传率
      aggsFields.put("tcpServerSendRetransmissionPacketsRate", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpServerSendRetransmissionPackets, tcpServerSendPackets"));
      // TCP客户端接收重传率
      aggsFields.put("tcpClientRecvRetransmissionPacketsRate", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientRecvRetransmissionPackets, tcpClientRecvPackets"));
      // TCP客户端发送重传率
      aggsFields.put("tcpClientSendRetransmissionPacketsRate", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpClientSendRetransmissionPackets, tcpClientSendPackets"));
    }
    if (metric.contains("intranetService")) {
      // 内网服务-建连失败分析-计算字段

      // TCP建连失败次数-内网服务
      aggsFields.put("tcpEstablishedFailCountsInsideService", Tuples.of(AggsFunctionEnum.PLUS,
          "tcpServerEstablishedFailCountsInsideService, tcpClientEstablishedFailCountsInsideService"));
      // TCP建连次数-内网服务
      aggsFields.put("tcpEstablishedCountsInsideService", Tuples.of(AggsFunctionEnum.PLUS,
          "tcpServerEstablishedCountsInsideService, tcpClientEstablishedCountsInsideService"));
      // TCP建连失败率-内网服务
      aggsFields.put("tcpEstablishedFailCountsInsideServiceRate", Tuples.of(AggsFunctionEnum.DIVIDE,
          "tcpEstablishedFailCountsInsideService, tcpEstablishedCountsInsideService"));
      // TCP服务端建连失败率-内网服务
      aggsFields.put("tcpServerEstablishedFailCountsInsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpServerEstablishedFailCountsInsideService, tcpServerEstablishedCountsInsideService"));
      // TCP客户端建连失败率-内网服务
      aggsFields.put("tcpClientEstablishedFailCountsInsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpClientEstablishedFailCountsInsideService, tcpClientEstablishedCountsInsideService"));

      // 内网服务-重传分析-计算字段

      // TCP服务端接收重传率-内网服务
      aggsFields.put("tcpServerRecvRetransmissionPacketsInsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpServerRecvRetransmissionPacketsInsideService, tcpServerRecvPacketsInsideService"));
      // TCP服务端发送重传率-内网服务
      aggsFields.put("tcpServerSendRetransmissionPacketsInsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpServerSendRetransmissionPacketsInsideService, tcpServerSendPacketsInsideService"));
      // TCP客户端接收重传率-内网服务
      aggsFields.put("tcpClientRecvRetransmissionPacketsInsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpClientRecvRetransmissionPacketsInsideService, tcpClientRecvPacketsInsideService"));
      // TCP客户端发送重传率-内网服务
      aggsFields.put("tcpClientSendRetransmissionPacketsInsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpClientSendRetransmissionPacketsInsideService, tcpClientSendPacketsInsideService"));
    }
    if (metric.contains("internetService")) {
      // 外网服务-建连失败分析-计算字段

      // TCP建连失败次数-外网服务
      aggsFields.put("tcpEstablishedFailCountsOutsideService", Tuples.of(AggsFunctionEnum.PLUS,
          "tcpServerEstablishedFailCountsOutsideService, tcpClientEstablishedFailCountsOutsideService"));
      // TCP建连次数-外网服务
      aggsFields.put("tcpEstablishedCountsOutsideService", Tuples.of(AggsFunctionEnum.PLUS,
          "tcpServerEstablishedCountsOutsideService, tcpClientEstablishedCountsOutsideService"));
      // TCP建连失败率-外网服务
      aggsFields.put("tcpEstablishedFailCountsOutsideServiceRate",
          Tuples.of(AggsFunctionEnum.DIVIDE,
              "tcpEstablishedFailCountsOutsideService, tcpEstablishedCountsOutsideService"));
      // TCP服务端建连失败率-外网服务
      aggsFields.put("tcpServerEstablishedFailCountsOutsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpServerEstablishedFailCountsOutsideService, tcpServerEstablishedCountsOutsideService"));
      // TCP客户端建连失败率-外网服务
      aggsFields.put("tcpClientEstablishedFailCountsOutsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpClientEstablishedFailCountsOutsideService, tcpClientEstablishedCountsOutsideService"));

      // 外网服务-重传分析-计算字段

      // TCP服务端接收重传率-外网服务
      aggsFields.put("tcpServerRecvRetransmissionPacketsOutsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpServerRecvRetransmissionPacketsOutsideService, tcpServerRecvPacketsOutsideService"));
      // TCP服务端发送重传率-外网服务
      aggsFields.put("tcpServerSendRetransmissionPacketsOutsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpServerSendRetransmissionPacketsOutsideService, tcpServerSendPacketsOutsideService"));
      // TCP客户端接收重传率-外网服务
      aggsFields.put("tcpClientRecvRetransmissionPacketsOutsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpClientRecvRetransmissionPacketsOutsideService, tcpClientRecvPacketsOutsideService"));
      // TCP客户端发送重传率-外网服务
      aggsFields.put("tcpClientSendRetransmissionPacketsOutsideServiceRate", Tuples.of(
          AggsFunctionEnum.DIVIDE,
          "tcpClientSendRetransmissionPacketsOutsideService, tcpClientSendPacketsOutsideService"));
    }
  }


  private MetricL3DeviceDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricL3DeviceDataRecordDO recordDO = new MetricL3DeviceDataRecordDO();
    String IpAddress = MapUtils.getString(item, "ipAddress");
    recordDO.setIpAddress(StringUtils.equals(IpAddress, "null") ? null : IpAddress);
    String IpLocality = MapUtils.getString(item, "ipLocality");
    recordDO.setIpLocality(StringUtils.equals(IpLocality, "null") ? null : IpLocality);
    String MacAddress = MapUtils.getString(item, "macAddress");
    recordDO.setMacAddress(StringUtils.equals(MacAddress, "null") ? null : MacAddress);

    tranKPIMapToDateRecord(item, recordDO);
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstreamBytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstreamPackets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstreamBytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstreamPackets"));
    recordDO.setActiveEstablishedSessions(MapUtils.getLongValue(item, "activeEstablishedSessions"));
    recordDO
        .setPassiveEstablishedSessions(MapUtils.getLongValue(item, "passiveEstablishedSessions"));
    recordDO.setTcpSynPackets(MapUtils.getLongValue(item, "tcpSynPackets"));
    recordDO.setTcpSynRstPackets(MapUtils.getLongValue(item, "tcpSynRstPackets"));
    recordDO.setTcpSynAckPackets(MapUtils.getLongValue(item, "tcpSynAckPackets"));
    recordDO.setTcpZeroWindowPackets(MapUtils.getLongValue(item, "tcpZeroWindowPackets"));

    return recordDO;
  }

  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("ip_address,");
    sql.append("ip_locality,");
    sql.append("mac_address,");
    sql.append("network_id,");
    sql.append("service_id,");

    sql.append("downstream_bytes,");
    sql.append("downstream_packets,");
    sql.append("upstream_bytes,");
    sql.append("upstream_packets,");
    sql.append("total_bytes,");
    sql.append("total_packets,");
    sql.append("established_sessions,");
    sql.append("active_established_sessions,");
    sql.append("passive_established_sessions,");
    sql.append("tcp_syn_packets,");
    sql.append("tcp_syn_ack_packets,");
    sql.append("tcp_syn_rst_packets,");
    sql.append("tcp_client_network_latency,");
    sql.append("tcp_client_network_latency_counts,");
    sql.append("tcp_server_network_latency,");
    sql.append("tcp_server_network_latency_counts,");
    sql.append("server_response_latency,");
    sql.append("server_response_latency_counts,");
    sql.append("tcp_client_retransmission_packets,");
    sql.append("tcp_client_packets,");
    sql.append("tcp_server_retransmission_packets,");
    sql.append("tcp_server_packets,");
    sql.append("tcp_zero_window_packets,");
    sql.append("tcp_client_zero_window_packets,");
    sql.append("tcp_server_zero_window_packets,");
    sql.append("tcp_established_fail_counts,");
    sql.append("tcp_established_success_counts,");
    // 建连失败分析与重传分析区分内外网服务新增字段
    sql.append("tcp_client_established_fail_counts_inside_service,");
    sql.append("tcp_client_established_fail_counts_outside_service,");
    sql.append("tcp_client_established_fail_counts,");
    sql.append("tcp_server_established_fail_counts_inside_service,");
    sql.append("tcp_server_established_fail_counts_outside_service,");
    sql.append("tcp_server_established_fail_counts,");
    sql.append("tcp_client_established_counts_inside_service,");
    sql.append("tcp_client_established_counts_outside_service,");
    sql.append("tcp_client_established_counts,");
    sql.append("tcp_server_established_counts_inside_service,");
    sql.append("tcp_server_established_counts_outside_service,");
    sql.append("tcp_server_established_counts,");
    sql.append("tcp_client_recv_retransmission_packets_inside_service,");
    sql.append("tcp_client_send_retransmission_packets_inside_service,");
    sql.append("tcp_client_recv_retransmission_packets_outside_service,");
    sql.append("tcp_client_send_retransmission_packets_outside_service,");
    sql.append("tcp_client_recv_retransmission_packets,");
    sql.append("tcp_client_send_retransmission_packets,");
    sql.append("tcp_server_recv_retransmission_packets_inside_service,");
    sql.append("tcp_server_send_retransmission_packets_inside_service,");
    sql.append("tcp_server_recv_retransmission_packets_outside_service,");
    sql.append("tcp_server_send_retransmission_packets_outside_service,");
    sql.append("tcp_server_recv_retransmission_packets,");
    sql.append("tcp_server_send_retransmission_packets,");
    sql.append("tcp_client_recv_packets_inside_service,");
    sql.append("tcp_client_send_packets_inside_service,");
    sql.append("tcp_client_recv_packets_outside_service,");
    sql.append("tcp_client_send_packets_outside_service,");
    sql.append("tcp_client_recv_packets,");
    sql.append("tcp_client_send_packets,");
    sql.append("tcp_server_recv_packets_inside_service,");
    sql.append("tcp_server_send_packets_inside_service,");
    sql.append("tcp_server_recv_packets_outside_service,");
    sql.append("tcp_server_send_packets_outside_service,");
    sql.append("tcp_server_recv_packets,");
    sql.append("tcp_server_send_packets,");
    sql.append("timestamp)");

    // values
    sql.append(" values (");
    sql.append(":ipAddress,");
    sql.append(":ipLocality,");
    sql.append(":macAddress,");
    sql.append(":networkId,");
    sql.append(":serviceId,");

    sql.append(":downstreamBytes,");
    sql.append(":downstreamPackets,");
    sql.append(":upstreamBytes,");
    sql.append(":upstreamPackets,");
    sql.append(":totalBytes,");
    sql.append(":totalPackets,");
    sql.append(":establishedSessions,");
    sql.append(":activeEstablishedSessions,");
    sql.append(":passiveEstablishedSessions,");
    sql.append(":tcpSynPackets,");
    sql.append(":tcpSynAckPackets,");
    sql.append(":tcpSynRstPackets,");
    sql.append(":tcpClientNetworkLatency,");
    sql.append(":tcpClientNetworkLatencyCounts,");
    sql.append(":tcpServerNetworkLatency,");
    sql.append(":tcpServerNetworkLatencyCounts,");
    sql.append(":serverResponseLatency,");
    sql.append(":serverResponseLatencyCounts,");
    sql.append(":tcpClientRetransmissionPackets,");
    sql.append(":tcpClientPackets,");
    sql.append(":tcpServerRetransmissionPackets,");
    sql.append(":tcpServerPackets,");
    sql.append(":tcpZeroWindowPackets,");
    sql.append(":tcpClientZeroWindowPackets,");
    sql.append(":tcpServerZeroWindowPackets,");
    sql.append(":tcpEstablishedFailCounts,");
    sql.append(":tcpEstablishedSuccessCounts,");
    // 建连失败分析与重传分析区分内外网服务新增字段
    sql.append(":tcpClientEstablishedFailCountsInsideService,");
    sql.append(":tcpClientEstablishedFailCountsOutsideService,");
    sql.append(":tcpClientEstablishedFailCounts,");
    sql.append(":tcpServerEstablishedFailCountsInsideService,");
    sql.append(":tcpServerEstablishedFailCountsOutsideService,");
    sql.append(":tcpServerEstablishedFailCounts,");
    sql.append(":tcpClientEstablishedCountsInsideService,");
    sql.append(":tcpClientEstablishedCountsOutsideService,");
    sql.append(":tcpClientEstablishedCounts,");
    sql.append(":tcpServerEstablishedCountsInsideService,");
    sql.append(":tcpServerEstablishedCountsOutsideService,");
    sql.append(":tcpServerEstablishedCounts,");
    sql.append(":tcpClientRecvRetransmissionPacketsInsideService,");
    sql.append(":tcpClientSendRetransmissionPacketsInsideService,");
    sql.append(":tcpClientRecvRetransmissionPacketsOutsideService,");
    sql.append(":tcpClientSendRetransmissionPacketsOutsideService,");
    sql.append(":tcpClientRecvRetransmissionPackets,");
    sql.append(":tcpClientSendRetransmissionPackets,");
    sql.append(":tcpServerRecvRetransmissionPacketsInsideService,");
    sql.append(":tcpServerSendRetransmissionPacketsInsideService,");
    sql.append(":tcpServerRecvRetransmissionPacketsOutsideService,");
    sql.append(":tcpServerSendRetransmissionPacketsOutsideService,");
    sql.append(":tcpServerRecvRetransmissionPackets,");
    sql.append(":tcpServerSendRetransmissionPackets,");
    sql.append(":tcpClientRecvPacketsInsideService,");
    sql.append(":tcpClientSendPacketsInsideService,");
    sql.append(":tcpClientRecvPacketsOutsideService,");
    sql.append(":tcpClientSendPacketsOutsideService,");
    sql.append(":tcpClientRecvPackets,");
    sql.append(":tcpClientSendPackets,");
    sql.append(":tcpServerRecvPacketsInsideService,");
    sql.append(":tcpServerSendPacketsInsideService,");
    sql.append(":tcpServerRecvPacketsOutsideService,");
    sql.append(":tcpServerSendPacketsOutsideService,");
    sql.append(":tcpServerRecvPackets,");
    sql.append(":tcpServerSendPackets,");
    sql.append(":timestamp)");

    return sql.toString();
  }
}
