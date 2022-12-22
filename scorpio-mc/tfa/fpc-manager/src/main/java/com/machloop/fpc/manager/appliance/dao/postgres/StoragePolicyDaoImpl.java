package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.dao.StoragePolicyDao;
import com.machloop.fpc.manager.appliance.data.StoragePolicyDO;

/**
 * @author liyongjun
 *
 * create at 2019年9月5日, fpc-manager
 */
@Repository
public class StoragePolicyDaoImpl implements StoragePolicyDao {

  private static final String TABLE_APPLIANCE_STORAGE_POLICY = "fpc_appliance_storage_policy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.StoragePolicyDao#queryStoragePolicy()
   */
  @Override
  public StoragePolicyDO queryStoragePolicy() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, compress_action, encrypt_action, encrypt_algorithm, ");
    sql.append(" update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_STORAGE_POLICY);
    sql.append(" order by id ASC ");

    List<StoragePolicyDO> storagePolicyList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(StoragePolicyDO.class));
    return CollectionUtils.isEmpty(storagePolicyList) ? new StoragePolicyDO()
        : storagePolicyList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.StoragePolicyDao#updateStoragePolicy(com.machloop.fpc.manager.appliance.data.StoragePolicyDO)
   */
  @Override
  public int updateStoragePolicy(StoragePolicyDO storagePolicyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_STORAGE_POLICY);
    sql.append(" set compress_action = :compressAction, ");
    sql.append(" encrypt_action = :encryptAction, encrypt_algorithm = :encryptAlgorithm, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    storagePolicyDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(storagePolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

}
