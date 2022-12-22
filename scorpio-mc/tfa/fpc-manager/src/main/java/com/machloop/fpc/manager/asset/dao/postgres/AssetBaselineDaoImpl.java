package com.machloop.fpc.manager.asset.dao.postgres;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.asset.dao.AssetBaselineDao;
import com.machloop.fpc.manager.asset.data.AssetBaselineDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月6日, fpc-manager
 */
@Repository
public class AssetBaselineDaoImpl implements AssetBaselineDao {

  private static final String TABLE_APPLIANCE_ASSET_BASELINE = "fpc_appliance_asset_baseline";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetBaselineDao#queryAssetBaselines(java.util.List)
   */
  @Override
  public List<AssetBaselineDO> queryAssetBaselines(List<String> ipList) {

    StringBuilder sql = new StringBuilder();
    sql.append("select id, ip_address, type, baseline, description, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_ASSET_BASELINE);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    if (CollectionUtils.isNotEmpty(ipList)) {
      whereSql.append(" and ip_address in (:ipList)");
      params.put("ipList", ipList);
    }

    sql.append(whereSql);
    List<AssetBaselineDO> assetBaselineList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssetBaselineDO.class));
    return CollectionUtils.isEmpty(assetBaselineList) ? Lists.newArrayListWithCapacity(0)
        : assetBaselineList;
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetBaselineDao#queryAssetBaselineByIp(java.lang.String)
   */
  @Override
  public List<AssetBaselineDO> queryAssetBaselineByIp(String ipAddress) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, ip_address, type, baseline, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_ASSET_BASELINE);

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where deleted = :deleted ");
    whereSql.append(" and ip_address = :ipAddress ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("ipAddress", ipAddress);

    sql.append(whereSql);
    List<AssetBaselineDO> assetBaselineList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AssetBaselineDO.class));
    return CollectionUtils.isEmpty(assetBaselineList) ? Lists.newArrayListWithCapacity(0)
        : assetBaselineList;
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetBaselineDao#saveOrUpdateAssetBaselines(java.util.List, java.lang.String)
   */
  @Override
  public void saveOrUpdateAssetBaselines(List<AssetBaselineDO> assetBaselineDOList,
      String operatorId) {
    // 为了适配新基线覆盖老基线的需求，更改为以下逻辑：使用ip查找基线，如果存在，先删除然后新增；如果不存在，直接新增
    Map<String, List<AssetBaselineDO>> ipBaselinesMap = assetBaselineDOList.stream()
        .collect(Collectors.groupingBy(AssetBaselineDO::getIpAddress));
    for (String ipAddress : ipBaselinesMap.keySet()) {
      List<AssetBaselineDO> exist = queryAssetBaselineByIp(ipAddress);
      if (CollectionUtils.isNotEmpty(exist)) {
        deleteAssetBaseline(ipAddress, operatorId);
      }
      saveAssetBaseline(ipBaselinesMap.get(ipAddress), operatorId);
    }
  }

  /**
   * @see com.machloop.fpc.manager.asset.dao.AssetBaselineDao#deleteAssetBaseline(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteAssetBaseline(String ipAddress, String operatorId) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_ASSET_BASELINE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where ip_address = :ipAddress ");

    AssetBaselineDO assetBaselineDO = new AssetBaselineDO();
    assetBaselineDO.setDeleted(Constants.BOOL_YES);
    assetBaselineDO.setDeleteTime(DateUtils.now());
    assetBaselineDO.setOperatorId(operatorId);
    assetBaselineDO.setIpAddress(ipAddress);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(assetBaselineDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int saveAssetBaseline(List<AssetBaselineDO> assetBaselineDOList, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_ASSET_BASELINE);
    sql.append(" (id, ip_address, type, baseline, description, ");
    sql.append(" deleted, update_time, operator_id) ");
    sql.append(" values (:id, :ipAddress, :type, :baseline, :description, ");
    sql.append(" :deleted, :updateTime, :operatorId) ");

    assetBaselineDOList.forEach(assetBaselineDO -> {
      assetBaselineDO.setId(IdGenerator.generateUUID());
      assetBaselineDO.setUpdateTime(DateUtils.now());
      assetBaselineDO.setDeleted(Constants.BOOL_NO);
      assetBaselineDO.setOperatorId(operatorId);
      if (StringUtils.isBlank(assetBaselineDO.getDescription())) {
        assetBaselineDO.setDescription("");
      }
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(assetBaselineDOList);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }
}
