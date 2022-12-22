package com.machloop.fpc.cms.center.metric.dao.impl;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
import com.machloop.fpc.cms.center.metric.dao.MetricDscpDataRecordDao;
import com.machloop.fpc.cms.center.metric.data.MetricDscpDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author  fengtianyou
 *
 * create at 2021年8月25日, fpc-manager
 */
@Repository
public class MetricDscpDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricDscpDataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricDscpDataRecordDaoImpl.class);

  private static final Map<String, String> SERVER_TERM_KEY;
  private static final Map<String, String> TERM_KEY;
  private static final Map<String, String> AGGS_TERM_KEY;
  static {
    SERVER_TERM_KEY = new HashMap<String, String>();
    TERM_KEY = new HashMap<String, String>();
    AGGS_TERM_KEY = new HashMap<String, String>();
    SERVER_TERM_KEY.put("dhcp_version", "dhcpVersion");
    SERVER_TERM_KEY.put("server_ip_address", "serverIpAddress");
    SERVER_TERM_KEY.put("server_mac_address", "serverMacAddress");
    TERM_KEY.put("type", "type");
    AGGS_TERM_KEY.put("network_id", "networkId");
    AGGS_TERM_KEY.put("service_id", "serviceId");
    AGGS_TERM_KEY.put("type", "type");
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
    return CenterConstants.TABLE_METRIC_DSCP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricDscpDataRecordDao#queryMetricDscps(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricDscpDataRecordDO> queryMetricDscps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<
        MetricDscpDataRecordDO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields);

    // 添加条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      for (String item : queryVO.getNetworkIds()) {
        Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        itemMap.put("network_id", item);
        itemMap.put("service_id", "");
        combinationConditions.add(itemMap);
      }
    }
    if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      for (Tuple2<String, String> item : queryVO.getServiceNetworkIds()) {
        Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        itemMap.put("service_id", item.getT1());
        itemMap.put("network_id", item.getT2());
        combinationConditions.add(itemMap);
      }
    }

    try {
      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, TERM_KEY, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query dscp metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.dao.MetricDscpDataRecordDao#queryMetricDscpHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricDscpHistograms(MetricQueryVO queryVO,
      String aggsField, List<String> dscpTypes) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(allAggsFields);
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

    // 过滤条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(queryVO.getServiceId())) {
      Map<String, Object> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      termKey.put("service_id", "");
      combinationConditions.add(termKey);
    }
    combinationConditions.addAll(dscpTypes.stream().map(item -> {
      Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      itemMap.put("type", item);
      return itemMap;
    }).collect(Collectors.toList()));

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, TERM_KEY, aggsFields);
      result.addAll(batchResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query dscp histograms.", e);
    }

    return result;
  }


  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {

    aggsFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    aggsFields.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
  }

  private MetricDscpDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricDscpDataRecordDO recordDO = new MetricDscpDataRecordDO();
    recordDO.setTimestamp((Date) item.get("timestamp"));
    String networkId = MapUtils.getString(item, "networkId", null);
    recordDO.setNetworkId(StringUtils.equals(networkId, "null") ? null : networkId);
    String serviceId = MapUtils.getString(item, "serviceId", null);
    recordDO.setServiceId(StringUtils.equals(serviceId, "null") ? null : serviceId);
    recordDO.setType(MapUtils.getString(item, "type"));

    recordDO.setTotalBytes(MapUtils.getLongValue(item, "totalBytes"));
    recordDO.setTotalPackets(MapUtils.getLongValue(item, "totalPackets"));

    return recordDO;
  }

}
