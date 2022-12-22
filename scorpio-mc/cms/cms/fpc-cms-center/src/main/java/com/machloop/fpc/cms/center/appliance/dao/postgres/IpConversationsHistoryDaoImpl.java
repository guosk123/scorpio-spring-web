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

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.IpConversationsHistorySDao;
import com.machloop.fpc.cms.center.appliance.data.IpConversationsHistoryDO;
import com.machloop.fpc.cms.center.appliance.vo.IpConversationsHistoryQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/11
 */
@Repository
public class IpConversationsHistoryDaoImpl implements IpConversationsHistorySDao {
  private static final String TABLE_APPLIANCE_IP_CONVERSATIONS_HISTORY = "fpccms_appliance_ip_conversations_history";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public List<IpConversationsHistoryDO> queryIpConversationsHistories(
      IpConversationsHistoryQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(queryVO.getName())) {
      whereSql.append(" and name like :name ");
      params.put("name", "%" + queryVO.getName() + "%");
    }

    sql.append(whereSql);
    List<IpConversationsHistoryDO> ipConversationsHistoryDOList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(IpConversationsHistoryDO.class));

    return ipConversationsHistoryDOList;
  }

  @Override
  public IpConversationsHistoryDO queryIpConversationsHistory(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);
    List<IpConversationsHistoryDO> ipConversationsHistoryDOList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(IpConversationsHistoryDO.class));
    return CollectionUtils.isEmpty(ipConversationsHistoryDOList) ? new IpConversationsHistoryDO()
        : ipConversationsHistoryDOList.get(0);
  }

  @Override
  public IpConversationsHistoryDO queryIpConversationsHistoryByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);
    List<IpConversationsHistoryDO> ipConversationsHistoryDOList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(IpConversationsHistoryDO.class));
    return CollectionUtils.isEmpty(ipConversationsHistoryDOList) ? new IpConversationsHistoryDO()
        : ipConversationsHistoryDOList.get(0);
  }

  @Override
  public IpConversationsHistoryDO saveIpConversationsHistory(
      IpConversationsHistoryDO ipConversationsHistoryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_IP_CONVERSATIONS_HISTORY);
    sql.append(" (id, name, data, ");
    sql.append(" deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :name, :data, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    ipConversationsHistoryDO.setId(IdGenerator.generateUUID());
    ipConversationsHistoryDO.setCreateTime(DateUtils.now());
    ipConversationsHistoryDO.setUpdateTime(ipConversationsHistoryDO.getCreateTime());
    ipConversationsHistoryDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(ipConversationsHistoryDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return ipConversationsHistoryDO;

  }

  @Override
  public int updateIpConversationsHistory(IpConversationsHistoryDO ipConversationsHistoryDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_IP_CONVERSATIONS_HISTORY);
    sql.append(" set name = :name, data = :data, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ipConversationsHistoryDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(ipConversationsHistoryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public int deleteIpConversationsHistory(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_IP_CONVERSATIONS_HISTORY);
    sql.append(" set deleted = :deleted, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    IpConversationsHistoryDO ipConversationsHistoryDO = new IpConversationsHistoryDO();
    ipConversationsHistoryDO.setDeleted(Constants.BOOL_YES);
    ipConversationsHistoryDO.setDeleteTime(DateUtils.now());
    ipConversationsHistoryDO.setOperatorId(operatorId);
    ipConversationsHistoryDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(ipConversationsHistoryDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();

    sql.append("select id, name, data, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_IP_CONVERSATIONS_HISTORY);

    return sql;
  }
}
