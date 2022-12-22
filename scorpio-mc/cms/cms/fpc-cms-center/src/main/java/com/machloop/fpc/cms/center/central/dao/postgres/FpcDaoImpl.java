package com.machloop.fpc.cms.center.central.dao.postgres;

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
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.data.FpcDO;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Repository
public class FpcDaoImpl implements FpcDao {

  private static final String TABLE_FPC = "fpccms_central_fpc";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#queryFpcs(com.machloop.fpc.cms.center.central.vo.FpcQueryVO)
   */
  @Override
  public List<FpcDO> queryFpcs(FpcQueryVO query) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    if (StringUtils.isNotBlank(query.getIp())) {
      sql.append(" and ip like :ip ");
      params.put("ip", "%" + query.getIp() + "%");
    }
    if (StringUtils.isNotBlank(query.getName())) {
      sql.append(" and name like :name ");
      params.put("name", "%" + query.getName() + "%");
    }
    sql.append(" order by create_time ASC ");

    List<FpcDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#queryFpcsBySerialNumbers(java.util.List)
   */
  @Override
  public List<FpcDO> queryFpcsBySerialNumbers(List<String> serialNumbers) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, ip, serial_number from ");
    sql.append(TABLE_FPC);
    sql.append(" where deleted = :deleted and serial_number in (:serialNumbers) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("serialNumbers", serialNumbers);

    List<FpcDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#queryFpcByCms(java.util.List)
   */
  @Override
  public List<FpcDO> queryFpcByCms(List<String> cmsSerialNumbers) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (CollectionUtils.isNotEmpty(cmsSerialNumbers)) {
      sql.append(" and cms_serial_number in (:cmsSerialNumbers) ");
      params.put("cmsSerialNumbers", cmsSerialNumbers);
    }

    List<FpcDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#queryFpcByReportState(java.lang.String)
   */
  @Override
  public List<FpcDO> queryFpcByReportState(String reportState) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where report_state = :reportState ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("reportState", reportState);

    List<FpcDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#queryFpcById(java.lang.String)
   */
  @Override
  public FpcDO queryFpcById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<FpcDO> deviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcDO.class));
    return CollectionUtils.isEmpty(deviceList) ? new FpcDO() : deviceList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#queryFpcByIpOrName(java.lang.String, java.lang.String)
   */
  @Override
  public FpcDO queryFpcByIpOrName(String ip, String name) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(ip)) {
      sql.append(" and ip = :ip ");
      params.put("ip", ip);
    }
    if (StringUtils.isNotBlank(name)) {
      sql.append(" and name = :name ");
      params.put("name", name);
    }

    List<FpcDO> deviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcDO.class));
    return CollectionUtils.isEmpty(deviceList) ? new FpcDO() : deviceList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#queryFpcBySerialNumber(java.lang.String)
   */
  @Override
  public FpcDO queryFpcBySerialNumber(String serialNumber) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted and serial_number = :serialNumber ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("serialNumber", serialNumber);

    List<FpcDO> deviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcDO.class));
    return CollectionUtils.isEmpty(deviceList) ? new FpcDO() : deviceList.get(0);
  }

  @Override
  public List<FpcDO> queryOnlineFpcs() {

    StringBuilder sql = buildSelectStatement();
    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    sql.append(" and report_state = :reportState ");
    params.put("reportState", Constants.BOOL_YES);

    List<FpcDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FpcDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }
  
  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#saveFpc(com.machloop.fpc.cms.center.central.data.FpcDO)
   */
  @Override
  public FpcDO saveFpc(FpcDO fpcDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_FPC);
    sql.append(" (id, name, ip, serial_number, version, type, app_key, ");
    sql.append(" app_token, cms_token, cms_serial_number, report_state, report_action, ");
    sql.append(" description, deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :name, :ip, :serialNumber, :version, :type, :appKey, ");
    sql.append(" :appToken, :cmsToken, :cmsSerialNumber, :reportState, :reportAction, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId) ");

    fpcDO.setId(IdGenerator.generateUUID());
    fpcDO.setDeleted(Constants.BOOL_NO);
    fpcDO.setCreateTime(DateUtils.now());
    fpcDO.setUpdateTime(fpcDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(fpcDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return fpcDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#batchSaveFpcs(java.util.List)
   */
  @Override
  public int batchSaveFpcs(List<FpcDO> fpcs) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_FPC);
    sql.append(" (id, name, ip, serial_number, version, type, app_key, ");
    sql.append(" app_token, cms_token, cms_serial_number, report_state, report_action, ");
    sql.append(" description, deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :name, :ip, :serialNumber, :version, :type, :appKey, ");
    sql.append(" :appToken, :cmsToken, :cmsSerialNumber, :reportState, :reportAction, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId) ");

    fpcs.forEach(fpcDO -> {
      fpcDO.setId(IdGenerator.generateUUID());
      fpcDO.setDeleted(Constants.BOOL_NO);
      fpcDO.setCreateTime(DateUtils.now());
      fpcDO.setUpdateTime(fpcDO.getCreateTime());
    });

    SqlParameterSource[] batchValues = SqlParameterSourceUtils.createBatch(fpcs);
    return Arrays.stream(jdbcTemplate.batchUpdate(sql.toString(), batchValues)).sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#updateFpcStatus(com.machloop.fpc.cms.center.central.data.FpcDO)
   */
  @Override
  public int updateFpcStatus(FpcDO fpcDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPC);
    sql.append(" set name = :name, ip = :ip, version = :version, app_key = :appKey, ");
    sql.append(" app_token = :appToken, cms_serial_number = :cmsSerialNumber, ");
    sql.append(" report_state = :reportState, report_action = :reportAction, ");
    sql.append(" update_time = :updateTime ");
    sql.append(" where serial_number = :serialNumber ");

    fpcDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(fpcDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#updateFpcReportState(java.util.List, java.lang.String)
   */
  @Override
  public int updateFpcReportState(List<String> serialNumbers, String reportState) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPC);
    sql.append(" set report_state = :reportState ");
    sql.append(" where serial_number in (:serialNumbers) ");

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("reportState", reportState);
    map.put("serialNumbers", serialNumbers);

    return jdbcTemplate.update(sql.toString(), map);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#deleteFpc(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteFpc(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPC);
    sql.append(" set report_state = :reportState, report_action = :reportAction, ");
    sql.append(" deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id");

    FpcDO fpcDO = new FpcDO();
    fpcDO.setId(id);
    fpcDO.setDeleted(Constants.BOOL_YES);
    fpcDO.setDeleteTime(DateUtils.now());
    fpcDO.setReportState(Constants.BOOL_NO);
    fpcDO.setReportAction(FpcCmsConstants.SYNC_ACTION_DELETE);
    fpcDO.setOperatorId(operatorId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(fpcDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.FpcDao#deleteFpcBySerialNumbers(java.util.List, java.lang.String)
   */
  @Override
  public int deleteFpcBySerialNumbers(List<String> serialNumbers, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPC);
    sql.append(" set report_state = :reportState, report_action = :reportAction, ");
    sql.append(" deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where serial_number in (:serialNumbers) ");

    Map<String, Object> paramMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    paramMap.put("reportState", Constants.BOOL_NO);
    paramMap.put("reportAction", FpcCmsConstants.SYNC_ACTION_DELETE);
    paramMap.put("deleted", Constants.BOOL_YES);
    paramMap.put("deleteTime", DateUtils.now());
    paramMap.put("operatorId", operatorId);
    paramMap.put("serialNumbers", serialNumbers);

    return jdbcTemplate.update(sql.toString(), paramMap);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, ip, serial_number, version, type, app_key, app_token, ");
    sql.append(" cms_token, cms_serial_number, description, report_state, report_action, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_FPC);
    return sql;
  }

}
