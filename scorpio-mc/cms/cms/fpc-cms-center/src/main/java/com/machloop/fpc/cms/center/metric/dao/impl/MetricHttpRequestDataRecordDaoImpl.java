package com.machloop.fpc.cms.center.metric.dao.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metric.dao.MetricHttpRequestDataRecordDao;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年7月21日, fpc-manager
 */
@Repository
public class MetricHttpRequestDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricHttpRequestDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricHttpRequestDataRecordDaoImpl.class);

  private static final String TABLE_NAME = "d_fpc_metric_http_request_data_record";

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricHttpRequestDataRecordDao#queryHttpRequestHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
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
    return TABLE_NAME;
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
