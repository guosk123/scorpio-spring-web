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
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.global.dao.clickhouse.AbstractDataRecordDaoCkImpl;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.dao.MetricHttpRequestDataRecordDao;
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
public class MetricHttpRequestDataRecordDaoImpl extends AbstractDataRecordDaoCkImpl
    implements MetricHttpRequestDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricHttpRequestDataRecordDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_metric_http_request_data_record";

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
   * @return
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
   * @see com.machloop.fpc.manager.metric.dao.MetricHttpRequestDataRecordDao#queryHttpRequestHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryHttpRequestHistograms(MetricQueryVO queryVO) {
    List<Map<String, Object>> dateHistogram = Lists.newArrayListWithCapacity(0);
    try {
      dateHistogram = dateHistogramMetricAggregate(convertTableName(queryVO.getSourceType(),
          queryVO.getInterval(), queryVO.getPacketFileId()), queryVO, null, getAggsFields());
    } catch (IOException e) {
      LOGGER.warn("failed to query http request datehistogram.");
    }

    return dateHistogram;
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

    Map<String, String> keys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    keys.put("network_id", "networkId");

    int totalSize = 0;
    int offset = 0;
    int currentSize = 0;
    List<Map<String, Object>> batchList = null;
    do {
      batchList = termMetricAggregate(inputTableName, queryVO, null, keys, getAggsFields(),
          "requestCounts", DEFAULT_SORT_DIRECTION, COMPOSITE_BATCH_SIZE, offset);
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

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(outputTableName);
    sql.append(" (timestamp, network_id, request_counts, response_counts, error_response_counts) ");
    sql.append(" values(:timestamp, :networkId, :requestCounts, ");
    sql.append(" :responseCounts, :errorResponseCounts)");

    batchList.forEach(item -> {
      item.put("timestamp", DateUtils.toStringFormat((Date) item.get("timestamp"),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(batchList);

    return Arrays.stream(jdbcTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource))
        .sum();
  }

  private Map<String, Tuple2<AggsFunctionEnum, String>> getAggsFields() {
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggsFields.put("requestCounts", Tuples.of(AggsFunctionEnum.SUM, "request_counts"));
    aggsFields.put("responseCounts", Tuples.of(AggsFunctionEnum.SUM, "response_counts"));
    aggsFields.put("errorResponseCounts", Tuples.of(AggsFunctionEnum.SUM, "error_response_counts"));

    return aggsFields;
  }

}
