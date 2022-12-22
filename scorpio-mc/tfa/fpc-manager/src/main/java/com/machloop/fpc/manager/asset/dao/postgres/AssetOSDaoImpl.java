package com.machloop.fpc.manager.asset.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.manager.asset.dao.AssetOSDao;
import com.machloop.fpc.manager.asset.data.OSNameDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月9日, fpc-manager
 */
@Repository
public class AssetOSDaoImpl implements AssetOSDao {

  private static final String TABLE_APPLIANCE_ASSET_OS = "fpc_appliance_asset_os";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetOSDao#queryAssetOS(java.lang.String)
   */
  @Override
  public List<OSNameDO> queryAssetOS(String id) {

    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append("select id, os, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_ASSET_OS);
    if (StringUtils.isNotBlank(id)) {
      sql.append(" where id = :id");
      params.put("id", Integer.parseInt(id));
    }

    List<OSNameDO> assetOSList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(OSNameDO.class));

    return CollectionUtils.isEmpty(assetOSList) ? Lists.newArrayListWithCapacity(0) : assetOSList;
  }

}
