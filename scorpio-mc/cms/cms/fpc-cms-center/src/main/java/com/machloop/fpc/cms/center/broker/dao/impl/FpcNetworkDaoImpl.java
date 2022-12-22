package com.machloop.fpc.cms.center.broker.dao.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao;
import com.machloop.fpc.cms.center.broker.data.FpcNetworkDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
@Repository
public class FpcNetworkDaoImpl implements FpcNetworkDao {

  private static final String TABLE_FPC_NETWORK = "fpccms_central_fpc_network";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#queryNetworks()
   */
  @Override
  public List<FpcNetworkDO> queryFpcNetworks(String fpcSerialNumber) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(fpcSerialNumber)) {
      sql.append(" and fpc_serial_number = :fpcSerialNumber ");
      params.put("fpcSerialNumber", fpcSerialNumber);
    }

    List<FpcNetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcNetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? Lists.newArrayListWithCapacity(0) : networkList;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#queryFpcNetworkByCms(java.lang.String)
   */
  @Override
  public List<FpcNetworkDO> queryFpcNetworkByCms(List<String> cmsSerialNumbers) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (CollectionUtils.isNotEmpty(cmsSerialNumbers)) {
      sql.append(" and fpc_serial_number in ");
      sql.append(" (select serial_number from fpccms_central_fpc ");
      sql.append(" where cms_serial_number in (:cmsSerialNumbers)) ");
      params.put("cmsSerialNumbers", cmsSerialNumbers);
    }

    List<FpcNetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcNetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? Lists.newArrayListWithCapacity(0) : networkList;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#queryFpcNetworksByReportState(java.lang.String)
   */
  @Override
  public List<FpcNetworkDO> queryFpcNetworksByReportState(String reportState) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where report_state = :reportState ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("reportState", reportState);

    List<FpcNetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcNetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? Lists.newArrayListWithCapacity(0) : networkList;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#queryFpcNetworkByFpcNetworkId(java.lang.String)
   */
  @Override
  public FpcNetworkDO queryFpcNetworkByFpcNetworkId(String fpcNetworkId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where fpc_network_id = :fpcNetworkId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("fpcNetworkId", fpcNetworkId);

    List<FpcNetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcNetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? new FpcNetworkDO() : networkList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#queryFpcNetworkByFpcNetworkIds(java.util.List)
   */
  @Override
  public List<FpcNetworkDO> queryFpcNetworkByFpcNetworkIds(List<String> fpcNetworkIdList) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where fpc_network_id in (:fpcNetworkIdList) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("fpcNetworkIdList", fpcNetworkIdList);

    List<FpcNetworkDO> networkList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcNetworkDO.class));

    return CollectionUtils.isEmpty(networkList) ? Lists.newArrayListWithCapacity(0) : networkList;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#batchSaveFpcNetworks(java.util.List)
   */
  @Override
  public int batchSaveFpcNetworks(List<FpcNetworkDO> networks) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_FPC_NETWORK);
    sql.append("(id, fpc_network_id, fpc_network_name, bandwidth, fpc_serial_number, ");
    sql.append(" report_state, report_action, deleted, create_time, update_time, operator_id) ");
    sql.append(" values(:id, :fpcNetworkId, :fpcNetworkName, :bandwidth, :fpcSerialNumber, ");
    sql.append(" :reportState, :reportAction,:deleted, :createTime, :updateTime, :operatorId)");

    networks.forEach(network -> {
      network.setId(IdGenerator.generateUUID());
      network.setDeleted(Constants.BOOL_NO);
      network.setCreateTime(DateUtils.now());
      network.setUpdateTime(network.getCreateTime());
    });

    SqlParameterSource[] createBatch = SqlParameterSourceUtils.createBatch(networks);
    return Arrays.stream(jdbcTemplate.batchUpdate(sql.toString(), createBatch)).sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#updateFpcNetwork(com.machloop.fpc.cms.center.broker.data.FpcNetworkDO)
   */
  @Override
  public int updateFpcNetwork(FpcNetworkDO networkDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPC_NETWORK);
    sql.append(" set fpc_network_name = :fpcNetworkName, bandwidth = :bandwidth, ");
    sql.append(" report_state = :reportState, report_action = :reportAction, ");
    sql.append(" update_time = :updateTime ");
    sql.append(" where fpc_serial_number = :fpcSerialNumber and fpc_network_id = :fpcNetworkId ");

    networkDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(networkDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#updateFpcNetworkReportState(java.util.List, java.lang.String)
   */
  @Override
  public int updateFpcNetworkReportState(List<String> networkIds, String reportState) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPC_NETWORK);
    sql.append(" set report_state = :reportState ");
    sql.append(" where fpc_network_id in (:networkIds) ");

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("reportState", reportState);
    map.put("networkIds", networkIds);

    return jdbcTemplate.update(sql.toString(), map);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#deleteFpcNetwork(java.util.List)
   */
  @Override
  public int deleteFpcNetwork(List<String> networkIds) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPC_NETWORK);
    sql.append(" set report_state = :reportState, report_action = :reportAction, ");
    sql.append(" deleted = :deleted, delete_time = :deleteTime ");
    sql.append(" where fpc_network_id in (:networkIds) ");

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("reportState", Constants.BOOL_NO);
    map.put("reportAction", FpcCmsConstants.SYNC_ACTION_DELETE);
    map.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_YES);
    map.put("deleteTime", DateUtils.now());
    map.put("networkIds", networkIds);

    return jdbcTemplate.update(sql.toString(), map);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao#deleteFpcNetwork(java.lang.String)
   */
  @Override
  public int deleteFpcNetwork(String fpcSerialNumber) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPC_NETWORK);
    sql.append(" set report_state = :reportState, report_action = :reportAction, ");
    sql.append(" deleted = :deleted, delete_time = :deleteTime ");
    sql.append(" where fpc_serial_number = :fpcSerialNumber ");

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("reportState", Constants.BOOL_NO);
    map.put("reportAction", FpcCmsConstants.SYNC_ACTION_DELETE);
    map.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_YES);
    map.put("deleteTime", DateUtils.now());
    map.put("fpcSerialNumber", fpcSerialNumber);

    return jdbcTemplate.update(sql.toString(), map);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, fpc_network_id, fpc_network_name, bandwidth, fpc_serial_number, ");
    sql.append(" report_state, report_action, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_FPC_NETWORK);

    return sql;
  }

}
