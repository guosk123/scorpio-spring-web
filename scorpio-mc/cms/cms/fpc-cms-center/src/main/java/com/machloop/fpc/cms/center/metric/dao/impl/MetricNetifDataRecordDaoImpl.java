package com.machloop.fpc.cms.center.metric.dao.impl;

import java.io.IOException;
import java.time.OffsetDateTime;
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
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metric.dao.MetricNetifDataRecordDao;
import com.machloop.fpc.cms.center.metric.data.MetricNetifDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * create at 2021年8月25日, fpc-manager
 */
@Repository
public class MetricNetifDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricNetifDataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricNetifDataRecordDaoImpl.class);
  private static final Map<String, String> TERM_KEY;
  private static final Map<String, String> AGG_KEY;
  static {
    TERM_KEY = new HashMap<String, String>();
    AGG_KEY = new HashMap<String, String>();
    TERM_KEY.put("netif_name", "netifName");
    AGG_KEY.put("netif_name", "netifName");
    AGG_KEY.put("network_id", "networkId");
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
    return CenterConstants.TABLE_METRIC_NETIF_DATA_RECORD;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricNetifDataRecordDao#queryMetricNetifHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, boolean)
   */
  @Override
  public List<MetricNetifDataRecordDO> queryMetricNetifHistograms(MetricQueryVO queryVO,
      String netifName, boolean extendedBound) {
    List<MetricNetifDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields);

    // 接口名称过滤
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(netifName)) {
      Map<String, Object> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      termKey.put("netif_name", netifName);
      combinationConditions.add(termKey);
    }

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, TERM_KEY, aggsFields);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query netif metric.", e);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricNetifDataRecordDao#queryNetifLatestState(java.lang.String)
   */
  @Override
  public Map<String, Object> queryNetifLatestState(String netifName) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    try {
      StringBuilder sql = new StringBuilder();
      sql.append("select timestamp, total_bytes, transmit_bytes ");
      sql.append(" from ").append(getTableName());
      sql.append(" where 1=1 ");

      Map<String, String> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      if (StringUtils.isNotBlank(netifName)) {
        sql.append(" and netif_name = :netifName ");
        param.put("netifName", netifName);
      }

      sql.append("order by timestamp desc limit 1");

      List<Map<String, Object>> tempResult = jdbcTemplate.getJdbcTemplate().query(sql.toString(),
          param, new ColumnMapRowMapper());

      if (CollectionUtils.isNotEmpty(tempResult)) {
        result = tempResult.get(0);
      }
    } catch (Exception e) {
      LOGGER.warn("failed to query metric netif.", e);
    }

    return result;
  }

  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    aggsFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
    aggsFields.put("totalPackets", Tuples.of(AggsFunctionEnum.SUM, "total_packets"));
    aggsFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    aggsFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    aggsFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggsFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    aggsFields.put("transmitBytes", Tuples.of(AggsFunctionEnum.SUM, "transmit_bytes"));
    aggsFields.put("transmitPackets", Tuples.of(AggsFunctionEnum.SUM, "transmit_packets"));
  }

  private MetricNetifDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricNetifDataRecordDO recordDO = new MetricNetifDataRecordDO();
    if (item.get("timestamp") != null) {
      OffsetDateTime timestamp = (OffsetDateTime) item.get("timestamp");
      recordDO.setTimestamp(Date.from(timestamp.toInstant()));
    }
    recordDO.setNetifName(MapUtils.getString(item, "netifName"));
    recordDO.setNetworkId(MapUtils.getString(item, "networkId"));

    recordDO.setTotalBytes(MapUtils.getLongValue(item, "totalBytes"));
    recordDO.setTotalPackets(MapUtils.getLongValue(item, "totalPackets"));
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstreamBytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstreamPackets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstreamBytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstreamPackets"));
    recordDO.setTransmitBytes(MapUtils.getLongValue(item, "transmitBytes"));
    recordDO.setTransmitPackets(MapUtils.getLongValue(item, "transmitPackets"));

    return recordDO;
  }

}
