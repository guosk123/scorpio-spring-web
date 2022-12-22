package com.machloop.fpc.npm.appliance.dao.clickhouse;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.boot.configuration.ClickHouseStatsJdbcTemplate;
import com.machloop.fpc.npm.appliance.dao.NetflowStatisticDao;
import com.machloop.fpc.npm.appliance.data.NetflowStatisticDO;
import com.machloop.fpc.npm.appliance.vo.NetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月13日, fpc-manager
 */
@Repository
public class NetflowStatisticDaoImpl implements NetflowStatisticDao {

  private static final String TABLE_NETFLOW_SOURCE = "t_netflow_source_statistics";

  @Autowired
  private ClickHouseStatsJdbcTemplate clickHouseTemplate;

  private static StringBuilder netflowStatisticStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select device_name, netif_no, sum(total_bytes) as total_bytes, ");
    sql.append("sum(ingest_bytes) as ingest_bytes, sum(transmit_bytes) as transmit_bytes ");
    sql.append(" from ").append(TABLE_NETFLOW_SOURCE);

    return sql;
  }

  private void enrichTimeRange(NetflowQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {

    if (queryVO.getStartTimeDate() == null || queryVO.getEndTimeDate() == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    whereSql.append(String.format(" and report_time %s toDateTime64(:start_time, 9, 'UTC') ",
        queryVO.getIncludeStartTime() ? ">=" : ">"));
    whereSql.append(String.format(" and report_time %s toDateTime64(:end_time, 9, 'UTC') ",
        queryVO.getIncludeEndTime() ? "<=" : "<"));
    params.put("start_time", DateUtils.toStringFormat(queryVO.getStartTimeDate(),
        "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    params.put("end_time",
        DateUtils.toStringFormat(queryVO.getEndTimeDate(), "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
  }


  @Override
  public List<NetflowStatisticDO> queryNetflowStatsGroupByDevAndNif(NetflowQueryVO queryVO) {
    StringBuilder sql = netflowStatisticStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where 1=1 ");
    enrichTimeRange(queryVO, whereSql, params);
    sql.append(whereSql);
    sql.append(" group by device_name, netif_no ");
    List<NetflowStatisticDO> result = clickHouseTemplate.getJdbcTemplate().query(sql.toString(),
        params, new BeanPropertyRowMapper<>(NetflowStatisticDO.class));

    return result;
  }


}
