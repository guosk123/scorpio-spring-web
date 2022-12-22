package com.machloop.fpc.manager.appliance.dao.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksRequest;
import org.elasticsearch.action.admin.cluster.node.tasks.list.ListTasksResponse;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.tasks.CancelTasksRequest;
import org.elasticsearch.client.tasks.CancelTasksResponse;
import org.elasticsearch.client.tasks.TaskId;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.ScriptQueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.ExtendedBounds;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.tasks.TaskInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.dao.FlowLogDao;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月19日, fpc-manager
 */
// @Repository
public class FlowLogDaoImpl implements FlowLogDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogDaoImpl.class);
  private static final String INDEX_PREFIX = "i_fpc-flow-log-record-";

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDao#queryFlowLogs(com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String, int, int, double, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public String queryFlowLogs(FlowLogQueryVO queryVO, String queryTaskId, int terminateAfter,
      int timeout, double samplingRate, String sortProperty, String sortDirection, int size,
      String searchAfter) {

    // 校验排序字段是否合法
    if (!StringUtils.equals(sortProperty, "start_time")
        && !StringUtils.equals(sortProperty, "end_time")
        && !PageUtils.validSortProperty(sortProperty, FlowLogQueryVO.class)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "指定排序的列不合法");
    }

    // 使用dsl表达式查询, 使用lowLevel方式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      String[] indices = getScanIndices(queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
      return lowLevelSearch(queryVO.getDsl(), queryTaskId, indices);
    }

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    String[] indices = getScanIndices(queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
    SearchRequest searchRequest = new SearchRequest(indices);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    BoolQueryBuilder boolQueryBuilder = getQueryBuilder(queryVO);
    // 设置采样率
    if (samplingRate > 0 && samplingRate != 1) {
      boolQueryBuilder.filter(getSamplingRateScriptQueryBuilder(samplingRate));
    }
    searchSourceBuilder.query(boolQueryBuilder);

    searchSourceBuilder.trackTotalHits(true);
    searchSourceBuilder.size(Math.abs(size));
    // size>=0代表下一页,size<0代表上一页,使用search_after
    // 先根据指定字段排序,再根据flow_id排序,前一页等价于相反的排序方向的search_after,前一页情况需要将结果翻转
    if (size >= 0) {
      if (StringUtils.equals(sortProperty, "start_time")
          || StringUtils.equals(sortProperty, "end_time")) {
        searchSourceBuilder.sort(SortBuilders.fieldSort(sortProperty).setNumericType("date_nanos")
            .order(SortOrder.fromString(sortDirection)));
      } else {
        searchSourceBuilder.sort(sortProperty, SortOrder.fromString(sortDirection));
      }
      searchSourceBuilder.sort("flow_id", SortOrder.ASC);
    } else {
      if (StringUtils.equals(sortProperty, "start_time")
          || StringUtils.equals(sortProperty, "end_time")) {
        searchSourceBuilder.sort(SortBuilders.fieldSort(sortProperty).setNumericType("date_nanos")
            .order(SortOrder.fromString(sortDirection) == SortOrder.ASC ? SortOrder.DESC
                : SortOrder.ASC));
      } else {
        searchSourceBuilder.sort(sortProperty,
            SortOrder.fromString(sortDirection) == SortOrder.ASC ? SortOrder.DESC : SortOrder.ASC);
      }
      searchSourceBuilder.sort("flow_id", SortOrder.DESC);
    }
    // 游标,格式为:排序字段值_flowId,es可以自动转换格式,所以此处直接传字符串
    if (StringUtils.isNotBlank(searchAfter)) {
      searchSourceBuilder.searchAfter(StringUtils.split(searchAfter, "_"));
    }
    // 查询超时时间,单位为秒
    if (timeout > 0) {
      searchSourceBuilder.timeout(TimeValue.timeValueSeconds(timeout));
    }
    // 最大扫描数据量
    if (terminateAfter > 0) {
      searchSourceBuilder.terminateAfter(terminateAfter);
    }
    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs, elastic index:[{}], query:[{}]",
          StringUtils.join(indices, ","), searchSourceBuilder.toString());
    }
    try {
      SearchResponse response = restHighLevelClient.search(searchRequest,
          StringUtils.isNotBlank(queryTaskId)
              ? RequestOptions.DEFAULT.toBuilder().addHeader("X-Opaque-Id", queryTaskId).build()
              : RequestOptions.DEFAULT);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("elasticsearch response: [{}]", response.toString());
      }
      return response.toString();
    } catch (ElasticsearchStatusException e) {
      LOGGER.warn("fail to query FlowLogs.", e);
      for (Throwable throwable : e.getSuppressed()) {
        if (throwable instanceof ResponseException) {
          ResponseException statusException = (ResponseException) throwable;
          try {
            return IOUtils.toString(statusException.getResponse().getEntity().getContent(),
                StandardCharsets.UTF_8);
          } catch (IOException e1) {
            LOGGER.warn("failed to read response.");
          }
        }
      }
      return "";
    } catch (IOException e) {
      LOGGER.warn("fail to query FlowLogs.", e);
      return "";
    }
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDao#queryFlowLogs(com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, int, int, double, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public Tuple2<String, List<Map<String, Object>>> queryFlowLogs(FlowLogQueryVO queryVO,
      int terminateAfter, int timeout, double samplingRate, String sortProperty,
      String sortDirection, int size, String scrollId) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    BoolQueryBuilder boolQueryBuilder = getQueryBuilder(queryVO);
    // 设置采样率
    if (samplingRate > 0 && samplingRate != 1) {
      boolQueryBuilder.filter(getSamplingRateScriptQueryBuilder(samplingRate));
    }
    searchSourceBuilder.query(boolQueryBuilder);

    searchSourceBuilder.size(size);

    if (StringUtils.equals(sortProperty, "start_time")
        || StringUtils.equals(sortProperty, "end_time")) {
      searchSourceBuilder.sort(SortBuilders.fieldSort(sortProperty).setNumericType("date_nanos")
          .order(SortOrder.fromString(sortDirection)));
    } else {
      searchSourceBuilder.sort(sortProperty, SortOrder.fromString(sortDirection));
    }

    String[] indices = getScanIndices(queryVO.getStartTimeDate(), queryVO.getEndTimeDate());

    String newScrollId = null;
    try {
      SearchResponse response;
      if (StringUtils.isBlank(scrollId)) {
        Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        SearchRequest searchRequest = new SearchRequest(indices);
        searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(scroll);
        response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
      } else {
        SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
        scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
        response = restHighLevelClient.scroll(scrollRequest, RequestOptions.DEFAULT);
      }
      newScrollId = response.getScrollId();
      SearchHit[] searchHits = response.getHits().getHits();
      for (SearchHit hit : searchHits) {
        result.add(hit.getSourceAsMap());
      }
    } catch (ElasticsearchStatusException ese) {
      LOGGER.warn("index not found, {}", ese);
    } catch (IOException e) {
      LOGGER.warn("error when querying flow logs by scroll", e);
    }
    return Tuples.of(newScrollId, result);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDao#queryFlowLogs(java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogs(String flowId, Date inclusiveTime) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    // 包含该时间点的流日志，存在跨天的情况，流日志存储的索引是开始时间对应的日期，需要查询inclusiveTime所在天，及前一天的索引
    String[] indices = inclusiveTime == null ? getScanIndices(null, null)
        : getScanIndices(DateUtils.beforeDayDate(inclusiveTime, 1), inclusiveTime);

    SearchRequest searchRequest = new SearchRequest(indices);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    searchSourceBuilder.query(QueryBuilders.termQuery("flow_id", flowId));

    searchSourceBuilder.size(10);

    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs, elastic index:[{}], query:[{}]",
          StringUtils.join(indices, ","), searchSourceBuilder.toString());
    }
    try {
      SearchResponse response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("elasticsearch response: [{}]", response.toString());
      }
      response.getHits().forEach(hit -> result.add(hit.getSourceAsMap()));
      return result;
    } catch (ElasticsearchStatusException e) {
      LOGGER.warn("fail to query FlowLogs.", e);
    } catch (IOException e) {
      LOGGER.warn("fail to query FlowLogs.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDao#queryFlowLogStatistics(com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, int, java.lang.String, int, int, int, double)
   */
  @Override
  public String queryFlowLogStatistics(FlowLogQueryVO queryVO, String queryTaskId,
      int histogramInterval, String termFieldName, int termSize, int terminateAfter, int timeout,
      double samplingRate) {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    String[] indices = getScanIndices(queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
    SearchRequest searchRequest = new SearchRequest(indices);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    BoolQueryBuilder boolQueryBuilder = getQueryBuilder(queryVO);
    // 设置采样率
    if (samplingRate > 0 && samplingRate != 1) {
      boolQueryBuilder.filter(getSamplingRateScriptQueryBuilder(samplingRate));
    }
    searchSourceBuilder.query(boolQueryBuilder);
    searchSourceBuilder.trackTotalHits(false);
    // size为0时terminateAfter不生效
    searchSourceBuilder.size(1);

    // 查询超时时间,单位为秒
    if (timeout > 0) {
      searchSourceBuilder.timeout(TimeValue.timeValueSeconds(timeout));
    }
    // 最大扫描数据量
    if (terminateAfter > 0) {
      searchSourceBuilder.terminateAfter(terminateAfter);
    }

    // 时间聚合统计数量
    DateHistogramAggregationBuilder histogramAggregation = AggregationBuilders
        .dateHistogram("start_time").field("start_time")
        .fixedInterval(DateHistogramInterval.seconds(histogramInterval))
        .timeZone(ZoneId.systemDefault()).format("epoch_millis").extendedBounds(new ExtendedBounds(
            queryVO.getStartTimeDate().getTime(), queryVO.getEndTimeDate().getTime()));
    searchSourceBuilder.aggregation(histogramAggregation);

    for (String field : StringUtils.split(termFieldName, ",")) {
      TermsAggregationBuilder termsBuilder = AggregationBuilders.terms(field).field(field)
          .minDocCount(1).size(termSize).order(BucketOrder.count(false));
      searchSourceBuilder.aggregation(termsBuilder);
    }

    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs statistics, elastic index:[{}], query:[{}]",
          StringUtils.join(indices, ","), searchSourceBuilder.toString());
    }
    try {
      SearchResponse response = restHighLevelClient.search(searchRequest,
          StringUtils.isNotBlank(queryTaskId)
              ? RequestOptions.DEFAULT.toBuilder().addHeader("X-Opaque-Id", queryTaskId).build()
              : RequestOptions.DEFAULT);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("elasticsearch response: [{}]", response.toString());
      }
      return response.toString();
    } catch (ResponseException e) {
      LOGGER.warn("fail to query FlowLogStatistics.", e);
      for (Throwable throwable : e.getSuppressed()) {
        if (throwable instanceof ResponseException) {
          ResponseException statusException = (ResponseException) throwable;
          try {
            return IOUtils.toString(statusException.getResponse().getEntity().getContent(),
                StandardCharsets.UTF_8);
          } catch (IOException e1) {
            LOGGER.warn("failed to read response.");
          }
        }
      }
      return "";
    } catch (IOException e) {
      LOGGER.warn("fail to query FlowLogStatistics.", e);
      return "";
    }
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDao#queryFlowLogStatisticsGroupByIp(com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String, int, int)
   */
  @Override
  public String queryFlowLogStatisticsGroupByIp(FlowLogQueryVO queryVO, String queryTaskId,
      int termSize, int timeout, String sortProperty, String sortDirection) {
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    String[] indices = getScanIndices(queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
    SearchRequest searchRequest = new SearchRequest(indices);
    searchRequest.indicesOptions(IndicesOptions.LENIENT_EXPAND_OPEN);

    BoolQueryBuilder boolQueryBuilder = getQueryBuilder(queryVO);
    searchSourceBuilder.query(boolQueryBuilder);
    searchSourceBuilder.size(0);
    searchSourceBuilder.trackTotalHits(false);

    // 查询超时时间,单位为秒
    if (timeout > 0) {
      searchSourceBuilder.timeout(TimeValue.timeValueSeconds(timeout));
    }

    String script = "if(doc.ipv4_initiator.value!=null&&doc.ipv4_responder.value!=null)"
        + "{doc.ipv4_initiator.value+\"_\"+doc.ipv4_responder.value} "
        + "else if(doc.ipv6_initiator.value!=null&&doc.ipv6_responder.value!=null)"
        + "{doc.ipv6_initiator.value+\"_\"+doc.ipv6_responder.value}";
    TermsAggregationBuilder termAggregation = AggregationBuilders.terms("ipTerm")
        .script(new Script(Script.DEFAULT_SCRIPT_TYPE, Script.DEFAULT_SCRIPT_LANG, script,
            Maps.newHashMapWithExpectedSize(0)))
        .minDocCount(1).size(termSize).order(BucketOrder.aggregation(sortProperty,
            SortOrder.fromString(sortDirection) == SortOrder.ASC));
    termAggregation
        .subAggregation(AggregationBuilders.sum("upstream_bytes").field("upstream_bytes"));
    termAggregation
        .subAggregation(AggregationBuilders.sum("downstream_bytes").field("downstream_bytes"));
    termAggregation.subAggregation(AggregationBuilders.sum("total_bytes").field("total_bytes"));
    termAggregation
        .subAggregation(AggregationBuilders.sum("upstream_packets").field("upstream_packets"));
    termAggregation
        .subAggregation(AggregationBuilders.sum("downstream_packets").field("downstream_packets"));
    termAggregation.subAggregation(AggregationBuilders.sum("total_packets").field("total_packets"));
    searchSourceBuilder.aggregation(termAggregation);

    searchRequest.source(searchSourceBuilder);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs statistics group by ip, elastic index:[{}], query:[{}]",
          StringUtils.join(indices, ","), searchSourceBuilder.toString());
    }
    try {
      SearchResponse response = restHighLevelClient.search(searchRequest,
          StringUtils.isNotBlank(queryTaskId)
              ? RequestOptions.DEFAULT.toBuilder().addHeader("X-Opaque-Id", queryTaskId).build()
              : RequestOptions.DEFAULT);
      if (LOGGER.isTraceEnabled()) {
        LOGGER.trace("elasticsearch response: [{}]", response.toString());
      }
      return response.toString();
    } catch (ResponseException e) {
      LOGGER.warn("fail to query FlowLogStatisticsGroupByIp.", e);
      for (Throwable throwable : e.getSuppressed()) {
        if (throwable instanceof ResponseException) {
          ResponseException statusException = (ResponseException) throwable;
          try {
            return IOUtils.toString(statusException.getResponse().getEntity().getContent(),
                StandardCharsets.UTF_8);
          } catch (IOException e1) {
            LOGGER.warn("failed to read response.");
          }
        }
      }
      return "";
    } catch (IOException e) {
      LOGGER.warn("fail to query FlowLogStatisticsGroupByIp.", e);
      return "";
    }
  }

  /**
   * 
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDao#cancelFlowLogsQueryTask(java.lang.String)
   */
  @Override
  public void cancelFlowLogsQueryTask(String queryTaskId) {
    try {
      ListTasksRequest request = new ListTasksRequest();
      request.setActions("indices:data/read/*");
      ListTasksResponse response = restHighLevelClient.tasks().list(request,
          RequestOptions.DEFAULT);
      Set<TaskId> parentTaskIds = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      for (TaskInfo task : response.getTasks()) {
        if (StringUtils.equals(MapUtils.getString(task.getHeaders(), "X-Opaque-Id"), queryTaskId)) {
          parentTaskIds
              .add(new TaskId(task.getParentTaskId().isSet() ? task.getParentTaskId().toString()
                  : task.getTaskId().toString()));
        }
      }

      for (TaskId taskId : parentTaskIds) {
        CancelTasksRequest cancelTasksRequest = new CancelTasksRequest.Builder().withTaskId(taskId)
            .build();
        CancelTasksResponse cancelResponse = restHighLevelClient.tasks().cancel(cancelTasksRequest,
            RequestOptions.DEFAULT);
        LOGGER.debug("cancel query task, id:[{}], response:[{}]", taskId,
            cancelResponse.toString());
      }
    } catch (IOException e) {
      LOGGER.warn("failed to cancel query task.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "取消查询任务失败");
    }
  }

  /**
   * @return 
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDao#clearScroll(java.lang.String)
   */
  @Override
  public boolean clearScroll(String scrollId) {
    if (StringUtils.isBlank(scrollId)) {
      return false;
    }
    // 清理游标
    ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
    clearScrollRequest.addScrollId(scrollId);
    ClearScrollResponse clearScrollResponse;
    try {
      clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest,
          RequestOptions.DEFAULT);
      boolean succeeded = clearScrollResponse.isSucceeded();
      return succeeded;
    } catch (IOException e) {
      LOGGER.warn("failed to clear scroll, scroll id: [{}]", scrollId);
      return false;
    }
  }

  private String[] getScanIndices(Date startTime, Date endTime) {

    if (startTime == null || endTime == null) {
      return new String[]{INDEX_PREFIX + "*"};
    }

    // 查询的索引要包含起止时间前后老化的范围
    startTime = DateUtils.beforeSecondDate(startTime,
        (int) (ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS / 1000));
    endTime = DateUtils.afterSecondDate(endTime,
        (int) (ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS / 1000));

    List<String> days = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    // 使用0时区的时间
    Calendar tmp = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
    tmp.setTime(startTime);
    tmp.set(Calendar.HOUR_OF_DAY, 0);
    tmp.set(Calendar.MINUTE, 0);
    tmp.set(Calendar.SECOND, 0);
    while (!tmp.getTime().after(endTime)) {
      days.add(DateUtils.toStringFormat(tmp.getTime(), "yyyy-MM-dd", ZoneOffset.UTC));
      tmp.add(Calendar.DATE, 1);
    }

    // 合并
    Map<String, Integer> yearCount = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, Integer> monthCount = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, Integer> tenDaysCount = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    for (String day : days) {
      yearCount.put(StringUtils.substring(day, 0, 5),
          MapUtils.getInteger(yearCount, StringUtils.substring(day, 0, 5), 0) + 1);
      monthCount.put(StringUtils.substring(day, 0, 8),
          MapUtils.getInteger(monthCount, StringUtils.substring(day, 0, 8), 0) + 1);
      tenDaysCount.put(StringUtils.substring(day, 0, 9),
          MapUtils.getInteger(tenDaysCount, StringUtils.substring(day, 0, 9), 0) + 1);
    }

    List<String> indices = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Set<String> yearPrefix = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Set<String> monthPrefix = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Set<String> tenDaysPrefix = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    for (Entry<String, Integer> entry : yearCount.entrySet()) {
      Calendar c = Calendar.getInstance();
      c.set(Calendar.YEAR, Integer.parseInt(StringUtils.substring(entry.getKey(), 0, 4)));
      // 含有该年所有的
      if (entry.getValue() == c.getActualMaximum(Calendar.DAY_OF_YEAR)) {
        // 年前缀
        yearPrefix.add(entry.getKey());
        indices.add(INDEX_PREFIX + entry.getKey() + "*");
      }
    }

    for (Entry<String, Integer> entry : monthCount.entrySet()) {
      if (yearPrefix.contains(StringUtils.substring(entry.getKey(), 0, 5))) {
        continue;
      }
      Calendar c = Calendar.getInstance();
      String[] yearAndMonth = StringUtils.split(entry.getKey(), "-");
      c.set(Calendar.YEAR, Integer.parseInt(yearAndMonth[0]));
      c.set(Calendar.MONTH, Integer.parseInt(yearAndMonth[1]) - 1);
      // 含有该月所有的
      if (entry.getValue() == c.getActualMaximum(Calendar.DAY_OF_MONTH)) {
        // 月前缀
        monthPrefix.add(entry.getKey());
        indices.add(INDEX_PREFIX + entry.getKey() + "*");
      }
    }

    for (Entry<String, Integer> entry : tenDaysCount.entrySet()) {
      if (yearPrefix.contains(StringUtils.substring(entry.getKey(), 0, 5))
          || monthPrefix.contains(StringUtils.substring(entry.getKey(), 0, 8))) {
        continue;
      }
      // 含有该十天所有的
      if ((StringUtils.endsWith(entry.getKey(), "0") && entry.getValue() == 9)
          || (!StringUtils.endsWith(entry.getKey(), "0") && entry.getValue() == 10)) {
        // 十天前缀
        tenDaysPrefix.add(entry.getKey());
        indices.add(INDEX_PREFIX + entry.getKey() + "*");
      }
    }

    for (String day : days) {
      if (yearPrefix.contains(StringUtils.substring(day, 0, 5))
          || monthPrefix.contains(StringUtils.substring(day, 0, 8))
          || tenDaysPrefix.contains(StringUtils.substring(day, 0, 9))) {
        continue;
      }
      indices.add(INDEX_PREFIX + day);
    }
    Collections.sort(indices);
    return indices.toArray(new String[indices.size()]);
  }

  private ScriptQueryBuilder getSamplingRateScriptQueryBuilder(double samplingRate) {
    int modBaseValue = 1000;
    int range = (int) (modBaseValue * samplingRate);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("base", modBaseValue);
    params.put("range", range);
    Script script = new Script(ScriptType.INLINE, "expression", "doc['flow_id'] % base < range",
        params);
    return new ScriptQueryBuilder(script);
  }

  private BoolQueryBuilder getQueryBuilder(FlowLogQueryVO queryVO) {
    BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
    if (StringUtils.isNotBlank(queryVO.getId())) {
      boolQueryBuilder
          .filter(QueryBuilders.termsQuery("_id", CsvUtils.convertCSVToSet(queryVO.getId())));
    }
    if (StringUtils.isNotBlank(queryVO.getInterfaceName())) {
      boolQueryBuilder.filter(QueryBuilders.termQuery("interface", queryVO.getInterfaceName()));
    }
    if (queryVO.getFlowId() != null) {
      boolQueryBuilder.filter(QueryBuilders.termQuery("flow_id", queryVO.getFlowId()));
    }
    if (queryVO.getFlowContinued() != null) {
      boolQueryBuilder
          .filter(QueryBuilders.termQuery("flow_continued", queryVO.getFlowContinued()));
    }
    if (StringUtils.isNotBlank(queryVO.getEthernetInitiator())) {
      boolQueryBuilder
          .filter(QueryBuilders.termQuery("ethernet_initiator", queryVO.getEthernetInitiator()));
    }
    if (StringUtils.isNotBlank(queryVO.getEthernetResponder())) {
      boolQueryBuilder
          .filter(QueryBuilders.termQuery("ethernet_responder", queryVO.getEthernetResponder()));
    }
    if (StringUtils.isNotBlank(queryVO.getEthernetProtocol())) {
      boolQueryBuilder
          .filter(QueryBuilders.termQuery("ethernet_protocol", queryVO.getEthernetProtocol()));
    }
    if (queryVO.getVlanId() != null) {
      boolQueryBuilder.filter(QueryBuilders.termQuery("vlan_id", queryVO.getVlanId()));
    }
    if (StringUtils.isNotBlank(queryVO.getIpInitiator())) {
      BoolQueryBuilder ipInitiatorQuery = getIpConditionQueryBuilder(queryVO.getIpInitiator(),
          "ipv4_initiator", "ipv6_initiator");
      if (ipInitiatorQuery != null) {
        boolQueryBuilder.filter(ipInitiatorQuery);
      }
    }
    if (StringUtils.isNotBlank(queryVO.getIpResponder())) {
      BoolQueryBuilder ipResponderQuery = getIpConditionQueryBuilder(queryVO.getIpResponder(),
          "ipv4_responder", "ipv6_responder");
      if (ipResponderQuery != null) {
        boolQueryBuilder.filter(ipResponderQuery);
      }
    }
    if (StringUtils.isNotBlank(queryVO.getIpProtocol())) {
      boolQueryBuilder.filter(QueryBuilders.termQuery("ip_protocol", queryVO.getIpProtocol()));
    }
    if (StringUtils.isNotBlank(queryVO.getPortInitiator())) {
      QueryBuilder portQueryBuilder = StringUtils.isNumeric(queryVO.getPortInitiator())
          || StringUtils.contains(queryVO.getPortInitiator(), ",")
          || StringUtils.contains(queryVO.getPortInitiator(), "-")
              ? getPortConditionQueryBuilder(queryVO.getPortInitiator(), "port_initiator")
              : new QueryStringQueryBuilder(formatQueryString(queryVO.getPortInitiator()))
                  .defaultField("port_initiator");
      boolQueryBuilder.filter(portQueryBuilder);
    }
    if (StringUtils.isNotBlank(queryVO.getPortResponder())) {
      QueryBuilder portQueryBuilder = StringUtils.isNumeric(queryVO.getPortResponder())
          || StringUtils.contains(queryVO.getPortResponder(), ",")
          || StringUtils.contains(queryVO.getPortResponder(), "-")
              ? getPortConditionQueryBuilder(queryVO.getPortResponder(), "port_responder")
              : new QueryStringQueryBuilder(formatQueryString(queryVO.getPortResponder()))
                  .defaultField("port_responder");
      boolQueryBuilder.filter(portQueryBuilder);
    }
    if (StringUtils.isNotBlank(queryVO.getL7ProtocolId())) {
      boolQueryBuilder.filter(QueryBuilders.termQuery("l7_protocol_id", queryVO.getL7ProtocolId()));
    }
    if (StringUtils.isNotBlank(queryVO.getApplicationIds())) {
      List<String> applicationIdList = CsvUtils.convertCSVToList(queryVO.getApplicationIds());
      for (String item : applicationIdList) {
        if (!StringUtils.isNumeric(item)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "应用ID参数错误");
        }
      }
      boolQueryBuilder.filter(QueryBuilders.termsQuery("application_id", applicationIdList));
    }
    if (StringUtils.isNotBlank(queryVO.getCountryIdInitiator())) {
      boolQueryBuilder
          .filter(QueryBuilders.termQuery("country_id_initiator", queryVO.getCountryIdInitiator()));
    }
    if (StringUtils.isNotBlank(queryVO.getProvinceIdInitiator())) {
      boolQueryBuilder.filter(
          QueryBuilders.termQuery("province_id_initiator", queryVO.getProvinceIdInitiator()));
    }
    if (StringUtils.isNotBlank(queryVO.getCityIdInitiator())) {
      boolQueryBuilder
          .filter(QueryBuilders.termQuery("city_id_initiator", queryVO.getCityIdInitiator()));
    }
    if (StringUtils.isNotBlank(queryVO.getCountryIdResponder())) {
      boolQueryBuilder
          .filter(QueryBuilders.termQuery("country_id_responder", queryVO.getCountryIdResponder()));
    }
    if (StringUtils.isNotBlank(queryVO.getProvinceIdResponder())) {
      boolQueryBuilder.filter(
          QueryBuilders.termQuery("province_id_responder", queryVO.getProvinceIdResponder()));
    }
    if (StringUtils.isNotBlank(queryVO.getCityIdResponder())) {
      boolQueryBuilder
          .filter(QueryBuilders.termQuery("city_id_responder", queryVO.getCityIdResponder()));
    }

    /*
     * 数字类型支持query_string表达式
     */
    if (StringUtils.isNotBlank(queryVO.getDuration())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getDuration())
          ? QueryBuilders.termQuery("duration", queryVO.getDuration())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getDuration()))
              .defaultField("duration");
      boolQueryBuilder.filter(query);
    }
    if (StringUtils.isNotBlank(queryVO.getUpstreamBytes())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getUpstreamBytes())
          ? QueryBuilders.termQuery("upstream_bytes", queryVO.getUpstreamBytes())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getUpstreamBytes()))
              .defaultField("upstream_bytes");
      boolQueryBuilder.filter(query);
    }
    if (StringUtils.isNotBlank(queryVO.getDownstreamBytes())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getDownstreamBytes())
          ? QueryBuilders.termQuery("downstream_bytes", queryVO.getDownstreamBytes())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getDownstreamBytes()))
              .defaultField("downstream_bytes");
      boolQueryBuilder.filter(query);
    }
    if (StringUtils.isNotBlank(queryVO.getTotalBytes())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getTotalBytes())
          ? QueryBuilders.termQuery("total_bytes", queryVO.getTotalBytes())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getTotalBytes()))
              .defaultField("total_bytes");
      boolQueryBuilder.filter(query);
    }
    if (StringUtils.isNotBlank(queryVO.getUpstreamPackets())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getUpstreamPackets())
          ? QueryBuilders.termQuery("upstream_packets", queryVO.getUpstreamPackets())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getUpstreamPackets()))
              .defaultField("upstream_packets");
      boolQueryBuilder.filter(query);
    }
    if (StringUtils.isNotBlank(queryVO.getDownstreamPackets())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getDownstreamPackets())
          ? QueryBuilders.termQuery("downstream_packets", queryVO.getDownstreamPackets())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getDownstreamPackets()))
              .defaultField("downstream_packets");
      boolQueryBuilder.filter(query);
    }
    if (StringUtils.isNotBlank(queryVO.getTotalPackets())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getTotalPackets())
          ? QueryBuilders.termQuery("total_packets", queryVO.getTotalPackets())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getTotalPackets()))
              .defaultField("total_packets");
      boolQueryBuilder.filter(query);
    }

    if (StringUtils.isNotBlank(queryVO.getUpstreamPayloadBytes())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getUpstreamPayloadBytes())
          ? QueryBuilders.termQuery("upstream_payload_bytes", queryVO.getUpstreamPayloadBytes())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getUpstreamPayloadBytes()))
              .defaultField("upstream_payload_bytes");
      boolQueryBuilder.filter(query);
    }
    if (StringUtils.isNotBlank(queryVO.getDownstreamPayloadBytes())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getDownstreamPayloadBytes())
          ? QueryBuilders.termQuery("downstream_payload_bytes", queryVO.getDownstreamPayloadBytes())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getDownstreamPayloadBytes()))
              .defaultField("downstream_payload_bytes");
      boolQueryBuilder.filter(query);
    }
    if (StringUtils.isNotBlank(queryVO.getTotalPayloadBytes())) {
      QueryBuilder query = StringUtils.isNumeric(queryVO.getTotalPayloadBytes())
          ? QueryBuilders.termQuery("total_payload_bytes", queryVO.getTotalPayloadBytes())
          : new QueryStringQueryBuilder(formatQueryString(queryVO.getTotalPayloadBytes()))
              .defaultField("total_payload_bytes");
      boolQueryBuilder.filter(query);
    }

    // 过滤时间
    boolQueryBuilder
        .filter(getContainTimeRangeBuilder(queryVO.getStartTimeDate(), queryVO.getEndTimeDate()));
    return boolQueryBuilder;
  }

  private BoolQueryBuilder getIpConditionQueryBuilder(String ipCondition, String ipv4FieldName,
      String ipv6FieldName) {
    List<String> ipv4List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> ipv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> ipv6List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> ipv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    String[] ipConditionList = StringUtils.split(ipCondition, ",");

    if (ipConditionList.length > FpcConstants.HOSTGROUP_MAX_IP_COUNT) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "查询的IP地址条目数过多, 请修改查询。");
    }

    for (String ip : ipConditionList) {
      if (StringUtils.contains(ip, "-")) {
        // ip范围 10.0.0.1-10.0.0.100
        String[] ipRange = StringUtils.split(ip, "-");
        if (ipRange.length != 2) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }

        String ipStart = ipRange[0];
        String ipEnd = ipRange[1];

        // 起止都是正确的ip
        if (!NetworkUtils.isInetAddress(StringUtils.trim(ipStart))
            || !NetworkUtils.isInetAddress(StringUtils.trim(ipEnd))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
        if (NetworkUtils.isInetAddress(ipStart, IpVersion.V4)
            && NetworkUtils.isInetAddress(ipEnd, IpVersion.V4)) {
          ipv4RangeList.add(Tuples.of(ipStart, ipEnd));
        } else if (NetworkUtils.isInetAddress(ipStart, IpVersion.V6)
            && NetworkUtils.isInetAddress(ipEnd, IpVersion.V6)) {
          ipv6RangeList.add(Tuples.of(ipStart, ipEnd));
        } else {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址");
        }
      } else {
        // 单个IP或CIDR格式
        ip = StringUtils.trim(ip);
        if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的IP地址");
        }
        if (NetworkUtils.isInetAddress(ip, IpVersion.V4) || NetworkUtils.isCidr(ip, IpVersion.V4)) {
          ipv4List.add(ip);
        } else if (NetworkUtils.isInetAddress(ip, IpVersion.V6)
            || NetworkUtils.isCidr(ip, IpVersion.V6)) {
          ipv6List.add(ip);
        }
      }
    }
    if (CollectionUtils.isNotEmpty(ipv4List) || CollectionUtils.isNotEmpty(ipv6List)
        || CollectionUtils.isNotEmpty(ipv4RangeList) || CollectionUtils.isNotEmpty(ipv6RangeList)) {

      BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
      if (CollectionUtils.isNotEmpty(ipv4List)) {
        boolQueryBuilder.should(QueryBuilders.termsQuery(ipv4FieldName, ipv4List));
      }
      if (CollectionUtils.isNotEmpty(ipv6List)) {
        boolQueryBuilder.should(QueryBuilders.termsQuery(ipv6FieldName, ipv6List));
      }
      for (Tuple2<String, String> range : ipv4RangeList) {
        boolQueryBuilder
            .should(QueryBuilders.rangeQuery(ipv4FieldName).gte(range.getT1()).lte(range.getT2()));
      }
      for (Tuple2<String, String> range : ipv6RangeList) {
        boolQueryBuilder
            .should(QueryBuilders.rangeQuery(ipv4FieldName).gte(range.getT1()).lte(range.getT2()));
      }
      return boolQueryBuilder;
    }

    return null;
  }

  private QueryBuilder getPortConditionQueryBuilder(String ports, String fieldName) {
    if (StringUtils.contains(ports, "-")) {
      // 端口范围 80-90
      String[] portRange = StringUtils.split(ports, "-");
      if (!NetworkUtils.isInetAddressPort(StringUtils.trim(portRange[0]))
          || !NetworkUtils.isInetAddressPort(StringUtils.trim(portRange[1]))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的端口");
      }
      return QueryBuilders.rangeQuery(fieldName).gte(StringUtils.trim(portRange[0]))
          .lte(StringUtils.trim(portRange[1]));
    } else {
      // 单个或多个端口 80,8080,8090
      List<String> portList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      for (String port : StringUtils.split(ports, ",")) {
        port = StringUtils.trim(port);
        if (!NetworkUtils.isInetAddressPort(StringUtils.trim(port))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的端口");
        }
        if (StringUtils.isNotBlank(port)) {
          portList.add(port);
        }
      }
      if (!portList.isEmpty()) {
        return QueryBuilders.termsQuery(fieldName, portList);
      }
    }
    return null;
  }

  @SuppressWarnings("unused")
  private RangeQueryBuilder getFlowCreateTimeRangeBuilder(Date startTime, Date endTime) {
    // 针对startTime过滤时间
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

  private String formatQueryString(String queryString) {
    queryString = StringUtils.replace(queryString, "||", " OR ");
    queryString = StringUtils.replace(queryString, "&&", " AND ");
    queryString = StringUtils.replace(queryString, "|", " OR ");
    queryString = StringUtils.replace(queryString, "&", " AND ");
    queryString = StringUtils.replace(queryString, "!", " NOT ");
    queryString = StringUtils.upperCase(queryString);
    return queryString;
  }

  public String lowLevelSearch(String dsl, String queryTaskId, String... indices) {
    String result = null;

    RestClient restClient = restHighLevelClient.getLowLevelClient();
    Request request = new Request("GET", StringUtils.join(indices, ',') + "/_search");

    request.setJsonEntity(dsl);
    request.setOptions(StringUtils.isNotBlank(queryTaskId)
        ? RequestOptions.DEFAULT.toBuilder().addHeader("X-Opaque-Id", queryTaskId).build()
        : RequestOptions.DEFAULT);

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
}
