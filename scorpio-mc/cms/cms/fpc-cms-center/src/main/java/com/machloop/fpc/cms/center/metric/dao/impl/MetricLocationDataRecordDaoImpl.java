package com.machloop.fpc.cms.center.metric.dao.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metric.dao.MetricLocationDataRecordDao;
import com.machloop.fpc.cms.center.metric.data.MetricLocationDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 * 
 * create at 2021年8月6日, fpc-manager
 */
@Repository
public class MetricLocationDataRecordDaoImpl extends AbstractDataRecordDaoImpl
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
  private ClickHouseJdbcTemplate jdbcTemplate;

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getSpl2SqlHelper()
   */
  @Override
  protected Spl2SqlHelper getSpl2SqlHelper() {
    return spl2SqlHelper;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return CenterConstants.TABLE_METRIC_LOCATION_DATA_RECORD;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricLocationDataRecordDao#queryMetricLocationRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
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
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricLocationDataRecordDao#queryMetricLocations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
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
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricLocationDataRecordDao#queryMetricLocationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
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

}
