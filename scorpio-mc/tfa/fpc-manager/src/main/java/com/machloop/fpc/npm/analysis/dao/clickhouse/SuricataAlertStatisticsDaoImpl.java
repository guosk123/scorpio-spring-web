package com.machloop.fpc.npm.analysis.dao.clickhouse;

import java.io.IOException;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.npm.analysis.dao.SuricataAlertStatisticsDao;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 *
 * create at 2022年4月11日, fpc-manager
 */
@Repository
public class SuricataAlertStatisticsDaoImpl implements SuricataAlertStatisticsDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SuricataAlertStatisticsDaoImpl.class);

  private static final String TABLE_ANALYSIS_SURICATA_ALERT_STATISTICS = "t_fpc_analysis_suricata_alert_statistics";

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  @Autowired
  private Spl2SqlHelper dslConverter;

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataAlertStatisticsDao#queryAlertStatistics(java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryAlertStatistics(String dsl, int topCount) {
    StringBuilder sql = new StringBuilder();
    sql.append("select key, sum(value) as count ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT_STATISTICS);

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(dsl, null, null, null, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by key ");
    sql.append(" order by count desc ");
    if (!params.values()
        .contains(FpcConstants.SURICATA_ALERT_STATISTICS_TYPE_CLASSIFICATION_PROPORTION)
        && !params.values()
            .contains(FpcConstants.SURICATA_ALERT_STATISTICS_TYPE_MITRE_TACTIC_PROPORTION)) {
      sql.append(" limit ").append(topCount);
    }

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataAlertStatisticsDao#queryAlertStatistics(java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public List<Map<String, Object>> queryAlertStatistics(String type, Date startTime, Date endTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select key, sum(value) as count ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT_STATISTICS);

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(null, type, startTime, endTime, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by key ");
    sql.append(" order by count desc ");

    return clickHouseTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataAlertStatisticsDao#queryAlertDateHistogram(java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryAlertDateHistogram(String dsl, int interval) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select toStartOfInterval(timestamp, INTERVAL :interval second) as temp_timestamp, ");
    sql.append(" sum(value) as count ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_ALERT_STATISTICS);

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("interval", interval);
    whereSql(dsl, null, null, null, whereSql, params);

    sql.append(whereSql);
    sql.append(" group by temp_timestamp ");
    sql.append(" order by temp_timestamp ");

    List<Map<String, Object>> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new ColumnMapRowMapper());

    result.forEach(item -> {
      item.put("timestamp", item.get("temp_timestamp"));
      item.remove("temp_timestamp");
    });

    return result;
  }

  private void whereSql(String dsl, String type, Date startTime, Date endTime,
      StringBuilder whereSql, Map<String, Object> params) {
    SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
    queryVO.setDsl(dsl);

    whereSql.append(" where 1=1 ");

    if (StringUtils.isNotBlank(queryVO.getDsl())) {
      try {
        Tuple2<String,
            Map<String, Object>> filterTuple = dslConverter.converte(queryVO.getDsl(), false,
                queryVO.getTimePrecision(), queryVO.getIncludeStartTime(),
                queryVO.getIncludeEndTime());
        whereSql.append(" and ");
        whereSql.append(filterTuple.getT1());
        params.putAll(filterTuple.getT2());
        return;
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    // 过滤时间
    enrichContainTimeRangeBetter(startTime, endTime, queryVO.getIncludeStartTime(),
        queryVO.getIncludeEndTime(), whereSql, params);

    if (StringUtils.isNotBlank(type)) {
      whereSql.append(" and type = :type ");
      params.put("type", type);
    }
  }

  private void enrichContainTimeRangeBetter(Date startTime, Date endTime, boolean includeStartTime,
      boolean includeEndTime, StringBuilder whereSql, Map<String, Object> params) {

    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    // 日志开始时间在查询时间范围中
    whereSql.append(String.format(" and timestamp %s toDateTime64(:start_time, 3, 'UTC') ",
        includeStartTime ? ">=" : ">"));
    whereSql.append(String.format(" and timestamp %s toDateTime64(:end_time, 3, 'UTC') ",
        includeEndTime ? "<=" : "<"));
    params.put("start_time",
        DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }

}
