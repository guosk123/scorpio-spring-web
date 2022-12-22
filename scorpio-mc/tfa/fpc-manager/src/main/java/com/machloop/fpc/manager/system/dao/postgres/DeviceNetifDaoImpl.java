package com.machloop.fpc.manager.system.dao.postgres;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.system.dao.DeviceNetifDao;
import com.machloop.fpc.manager.system.data.DeviceNetifDO;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@Repository
public class DeviceNetifDaoImpl implements DeviceNetifDao {

  private static final String TABLE_SYSTEM_DEVICE_NETIF = "fpc_system_device_netif";
  
  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.system.dao.DeviceNetifDao#queryDeviceNetifs(java.util.List)
   */
  @Override
  public List<DeviceNetifDO> queryDeviceNetifs(List<String> categoryList) {
    StringBuilder sql = new StringBuilder();
    sql.append(
        "select id, name, type, state, category, specification, ipv4_address, ipv4_gateway, ");

    sql.append(" description, ipv6_address, ipv6_gateway, update_time, operator_id ");
    sql.append(" from ").append(TABLE_SYSTEM_DEVICE_NETIF);
    sql.append(" where category in (:categoryList) ");
    sql.append(" and deleted = :deleted ");
    sql.append(" order by name ASC ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("categoryList", categoryList);
    params.put("deleted", Constants.BOOL_NO);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DeviceNetifDO.class));
  }

  @Override
  public int updateDeviceNetifs(List<DeviceNetifDO> netifDOList) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_SYSTEM_DEVICE_NETIF);
    sql.append(" set category = :category, description = :description, ");
    sql.append(" ipv4_address = :ipv4Address, ipv4_gateway = :ipv4Gateway, ");
    sql.append(" ipv6_address = :ipv6Address, ipv6_gateway = :ipv6Gateway, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    Date updateDate = DateUtils.now();

    int result = 0;
    for (DeviceNetifDO netifDO : netifDOList) {

      netifDO.setUpdateTime(updateDate);
      if(StringUtils.isBlank(netifDO.getIpv4Address())) {
        netifDO.setIpv4Address("");
        netifDO.setIpv4Gateway("");
      }
      if(StringUtils.isBlank(netifDO.getIpv6Address())) {
        netifDO.setIpv6Address("");
        netifDO.setIpv6Gateway("");
      }
      SqlParameterSource paramSource = new BeanPropertySqlParameterSource(netifDO);
      result += jdbcTemplate.update(sql.toString(), paramSource);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.DeviceNetifDao#updateDeviceNetifState(java.lang.String, java.lang.String)
   */
  @Override
  public int updateDeviceNetifState(String id, String state) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_SYSTEM_DEVICE_NETIF);
    sql.append(" set state = :state ");
    sql.append(" where id = :id ");

    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);
    params.put("state", state);

    return jdbcTemplate.update(sql.toString(), params);
  }

}
