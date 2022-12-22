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
import com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricApplicationDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * create at 2021年8月12日, fpc-manager
 */
@Repository
public class MetricApplicationDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricApplicationDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricApplicationDataRecordDaoImpl.class);

  private static final String CATEGORY_TERM_FIELD = "category_id";
  private static final String SUBCATEGORY_TERM_FIELD = "subcategory_id";
  private static final String APPLICATION_TERM_FIELD = "application_id";

  // 查询基础分组字段
  private static final Map<String, String> PRIMARY_TERM_KEY;
  private static final Map<String, String> AGGS_TERM_KEY;
  static {
    PRIMARY_TERM_KEY = new HashMap<String, String>();
    PRIMARY_TERM_KEY.put("application_id", "applicationId");

    AGGS_TERM_KEY = new HashMap<String, String>();
    AGGS_TERM_KEY.put("network_id", "networkId");
    AGGS_TERM_KEY.put("service_id", "serviceId");
    AGGS_TERM_KEY.put(CATEGORY_TERM_FIELD, "categoryId");
    AGGS_TERM_KEY.put(SUBCATEGORY_TERM_FIELD, "subcategoryId");
    AGGS_TERM_KEY.put(APPLICATION_TERM_FIELD, "applicationId");
    AGGS_TERM_KEY.put("type", "type");
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
    return ManagerConstants.TABLE_METRIC_APP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao#queryMetricApplicationRawdatas(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplicationRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      result = queryMetricDataRecord(queryVO);
    } catch (IOException e) {
      LOGGER.warn("failed to query application metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao#queryMetricApplications(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<MetricApplicationDataRecordDO> queryMetricApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, int type) {
    List<MetricApplicationDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      // 分组字段
      Map<String, String> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      switch (type) {
        // 分类
        case FpcConstants.METRIC_TYPE_APPLICATION_CATEGORY:
          termKey.put(CATEGORY_TERM_FIELD, "categoryId");
          break;
        // 子分类
        case FpcConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
          termKey.put(SUBCATEGORY_TERM_FIELD, "subcategoryId");
          break;
        // 应用
        case FpcConstants.METRIC_TYPE_APPLICATION_APP:
          termKey.put(APPLICATION_TERM_FIELD, "applicationId");
          break;
        default:
          return result;
      }

      // 聚合字段
      Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      setAggsFields(aggsFields);
      setSpecialKpiAggs(aggsFields);

      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, termKey, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query application metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao#countMetricApplications(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> countMetricApplications(MetricQueryVO queryVO, String aggsField,
      String sortProperty, String sortDirection, List<String> networkIds) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(allAggsFields);
    setSpecialKpiAggs(allAggsFields);
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

    // 添加附加条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Map<String, Object> type = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    type.put("type", FpcConstants.METRIC_TYPE_APPLICATION_APP);
    combinationConditions.add(type);

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
      result = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, PRIMARY_TERM_KEY, aggsFields, sortProperty,
          sortDirection);
    } catch (IOException e) {
      LOGGER.warn("failed to count all application metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao#queryMetricApplicationHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplicationHistograms(MetricQueryVO queryVO,
      String termField, String aggsField, List<String> ids) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 分组字段
    Map<String, String> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    termKey.put(termField, TextUtils.underLineToCamel(termField));

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.equals(aggsField, ManagerConstants.METRIC_NPM_ALL_AGGSFILED)) {
      setAggsFields(aggsFields);
      setSpecialKpiAggs(aggsFields);
    } else {
      Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      setAggsFields(allAggsFields);
      setSpecialKpiAggs(allAggsFields);
      getCombinationAggsFields(aggsField).forEach(item -> {
        if (allAggsFields.containsKey(item)) {
          aggsFields.put(item, allAggsFields.get(item));
        }
      });
    }
    if (MapUtils.isEmpty(aggsFields)) {
      return result;
    }

    // 添加附加条件
    List<Map<String, Object>> combinationConditions = ids.stream().map(item -> {
      Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      itemMap.put(termField, item);
      return itemMap;
    }).collect(Collectors.toList());

    try {
      result = dateHistogramTermMetricAggregate(convertTableName(queryVO.getSourceType(),
          queryVO.getInterval(), queryVO.getPacketFileId()), queryVO, combinationConditions,
          termKey, aggsFields);
    } catch (IOException e) {
      LOGGER.warn("failed to query application histograms.", e);
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationApplications(MetricQueryVO queryVO,
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
    setAggsFields(aggsFields);

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

  // 添加普通聚合字段
  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    kpiAggs(aggsFields);
    aggsFields.put("bytepsPeak", Tuples.of(AggsFunctionEnum.MAX, "byteps_peak"));
    aggsFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    aggsFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    aggsFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggsFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    aggsFields.put("totalPayloadBytes", Tuples.of(AggsFunctionEnum.SUM, "total_payload_bytes"));
    aggsFields.put("totalPayloadPackets", Tuples.of(AggsFunctionEnum.SUM, "total_payload_packets"));
    aggsFields.put("downstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_bytes"));
    aggsFields.put("downstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "downstream_payload_packets"));
    aggsFields.put("upstreamPayloadBytes",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_bytes"));
    aggsFields.put("upstreamPayloadPackets",
        Tuples.of(AggsFunctionEnum.SUM, "upstream_payload_packets"));
    aggsFields.put("tcpSynPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_packets"));
    aggsFields.put("tcpSynAckPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_ack_packets"));
    aggsFields.put("tcpSynRstPackets", Tuples.of(AggsFunctionEnum.SUM, "tcp_syn_rst_packets"));
    aggsFields.put("tcpZeroWindowPackets",
        Tuples.of(AggsFunctionEnum.SUM, "tcp_zero_window_packets"));
  }

  // 添加特殊聚合字段
  private void setSpecialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    specialKpiAggs(aggsFields);

    // tcpEstablishedFailRate
    aggsFields.put("tcpEstablishedCounts",
        Tuples.of(AggsFunctionEnum.PLUS, "tcpEstablishedSuccessCounts , tcpEstablishedFailCounts"));
    aggsFields.put("tcpEstablishedFailRate",
        Tuples.of(AggsFunctionEnum.DIVIDE, "tcpEstablishedFailCounts , tcpEstablishedCounts"));
  }

  private MetricApplicationDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricApplicationDataRecordDO recordDO = new MetricApplicationDataRecordDO();
    recordDO.setApplicationId(MapUtils.getIntValue(item, "applicationId"));
    recordDO.setCategoryId(MapUtils.getIntValue(item, "categoryId"));
    recordDO.setSubcategoryId(MapUtils.getIntValue(item, "subcategoryId"));

    tranKPIMapToDateRecord(item, recordDO);
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "bytepsPeak"));
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstreamBytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstreamPackets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstreamBytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstreamPackets"));
    recordDO.setTotalPayloadBytes(MapUtils.getLongValue(item, "totalPayloadBytes"));
    recordDO.setTotalPayloadPackets(MapUtils.getLongValue(item, "totalPayloadPackets"));
    recordDO.setDownstreamPayloadBytes(MapUtils.getLongValue(item, "downstreamPayloadBytes"));
    recordDO.setDownstreamPayloadPackets(MapUtils.getLongValue(item, "downstreamPayloadPackets"));
    recordDO.setUpstreamPayloadBytes(MapUtils.getLongValue(item, "upstreamPayloadBytes"));
    recordDO.setUpstreamPayloadPackets(MapUtils.getLongValue(item, "upstreamPayloadPackets"));
    recordDO.setTcpSynPackets(MapUtils.getLongValue(item, "tcpSynPackets"));
    recordDO.setTcpSynAckPackets(MapUtils.getLongValue(item, "tcpSynAckPackets"));
    recordDO.setTcpSynRstPackets(MapUtils.getLongValue(item, "tcpSynRstPackets"));
    recordDO.setType(MapUtils.getIntValue(item, "type"));
    recordDO.setTcpZeroWindowPackets(MapUtils.getLongValue(item, "tcpZeroWindowPackets"));
    return recordDO;
  }

  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("category_id,");
    sql.append("subcategory_id,");
    sql.append("application_id,");
    sql.append("network_id,");
    sql.append("service_id,");
    sql.append("type,");

    sql.append("byteps_peak,");
    sql.append("downstream_bytes,");
    sql.append("downstream_packets,");
    sql.append("upstream_bytes,");
    sql.append("upstream_packets,");
    sql.append("total_payload_bytes,");
    sql.append("total_payload_packets,");
    sql.append("downstream_payload_bytes,");
    sql.append("downstream_payload_packets,");
    sql.append("upstream_payload_bytes,");
    sql.append("upstream_payload_packets,");
    sql.append("tcp_syn_packets,");
    sql.append("tcp_syn_ack_packets,");
    sql.append("tcp_syn_rst_packets,");
    sql.append("total_bytes,");
    sql.append("total_packets,");
    sql.append("established_sessions,");
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
    sql.append("timestamp)");
    sql.append(" values (");
    sql.append(":categoryId,");
    sql.append(":subcategoryId,");
    sql.append(":applicationId,");
    sql.append(":networkId,");
    sql.append(":serviceId,");
    sql.append(":type,");

    sql.append(":bytepsPeak,");
    sql.append(":downstreamBytes,");
    sql.append(":downstreamPackets,");
    sql.append(":upstreamBytes,");
    sql.append(":upstreamPackets,");
    sql.append(":totalPayloadBytes,");
    sql.append(":totalPayloadPackets,");
    sql.append(":downstreamPayloadBytes,");
    sql.append(":downstreamPayloadPackets,");
    sql.append(":upstreamPayloadBytes,");
    sql.append(":upstreamPayloadPackets,");
    sql.append(":tcpSynPackets,");
    sql.append(":tcpSynAckPackets,");
    sql.append(":tcpSynRstPackets,");
    sql.append(":totalBytes,");
    sql.append(":totalPackets,");
    sql.append(":establishedSessions,");
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
    sql.append(":timestamp)");

    return sql.toString();
  }

}
