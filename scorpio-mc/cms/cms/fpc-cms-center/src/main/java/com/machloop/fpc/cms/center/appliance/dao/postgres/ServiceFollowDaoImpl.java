package com.machloop.fpc.cms.center.appliance.dao.postgres;

import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.ServiceFollowDao;
import com.machloop.fpc.cms.center.appliance.data.ServiceFollowDO;


/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
@Repository
public class ServiceFollowDaoImpl implements ServiceFollowDao {

  private static final String TABLE_APPLIANCE_USER_SERVICE_FOLLOW = "fpccms_appliance_user_service_follow";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceFollowDao#queryUserFollowService(java.lang.String)
   */
  @Override
  public List<ServiceFollowDO> queryUserFollowService(String userId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, user_id, service_id, network_id, network_group_id, follow_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_USER_SERVICE_FOLLOW);
    sql.append(" where user_id = :userId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("userId", userId);

    List<ServiceFollowDO> serviceFollowList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceFollowDO.class));

    return CollectionUtils.isEmpty(serviceFollowList) ? Lists.newArrayListWithCapacity(0)
        : serviceFollowList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceFollowDao#saveServiceFollow(com.machloop.fpc.cms.center.appliance.data.ServiceFollowDO)
   */
  @Override
  public ServiceFollowDO saveServiceFollow(ServiceFollowDO serviceFollowDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_USER_SERVICE_FOLLOW);
    sql.append(" (id, user_id, service_id, network_id, network_group_id, follow_time) ");
    sql.append(" values (:id, :userId, :serviceId, :networkId, :networkGroupId, :followTime) ");

    serviceFollowDO.setId(IdGenerator.generateUUID());
    serviceFollowDO.setFollowTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(serviceFollowDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return serviceFollowDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceFollowDao#deleteServiceFollow(java.lang.String)
   */
  @Override
  public int deleteServiceFollow(String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_USER_SERVICE_FOLLOW);
    sql.append(" where service_id = :serviceId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("serviceId", serviceId);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceFollowDao#deleteServiceFollow(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int deleteServiceFollow(String userId, String serviceId, String networkId,
      String networkGroupId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_USER_SERVICE_FOLLOW);
    sql.append(" where service_id = :serviceId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("serviceId", serviceId);

    if (StringUtils.isNotBlank(userId)) {
      sql.append(" and user_id = :userId ");
      params.put("userId", userId);
    }

    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" and network_id = :networkId ");
      params.put("networkId", networkId);
    }

    if (StringUtils.isNotBlank(networkGroupId)) {
      sql.append(" and network_group_id = :networkGroupId ");
      params.put("networkGroupId", networkGroupId);
    }

    return jdbcTemplate.update(sql.toString(), params);
  }

}
