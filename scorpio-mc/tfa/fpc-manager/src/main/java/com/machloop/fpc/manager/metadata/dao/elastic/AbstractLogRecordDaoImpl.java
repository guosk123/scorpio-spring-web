package com.machloop.fpc.manager.metadata.dao.elastic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.metadata.data.AbstractLogRecordDO;
import com.machloop.fpc.manager.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * @author liyongjun
 *
 * create at 2019年9月24日, fpc-manager
 */
public abstract class AbstractLogRecordDaoImpl<DO extends AbstractLogRecordDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLogRecordDaoImpl.class);

  private static List<String> IP_FIELD_NAMES = Lists.newArrayList("src_ip", "dest_ip");

  private static List<String> PORT_FIELD_NAMES = Lists.newArrayList("src_port", "dest_port");

  /**
   * @param queryVO
   * @param page
   * @return
   */
  public Page<DO> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds, Pageable page) {

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    SearchRequest searchRequest = new SearchRequest(getIndexName());

    // 分页
    searchSourceBuilder.from(page.getOffset());
    searchSourceBuilder.size(page.getPageSize());

    // 按照字段排序
    Iterator<Order> orderIterator = page.getSort().iterator();
    while (orderIterator.hasNext()) {
      Order order = orderIterator.next();
      searchSourceBuilder.sort(order.getProperty(),
          SortOrder.fromString(order.getDirection().name()));
    }

    // 过滤时间
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    if (queryVO.getStartTimeDate() != null && queryVO.getEndTimeDate() != null) {
      boolQueryBuilder
          .filter(getContainTimeRangeBuilder(queryVO.getStartTimeDate(), queryVO.getEndTimeDate()));
    }

    // 多字段匹配
    if (StringUtils.isNotBlank(queryVO.getKeyword())) {
      // 通配符方式
      if (ArrayUtils.isNotEmpty(getWildcardSearchFieldNames(queryVO.getKeyword()))
          && (StringUtils.containsAny(StringUtils.remove(queryVO.getKeyword(), "\\?"), "?")
              || StringUtils.containsAny(StringUtils.remove(queryVO.getKeyword(), "\\*"), "*"))) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        for (String field : getWildcardSearchFieldNames(queryVO.getKeyword())) {
          boolQuery.should(QueryBuilders.wildcardQuery(field, queryVO.getKeyword()));
        }
        boolQueryBuilder.filter(boolQuery);
      } else {
        MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders
            .multiMatchQuery(queryVO.getKeyword(), getSearchFieldNames(queryVO.getKeyword()));
        boolQueryBuilder.filter(multiMatchQueryBuilder);
      }
    }

    if (queryVO.getDecrypted() != null) {
      boolQueryBuilder.filter(QueryBuilders.termsQuery("decrypted", queryVO.getDecrypted()));
    }

    // id查询
    if (StringUtils.isNotBlank(queryVO.getId())) {
      boolQueryBuilder
          .filter(QueryBuilders.termsQuery("_id", CsvUtils.convertCSVToSet(queryVO.getId())));
    }

    searchSourceBuilder.query(boolQueryBuilder);
    searchRequest.source(searchSourceBuilder);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query log records, elastic index:[{}], query:[{}]", getIndexName(),
          searchSourceBuilder.toString());
    }

    List<DO> logDOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    long total = 0L;
    try {
      SearchResponse response = getRestHighLevelClient().search(searchRequest,
          RequestOptions.DEFAULT);
      for (SearchHit hit : response.getHits()) {
        logDOList.add(convertLogMap2LogDO(hit.getId(), hit.getSourceAsMap()));
      }

      total = response.getHits().getTotalHits().value;

    } catch (ElasticsearchStatusException ese) {
      LOGGER.warn("index not found, {}", ese);
    } catch (IOException e) {
      LOGGER.warn("error when querying {}", getIndexName(), e);
    }

    return new PageImpl<DO>(logDOList, page, total);
  }

  /**
   * @param queryVO
   * @param sort
   * @param size
   * @return
   */
  public List<DO> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds, Sort sort,
      int size) {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    // 按照字段排序
    Iterator<Order> orderIterator = sort.iterator();
    while (orderIterator.hasNext()) {
      Order order = orderIterator.next();
      searchSourceBuilder.sort(order.getProperty(),
          SortOrder.fromString(order.getDirection().name()));
    }
    searchSourceBuilder.size(size);

    SearchRequest searchRequest = new SearchRequest(getIndexName());

    final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));

    // 过滤时间
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    if (queryVO.getStartTimeDate() != null && queryVO.getEndTimeDate() != null) {
      boolQueryBuilder
          .filter(getContainTimeRangeBuilder(queryVO.getStartTimeDate(), queryVO.getEndTimeDate()));
    }

    // 多字段匹配
    if (StringUtils.isNotBlank(queryVO.getKeyword())) {
      MultiMatchQueryBuilder multiMatchQueryBuilder = QueryBuilders
          .multiMatchQuery(queryVO.getKeyword(), getSearchFieldNames(queryVO.getKeyword()));
      boolQueryBuilder.filter(multiMatchQueryBuilder);
    }

    searchSourceBuilder.query(boolQueryBuilder);
    searchRequest.source(searchSourceBuilder);
    searchRequest.scroll(scroll);

    List<DO> protocolDnsLogList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      SearchResponse response = getRestHighLevelClient().search(searchRequest,
          RequestOptions.DEFAULT);
      String scrollId = response.getScrollId();
      SearchHit[] searchHits = response.getHits().getHits();
      for (SearchHit hit : searchHits) {
        protocolDnsLogList.add(convertLogMap2LogDO(hit.getId(), hit.getSourceAsMap()));
      }

      // 清理游标
      ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
      clearScrollRequest.addScrollId(scrollId);
      ClearScrollResponse clearScrollResponse = getRestHighLevelClient()
          .clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
      boolean succeeded = clearScrollResponse.isSucceeded();
      if (succeeded) {
        LOGGER.debug("clear scroll success");
      } else {
        LOGGER.debug("clear scroll fail");
      }
    } catch (ElasticsearchStatusException ese) {
      LOGGER.warn("index not found, {}", ese);
    } catch (IOException e) {
      LOGGER.warn("error when querying protocol dns", e);
    }

    return protocolDnsLogList;
  }

  public List<Map<String, Object>> queryLogRecords(String startTime, String endTime,
      List<String> flowIds, String dsl, int size) {
    throw new UnsupportedOperationException();
  }

  public List<Object> queryFlowIds(String queryId, LogRecordQueryVO queryVO, Sort sort, int size) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param dsl
   * @return
   */
  public String queryLogRecordsViaDsl(String dsl) {
    String result = null;

    RestClient restClient = getRestHighLevelClient().getLowLevelClient();
    Request request = new Request("GET", getIndexName() + "/_search");

    request.setJsonEntity(dsl);
    request.setOptions(RequestOptions.DEFAULT);

    request.addParameter("allow_no_indices",
        Boolean.toString(IndicesOptions.LENIENT_EXPAND_OPEN.allowNoIndices()));
    request.addParameter("expand_wildcards", "open");
    request.addParameter("ignore_throttled",
        Boolean.toString(IndicesOptions.LENIENT_EXPAND_OPEN.ignoreThrottled()));
    request.addParameter("ignore_unavailable",
        Boolean.toString(IndicesOptions.LENIENT_EXPAND_OPEN.ignoreUnavailable()));

    LOGGER.debug("low level search request: [{}]", request);

    try {
      Response response = restClient.performRequest(request);
      try (InputStream inputStream = response.getEntity().getContent()) {
        result = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        if (LOGGER.isTraceEnabled()) {
          LOGGER.trace("low level search result: [{}]", result);
        }
      }
    } catch (IOException ese) {
      LOGGER.warn("failed to search by low level client, {}", ese);
    }
    return result;
  }

  /**
   * 
   * @param id
   * @return
   */
  public DO queryLogRecord(LogRecordQueryVO queryVO, String id) {

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    SearchRequest searchRequest = new SearchRequest(getIndexName());

    searchSourceBuilder.query(QueryBuilders.termQuery("_id", id));

    searchRequest.source(searchSourceBuilder);

    DO protocolDnsLogDO = buildEmptyLogDO();

    try {
      SearchResponse response = getRestHighLevelClient().search(searchRequest,
          RequestOptions.DEFAULT);
      if (response.getHits().getTotalHits().value > 0) {
        return convertLogMap2LogDO(id, response.getHits().getAt(0).getSourceAsMap());
      }
    } catch (ElasticsearchStatusException ese) {
      LOGGER.warn("index not found, {}", ese);
    } catch (IOException e) {
      LOGGER.warn("error when querying protocol dns by id", e);
    }

    return protocolDnsLogDO;
  }

  public long countLogRecords(LogRecordQueryVO queryVO, List<String> flowIds) {
    throw new UnsupportedOperationException();
  }

  public Map<String, Long> countLogRecords(LogCountQueryVO queryVO) {
    long count = 0L;
    String indexName = "";
    try {
      indexName = getIndexName();
      CountRequest countRequest = new CountRequest(indexName);
      countRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
      MatchQueryBuilder query = StringUtils.isNotBlank(queryVO.getSrcIp())
          ? QueryBuilders.matchQuery("src_ip", queryVO.getSrcIp())
          : null;
      if (query != null) {
        countRequest.query(query);
      }
      CountResponse countResponse = getRestHighLevelClient().count(countRequest,
          RequestOptions.DEFAULT);
      count = countResponse.getCount();
    } catch (IOException e) {
      LOGGER.warn("failed to count document, indexName: " + getIndexName() + e);
    }

    String protocol = StringUtils.swapCase(StringUtils.substringAfterLast(
        StringUtils.substringBefore(indexName, "-log-record"), "a_fpc-protocol-"));

    Map<String, Long> result = Maps.newHashMapWithExpectedSize(1);
    result.put(protocol, count);
    return result;
  }

  protected abstract RestHighLevelClient getRestHighLevelClient();

  protected abstract String getIndexName();

  protected abstract DO convertLogMap2LogDO(String id, Map<String, Object> map);

  protected void convertBaseLogMap2AbstractLogDO(AbstractLogRecordDO abstractLogRecordDO, String id,
      Map<String, Object> map) {
    abstractLogRecordDO.setStartTime((MapUtils.getString(map, "start_time")));
    abstractLogRecordDO.setEndTime((MapUtils.getString(map, "end_time")));
    abstractLogRecordDO.setPolicyName(MapUtils.getString(map, "policy_name"));
    abstractLogRecordDO.setLevel(MapUtils.getString(map, "level"));
    abstractLogRecordDO.setFlowId(String.valueOf(MapUtils.getLong(map, "flow_id")));
    abstractLogRecordDO.setSrcIp(MapUtils.getString(map, "src_ip"));
    abstractLogRecordDO.setSrcPort(MapUtils.getIntValue(map, "src_port"));
    abstractLogRecordDO.setDestIp(MapUtils.getString(map, "dest_ip"));
    abstractLogRecordDO.setDestPort(MapUtils.getIntValue(map, "dest_port"));
  }

  protected abstract DO buildEmptyLogDO();

  protected String[] getSearchFieldNames(String keyword) {

    List<String> searchfieldNameList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 判断关键字是否为IP或IP段
    if (NetworkUtils.isCidr(keyword) || NetworkUtils.isInetAddress(keyword)) {
      searchfieldNameList.addAll(IP_FIELD_NAMES);
    }

    if (NetworkUtils.isInetAddressPort(keyword)) {
      searchfieldNameList.addAll(PORT_FIELD_NAMES);
    }

    return searchfieldNameList.toArray(new String[searchfieldNameList.size()]);
  }

  protected String[] getWildcardSearchFieldNames(String keyword) {
    return null;
  };

  @SuppressWarnings("unused")
  private RangeQueryBuilder getCreateTimeRangeBuilder(Date startTime, Date endTime) {
    // 时间
    RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("start_time");
    rangeQueryBuilder.format("epoch_millis");
    if (startTime != null) {
      rangeQueryBuilder.from(startTime.getTime(), true);
    }
    if (endTime != null) {
      rangeQueryBuilder.to(endTime.getTime(), false);
    }
    return rangeQueryBuilder;
  }

  private BoolQueryBuilder getContainTimeRangeBuilder(Date startTime, Date endTime) {
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

    // 日志开始时间在查询时间范围中
    RangeQueryBuilder startTimeRangeQueryBuilder = QueryBuilders.rangeQuery("start_time");
    startTimeRangeQueryBuilder.format("epoch_millis");
    if (startTime != null) {
      startTimeRangeQueryBuilder.from(startTime.getTime(), true);
    }
    if (endTime != null) {
      startTimeRangeQueryBuilder.to(endTime.getTime(), true);
    }
    boolQuery.should(startTimeRangeQueryBuilder);

    // 日志结束时间在查询时间范围中
    RangeQueryBuilder endTimeRangeQueryBuilder = QueryBuilders.rangeQuery("end_time");
    endTimeRangeQueryBuilder.format("epoch_millis");
    if (startTime != null) {
      endTimeRangeQueryBuilder.from(startTime.getTime(), true);
    }
    if (endTime != null) {
      endTimeRangeQueryBuilder.to(endTime.getTime(), true);
    }
    boolQuery.should(endTimeRangeQueryBuilder);

    // 起止时间小于老化时间, 则存在长连接时间范围覆盖查询时间, 也需要查出来
    if (startTime != null && endTime != null && (endTime.getTime()
        - startTime.getTime() <= ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS)) {
      BoolQueryBuilder bool = QueryBuilders.boolQuery();
      bool.must(QueryBuilders.rangeQuery("start_time").format("epoch_millis")
          .from(startTime.getTime() - ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS, true)
          .to(startTime.getTime(), true));
      bool.must(
          QueryBuilders.rangeQuery("end_time").format("epoch_millis").from(endTime.getTime(), true)
              .to(endTime.getTime() + ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS, true));

      boolQuery.should(bool);
    }
    return boolQuery;
  }
}
