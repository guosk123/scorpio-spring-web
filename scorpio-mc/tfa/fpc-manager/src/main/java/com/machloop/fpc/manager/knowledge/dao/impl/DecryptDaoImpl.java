package com.machloop.fpc.manager.knowledge.dao.impl;

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
import com.machloop.fpc.manager.knowledge.dao.DecryptDao;
import com.machloop.fpc.manager.knowledge.data.DecryptSettingDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月20日, fpc-manager
 */
@Repository
public class DecryptDaoImpl implements DecryptDao {

  private static final String TABLE_APPLIANCE_SERVICE_DECRYPT = "fpc_appliance_service_decrypt";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.DecryptDao#queryDecryptSettings(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<DecryptSettingDO> queryDecryptSettings(String ipAddress, String port,
      String protocol) {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(ipAddress)) {
      whereSql.append(" and ip_address = :ipAddress ");
      params.put("ipAddress", ipAddress);
    }
    if (StringUtils.isNotBlank(port)) {
      whereSql.append(" and port = :port ");
      params.put("port", port);
    }
    if (StringUtils.isNotBlank(protocol)) {
      whereSql.append(" and protocol = :protocol ");
      params.put("protocol", protocol);
    }

    sql.append(whereSql);
    sql.append(" order by create_time desc");

    List<DecryptSettingDO> decryptSettingList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DecryptSettingDO.class));
    return CollectionUtils.isEmpty(decryptSettingList) ? Lists.newArrayListWithCapacity(0)
        : decryptSettingList;
  }

  @Override
  public List<DecryptSettingDO> queryDecryptSettings() {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    sql.append(whereSql);
    sql.append(" order by create_time desc");

    List<DecryptSettingDO> decryptSettingList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DecryptSettingDO.class));
    return CollectionUtils.isEmpty(decryptSettingList) ? Lists.newArrayListWithCapacity(0)
        : decryptSettingList;
  }
  /**
   * @see com.machloop.fpc.manager.knowledge.dao.DecryptDao#queryDecryptSetting(java.lang.String)
   */
  @Override
  public DecryptSettingDO queryDecryptSetting(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<DecryptSettingDO> decryptSettingList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DecryptSettingDO.class));
    return CollectionUtils.isEmpty(decryptSettingList) ? new DecryptSettingDO()
        : decryptSettingList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.DecryptDao#saveDecryptSetting(com.machloop.fpc.manager.knowledge.data.DecryptSettingDO, byte[])
   */
  @Override
  public DecryptSettingDO saveDecryptSetting(DecryptSettingDO decryptSettingDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SERVICE_DECRYPT);
    sql.append(" (id, ip_address, port, protocol, cert_content, cert_hash, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :ipAddress, :port, :protocol, :certContent, :certHash, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    decryptSettingDO.setId(IdGenerator.generateUUID());
    decryptSettingDO.setCreateTime(DateUtils.now());
    decryptSettingDO.setUpdateTime(decryptSettingDO.getCreateTime());
    decryptSettingDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(decryptSettingDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return decryptSettingDO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.DecryptDao#updateDecryptSetting(com.machloop.fpc.manager.knowledge.data.DecryptSettingDO)
   */
  @Override
  public int updateDecryptSetting(DecryptSettingDO decryptSettingDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SERVICE_DECRYPT);
    sql.append(" set ip_address = :ipAddress, port = :port, protocol = :protocol, ");
    if (decryptSettingDO.getCertContent() != null && decryptSettingDO.getCertHash() != null) {
      sql.append(" cert_content = :certContent, cert_hash = :certHash, ");
    }
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    decryptSettingDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(decryptSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.dao.DecryptDao#deleteDecryptSetting(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteDecryptSetting(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SERVICE_DECRYPT);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    DecryptSettingDO decryptSettingDO = new DecryptSettingDO();
    decryptSettingDO.setDeleted(Constants.BOOL_YES);
    decryptSettingDO.setDeleteTime(DateUtils.now());
    decryptSettingDO.setOperatorId(operatorId);
    decryptSettingDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(decryptSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, ip_address, port, protocol, cert_hash, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SERVICE_DECRYPT);
    return sql;
  }
}
