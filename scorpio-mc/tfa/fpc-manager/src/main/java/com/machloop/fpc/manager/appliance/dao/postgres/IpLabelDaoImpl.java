package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.appliance.dao.IpLabelDao;
import com.machloop.fpc.manager.appliance.data.IpLabelDO;

/**
 * @author chenshimiao
 *
 * create at 2022/9/6 10:46 AM,cms
 * @version 1.0
 */
@Repository
public class IpLabelDaoImpl implements IpLabelDao {

  private static final String TABLE_APPLIANCE_IP_LABEL = "fpc_appliance_ip_label";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<IpLabelDO> queryIpLabels(PageRequest page, String name, String category) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name ");
    }
    if (StringUtils.isNotBlank(category)) {
      whereSql.append(" and category = :category ");
    }
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", "%" + name + "%");
    params.put("category", category);

    PageUtils.appendPage(sql, page, IpLabelDO.class);

    List<IpLabelDO> ipLabelList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IpLabelDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_IP_LABEL);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);

    return new PageImpl<>(ipLabelList, page, total);
  }

  @Override
  public List<IpLabelDO> queryIpLabels() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");

    sql.append(whereSql);
    sql.append(" order by name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<IpLabelDO> ipLabelDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IpLabelDO.class));

    return CollectionUtils.isEmpty(ipLabelDOList) ? Lists.newArrayListWithCapacity(0)
        : ipLabelDOList;
  }

  @Override
  public List<IpLabelDO> queryIdAndIp() {
    StringBuilder sql = new StringBuilder();
    sql.append("select name, ip_address from ").append(TABLE_APPLIANCE_IP_LABEL);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");

    sql.append(whereSql);
    sql.append(" order by name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<IpLabelDO> ipLabelDOList = jdbcTemplate.query(sql.toString(), params,
            new BeanPropertyRowMapper<>(IpLabelDO.class));

    return CollectionUtils.isEmpty(ipLabelDOList) ? Lists.newArrayListWithCapacity(0)
            : ipLabelDOList;
  }

  @Override
  public IpLabelDO queryIpLabel(String id) {
    StringBuilder sql = buildSelectStatement();

    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<IpLabelDO> ipLabelList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IpLabelDO.class));

    return CollectionUtils.isEmpty(ipLabelList) ? new IpLabelDO() : ipLabelList.get(0);
  }

  @Override
  public IpLabelDO queryIpLabelByName(String name) {
    StringBuilder sql = buildSelectStatement();

    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<IpLabelDO> ipLabelList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IpLabelDO.class));

    return CollectionUtils.isEmpty(ipLabelList) ? new IpLabelDO() : ipLabelList.get(0);
  }

  @Override
  public List<Map<String, Object>> queryIpLabelByCategory() {
    StringBuilder sql = new StringBuilder();

    sql.append(" select category, count(category) as countCategory ");
    sql.append(" from ").append(TABLE_APPLIANCE_IP_LABEL);
    sql.append(" where deleted = :deleted group by category ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.query(sql.toString(), params, new ColumnMapRowMapper());
  }

  @Override
  public IpLabelDO saveIpLabel(IpLabelDO ipLabelDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_IP_LABEL);
    sql.append(" (id, name, ip_address, category, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :ipAddress, :category, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    if (StringUtils.isBlank(ipLabelDO.getId())) {
      ipLabelDO.setId(IdGenerator.generateUUID());
    }
    ipLabelDO.setCreateTime(DateUtils.now());
    ipLabelDO.setUpdateTime(ipLabelDO.getUpdateTime());
    ipLabelDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(ipLabelDO);
    jdbcTemplate.update(sql.toString(), parameterSource);

    return ipLabelDO;
  }

  @Override
  public int updateIpLabel(IpLabelDO ipLabelDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_IP_LABEL);
    sql.append(" set name = :name, ip_address = :ipAddress, category = :category, ");
    sql.append(" description = :description, update_time = :updateTime, ");
    sql.append(" operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ipLabelDO.setUpdateTime(DateUtils.now());

    SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(ipLabelDO);
    return jdbcTemplate.update(sql.toString(), parameterSource);
  }

  @Override
  public int deleteIpLabel(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_IP_LABEL);
    sql.append(" set deleted = :deleted, delete_time = delete_time, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    IpLabelDO ipLabelDO = new IpLabelDO();
    ipLabelDO.setDeleted(Constants.BOOL_YES);
    ipLabelDO.setDeleteTime(DateUtils.now());
    ipLabelDO.setOperatorId(operatorId);
    ipLabelDO.setId(id);

    SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(ipLabelDO);
    return jdbcTemplate.update(sql.toString(), parameterSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, ip_address, category, description, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_IP_LABEL);
    return sql;
  }
}
