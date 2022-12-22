package com.machloop.fpc.manager.system.dao.clickhouse;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.system.dao.MetricRestApiDao;
import com.machloop.fpc.manager.system.data.MetricRestApiRecordDO;
import com.machloop.fpc.manager.system.vo.MetricRestApiQueryVO;

import reactor.util.function.Tuple2;


@Repository
@EnableScheduling
public class MetricRestApiDaoImpl implements MetricRestApiDao {

  @Autowired
  private ClickHouseStatsJdbcTemplate jdbcTemplate;
  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricRestApiDaoImpl.class);

  private static final String TABLE_NAME = ManagerConstants.TABLE_METRIC_RESTAPI_DATA_RECORD;


  @Override
  public Page<MetricRestApiRecordDO> queryMetricRestApiRecords(MetricRestApiQueryVO queryVO,
      PageRequest page, String sortProperty, String sortDirection) {
    List<MetricRestApiRecordDO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Integer totalSize = 0;
    try {
      StringBuilder sql = new StringBuilder();
      sql.append(
          " select timestamp, api_name, uri, method, user_ip, user_id, status, response from ")
          .append(TABLE_NAME);
      StringBuilder whereSql = new StringBuilder();
      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

      enrichWhereSql(queryVO, whereSql, params);

      sql.append(whereSql);

      sql.append(" order by ").append(sortProperty).append(" ").append(sortDirection);


      List<Map<String, Object>> tempResult = queryWithExceptionHandle(sql.toString(), params,
          new ColumnMapRowMapper());
      // 没有记录
      if (CollectionUtils.isEmpty(tempResult)) {
        return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
      }
      totalSize = tempResult.size();
      if (page != null) {
        int currentPageStart = page.getOffset();
        int currentPageEnd = page.getOffset() + page.getPageSize();
        if (currentPageStart < totalSize) {
          tempResult = tempResult.subList(currentPageStart,
              currentPageEnd > totalSize ? totalSize : currentPageEnd);
        } else {
          tempResult = Lists.newArrayListWithCapacity(0);
        }
      }
      tempResult.forEach(item -> result.add(convertMetricRestApiRecordMap2LogDO(item)));

    } catch (Exception e) {
      LOGGER.warn("failed to query restapi metric.", e);
    }
    return new PageImpl<>(result, page, totalSize);
  }


  @Override
  public List<Map<String, Object>> queryUserTop(MetricRestApiQueryVO queryVO) {

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    enrichWhereSql(queryVO, whereSql, params);

    StringBuilder sql = new StringBuilder();
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') as temp_timestamp, ");
    sql.append(" user_id, count(uri) as count ").append(" from ").append(TABLE_NAME);
    sql.append(whereSql);
    List<String> userIdList = queryUserId(queryVO);
    if (!userIdList.isEmpty()) {
      sql.append(" and user_id in (:userId) ");
      params.put("userId", userIdList);
    }
    sql.append(" group by temp_timestamp, user_id ");
    sql.append(" order by temp_timestamp , count desc ");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryUserTop sql : [{}], param: [{}] ", sql, params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      item.put("timestamp", DateUtils.toStringYYYYMMDDHHMMSS(
          (OffsetDateTime) item.get("temp_timestamp"), ZoneId.systemDefault()));
      item.put("userId", item.get("user_id"));
      item.remove("temp_timestamp");
      item.remove("user_id");
    });

    return result;
  }

  public List<String> queryUserId(MetricRestApiQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select user_id, count(uri) as count from ").append(TABLE_NAME);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    enrichWhereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by user_id order by count desc ");
    sql.append(" limit ").append(queryVO.getCount());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page, sql: {}, params: {}", TABLE_NAME, sql, params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    List<String> list = new ArrayList<>();
    for (Map<String, Object> map : result) {
      list.add(MapUtils.getString(map, "user_id"));
    }
    return list;
  }

  @Override
  public List<Map<String, Object>> queryApiTop(MetricRestApiQueryVO queryVO) {

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", queryVO.getInterval());
    enrichWhereSql(queryVO, whereSql, params);

    StringBuilder sql = new StringBuilder();
    sql.append(
        " select toDateTime(multiply(ceil(divide(toUnixTimestamp(timestamp), :interval)), :interval), 'UTC') as  temp_timestamp, ");
    sql.append(" api_name, count(uri) as count ").append(" from ").append(TABLE_NAME);
    sql.append(whereSql);
    List<String> apiNameList = queryApiName(queryVO);
    if (!apiNameList.isEmpty()) {
      sql.append(" and api_name in (:apiNameList) ");
      params.put("apiNameList", apiNameList);
    }
    sql.append(" group by  temp_timestamp, api_name ");
    sql.append(" order by temp_timestamp , count desc ");
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryUserTop sql : [{}], param: [{}] ", sql, params);
    }

    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());

    result.forEach(item -> {
      item.put("timestamp", DateUtils.toStringYYYYMMDDHHMMSS(
          (OffsetDateTime) item.get("temp_timestamp"), ZoneId.systemDefault()));
      item.put("apiName", item.get("api_name"));
      item.remove("temp_timestamp");
      item.remove("api_name");
    });

    return result;
  }

  public List<String> queryApiName(MetricRestApiQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select api_name, count(uri) as count from ").append(TABLE_NAME);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    enrichWhereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by api_name order by count desc ");
    sql.append(" limit ").append(queryVO.getCount());

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page, sql: {}, params: {}", TABLE_NAME, sql, params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    List<String> list = new ArrayList<>();
    for (Map<String, Object> map : result) {
      list.add(MapUtils.getString(map, "api_name"));
    }
    return list;
  }

  @Override
  public List<Map<String, Object>> queryUserList(MetricRestApiQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select user_id, count(uri) as count from ").append(TABLE_NAME);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    enrichWhereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by user_id order by count desc ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page, sql: {}, params: {}", TABLE_NAME, sql, params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    result.forEach(item -> {
      item.put("userId", item.get("user_id"));
      item.remove("user_id");
    });

    return result;
  }

  @Override
  public List<Map<String, Object>> queryApiList(MetricRestApiQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select api_name, count(uri) as count from ").append(TABLE_NAME);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    enrichWhereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by api_name order by count desc ");

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("query {} logs page, sql: {}, params: {}", TABLE_NAME, sql, params);
    }
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    result.forEach(item -> {
      item.put("apiName", item.get("api_name"));
      item.remove("api_name");
    });

    return result;
  }

  private MetricRestApiRecordDO convertMetricRestApiRecordMap2LogDO(Map<String, Object> item) {
    MetricRestApiRecordDO metricRestApiRecordDO = new MetricRestApiRecordDO();

    metricRestApiRecordDO.setTimestamp(DateUtils
        .toStringYYYYMMDDHHMMSS((OffsetDateTime) item.get("timestamp"), ZoneId.systemDefault()));
    metricRestApiRecordDO.setApiName(MapUtils.getString(item, "api_name"));
    metricRestApiRecordDO.setUri(MapUtils.getString(item, "uri"));
    metricRestApiRecordDO.setMethod(MapUtils.getString(item, "method"));
    metricRestApiRecordDO.setUserIp(MapUtils.getString(item, "user_ip"));
    metricRestApiRecordDO.setUserId(MapUtils.getString(item, "user_id"));
    metricRestApiRecordDO.setStatus(MapUtils.getIntValue(item, "status"));
    metricRestApiRecordDO.setResponse(MapUtils.getString(item, "response"));
    return metricRestApiRecordDO;

  }

  private List<Map<String, Object>> queryWithExceptionHandle(String sql,
      Map<String, Object> paramMap, ColumnMapRowMapper rowMapper) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    try {
      result = jdbcTemplate.getJdbcTemplate().query(sql, paramMap, rowMapper);
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
      } else {
        // 其他异常重新抛出
        throw e;
      }
    }
    return result;
  }

  protected void enrichWhereSql(MetricRestApiQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {

    whereSql.append(" where 1=1 ");

    // 使用dsl表达式查询
    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> dsl = spl2SqlHelper.converte(queryVO.getDsl(),
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
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(),
        queryVO.getIncludeStartTime(), queryVO.getIncludeEndTime(), whereSql, params);

  }

  protected void enrichContainTimeRangeBetter(Date startTime, Date endTime,
      boolean includeStartTime, boolean includeEndTime, StringBuilder whereSql,
      Map<String, Object> params) {
    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(String.format(" and timestamp %s toDateTime(:start_time, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and timestamp %s toDateTime(:end_time, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }


  @Override
  public void saveMetricRestApiRecord(List<Map<String, Object>> batchList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_NAME);
    sql.append("(timestamp, api_name, uri, method, user_ip, user_id, status, response)");
    sql.append(
        " values (:timestamp, :apiName, :uri, :method, :userIp, :userId, :status, :response)");
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(batchList);
    jdbcTemplate.getJdbcTemplate().batchUpdate(sql.toString(), batchSource);
  }

}
