package com.machloop.fpc.manager.metric.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.dao.MetricOsDataRecordDao;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年7月21日, fpc-manager
 */
@Repository
public class MetricOsDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricOsDataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricOsDataRecordDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_metric_os_data_record";

  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

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
    return TABLE_NAME;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricOsDataRecordDao#queryOsMetric(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryOsMetric(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);

    // 分组
    Map<String, String> keyMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    keyMap.put("type", "type");

    // 聚合
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggsFields.put("count", Tuples.of(AggsFunctionEnum.UNIQ_ARRAY, "ip_list"));

    try {
      result = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, keyMap, aggsFields, sortProperty, sortDirection);
    } catch (IOException e) {
      LOGGER.warn("failed to query os analysis metric.");
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl#aggregate(java.util.Date, java.util.Date, int, java.lang.String, java.lang.String)
   */
  @Override
  protected int aggregate(Date startTime, Date endTime, int interval, String inputTableName,
      String outputTableName) throws IOException {
    // 构造查询语句
    StringBuilder sql = new StringBuilder();
    sql.append(" select network_id as networkId, type, ");
    sql.append(" groupUniqArray(arrayJoin(ip_list)) as ipList ");
    sql.append(" from ").append(inputTableName);
    sql.append(" where timestamp > toDateTime64(:start_time, 3, 'UTC') ");
    sql.append(" and timestamp <= toDateTime64(:end_time, 3, 'UTC') ");
    sql.append(" group by network_id, type ");

    // 构造过滤条件
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("termMetricAggregate sql : [{}], param: [{}] ", sql.toString(), params);
    }

    List<Map<String, Object>> aggregate = getClickHouseJdbcTemplate().getJdbcTemplate()
        .query(sql.toString(), params, new ColumnMapRowMapper());
    aggregate.forEach(item -> item.put("timestamp", endTime));

    int totalSize = 0;
    if (aggregate.size() > 0) {
      int lastTimeBatchSize = 0;
      do {
        List<Map<String, Object>> batchList = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (aggregate.size() >= totalSize + COMPOSITE_BATCH_SIZE) {
          batchList = aggregate.subList(totalSize, totalSize + COMPOSITE_BATCH_SIZE);
        } else {
          batchList = aggregate.subList(totalSize, aggregate.size());
        }

        lastTimeBatchSize = saveMetricDataRecord(batchList, outputTableName);
        totalSize += lastTimeBatchSize;
      } while (lastTimeBatchSize == COMPOSITE_BATCH_SIZE
          && !GracefulShutdownHelper.isShutdownNow());
    }

    return totalSize;
  }

  private int saveMetricDataRecord(List<Map<String, Object>> batchList, String outputTableName) {
    if (CollectionUtils.isEmpty(batchList)) {
      return 0;
    }

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(outputTableName);
    sql.append(" (timestamp, network_id, type, ip_list) ");
    sql.append(" values(:timestamp, :networkId, :type, :ipList) ");

    batchList.forEach(item -> {
      item.put("timestamp", DateUtils.toStringFormat((Date) item.get("timestamp"),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

      // ipList
      if (item.get("ipList") != null) {
        String[] ipArray = JsonHelper.deserialize(JsonHelper.serialize(item.get("ipList")),
            new TypeReference<String[]>() {
            }, false);
        item.put("ipList", ipArray);
      }
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(batchList);
    return Arrays.stream(jdbcTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource))
        .sum();
  }

}
