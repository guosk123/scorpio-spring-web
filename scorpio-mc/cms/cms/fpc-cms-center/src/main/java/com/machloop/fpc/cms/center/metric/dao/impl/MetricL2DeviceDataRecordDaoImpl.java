package com.machloop.fpc.cms.center.metric.dao.impl;


import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metric.dao.MetricL2DeviceDataRecordDao;
import com.machloop.fpc.cms.center.metric.data.MetricL2DeviceDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * Create at 2021年8月16日, fpc-manager
 */
@Repository
public class MetricL2DeviceDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricL2DeviceDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricL2DeviceDataRecordDaoImpl.class);

  private static final Map<String, String> PRIMARY_TERM_KEY;
  private static final Map<String, String> AGGS_TERM_KEY;
  static {
    PRIMARY_TERM_KEY = new HashMap<String, String>();
    AGGS_TERM_KEY = new HashMap<String, String>();
    PRIMARY_TERM_KEY.put("mac_address", "macAddress");
    AGGS_TERM_KEY.put("network_id", "networkId");
    AGGS_TERM_KEY.put("service_id", "serviceId");
    AGGS_TERM_KEY.put("mac_address", "macAddress");
    AGGS_TERM_KEY.put("ethernet_type", "ethernetType");
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
    return CenterConstants.TABLE_METRIC_L2DEVICE_DATA_RECORD;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricL2DeviceDataRecordDao#queryMetricL2DeviceRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2DeviceRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      result = queryMetricDataRecord(queryVO);
    } catch (IOException e) {
      LOGGER.warn("failed to query l2-device metric.", e);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricL2DeviceDataRecordDao#queryMetricL2Devices(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricL2DeviceDataRecordDO> queryMetricL2Devices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<MetricL2DeviceDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 分组字段
    Map<String, String> termKeys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    termKeys.put("mac_address", "macAddress");
    try {
      if (spl2SqlHelper.getFilterFields(queryVO.getDsl()).contains("ethernet_type")) {
        termKeys.put("ethernet_type", "ethernetType");
      }
    } catch (V8ScriptExecutionException | IOException e) {
      LOGGER.warn("failed to query l2-device metric.", e);
      return result;
    }

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields);
    setSpecialKpiAggs(aggsFields);

    try {
      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, termKeys, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query l2-device metric.", e);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricL2DeviceDataRecordDao#queryMetricL2DeviceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2DeviceHistograms(MetricQueryVO queryVO,
      String aggsField, List<String> macAddress) {
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

    // 附加过滤条件
    List<Map<String, Object>> combinationConditions = macAddress.stream().map(item -> {
      Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      itemMap.put("mac_address", item);
      return itemMap;
    }).collect(Collectors.toList());

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, PRIMARY_TERM_KEY, aggsFields);
      result.addAll(batchResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query l2-device histograms.", e);
    }

    return result;
  }

  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    kpiAggs(aggsFields);
    aggsFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    aggsFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    aggsFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggsFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
  }

  private void setSpecialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    specialKpiAggs(aggsFields);
  }

  private MetricL2DeviceDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricL2DeviceDataRecordDO recordDO = new MetricL2DeviceDataRecordDO();
    String mac_address = MapUtils.getString(item, "macAddress");
    recordDO.setMacAddress(StringUtils.equals(mac_address, "null") ? null : mac_address);
    String ethernet_type = MapUtils.getString(item, "ethernetType");
    recordDO.setEthernetType(StringUtils.equals(ethernet_type, "null") ? null : ethernet_type);

    tranKPIMapToDateRecord(item, recordDO);
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstreamBytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstreamPackets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstreamBytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstreamPackets"));

    return recordDO;
  }

}
