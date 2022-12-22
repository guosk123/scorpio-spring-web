package com.machloop.fpc.manager.appliance.dao.clickhouse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.*;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisSubTaskDO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年9月18日, fpc-manager
 */
@Repository
public class FlowLogDaoImpl implements FlowLogDaoCk {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogDaoImpl.class);

  private static final String TABLE_FLOW_LOG_RECORD = "t_fpc_flow_log_record";

  private static final int QUERY_BY_ID_LIMIT_SIZE = 10000;

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private PacketAnalysisSubTaskDao packetAnalysisSubTaskDao;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowLogs(java.lang.String, com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.util.List)
   */
  @Override
  public Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page,
      FlowLogQueryVO queryVO, String columns, List<String> ids) {

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 数据源
    String tableName = getTableName(queryVO);

    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    // 查询符合条件的记录report_time, flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder(securityQueryId);
    innerSelectSql.append(" select report_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, whereSql, innerParams);
    }

    innerSelectSql.append(whereSql);

    PageUtils.appendPage(innerSelectSql, page, FlowLogQueryVO.class);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs page, inner sql: {}, params: {}", innerSelectSql.toString(),
          innerParams);
    }

    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    // 使用已查到的report_time,flow_id查找对应记录的全部字段
    List<String> rowIds = innerResult.stream()
        .map(r -> DateUtils.toStringYYYYMMDDHHMMSS((OffsetDateTime) r.get("report_time"),
            ZoneId.of("UTC")) + "_" + MapUtils.getString(r, "flow_id"))
        .collect(Collectors.toList());

    List<Map<String, Object>> result = queryFlowLogsByIds(queryId, tableName, columns,
        page.getSort(), rowIds);
    return new PageImpl<>(result, page, 0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowLogs(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.util.List, com.machloop.alpha.common.base.page.Sort, int)
   */
  @Override
  public Tuple2<String, List<String>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      List<String> ids, Sort sort, int size) {

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 数据源
    String tableName = getTableName(queryVO);

    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return Tuples.of(tableName, Lists.newArrayListWithCapacity(0));
    }

    // 查询符合条件的记录report_time, flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder(securityQueryId);
    innerSelectSql.append(" select report_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, whereSql, innerParams);
    }

    innerSelectSql.append(whereSql);

    PageUtils.appendSort(innerSelectSql, sort, FlowLogQueryVO.class);
    innerSelectSql.append(" limit ");
    innerSelectSql.append(size);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs limit size, inner sql: {}, params: {}",
          innerSelectSql.toString(), innerParams);
    }
    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return Tuples.of(tableName, Lists.newArrayListWithCapacity(0));
    }

    return Tuples.of(tableName,
        innerResult.stream()
            .map(row -> DateUtils.toStringYYYYMMDDHHMMSS((OffsetDateTime) row.get("report_time"),
                ZoneId.of("UTC")) + "_" + MapUtils.getString(row, "flow_id"))
            .collect(Collectors.toList()));
  }

  // 导出时 通过flowIds进行导出
  @Override
  public Tuple2<String, List<String>> queryFlowLogsByFlowIds(String queryId, FlowLogQueryVO queryVO,
      List<String> flowIds, Sort sort, int count) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 数据源
    String tableName = getTableName(queryVO);

    // 查询符合条件的记录
    StringBuilder innerSelectSql = new StringBuilder(securityQueryId);
    innerSelectSql.append(" select report_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    if (!flowIds.isEmpty()) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }

    innerSelectSql.append(whereSql);

    PageUtils.appendSort(innerSelectSql, sort, FlowLogQueryVO.class);
    innerSelectSql.append(" limit ");
    innerSelectSql.append(count);
    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    return Tuples.of(tableName,
        innerResult.stream()
            .map(row -> DateUtils.toStringYYYYMMDDHHMMSS((OffsetDateTime) row.get("report_time"),
                ZoneId.of("UTC")) + "_" + MapUtils.getString(row, "flow_id"))
            .collect(Collectors.toList()));
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowLogs(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.List, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      String l7ProtocolId, List<String> flowIds, int size) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 数据源
    String tableName = getTableName(queryVO);

    // 查询符合条件的记录report_time, flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder(securityQueryId);
    innerSelectSql.append(" select report_time, flow_id from ").append(tableName);

    // 构造查询条件
    StringBuilder whereSql = new StringBuilder(" where 1=1 ");
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 时间范围 <左闭右开>
    whereSql.append(" and report_time >= toDateTime64(:start_time, 9, 'UTC') ");
    whereSql.append(" and report_time < toDateTime64(:end_time, 9, 'UTC') ");
    innerParams.put("start_time", queryVO.getStartTime());
    innerParams.put("end_time", queryVO.getEndTime());
    // 应用层协议
    if (StringUtils.isNotBlank(l7ProtocolId)) {
      whereSql.append(" and l7_protocol_id = :l7_protocol_id ");
      innerParams.put("l7_protocol_id", l7ProtocolId);
    }
    // 会话ID
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }
    // dsl
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String, Map<String, Object>> dslTuples = dslConverter.converte(queryVO.getDsl(),
            false, 9, true, false);
        whereSql.append(" and ");
        whereSql.append(dslTuples.getT1());
        innerParams.putAll(dslTuples.getT2());
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析dsl失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "dsl格式错误");
      }
    }
    innerSelectSql.append(whereSql);

    Sort sort = new Sort(new Order(Sort.Direction.DESC, "report_time"), new Order("flow_id"));
    PageUtils.appendSort(innerSelectSql, sort, FlowLogQueryVO.class);
    innerSelectSql.append(" limit ");
    innerSelectSql.append(size);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs limit size, inner sql: {}, params: {}",
          innerSelectSql.toString(), innerParams);
    }
    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return Lists.newArrayListWithCapacity(0);
    }

    List<String> rowIds = innerResult.stream()
        .map(r -> DateUtils.toStringYYYYMMDDHHMMSS((OffsetDateTime) r.get("report_time"),
            ZoneId.of("UTC")) + "_" + MapUtils.getString(r, "flow_id"))
        .collect(Collectors.toList());

    return queryFlowLogsByIds(queryId, tableName, "*", sort, rowIds);
  }

  @Override
  public List<Map<String, Object>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      List<String> flowIds, Pageable page, String columns) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 数据源
    String tableName = getTableName(queryVO);

    StringBuilder sql = buildSelectStatement(securityQueryId, tableName, columns,
        Sets.newHashSet("report_time", "flow_id"));

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where 1 = 1 ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 会话ID
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds) ");
      params.put("flowIds", flowIds);
    }

    // dsl
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String, Map<String, Object>> dslTuples = dslConverter.converte(queryVO.getDsl(),
            false, 9, true, false);
        whereSql.append(" and ");
        whereSql.append(dslTuples.getT1());
        params.putAll(dslTuples.getT2());
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析dsl失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "dsl格式错误");
      }
    }

    sql.append(whereSql);
    PageUtils.appendSort(sql, page.getSort(), FlowLogQueryVO.class);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs limit size, inner sql: {}, params: {}", sql.toString(), params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      List<String> networkIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (item.get("network_id") != null) {
        String[] networkIdArray = JsonHelper.deserialize(
            JsonHelper.serialize(item.get("network_id")), new TypeReference<String[]>() {
            }, false);
        if (networkIdArray != null) {
          networkIdList = Lists.newArrayList(networkIdArray);
        }
      }
      item.put("network_id", networkIdList);
      List<String> serviceIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (item.get("service_id") != null) {
        String[] serviceIdArray = JsonHelper.deserialize(
            JsonHelper.serialize(item.get("service_id")), new TypeReference<String[]>() {
            }, false);
        if (serviceIdArray != null) {
          serviceIdList = Lists.newArrayList(serviceIdArray);
        }
      }
      item.put("service_id", serviceIdList);
    });

    if (CollectionUtils.isEmpty(result)) {
      return Lists.newArrayListWithCapacity(0);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowLogByFlowId(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogByFlowId(String queryId, FlowLogQueryVO queryVO,
      String columns) {

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    StringBuilder sql = buildSelectStatement(securityQueryId, getTableName(queryVO), columns,
        Sets.newHashSet("report_time"));

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 过滤流ID
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where flow_id = :flow_id ");
    params.put("flow_id", queryVO.getFlowId());

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    sql.append(whereSql);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs by flow id, sql: {}, params: {}", sql.toString(), params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      List<String> networkIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (item.get("network_id") != null) {
        String[] networkIdArray = JsonHelper.deserialize(
            JsonHelper.serialize(item.get("network_id")), new TypeReference<String[]>() {
            }, false);
        if (networkIdArray != null) {
          networkIdList = Lists.newArrayList(networkIdArray);
        }
      }
      item.put("network_id", networkIdList);
      List<String> serviceIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (item.get("service_id") != null) {
        String[] serviceIdArray = JsonHelper.deserialize(
            JsonHelper.serialize(item.get("service_id")), new TypeReference<String[]>() {
            }, false);
        if (serviceIdArray != null) {
          serviceIdList = Lists.newArrayList(serviceIdArray);
        }
      }
      item.put("service_id", serviceIdList);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#countFlowLogs(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.util.List)
   */
  @Override
  public long countFlowLogs(String queryId, FlowLogQueryVO queryVO, List<String> ids) {
    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return 0;
    }

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    StringBuilder totalSql = new StringBuilder(securityQueryId);
    totalSql.append("select count(1) from ");
    totalSql.append(getTableName(queryVO));

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, totalSql, params);
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, totalSql, params);
    }

    return queryForLongWithExceptionHandle(totalSql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowLogsByIds(java.lang.String, java.lang.String, java.lang.String, com.machloop.alpha.common.base.page.Sort, java.util.List)
   */
  public List<Map<String, Object>> queryFlowLogsByIds(String queryId, String tableName,
      String columns, Sort sort, List<String> ids) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // id过滤，id为reportTime_flowId，提前进行拆分
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> reportTimeConditions = Lists.newArrayListWithCapacity(ids.size());
    List<String> flowIdConditions = Lists.newArrayListWithCapacity(ids.size());
    int index = 0;
    for (String id : ids) {
      String[] filters = StringUtils.split(id, "_");
      // report_time
      reportTimeConditions.add(String.format("toDateTime64(:report_time%s, 9, 'UTC')", index));
      params.put("report_time" + index, filters[0]);
      // flow_id
      flowIdConditions.add(String.format(":flow_id%s", index));
      params.put("flow_id" + index, filters[1]);

      index += 1;
    }

    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append("select * from (");

    // 嵌套子查询
    Set<String> sortProperties = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Iterator<Order> iterator = sort.iterator();
    while (iterator.hasNext()) {
      sortProperties.add(iterator.next().getProperty());
    }
    StringBuilder innersql = buildSelectStatement("", tableName, columns, sortProperties);
    // 主键report_time过滤
    innersql.append(" where report_time in (");
    innersql.append(StringUtils.join(reportTimeConditions, ",")).append(")");
    sql.append(innersql).append(")");

    // flow_id过滤
    sql.append(" where flow_id in (");
    sql.append(StringUtils.join(flowIdConditions, ",")).append(")");

    PageUtils.appendSort(sql, sort, FlowLogQueryVO.class);
    sql.append(" limit ").append(QUERY_BY_ID_LIMIT_SIZE);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow logs page by ids, ids: {}, sql: {}, params: {}",
          StringUtils.join(ids), sql.toString(), params);
    }

    List<Map<String, Object>> allResult = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    List<Map<String, Object>> filterResult = allResult.stream().filter(item -> ids.contains(
        DateUtils.toStringYYYYMMDDHHMMSS((OffsetDateTime) item.get("report_time"), ZoneId.of("UTC"))
            + "_" + MapUtils.getString(item, "flow_id")))
        .map(item -> {
          List<String> networkIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
          if (item.get("network_id") != null) {
            String[] networkIdArray = JsonHelper.deserialize(
                JsonHelper.serialize(item.get("network_id")), new TypeReference<String[]>() {
                }, false);
            if (networkIdArray != null) {
              networkIdList = Lists.newArrayList(networkIdArray);
            }
          }
          item.put("network_id", networkIdList);
          List<String> serviceIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
          if (item.get("service_id") != null) {
            String[] serviceIdArray = JsonHelper.deserialize(
                JsonHelper.serialize(item.get("service_id")), new TypeReference<String[]>() {
                }, false);
            if (serviceIdArray != null) {
              serviceIdList = Lists.newArrayList(serviceIdArray);
            }
          }
          item.put("service_id", serviceIdList);

          return item;
        }).collect(Collectors.toList());

    return filterResult;
  }

  /**
   * @throws QueryCanceledException 
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowLogGroupByStatistics(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogGroupByStatistics(String queryId,
      FlowLogQueryVO queryVO, String termFieldName, int termSize) throws QueryCanceledException {

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 数据源
    String tableName = getTableName(queryVO);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();

    StringBuilder sql = new StringBuilder(securityQueryId);
    if (StringUtils.equals(termFieldName, "ip_protocol")) {
      sql.append("select ip_protocol, count(1) as count ");
      sql.append(" from ").append(tableName);
      enrichWhereSql(queryVO, whereSql, params);
      sql.append(whereSql);
      sql.append(" group by ip_protocol order by count desc ");
      sql.append(" limit ").append(termSize);
    } else if (StringUtils.equals(termFieldName, "l7_protocol_id")) {
      sql.append("select l7_protocol_id, count(1) as count ");
      sql.append(" from ").append(tableName);
      enrichWhereSql(queryVO, whereSql, params);
      sql.append(whereSql);
      sql.append(" group by l7_protocol_id order by count desc ");
      sql.append(" limit ").append(termSize);
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow log groupby statistics, sql: {}, params: {}", sql.toString(),
          params);
    }
    List<Map<String, Object>> result = queryWithExceptionThrow(sql.toString(), params,
        new ColumnMapRowMapper());
    return result;
  }

  /**
   * @throws QueryCanceledException 
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowLogDateHistogramStatistics(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, int)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogDateHistogramStatistics(String queryId,
      FlowLogQueryVO queryVO, int histogramInterval) throws QueryCanceledException {

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();

    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append("select ");
    sql.append(" toStartOfInterval(report_time, INTERVAL :interval second) as timestamp, ");
    sql.append(" count(1) as count ");
    sql.append(" from ").append(getTableName(queryVO));
    enrichWhereSql(queryVO, whereSql, params);
    sql.append(whereSql);
    sql.append(" group by timestamp order by timestamp ");
    params.put("interval", histogramInterval);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow log date histogram statistics, sql: {}, params: {}", sql.toString(),
          params);
    }
    List<Map<String, Object>> result = queryWithExceptionThrow(sql.toString(), params,
        new ColumnMapRowMapper());
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowLogStatisticsGroupByIp(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, int, com.machloop.alpha.common.base.page.Sort)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogStatisticsGroupByIp(String queryId,
      FlowLogQueryVO queryVO, int termSize, Sort sort) {

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 数据源
    String tableName = getTableName(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);

    StringBuilder innersql = new StringBuilder(securityQueryId);
    innersql.append("select ipv4_initiator,ipv4_responder,ipv6_initiator,ipv6_responder, ");
    innersql.append(" sum(total_bytes) as total_bytes ");
    innersql.append(" from ").append(tableName);
    innersql.append(whereSql);
    innersql.append(" group by ipv4_initiator, ipv4_responder, ipv6_initiator, ipv6_responder ");
    innersql.append(" order by total_bytes desc ");
    innersql.append(" limit ").append(termSize);
    List<Map<String, Object>> ipresult = queryWithExceptionHandle(innersql.toString(), params,
        new ColumnMapRowMapper());

    if (ipresult.isEmpty()) {
      return result;
    }

    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append("select ipv4_initiator,ipv4_responder,ipv6_initiator,ipv6_responder, ");
    sql.append(" count(1) as count, ");
    sql.append(" sum(upstream_bytes) as upstream_bytes, ");
    sql.append(" sum(downstream_bytes) as downstream_bytes, ");
    sql.append(" sum(total_bytes) as total_bytes, sum(upstream_packets) as upstream_packets, ");
    sql.append(" sum(downstream_packets) as downstream_packets, ");
    sql.append(" sum(total_packets) as total_packets");
    sql.append(" from ").append(tableName);
    sql.append(whereSql);
    List<String> ipConditionList = ipresult.stream().map(item -> {
      String ipv4Initiator = MapUtils.getString(item, "ipv4_initiator");
      String ipv4Responser = MapUtils.getString(item, "ipv4_responder");
      String ipv6Initiator = MapUtils.getString(item, "ipv6_initiator");
      String ipv6Responser = MapUtils.getString(item, "ipv6_responder");
      StringBuilder sb = new StringBuilder();
      sb.append("(1=1 ");
      if (StringUtils.isNotBlank(ipv4Initiator)) {
        sb.append(" and ipv4_initiator=toIPv4('").append(ipv4Initiator).append("')");
      }
      if (StringUtils.isNotBlank(ipv4Responser)) {
        sb.append(" and ipv4_responder=toIPv4('").append(ipv4Responser).append("')");
      }
      if (StringUtils.isNotBlank(ipv6Initiator)) {
        sb.append(" and ipv6_initiator=toIPv6('").append(ipv6Initiator).append("')");
      }
      if (StringUtils.isNotBlank(ipv6Responser)) {
        sb.append(" and ipv6_responder=toIPv6('").append(ipv6Responser).append("')");
      }
      sb.append(")");
      return sb.toString();
    }).collect(Collectors.toList());
    sql.append(" and (");
    sql.append(StringUtils.join(ipConditionList, " or "));
    sql.append(" ) ");
    sql.append(" group by ipv4_initiator, ipv4_responder, ipv6_initiator, ipv6_responder ");

    sql.append(" order by total_bytes desc ");
    sql.append(" limit ").append(termSize);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query flow log date group by ip statistics, sql: {}, params: {}",
          sql.toString(), params);
    }
    result = queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk#queryFlowIds(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.util.List, com.machloop.alpha.common.base.page.Sort, int)
   */
  @Override
  public List<Object> queryFlowIds(String queryId, FlowLogQueryVO queryVO, List<String> ids,
      Sort sort, int size) {
    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return Lists.newArrayListWithCapacity(0);
    }

    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryId)
        ? String.format("/*%s*/", Base64Utils.encode(queryId))
        : "";

    // 查询符合条件的记录flow_id，并去重
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(" select distinct flow_id from ").append(getTableName(queryVO));

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, sql, params);
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, sql, params);
    }

    PageUtils.appendSort(sql, sort, FlowLogQueryVO.class);

    sql.append(" limit ").append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query mutl flow_id, sql: {}, params: {}", sql.toString(), params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    if (CollectionUtils.isEmpty(result)) {
      return Lists.newArrayListWithCapacity(0);
    }

    return result.stream().map(item -> item.get("flow_id")).collect(Collectors.toList());
  }

  private String getTableName(FlowLogQueryVO queryVO) {
    String tableName = TABLE_FLOW_LOG_RECORD;
    if (StringUtils.equals(queryVO.getSourceType(), FpcConstants.SOURCE_TYPE_PACKET_FILE)) {
      PacketAnalysisSubTaskDO offlineAnalysisSubTask = packetAnalysisSubTaskDao
          .queryPacketAnalysisSubTask(queryVO.getPacketFileId());
      if (StringUtils.isBlank(offlineAnalysisSubTask.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "未查询到离线分析子任务");
      }

      tableName = String.join("_", TABLE_FLOW_LOG_RECORD, offlineAnalysisSubTask.getTaskId());

      // 判断离线数据包文件的分析结果是否存在
      List<Map<String, Object>> existResult = clickHouseTemplate.getJdbcTemplate()
          .query(String.format("show tables from %s where name = '%s'",
              ManagerConstants.FPC_DATABASE, tableName), new ColumnMapRowMapper());
      if (CollectionUtils.isEmpty(existResult)) {
        LOGGER.warn("search failed, offline packet file flowlog not found: {}", tableName);
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "离线分析结果异常，未查找到会话详单");
      }
    }

    return tableName;
  }

  private StringBuilder buildSelectStatement(String queryId, String tableName, String columns,
      Set<String> sortPropertys) {
    StringBuilder sql = new StringBuilder(queryId);

    if (!StringUtils.equals(columns, "*")) {
      // 必须查询排序字段
      sortPropertys = sortPropertys.stream()
          .filter(sortProperty -> !StringUtils.contains(columns, sortProperty))
          .collect(Collectors.toSet());

      sql.append("select ").append(CsvUtils.convertCollectionToCSV(sortPropertys));
      if (StringUtils.isNotBlank(columns)) {
        sql.append(CollectionUtils.isNotEmpty(sortPropertys) ? "," : "").append(columns);
      }
      sql.append(" from ").append(tableName);
      String newSql = sql.toString()
          .replace("ip_nat_initiator", "IPv6NumToString(ip_nat_initiator) as ip_nat_initiator")
          .replace("ip_nat_responder", "IPv6NumToString(ip_nat_responder) as ip_nat_responder");
      sql = new StringBuilder(newSql);
      return sql;
    }

    sql.append("select interface, flow_id, network_id, service_id, start_time, report_time, ");
    sql.append(" duration, flow_continued, packet_sigseq, upstream_bytes, downstream_bytes, ");
    sql.append(" total_bytes, upstream_packets, downstream_packets, total_packets, ");
    sql.append(" upstream_payload_bytes, downstream_payload_bytes, total_payload_bytes, ");
    sql.append(" upstream_payload_packets, downstream_payload_packets, total_payload_packets, ");
    sql.append(" tcp_client_network_latency, tcp_client_network_latency_flag, ");
    sql.append(" tcp_server_network_latency, tcp_server_network_latency_flag, ");
    sql.append(" server_response_latency, server_response_latency_flag, ");
    sql.append(" tcp_client_loss_bytes, tcp_server_loss_bytes, ");
    sql.append(" tcp_client_zero_window_packets, tcp_server_zero_window_packets, ");
    sql.append(" tcp_session_state, tcp_established_success_flag, tcp_established_fail_flag, ");
    sql.append(" established_sessions, tcp_syn_packets, tcp_syn_ack_packets, ");
    sql.append(" tcp_syn_rst_packets, tcp_client_packets, tcp_server_packets, ");
    sql.append(" tcp_client_retransmission_packets, tcp_server_retransmission_packets, ");
    sql.append(" ethernet_type, ethernet_initiator, ethernet_responder, ethernet_protocol, ");
    sql.append(" vlan_id, hostgroup_id_initiator, hostgroup_id_responder, ");
    sql.append(" ip_locality_initiator, ip_locality_responder, ");
    sql.append(" ipv4_initiator, ipv4_responder, ipv6_initiator, ipv6_responder, ");
    sql.append(" ip_protocol, port_initiator, port_responder, l7_protocol_id, ");
    sql.append(" application_category_id, application_subcategory_id, application_id, ");
    sql.append(" malicious_application_id, country_id_initiator, province_id_initiator, ");
    sql.append(" city_id_initiator, district_initiator, aoi_type_initiator, aoi_name_initiator, ");
    sql.append(" country_id_responder, province_id_responder, city_id_responder, ");
    sql.append(" district_responder, aoi_type_responder, aoi_name_responder, ");
    sql.append(
        " IPv6NumToString(ip_nat_initiator) as ip_nat_initiator, IPv6NumToString(ip_nat_responder) as ip_nat_responder, ");
    sql.append(" port_nat_initiator, port_nat_responder ");
    sql.append(" from ").append(tableName);
    return sql;
  }

  private void enrichWhereSql(FlowLogQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where 1 = 1 ");

    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String, Map<String, Object>> dsl = dslConverter.converte(queryVO.getDsl(), false,
            queryVO.getTimePrecision(), queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime());
        whereSql.append(" and ");
        whereSql.append(dsl.getT1());
        params.putAll(dsl.getT2());
        return;
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and has(network_id, :networkId)=1 ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      whereSql.append(" and has(service_id, :serviceId)=1 ");
      params.put("serviceId", queryVO.getServiceId());
    }
    if (StringUtils.isNotBlank(queryVO.getInterfaceName())) {
      whereSql.append(" and interface = :interface ");
      params.put("interface", queryVO.getInterfaceName());
    }
    if (queryVO.getFlowId() != null) {
      whereSql.append(" and flow_id = :flow_id ");
      params.put("flow_id", queryVO.getFlowId());
    }
    if (queryVO.getFlowContinued() != null) {
      whereSql.append(" and flow_continued = :flow_continued ");
      params.put("flow_continued", queryVO.getFlowContinued());
    }
    if (queryVO.getTcpSessionState() != null) {
      whereSql.append(" and tcp_session_state = :tcp_session_state ");
      params.put("tcp_session_state", queryVO.getTcpSessionState());
    }
    if (queryVO.getEthernetType() != null) {
      whereSql.append(" and ethernet_type = :ethernet_type ");
      params.put("ethernet_type", queryVO.getEthernetType());
    }
    if (StringUtils.isNotBlank(queryVO.getEthernetInitiator())) {
      whereSql.append(" and ethernet_initiator = :ethernet_initiator ");
      params.put("ethernet_initiator", queryVO.getEthernetInitiator());
    }
    if (StringUtils.isNotBlank(queryVO.getEthernetResponder())) {
      whereSql.append(" and ethernet_responder = :ethernet_responder ");
      params.put("ethernet_responder", queryVO.getEthernetResponder());
    }
    if (StringUtils.isNotBlank(queryVO.getEthernetProtocol())) {
      whereSql.append(" and ethernet_protocol = :ethernet_protocol ");
      params.put("ethernet_protocol", queryVO.getEthernetProtocol());
    }
    if (queryVO.getVlanId() != null) {
      whereSql.append(" and vlan_id = :vlan_id ");
      params.put("vlan_id", queryVO.getVlanId());
    }
    if (queryVO.getIpLocalityInitiator() != null) {
      whereSql.append(" and ip_locality_initiator = :ip_locality_initiator ");
      params.put("ip_locality_initiator", queryVO.getIpLocalityInitiator());
    }
    if (StringUtils.isNotBlank(queryVO.getIpInitiator())) {
      enrichIpCondition(queryVO.getIpInitiator(), "ipv4_initiator", "ipv6_initiator", whereSql,
          params, "initiator");
    }
    if (queryVO.getIpLocalityResponder() != null) {
      whereSql.append(" and ip_locality_responder = :ip_locality_responder ");
      params.put("ip_locality_responder", queryVO.getIpLocalityResponder());
    }
    if (StringUtils.isNotBlank(queryVO.getIpResponder())) {
      enrichIpCondition(queryVO.getIpResponder(), "ipv4_responder", "ipv6_responder", whereSql,
          params, "responder");
    }
    if (StringUtils.isNotBlank(queryVO.getIpProtocol())) {
      whereSql.append(" and ip_protocol = :ip_protocol ");
      params.put("ip_protocol", queryVO.getIpProtocol());
    }
    if (StringUtils.isNotBlank(queryVO.getPortInitiator())) {
      enrichPortCondition(queryVO.getPortInitiator(), "port_initiator", whereSql, params,
          "port_initiator");
    }
    if (StringUtils.isNotBlank(queryVO.getPortResponder())) {
      enrichPortCondition(queryVO.getPortResponder(), "port_responder", whereSql, params,
          "port_responder");
    }
    if (StringUtils.isNotBlank(queryVO.getL7ProtocolId())) {
      whereSql.append(" and l7_protocol_id = :l7_protocol_id ");
      params.put("l7_protocol_id", queryVO.getL7ProtocolId());
    }
    if (StringUtils.isNotBlank(queryVO.getApplicationIds())) {
      List<String> applicationIdList = CsvUtils.convertCSVToList(queryVO.getApplicationIds());
      for (String item : applicationIdList) {
        if (!StringUtils.isNumeric(item)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "应用ID参数错误");
        }
      }
      whereSql.append(" and application_id in (:application_id) ");
      params.put("application_id", applicationIdList);
    }
    if (StringUtils.isNotBlank(queryVO.getMaliciousApplicationIds())) {
      List<String> maliciousAppIdList = CsvUtils
          .convertCSVToList(queryVO.getMaliciousApplicationIds());
      for (String item : maliciousAppIdList) {
        if (!StringUtils.isNumeric(item)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "僵木蠕应用ID参数错误");
        }
      }
      whereSql.append(" and malicious_application_id in (:malicious_application_id) ");
      params.put("malicious_application_id", maliciousAppIdList);
    }
    if (StringUtils.isNotBlank(queryVO.getCountryIdInitiator())) {
      whereSql.append(" and country_id_initiator = :country_id_initiator ");
      params.put("country_id_initiator", queryVO.getCountryIdInitiator());
    }
    if (StringUtils.isNotBlank(queryVO.getProvinceIdInitiator())) {
      whereSql.append(" and province_id_initiator = :province_id_initiator ");
      params.put("province_id_initiator", queryVO.getProvinceIdInitiator());
    }
    if (StringUtils.isNotBlank(queryVO.getCityIdInitiator())) {
      whereSql.append(" and city_id_initiator = :city_id_initiator ");
      params.put("city_id_initiator", queryVO.getCityIdInitiator());
    }

    if (StringUtils.isNotBlank(queryVO.getCountryIdResponder())) {
      whereSql.append(" and country_id_responder = :country_id_responder ");
      params.put("country_id_responder", queryVO.getCountryIdResponder());
    }
    if (StringUtils.isNotBlank(queryVO.getProvinceIdResponder())) {
      whereSql.append(" and province_id_responder = :province_id_responder ");
      params.put("province_id_responder", queryVO.getProvinceIdResponder());
    }
    if (StringUtils.isNotBlank(queryVO.getCityIdResponder())) {
      whereSql.append(" and city_id_responder = :city_id_responder ");
      params.put("city_id_responder", queryVO.getCityIdResponder());
    }

    if (StringUtils.isNotBlank(queryVO.getDuration())) {
      enrichNumericCondition(queryVO.getDuration(), "duration", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getUpstreamBytes())) {
      enrichNumericCondition(queryVO.getUpstreamBytes(), "upstream_bytes", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getDownstreamBytes())) {
      enrichNumericCondition(queryVO.getDownstreamBytes(), "downstream_bytes", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTotalBytes())) {
      enrichNumericCondition(queryVO.getTotalBytes(), "total_bytes", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getUpstreamPackets())) {
      enrichNumericCondition(queryVO.getUpstreamPackets(), "upstream_packets", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getDownstreamPackets())) {
      enrichNumericCondition(queryVO.getDownstreamPackets(), "downstream_packets", whereSql,
          params);
    }
    if (StringUtils.isNotBlank(queryVO.getTotalPackets())) {
      enrichNumericCondition(queryVO.getTotalPackets(), "total_packets", whereSql, params);
    }

    if (StringUtils.isNotBlank(queryVO.getUpstreamPayloadBytes())) {
      enrichNumericCondition(queryVO.getUpstreamPayloadBytes(), "upstream_payload_bytes", whereSql,
          params);
    }
    if (StringUtils.isNotBlank(queryVO.getDownstreamPayloadBytes())) {
      enrichNumericCondition(queryVO.getDownstreamPayloadBytes(), "downstream_payload_bytes",
          whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTotalPayloadBytes())) {
      enrichNumericCondition(queryVO.getTotalPayloadBytes(), "total_payload_bytes", whereSql,
          params);
    }

    if (StringUtils.isNotBlank(queryVO.getUpstreamPayloadPackets())) {
      enrichNumericCondition(queryVO.getUpstreamPayloadPackets(), "upstream_payload_packets",
          whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getDownstreamPayloadPackets())) {
      enrichNumericCondition(queryVO.getDownstreamPayloadPackets(), "downstream_payload_packets",
          whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTotalPayloadPackets())) {
      enrichNumericCondition(queryVO.getTotalPayloadPackets(), "total_payload_packets", whereSql,
          params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpClientNetworkLatency())) {
      enrichNumericCondition(queryVO.getTcpClientNetworkLatency(), "tcp_client_network_latency",
          whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpServerNetworkLatency())) {
      enrichNumericCondition(queryVO.getTcpServerNetworkLatency(), "tcp_server_network_latency",
          whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getServerResponseLatency())) {
      enrichNumericCondition(queryVO.getServerResponseLatency(), "server_response_latency",
          whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpClientZeroWindowPackets())) {
      enrichNumericCondition(queryVO.getTcpClientZeroWindowPackets(),
          "tcp_client_zero_window_packets", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpServerZeroWindowPackets())) {
      enrichNumericCondition(queryVO.getTcpServerZeroWindowPackets(),
          "tcp_server_zero_window_packets", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpEstablishedSuccessFlag())) {
      enrichNumericCondition(queryVO.getTcpEstablishedSuccessFlag(), "tcp_established_success_flag",
          whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpEstablishedFailFlag())) {
      enrichNumericCondition(queryVO.getTcpEstablishedFailFlag(), "tcp_established_fail_flag",
          whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getEstablishedSessions())) {
      enrichNumericCondition(queryVO.getEstablishedSessions(), "established_sessions", whereSql,
          params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpSynPackets())) {
      enrichNumericCondition(queryVO.getTcpSynPackets(), "tcp_syn_packets", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpSynAckPackets())) {
      enrichNumericCondition(queryVO.getTcpSynAckPackets(), "tcp_syn_ack_packets", whereSql,
          params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpSynRstPackets())) {
      enrichNumericCondition(queryVO.getTcpSynRstPackets(), "tcp_syn_rst_packets", whereSql,
          params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpClientPackets())) {
      enrichNumericCondition(queryVO.getTcpClientPackets(), "tcp_client_packets", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpServerPackets())) {
      enrichNumericCondition(queryVO.getTcpServerPackets(), "tcp_server_packets", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpClientRetransmissionPackets())) {
      enrichNumericCondition(queryVO.getTcpClientRetransmissionPackets(),
          "tcp_client_retransmission_packets", whereSql, params);
    }
    if (StringUtils.isNotBlank(queryVO.getTcpServerRetransmissionPackets())) {
      enrichNumericCondition(queryVO.getTcpServerRetransmissionPackets(),
          "tcp_server_retransmission_packets", whereSql, params);
    }
  }

  private void enrichNumericCondition(String numCondition, String fieldName, StringBuilder whereSql,
      Map<String, Object> params) {
    String condition = StringUtils.strip(numCondition);
    if (StringUtils.isNumeric(condition)) {
      whereSql.append(String.format(" and %s = :%s ", fieldName, fieldName));
    } else if (StringUtils.startsWith(condition, ">=")) {
      whereSql.append(String.format(" and %s >= :%s ", fieldName, fieldName));
      condition = StringUtils.substring(condition, 2);
    } else if (StringUtils.startsWith(condition, "<=")) {
      whereSql.append(String.format(" and %s <= :%s ", fieldName, fieldName));
      condition = StringUtils.substring(condition, 2);
    } else if (StringUtils.startsWith(condition, "=")) {
      whereSql.append(String.format(" and %s = :%s ", fieldName, fieldName));
      condition = StringUtils.substring(condition, 1);
    } else if (StringUtils.startsWith(condition, "<")) {
      whereSql.append(String.format(" and %s < :%s ", fieldName, fieldName));
      condition = StringUtils.substring(condition, 1);
    } else if (StringUtils.startsWith(condition, ">")) {
      whereSql.append(String.format(" and %s > :%s ", fieldName, fieldName));
      condition = StringUtils.substring(condition, 1);
    } else {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的查询条件");
    }
    condition = StringUtils.strip(condition);
    if (!StringUtils.isNumeric(condition)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "大于小于查询值必须为数字类型");
    }
    params.put(fieldName, Long.parseLong(condition));
  }

  private void enrichIpCondition(String ipCondition, String ipv4FieldName, String ipv6FieldName,
      StringBuilder whereSql, Map<String, Object> params, String paramsIndexIdent) {
    List<String> ipv4List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> ipv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> ipv6List = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, String>> ipv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<String> ipv4CidrList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> ipv6CidrList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    String[] ipConditionList = StringUtils.split(ipCondition, ",");

    if (ipConditionList.length > FpcConstants.HOSTGROUP_MAX_IP_COUNT) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "查询的IP地址条目数过多, 请修改查询。");
    }

    // 按ip类型分类
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
        if (NetworkUtils.isInetAddress(ip, IpVersion.V4)) {
          ipv4List.add(ip);
        } else if (NetworkUtils.isCidr(ip, IpVersion.V4)) {
          ipv4CidrList.add(ip);
        } else if (NetworkUtils.isInetAddress(ip, IpVersion.V6)
            || NetworkUtils.isCidr(ip, IpVersion.V6)) {
          ipv6List.add(ip);
        } else if (NetworkUtils.isCidr(ip, IpVersion.V6)) {
          ipv6CidrList.add(ip);
        }
      }
    }

    List<String> ipConditionSqlList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    // 拼接sql
    if (CollectionUtils.isNotEmpty(ipv4List) || CollectionUtils.isNotEmpty(ipv6List)
        || CollectionUtils.isNotEmpty(ipv4CidrList) || CollectionUtils.isNotEmpty(ipv6CidrList)
        || CollectionUtils.isNotEmpty(ipv4RangeList) || CollectionUtils.isNotEmpty(ipv6RangeList)) {
      int index = 0;

      // 单ipv4
      if (CollectionUtils.isNotEmpty(ipv4List)) {
        List<String> tmpList = Lists.newArrayListWithCapacity(ipv4List.size());
        for (String ip : ipv4List) {
          tmpList.add("toIPv4(:ipv4_" + index + paramsIndexIdent + ")");
          params.put("ipv4_" + index + paramsIndexIdent, ip);
          index += 1;
        }
        ipConditionSqlList
            .add(String.format(" (%s in (%s)) ", ipv4FieldName, StringUtils.join(tmpList, ",")));
      }

      // 单ipv6
      if (CollectionUtils.isNotEmpty(ipv6List)) {
        List<String> tmpList = Lists.newArrayListWithCapacity(ipv6List.size());
        for (String ip : ipv6List) {
          tmpList.add("toIPv6(:ipv6_" + index + paramsIndexIdent + ")");
          params.put("ipv6_" + index + paramsIndexIdent, ip);
          index += 1;
        }
        ipConditionSqlList
            .add(String.format(" (%s in (%s)) ", ipv6FieldName, StringUtils.join(tmpList, ",")));
      }

      // ipv4掩码
      if (CollectionUtils.isNotEmpty(ipv4CidrList)) {
        for (String ip : ipv4CidrList) {
          ipConditionSqlList.add(String.format(
              " (%s between IPv4CIDRToRange(toIPv4(:ipv4_%s), :cidr_%s).1 and IPv4CIDRToRange(toIPv4(:ipv4_%s), :cidr_%s).2) ",
              ipv4FieldName, index + paramsIndexIdent, index + paramsIndexIdent,
              index + paramsIndexIdent, index + paramsIndexIdent));
          String[] ipAndCidr = ip.split("/");
          params.put("ipv4_" + index + paramsIndexIdent, ipAndCidr[0]);
          params.put("cidr_" + index + paramsIndexIdent, Integer.parseInt(ipAndCidr[1]));
          index += 1;
        }
      }
      // ipv6掩码
      if (CollectionUtils.isNotEmpty(ipv6CidrList)) {
        for (String ip : ipv6CidrList) {
          ipConditionSqlList.add(String.format(
              " (%s between IPv6CIDRToRange(toIPv6(:ipv6_%s), :cidr_%s).1 and IPv6CIDRToRange(toIPv6(:ipv6_%s), :cidr_%s).2) ",
              ipv6FieldName, index + paramsIndexIdent, index + paramsIndexIdent,
              index + paramsIndexIdent, index + paramsIndexIdent));
          String[] ipAndCidr = ip.split("/");
          params.put("ipv6_" + index + paramsIndexIdent, ipAndCidr[0]);
          params.put("cidr_" + index + paramsIndexIdent, Integer.parseInt(ipAndCidr[1]));
          index += 1;
        }
      }

      // ipv4范围
      for (Tuple2<String, String> range : ipv4RangeList) {
        ipConditionSqlList
            .add(String.format(" (%s between toIPv4(:ipv4_start%s) and toIPv4(:ipv4_end%s)) ",
                ipv4FieldName, index + paramsIndexIdent, index + paramsIndexIdent));
        params.put("ipv4_start" + index + paramsIndexIdent, range.getT1());
        params.put("ipv4_end" + index + paramsIndexIdent, range.getT2());
        index += 1;
      }
      // ipv6范围
      for (Tuple2<String, String> range : ipv6RangeList) {
        ipConditionSqlList
            .add(String.format(" (%s between toIPv6(:ipv6_start%s) and toIPv6(:ipv6_end%s)) ",
                ipv6FieldName, index + paramsIndexIdent, index + paramsIndexIdent));
        params.put("ipv6_start" + index + paramsIndexIdent, range.getT1());
        params.put("ipv6_end" + index + paramsIndexIdent, range.getT2());
        index += 1;
      }
      whereSql.append(" and ( ");
      whereSql.append(String.join(" or ", ipConditionSqlList));
      whereSql.append(" ) ");
    }
  }

  private void enrichPortCondition(String ports, String fieldName, StringBuilder whereSql,
      Map<String, Object> params, String paramsIndexIdent) {
    if (StringUtils.contains(ports, "-")) {
      // 端口范围 80-90
      String[] portRange = StringUtils.split(ports, "-");
      if (!NetworkUtils.isInetAddressPort(StringUtils.trim(portRange[0]))
          || !NetworkUtils.isInetAddressPort(StringUtils.trim(portRange[1]))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的端口");
      }
      String sql = String.format(" and (%s >= :port_start%s and %s <= :port_end%s) ", fieldName,
          paramsIndexIdent, fieldName, paramsIndexIdent);

      params.put("port_start" + paramsIndexIdent, Integer.valueOf(StringUtils.trim(portRange[0])));
      params.put("port_end" + paramsIndexIdent, Integer.valueOf(StringUtils.trim(portRange[1])));
      whereSql.append(sql);
    } else if (StringUtils.contains(ports, ",")) {
      // 单个或多个端口 80,8080,8090
      List<Integer> portList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      for (String port : StringUtils.split(ports, ",")) {
        port = StringUtils.trim(port);
        if (!NetworkUtils.isInetAddressPort(StringUtils.trim(port))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请输入正确的端口");
        }
        if (StringUtils.isNotBlank(port)) {
          portList.add(Integer.valueOf(port));
        }
      }
      if (!portList.isEmpty()) {
        whereSql.append(String.format(" and %s in (:portList%s) ", fieldName, paramsIndexIdent));
        params.put("portList" + paramsIndexIdent, portList);
      }
    } else {
      enrichNumericCondition(ports, fieldName, whereSql, params);
    }
  }

  @SuppressWarnings("unused")
  private void enrichContainTimeRange(Date startTime, Date endTime, StringBuilder whereSql,
      Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(" and ( (start_time >= toDateTime64(:start_time, 9, 'UTC') ");
    whereSql.append(" and start_time <= toDateTime64(:end_time, 9, 'UTC')) ");
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    // 日志结束时间在查询时间范围中
    whereSql.append(" or (end_time >= toDateTime64(:start_time, 9, 'UTC') ");
    whereSql.append(" and end_time <= toDateTime64(:end_time, 9, 'UTC') ");
    whereSql.append(" and start_time >= toDateTime64(:aging_start_time, 9, 'UTC') ");
    whereSql.append(" and start_time <= toDateTime64(:end_time, 9, 'UTC')) ");
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("aging_start_time",
        DateUtils.toStringFormat(
            DateUtils.beforeSecondDate(startTime,
                (int) ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS / 1000),
            "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    // 起止时间小于老化时间, 则存在长连接时间范围覆盖查询时间, 也需要查出来
    if (startTime != null && endTime != null && (endTime.getTime()
        - startTime.getTime() <= ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS)) {
      whereSql.append(" or (start_time >= toDateTime64(:aging_start_time, 9, 'UTC')");
      whereSql.append(" and start_time <= toDateTime64(:start_time, 9, 'UTC') ");
      whereSql.append(" and end_time >= toDateTime64(:end_time, 9, 'UTC') ");
      whereSql.append(" and end_time <= toDateTime64(:aging_end_time, 9, 'UTC'))");

      params.put("aging_start_time",
          DateUtils.toStringFormat(
              DateUtils.beforeSecondDate(startTime,
                  (int) ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS / 1000),
              "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      params.put("aging_end_time",
          DateUtils.toStringFormat(
              DateUtils.afterSecondDate(endTime,
                  (int) ManagerConstants.ENGINE_LOG_AGINGTIME_MILLS / 1000),
              "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    whereSql.append(")");
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    whereSql.append(String.format(" and report_time %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and report_time %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  private void enrichIdCondition(List<String> ids, StringBuilder whereSql,
      Map<String, Object> params) {
    List<String> rowsCondition = Lists.newArrayListWithCapacity(ids.size());
    int index = 0;
    for (String id : ids) {
      String[] split = StringUtils.split(id, "_");
      rowsCondition.add(
          String.format(" (report_time=:report_time%d and flow_id=:flow_id%d) ", index, index));
      params.put("report_time" + index, split[0]);
      params.put("flow_id" + index, split[1]);
      index += 1;
    }

    whereSql.append(" and (").append(StringUtils.join(rowsCondition, " or ")).append(")");
  }

  private <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query flow log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query flow log failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }

  private <T> List<T> queryWithExceptionThrow(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) throws QueryCanceledException {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = clickHouseTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        throw new QueryCanceledException();
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query flow log failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else {
        throw e;
      }
    }
    return result;
  }

  private Long queryForLongWithExceptionHandle(String sql, Map<String, ?> paramMap) {
    Long result = 0L;
    try {
      result = clickHouseTemplate.getJdbcTemplate().queryForObject(sql, paramMap, Long.class);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query flow log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query flow log failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else {
        throw e;
      }
    }
    return result;
  }

  public class QueryCanceledException extends IOException {

    /**
     * 
     */
    private static final long serialVersionUID = -2800513606515225916L;

    public QueryCanceledException() {
      super();
    }
  }
}
