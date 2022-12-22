package com.machloop.fpc.cms.center.metadata.dao.impl;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;

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
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.*;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metadata.data.AbstractLogRecordDO;
import com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;


/**
 * @author mazhiyuan
 *
 * create at 2020年9月24日, fpc-manager
 */
public abstract class AbstractLogRecordDaoImpl<DO extends AbstractLogRecordDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLogRecordDaoImpl.class);

  protected static final int QUERY_BY_ID_LIMIT_SIZE = 10000;

  protected static final String START_TIME_DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";

  /**
   * @param queryVO
   * @param flowIds
   * @param page
   * @return
   */
  public Page<DO> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds, Pageable page) {
    // 数据源
    String tableName = convertTableName(queryVO);

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select start_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    fillAdditionalConditions(parseQuerySource(queryVO), whereSql, innerParams);
    // 流ID集合
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }

    innerSelectSql.append(whereSql);

    PageUtils.appendPage(innerSelectSql, page, Lists.newArrayList("start_time", "flow_id"));

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page, inner sql: {}, params: {}", tableName,
          innerSelectSql.toString(), innerParams);
    }

    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    // 使用已查到的start_time,flow_id查找对应记录的全部字段
    List<String> rowIds = Lists.newArrayListWithCapacity(innerResult.size());
    for (Map<String, Object> r : innerResult) {
      OffsetDateTime start_time = (OffsetDateTime) r.get("start_time");
      rowIds.add(start_time.format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT)) + "_"
          + MapUtils.getString(r, "flow_id"));
    }

    List<Map<String, Object>> result = queryLogRecordsByIds(tableName, queryVO.getColumns(), rowIds,
        page.getSort());
    List<DO> resultDOList = result.stream().map(item -> convertLogMap2LogDO(item))
        .collect(Collectors.toList());

    return new PageImpl<DO>(resultDOList, page, 0);
  }

  /**
   * @param queryVO
   * @param flowIds
   * @param sort
   * @param size
   * @return
   */
  public List<DO> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds, Sort sort,
      int size) {
    // 数据源
    String tableName = convertTableName(queryVO);

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select start_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    fillAdditionalConditions(parseQuerySource(queryVO), whereSql, innerParams);
    // 流ID集合
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }

    innerSelectSql.append(whereSql);
    PageUtils.appendSort(innerSelectSql, sort, Lists.newArrayList("start_time", "flow_id"));
    innerSelectSql.append(" limit ").append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs limit size, inner sql: {}, params: {}", tableName,
          innerSelectSql.toString(), innerParams);
    }
    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return Lists.newArrayListWithCapacity(0);
    }

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    int now = 0;
    List<String> rowIds = Lists.newArrayListWithCapacity(batchSize);
    while (now < innerResult.size()) {
      // 使用已查到的start_time,flow_id查找对应记录的全部字段
      Map<String, Object> row = innerResult.get(now);
      OffsetDateTime start_time = (OffsetDateTime) row.get("start_time");
      rowIds.add(start_time.format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT)) + "_"
          + MapUtils.getString(row, "flow_id"));
      now += 1;
      if (now % batchSize == 0) {
        List<Map<String, Object>> tmp = queryLogRecordsByIds(tableName, queryVO.getColumns(),
            rowIds, sort);
        result.addAll(tmp);
        rowIds = Lists.newArrayListWithCapacity(innerResult.size());
      }
    }
    if (now % batchSize != 0) {
      List<Map<String, Object>> tmp = queryLogRecordsByIds(tableName, queryVO.getColumns(), rowIds,
          sort);
      result.addAll(tmp);
    }

    List<DO> resultDOList = result.stream().map(item -> convertLogMap2LogDO(item))
        .collect(Collectors.toList());
    return resultDOList;
  }

  public Tuple2<String, List<String>> queryLogRecord(LogRecordQueryVO queryVO, List<String> flowIds,
      Sort sort, int size) {
    // 数据源
    String tableName = convertTableName(queryVO);

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select start_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    // 流ID集合
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }

    innerSelectSql.append(whereSql);
    PageUtils.appendSort(innerSelectSql, sort, Lists.newArrayList("start_time", "flow_id"));
    innerSelectSql.append(" limit ").append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs limit size, inner sql: {}, params: {}", tableName,
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
            .map(row -> ((OffsetDateTime) row.get("start_time"))
                .format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT)) + "_"
                + MapUtils.getString(row, "flow_id"))
            .collect(Collectors.toList()));
  }

  public List<DO> queryLogRecordByIds(String tableName, String columns, List<String> ids,
      Sort sort) {
    List<Map<String, Object>> result = queryLogRecordsByIds(tableName, columns, ids, sort);

    List<DO> resultDOList = result.stream().map(item -> convertLogMap2LogDO(item))
        .collect(Collectors.toList());
    return resultDOList;
  }

  /**
   * @param startTime
   * @param endTime
   * @param flowIds
   * @param size
   * @return
   */
  public List<Map<String, Object>> queryLogRecords(String startTime, String endTime,
      List<String> flowIds, int size) {
    // 数据源
    String tableName = convertTableName(new LogRecordQueryVO());

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select start_time, flow_id from ").append(tableName);

    // 构造查询条件
    StringBuilder whereSql = new StringBuilder(" where 1=1 ");
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 时间范围
    if (StringUtils.equals(startTime, endTime)) {
      // 开始结束时间相同时查询具体的时间点
      whereSql.append(" and start_time = toDateTime64(:start_time, 9, 'UTC') ");
      innerParams.put("start_time", startTime);
    } else {
      LogRecordQueryVO queryVO = new LogRecordQueryVO();
      whereSql.append(String.format(" and start_time %s toDateTime64(:start_time, 9, 'UTC') ",
          queryVO.getIncludeStartTime() ? ">=" : ">"));
      whereSql.append(String.format(" and start_time %s toDateTime64(:end_time, 9, 'UTC') ",
          queryVO.getIncludeEndTime() ? "<=" : "<"));
      innerParams.put("start_time", startTime);
      innerParams.put("end_time", endTime);
    }
    // 会话ID
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }
    innerSelectSql.append(whereSql);

    Sort sort = new Sort(new Order(Sort.Direction.DESC, "start_time"), new Order("flow_id"));
    PageUtils.appendSort(innerSelectSql, sort, Lists.newArrayList("start_time", "flow_id"));
    innerSelectSql.append(" limit ");
    innerSelectSql.append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs limit size, inner sql: {}, params: {}", tableName,
          innerSelectSql.toString(), innerParams);
    }
    List<Map<String, Object>> innerResult = queryWithExceptionHandle(innerSelectSql.toString(),
        innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(innerResult)) {
      return Lists.newArrayListWithCapacity(0);
    }

    // 使用已查到的start_time,flow_id查找对应记录的全部字段
    List<String> rowIds = innerResult.stream().map(r -> {
      OffsetDateTime start_time = (OffsetDateTime) r.get("start_time");
      return start_time.format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT)) + "_"
          + MapUtils.getString(r, "flow_id");
    }).collect(Collectors.toList());
    List<Map<String, Object>> result = queryLogRecordsByIds(tableName, "*", rowIds, sort);

    return result.stream().map(item -> {
      DO logDO = convertLogMap2LogDO(item);
      Map<String, Object> map = JsonHelper.deserialize(JsonHelper.serialize(logDO, false),
          new TypeReference<Map<String, Object>>() {
          }, false);
      map.put("startTime", DateUtils.toStringNanoISO8601((OffsetDateTime) item.get("start_time"),
          ZoneId.systemDefault()));
      map.put("endTime", DateUtils.toStringNanoISO8601((OffsetDateTime) item.get("end_time"),
          ZoneId.systemDefault()));

      return map;
    }).collect(Collectors.toList());
  }

  protected List<Map<String, Object>> queryLogRecordsByIds(String tableName, String columns,
      List<String> ids, Sort sort) {
    // id过滤，id为startTime_flowId，提前进行拆分
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> startTimeConditions = Lists.newArrayListWithCapacity(ids.size());
    List<String> flowIdConditions = Lists.newArrayListWithCapacity(ids.size());
    int index = 0;
    for (String id : ids) {
      String[] filters = StringUtils.split(id, "_");
      // report_time
      startTimeConditions.add(String.format("toDateTime64(:start_time%s, 9, 'UTC')", index));
      params.put("start_time" + index, filters[0]);
      // flow_id
      flowIdConditions.add(String.format(":flow_id%s", index));
      params.put("flow_id" + index, filters[1]);

      index += 1;
    }

    StringBuilder sql = new StringBuilder();
    sql.append("select * from (");

    // 排序字段
    Set<String> sortProperties = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Iterator<Order> iterator = sort.iterator();
    while (iterator.hasNext()) {
      sortProperties.add(iterator.next().getProperty());
    }

    // 嵌套子查询
    StringBuilder innersql = new StringBuilder();
    innersql.append("select ").append(buildSelectStatement(columns, sortProperties));
    innersql.append(" from ").append(tableName);
    // 主键report_time过滤
    innersql.append(" where start_time in (");
    innersql.append(StringUtils.join(startTimeConditions, ",")).append(")");
    sql.append(innersql).append(")");

    // flow_id过滤
    sql.append(" where flow_id in (");
    sql.append(StringUtils.join(flowIdConditions, ",")).append(")");

    PageUtils.appendSort(sql, sort, FlowLogQueryVO.class);
    sql.append(" limit ").append(QUERY_BY_ID_LIMIT_SIZE);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page by ids, ids: {}, sql: {}, params: {}", StringUtils.join(ids),
          sql.toString(), params);
    }

    List<Map<String, Object>> allResult = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    List<Map<String, Object>> result = allResult.stream().filter(item -> {
      OffsetDateTime start_time = (OffsetDateTime) item.get("start_time");
      return ids.contains(start_time.format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT))
          + "_" + MapUtils.getString(item, "flow_id"));
    }).collect(Collectors.toList());
    return result;
  }

  /**
   * @param dsl
   * @return
   */
  public String queryLogRecordsViaDsl(String dsl) {
    throw new UnsupportedOperationException();
  }

  /**
   * @param queryVO
   * @param id
   * @return
   */
  public DO queryLogRecord(LogRecordQueryVO queryVO, String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("select ")
        .append(buildSelectStatement(queryVO.getColumns(), Sets.newHashSet("start_time")));
    sql.append(" from ").append(convertTableName(queryVO));

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    String[] startTimeAndFlowId = StringUtils.split(id, "_");
    whereSql.append(" where start_time = toDateTime64(:start_time, 9, 'UTC') ");
    whereSql.append(" and flow_id = :flow_id ");
    params.put("start_time", String.valueOf(startTimeAndFlowId[0]));
    params.put("flow_id", String.valueOf(startTimeAndFlowId[1]));

    sql.append(whereSql);
    sql.append(" limit 1 ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} log record by id, sql: {}, params: {}", convertTableName(queryVO),
          sql.toString(), params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    return CollectionUtils.isEmpty(result) ? buildEmptyLogDO() : convertLogMap2LogDO(result.get(0));
  }

  /**
   * @param queryVO
   * @param flowIds
   * @return
   */
  public long countLogRecords(LogRecordQueryVO queryVO, List<String> flowIds) {

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    fillAdditionalConditions(parseQuerySource(queryVO), whereSql, params);
    // 流ID集合
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      params.put("flowIds", flowIds);
    }

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(1) from ");
    totalSql.append(convertTableName(queryVO));
    totalSql.append(whereSql);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("count {} logs, sql: {}, params: {}", convertTableName(queryVO),
          totalSql.toString(), params);
    }

    Long total = queryForLongWithExceptionHandle(totalSql.toString(), params);

    return total;
  }

  /**
   * @param queryVO
   * @return
   */
  public Map<String, Long> countLogRecords(LogCountQueryVO queryVO) {
    LogRecordQueryVO logRecordQueryVO = new LogRecordQueryVO();
    logRecordQueryVO.setSourceType(queryVO.getSourceType());
    logRecordQueryVO.setPacketFileId(queryVO.getPacketFileId());
    StringBuilder countSql = new StringBuilder();
    countSql.append("select count(1) from ");
    String tableName = convertTableName(logRecordQueryVO);
    countSql.append(tableName);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1=1 ");

    if (queryVO.getStartTimeDate() != null && queryVO.getEndTimeDate() != null) {
      whereSql.append(" and start_time >= toDateTime64(:start_time, 9, 'UTC') ");
      whereSql.append(" and start_time < toDateTime64(:end_time, 9, 'UTC') ");
      params.put("start_time", DateUtils.toStringFormat(queryVO.getStartTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      params.put("end_time", DateUtils.toStringFormat(queryVO.getEndTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      whereSql.append(" and (1=2 ");
      for (int i = 0; i < queryVO.getNetworkIds().size(); i++) {
        whereSql.append(String.format(" or has(network_id, :networkId%s) ", i));
        params.put("networkId" + i, queryVO.getNetworkIds().get(i));
      }
      whereSql.append(" ) ");
    } else if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      whereSql.append(" and (1=2 ");
      for (int i = 0; i < queryVO.getServiceNetworkIds().size(); i++) {
        whereSql.append(String.format(
            " or ( has(service_id, :serviceId%s) and has(network_id, :networkId%s) )", i, i));
        params.put("serviceId" + i, queryVO.getServiceNetworkIds().get(i).getT1());
        params.put("networkId" + i, queryVO.getServiceNetworkIds().get(i).getT2());
      }
      whereSql.append(" ) ");
    } else {
      if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
        whereSql.append(" and has(network_id, :network_id)=1 ");
        params.put("network_id", queryVO.getNetworkId());
      }
      if (StringUtils.isNotBlank(queryVO.getServiceId())) {
        whereSql.append(" and has(service_id, :service_id)=1 ");
        params.put("service_id", queryVO.getServiceId());
      }
    }

    if (StringUtils.isNotBlank(queryVO.getSrcIp())) {
      if (NetworkUtils.isInetAddress(queryVO.getSrcIp())) {
        if (NetworkUtils.isInetAddress(queryVO.getSrcIp(), IpVersion.V4)) {
          whereSql.append(" and src_ipv4 = toIPv4(:src_ip) ");
        } else {
          whereSql.append(" and src_ipv6 = toIPv6(:src_ip) ");
        }
        params.put("src_ip", queryVO.getSrcIp());
      } else if (NetworkUtils.isCidr(queryVO.getSrcIp())) {
        if (NetworkUtils.isCidr(queryVO.getSrcIp(), IpVersion.V4)) {
          whereSql.append(" and (src_ipv4 between IPv4CIDRToRange(toIPv4(:ip_address), :cidr).1 ");
          whereSql.append(" and IPv4CIDRToRange(toIPv4(:ip_address), :cidr).2)");
        } else {
          whereSql.append(" and (src_ipv6 between IPv6CIDRToRange(toIPv6(:ip_address), :cidr).1 ");
          whereSql.append(" and IPv6CIDRToRange(toIPv6(:ip_address), :cidr).2)");
        }
        String[] ipAndCidr = queryVO.getSrcIp().split("/");
        params.put("ip_address", ipAndCidr[0]);
        params.put("cidr", Integer.parseInt(ipAndCidr[1]));
      } else {
        whereSql.append(" and 1=2 ");
      }
    }
    countSql.append(whereSql);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("count {} logs, sql: {}, params: {}", convertTableName(logRecordQueryVO),
          countSql.toString(), params);
    }

    Long count = queryForLongWithExceptionHandle(countSql.toString(), params);

    String protocol = StringUtils.upperCase(StringUtils.substringAfterLast(
        StringUtils.substringBefore(tableName, "_log_record"), "d_fpc_protocol_"));

    Map<String, Long> result = Maps.newHashMapWithExpectedSize(1);
    result.put(protocol, count);
    return result;
  }

  protected String buildSelectStatement(String columns, Set<String> sortPropertys) {
    if (StringUtils.equals(columns, "*")) {
      return columns;
    }

    // 必须查询排序字段
    sortPropertys = sortPropertys.stream()
        .filter(sortProperty -> !StringUtils.contains(columns, sortProperty))
        .collect(Collectors.toSet());
    String selectFields = CsvUtils.convertCollectionToCSV(sortPropertys);

    if (StringUtils.isBlank(columns)) {
      return selectFields;
    } else {
      return (StringUtils.isNotBlank(selectFields) ? selectFields + "," : "") + columns;
    }
  }

  protected abstract ClickHouseJdbcTemplate getClickHouseJdbcTemplate();

  protected abstract String getTableName();

  protected abstract DO convertLogMap2LogDO(Map<String, Object> map);

  protected abstract Spl2SqlHelper getDslConverter();

  protected void convertBaseLogMap2AbstractLogDO(AbstractLogRecordDO abstractLogRecordDO,
      Map<String, Object> map) {
    if (map.containsKey("start_time")) {
      abstractLogRecordDO.setStartTime(DateUtils
          .toStringNanoISO8601((OffsetDateTime) map.get("start_time"), ZoneId.systemDefault()));
    }
    if (map.containsKey("end_time")) {
      abstractLogRecordDO.setEndTime(DateUtils
          .toStringNanoISO8601((OffsetDateTime) map.get("end_time"), ZoneId.systemDefault()));
    }
    abstractLogRecordDO.setPolicyName(MapUtils.getString(map, "policy_name"));
    abstractLogRecordDO.setLevel(MapUtils.getString(map, "level"));
    abstractLogRecordDO.setFlowId(String.valueOf(MapUtils.getLong(map, "flow_id")));
    abstractLogRecordDO.setApplicationId(MapUtils.getString(map, "application_id"));

    List<String> networkIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("network_id") != null) {
      String[] networkIdArray = JsonHelper.deserialize(JsonHelper.serialize(map.get("network_id")),
          new TypeReference<String[]>() {
          }, false);
      if (networkIdArray != null) {
        networkIdList = Lists.newArrayList(networkIdArray);
      }
    }
    abstractLogRecordDO.setNetworkId(networkIdList);
    List<String> serviceIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (map.get("service_id") != null) {
      String[] serviceIdArray = JsonHelper.deserialize(JsonHelper.serialize(map.get("service_id")),
          new TypeReference<String[]>() {
          }, false);
      if (serviceIdArray != null) {
        serviceIdList = Lists.newArrayList(serviceIdArray);
      }
    }
    abstractLogRecordDO.setServiceId(serviceIdList);

    if (map.containsKey("src_ip")) {
      String srcIp = MapUtils.getString(map, "src_ip");
      if (StringUtils.isNotBlank(srcIp)
          && StringUtils.startsWith(srcIp, CenterConstants.IPV4_TO_IPV6_PREFIX)) {
        srcIp = StringUtils.substringAfter(srcIp, CenterConstants.IPV4_TO_IPV6_PREFIX);
      }
      abstractLogRecordDO.setSrcIp(srcIp);
    }

    if (map.containsKey("dest_ip")) {
      String destIp = MapUtils.getString(map, "dest_ip");
      if (StringUtils.isNotBlank(destIp)
          && StringUtils.startsWith(destIp, CenterConstants.IPV4_TO_IPV6_PREFIX)) {
        destIp = StringUtils.substringAfter(destIp, CenterConstants.IPV4_TO_IPV6_PREFIX);
      }
      abstractLogRecordDO.setDestIp(destIp);
    }

    if (map.containsKey("src_ipv4") || map.containsKey("src_ipv6")) {
      Inet4Address srcIpv4 = (Inet4Address) map.get("src_ipv4");
      Inet6Address srcIpv6 = (Inet6Address) map.get("src_ipv6");
      abstractLogRecordDO
          .setSrcIp(srcIpv4 != null ? srcIpv4.getHostAddress() : srcIpv6.getHostAddress());
    }

    if (map.containsKey("dest_ipv4") || map.containsKey("dest_ipv6")) {
      Inet4Address destIpv4 = (Inet4Address) map.get("dest_ipv4");
      Inet6Address destIpv6 = (Inet6Address) map.get("dest_ipv6");
      abstractLogRecordDO
          .setDestIp(destIpv4 != null ? destIpv4.getHostAddress() : destIpv6.getHostAddress());
    }

    abstractLogRecordDO.setSrcPort(MapUtils.getIntValue(map, "src_port"));
    abstractLogRecordDO.setDestPort(MapUtils.getIntValue(map, "dest_port"));
  }

  protected abstract DO buildEmptyLogDO();

  protected abstract String getPreciseConditionSql(String keyword);

  protected abstract String getWildcardConditionSql(String keyword);

  protected String convertTableName(LogRecordQueryVO queryVO) {
    String tableName = getTableName();
    if (StringUtils.equals(queryVO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_PACKET_FILE)) {
      tableName = String.join("_", getTableName(), queryVO.getPacketFileId());

      // 判断离线数据包文件的分析结果是否存在
      List<Map<String, Object>> existResult = getClickHouseJdbcTemplate().getJdbcTemplate()
          .query(String.format("show tables from %s where name = '%s'",
              CenterConstants.FPC_DATABASE, tableName), new ColumnMapRowMapper());
      if (CollectionUtils.isEmpty(existResult)) {
        LOGGER.warn("search failed, offline packet file metadata not found: {}", tableName);
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "离线分析结果异常，未查找到协议详单");
      }
    }

    return tableName;
  }

  protected void enrichWhereSql(LogRecordQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {

    whereSql.append(" where 1=1 ");

    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> dsl = getDslConverter().converte(queryVO.getDsl(),
                queryVO.getHasAgingTime(), queryVO.getTimePrecision(),
                queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime());
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

    if (queryVO.getDecrypted() != null) {
      whereSql.append(" and decrypted = :decrypted");
      params.put("decrypted", queryVO.getDecrypted());
    }

    if (StringUtils.isNotBlank(queryVO.getKeyword())) {
      // 通配符方式
      if (StringUtils.containsAny(StringUtils.remove(queryVO.getKeyword(), "\\%"), "%")
          || StringUtils.containsAny(StringUtils.remove(queryVO.getKeyword(), "\\_"), "_")) {
        whereSql.append(" and ( ");
        whereSql.append(getWildcardConditionSql(queryVO.getKeyword()));
        whereSql.append(" ) ");
        params.put("keyword_like", queryVO.getKeyword());
      } else {
        List<String> tmpList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (NetworkUtils.isInetAddress(queryVO.getKeyword())) {
          if (NetworkUtils.isInetAddress(queryVO.getKeyword(), IpVersion.V4)) {
            tmpList.add(" src_ipv4 = toIPv4(:keyword) or dest_ipv4 = toIPv4(:keyword) ");
          } else {
            tmpList.add(" src_ipv6 = toIPv6(:keyword) or dest_ipv6 = toIPv6(:keyword) ");
          }
        } else if (NetworkUtils.isInetAddressPort(queryVO.getKeyword())) {
          tmpList.add(" src_port = :port_keyword or dest_port = :port_keyword ");
          params.put("port_keyword", Integer.parseInt(queryVO.getKeyword()));
        }
        String condition = getPreciseConditionSql(queryVO.getKeyword());
        tmpList.add(condition);

        if (!tmpList.isEmpty()) {
          whereSql.append(" and ( ");
          whereSql.append(StringUtils.join(tmpList, " or "));
          whereSql.append(" ) ");
        }
        params.put("keyword", queryVO.getKeyword());
      }
    }
  }

  protected void enrichContainTimeRangeBetter(Date startTime, Date endTime,
      boolean includeStartTime, boolean includeEndTime, StringBuilder whereSql,
      Map<String, Object> params) {
    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(String.format(" and start_time %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and start_time %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
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
                (int) CenterConstants.ENGINE_LOG_AGINGTIME_MILLS / 1000),
            "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));

    // 起止时间小于老化时间, 则存在长连接时间范围覆盖查询时间, 也需要查出来
    if (startTime != null && endTime != null && (endTime.getTime()
        - startTime.getTime() <= CenterConstants.ENGINE_LOG_AGINGTIME_MILLS)) {
      whereSql.append(" or (start_time >= toDateTime64(:aging_start_time, 9, 'UTC')");
      whereSql.append(" and start_time <= toDateTime64(:start_time, 9, 'UTC') ");
      whereSql.append(" and end_time >= toDateTime64(:end_time, 9, 'UTC') ");
      whereSql.append(" and end_time <= toDateTime64(:aging_end_time, 9, 'UTC'))");

      params.put("aging_start_time",
          DateUtils.toStringFormat(
              DateUtils.beforeSecondDate(startTime,
                  (int) CenterConstants.ENGINE_LOG_AGINGTIME_MILLS / 1000),
              "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
      params.put("aging_end_time",
          DateUtils.toStringFormat(
              DateUtils.afterSecondDate(endTime,
                  (int) CenterConstants.ENGINE_LOG_AGINGTIME_MILLS / 1000),
              "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    whereSql.append(")");
  }

  protected <T> List<T> queryWithExceptionHandle(String sql, Map<String, ?> paramMap,
      RowMapper<T> rowMapper) {
    List<T> result = Lists.newArrayListWithCapacity(0);
    try {
      result = getClickHouseJdbcTemplate().getJdbcTemplate().query(sql, paramMap, rowMapper);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query metadata log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata log failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else if (errorCode == 60 || errorCode == 102) {
        // 60(UNKNOWN_TABLE，表不存在)：集群内没有探针节点，仅存在默认节点，默认节点会从本地查找数据表，此时就会返回表不存在的错误码
        LOGGER.info("not found sensor node.");
      } else if (errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        LOGGER.warn("connection sensor clikhousenode error.", e.getMessage());
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }

  protected Long queryForLongWithExceptionHandle(String sql, Map<String, ?> paramMap) {
    Long result = 0L;
    try {
      result = getClickHouseJdbcTemplate().getJdbcTemplate().queryForObject(sql, paramMap,
          Long.class);
    } catch (UncategorizedSQLException | BadSqlGrammarException e) {
      int errorCode = e instanceof UncategorizedSQLException
          ? ((UncategorizedSQLException) e).getSQLException().getErrorCode()
          : ((BadSqlGrammarException) e).getSQLException().getErrorCode();
      if (errorCode == 394) {
        // 任务被取消时返回错误码为394
        LOGGER.info("query metadata log has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata log failed, error msg: {}",
            errorCode == 241 ? "Memory limit (total) exceeded" : "cannot allocate memory");

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "查询可用内存不足，请缩短时间范围后重试");
      } else if (errorCode == 159) {
        // M(159, TIMEOUT_EXCEEDED),查询超时
        LOGGER.warn("read timed out, error sql: {}, param: {}.", sql, paramMap);

        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "查询超时，请缩短时间范围后重试");
      } else if (errorCode == 60 || errorCode == 102) {
        // 60(UNKNOWN_TABLE，表不存在)：集群内没有探针节点，仅存在默认节点，默认节点会从本地查找数据表，此时就会返回表不存在的错误码
        LOGGER.info("not found sensor node.");
      } else if (errorCode == 279 || errorCode == 210 || errorCode == 209) {
        // 279 (ALL_CONNECTION_TRIES_FAILED) 探针上clickhouse端口不通
        // 210 (NETWORK_ERROR)
        // 209 (SOCKET_TIMEOUT)
        LOGGER.warn("connection sensor clikhousenode error.", e.getMessage());
      } else {
        throw e;
      }
    }
    return result;
  }

  /**
   * 增加额外过滤条件，格式：[{"W":xx},{"A":x1, "B":y1},{"A":x2, "B":y2}]，</br>
   * 单个过滤组合内条件用 "and" 拼接，</br>
   * 过滤组合相同的项将通过 "or" 连接，作为一整项过滤，</br>
   * 过滤组合不同的项用 "and" 连接；</br>
   * 最终过滤语句如下：where xxx and has(W, xx) =1 and (has(A, x1) =1 and has(B, y1) =1) or (has(A, x2) =1 and has(B, y2) =1)
   * @param additionalConditions
   * @param whereSql
   * @param params
   */
  protected void fillAdditionalConditions(List<Map<String, Object>> additionalConditions,
      StringBuilder whereSql, Map<String, Object> params) {
    // 添加附加条件
    if (CollectionUtils.isNotEmpty(additionalConditions)) {
      Map<String,
          List<String>> conditions = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      int index = 0;
      for (Map<String, Object> condition : additionalConditions) {
        StringBuilder conditionSql = new StringBuilder("(");
        Iterator<Entry<String, Object>> iterator = condition.entrySet().iterator();
        while (iterator.hasNext()) {
          Entry<String, Object> entry = iterator.next();
          if (entry.getValue() == null) {
            conditionSql.append("isNull(").append(entry.getKey()).append(")");
          } else {
            conditionSql.append(" has(");
            conditionSql.append(entry.getKey()).append(" , :").append(entry.getKey()).append(index);
            conditionSql.append(" )=1");
            params.put(entry.getKey() + index, entry.getValue());
          }

          if (iterator.hasNext()) {
            conditionSql.append(" and ");
          }
        }
        conditionSql.append(")");
        String conditionKey = StringUtils.join(condition.keySet(), "_");
        List<String> list = conditions.getOrDefault(conditionKey,
            Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
        list.add(conditionSql.toString());
        conditions.put(conditionKey, list);

        index++;
      }

      conditions.values().forEach(conditionSqlList -> {
        whereSql.append(" and (").append(StringUtils.join(conditionSqlList, " or ")).append(")");
      });
    }
  }

  /**
   * 解析实际查询的网络业务
   * @param queryVO
   * @return
   */
  protected List<Map<String, Object>> parseQuerySource(LogRecordQueryVO queryVO) {
    List<Map<String, Object>> sourceConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      sourceConditions = queryVO.getNetworkIds().stream().map(networkId -> {
        Map<String, Object> termKey = Maps.newHashMap();
        termKey.put("network_id", networkId);
        return termKey;
      }).collect(Collectors.toList());
    } else if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      sourceConditions = queryVO.getServiceNetworkIds().stream().map(serviceNetworkId -> {
        Map<String, Object> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        termKey.put("service_id", serviceNetworkId.getT1());
        termKey.put("network_id", serviceNetworkId.getT2());

        return termKey;
      }).collect(Collectors.toList());
    }

    return sourceConditions;
  }

}