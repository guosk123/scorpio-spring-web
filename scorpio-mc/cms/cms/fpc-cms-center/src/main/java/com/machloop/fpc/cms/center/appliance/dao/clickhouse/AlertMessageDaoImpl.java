package com.machloop.fpc.cms.center.appliance.dao.clickhouse;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.cms.center.appliance.dao.AlertMessageDao;
import com.machloop.fpc.cms.center.appliance.data.AlertMessageDO;
import com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 *
 * create at 2021年6月15日, fpc-manager
 */
@Repository
public class AlertMessageDaoImpl implements AlertMessageDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertMessageDaoImpl.class);

  private static final String TABLE_APPLIANCE_ALERT_MESSAGE = "d_fpc_appliance_alert_message";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertMessageDao#queryAlertMessages(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO, java.util.List)
   */
  @Override
  public Page<AlertMessageDO> queryAlertMessages(Pageable page, AlertMessageQueryVO queryVO,
      List<String> solverIds) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where 1=1 ");
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      whereSql.append(" and arise_time >= toDateTime64(:start_time, 3, 'UTC') ");
      params.put("start_time", queryVO.getStartTime());
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      whereSql.append(" and arise_time <= toDateTime64(:end_time, 3, 'UTC') ");
      params.put("end_time", queryVO.getEndTime());
    }
    if (StringUtils.isNotBlank(queryVO.getSolveTimeBegin())) {
      whereSql.append(" and solve_time >= toDateTime64(:solveTimeBegin, 3, 'UTC') ");
      params.put("solveTimeBegin", queryVO.getSolveTimeBegin());
    }
    if (StringUtils.isNotBlank(queryVO.getSolveTimeEnd())) {
      whereSql.append(" and solve_time <= toDateTime64(:solveTimeEnd, 3, 'UTC') ");
      params.put("solveTimeEnd", queryVO.getSolveTimeEnd());
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      whereSql.append(" and network_id = :networkId ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      whereSql.append(" and network_id in (:networkIds) ");
      params.put("networkIds", queryVO.getNetworkIds());
    }
    if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      whereSql.append(" and (1=2 ");
      for (int i = 0; i < queryVO.getServiceNetworkIds().size(); i++) {
        whereSql.append(
            String.format(" or (service_id = :serviceId%s and network_id = :networkId%s) ", i, i));
        params.put("serviceId" + i, queryVO.getServiceNetworkIds().get(i).getT1());
        params.put("networkId" + i, queryVO.getServiceNetworkIds().get(i).getT2());
      }
      whereSql.append(" ) ");
    }
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      whereSql.append(" and service_id = :serviceId ");
      params.put("serviceId", queryVO.getServiceId());
    } else {
      whereSql.append(" and isNull(service_id) ");
    }
    if (StringUtils.isNotBlank(queryVO.getName())) {
      whereSql.append(" and name like :name ");
      params.put("name", "%" + queryVO.getName() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getCategory())) {
      whereSql.append(" and category = :category ");
      params.put("category", queryVO.getCategory());
    }
    if (StringUtils.isNotBlank(queryVO.getLevel())) {
      whereSql.append(" and level = :level ");
      params.put("level", queryVO.getLevel());
    }
    if (StringUtils.isNotBlank(queryVO.getStatus())) {
      whereSql.append(" and status = :status ");
      params.put("status", queryVO.getStatus());
    }
    if (StringUtils.isNotBlank(queryVO.getSolver())) {
      if (CollectionUtils.isNotEmpty(solverIds)) {
        whereSql.append(" and solver_id in (:solverIds) ");
        params.put("solverIds", solverIds);
      } else {
        whereSql.append(" and 1=2 ");
      }
    }
    sql.append(whereSql);

    PageUtils.appendPage(sql, page, AlertMessageDO.class);

    List<Map<String, Object>> messageList = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_ALERT_MESSAGE);
    totalSql.append(whereSql);

    Long total = queryForLongWithExceptionHandle(totalSql.toString(), params);

    List<AlertMessageDO> resultList = messageList.stream()
        .map(item -> convertMessageLogMap2DO(item)).collect(Collectors.toList());
    return new PageImpl<>(resultList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertMessageDao#queryAlertMessages(com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO)
   */
  @Override
  public List<AlertMessageDO> queryAlertMessages(AlertMessageQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    sql.append(" where 1=1 ");
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      sql.append(" and arise_time >= toDateTime64(:start_time, 3, 'UTC') ");
      params.put("start_time", queryVO.getStartTime());
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      sql.append(" and arise_time <= toDateTime64(:end_time, 3, 'UTC') ");
      params.put("end_time", queryVO.getEndTime());
    }

    sql.append(" order by arise_time asc ");

    List<Map<String, Object>> messageList = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    return CollectionUtils.isEmpty(messageList) ? Lists.newArrayListWithCapacity(0)
        : messageList.stream().map(item -> convertMessageLogMap2DO(item))
            .collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.AlertMessageDao#countAlertMessages(com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO)
   */
  @Override
  public long countAlertMessages(AlertMessageQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append("select count(1) from ");
    sql.append(TABLE_APPLIANCE_ALERT_MESSAGE);
    sql.append(" where 1=1 ");

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      sql.append(" and arise_time >= toDateTime64(:start_time, 3, 'UTC') ");
      params.put("start_time", queryVO.getStartTime());
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      sql.append(" and arise_time <= toDateTime64(:end_time, 3, 'UTC') ");
      params.put("end_time", queryVO.getEndTime());
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      sql.append(" and network_id = :networkId ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      sql.append(" and service_id = :serviceId ");
      params.put("serviceId", queryVO.getServiceId());
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkId())
        && StringUtils.isBlank(queryVO.getServiceId())) {
      sql.append(" and isNull(service_id) ");
    }
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      sql.append(" and network_id in (:networkIds) and isNull(service_id) ");
      params.put("networkIds", queryVO.getNetworkIds());
    }
    if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      List<Tuple2<String, String>> serviceNetworkIds = queryVO.getServiceNetworkIds();
      sql.append(" and (1=2 ");
      for (int i = 0; i < serviceNetworkIds.size(); i++) {
        sql.append(
            String.format(" or (service_id = :serviceId%s and network_id = :networkId%s) ", i, i));
        params.put("serviceId" + i, serviceNetworkIds.get(i).getT1());
        params.put("networkId" + i, serviceNetworkIds.get(i).getT2());
      }
      sql.append(" ) ");
    }

    return queryForLongWithExceptionHandle(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertMessageDao#queryAlertMessage(java.lang.String)
   */
  @Override
  public AlertMessageDO queryAlertMessage(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<Map<String, Object>> messageList = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    return CollectionUtils.isEmpty(messageList) ? new AlertMessageDO()
        : convertMessageLogMap2DO(messageList.get(0));
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertMessageDao#updateAlertMessageStatus(com.machloop.fpc.manager.appliance.data.AlertMessageDO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int updateAlertMessageStatus(AlertMessageDO alertMessage, String status, String reason,
      String solverId) {
    StringBuilder sql = new StringBuilder();
    sql.append("alter table ").append(TABLE_APPLIANCE_ALERT_MESSAGE);
    sql.append(" update status = :status, solver_id = :solverId, ");
    sql.append(" solve_time = toDateTime64(:solveTime, 3, 'UTC'), reason = :reason ");
    sql.append(" where alert_id = :alertId ");
    if (StringUtils.isNotBlank(alertMessage.getNetworkId())) {
      sql.append(" and network_id = :networkId ");
    }
    if (StringUtils.isNotBlank(alertMessage.getServiceId())) {
      sql.append(" and service_id = :serviceId ");
    } else {
      sql.append(" and isNull(service_id) ");
    }

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("status", status);
    params.put("solverId", solverId);
    params.put("solveTime",
        DateUtils.toStringFormat(DateUtils.now(), "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("reason", reason);
    params.put("alertId", alertMessage.getAlertId());
    params.put("networkId", alertMessage.getNetworkId());
    params.put("serviceId", alertMessage.getServiceId());

    return clickHouseTemplate.getJdbcTemplate().update(sql.toString(), params);
  }

  @Override
  public List<Map<String, Object>> queryAlertMessageAsHistogram(AlertMessageQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(arise_time), :interval)), :interval), 'UTC') as  ariseTime, ");
    sql.append(" network_id, service_id, count(id) as alertCount ").append(" from ")
        .append(TABLE_APPLIANCE_ALERT_MESSAGE);
    sql.append(" where 1=1 ");

    if (queryVO.getStartTimeDate() != null) {
      sql.append(" and arise_time >= toDateTime64(:start_time, 3, 'UTC') ");
      params.put("start_time", DateUtils.toStringFormat(queryVO.getStartTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (queryVO.getEndTimeDate() != null) {
      sql.append(" and arise_time <= toDateTime64(:end_time, 3, 'UTC') ");
      params.put("end_time", DateUtils.toStringFormat(queryVO.getEndTimeDate(),
          "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      sql.append(" and network_id = :networkId ");
      params.put("networkId", queryVO.getNetworkId());
    }
    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      sql.append(" and service_id = :serviceId ");
      params.put("serviceId", queryVO.getServiceId());
    } else {
      sql.append(" and isNull(service_id) ");
    }
    sql.append(" group by ariseTime, network_id, service_id ");
    sql.append(" order by ariseTime ");

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, arise_time, alert_id, network_id, service_id, ");
    sql.append(" name, category, level, alert_define, components, status, ");
    sql.append(" solver_id, solve_time, reason ");
    sql.append(" from ").append(TABLE_APPLIANCE_ALERT_MESSAGE);

    return sql;
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
        LOGGER.info("queryFlowLogs has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryFlowLogs failed, error msg: {}",
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
        LOGGER.info("queryFlowLogs has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("queryFlowLogs failed, error msg: {}",
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

  private AlertMessageDO convertMessageLogMap2DO(Map<String, Object> map) {
    AlertMessageDO alertMessageDO = new AlertMessageDO();

    alertMessageDO.setId(MapUtils.getString(map, "id"));
    alertMessageDO.setAlertId(MapUtils.getString(map, "alert_id"));
    alertMessageDO.setNetworkId(MapUtils.getString(map, "network_id"));
    alertMessageDO.setNetworkId(MapUtils.getString(map, "network_id"));
    alertMessageDO.setServiceId(MapUtils.getString(map, "service_id"));
    alertMessageDO.setName(MapUtils.getString(map, "name"));
    alertMessageDO.setCategory(MapUtils.getString(map, "category"));
    alertMessageDO.setLevel(MapUtils.getString(map, "level"));
    alertMessageDO.setAlertDefine(MapUtils.getString(map, "alert_define"));
    alertMessageDO.setComponents(MapUtils.getString(map, "components"));
    alertMessageDO.setAriseTime(MapUtils.getString(map, "arise_time"));
    alertMessageDO.setStatus(MapUtils.getString(map, "status"));
    alertMessageDO.setSolverId(MapUtils.getString(map, "solver_id"));
    alertMessageDO.setSolveTime(MapUtils.getString(map, "solve_time"));
    alertMessageDO.setReason(MapUtils.getString(map, "reason"));

    return alertMessageDO;
  }

}
