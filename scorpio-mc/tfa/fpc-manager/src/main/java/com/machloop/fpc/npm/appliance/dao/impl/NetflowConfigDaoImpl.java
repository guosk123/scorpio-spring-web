package com.machloop.fpc.npm.appliance.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.npm.appliance.dao.NetflowConfigDao;
import com.machloop.fpc.npm.appliance.data.NetflowConfigDO;
import com.machloop.fpc.npm.appliance.vo.NetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月12日, fpc-manager
 */
@Repository
public class NetflowConfigDaoImpl implements NetflowConfigDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(NetflowConfigDaoImpl.class);

  private static final String TABLE_APPLIANCE_NETFLOW_SOURCE = "fpc_appliance_netflow_source";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  public List<NetflowConfigDO> queryNetflowConfigs(String keywords) {
    StringBuilder sql = netflowDeviceSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (StringUtils.isNotBlank(keywords)) {
      whereSql.append(" where device_name like :keywords ");
      whereSql.append(" or netif_no like :keywords ");
      whereSql.append(" or alias like :keywords ");
      params.put("keywords", "%" + keywords + "%");
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryNetflowsConfigWithKeywords, inner sql: {}, params: {}", sql.toString());
    }

    sql.append(whereSql);
    List<NetflowConfigDO> netflowDeviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetflowConfigDO.class));

    return netflowDeviceList;
  }

  private static StringBuilder netflowDeviceSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select id, device_name, device_type, netif_no,");
    sql.append(" alias, netif_speed, protocol_version, description, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_NETFLOW_SOURCE);

    return sql;
  }

  @Override
  public int updateNetflowDevice(List<NetflowConfigDO> netflowDeviceList) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETFLOW_SOURCE);
    sql.append(" set alias = :alias, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    Date updateDate = DateUtils.now();

    int result = 0;
    for (NetflowConfigDO netflowDevice : netflowDeviceList) {

      netflowDevice.setUpdateTime(updateDate);
      SqlParameterSource paramSource = new BeanPropertySqlParameterSource(netflowDevice);
      result += jdbcTemplate.update(sql.toString(), paramSource);
    }
    return result;
  }

  @Override
  public int updateNetflowNetif(List<NetflowConfigDO> netflowNetifList) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_NETFLOW_SOURCE);
    sql.append(" set alias = :alias, description = :description, netif_speed = :netifSpeed, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    Date updateDate = DateUtils.now();

    int result = 0;
    for (NetflowConfigDO netifDO : netflowNetifList) {

      netifDO.setUpdateTime(updateDate);
      SqlParameterSource paramSource = new BeanPropertySqlParameterSource(netifDO);
      result += jdbcTemplate.update(sql.toString(), paramSource);
    }
    return result;
  }

  @Override
  public List<NetflowConfigDO> queryNetflowConfigsByName(List<String> deviceNameList) {
    StringBuilder sql = netflowDeviceSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where device_name in (:deviceNameList)");
    params.put("deviceNameList", deviceNameList);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("queryNetflowsByNameList, inner sql: {}, params: {}", sql.toString());
    }

    List<NetflowConfigDO> netflowDeviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(NetflowConfigDO.class));
    return netflowDeviceList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.NetflowConfigDao#queryNetflowConfigGroupByDevAndNif(com.machloop.fpc.npm.appliance.vo.NetflowQueryVO)
   */
  @Override
  public List<NetflowConfigDO> queryNetflowConfigsGroupByDevAndNif(NetflowQueryVO queryVO) {
    StringBuilder sql = netflowDeviceSelectStatement();
    List<NetflowConfigDO> netflowConfigList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(NetflowConfigDO.class));

    return netflowConfigList;
  }

}
