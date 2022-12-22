package com.machloop.fpc.manager.appliance.dao.clickhouse;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

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
import com.machloop.fpc.manager.appliance.dao.MailAlertDao;
import com.machloop.fpc.manager.appliance.vo.MailAlertQueryVO;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月31日, fpc-manager
 */
@Component
public class MailAlertDaoImpl implements MailAlertDao {

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  private static final String TABLE_MAIL_ALERT_MESSAGE = "t_fpc_mail_alert_message";

  private static final Logger LOGGER = LoggerFactory.getLogger(MailAlertDaoImpl.class);

  /**
   * @see com.machloop.fpc.manager.appliance.dao.MailAlertDao#queryMailAlerts(com.machloop.fpc.manager.appliance.vo.MailAlertQueryVO, com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<Map<String, Object>> queryMailAlerts(MailAlertQueryVO queryVO, Pageable page) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    sql.append(whereSql);

    PageUtils.appendPage(sql, page, MailAlertQueryVO.class);

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    // 没有记录
    if (CollectionUtils.isEmpty(result)) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }
    return new PageImpl<>(result, page, 0);
  }

  private void enrichWhereSql(MailAlertQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where 1 = 1 ");

    if (StringUtils.isNotBlank(queryVO.getMailAddress())) {
      whereSql.append(" and mail_address = :mailAddress ");
      params.put("mailAddress", queryVO.getMailAddress());
    }

    if (StringUtils.isNotBlank(queryVO.getSrcIp())) {
      whereSql.append(" and src_ip = :srcIp ");
      params.put("srcIp", queryVO.getSrcIp());
    }
    if (StringUtils.isNotBlank(queryVO.getDestIp())) {
      whereSql.append(" and dest_ip = :destIp ");
      params.put("destIp", queryVO.getDestIp());
    }
    if (StringUtils.isNotBlank(queryVO.getSrcPort())) {
      whereSql.append(" and src_port = :srcPort ");
      params.put("srcPort", Integer.parseInt(queryVO.getSrcPort()));
    }
    if (StringUtils.isNotBlank(queryVO.getDestPort())) {
      whereSql.append(" and dest_port = :destPort ");
      params.put("destPort", Integer.parseInt(queryVO.getDestPort()));
    }
    if (StringUtils.isNotBlank(queryVO.getProtocol())) {
      whereSql.append(" and protocol = :protocol ");
      params.put("protocol", queryVO.getProtocol());
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.isIncludeStartTime(), queryVO.isIncludeEndTime(), whereSql, params);
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    whereSql.append(String.format(" and timestamp %s toDateTime64(:start_time, 9, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and timestamp %s toDateTime64(:end_time, 9, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.MailAlertDao#countMailAlerts(com.machloop.fpc.manager.appliance.vo.MailAlertQueryVO)
   */
  @Override
  public long countMailAlerts(MailAlertQueryVO queryVO) {
    StringBuilder countSql = new StringBuilder();
    countSql.append("select count(1) from ");
    countSql.append(TABLE_MAIL_ALERT_MESSAGE);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, countSql, params);

    return queryForLongWithExceptionHandle(countSql.toString(), params);
  }

  private static StringBuilder buildSelectStatement() {

    StringBuilder sql = new StringBuilder();
    sql.append(
        "select timestamp, IPv6NumToString(src_ip) as src_ip, IPv6NumToString(dest_ip) as dest_ip, ");
    sql.append(" src_port, dest_port, protocol, mail_address, country_id, province_id, city_id, ");
    sql.append(" login_timestamp, description, flow_id, network_id, rule_id ");
    sql.append(" from ").append(TABLE_MAIL_ALERT_MESSAGE);
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
        LOGGER.info("query metadata counter has been canceled.");
      } else if (errorCode == 241 || errorCode == 173) {
        // 查询时可用内存不足返回错误码241，无法申请内存返回173
        LOGGER.warn("query metadata counter failed, error msg: {}",
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
}
