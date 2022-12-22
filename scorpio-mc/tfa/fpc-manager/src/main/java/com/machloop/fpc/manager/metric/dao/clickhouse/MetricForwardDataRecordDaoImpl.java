package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

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
import com.machloop.fpc.manager.metric.dao.MetricForwardDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricForwardPolicyDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author ChenXiao
 *
 * create at 2022/5/12 10:16,IntelliJ IDEA
 *
 */
@Repository
public class MetricForwardDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricForwardDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricForwardDataRecordDaoImpl.class);

  private static final Map<String, String> TERM_KEY;
  private static final Map<String, String> AGG_KEY;
  static {
    TERM_KEY = new HashMap<String, String>();
    AGG_KEY = new HashMap<String, String>();
    TERM_KEY.put("policy_id", "policyId");
    TERM_KEY.put("network_id", "networkId");
    TERM_KEY.put("netif_name", "netifName");

    AGG_KEY.put("policy_id", "policyId");
    AGG_KEY.put("network_id", "networkId");
    AGG_KEY.put("netif_name", "netifName");
  }


  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  @Autowired
  private PacketAnalysisSubTaskDao offlineAnalysisSubTaskDao;


  @Override
  public Map<String, Object> queryBandWidthByPolicyId(String policyId) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    try {
      StringBuilder sql = new StringBuilder();
      sql.append("select sum(forward_total_bytes) as forward_total_bytes, ");
      sql.append("timestamp");
      sql.append(" from ").append(getTableName());
      sql.append(" where 1=1 ");

      Map<String, String> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      if (StringUtils.isNotBlank(policyId)) {
        sql.append(" and policy_id = :policyId ");
        param.put("policyId", policyId);
      }
      sql.append(" group by timestamp order by timestamp desc ");

      List<Map<String, Object>> tempResult = jdbcTemplate.getJdbcTemplate().query(sql.toString(),
          param, new ColumnMapRowMapper());

      if (CollectionUtils.isNotEmpty(tempResult)) {
        result = tempResult.get(0);
      }
    } catch (Exception e) {
      LOGGER.warn("failed to query forward policy metric.", e);
    }

    return result;

  }


  @Override
  public List<MetricForwardPolicyDataRecordDO> queryMetricForwardPolicyHistograms(
      MetricQueryVO queryVO, String policyId, boolean extendedBound) {
    List<MetricForwardPolicyDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields);

    // 接口名称过滤
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(policyId)) {
      Map<String, Object> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      termKey.put("policy_id", policyId);
      combinationConditions.add(termKey);
    }

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, TERM_KEY, aggsFields);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query forward policy metric.", e);
    }

    return result;

  }

  @Override
  protected Spl2SqlHelper getSpl2SqlHelper() {
    return spl2SqlHelper;
  }

  @Override
  protected ClickHouseStatsJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  @Override
  protected PacketAnalysisSubTaskDao getOfflineAnalysisSubTaskDao() {
    return offlineAnalysisSubTaskDao;
  }

  @Override
  protected String getTableName() {
    return ManagerConstants.TABLE_METRIC_FORWARD_DATA_RECORD;
  }

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
          "forward_total_bytes", DEFAULT_SORT_DIRECTION, COMPOSITE_BATCH_SIZE, offset);
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
    aggsFields.put("forwardTotalBytes", Tuples.of(AggsFunctionEnum.SUM, "forward_total_bytes"));
    aggsFields.put("forwardSuccessBytes", Tuples.of(AggsFunctionEnum.SUM, "forward_success_bytes"));
    aggsFields.put("forwardFailBytes", Tuples.of(AggsFunctionEnum.SUM, "forward_fail_bytes"));
    aggsFields.put("forwardTotalPackets", Tuples.of(AggsFunctionEnum.SUM, "forward_total_packets"));
    aggsFields.put("forwardSuccessPackets",
        Tuples.of(AggsFunctionEnum.SUM, "forward_success_packets"));
    aggsFields.put("forwardFailPackets", Tuples.of(AggsFunctionEnum.SUM, "forward_fail_packets"));
  }

  private MetricForwardPolicyDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricForwardPolicyDataRecordDO recordDO = new MetricForwardPolicyDataRecordDO();

    if (item.get("timestamp") != null) {
      OffsetDateTime timestamp = (OffsetDateTime) item.get("timestamp");
      recordDO.setTimestamp(Date.from(timestamp.toInstant()));
    }

    recordDO.setNetifName(MapUtils.getString(item, "netifName"));
    recordDO.setNetworkId(MapUtils.getString(item, "networkId"));
    recordDO.setPolicyId(MapUtils.getString(item, "policyId"));

    recordDO.setForwardTotalBytes(MapUtils.getLongValue(item, "forwardTotalBytes"));
    recordDO.setForwardSuccessBytes(MapUtils.getLongValue(item, "forwardSuccessBytes"));
    recordDO.setForwardFailBytes(MapUtils.getLongValue(item, "forwardFailBytes"));
    recordDO.setForwardTotalPackets(MapUtils.getLongValue(item, "forwardTotalPackets"));
    recordDO.setForwardSuccessPackets(MapUtils.getLongValue(item, "forwardSuccessPackets"));
    recordDO.setForwardFailPackets(MapUtils.getLongValue(item, "forwardFailPackets"));

    return recordDO;
  }

  private String batchUpdatesql(String tableName) {
    StringBuilder sql = new StringBuilder();

    sql.append("insert into ");
    sql.append(tableName);
    sql.append("(");
    sql.append("network_id,");
    sql.append("netif_name,");
    sql.append("policy_id,");
    sql.append("forward_total_bytes,");
    sql.append("forward_success_bytes,");
    sql.append("forward_fail_bytes,");
    sql.append("forward_total_packets,");
    sql.append("forward_success_packets,");
    sql.append("forward_fail_packets,");
    sql.append("timestamp)");
    sql.append(" values (");
    sql.append(":networkId,");
    sql.append(":netifName,");
    sql.append(":policyId,");
    sql.append(":forwardTotalBytes,");
    sql.append(":forwardSuccessBytes,");
    sql.append(":forwardFailBytes,");
    sql.append(":forwardTotalPackets,");
    sql.append(":forwardSuccessPackets,");
    sql.append(":forwardFailPackets,");
    sql.append(":timestamp)");

    return sql.toString();
  }
}
