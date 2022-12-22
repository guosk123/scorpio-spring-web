package com.machloop.fpc.manager.metadata.dao.clickhouse;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.dao.ProtocolRtpLogDao;
import com.machloop.fpc.manager.metadata.data.ProtocolRtpLogDO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisSubTaskDao;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author ChenXiao
 * create at 2022/9/8
 */
@Repository("protocolRtpLogDao")
public class ProtocolRtpLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolRtpLogDO>
    implements LogRecordDao<ProtocolRtpLogDO>, ProtocolRtpLogDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolRtpLogDaoImpl.class);

  private static final String TABLE_NAME = "t_fpc_protocol_rtp_log_record_analysis";

  private static final ProtocolRtpLogDO EMPTY_DO = new ProtocolRtpLogDO();

  public static final List<
      String> groupFields = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

  static {
    groupFields.add("level");
    groupFields.add("policy_name");
    groupFields.add("flow_id");
    groupFields.add("network_id");
    groupFields.add("service_id");
    groupFields.add("application_id");
    groupFields.add("start_time");
    groupFields.add("invite_time");
    groupFields.add("from");
    groupFields.add("src_ip");
    groupFields.add("src_port");
    groupFields.add("to");
    groupFields.add("dest_ip");
    groupFields.add("dest_port");
    groupFields.add("ip_protocol");
    groupFields.add("invite_src_ip");
    groupFields.add("invite_src_port");
    groupFields.add("invite_dest_ip");
    groupFields.add("invite_dest_port");
    groupFields.add("invite_ip_protocol");
  }


  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  @Autowired
  private PacketAnalysisSubTaskDao offlineAnalysisSubTaskDao;


  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  @Override
  protected PacketAnalysisSubTaskDao getOfflineAnalysisSubTaskDao() {
    return offlineAnalysisSubTaskDao;
  }

  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  @Override
  public Page<ProtocolRtpLogDO> queryLogRecords(LogRecordQueryVO queryVO, List<String> ids,
      Pageable page) {
    // 数据源
    String tableName = convertTableName(queryVO);

    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select start_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where 1=1 ");
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, whereSql, innerParams);
    }
    innerSelectSql.append(whereSql);

    innerSelectSql.append(" group by ").append(StringUtils.join(groupFields, ","));
    StringBuilder havingSql = new StringBuilder();
    enrichHavingSql(queryVO, havingSql, innerParams);
    innerSelectSql.append(havingSql);

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

    List<ProtocolRtpLogDO> resultDOList = result.stream().map(item -> {
      ProtocolRtpLogDO protocolRtpLogDO = convertLogMap2LogDO(item);
      if (queryVO.getColumns().contains("rtp_loss_packets_rate")) {
        long rtpTotalPackets = protocolRtpLogDO.getRtpTotalPackets();
        long rtpLossPackets = protocolRtpLogDO.getRtpLossPackets();
        protocolRtpLogDO
            .setRtpLossPacketsRate(new BigDecimal((double) rtpLossPackets * 100 / rtpTotalPackets)
                .setScale(2, RoundingMode.HALF_UP).doubleValue() + "%");
      }
      return protocolRtpLogDO;
    }).collect(Collectors.toList());
    return new PageImpl<>(resultDOList, page, 0);

  }

  @Override
  public List<Map<String, Object>> queryRtpNetworkSegmentation(LogRecordQueryVO queryVO) {
    // 数据源
    String tableName = convertTableName(queryVO);
    StringBuilder outSql = new StringBuilder();
    outSql
        .append(" select src_ip,dest_ip, network_id,sum(rtp_total_packets) AS rtp_total_packets,");
    outSql.append(
        "sum(rtp_loss_packets) AS rtp_loss_packets,max(jitter_max) AS jitter_max,avg(jitter_mean) AS jitter_mean ");
    outSql.append(" from ( ");
    StringBuilder sql = new StringBuilder();
    sql.append(" select src_ip,dest_ip, ");
    sql.append(
        "arrayElement(network_id,1) as network_id, sumMerge(rtp_total_packets) as rtp_total_packets, sumMerge(rtp_loss_packets)  as rtp_loss_packets,");
    sql.append("maxMerge(jitter_max) as jitter_max,avgMerge(jitter_mean) as jitter_mean");
    sql.append(" from ").append(tableName);
    sql.append(" group by src_ip, dest_ip,network_id,start_time ");
    StringBuilder havingSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichHavingSql(queryVO, havingSql, params);
    sql.append(havingSql);
    outSql.append(sql).append(" )");
    outSql.append(" group by src_ip, dest_ip, network_id ");

    return queryWithExceptionHandle(outSql.toString(), params, new ColumnMapRowMapper());
  }

  private void enrichHavingSql(LogRecordQueryVO queryVO, StringBuilder havingSql,
      Map<String, Object> params) {
    havingSql.append(" having 1=1 ");
    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> dsl = getDslConverter().converte(queryVO.getDsl(),
                queryVO.getHasAgingTime(), queryVO.getTimePrecision(),
                queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime());
        havingSql.append(" and ");
        String t1 = dsl.getT1();
        t1 = t1.replace("`ssrc`", "maxMerge(ssrc)").replace("`status`", "maxMerge(status)")
            .replace("`rtp_total_packets`", "sumMerge(rtp_total_packets)")
            .replace("`rtp_loss_packets`", "sumMerge(rtp_loss_packets)")
            .replace("`jitter_max`", "maxMerge(jitter_max)")
            .replace("`jitter_mean`", "avgMerge(jitter_mean)")
            .replace("`payload`", "anyMerge(payload)");
        if (t1.indexOf("ssrc") != t1.lastIndexOf("ssrc")) {
          int ssrc = t1.lastIndexOf("ssrc");
          t1 = t1.substring(0, ssrc) + t1.substring(ssrc).replace("ssrc", "maxMerge(ssrc)");
        }
        if (t1.indexOf("status") != t1.lastIndexOf("status")) {
          int status = t1.lastIndexOf("status");
          t1 = t1.substring(0, status) + t1.substring(status).replace("status", "maxMerge(status)");
        }
        if (t1.indexOf("rtp_total_packets") != t1.lastIndexOf("rtp_total_packets")) {
          int rtp_total_packets = t1.lastIndexOf("rtp_total_packets");
          t1 = t1.substring(0, rtp_total_packets) + t1.substring(rtp_total_packets)
              .replace("rtp_total_packets", "sumMerge(rtp_total_packets)");
        }
        if (t1.indexOf("rtp_loss_packets") != t1.lastIndexOf("rtp_loss_packets")) {
          int rtp_loss_packets = t1.lastIndexOf("rtp_loss_packets");
          t1 = t1.substring(0, rtp_loss_packets) + t1.substring(rtp_loss_packets)
              .replace("rtp_loss_packets", "sumMerge(rtp_loss_packets)");
        }
        if (t1.indexOf("jitter_max") != t1.lastIndexOf("jitter_max")) {
          int jitter_max = t1.lastIndexOf("jitter_max");
          t1 = t1.substring(0, jitter_max)
              + t1.substring(jitter_max).replace("jitter_max", "maxMerge(jitter_max)");
        }
        if (t1.indexOf("jitter_mean") != t1.lastIndexOf("jitter_mean")) {
          int jitter_mean = t1.lastIndexOf("jitter_mean");
          t1 = t1.substring(0, jitter_mean)
              + t1.substring(jitter_mean).replace("jitter_mean", "meanMerge(jitter_mean)");
        }
        if (t1.indexOf("payload") != t1.lastIndexOf("payload")) {
          int payload = t1.lastIndexOf("payload");
          t1 = t1.substring(0, payload)
              + t1.substring(payload).replace("payload", "anyMerge(payload)");
        }

        havingSql.append(t1);
        params.putAll(dsl.getT2());
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(), havingSql,
        params);
  }

  protected void enrichContainTimeRangeBetter(Date startTime, Date endTime, StringBuilder havingSql,
      Map<String, Object> params) {
    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    havingSql.append(
        "and ((start_time < toDateTime64(:start_time, 9, 'UTC') and maxMerge(end_time) > toDateTime64(:start_time, 9, 'UTC')) "
            + "or (start_time >= toDateTime64(:start_time, 9, 'UTC') and start_time < toDateTime64(:end_time, 9, 'UTC'))"
            + " or (start_time < toDateTime64(:end_time, 9, 'UTC') and toYear(maxMerge(end_time))='1970'))");
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  @Override
  public List<ProtocolRtpLogDO> queryLogRecordByIds(String tableName, String columns,
      List<String> ids, Sort sort) {
    List<Map<String, Object>> result = queryLogRecordsByIds(tableName, columns, ids, sort);
    List<ProtocolRtpLogDO> resultDOList = result.stream().map(item -> {
      ProtocolRtpLogDO protocolRtpLogDO = convertLogMap2LogDO(item);
      if (columns.contains("rtp_loss_packets_rate")) {
        long rtpTotalPackets = protocolRtpLogDO.getRtpTotalPackets();
        long rtpLossPackets = protocolRtpLogDO.getRtpLossPackets();
        protocolRtpLogDO
            .setRtpLossPacketsRate(new BigDecimal((double) rtpLossPackets * 100 / rtpTotalPackets)
                .setScale(2, RoundingMode.HALF_UP).doubleValue() + "%");
      }
      return protocolRtpLogDO;
    }).collect(Collectors.toList());
    return resultDOList;
  }

  @Override
  public ProtocolRtpLogDO queryLogRecord(LogRecordQueryVO queryVO, String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("select ")
        .append(buildSelectStatement(queryVO.getColumns(), Sets.newHashSet("start_time")));
    sql.append(" from ").append(convertTableName(queryVO));
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    // id查询
    String[] startTimeAndFlowId = StringUtils.split(id, "_");
    whereSql.append(" where start_time = toDateTime64(:start_time, 9, 'UTC') ");
    whereSql.append(" and flow_id = :flow_id ");
    whereSql.append("");
    sql.append(" limit 1 ");
    params.put("start_time", String.valueOf(startTimeAndFlowId[0]));
    params.put("flow_id", String.valueOf(startTimeAndFlowId[1]));

    sql.append(whereSql);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} log record by id, sql: {}, params: {}", convertTableName(queryVO),
          sql.toString(), params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    if (CollectionUtils.isEmpty(result)) {
      return buildEmptyLogDO();
    }
    ProtocolRtpLogDO protocolRtpLogDO = convertLogMap2LogDO(result.get(0));
    if (queryVO.getColumns().contains("rtp_loss_packets_rate")) {
      long rtpTotalPackets = protocolRtpLogDO.getRtpTotalPackets();
      long rtpLossPackets = protocolRtpLogDO.getRtpLossPackets();
      protocolRtpLogDO
          .setRtpLossPacketsRate(new BigDecimal((double) rtpLossPackets * 100 / rtpTotalPackets)
              .setScale(2, RoundingMode.HALF_UP).doubleValue() + "%");
    }
    return protocolRtpLogDO;
  }

  @Override
  protected List<Map<String, Object>> queryLogRecordsByIds(String tableName, String columns,
      List<String> ids, Sort sort) {
    // id过滤，id为startTime_flowId，提前进行拆分
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> startTimeConditions = Lists.newArrayListWithCapacity(ids.size());
    List<String> flowIdConditions = Lists.newArrayListWithCapacity(ids.size());
    int index = 0;
    for (String id : ids) {
      String[] filters = StringUtils.split(id, "_");
      // start_time
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
    Iterator<Sort.Order> iterator = sort.iterator();
    while (iterator.hasNext()) {
      sortProperties.add(iterator.next().getProperty());
    }

    // 嵌套子查询
    StringBuilder innersql = new StringBuilder();
    innersql.append("select ").append(buildSelectStatement(columns, sortProperties));
    innersql.append(" from ").append(tableName);
    // 主键start_time过滤
    innersql.append(" where start_time in (");
    innersql.append(StringUtils.join(startTimeConditions, ",")).append(")");
    sql.append(innersql);
    sql.append(" group by ").append(StringUtils.join(groupFields, ",")).append(")");

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

  @Override
  public long countLogRecords(LogRecordQueryVO queryVO, List<String> ids) {
    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return 0;
    }

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1=1 ");
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, whereSql, params);
    }

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select start_time,flow_id from ");
    totalSql.append(convertTableName(queryVO));
    totalSql.append(whereSql);
    totalSql.append(" group by ").append(StringUtils.join(groupFields, ","));
    StringBuilder havingSql = new StringBuilder();
    enrichHavingSql(queryVO, havingSql, params);
    totalSql.append(havingSql);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("count {} logs, sql: {}, params: {}", convertTableName(queryVO),
          totalSql.toString(), params);
    }

    return queryWithExceptionHandle(totalSql.toString(), params, new ColumnMapRowMapper()).size();
  }

  @Override
  public List<Object> queryFlowIds(String queryId, LogRecordQueryVO queryVO, List<String> ids,
      Sort sort, int size) {
    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return Lists.newArrayListWithCapacity(0);
    }

    // 数据源
    String tableName = convertTableName(queryVO);

    // 查询符合条件的记录flow_id，并去重
    StringBuilder sql = new StringBuilder();
    sql.append(" select distinct flow_id from ").append(tableName);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(" where 1=1 ");
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, sql, params);
    }

    sql.append(" group by ").append(StringUtils.join(groupFields, ","));

    StringBuilder havingSql = new StringBuilder();
    enrichHavingSql(queryVO, havingSql, params);
    sql.append(havingSql);

    PageUtils.appendSort(sql, sort, Lists.newArrayList("start_time", "flow_id"));

    sql.append(" limit ").append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page, inner sql: {}, params: {}", tableName, sql.toString(),
          params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(result)) {
      return Lists.newArrayListWithCapacity(0);
    }

    return result.stream().map(item -> item.get("flow_id")).collect(Collectors.toList());
  }

  @Override
  public Tuple2<String, List<String>> queryLogRecords(LogRecordQueryVO queryVO, List<String> ids,
      Sort sort, int size) {
    // 数据源
    String tableName = convertTableName(queryVO);

    // 兼容历史版本（id仅包含flow_id的版本）
    if (CollectionUtils.isNotEmpty(ids) && !ids.get(0).contains("_")) {
      return Tuples.of(tableName, Lists.newArrayListWithCapacity(0));
    }

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder innerSelectSql = new StringBuilder();
    innerSelectSql.append(" select start_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1=1 ");
    if (CollectionUtils.isNotEmpty(ids)) {
      enrichIdCondition(ids, whereSql, innerParams);
    }

    innerSelectSql.append(whereSql);
    innerSelectSql.append(" group by ").append(StringUtils.join(groupFields, ","));
    StringBuilder havingSql = new StringBuilder();
    enrichHavingSql(queryVO, havingSql, innerParams);
    innerSelectSql.append(havingSql);
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


  protected ProtocolRtpLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolRtpLogDO protocolRtpLogDO = new ProtocolRtpLogDO();
    convertBaseLogMap2AbstractLogDO(protocolRtpLogDO, map);

    if (map.containsKey("invite_time")) {
      protocolRtpLogDO.setInviteTime(DateUtils
          .toStringNanoISO8601((OffsetDateTime) map.get("invite_time"), ZoneId.systemDefault()));
    }
    protocolRtpLogDO.setFrom(MapUtils.getString(map, "from"));
    protocolRtpLogDO.setTo(MapUtils.getString(map, "to"));
    protocolRtpLogDO.setIpProtocol(MapUtils.getString(map, "ip_protocol"));
    protocolRtpLogDO.setSsrc(MapUtils.getLongValue(map, "ssrc"));
    protocolRtpLogDO.setStatus(MapUtils.getIntValue(map, "status"));
    protocolRtpLogDO.setRtpTotalPackets(MapUtils.getLongValue(map, "rtp_total_packets"));
    protocolRtpLogDO.setRtpLossPackets(MapUtils.getLongValue(map, "rtp_loss_packets"));
    protocolRtpLogDO.setJitterMax(MapUtils.getLongValue(map, "jitter_max"));
    protocolRtpLogDO.setJitterMean(MapUtils.getLongValue(map, "jitter_mean"));
    protocolRtpLogDO.setPayload(MapUtils.getString(map, "payload"));
    String inviteSrcIp = MapUtils.getString(map, "invite_src_ip");
    if (StringUtils.isNotBlank(inviteSrcIp)
        && StringUtils.startsWith(inviteSrcIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
      inviteSrcIp = StringUtils.substringAfter(inviteSrcIp, ManagerConstants.IPV4_TO_IPV6_PREFIX);
    }
    protocolRtpLogDO.setInviteSrcIp(inviteSrcIp);
    protocolRtpLogDO.setInviteSrcPort(MapUtils.getIntValue(map, "invite_src_port"));
    String inviteDestIp = MapUtils.getString(map, "invite_dest_ip");
    if (StringUtils.isNotBlank(inviteDestIp)
        && StringUtils.startsWith(inviteDestIp, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
      inviteDestIp = StringUtils.substringAfter(inviteDestIp, ManagerConstants.IPV4_TO_IPV6_PREFIX);
    }
    protocolRtpLogDO.setInviteDestIp(inviteDestIp);
    protocolRtpLogDO.setInviteDestPort(MapUtils.getIntValue(map, "invite_dest_port"));
    protocolRtpLogDO.setInviteIpProtocol(MapUtils.getString(map, "invite_ip_protocol"));
    protocolRtpLogDO.setSipFlowId(String.valueOf(MapUtils.getLong(map, "sip_flow_id")));


    return protocolRtpLogDO;
  }

  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }

  @Override
  protected ProtocolRtpLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" policy_name = :keyword ");
    conditionSql.append(" or invite_time = :keyword ");
    conditionSql.append(" or from = :keyword ");
    conditionSql.append(" or to = :keyword ");
    conditionSql.append(" or ip_protocol = :keyword ");
    conditionSql.append(" or ssrc = :keyword ");
    conditionSql.append(" or status = :keyword ");
    conditionSql.append(" or rtp_total_packets = :keyword ");
    conditionSql.append(" or rtp_loss_packets = :keyword ");
    conditionSql.append(" or jitter_max = :keyword ");
    conditionSql.append(" or jitter_mean = :keyword ");
    conditionSql.append(" or payload = :keyword ");
    conditionSql.append(" or invite_src_ip = :keyword ");
    conditionSql.append(" or invite_src_port = :keyword ");
    conditionSql.append(" or invite_dest_ip = :keyword ");
    conditionSql.append(" or invite_dest_port = :keyword ");
    conditionSql.append(" or invite_ip_protocol = :keyword ");
    return conditionSql.toString();
  }

  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
  }


  @Override
  protected String buildSelectStatement(String columns, Set<String> sortPropertys) {
    if (StringUtils.equals(columns, "*")) {
      return " level,policy_name,flow_id,network_id,service_id,application_id,start_time,invite_time,from,IPv6NumToString(src_ip) as src_ip,src_port,to,IPv6NumToString(dest_ip) as dest_ip,dest_port,ip_protocol,"
          + "IPv6NumToString(invite_src_ip) as invite_src_ip,invite_src_port,IPv6NumToString(invite_dest_ip) as invite_dest_ip,invite_dest_port,invite_ip_protocol,sip_flow_id,"
          + "maxMerge(end_time) as end_time,maxMerge(ssrc) as ssrc,maxMerge(status) as status,sumMerge(rtp_total_packets) as rtp_total_packets,"
          + "sumMerge(rtp_loss_packets) as rtp_loss_packets,maxMerge(jitter_max) as jitter_max,avgMerge(jitter_mean) as jitter_mean,anyMerge(payload) as payload";

    }
    // 必须查询排序字段
    sortPropertys = sortPropertys.stream()
        .filter(sortProperty -> !StringUtils.contains(columns, sortProperty))
        .collect(Collectors.toSet());
    String selectFields = CsvUtils.convertCollectionToCSV(sortPropertys);

    if (StringUtils.isBlank(columns)) {
      return selectFields;
    } else {
      String result = (StringUtils.isNotBlank(selectFields) ? selectFields + "," : "") + columns;
      if (result.contains("rtp_loss_packets_rate")) {
        result = result.replace(",rtp_loss_packets_rate", "");
      }

      return result.replace(",src_ip", ",IPv6NumToString(src_ip) as src_ip")
          .replace(",dest_ip", ",IPv6NumToString(dest_ip) as dest_ip")
          .replace("invite_src_ip", "IPv6NumToString(invite_src_ip) as invite_src_ip")
          .replace("invite_dest_ip", "IPv6NumToString(invite_dest_ip) as invite_dest_ip")
          .replace("end_time", "maxMerge(end_time) as end_time")
          .replace("ssrc", "maxMerge(ssrc) as ssrc").replace("status", "maxMerge(status) as status")
          .replace("rtp_total_packets", "sumMerge(rtp_total_packets) as rtp_total_packets")
          .replace("rtp_loss_packets", "sumMerge(rtp_loss_packets) as rtp_loss_packets")
          .replace("jitter_max", "maxMerge(jitter_max) as jitter_max")
          .replace("jitter_mean", "avgMerge(jitter_mean) as jitter_mean")
          .replace("payload", "anyMerge(payload) as payload");
    }
  }

}
