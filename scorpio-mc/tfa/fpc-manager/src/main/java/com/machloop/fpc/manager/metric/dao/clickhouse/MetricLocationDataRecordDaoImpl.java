package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.dao.MetricLocationDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricLocationDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 * 
 * create at 2021年8月6日, fpc-manager
 */
@Repository
public class MetricLocationDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricLocationDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricLocationDataRecordDaoImpl.class);
  private static final Map<String, String> PRIMARY_TERM_KEY;
  private static final Map<String, String> AGGS_TERM_KEY;
  static {
    PRIMARY_TERM_KEY = new HashMap<String, String>();
    AGGS_TERM_KEY = new HashMap<String, String>();
    PRIMARY_TERM_KEY.put("country_id", "countryId");
    PRIMARY_TERM_KEY.put("province_id", "provinceId");
    PRIMARY_TERM_KEY.put("city_id", "cityId");
    AGGS_TERM_KEY.put("network_id", "networkId");
    AGGS_TERM_KEY.put("service_id", "serviceId");
    AGGS_TERM_KEY.put("country_id", "countryId");
    AGGS_TERM_KEY.put("province_id", "provinceId");
    AGGS_TERM_KEY.put("city_id", "cityId");
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
    return ManagerConstants.TABLE_METRIC_LOCATION_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricLocationDataRecordDao#queryMetricLocationRawdatas(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocationRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      result = queryMetricDataRecord(queryVO);
    } catch (IOException e) {
      LOGGER.warn("failed to query location metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricLocationDataRecordDao#queryMetricLocations(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricLocationDataRecordDO> queryMetricLocations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<MetricLocationDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields);
    setSpecialKpiAggs(aggsFields);

    try {
      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, PRIMARY_TERM_KEY, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query location metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricLocationDataRecordDao#queryMetricLocationHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocationHistograms(MetricQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions) {
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

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, PRIMARY_TERM_KEY, aggsFields);
      result.addAll(batchResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query location histograms.", e);
    }

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

  private void setSpecialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    specialKpiAggs(aggsFields);
  }

  private MetricLocationDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricLocationDataRecordDO recordDO = new MetricLocationDataRecordDO();
    String countryId = MapUtils.getString(item, "countryId", null);
    recordDO.setNetworkId(StringUtils.equals(countryId, "null") ? null : countryId);
    recordDO.setCountryId(MapUtils.getString(item, "countryId"));
    String provinceId = MapUtils.getString(item, "provinceId");
    recordDO.setProvinceId(StringUtils.equals(provinceId, "null") ? null : provinceId);
    String cityId = MapUtils.getString(item, "cityId");
    recordDO.setCityId(StringUtils.equals(cityId, "null") ? null : cityId);

    tranKPIMapToDateRecord(item, recordDO);
    recordDO.setBytepsPeak(MapUtils.getLongValue(item, "bytepsPeak"));
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
    recordDO.setTcpZeroWindowPackets(MapUtils.getLongValue(item, "tcpZeroWindowPackets;"));
    recordDO.setBytepsPeak(MapUtils.getLongValue(item, "bytepsPeak"));

    return recordDO;
  }

  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("country_id,");
    sql.append("province_id,");
    sql.append("city_id,");
    sql.append("network_id,");
    sql.append("service_id,");

    sql.append("byteps_peak,");
    sql.append("total_bytes,");
    sql.append("total_packets,");
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
    sql.append("established_sessions,");
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
    sql.append("timestamp)");
    sql.append(" values (");
    sql.append(":countryId,");
    sql.append(":provinceId,");
    sql.append(":cityId,");
    sql.append(":networkId,");
    sql.append(":serviceId,");

    sql.append(":bytepsPeak,");
    sql.append(":totalBytes,");
    sql.append(":totalPackets,");
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
    sql.append(":establishedSessions,");
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
    sql.append(":timestamp)");

    return sql.toString();
  }

}

