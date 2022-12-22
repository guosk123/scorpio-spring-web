package com.machloop.fpc.manager.metric.dao.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.global.dao.ElasticIndexDao;
import com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.manager.helper.Spl2DslHelper;
import com.machloop.fpc.manager.metric.dao.MetricDscpDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricDscpDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年4月23日, fpc-manager
 */
// @Repository
public class MetricDscpDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricDscpDataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricDscpDataRecordDaoImpl.class);

  private static final String TERM_FIELD = "type";

  private static final List<Tuple2<String, Boolean>> AGGS_TERM_FIELD = Lists.newArrayList(
      Tuples.of("network_id", false), Tuples.of("service_id", true), Tuples.of("type", false));

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  @Autowired
  private ElasticIndexDao elasticIndexDao;

  @Autowired
  private Spl2DslHelper spl2DslHelper;

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDscpDataRecordDao#queryMetricDscps(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricDscpDataRecordDO> queryMetricDscps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<
        MetricDscpDataRecordDO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    TermQueryBuilder termQuery = null;
    if (StringUtils.isBlank(queryVO.getServiceId())) {
      termQuery = QueryBuilders.termQuery("service_id", "");
    }

    try {
      Map<String, String> aggsFields = getAggsFields();
      List<Map<String, Object>> aggregate = termMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          TERM_FIELD, queryVO.getDsl(), termQuery, aggsFields, Lists.newArrayList(), sortProperty,
          sortDirection);

      aggregate.forEach(item -> result.add(tranResultMapToDateRecord(item)));
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

    try {
      // 获取聚合字段的聚合方式
      Map<String, String> allAggsFields = getAggsFields();
      if (!allAggsFields.containsKey(aggsField)) {
        return result;
      }
      Map<String, String> aggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      aggsFields.put(aggsField, allAggsFields.get(aggsField));

      BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
      if (StringUtils.isBlank(queryVO.getServiceId())) {
        boolQuery.must(QueryBuilders.termQuery("service_id", ""));
      }
      boolQuery.must(QueryBuilders.termsQuery(TERM_FIELD, dscpTypes));

      result = dateHistogramTermMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()), queryVO.getInterval(),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          TERM_FIELD, queryVO.getDsl(), boolQuery, aggsFields, Lists.newArrayList(), false);
    } catch (IOException e) {
      LOGGER.warn("failed to query dscp datehistogram.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getElasticIndexDao()
   */
  @Override
  protected ElasticIndexDao getElasticIndexDao() {
    return elasticIndexDao;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getRestHighLevelClient()
   */
  @Override
  protected RestHighLevelClient getRestHighLevelClient() {
    return restHighLevelClient;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getSpl2DslHelper()
   */
  @Override
  protected Spl2DslHelper getSpl2DslHelper() {
    return spl2DslHelper;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexName()
   */
  @Override
  protected String getIndexName() {
    return ManagerConstants.INDEX_METRIC_DSCP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexAliasName()
   */
  @Override
  protected String getIndexAliasName() {
    return ManagerConstants.ALIAS_METRIC_DSCP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#aggregate(java.util.Date, java.util.Date, int, java.lang.String, com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl.WriteCallback)
   */
  @Override
  protected int aggregate(Date startTime, Date endTime, int interval, String indexName,
      WriteCallback writeCallback) throws IOException {
    int success = 0;
    Map<String, String> aggsFields = getAggsFields();

    Map<String, Object> after = null;
    List<Map<String, Object>> batchList = null;
    do {
      List<String> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      Tuple2<Map<String, Object>,
          List<Map<String, Object>>> batchResult = compositeTermMetricAggregate(startTime, endTime,
              indexName, AGGS_TERM_FIELD, aggsFields, COMPOSITE_BATCH_SIZE, after);

      after = batchResult.getT1();
      batchList = batchResult.getT2();

      batchList.forEach(item -> {
        item.put("timestamp", endTime);
        result.add(JsonHelper.serialize(tranResultMapToDateRecord(item)));
      });
      success += writeCallback.process(result);
    } while (batchList.size() == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());

    return success;
  }

  private Map<String, String> getAggsFields() {
    Map<String, String> aggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggsFields.put("total_bytes", "sum");
    aggsFields.put("total_packets", "sum");
    return aggsFields;
  }

  private MetricDscpDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricDscpDataRecordDO recordDO = new MetricDscpDataRecordDO();
    recordDO.setTimestamp((Date) item.get("timestamp"));
    recordDO.setNetworkId(MapUtils.getString(item, "network_id", null));
    String serviceId = MapUtils.getString(item, "service_id", null);
    recordDO.setServiceId(StringUtils.equals(serviceId, "null") ? null : serviceId);
    recordDO.setType(MapUtils.getString(item, "type"));

    recordDO.setTotalBytes(MapUtils.getLongValue(item, "total_bytes"));
    recordDO.setTotalPackets(MapUtils.getLongValue(item, "total_packets"));

    return recordDO;
  }

}
