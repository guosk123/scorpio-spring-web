package com.machloop.fpc.manager.metric.dao.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.global.dao.ElasticIndexDao;
import com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.manager.helper.Spl2DslHelper;
import com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricApplicationDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
// @Repository
public class MetricApplicationDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricApplicationDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricApplicationDataRecordDaoImpl.class);

  private static final String CATEGORY_TERM_FIELD = "category_id";
  private static final String SUBCATEGORY_TERM_FIELD = "subcategory_id";
  private static final String APPLICATION_TERM_FIELD = "application_id";

  private static final List<Tuple2<String, Boolean>> AGGS_TERM_FIELD = Lists.newArrayList(
      Tuples.of("network_id", false), Tuples.of("service_id", true), Tuples.of("type", false),
      Tuples.of("application_id", false), Tuples.of("category_id", false),
      Tuples.of("subcategory_id", false));

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  @Autowired
  private ElasticIndexDao elasticIndexDao;

  @Autowired
  private Spl2DslHelper spl2DslHelper;

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao#queryMetricApplicationRawdatas(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplicationRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      result = queryMetricDataRecord(queryVO, convertIndexAlias(queryVO.getSourceType(),
          queryVO.getInterval(), queryVO.getPacketFileId()));
    } catch (IOException e) {
      LOGGER.warn("failed to query metric application.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao#queryMetricApplications(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<MetricApplicationDataRecordDO> queryMetricApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, int type) {
    Map<String, String> aggsFields = getAggsFields();
    List<
        BucketScriptPipelineAggregationBuilder> bucketScriptAggsFields = getBucketScriptAggsFields();

    List<MetricApplicationDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      String termField = "";
      switch (type) {
        case FpcConstants.METRIC_TYPE_APPLICATION_CATEGORY:
          termField = CATEGORY_TERM_FIELD;
          break;
        case FpcConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
          termField = SUBCATEGORY_TERM_FIELD;
          break;
        case FpcConstants.METRIC_TYPE_APPLICATION_APP:
          termField = APPLICATION_TERM_FIELD;
          break;
        default:
          return result;
      }

      List<Map<String, Object>> tempResult = termMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          termField, queryVO.getDsl(), null, aggsFields, bucketScriptAggsFields, sortProperty,
          sortDirection);

      tempResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query application metric.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao#countMetricApplications(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> countMetricApplications(MetricQueryVO queryVO, String aggsField,
      String sortProperty, String sortDirection, List<String> networkIds) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 获取聚合字段的聚合方式
    Map<String, String> allAggsFields = getAggsFields();
    Map<String,
        BucketScriptPipelineAggregationBuilder> bucketScriptAggsFieldMap = getBucketScriptAggsFields()
            .stream()
            .collect(Collectors.toMap(BucketScriptPipelineAggregationBuilder::getName, agg -> agg));

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

    // 附加过滤
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    boolQuery.must(QueryBuilders.termQuery("type", FpcConstants.METRIC_TYPE_APPLICATION_APP));
    if (CollectionUtils.isNotEmpty(networkIds)) {
      boolQuery.must(QueryBuilders.termsQuery("network_id", networkIds));
      boolQuery.must(QueryBuilders.termQuery("service_id", ""));
    } else {
      String networkId = StringUtils.equals(queryVO.getSourceType(),
          FpcConstants.SOURCE_TYPE_PACKET_FILE) ? queryVO.getPacketFileId()
              : queryVO.getNetworkId();
      boolQuery.must(QueryBuilders.termQuery("network_id", networkId));
      boolQuery.must(QueryBuilders.termQuery("service_id",
          StringUtils.defaultIfBlank(queryVO.getServiceId(), "")));
    }

    try {
      result = termMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          "application_id", queryVO.getDsl(), boolQuery, aggsFields, bucketScriptAggsFields,
          sortProperty, sortDirection);
    } catch (IOException e) {
      LOGGER.warn("failed to count all application metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricApplicationDataRecordDao#queryMetricApplicationHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplicationHistograms(MetricQueryVO queryVO,
      String termField, String aggsField, List<String> ids) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      // 获取聚合字段的聚合方式
      Map<String, String> aggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      List<BucketScriptPipelineAggregationBuilder> bucketScriptAggsFields = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (StringUtils.equals(aggsField, ManagerConstants.METRIC_NPM_ALL_AGGSFILED)) {
        aggsFields = getAggsFields();
        bucketScriptAggsFields = getBucketScriptAggsFields();
      } else {
        Map<String, String> allAggsFields = getAggsFields();
        Map<String,
            BucketScriptPipelineAggregationBuilder> bucketScriptAggsFieldMap = getBucketScriptAggsFields()
                .stream().collect(
                    Collectors.toMap(BucketScriptPipelineAggregationBuilder::getName, agg -> agg));
        List<String> combinationAggsFields = getCombinationAggsFields(aggsField);
        for (String item : combinationAggsFields) {
          if (allAggsFields.containsKey(item)) {
            aggsFields.put(item, allAggsFields.get(item));
          } else if (bucketScriptAggsFieldMap.containsKey(item)) {
            bucketScriptAggsFields.add(bucketScriptAggsFieldMap.get(item));
          }
        }

        if (aggsFields.isEmpty() && bucketScriptAggsFields.isEmpty()) {
          return result;
        }
      }

      result = dateHistogramTermMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()), queryVO.getInterval(),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          termField, queryVO.getDsl(), QueryBuilders.termsQuery(termField, ids), aggsFields,
          bucketScriptAggsFields, false);
    } catch (IOException e) {
      LOGGER.warn("failed to query app datehistogram.", e);
    }
    return result;
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    throw new UnsupportedOperationException();
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
    return ManagerConstants.INDEX_METRIC_APP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexAliasName()
   */
  @Override
  protected String getIndexAliasName() {
    return ManagerConstants.ALIAS_METRIC_APP_DATA_RECORD;
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

    // tcpEstablishedFailRate
    Map<String,
        String> tEFRBucketsPathsMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    tEFRBucketsPathsMap.put("tcpEstablishedFailCounts", "tcp_established_fail_counts");
    tEFRBucketsPathsMap.put("tcpEstablishedSuccessCounts", "tcp_established_success_counts");
    BucketScriptPipelineAggregationBuilder tEFRBucketScript = PipelineAggregatorBuilders
        .bucketScript("tcp_established_fail_rate", tEFRBucketsPathsMap, new Script(
            "params.tcpEstablishedFailCounts + params.tcpEstablishedSuccessCounts == 0 ? "
                + "0 : params.tcpEstablishedFailCounts/(params.tcpEstablishedFailCounts + params.tcpEstablishedSuccessCounts)"));
    bucketScriptList.add(tEFRBucketScript);

    return bucketScriptList;
  }

  private MetricApplicationDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricApplicationDataRecordDO recordDO = new MetricApplicationDataRecordDO();
    tranKPIMapToDateRecord(item, recordDO);
    recordDO.setApplicationId(MapUtils.getIntValue(item, "application_id"));
    recordDO.setCategoryId(MapUtils.getIntValue(item, "category_id"));
    recordDO.setSubcategoryId(MapUtils.getIntValue(item, "subcategory_id"));
    recordDO.setType(MapUtils.getIntValue(item, "type"));

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
