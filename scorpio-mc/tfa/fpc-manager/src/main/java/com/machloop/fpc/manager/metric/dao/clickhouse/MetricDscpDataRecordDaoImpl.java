package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Arrays;
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
import com.machloop.fpc.manager.metric.dao.MetricDscpDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricDscpDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author  fengtianyou
 *
 * create at 2021年8月25日, fpc-manager
 */
@Repository
public class MetricDscpDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
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
    return ManagerConstants.TABLE_METRIC_DSCP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDscpDataRecordDao#queryMetricDscps(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
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
    List<Map<String, Object>> CombinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(queryVO.getServiceId())) {
      Map<String, Object> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      termKey.put("service_id", "");
      CombinationConditions.add(termKey);
    }

    try {
      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, CombinationConditions, TERM_KEY, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query dscp metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDscpDataRecordDao#queryMetricDscpHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
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

  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("network_id,");
    sql.append("service_id,");
    sql.append("type,");
    sql.append("total_bytes,");
    sql.append("total_packets,");
    sql.append("timestamp)");
    sql.append(" values (");
    sql.append(":networkId,");
    sql.append(":serviceId,");
    sql.append(":type,");
    sql.append(":totalBytes,");
    sql.append(":totalPackets,");
    sql.append(":timestamp)");

    return sql.toString();
  }
}
