package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.io.IOException;
import java.time.OffsetDateTime;
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
import org.springframework.jdbc.core.ColumnMapRowMapper;
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
import com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricNetifDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * create at 2021年8月25日, fpc-manager
 */
@Repository
public class MetricNetifDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
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
    return ManagerConstants.TABLE_METRIC_NETIF_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao#queryMetricNetifHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, boolean)
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
   * @see com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao#queryMetricNetifs(java.util.Date)
   */
  @Override
  public List<MetricNetifDataRecordDO> queryMetricNetifs(Date afterTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select timestamp, network_id, netif_name, ");
    sql.append(" total_bytes, downstream_bytes,  upstream_bytes, transmit_bytes, ");
    sql.append(" total_packets, downstream_packets, upstream_packets, transmit_packets ");
    sql.append(" from ").append(ManagerConstants.TABLE_METRIC_NETIF_DATA_RECORD);
    sql.append(" where timestamp > toDateTime64(:afterTime, 3, 'UTC') ");
    sql.append(" order by timestamp desc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("afterTime",
        DateUtils.toStringFormat(afterTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    List<Map<String, Object>> results = jdbcTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());

    return results.stream().map(item -> {
      MetricNetifDataRecordDO recordDO = new MetricNetifDataRecordDO();

      if (item.get("timestamp") != null) {
        OffsetDateTime timestamp = (OffsetDateTime) item.get("timestamp");
        recordDO.setTimestamp(Date.from(timestamp.toInstant()));
      }
      recordDO.setNetifName(MapUtils.getString(item, "netif_name"));
      recordDO.setNetworkId(MapUtils.getString(item, "network_id"));

      recordDO.setTotalBytes(MapUtils.getLongValue(item, "total_bytes"));
      recordDO.setTotalPackets(MapUtils.getLongValue(item, "total_packets"));
      recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstream_bytes"));
      recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstream_packets"));
      recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstream_bytes"));
      recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstream_packets"));
      recordDO.setTransmitBytes(MapUtils.getLongValue(item, "transmit_bytes"));
      recordDO.setTransmitPackets(MapUtils.getLongValue(item, "transmit_packets"));

      return recordDO;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao#queryNetifLatestState(java.lang.String)
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
      batchList = termMetricAggregate(inputTableName, queryVO, null, AGG_KEY, aggsFields,
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

  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("network_id,");
    sql.append("netif_name,");
    sql.append("transmit_bytes,");
    sql.append("transmit_packets,");
    sql.append("downstream_bytes,");
    sql.append("downstream_packets,");
    sql.append("upstream_bytes,");
    sql.append("upstream_packets,");
    sql.append("total_bytes,");
    sql.append("total_packets,");
    sql.append("timestamp)");
    sql.append(" values (");
    sql.append(":networkId,");
    sql.append(":netifName,");
    sql.append(":transmitBytes,");
    sql.append(":transmitPackets,");
    sql.append(":downstreamBytes,");
    sql.append(":downstreamPackets,");
    sql.append(":upstreamBytes,");
    sql.append(":upstreamPackets,");
    sql.append(":totalBytes,");
    sql.append(":totalPackets,");
    sql.append(":timestamp)");

    return sql.toString();
  }
}
