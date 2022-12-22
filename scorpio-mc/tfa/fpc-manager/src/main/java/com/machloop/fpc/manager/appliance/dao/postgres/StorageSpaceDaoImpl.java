package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.dao.StorageSpaceDao;
import com.machloop.fpc.manager.appliance.data.StorageSpaceDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
@Repository
public class StorageSpaceDaoImpl implements StorageSpaceDao {

  private static final String TABLE_APPLIANCE_STORAGE_SPACE = "fpc_appliance_storage_space";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.StorageSpaceDao#queryStorageSpaces()
   */
  @Override
  public List<StorageSpaceDO> queryStorageSpaces() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, space_type, capacity, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_STORAGE_SPACE);
    sql.append(" order by id ASC ");

    List<StorageSpaceDO> storageSpaceList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(StorageSpaceDO.class));
    return CollectionUtils.isEmpty(storageSpaceList) ? Lists.newArrayListWithCapacity(0)
        : storageSpaceList;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.StorageSpaceDao#updateStorageSpace(java.lang.String, long, java.lang.String)
   */
  @Override
  public int updateStorageSpace(String spaceType, long capacity, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_STORAGE_SPACE);
    sql.append(" set capacity = :capacity, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where space_type = :spaceType ");

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("capacity", capacity);
    map.put("updateTime", DateUtils.now());
    map.put("operatorId", operatorId);
    map.put("spaceType", spaceType);

    return jdbcTemplate.update(sql.toString(), map);
  }

}
