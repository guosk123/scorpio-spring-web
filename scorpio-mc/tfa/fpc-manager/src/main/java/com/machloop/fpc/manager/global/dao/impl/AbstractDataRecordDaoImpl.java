package com.machloop.fpc.manager.global.dao.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.DateHistogramValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.Max;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.aggregations.pipeline.BucketScriptPipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.SimpleValue;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.global.dao.DataRecordDao;
import com.machloop.fpc.manager.global.dao.ElasticIndexDao;
import com.machloop.fpc.manager.global.data.AbstractDataRecordDO;
import com.machloop.fpc.manager.helper.Spl2DslHelper;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

public abstract class AbstractDataRecordDaoImpl implements DataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataRecordDaoImpl.class);

  private static final int SCALE_COUNTS = 4;

  // 只取统计结果的前1000条数据
  protected static final int TOP_SIZE = 1000;

  protected static final int COMPOSITE_BATCH_SIZE = 10000;

  protected static final String DEFAULT_SORT_FIELD = "total_bytes";
  protected static final String DEFAULT_SORT_DIRECTION = "desc";

  /**
   * @throws IOException 
   * @see com.machloop.fpc.manager.global.dao.DataRecordDao#rollup(java.util.Date, java.util.Date)
   */
  @Override
  public int rollup(final Date startTime, final Date endTime) throws IOException {

    int success = 0;

    String inputIndexPrefix;
    String outIndexPrefix;

    long intervalSeconds = (endTime.getTime() - startTime.getTime()) / 1000;
    if (intervalSeconds == Constants.FIVE_MINUTE_SECONDS) {
      // 60秒汇总到5分钟
      inputIndexPrefix = getIndexName();
      outIndexPrefix = getIndexName() + "5m-";
    } else if (intervalSeconds == Constants.ONE_HOUR_SECONDS) {
      // 5分钟汇总到1小时
      inputIndexPrefix = getIndexName() + "5m-";
      outIndexPrefix = getIndexName() + "1h-";
    } else {
      return success;
    }

    String startDayStr = DateUtils.toStringFormat(startTime, "yyyy-MM-dd", ZoneOffset.UTC);
    String inputIndex = inputIndexPrefix + startDayStr;
    String outIndex = outIndexPrefix + startDayStr;

    // 聚合前先查询是否存在当前聚合时间的脏数据，有则清除, 防止重复统计
    RangeQueryBuilder dirtyQuery = QueryBuilders.rangeQuery("timestamp").gt(startTime);
    long dirtyCount = getElasticIndexDao().countDocument(outIndex, dirtyQuery);
    if (dirtyCount > 0) {
      int clean = getElasticIndexDao().deleteDocumentSilence(outIndex, dirtyQuery);
      LOGGER.info(
          "found dirty rollup record in [{}] (after time : [{}]), start to clean, total: [{}], clean: [{}]",
          outIndex, startTime, dirtyCount, clean);
    }

    // 聚合 统计需要inputIndex
    success += aggregate(startTime, endTime, (int) intervalSeconds, inputIndex,
        new WriteCallback() {

          @Override
          public int process(List<String> result) {
            int success = 0;

            if (CollectionUtils.isNotEmpty(result)) {
              success = getElasticIndexDao().batchSaveDocument(outIndex, result);
            }
            return success;
          }

        });
    LOGGER.info(
        "finish to roll up, input index: [{}], out index: [{}], startTime: [{}], endTime: [{}], "
            + "total roll up sucess count: [{}]",
        inputIndex, outIndex, startTime, endTime, success);

    return success;
  }

  public interface WriteCallback {
    int process(List<String> result);
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.DataRecordDao#addAlias()
   */
  @Override
  public int addAlias() {
    int success = 0;
    success += getElasticIndexDao().addAlias(getIndexName() + "20*", getIndexAliasName());
    success += getElasticIndexDao().addAlias(getIndexName() + "5m-*", getIndexAliasName() + "-5m");
    success += getElasticIndexDao().addAlias(getIndexName() + "1h-*", getIndexAliasName() + "-1h");

    return success;
  }

  protected abstract ElasticIndexDao getElasticIndexDao();

  protected abstract RestHighLevelClient getRestHighLevelClient();

  protected abstract Spl2DslHelper getSpl2DslHelper();

  protected abstract String getIndexName();

  protected abstract String getIndexAliasName();

  protected abstract int aggregate(final Date startTime, final Date endTime, final int interval,
      final String indexName, WriteCallback writeCallback) throws IOException;

  protected List<String> getCombinationAggsFields(String aggsField) {
    List<String> aggsFields = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    switch (aggsField) {
      case "tcp_client_network_latency_avg":
        aggsFields = Lists.newArrayList("tcp_client_network_latency",
            "tcp_client_network_latency_counts", "tcp_client_network_latency_avg");
        break;
      case "tcp_server_network_latency_avg":
        aggsFields = Lists.newArrayList("tcp_server_network_latency",
            "tcp_server_network_latency_counts", "tcp_server_network_latency_avg");
        break;
      case "server_response_latency_avg":
        aggsFields = Lists.newArrayList("server_response_latency", "server_response_latency_counts",
            "server_response_latency_avg");
        break;
      case "tcp_client_retransmission_rate":
        aggsFields = Lists.newArrayList("tcp_client_retransmission_packets", "tcp_client_packets",
            "tcp_client_retransmission_rate");
        break;
      case "tcp_server_retransmission_rate":
        aggsFields = Lists.newArrayList("tcp_server_retransmission_packets", "tcp_server_packets",
            "tcp_server_retransmission_rate");
        break;
      case "tcp_established_fail_rate":
        aggsFields = Lists.newArrayList("tcp_established_fail_counts",
            "tcp_established_success_counts", "tcp_established_fail_rate");
        break;
      default:
        aggsFields.add(aggsField);
        break;
    }

    return aggsFields;
  }

  protected void setAggsKPIFields(Map<String, String> aggsFields) {
    aggsFields.put("total_bytes", "sum");
    aggsFields.put("total_packets", "sum");
    aggsFields.put("established_sessions", "sum");
    aggsFields.put("tcp_client_network_latency", "sum");
    aggsFields.put("tcp_client_network_latency_counts", "sum");
    aggsFields.put("tcp_server_network_latency", "sum");
    aggsFields.put("tcp_server_network_latency_counts", "sum");
    aggsFields.put("server_response_latency", "sum");
    aggsFields.put("server_response_latency_counts", "sum");
    aggsFields.put("tcp_client_retransmission_packets", "sum");
    aggsFields.put("tcp_client_packets", "sum");
    aggsFields.put("tcp_server_retransmission_packets", "sum");
    aggsFields.put("tcp_server_packets", "sum");
    aggsFields.put("tcp_established_fail_counts", "sum");
    aggsFields.put("tcp_established_success_counts", "sum");
    aggsFields.put("tcp_client_zero_window_packets", "sum");
    aggsFields.put("tcp_server_zero_window_packets", "sum");
  }

  protected void setBucketScriptAggsKPIFields(
      List<BucketScriptPipelineAggregationBuilder> bucketScriptList) {
    // clientNetworkLatencyAvg
    Map<String,
        String> cNLABucketsPathsMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    cNLABucketsPathsMap.put("tcpClientNetworkLatency", "tcp_client_network_latency");
    cNLABucketsPathsMap.put("tcpClientNetworkLatencyCounts", "tcp_client_network_latency_counts");
    BucketScriptPipelineAggregationBuilder cNLABucketScript = PipelineAggregatorBuilders
        .bucketScript("tcp_client_network_latency_avg", cNLABucketsPathsMap, new Script(
            "params.tcpClientNetworkLatencyCounts == 0 ? 0 : params.tcpClientNetworkLatency/params.tcpClientNetworkLatencyCounts"));
    bucketScriptList.add(cNLABucketScript);

    // serverNetworkLatencyAvg
    Map<String,
        String> sNLABucketsPathsMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sNLABucketsPathsMap.put("tcpServerNetworkLatency", "tcp_server_network_latency");
    sNLABucketsPathsMap.put("tcpServerNetworkLatencyCounts", "tcp_server_network_latency_counts");
    BucketScriptPipelineAggregationBuilder sNLABucketScript = PipelineAggregatorBuilders
        .bucketScript("tcp_server_network_latency_avg", sNLABucketsPathsMap, new Script(
            "params.tcpServerNetworkLatencyCounts == 0 ? 0 : params.tcpServerNetworkLatency/params.tcpServerNetworkLatencyCounts"));
    bucketScriptList.add(sNLABucketScript);

    // serverResponseLatencyAvg
    Map<String,
        String> sPTABucketsPathsMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sPTABucketsPathsMap.put("serverResponseLatency", "server_response_latency");
    sPTABucketsPathsMap.put("serverResponseLatencyCounts", "server_response_latency_counts");
    BucketScriptPipelineAggregationBuilder sPLABucketScript = PipelineAggregatorBuilders
        .bucketScript("server_response_latency_avg", sPTABucketsPathsMap, new Script(
            "params.serverResponseLatencyCounts == 0 ? 0 : params.serverResponseLatency/params.serverResponseLatencyCounts"));
    bucketScriptList.add(sPLABucketScript);

    // tcpClientRetransmissionRate
    Map<String,
        String> tCRRBucketsPathsMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    tCRRBucketsPathsMap.put("tcpClientRetransmissionPackets", "tcp_client_retransmission_packets");
    tCRRBucketsPathsMap.put("tcpClientPackets", "tcp_client_packets");
    BucketScriptPipelineAggregationBuilder tCRRBucketScript = PipelineAggregatorBuilders
        .bucketScript("tcp_client_retransmission_rate", tCRRBucketsPathsMap, new Script(
            "params.tcpClientPackets == 0 ? 0 : params.tcpClientRetransmissionPackets/params.tcpClientPackets"));
    bucketScriptList.add(tCRRBucketScript);

    // tcpServerRetransmissionRate
    Map<String,
        String> tSRRBucketsPathsMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    tSRRBucketsPathsMap.put("tcpServerRetransmissionPackets", "tcp_server_retransmission_packets");
    tSRRBucketsPathsMap.put("tcpServerPackets", "tcp_server_packets");
    BucketScriptPipelineAggregationBuilder tSRRBucketScript = PipelineAggregatorBuilders
        .bucketScript("tcp_server_retransmission_rate", tSRRBucketsPathsMap, new Script(
            "params.tcpServerPackets == 0 ? 0 : params.tcpServerRetransmissionPackets/params.tcpServerPackets"));
    bucketScriptList.add(tSRRBucketScript);
  }

  protected void tranKPIMapToDateRecord(Map<String, Object> item,
      AbstractDataRecordDO abstractDataRecordDO) {
    abstractDataRecordDO.setTimestamp((Date) item.get("timestamp"));
    abstractDataRecordDO.setNetworkId(MapUtils.getString(item, "network_id", null));
    String serviceId = MapUtils.getString(item, "service_id", null);
    abstractDataRecordDO.setServiceId(StringUtils.equals(serviceId, "null") ? null : serviceId);

    abstractDataRecordDO.setTotalBytes(MapUtils.getLongValue(item, "total_bytes"));
    abstractDataRecordDO.setTotalPackets(MapUtils.getLongValue(item, "total_packets"));
    abstractDataRecordDO
        .setEstablishedSessions(MapUtils.getLongValue(item, "established_sessions"));
    abstractDataRecordDO
        .setTcpClientNetworkLatency(MapUtils.getLongValue(item, "tcp_client_network_latency"));
    abstractDataRecordDO.setTcpClientNetworkLatencyCounts(
        MapUtils.getLongValue(item, "tcp_client_network_latency_counts"));
    abstractDataRecordDO.setTcpClientNetworkLatencyAvg(
        MapUtils.getLongValue(item, "tcp_client_network_latency_avg"));
    abstractDataRecordDO
        .setTcpServerNetworkLatency(MapUtils.getLongValue(item, "tcp_server_network_latency"));
    abstractDataRecordDO.setTcpServerNetworkLatencyCounts(
        MapUtils.getLongValue(item, "tcp_server_network_latency_counts"));
    abstractDataRecordDO.setTcpServerNetworkLatencyAvg(
        MapUtils.getLongValue(item, "tcp_server_network_latency_avg"));
    abstractDataRecordDO
        .setServerResponseLatency(MapUtils.getLongValue(item, "server_response_latency"));
    abstractDataRecordDO.setServerResponseLatencyCounts(
        MapUtils.getLongValue(item, "server_response_latency_counts"));
    abstractDataRecordDO
        .setServerResponseLatencyAvg(MapUtils.getLongValue(item, "server_response_latency_avg"));
    abstractDataRecordDO.setTcpClientRetransmissionPackets(
        MapUtils.getLongValue(item, "tcp_client_retransmission_packets"));
    abstractDataRecordDO.setTcpClientPackets(MapUtils.getLongValue(item, "tcp_client_packets"));
    abstractDataRecordDO.setTcpClientRetransmissionRate(
        MapUtils.getDoubleValue(item, "tcp_client_retransmission_rate"));
    abstractDataRecordDO.setTcpServerRetransmissionPackets(
        MapUtils.getLongValue(item, "tcp_server_retransmission_packets"));
    abstractDataRecordDO.setTcpServerPackets(MapUtils.getLongValue(item, "tcp_server_packets"));
    abstractDataRecordDO.setTcpServerRetransmissionRate(
        MapUtils.getDoubleValue(item, "tcp_server_retransmission_rate"));
    abstractDataRecordDO.setTcpClientZeroWindowPackets(
        MapUtils.getLongValue(item, "tcp_client_zero_window_packets"));
    abstractDataRecordDO.setTcpServerZeroWindowPackets(
        MapUtils.getLongValue(item, "tcp_server_zero_window_packets"));
    abstractDataRecordDO
        .setTcpEstablishedFailCounts(MapUtils.getLongValue(item, "tcp_established_fail_counts"));
    abstractDataRecordDO.setTcpEstablishedSuccessCounts(
        MapUtils.getLongValue(item, "tcp_established_success_counts"));
  }

  /**
   * 查询基础的统计数据
   * @param queryVO
   * @param indexName
   * @return
   * @throws IOException
   */
  protected List<Map<String, Object>> queryMetricDataRecord(MetricQueryVO queryVO, String indexName)
      throws IOException {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    // 过滤
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    boolQuery.filter(QueryBuilders.termQuery("network_id", queryVO.getNetworkId()));
    boolQuery.filter(QueryBuilders.termQuery("service_id",
        StringUtils.defaultIfBlank(queryVO.getServiceId(), "")));
    boolQuery.filter(getTimeRangeBuilder(Tuples.of(queryVO.getStartTimeDate(), true),
        Tuples.of(queryVO.getEndTimeDate(), true)));
    searchSourceBuilder.query(boolQuery);
    searchSourceBuilder.size(
        Integer.parseInt(HotPropertiesHelper.getProperty("rest.metric.result.query.max.count")));

    // 排序
    searchSourceBuilder.sort(SortBuilders.fieldSort("timestamp").order(SortOrder.ASC));

    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query metric data record, elastic index:[{}], query:[{}]", getIndexAliasName(),
          searchSourceBuilder.toString());
    }

    // 执行结果
    SearchResponse response = getRestHighLevelClient().search(searchRequest,
        RequestOptions.DEFAULT);

    // 无匹配结果
    if (response.getHits().equals(SearchHits.empty())) {
      return result;
    }

    for (SearchHit hit : response.getHits().getHits()) {
      Map<String, Object> map = hit.getSourceAsMap();
      map.put("timestamp", new Date(MapUtils.getLong(map, "timestamp")));

      Map<String, Object> transition = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.entrySet().forEach(entry -> {
        transition.put(TextUtils.underLineToCamel(entry.getKey()), entry.getValue());
      });
      result.add(transition);
    }

    return result;
  }

  /**
   * 使用单个字段分组，并且bucket数量不会超过10000个时使用此方法聚合，统计时间曲线
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param interval datehistogram的间隔（秒）
   * @param indexName 索引名称
   * @param termField 分组字段名
   * @param dsl 前端过滤条件
   * @param queryBuilder 附加条件
   * @param aggsFields 需要进行metric计算的字段，{"field1": "sum", "field2": "avg"}
   * @param bucketScriptList 需要特殊计算的字段
   * @param extendedBound
   * @return bucket列表 
   * 列表中中每一个bucket元素值为 {"timestamp": 时间, "{termField}": "分组字段的值", "aggsFields中的key": 计算结果}
   * @throws IOException 
   */
  protected List<Map<String, Object>> dateHistogramTermMetricAggregate(
      Tuple2<Date, Boolean> startTime, Tuple2<Date, Boolean> endTime, int interval,
      String indexName, String termField, String dsl, QueryBuilder queryBuilder,
      Map<String, String> aggsFields, List<BucketScriptPipelineAggregationBuilder> bucketScriptList,
      boolean extendedBound) throws IOException {

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 聚合不需要返回命中值
    searchSourceBuilder.size(0);

    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    // 添加过滤条件
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (StringUtils.isNotBlank(dsl)) {
      boolQuery.must(QueryBuilders.wrapperQuery(getSpl2DslHelper().converte(dsl)));
    } else {
      // 必须过滤时间
      boolQuery.must(getTimeRangeBuilder(startTime, endTime));
    }
    if (queryBuilder != null) {
      boolQuery.must(queryBuilder);
    }
    searchSourceBuilder.query(boolQuery);

    DateHistogramAggregationBuilder dateHistoAggsBuilder = AggregationBuilders
        .dateHistogram("timestamp").field("timestamp")
        .fixedInterval(DateHistogramInterval.seconds(interval)).timeZone(ZoneId.systemDefault())
        .format("epoch_millis");
    // 后端不再补点
    /*if (extendedBound) {
      dateHistoAggsBuilder.extendedBounds(
          new ExtendedBounds(startTime.getT1().getTime(), endTime.getT1().getTime() - 1));
    }*/

    TermsAggregationBuilder termBuilder = AggregationBuilders.terms(termField).field(termField);
    termBuilder.size(10000).minDocCount(1);

    for (Entry<String, String> entry : aggsFields.entrySet()) {
      termBuilder.subAggregation(buildMetricAggs(entry.getKey(), entry.getValue()));
    }
    bucketScriptList.forEach(bucketScript -> {
      termBuilder.subAggregation(bucketScript);
    });

    dateHistoAggsBuilder.subAggregation(termBuilder);
    searchSourceBuilder.aggregation(dateHistoAggsBuilder);
    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query dateHistogramTermMetricAggregate, elastic index:[{}], query:[{}]",
          indexName, searchSourceBuilder.toString());
    }

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchResponse response = getRestHighLevelClient().search(searchRequest,
        RequestOptions.DEFAULT);

    // 索引未创建时会为null
    if (response.getAggregations() == null) {
      return resultList;
    }

    boolean needToEndOfInterval = isNeedToEndOfInterval(interval, indexName);
    Histogram histogramResult = response.getAggregations().get("timestamp");
    if (CollectionUtils.isNotEmpty(histogramResult.getBuckets())) {
      for (Histogram.Bucket histogramBucket : histogramResult.getBuckets()) {
        ZonedDateTime time = (ZonedDateTime) histogramBucket.getKey();
        if (needToEndOfInterval) {
          time = time.plusSeconds(interval);
        }

        Terms termResult = histogramBucket.getAggregations().get(termField);
        if (termResult.getBuckets().isEmpty()) {
          Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          item.put("timestamp", Date.from(time.toInstant()));
          resultList.add(item);
        } else {
          for (Terms.Bucket bucket : termResult.getBuckets()) {
            Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            item.put("timestamp", Date.from(time.toInstant()));

            String key = String.valueOf(bucket.getKey());
            item.put(termField, key);

            for (Entry<String, String> entry : aggsFields.entrySet()) {
              item.put(entry.getKey(), fetchMetricAggsResult(bucket.getAggregations(),
                  entry.getKey(), entry.getValue()));
            }
            bucketScriptList.forEach(bucketScript -> {
              SimpleValue aggregation = bucket.getAggregations().get(bucketScript.getName());
              String value = aggregation == null ? "0" : aggregation.getValueAsString();
              item.put(bucketScript.getName(),
                  new BigDecimal(value).setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
            });
            resultList.add(item);
          }
        }
      }
    }

    return resultList;
  }

  /**
   * 使用单个字段分组，并且bucket数量不会超过10000个时使用此方法聚合，不统计时间曲线
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param indexName 索引名称
   * @param termField 分组字段名 
   * @param dsl 前端过滤条件
   * @param queryBuilder 附加条件
   * @param aggsFields 聚合字段
   * @param bucketScriptList 需要特殊计算的字段
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @return bucket列表 
   * 列表中中每一个bucket元素值为 {"timestamp": 时间, "{termField}": "分组字段的值", "aggsFields中的key": 计算结果}
   * @throws IOException
   */
  protected List<Map<String, Object>> termMetricAggregate(Tuple2<Date, Boolean> startTime,
      Tuple2<Date, Boolean> endTime, String indexName, String termField, String dsl,
      QueryBuilder queryBuilder, Map<String, String> aggsFields,
      List<BucketScriptPipelineAggregationBuilder> bucketScriptList, String sortProperty,
      String sortDirection) throws IOException {

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 聚合不需要返回命中值
    searchSourceBuilder.size(0);

    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    // 添加过滤条件
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (StringUtils.isNotBlank(dsl)) {
      boolQuery.must(QueryBuilders.wrapperQuery(getSpl2DslHelper().converte(dsl)));
    } else {
      // 必须过滤时间
      boolQuery.must(getTimeRangeBuilder(startTime, endTime));
    }
    if (queryBuilder != null) {
      boolQuery.must(queryBuilder);
    }
    searchSourceBuilder.query(boolQuery);

    TermsAggregationBuilder termBuilder = AggregationBuilders.terms(termField).field(termField);
    termBuilder.size(10000).minDocCount(1);
    termBuilder
        .subAggregation(PipelineAggregatorBuilders.bucketSort("metric_sort", Lists.newArrayList(
            new FieldSortBuilder(sortProperty).order(SortOrder.fromString(sortDirection)))));

    for (Entry<String, String> entry : aggsFields.entrySet()) {
      termBuilder.subAggregation(buildMetricAggs(entry.getKey(), entry.getValue()));
    }
    bucketScriptList.forEach(bucketScript -> {
      termBuilder.subAggregation(bucketScript);
    });

    searchSourceBuilder.aggregation(termBuilder);
    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query termMetricAggregate, elastic index:[{}], query:[{}]", indexName,
          searchSourceBuilder.toString());
    }

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchResponse response = getRestHighLevelClient().search(searchRequest,
        RequestOptions.DEFAULT);

    // 索引未创建时会为null
    if (response.getAggregations() == null) {
      return resultList;
    }
    Terms termResult = response.getAggregations().get(termField);
    for (Terms.Bucket bucket : termResult.getBuckets()) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

      String key = String.valueOf(bucket.getKey());
      item.put(termField, key);

      for (Entry<String, String> entry : aggsFields.entrySet()) {
        item.put(entry.getKey(),
            fetchMetricAggsResult(bucket.getAggregations(), entry.getKey(), entry.getValue()));
      }
      bucketScriptList.forEach(bucketScript -> {
        SimpleValue aggregation = bucket.getAggregations().get(bucketScript.getName());
        String value = aggregation == null ? "0" : aggregation.getValueAsString();
        item.put(bucketScript.getName(),
            new BigDecimal(value).setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      });
      resultList.add(item);
    }

    return resultList;
  }

  /**
   * 不分组，并且bucket数量不会超过10000个时使用此方法聚合，统计时间曲线
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param interval datehistogram的间隔（秒）
   * @param indexName 索引名称
   * @param dsl 过滤条件
   * @param queryBuilder 附加条件
   * @param aggsFields 需要进行metric计算的字段，{"field1": "sum", "field2": "avg"}
   * @param bucketScriptList 需要特殊计算的字段
   * @param extendedBound
   * @return bucket列表 
   * 列表中中每一个bucket元素值为 {"timestamp": 时间, "{termField}": "分组字段的值", "aggsFields中的key": 计算结果}
   * @throws IOException
   */
  protected List<Map<String, Object>> dateHistogramMetricAggregate(Tuple2<Date, Boolean> startTime,
      Tuple2<Date, Boolean> endTime, int interval, String indexName, String dsl,
      QueryBuilder queryBuilder, Map<String, String> aggsFields,
      List<BucketScriptPipelineAggregationBuilder> bucketScriptList, boolean extendedBound)
      throws IOException {

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 聚合不需要返回命中值
    searchSourceBuilder.size(0);

    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    // 添加过滤条件
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (StringUtils.isNotBlank(dsl)) {
      boolQuery.must(QueryBuilders.wrapperQuery(getSpl2DslHelper().converte(dsl)));
    } else {
      // 必须过滤时间
      boolQuery.must(getTimeRangeBuilder(startTime, endTime));
    }
    if (queryBuilder != null) {
      boolQuery.must(queryBuilder);
    }
    searchSourceBuilder.query(boolQuery);

    DateHistogramAggregationBuilder dateHistoAggsBuilder = AggregationBuilders
        .dateHistogram("timestamp").field("timestamp")
        .fixedInterval(DateHistogramInterval.seconds(interval)).timeZone(ZoneId.systemDefault())
        .format("epoch_millis");
    // 后端不再补点
    /*if (extendedBound) {
      dateHistoAggsBuilder.extendedBounds(
          new ExtendedBounds(startTime.getT1().getTime(), endTime.getT1().getTime() - 1));
    }*/

    for (Entry<String, String> entry : aggsFields.entrySet()) {
      dateHistoAggsBuilder.subAggregation(buildMetricAggs(entry.getKey(), entry.getValue()));
    }
    bucketScriptList.forEach(bucketScript -> {
      dateHistoAggsBuilder.subAggregation(bucketScript);
    });

    searchSourceBuilder.aggregation(dateHistoAggsBuilder);
    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query dateHistogramMetricAggregate, elastic index:[{}], query:[{}]", indexName,
          searchSourceBuilder.toString());
    }

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchResponse response = getRestHighLevelClient().search(searchRequest,
        RequestOptions.DEFAULT);

    // 索引未创建时会为null
    if (response.getAggregations() == null) {
      return resultList;
    }

    boolean needToEndOfInterval = isNeedToEndOfInterval(interval, indexName);
    Histogram histogramResult = response.getAggregations().get("timestamp");
    if (CollectionUtils.isNotEmpty(histogramResult.getBuckets())) {
      for (Histogram.Bucket bucket : histogramResult.getBuckets()) {
        Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

        ZonedDateTime time = (ZonedDateTime) bucket.getKey();
        if (needToEndOfInterval) {
          time = time.plusSeconds(interval);
        }
        item.put("timestamp", Date.from(time.toInstant()));

        for (Entry<String, String> entry : aggsFields.entrySet()) {
          item.put(entry.getKey(),
              fetchMetricAggsResult(bucket.getAggregations(), entry.getKey(), entry.getValue()));
        }
        bucketScriptList.forEach(bucketScript -> {
          SimpleValue aggregation = bucket.getAggregations().get(bucketScript.getName());
          String value = aggregation == null ? "0" : aggregation.getValueAsString();
          item.put(bucketScript.getName(),
              new BigDecimal(value).setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
        });
        resultList.add(item);
      }
    }

    return resultList;
  }

  /**
   * 使用多个字段组合分组，bucket数量会超过10000个时使用此方法聚合，传入batchSize指定每次聚合的桶数
   * 要获取全部bucket需要多次调用此方法，调用时传入上一次返回结果的afterKey（Tuple T1），第一次调用afterKey为null
   * 返回的List（Tuple T2）为bucket结果，长度小于batchSize时表示全部结果返回完
   * 
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param interval datehistogram的间隔（秒）
   * @param indexName 索引名称
   * @param termFields 分组字段集合 <字段名,字段值为null时是否统计：true(统计)/false(忽略)>
   * @param dsl 前端过滤条件
   * @param queryBuilder 附加条件
   * @param aggsFields 需要进行metric计算的字段，{"field1": "sum", "field2": "avg"}
   * @param bucketScriptList 需要特殊计算的字段
   * @param batchSize 每次返回的bucket数量
   * @param afterKey afterKey
   * @return (afertKey, bucket列表), 
   * 列表中中每一个bucket元素值为 {"timestamp": 时间, "{termField}": "分组字段的值", "aggsFields中的key": 计算结果}
   * @throws IOException
   */
  protected Tuple2<Map<String, Object>,
      List<Map<String, Object>>> compositeDateHistogramTermMetricAggregate(
          Tuple2<Date, Boolean> startTime, Tuple2<Date, Boolean> endTime, int interval,
          String indexName, List<Tuple2<String, Boolean>> termFields, String dsl,
          QueryBuilder queryBuilder, Map<String, String> aggsFields,
          List<BucketScriptPipelineAggregationBuilder> bucketScriptList, int batchSize,
          Map<String, Object> afterKey) throws IOException {
    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 聚合不需要返回命中值
    searchSourceBuilder.size(0);

    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    // 添加过滤条件
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (StringUtils.isNotBlank(dsl)) {
      boolQuery.must(QueryBuilders.wrapperQuery(getSpl2DslHelper().converte(dsl)));
    } else {
      boolQuery.must(getTimeRangeBuilder(startTime, endTime));
    }
    if (queryBuilder != null) {
      boolQuery.must(queryBuilder);
    }
    searchSourceBuilder.query(boolQuery);

    ArrayList<CompositeValuesSourceBuilder<?>> list = Lists
        .newArrayList(new DateHistogramValuesSourceBuilder("timestamp").field("timestamp")
            .fixedInterval(DateHistogramInterval.seconds(interval)).timeZone(ZoneId.systemDefault())
            .format("epoch_millis"));
    list.addAll(termFields
        .stream().map(termField -> new TermsValuesSourceBuilder(termField.getT1())
            .field(termField.getT1()).missingBucket(termField.getT2()))
        .collect(Collectors.toList()));
    CompositeAggregationBuilder composite = AggregationBuilders.composite("composite", list);
    for (Entry<String, String> entry : aggsFields.entrySet()) {
      composite.subAggregation(buildMetricAggs(entry.getKey(), entry.getValue()));
    }
    for (BucketScriptPipelineAggregationBuilder bucketScript : bucketScriptList) {
      composite.subAggregation(bucketScript);
    }
    composite.size(batchSize);
    if (afterKey != null) {
      composite.aggregateAfter(afterKey);
    }

    searchSourceBuilder.aggregation(composite);
    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "query compositeDateHistogramTermMetricAggregate, elastic index:[{}], query:[{}]",
          indexName, searchSourceBuilder.toString());
    }

    SearchResponse response = getRestHighLevelClient().search(searchRequest,
        RequestOptions.DEFAULT);

    // 索引未创建时会为null
    if (response.getAggregations() == null) {
      return Tuples.of(Maps.newHashMapWithExpectedSize(0), resultList);
    }

    boolean needToEndOfInterval = isNeedToEndOfInterval(interval, indexName);
    CompositeAggregation compositeAggs = response.getAggregations().get("composite");
    for (CompositeAggregation.Bucket compositeBucket : compositeAggs.getBuckets()) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Map<String, Object> key = compositeBucket.getKey();
      Date time = new Date(Long.valueOf((String) key.get("timestamp")));
      if (needToEndOfInterval) {
        time = DateUtils.afterSecondDate(time, interval);
      }
      item.put("timestamp", time);

      termFields.forEach(termField -> {
        item.put(termField.getT1(), String.valueOf(key.get(termField.getT1())));
      });

      for (Entry<String, String> entry : aggsFields.entrySet()) {
        item.put(entry.getKey(), fetchMetricAggsResult(compositeBucket.getAggregations(),
            entry.getKey(), entry.getValue()));
      }
      bucketScriptList.forEach(bucketScript -> {
        SimpleValue aggregation = compositeBucket.getAggregations().get(bucketScript.getName());
        String value = aggregation == null ? "0" : aggregation.getValueAsString();
        item.put(bucketScript.getName(),
            new BigDecimal(value).setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      });
      resultList.add(item);
    }

    return Tuples.of(compositeAggs.afterKey() == null ? Maps.newHashMapWithExpectedSize(0)
        : compositeAggs.afterKey(), resultList);
  }

  /**
   * @param startTime 开始时间
   * @param endTime 结束时间
   * @param indexName 索引名称
   * @param termFields 分组字段集合 <字段名,字段值为null时是否统计：true(统计)/false(忽略)>
   * @param dsl 前端过滤条件
   * @param queryBuilder 附加条件
   * @param aggsFields 需要进行metric计算的字段，{"field1": "sum", "field2": "avg"}
   * @param bucketScriptList 需要特殊计算的字段
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @param batchSize 每次返回的bucket数量
   * @param afterKey afterKey
   * @return (afertKey, bucket列表), 
   * 列表中中每一个bucket元素值为 {"timestamp": 时间, "{termField}": "分组字段的值", "aggsFields中的key": 计算结果}
   * @throws IOException
   */
  protected Tuple2<Map<String, Object>, List<Map<String, Object>>> compositeTermMetricAggregate(
      Tuple2<Date, Boolean> startTime, Tuple2<Date, Boolean> endTime, String indexName,
      List<Tuple2<String, Boolean>> termFields, String dsl, QueryBuilder queryBuilder,
      Map<String, String> aggsFields, List<BucketScriptPipelineAggregationBuilder> bucketScriptList,
      String sortProperty, String sortDirection, int batchSize, Map<String, Object> afterKey)
      throws IOException {
    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 聚合不需要返回命中值
    searchSourceBuilder.size(0);

    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    // 添加过滤条件
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    if (StringUtils.isNotBlank(dsl)) {
      boolQuery.must(QueryBuilders.wrapperQuery(getSpl2DslHelper().converte(dsl)));
    } else {
      boolQuery.must(getTimeRangeBuilder(startTime, endTime));
    }
    if (queryBuilder != null) {
      boolQuery.must(queryBuilder);
    }
    searchSourceBuilder.query(boolQuery);

    ArrayList<
        CompositeValuesSourceBuilder<?>> list = Lists.newArrayListWithCapacity(termFields.size());
    list.addAll(termFields
        .stream().map(termField -> new TermsValuesSourceBuilder(termField.getT1())
            .field(termField.getT1()).missingBucket(termField.getT2()))
        .collect(Collectors.toList()));

    CompositeAggregationBuilder composite = AggregationBuilders.composite("composite", list);

    for (Entry<String, String> entry : aggsFields.entrySet()) {
      composite.subAggregation(buildMetricAggs(entry.getKey(), entry.getValue()));
    }
    bucketScriptList.forEach(bucketScript -> {
      composite.subAggregation(bucketScript);
    });

    // metric结果排序
    composite
        .subAggregation(PipelineAggregatorBuilders.bucketSort("metric_sort", Lists.newArrayList(
            new FieldSortBuilder(sortProperty).order(SortOrder.fromString(sortDirection)))));

    composite.size(batchSize);
    if (afterKey != null) {
      composite.aggregateAfter(afterKey);
    }

    searchSourceBuilder.aggregation(composite);
    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query compositeTermMetricAggregate, elastic index:[{}], query:[{}]", indexName,
          searchSourceBuilder.toString());
    }

    SearchResponse response = getRestHighLevelClient().search(searchRequest,
        RequestOptions.DEFAULT);

    // 索引未创建时会为null
    if (response.getAggregations() == null) {
      return Tuples.of(Maps.newHashMapWithExpectedSize(0), resultList);
    }
    CompositeAggregation compositeAggs = response.getAggregations().get("composite");
    for (CompositeAggregation.Bucket compositeBucket : compositeAggs.getBuckets()) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Map<String, Object> key = compositeBucket.getKey();
      termFields.forEach(termField -> {
        item.put(termField.getT1(), String.valueOf(key.get(termField.getT1())));
      });

      for (Entry<String, String> entry : aggsFields.entrySet()) {
        item.put(entry.getKey(), fetchMetricAggsResult(compositeBucket.getAggregations(),
            entry.getKey(), entry.getValue()));
      }
      bucketScriptList.forEach(bucketScript -> {
        SimpleValue aggregation = compositeBucket.getAggregations().get(bucketScript.getName());
        String value = aggregation == null ? "0" : aggregation.getValueAsString();
        item.put(bucketScript.getName(),
            new BigDecimal(value).setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      });
      resultList.add(item);
    }
    return Tuples.of(compositeAggs.afterKey() == null ? Maps.newHashMapWithExpectedSize(0)
        : compositeAggs.afterKey(), resultList);
  }

  /**
   * 聚合数据（1m -> 5m, 5m -> 1h）
   * @param startTime 开始时间（不包含）
   * @param endTime 结束时间（包含）
   * @param indexName 索引名称
   * @param termFields 分组字段集合 <字段名,字段值为null时是否统计：true(统计)/false(忽略)>
   * @param aggsFields 需要进行metric计算的字段，{"field1": "sum", "field2": "avg"}
   * @param batchSize 每次返回的bucket数量
   * @param afterKey afterKey
   * @return (afertKey, bucket列表), 
   * 列表中中每一个bucket元素值为 {"timestamp": 时间, "{termField}": "分组字段的值", "aggsFields中的key": 计算结果}
   * @throws IOException
   */
  protected Tuple2<Map<String, Object>, List<Map<String, Object>>> compositeTermMetricAggregate(
      Date startTime, Date endTime, String indexName, List<Tuple2<String, Boolean>> termFields,
      Map<String, String> aggsFields, int batchSize, Map<String, Object> afterKey)
      throws IOException {
    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 聚合不需要返回命中值
    searchSourceBuilder.size(0);

    SearchRequest searchRequest = new SearchRequest(indexName);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    // 添加时间过滤条件
    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("timestamp");
    rangeQueryBuilder.format("epoch_millis");
    rangeQueryBuilder.from(startTime.getTime(), false);
    rangeQueryBuilder.to(endTime.getTime(), true);
    searchSourceBuilder.query(rangeQueryBuilder);

    ArrayList<
        CompositeValuesSourceBuilder<?>> list = Lists.newArrayListWithCapacity(termFields.size());
    list.addAll(termFields
        .stream().map(termField -> new TermsValuesSourceBuilder(termField.getT1())
            .field(termField.getT1()).missingBucket(termField.getT2()))
        .collect(Collectors.toList()));

    CompositeAggregationBuilder composite = AggregationBuilders.composite("composite", list);

    for (Entry<String, String> entry : aggsFields.entrySet()) {
      composite.subAggregation(buildMetricAggs(entry.getKey(), entry.getValue()));
    }

    composite.size(batchSize);

    if (afterKey != null) {
      composite.aggregateAfter(afterKey);
    }

    searchSourceBuilder.aggregation(composite);
    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("merage data, elastic index:[{}], query:[{}]", indexName,
          searchSourceBuilder.toString());
    }

    SearchResponse response = getRestHighLevelClient().search(searchRequest,
        RequestOptions.DEFAULT);

    // 索引未创建时会为null
    if (response.getAggregations() == null) {
      return Tuples.of(Maps.newHashMapWithExpectedSize(0), resultList);
    }
    CompositeAggregation compositeAggs = response.getAggregations().get("composite");
    for (CompositeAggregation.Bucket compositeBucket : compositeAggs.getBuckets()) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Map<String, Object> key = compositeBucket.getKey();
      termFields.forEach(termField -> {
        item.put(termField.getT1(), String.valueOf(key.get(termField.getT1())));
      });

      for (Entry<String, String> entry : aggsFields.entrySet()) {
        item.put(entry.getKey(), fetchMetricAggsResult(compositeBucket.getAggregations(),
            entry.getKey(), entry.getValue()));
      }

      resultList.add(item);
    }
    return Tuples.of(compositeAggs.afterKey() == null ? Maps.newHashMapWithExpectedSize(0)
        : compositeAggs.afterKey(), resultList);
  }

  /**
   * @param timeBegin
   * @param timeEnd
   * @return
   */
  protected RangeQueryBuilder getTimeRangeBuilder(Tuple2<Date, Boolean> startTime,
      Tuple2<Date, Boolean> endTime) {
    // 时间
    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("timestamp");
    rangeQueryBuilder.format("epoch_millis");
    if (startTime.getT1() != null) {
      rangeQueryBuilder.from(startTime.getT1().getTime(), startTime.getT2());
    }
    if (endTime.getT1() != null) {
      rangeQueryBuilder.to(endTime.getT1().getTime(), endTime.getT2());
    }
    return rangeQueryBuilder;
  }

  /**
   * @param sourceType 数据源类型
   * @param interval 查询时间间隔
   * @param packetFileId 如果数据源为数据包文件时需要使用该参数拼接完整的索引名称
   * @return
   * @throws IOException 
   */
  protected String convertIndexAlias(String sourceType, int interval, String packetFileId)
      throws IOException {
    if (StringUtils.equals(sourceType, FpcConstants.SOURCE_TYPE_PACKET_FILE)) {
      String packetFileIndexName = String.join("", getIndexName(), packetFileId);

      // 判断离线数据包文件的分析结果是否存在
      boolean exists = getRestHighLevelClient().indices()
          .exists(new GetIndexRequest(packetFileIndexName), RequestOptions.DEFAULT);
      if (!exists) {
        LOGGER.warn("search failed, offline packet file metric data not found: {}",
            packetFileIndexName);
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "离线分析结果异常，未查找到流量分析数据");
      }

      return packetFileIndexName;
    }

    String index = getIndexAliasName();
    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      index = getIndexAliasName() + "-5m";
    } else if (interval >= Constants.ONE_HOUR_SECONDS) {
      index = getIndexAliasName() + "-1h";
    }

    return index;
  }

  /**
   * 查询时间间隔大于数据自身时间间隔时，需要取查询时间间隔内的结束时间作为查询结果的记录时间（引擎端上报的时间为结束时间）
   * @param interval
   * @param tableName
   * @return
   */
  private boolean isNeedToEndOfInterval(int interval, String indexName) {
    int currentTableInteval = Constants.ONE_MINUTE_SECONDS;
    if (StringUtils.contains(indexName, "-5m")) {
      currentTableInteval = Constants.FIVE_MINUTE_SECONDS;
    } else if (StringUtils.contains(indexName, "-1h")) {
      currentTableInteval = Constants.ONE_HOUR_SECONDS;
    }

    return interval > currentTableInteval;
  }

  /**
   * @param interval
   * @return
   */
  protected int calculateGap(int interval) {
    int gap = 1;

    if (interval == Constants.FIVE_MINUTE_SECONDS) {
      // 60秒汇总到5分钟
      gap = interval / Constants.ONE_MINUTE_SECONDS;
    } else if (interval == Constants.ONE_HOUR_SECONDS) {
      // 5分钟汇总到1小时
      gap = interval / Constants.FIVE_MINUTE_SECONDS;
    }

    return gap;
  }

  /**
   * @param result
   * @param sortProperty
   * @param sortDirection
   */
  protected List<Map<String, Object>> sortMetricResult(List<Map<String, Object>> result,
      String sortProperty, String sortDirection) {
    if (result.size() <= TOP_SIZE) {
      return result;
    }

    // 排序
    result.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        String sortField = sortProperty;
        if (!o1.containsKey(sortField)) {
          sortField = DEFAULT_SORT_FIELD;
        }
        Long o1Value = MapUtils.getLongValue(o1, sortField);
        Long o2Value = MapUtils.getLongValue(o2, sortField);

        return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
            ? o1Value.compareTo(o2Value)
            : o2Value.compareTo(o1Value);
      }
    });

    // 截取数据
    return result.subList(0, TOP_SIZE);
  }

  private AggregationBuilder buildMetricAggs(String field, String type) {
    AggregationBuilder aggs;
    switch (type) {
      case "sum":
        aggs = AggregationBuilders.sum(field).field(field);
        break;
      case "avg":
        aggs = AggregationBuilders.avg(field).field(field);
        break;
      case "max":
        aggs = AggregationBuilders.max(field).field(field);
        break;
      default:
        throw new UnsupportedOperationException();
    }
    return aggs;
  }

  private long fetchMetricAggsResult(Aggregations aggs, String field, String type) {
    long result = 0;
    switch (type) {
      case "sum":
        Sum sum = aggs.get(field);
        result = Math.round(Double.isInfinite(sum.getValue()) ? 0 : sum.getValue());
        break;
      case "avg":
        Avg avg = aggs.get(field);
        result = Math.round(Double.isInfinite(avg.getValue()) ? 0 : avg.getValue());
        break;
      case "max":
        Max max = aggs.get(field);
        result = Math.round(Double.isInfinite(max.getValue()) ? 0 : max.getValue());
        break;
      default:
        result = 0;
        break;
    }
    return result;
  }

}
