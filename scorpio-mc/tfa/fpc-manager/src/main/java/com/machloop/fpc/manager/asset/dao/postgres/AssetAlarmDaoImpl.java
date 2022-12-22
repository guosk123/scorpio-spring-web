package com.machloop.fpc.manager.asset.dao.postgres;

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

import com.clickhouse.client.internal.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.asset.dao.AssetAlarmDao;
import com.machloop.fpc.manager.asset.data.AssetAlarmDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月7日, fpc-manager
 */
@Repository
public class AssetAlarmDaoImpl implements AssetAlarmDao {

  private static final String TABLE_APPLIANCE_ASSET_ALARM = "fpc_appliance_asset_alarm";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetAlarmDao#queryAssetAlarms(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String)
   */
  @Override
  public Page<AssetAlarmDO> queryAssetAlarms(Pageable page, String ipAddress, String type) {

    StringBuilder sql = new StringBuilder();
    sql.append("select id, ip_address, type, baseline, current, alarm_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_ASSET_ALARM);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1 = 1 ");
    if (StringUtils.isNotBlank(ipAddress)) {
      whereSql.append(" and ip_address = :ipAddress ");
      params.put("ipAddress", ipAddress);
    }
    if (StringUtils.isNotBlank(type)) {
      whereSql.append(" and type = :type ");
      params.put("type", type);
    }

    sql.append(whereSql);

    PageUtils.appendPage(sql, page, AssetAlarmDO.class);

    List<AssetAlarmDO> assetAlarmList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssetAlarmDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_ASSET_ALARM);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(assetAlarmList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetAlarmDao#saveAssetAlarms(com.machloop.fpc.manager.asset.data.AssetAlarmDO)
   */
  @Override
  public void saveAssetAlarms(AssetAlarmDO assetAlarmDO) {

    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_ASSET_ALARM);
    sql.append(" (id, ip_address, type, baseline, current, alarm_time) ");
    sql.append(" values (:id, :ipAddress, :type, :baseline, :current, :alarmTime) ");

    assetAlarmDO.setId(IdGenerator.generateUUID());
    assetAlarmDO.setAlarmTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(assetAlarmDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetAlarmDao#queryAssetAlarms(java.lang.String, int)
   */
  @Override
  public List<AssetAlarmDO> queryAssetAlarms(String ipAddress, int type) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, ip_address, type, baseline, current, alarm_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_ASSET_ALARM);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1 = 1 ");
    if (StringUtils.isNotBlank(ipAddress)) {
      whereSql.append(" and ip_address = :ipAddress ");
      params.put("ipAddress", ipAddress);
    }
    if (type != 0) {
      whereSql.append(" and type = :type ");
      params.put("type", type);
    }
    sql.append(whereSql);

    List<AssetAlarmDO> assetAlarmList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssetAlarmDO.class));
    return CollectionUtils.isEmpty(assetAlarmList)
        ? Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE)
        : assetAlarmList;
  }
}
