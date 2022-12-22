package com.machloop.fpc.manager.metric.dao.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
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
import com.machloop.fpc.manager.metric.dao.MetricLocationDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricLocationDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
// @Repository
public class MetricLocationDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricLocationDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricLocationDataRecordDaoImpl.class);

  private static final List<Tuple2<String, Boolean>> PRIMARY_TERM_FIELD = Lists
      .newArrayList(Tuples.of("country_id", false), Tuples.of("province_id", true));

  private static final List<Tuple2<String, Boolean>> AGGS_TERM_FIELD = Lists.newArrayList(
      Tuples.of("network_id", false), Tuples.of("service_id", true), Tuples.of("country_id", false),
      Tuples.of("province_id", true), Tuples.of("city_id", true));

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  @Autowired
  private ElasticIndexDao elasticIndexDao;

  @Autowired
  private Spl2DslHelper spl2DslHelper;

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricLocationDataRecordDao#queryMetricLocationRawdatas(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  public List<Map<String, Object>> queryMetricLocationRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      result = queryMetricDataRecord(queryVO, convertIndexAlias(queryVO.getSourceType(),
          queryVO.getInterval(), queryVO.getPacketFileId()));
    } catch (IOException e) {
      LOGGER.warn("failed to query location metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricLocationDataRecordDao#queryMetricLocations(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricLocationDataRecordDO> queryMetricLocations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    Map<String, String> aggsFields = getAggsFields();
    List<
        BucketScriptPipelineAggregationBuilder> bucketScriptAggsFields = getBucketScriptAggsFields();

    List<MetricLocationDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      Map<String, Object> after = null;
      int currentBatchSize = 0;
      List<Map<String, Object>> tempResult = Lists.newArrayListWithCapacity(0);
      do {
        Tuple2<Map<String, Object>,
            List<Map<String, Object>>> batchResult = compositeTermMetricAggregate(
                Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
                Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()),
                convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
                    queryVO.getPacketFileId()),
                PRIMARY_TERM_FIELD, queryVO.getDsl(), null, aggsFields, bucketScriptAggsFields,
                sortProperty, sortDirection, COMPOSITE_BATCH_SIZE, after);

        after = batchResult.getT1();
        currentBatchSize = batchResult.getT2().size();
        List<Map<String, Object>> temp = batchResult.getT2();
        if (temp.size() > TOP_SIZE) {
          temp = temp.subList(0, TOP_SIZE);
        }
        tempResult.addAll(temp);
        tempResult = sortMetricResult(tempResult, sortProperty, sortDirection);
      } while (currentBatchSize == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());

      tempResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query location metric.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricLocationDataRecordDao#queryMetricLocationHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocationHistograms(MetricQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      // 获取聚合字段的聚合方式
      Map<String, String> allAggsFields = getAggsFields();
      Map<String,
          BucketScriptPipelineAggregationBuilder> bucketScriptAggsFieldMap = getBucketScriptAggsFields()
              .stream().collect(
                  Collectors.toMap(BucketScriptPipelineAggregationBuilder::getName, agg -> agg));

      Map<String, String> aggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      List<BucketScriptPipelineAggregationBuilder> bucketScriptAggsFields = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      getCombinationAggsFields(aggsField).forEach(item -> {
        if (allAggsFields.containsKey(item)) {
          aggsFields.put(item, allAggsFields.get(item));
        } else if (bucketScriptAggsFieldMap.containsKey(item)) {
          bucketScriptAggsFields.add(bucketScriptAggsFieldMap.get(item));
        }
      });
      if (aggsFields.isEmpty() && bucketScriptAggsFields.isEmpty()) {
        return result;
      }

      // 生成附加条件
      BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
      combinationConditions.forEach(combinationCondition -> {
        BoolQueryBuilder itemQuery = QueryBuilders.boolQuery();
        combinationCondition.entrySet().forEach(entry -> {
          if (entry.getValue() != null) {
            itemQuery.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
          } else {
            itemQuery.mustNot(QueryBuilders.existsQuery(entry.getKey()));
          }
        });

        boolQuery.should(itemQuery);
      });

      Map<String, Object> after = null;
      List<Map<String, Object>> batchList = null;
      do {
        Tuple2<Map<String, Object>,
            List<Map<String, Object>>> batchResult = compositeDateHistogramTermMetricAggregate(
                Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
                Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()),
                queryVO.getInterval(),
                convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
                    queryVO.getPacketFileId()),
                PRIMARY_TERM_FIELD, queryVO.getDsl(), boolQuery, aggsFields, bucketScriptAggsFields,
                COMPOSITE_BATCH_SIZE, after);
        after = batchResult.getT1();
        batchList = batchResult.getT2();

        result.addAll(batchList);
      } while (batchList.size() == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());
    } catch (IOException e) {
      LOGGER.warn("failed to query location datehistogram.", e);
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
    return ManagerConstants.INDEX_METRIC_LOCATION_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexAliasName()
   */
  @Override
  protected String getIndexAliasName() {
    return ManagerConstants.ALIAS_METRIC_LOCATION_DATA_RECORD;
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
    setAggsKPIFields(aggsFields);

    aggsFields.put("byteps_peak", "max");
    aggsFields.put("downstream_bytes", "sum");
    aggsFields.put("downstream_packets", "sum");
    aggsFields.put("upstream_bytes", "sum");
    aggsFields.put("upstream_packets", "sum");
    aggsFields.put("total_payload_bytes", "sum");
    aggsFields.put("total_payload_packets", "sum");
    aggsFields.put("downstream_payload_bytes", "sum");
    aggsFields.put("downstream_payload_packets", "sum");
    aggsFields.put("upstream_payload_bytes", "sum");
    aggsFields.put("upstream_payload_packets", "sum");
    aggsFields.put("tcp_syn_packets", "sum");
    aggsFields.put("tcp_syn_ack_packets", "sum");
    aggsFields.put("tcp_syn_rst_packets", "sum");
    return aggsFields;
  }

  private List<BucketScriptPipelineAggregationBuilder> getBucketScriptAggsFields() {
    List<BucketScriptPipelineAggregationBuilder> bucketScriptList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    setBucketScriptAggsKPIFields(bucketScriptList);
    return bucketScriptList;
  }

  private MetricLocationDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricLocationDataRecordDO recordDO = new MetricLocationDataRecordDO();
    tranKPIMapToDateRecord(item, recordDO);
    recordDO.setCountryId(MapUtils.getString(item, "country_id"));
    String provinceId = MapUtils.getString(item, "province_id");
    recordDO.setProvinceId(StringUtils.equals(provinceId, "null") ? null : provinceId);
    String cityId = MapUtils.getString(item, "city_id");
    recordDO.setCityId(StringUtils.equals(cityId, "null") ? null : cityId);

    recordDO.setBytepsPeak(MapUtils.getLongValue(item, "byteps_peak"));
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstream_bytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstream_packets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstream_bytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstream_packets"));
    recordDO.setTotalPayloadBytes(MapUtils.getLongValue(item, "total_payload_bytes"));
    recordDO.setTotalPayloadPackets(MapUtils.getLongValue(item, "total_payload_packets"));
    recordDO.setDownstreamPayloadBytes(MapUtils.getLongValue(item, "downstream_payload_bytes"));
    recordDO.setDownstreamPayloadPackets(MapUtils.getLongValue(item, "downstream_payload_packets"));
    recordDO.setUpstreamPayloadBytes(MapUtils.getLongValue(item, "upstream_payload_bytes"));
    recordDO.setUpstreamPayloadPackets(MapUtils.getLongValue(item, "upstream_payload_packets"));
    recordDO.setTcpSynPackets(MapUtils.getLongValue(item, "tcp_syn_packets"));
    recordDO.setTcpSynAckPackets(MapUtils.getLongValue(item, "tcp_syn_ack_packets"));
    recordDO.setTcpSynRstPackets(MapUtils.getLongValue(item, "tcp_syn_rst_packets"));
    return recordDO;
  }
}
