package com.machloop.fpc.cms.center.appliance.dao.postgres;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.appliance.dao.BaselineValueDao;
import com.machloop.fpc.cms.center.appliance.data.BaselineValueDO;

/**
 * @author guosk
 *
 * create at 2021年5月10日, fpc-manager
 */
@Repository
public class BaselineValueDaoImpl implements BaselineValueDao {

  private static final String TABLE_APPLIANCE_BASELINE_VALUE = "fpccms_appliance_baseline_value";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.BaselineValueDao#queryBaselineValues(java.lang.String, java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public List<BaselineValueDO> queryBaselineValues(String sourceType, String sourceId,
      Date startTime, Date endTime) {
    StringBuilder sql = new StringBuilder();
    sql.append(" select id, source_type, source_id, value, calculate_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_BASELINE_VALUE);
    sql.append(" where source_type = :sourceType and source_id = :sourceId ");
    sql.append(" and calculate_time >= :startTime and calculate_time < :endTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("sourceType", sourceType);
    params.put("sourceId", sourceId);
    params.put("startTime", startTime);
    params.put("endTime", endTime);

    List<BaselineValueDO> baselineValueList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(BaselineValueDO.class));

    return CollectionUtils.isEmpty(baselineValueList) ? Lists.newArrayListWithCapacity(0)
        : baselineValueList;
  }

}
