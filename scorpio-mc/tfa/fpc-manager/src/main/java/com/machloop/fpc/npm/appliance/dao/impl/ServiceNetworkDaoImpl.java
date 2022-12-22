package com.machloop.fpc.npm.appliance.dao.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao;
import com.machloop.fpc.npm.appliance.data.ServiceNetworkDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
@Repository
public class ServiceNetworkDaoImpl implements ServiceNetworkDao {

  private static final String TABLE_APPLIANCE_SERVICE_NETWORK = "fpc_appliance_service_network";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao#queryServiceNetworks()
   */
  @Override
  public List<ServiceNetworkDO> queryServiceNetworks() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, service_id, network_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SERVICE_NETWORK);

    List<ServiceNetworkDO> serviceNetworkList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(ServiceNetworkDO.class));

    return CollectionUtils.isEmpty(serviceNetworkList) ? Lists.newArrayListWithCapacity(0)
        : serviceNetworkList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao#queryAssignServiceNetworkIds(java.util.Date)
   */
  @Override
  public List<String> queryAssignServiceNetworkIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select service_network_in_cms_id from ").append(TABLE_APPLIANCE_SERVICE_NETWORK);
    sql.append(" where deleted = :deleted and service_network_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao#queryServiceNetworks(java.util.List)
   */
  @Override
  public List<ServiceNetworkDO> queryServiceNetworks(List<String> serviceIds) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, service_id, network_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SERVICE_NETWORK);
    sql.append(" where service_id in (:serviceIds) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("serviceIds", serviceIds);

    List<ServiceNetworkDO> serviceNetworkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceNetworkDO.class));

    return CollectionUtils.isEmpty(serviceNetworkList) ? Lists.newArrayListWithCapacity(0)
        : serviceNetworkList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao#queryServiceNetworks(java.lang.String, java.lang.String)
   */
  @Override
  public List<ServiceNetworkDO> queryServiceNetworks(String serviceId, String networkId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, service_id, network_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SERVICE_NETWORK);
    sql.append(" where 1=1 ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(serviceId)) {
      sql.append(" and service_id = :serviceId ");
      params.put("serviceId", serviceId);
    }
    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" and network_id = :networkId ");
      params.put("networkId", networkId);
    }

    List<ServiceNetworkDO> serviceNetworkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceNetworkDO.class));

    return CollectionUtils.isEmpty(serviceNetworkList) ? Lists.newArrayListWithCapacity(0)
        : serviceNetworkList;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao#mergeServiceNetworks(java.util.List)
   */
  @Override
  public void mergeServiceNetworks(List<ServiceNetworkDO> serviceNetworks) {
    // delete
    deleteServiceNetwork(serviceNetworks.get(0).getServiceId());

    // batch insert
    batchSaveServiceNetwork(serviceNetworks);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao#batchSaveServiceNetwork(java.util.List)
   */
  @Override
  public void batchSaveServiceNetwork(List<ServiceNetworkDO> serviceNetworks) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_SERVICE_NETWORK);
    sql.append(" (id, service_id, network_id) ");
    sql.append(" values (:id, :serviceId, :networkId) ");

    serviceNetworks.forEach(serviceRule -> {
      serviceRule.setId(IdGenerator.generateUUID());
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(serviceNetworks);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao#deleteServiceNetwork(java.lang.String)
   */
  @Override
  public int deleteServiceNetwork(String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_SERVICE_NETWORK);
    sql.append(" where service_id = :serviceId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("serviceId", serviceId);
    return jdbcTemplate.update(sql.toString(), params);
  }

}
