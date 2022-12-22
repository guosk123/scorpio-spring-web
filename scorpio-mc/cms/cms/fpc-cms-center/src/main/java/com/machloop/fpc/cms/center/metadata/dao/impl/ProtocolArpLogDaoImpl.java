package com.machloop.fpc.cms.center.metadata.dao.impl;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolArpLogDO;
import com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2020年12月11日, fpc-manager
 */
@SuppressWarnings("unused")
@Repository("protocolArpLogDao")
public class ProtocolArpLogDaoImpl extends AbstractLogRecordDaoImpl<ProtocolArpLogDO>
    implements LogRecordDao<ProtocolArpLogDO> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolArpLogDaoImpl.class);

  private static final String TABLE_NAME = "d_fpc_protocol_arp_log_record";

  private static final List<
      String> OTHER_FIELD_NAMES = Lists.newArrayList("src_mac", "dest_mac", "type");

  private static final ProtocolArpLogDO EMPTY_DO = new ProtocolArpLogDO();

  protected static final String START_TIME_DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  public Page<ProtocolArpLogDO> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds,
      Pageable page) {
    // 数据源
    String tableName = convertTableName(queryVO);

    StringBuilder sql = new StringBuilder();
    sql.append("select * from ").append(tableName);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    fillAdditionalConditions(parseQuerySource(queryVO), whereSql, innerParams);
    // 流ID集合
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }

    sql.append(whereSql);

    PageUtils.appendPage(sql, page, Lists.newArrayList("start_time", "flow_id"));

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page, sql: {}, params: {}", tableName, sql.toString(),
          innerParams);
    }

    List<Map<String, Object>> resultList = getClickHouseJdbcTemplate().getJdbcTemplate()
        .query(sql.toString(), innerParams, new ColumnMapRowMapper());
    List<ProtocolArpLogDO> resultDOList = resultList.stream().map(item -> convertLogMap2LogDO(item))
        .collect(Collectors.toList());

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(1) from ");
    totalSql.append(tableName);
    totalSql.append(whereSql);
    Long total = getClickHouseJdbcTemplate().getJdbcTemplate().queryForObject(totalSql.toString(),
        innerParams, Long.class);

    return new PageImpl<ProtocolArpLogDO>(resultDOList, page, total);
  }

  public List<ProtocolArpLogDO> queryLogRecords(LogRecordQueryVO queryVO, List<String> flowIds,
      Sort sort, int size) {
    // 数据源
    String tableName = convertTableName(queryVO);

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder sql = new StringBuilder();
    sql.append("select * from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    fillAdditionalConditions(parseQuerySource(queryVO), whereSql, innerParams);
    // 流ID集合
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }
    sql.append(whereSql);

    PageUtils.appendSort(sql, sort, Lists.newArrayList("start_time", "flow_id"));
    sql.append(" limit ").append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs limit size, sql: {}, params: {}", tableName, sql.toString(),
          innerParams);
    }

    List<Map<String, Object>> resultList = getClickHouseJdbcTemplate().getJdbcTemplate()
        .query(sql.toString(), innerParams, new ColumnMapRowMapper());

    List<ProtocolArpLogDO> resultDOList = resultList.stream().map(item -> convertLogMap2LogDO(item))
        .collect(Collectors.toList());
    return resultDOList;
  }

  public List<ProtocolArpLogDO> queryLogRecordByIds(String tableName, String columns,
      List<String> ids, Sort sort) {
    List<Map<String, Object>> result = queryLogRecordsByIds(tableName, "*", ids, sort);

    List<ProtocolArpLogDO> resultDOList = result.stream().map(item -> convertLogMap2LogDO(item))
        .collect(Collectors.toList());
    return resultDOList;
  }

  public List<Map<String, Object>> queryLogRecords(String startTime, String endTime,
      List<String> flowIds, int size) {
    // 数据源
    String tableName = convertTableName(new LogRecordQueryVO());

    StringBuilder sql = new StringBuilder();
    sql.append(" select * from ").append(tableName);

    // 构造查询条件
    StringBuilder whereSql = new StringBuilder(" where 1=1 ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    LogRecordQueryVO queryVO = new LogRecordQueryVO();
    // 时间范围
    if (StringUtils.equals(startTime, endTime)) {
      // 开始结束时间相同时查询具体的时间点
      whereSql.append(" and start_time = toDateTime64(:start_time, 9, 'UTC') ");
      params.put("start_time", startTime);
    } else {
      whereSql.append(String.format(" and start_time %s toDateTime64(:start_time, 9, 'UTC') ",
          queryVO.getIncludeStartTime() ? ">=" : ">"));
      whereSql.append(String.format(" and start_time %s toDateTime64(:end_time, 9, 'UTC') ",
          queryVO.getIncludeEndTime() ? "<=" : "<"));
      params.put("start_time", startTime);
      params.put("end_time", endTime);
    }
    // 会话ID
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      params.put("flowIds", flowIds);
    }
    sql.append(whereSql);

    Sort sort = new Sort(new Order(Sort.Direction.DESC, "start_time"), new Order("flow_id"));
    PageUtils.appendSort(sql, sort, Lists.newArrayList("start_time", "flow_id"));
    sql.append(" limit ");
    sql.append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs limit size, sql: {}, params: {}", tableName, sql.toString(),
          params);
    }
    List<Map<String, Object>> resultList = getClickHouseJdbcTemplate().getJdbcTemplate()
        .query(sql.toString(), params, new ColumnMapRowMapper());

    return resultList.stream().map(item -> {
      ProtocolArpLogDO logDO = convertLogMap2LogDO(item);
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


  public Tuple2<String, List<String>> queryLogRecord(LogRecordQueryVO queryVO, List<String> flowIds,
      Sort sort, int size) {
    // 数据源
    String tableName = convertTableName(queryVO);

    // 查询符合条件的记录start_time,flow_id标识一条记录
    StringBuilder sql = new StringBuilder();
    sql.append("select start_time, flow_id from ").append(tableName);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> innerParams = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, innerParams);
    // 流ID集合
    if (CollectionUtils.isNotEmpty(flowIds)) {
      whereSql.append(" and flow_id in (:flowIds)");
      innerParams.put("flowIds", flowIds);
    }
    sql.append(whereSql);

    PageUtils.appendSort(sql, sort, Lists.newArrayList("start_time", "flow_id"));
    sql.append(" limit ").append(size);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs limit size, sql: {}, params: {}", tableName, sql.toString(),
          innerParams);
    }

    List<Map<String, Object>> resultList = getClickHouseJdbcTemplate().getJdbcTemplate()
        .query(sql.toString(), innerParams, new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(resultList)) {
      return Tuples.of(tableName, Lists.newArrayListWithCapacity(0));
    }

    return Tuples.of(tableName,
        resultList.stream()
            .map(row -> ((OffsetDateTime) row.get("start_time"))
                .format(DateTimeFormatter.ofPattern(START_TIME_DEFAULT_FORMAT)) + "_"
                + MapUtils.getString(row, "flow_id"))
            .collect(Collectors.toList()));
  }

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
          whereSql.append(" and 1=2 ");
        }
        params.put("src_ip", queryVO.getSrcIp());
      } else if (NetworkUtils.isCidr(queryVO.getSrcIp())) {
        if (NetworkUtils.isCidr(queryVO.getSrcIp(), IpVersion.V4)) {
          whereSql.append(" and (src_ipv4 between IPv4CIDRToRange(toIPv4(:ip_address), :cidr).1 ");
          whereSql.append(" and IPv4CIDRToRange(toIPv4(:ip_address), :cidr).2)");
        } else {
          whereSql.append(" and 1=2 ");
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

  /**
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return TABLE_NAME;
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#convertLogMap2LogDO(java.util.Map)
   */
  @Override
  protected ProtocolArpLogDO convertLogMap2LogDO(Map<String, Object> map) {
    ProtocolArpLogDO protocolArpLogDO = new ProtocolArpLogDO();

    convertBaseLogMap2AbstractLogDO(protocolArpLogDO, map);

    protocolArpLogDO.setSrcMac(MapUtils.getString(map, "src_mac"));
    protocolArpLogDO.setDestMac(MapUtils.getString(map, "dest_mac"));
    protocolArpLogDO.setType(MapUtils.getIntValue(map, "type"));

    return protocolArpLogDO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getDslConverter()
   */
  @Override
  protected Spl2SqlHelper getDslConverter() {
    return dslConverter;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#buildEmptyLogDO()
   */
  @Override
  protected ProtocolArpLogDO buildEmptyLogDO() {
    return EMPTY_DO;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getPreciseConditionSql(java.lang.String)
   */
  @Override
  protected String getPreciseConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" type = :keyword ");
    conditionSql.append(" or src_mac = :keyword ");
    conditionSql.append(" or dest_mac = :keyword");

    return conditionSql.toString();
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.dao.impl.AbstractLogRecordDaoImpl#getWildcardConditionSql(java.lang.String)
   */
  @Override
  protected String getWildcardConditionSql(String keyword) {
    StringBuilder conditionSql = new StringBuilder();
    conditionSql.append(" 1=2 ");
    return conditionSql.toString();
  }

}
