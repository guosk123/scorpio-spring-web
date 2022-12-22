package com.machloop.fpc.manager.metadata.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.metadata.dao.CollectPolicyDao;
import com.machloop.fpc.manager.metadata.data.CollectPolicyDO;

@Repository
public class CollectPolicyDaoImpl implements CollectPolicyDao {

  private static final String TABLE_COLLECT_POLICY = "fpc_appliance_collect_policy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.metadata.dao.CollectPolicyDao#queryCollectPolicys()
   */
  @Override
  public List<CollectPolicyDO> queryCollectPolicys() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" order by order_no desc");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CollectPolicyDO.class));
  }

  @Override
  public List<CollectPolicyDO> queryCollectAllPolicy() {
    StringBuilder sql = buildSelectStatement();
    List<CollectPolicyDO> query = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(CollectPolicyDO.class));
    return query;
  }

  @Override
  public CollectPolicyDO queryCollectPolicyWithIpv6(String ipAddress) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where ip_address = :ipAddress");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("ipAddress", ipAddress);

    List<CollectPolicyDO> query = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CollectPolicyDO.class));

    return query.size() > 0 ? query.get(0) : new CollectPolicyDO();
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.CollectPolicyDao#queryCollectPolicy(java.lang.String)
   */
  @Override
  public CollectPolicyDO queryCollectPolicy(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<CollectPolicyDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CollectPolicyDO.class));

    return CollectionUtils.isEmpty(list) ? new CollectPolicyDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.CollectPolicyDao#queryCollectPolicyByName(java.lang.String)
   */
  @Override
  public CollectPolicyDO queryCollectPolicyByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<CollectPolicyDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CollectPolicyDO.class));

    return CollectionUtils.isEmpty(list) ? new CollectPolicyDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.CollectPolicyDao#queryCollectPolicyWithIpBetween(long, long)
   */
  @Override
  public CollectPolicyDO queryCollectPolicyWithIpBetween(long ipStart, long ipEnd) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and ( (:ipStart between ip_start and ip_end) ");
    if (ipEnd > 0) {
      sql.append(" or (:ipEnd between ip_start and ip_end)) ");
    } else {
      sql.append(")");
    }

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("ipStart", ipStart);
    params.put("ipEnd", ipEnd);

    List<CollectPolicyDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CollectPolicyDO.class));
    return CollectionUtils.isEmpty(list) ? new CollectPolicyDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.CollectPolicyDao#saveCollectPolicy(com.machloop.fpc.manager.metadata.data.CollectPolicyDO)
   */
  @Override
  public CollectPolicyDO saveCollectPolicy(CollectPolicyDO collectPolicyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_COLLECT_POLICY);
    sql.append(" (id, name, ip_address, ip_start, ip_end, l7_protocol_id, level, state,");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :ipAddress, :ipStart, :ipEnd, :l7ProtocolId, :level, :state,");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    collectPolicyDO.setId(IdGenerator.generateUUID());
    collectPolicyDO.setState(Constants.BOOL_NO);
    collectPolicyDO.setCreateTime(DateUtils.now());
    collectPolicyDO.setUpdateTime(collectPolicyDO.getCreateTime());
    collectPolicyDO.setDeleted(Constants.BOOL_NO);

    jdbcTemplate.update(sql.toString(), new BeanPropertySqlParameterSource(collectPolicyDO));
    return collectPolicyDO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.CollectPolicyDao#updateCollectPolicy(com.machloop.fpc.manager.metadata.data.CollectPolicyDO)
   */
  @Override
  public int updateCollectPolicy(CollectPolicyDO collectPolicyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_COLLECT_POLICY);
    sql.append(" set name = :name, ip_address = :ipAddress, ip_start = :ipStart, ");
    sql.append(" ip_end = :ipEnd, l7_protocol_id = :l7ProtocolId, level = :level, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    collectPolicyDO.setUpdateTime(DateUtils.now());

    return jdbcTemplate.update(sql.toString(), new BeanPropertySqlParameterSource(collectPolicyDO));
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.CollectPolicyDao#changeCollectPolicyState(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int changeCollectPolicyState(String id, String state, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_COLLECT_POLICY);
    sql.append(" set state = :state, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("state", state);
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("id", id);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.metadata.dao.CollectPolicyDao#deleteCollectPolicy(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCollectPolicy(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_COLLECT_POLICY);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deleted", Constants.BOOL_YES);
    params.put("deleteTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("id", id);

    return jdbcTemplate.update(sql.toString(), params);
  }


  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, order_no, ip_address, ip_start, ip_end, ");
    sql.append(" l7_protocol_id, level, state, ");
    sql.append(" deleted, create_time, update_time, delete_time, operator_id ");
    sql.append(" from ").append(TABLE_COLLECT_POLICY);
    return sql;
  }

}
