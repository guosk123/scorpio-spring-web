package com.machloop.fpc.baseline.publish.postgre;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.baseline.publish.PublishDao;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.npm.appliance.data.BaselineValueDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月4日, fpc-baseline
 */
@Repository
public class PublishDaoImpl implements PublishDao {

  private static final String TABLE_APPLIANCE_BASELINE_VALUE = "fpc_appliance_baseline_value";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.baseline.publish.PublishDao#publish(java.util.List)
   */
  @Override
  public void publish(List<BaselineValueDO> baselineDOList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_BASELINE_VALUE);
    sql.append(" (id, source_type, source_id, alert_network_id, alert_service_id, ");
    sql.append(" value, calculate_time, timestamp) ");
    sql.append(" values (:id, :sourceType, :sourceId, :alertNetworkId, :alertServiceId, ");
    sql.append(" :value, :calculateTime, :timestamp) ");

    Date now = DateUtils.now();
    for (BaselineValueDO baselineDO : baselineDOList) {
      baselineDO.setId(IdGenerator.generateUUID());
      baselineDO.setTimestamp(now);
    }
    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(baselineDOList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.baseline.publish.PublishDao#cleanAlertBefore(java.util.Date)
   */
  @Override
  public int cleanAlertBefore(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_BASELINE_VALUE);
    sql.append(" where source_type = :sourceType and calculate_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("sourceType", FpcConstants.BASELINE_SETTING_SOURCE_ALERT);
    params.put("beforeTime", beforeTime);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.baseline.publish.PublishDao#cleanNpmBefore(java.util.List, java.util.Date)
   */
  @Override
  public int cleanNpmBefore(List<String> ids, Date beforeTime) {
    if (CollectionUtils.isEmpty(ids)) {
      return 0;
    }

    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_BASELINE_VALUE);
    sql.append(" where source_type = :sourceType ");
    sql.append(" and source_id in (:ids) ");
    sql.append(" and calculate_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("sourceType", FpcConstants.BASELINE_SETTING_SOURCE_NPM);
    params.put("ids", ids);
    params.put("beforeTime", beforeTime);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.baseline.publish.PublishDao#cleanInvalidValue(java.util.List)
   */
  @Override
  public int cleanInvalidValue(List<String> validIds) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_BASELINE_VALUE);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(validIds)) {
      sql.append(" where source_id not in (:validIds) ");
      params.put("validIds", validIds);
    }

    return jdbcTemplate.update(sql.toString(), params);
  }

}
