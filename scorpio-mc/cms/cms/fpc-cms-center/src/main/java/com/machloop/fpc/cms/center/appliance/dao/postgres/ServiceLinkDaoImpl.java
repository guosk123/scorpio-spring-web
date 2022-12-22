package com.machloop.fpc.cms.center.appliance.dao.postgres;

import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao;
import com.machloop.fpc.cms.center.appliance.data.ServiceLinkDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
@Repository
public class ServiceLinkDaoImpl implements ServiceLinkDao {

  private static final String TABLE_APPLIANCE_SERVICE_LINK = "fpccms_appliance_service_link";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao#queryServiceLinks()
   */
  @Override
  public List<ServiceLinkDO> queryServiceLinks() {
    StringBuilder sql = buildSelectStatement();
    List<ServiceLinkDO> serviceLinkList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(ServiceLinkDO.class));

    return CollectionUtils.isEmpty(serviceLinkList) ? Lists.newArrayListWithCapacity(0)
        : serviceLinkList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao#queryServiceLink(java.lang.String)
   */
  @Override
  public ServiceLinkDO queryServiceLink(String serviceId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where service_id = :serviceId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("serviceId", serviceId);

    List<ServiceLinkDO> serviceLinkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceLinkDO.class));

    return CollectionUtils.isEmpty(serviceLinkList) ? new ServiceLinkDO() : serviceLinkList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao#queryAssignServiceLinkIds(java.util.Date)
   */
  @Override
  public List<ServiceLinkDO> queryAssignServiceLinkIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_SERVICE_LINK);
    sql.append(" where assign_id != '' ");
    sql.append(" and timestamp < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);

    List<ServiceLinkDO> serviceLinkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ServiceLinkDO.class));

    return CollectionUtils.isEmpty(serviceLinkList) ? Lists.newArrayListWithCapacity(0)
        : serviceLinkList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao#queryServiceLinkIds(java.util.Date)
   */
  @Override
  public List<String> queryServiceLinkIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_SERVICE_LINK);
    sql.append(" where timestamp < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao#saveOrUpdateServiceLink(com.machloop.fpc.cms.center.appliance.data.ServiceLinkDO)
   */
  @Override
  public int saveOrUpdateServiceLink(ServiceLinkDO serviceLinkDO) {
    int update = updateServiceLink(serviceLinkDO);
    return update > 0 ? update : saveServiceLink(serviceLinkDO);
  }

  private int saveServiceLink(ServiceLinkDO serviceLinkDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SERVICE_LINK);
    sql.append(" (id, service_id, link, metric, timestamp, operator_id) ");
    sql.append(" values (:id, :serviceId, :link, :metric, :timestamp, :operatorId)");

    if (StringUtils.isBlank(serviceLinkDO.getId())) {
      serviceLinkDO.setId(IdGenerator.generateUUID());
    }
    serviceLinkDO.setTimestamp(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(serviceLinkDO);

    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int updateServiceLink(ServiceLinkDO serviceLinkDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SERVICE_LINK);
    sql.append(" set link = :link, metric = :metric, timestamp = :timestamp, ");
    sql.append(" operator_id = :operatorId ");
    sql.append(" where service_id = :serviceId ");

    serviceLinkDO.setTimestamp(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(serviceLinkDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao#batchSaveServiceLink(java.util.List)
   */
  @Override
  public int batchSaveServiceLink(List<ServiceLinkDO> serviceLinks) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SERVICE_LINK);
    sql.append(" (id, service_id, link, metric, timestamp, operator_id) ");
    sql.append(" values (:id, :serviceId, :link, :metric, :timestamp, :operatorId)");

    serviceLinks.forEach(serviceLink -> {
      serviceLink.setId(IdGenerator.generateUUID());
      serviceLink.setTimestamp(DateUtils.now());
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(serviceLinks);

    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao#deleteServiceLink(java.lang.String)
   */
  @Override
  public int deleteServiceLink(String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_SERVICE_LINK);
    sql.append(" where service_id = :serviceId ");

    Map<String, Object> param = Maps.newHashMapWithExpectedSize(1);
    param.put("serviceId", serviceId);

    return jdbcTemplate.update(sql.toString(), param);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, service_id, link, metric, timestamp, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SERVICE_LINK);

    return sql;
  }

}
