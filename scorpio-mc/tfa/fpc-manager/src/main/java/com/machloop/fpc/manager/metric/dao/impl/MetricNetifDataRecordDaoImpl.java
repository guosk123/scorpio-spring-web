package com.machloop.fpc.manager.metric.dao.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
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
import com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricNetifDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2020年12月4日, fpc-manager
 */
// @Repository
public class MetricNetifDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricNetifDataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricNetifDataRecordDaoImpl.class);

  private static final String TERM_FIELD = "netif_name";

  private static final List<Tuple2<String, Boolean>> AGGS_TERM_FIELD = Lists
      .newArrayList(Tuples.of("network_id", false), Tuples.of("netif_name", false));

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  @Autowired
  private ElasticIndexDao elasticIndexDao;

  @Autowired
  private Spl2DslHelper spl2DslHelper;

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao#queryMetricNetifHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, boolean)
   */
  @Override
  public List<MetricNetifDataRecordDO> queryMetricNetifHistograms(MetricQueryVO queryVO,
      String netifName, boolean extendedBound) {
    List<MetricNetifDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      Map<String, String> aggsFields = getAggsFields();

      TermsQueryBuilder queryBuilder = StringUtils.isBlank(netifName) ? null
          : QueryBuilders.termsQuery(TERM_FIELD, new String[]{netifName});
      List<Map<String, Object>> aggsResult = dateHistogramTermMetricAggregate(
          Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
          Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()), queryVO.getInterval(),
          convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          TERM_FIELD, queryVO.getDsl(), queryBuilder, aggsFields, Lists.newArrayList(),
          extendedBound);
      aggsResult.forEach(item -> result.add(tranResultMapToDateRecord(TERM_FIELD, item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query netif histogram.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao#queryMetricNetifs(java.util.Date)
   */
  @Override
  public List<MetricNetifDataRecordDO> queryMetricNetifs(Date afterTime) {
    throw new UnsupportedOperationException();
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao#queryNetifLatestState(java.lang.String)
   */
  @Override
  public Map<String, Object> queryNetifLatestState(String netifName) {
    Map<String, Object> result = null;

    try {
      SearchRequest searchRequest = new SearchRequest(getIndexAliasName());
      searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

      SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
      // 只返回最新上报的数据
      searchSourceBuilder.size(1);

      // 过滤
      searchSourceBuilder.fetchSource(new String[]{"timestamp", "total_bytes", "transmit_bytes"},
          null);
      if (StringUtils.isNotBlank(netifName)) {
        searchSourceBuilder.query(QueryBuilders.termQuery(TERM_FIELD, netifName));
      }
      // 排序
      searchSourceBuilder.sort(SortBuilders.fieldSort("timestamp").order(SortOrder.DESC));
      searchRequest.source(searchSourceBuilder);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("query netifLatestState, elastic index:[{}], query:[{}]", getIndexAliasName(),
            searchSourceBuilder.toString());
      }

      // 执行结果
      SearchResponse response = getRestHighLevelClient().search(searchRequest,
          RequestOptions.DEFAULT);

      // 无匹配结果
      if (response.getHits().equals(SearchHits.empty())) {
        return result;
      }

      if (response.getHits().getHits().length > 0) {
        result = response.getHits().getHits()[0].getSourceAsMap();
      }
    } catch (IOException e) {
      LOGGER.warn("failed to query metric netif.", e);
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
    return ManagerConstants.INDEX_METRIC_NETIF_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexAliasName()
   */
  @Override
  protected String getIndexAliasName() {
    return ManagerConstants.ALIAS_METRIC_NETIF_DATA_RECORD;
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
        result.add(JsonHelper.serialize(tranResultMapToDateRecord(TERM_FIELD, item)));
      });
      success += writeCallback.process(result);
    } while (batchList.size() == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());

    return success;
  }

  private Map<String, String> getAggsFields() {
    Map<String, String> aggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    aggsFields.put("total_bytes", "sum");
    aggsFields.put("total_packets", "sum");
    aggsFields.put("downstream_bytes", "sum");
    aggsFields.put("downstream_packets", "sum");
    aggsFields.put("upstream_bytes", "sum");
    aggsFields.put("upstream_packets", "sum");
    aggsFields.put("transmit_bytes", "sum");
    aggsFields.put("transmit_packets", "sum");

    return aggsFields;
  }

  private MetricNetifDataRecordDO tranResultMapToDateRecord(String termField,
      Map<String, Object> item) {
    MetricNetifDataRecordDO recordDO = new MetricNetifDataRecordDO();
    recordDO.setTimestamp((Date) item.get("timestamp"));
    recordDO.setNetifName(MapUtils.getString(item, termField));
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
  }

}
