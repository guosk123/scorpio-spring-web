package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.dao.NatConfigDao;
import com.machloop.fpc.manager.appliance.data.NatConfigDO;

/**
 * @author ChenXiao
 * create at 2022/11/9
 */
@Repository
public class NatConfigDaoImpl implements NatConfigDao {


  private static final String TABLE_APPLIANCE_NAT_CONFIG = "fpc_appliance_nat_config";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public NatConfigDO queryNatConfig() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select id,nat_action,update_time,operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_NAT_CONFIG);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    sql.append(whereSql);

    return jdbcTemplate
        .query(sql.toString(), params, new BeanPropertyRowMapper<>(NatConfigDO.class)).get(0);

  }

  @Override
  public void updateNatConfig(NatConfigDO natConfigDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NAT_CONFIG);
    sql.append(" set nat_action = :natAction, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    natConfigDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(natConfigDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }
}
