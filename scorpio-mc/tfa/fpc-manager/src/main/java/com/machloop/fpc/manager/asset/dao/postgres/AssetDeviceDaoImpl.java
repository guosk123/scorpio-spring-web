package com.machloop.fpc.manager.asset.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.fpc.manager.asset.dao.AssetDeviceDao;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月9日, fpc-manager
 */
@Repository
public class AssetDeviceDaoImpl implements AssetDeviceDao {

  private static final String TABLE_APPLIANCE_ASSET_DEVICE = "fpc_appliance_asset_device";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetDeviceDao#queryAssetDevices()
   */
  @Override
  public List<Map<String, Object>> queryAssetDevices() {

    StringBuilder sql = new StringBuilder();
    sql.append("select id, device_name, update_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_ASSET_DEVICE);

    List<Map<String, Object>> assetDeviceList = jdbcTemplate.queryForList(sql.toString(),
        Maps.newHashMapWithExpectedSize(0));

    return CollectionUtils.isEmpty(assetDeviceList) ? Lists.newArrayListWithCapacity(0)
        : assetDeviceList;
  }

}
