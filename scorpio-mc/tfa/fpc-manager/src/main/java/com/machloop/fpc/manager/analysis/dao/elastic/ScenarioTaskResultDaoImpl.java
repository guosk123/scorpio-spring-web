package com.machloop.fpc.manager.analysis.dao.elastic;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Sum;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月18日, fpc-manager
 */
// @Repository
public class ScenarioTaskResultDaoImpl implements ScenarioTaskResultDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioTaskResultDaoImpl.class);

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao#queryScenarioTaskResults(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<Map<String, Object>> queryScenarioTaskResults(Pageable page, String taskId,
      String index, String query) {

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    SearchRequest searchRequest = new SearchRequest(index);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    // 分页
    searchSourceBuilder.from(page.getOffset());
    searchSourceBuilder.size(page.getPageSize());

    // 不返回record id
    searchSourceBuilder.fetchSource(null, "record_id_list");

    // 按照字段排序
    Iterator<Order> orderIterator = page.getSort().iterator();
    while (orderIterator.hasNext()) {
      Order order = orderIterator.next();
      searchSourceBuilder.sort(order.getProperty(),
          SortOrder.fromString(order.getDirection().name()));
    }

    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    boolQueryBuilder.filter(QueryBuilders.termQuery("task_id", taskId));

    if (StringUtils.isNotBlank(query)) {
      Map<String, Object> queryMap = JsonHelper.deserialize(query,
          new TypeReference<Map<String, Object>>() {
          });
      queryMap.entrySet().forEach(entry -> boolQueryBuilder
          .filter(QueryBuilders.termQuery(entry.getKey(), entry.getValue())));
    }

    searchSourceBuilder.query(boolQueryBuilder);
    searchSourceBuilder.trackTotalHits(true);
    searchRequest.source(searchSourceBuilder);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query scenario task result, elastic index:[{}], query:[{}]", index,
          searchSourceBuilder.toString());
    }

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    long total = 0L;
    try {
      SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
      for (SearchHit hit : response.getHits()) {
        Map<String, Object> source = hit.getSourceAsMap();
        source.put("id", hit.getId());
        resultList.add(hit.getSourceAsMap());
      }

      total = response.getHits().getTotalHits().value;

    } catch (IOException e) {
      LOGGER.warn("error when querying {}", index, e);
    }

    return new PageImpl<Map<String, Object>>(resultList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao#queryScenarioTaskTermsResults(com.machloop.alpha.common.base.page.Sort, java.lang.String, int, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryScenarioTaskTermsResults(Sort sort, String termField,
      int termSize, String taskId, String index) {

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    SearchRequest searchRequest = new SearchRequest(index);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    QueryBuilder queryBuilder = QueryBuilders.termQuery("task_id", taskId);

    searchSourceBuilder.query(queryBuilder);
    searchSourceBuilder.size(0);

    TermsAggregationBuilder termsBuilder = AggregationBuilders.terms(termField).field(termField)
        .minDocCount(1).size(termSize).order(BucketOrder.count(false));
    termsBuilder
        .subAggregation(AggregationBuilders.sum("record_total_hit").field("record_total_hit"));
    if (sort.getOrderFor("record_total_hit") != null) {
      termsBuilder.order(BucketOrder.aggregation("record_total_hit",
          sort.getOrderFor("record_total_hit").isAscending()));
    } else if (sort.getOrderFor("count") != null) {
      termsBuilder.order(BucketOrder.count(sort.getOrderFor("count").isAscending()));
    }
    searchSourceBuilder.aggregation(termsBuilder);

    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query scenario task terms aggs results, elastic index:[{}], query:[{}]", index,
          searchSourceBuilder.toString());
    }
    try {
      SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("elasticsearch response: [{}]", response.toString());
      }
      Terms terms = response.getAggregations().get(termField);
      for (Terms.Bucket bucket : terms.getBuckets()) {
        String key = bucket.getKeyAsString();
        long count = bucket.getDocCount();
        Sum recordTotalHit = bucket.getAggregations().get("record_total_hit");
        long totalHit = (long) recordTotalHit.getValue();
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(3);
        map.put(termField, key);
        map.put("count", count);
        map.put("record_total_hit", totalHit);
        result.add(map);
      }
    } catch (IOException e) {
      LOGGER.warn("fail to query scenario task terms aggs.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao#queryScenarioTaskResult(java.lang.String, java.lang.String)
   */
  @Override
  public Map<String, Object> queryScenarioTaskResult(String taskResultId, String index) {

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(1);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    SearchRequest searchRequest = new SearchRequest(index);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    boolQueryBuilder.filter(QueryBuilders.termQuery("_id", taskResultId));

    searchSourceBuilder.query(boolQueryBuilder);
    searchRequest.source(searchSourceBuilder);

    try {
      SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
      if (response.getHits().getTotalHits().value > 0) {
        result = response.getHits().getAt(0).getSourceAsMap();
        result.put("id", response.getHits().getAt(0).getId());
      }
    } catch (IOException e) {
      LOGGER.warn("error when querying {}", index, e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao#deleteScenarioTaskTermsResults(java.lang.String)
   */
  @Override
  public void deleteScenarioTaskTermsResults(String taskId, String index) {
    DeleteByQueryRequest deleteByQueryRequest = new DeleteByQueryRequest(index);
    deleteByQueryRequest.setIndicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
    QueryBuilder queryBuilder = QueryBuilders.termQuery("task_id", taskId);
    deleteByQueryRequest.setQuery(queryBuilder);
    try {
      restHighLevelClient.deleteByQuery(deleteByQueryRequest, RequestOptions.DEFAULT);
    } catch (IOException e) {
      LOGGER.warn("fail to delete scenario task results, task id: [{}], index: [{}].",
          StringEscapeUtils.unescapeEcmaScript(taskId), index, e);
    } catch (ElasticsearchStatusException ese) {
      if (ese.status() == RestStatus.NOT_FOUND) {
        LOGGER.debug("index not fount, {}", index);
      } else {
        LOGGER.warn("failed to delete scenario task terms results, {}", ese);
      }
    }
  }
}
