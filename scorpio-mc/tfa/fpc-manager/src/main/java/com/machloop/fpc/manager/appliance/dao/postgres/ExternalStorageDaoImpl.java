package com.machloop.fpc.manager.appliance.dao.postgres;

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
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.appliance.dao.ExternalStorageDao;
import com.machloop.fpc.manager.appliance.data.ExternalStorageDO;

/**
 * @author guosk
 *
 * create at 2020年11月13日, fpc-manager
 */
@Repository
public class ExternalStorageDaoImpl implements ExternalStorageDao {

  private static final String TABLE_APPLIANCE_EXTERNAL_STORAGE = "fpc_appliance_external_storage";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.ExternalStorageDao#queryExternalStorages(java.lang.String, java.lang.String)
   */
  @Override
  public List<ExternalStorageDO> queryExternalStorages(String usage, String type) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(usage)) {
      sql.append(" and usage = :usage ");
      params.put("usage", usage);
    }
    if (StringUtils.isNotBlank(type)) {
      sql.append(" and type = :type ");
      params.put("type", type);
    }

    List<ExternalStorageDO> externalStorageList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalStorageDO.class));

    return CollectionUtils.isEmpty(externalStorageList) ? Lists.newArrayListWithCapacity(0)
        : externalStorageList;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.ExternalStorageDao#queryExternalStorage(java.lang.String)
   */
  @Override
  public ExternalStorageDO queryExternalStorage(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<ExternalStorageDO> externalStorageList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalStorageDO.class));

    return CollectionUtils.isEmpty(externalStorageList) ? new ExternalStorageDO()
        : externalStorageList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.ExternalStorageDao#queryExternalStorageByName(java.lang.String)
   */
  @Override
  public ExternalStorageDO queryExternalStorageByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<ExternalStorageDO> externalStorageList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ExternalStorageDO.class));

    return CollectionUtils.isEmpty(externalStorageList) ? new ExternalStorageDO()
        : externalStorageList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.ExternalStorageDao#saveExternalStorage(com.machloop.fpc.manager.appliance.data.ExternalStorageDO)
   */
  @Override
  public ExternalStorageDO saveExternalStorage(ExternalStorageDO externalStorageDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_EXTERNAL_STORAGE);
    sql.append(" (id, name, state, usage, type, ip_address, port, username, ");
    sql.append(" password, directory, capacity, ");
    sql.append(" deleted, create_time, update_time, operator_id)");
    sql.append(" values (:id, :name, :state, :usage, :type, :ipAddress, :port, :username, ");
    sql.append(" :password, :directory, :capacity, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    externalStorageDO.setId(IdGenerator.generateUUID());
    externalStorageDO.setDeleted(Constants.BOOL_NO);
    externalStorageDO.setCreateTime(DateUtils.now());
    externalStorageDO.setUpdateTime(externalStorageDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(externalStorageDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return externalStorageDO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.ExternalStorageDao#updateExternalStorage(com.machloop.fpc.manager.appliance.data.ExternalStorageDO)
   */
  @Override
  public int updateExternalStorage(ExternalStorageDO externalStorageDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_EXTERNAL_STORAGE);
    sql.append(" set name = :name, state = :state, usage = :usage, type = :type, ");
    sql.append(" ip_address = :ipAddress, port = :port, username = :username, ");
    sql.append(" password = :password, directory = :directory, capacity = :capacity, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    externalStorageDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(externalStorageDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.ExternalStorageDao#deleteExternalStorage(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteExternalStorage(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_EXTERNAL_STORAGE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ExternalStorageDO externalStorageDO = new ExternalStorageDO();
    externalStorageDO.setId(id);
    externalStorageDO.setDeleted(Constants.BOOL_YES);
    externalStorageDO.setDeleteTime(DateUtils.now());
    externalStorageDO.setOperatorId(operatorId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(externalStorageDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, state, usage, type, ip_address, port, ");
    sql.append(" username, password, directory, capacity, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_EXTERNAL_STORAGE);

    return sql;
  }

}
