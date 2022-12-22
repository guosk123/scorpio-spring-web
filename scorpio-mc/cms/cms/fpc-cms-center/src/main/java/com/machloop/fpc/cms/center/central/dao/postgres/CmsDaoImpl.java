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
import com.machloop.fpc.cms.center.central.dao.CmsDao;
import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2021年12月15日, fpc-cms-center
 */
@Repository
public class CmsDaoImpl implements CmsDao {

  private static final String TABLE_CMS = "fpccms_central_cms";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#queryCms(com.machloop.fpc.cms.center.central.vo.CmsQueryVO)
   */
  @Override
  public List<CmsDO> queryCms(CmsQueryVO query) {
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

    List<CmsDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CmsDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#queryCmsBySerialNumbers(java.util.List)
   */
  @Override
  public List<CmsDO> queryCmsBySerialNumbers(List<String> serialNumbers) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, ip, serial_number from ");
    sql.append(TABLE_CMS);
    sql.append(" where deleted = :deleted and serial_number in (:serialNumbers) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("serialNumbers", serialNumbers);

    List<CmsDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CmsDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#queryCmsBySuperior(java.lang.String)
   */
  @Override
  public List<CmsDO> queryCmsBySuperior(String superiorCmsSerialNumber) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and superior_cms_serial_number = :superiorCmsSerialNumber ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("superiorCmsSerialNumber", superiorCmsSerialNumber);

    List<CmsDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CmsDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#queryCmsByReportState(java.lang.String)
   */
  @Override
  public List<CmsDO> queryCmsByReportState(String reportState) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where report_state = :reportState ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("reportState", reportState);

    List<CmsDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CmsDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#queryCmsById(java.lang.String)
   */
  @Override
  public CmsDO queryCmsById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<CmsDO> deviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CmsDO.class));
    return CollectionUtils.isEmpty(deviceList) ? new CmsDO() : deviceList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#queryCmsByIpOrName(java.lang.String, java.lang.String)
   */
  @Override
  public CmsDO queryCmsByIpOrName(String ip, String name) {
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

    List<CmsDO> deviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CmsDO.class));
    return CollectionUtils.isEmpty(deviceList) ? new CmsDO() : deviceList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#queryCmsBySerialNumber(java.lang.String)
   */
  @Override
  public CmsDO queryCmsBySerialNumber(String serialNumber) {
    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    sql.append(" where deleted = :deleted and serial_number = :serialNumber ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("serialNumber", serialNumber);

    List<CmsDO> deviceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CmsDO.class));
    return CollectionUtils.isEmpty(deviceList) ? new CmsDO() : deviceList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#saveCms(com.machloop.fpc.cms.center.central.data.CmsDO)
   */
  @Override
  public CmsDO saveCms(CmsDO cmsDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_CMS);
    sql.append(" (id, name, ip, serial_number, version, app_key, app_token, ");
    sql.append(" cms_token, superior_cms_serial_number, description, report_state, ");
    sql.append(" report_action, deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :name, :ip, :serialNumber, :version, :appKey, :appToken, ");
    sql.append(" :cmsToken, :superiorCmsSerialNumber, :description, :reportState, ");
    sql.append(" :reportAction, :deleted, :createTime, :updateTime, :operatorId) ");

    cmsDO.setId(IdGenerator.generateUUID());
    cmsDO.setDeleted(Constants.BOOL_NO);
    cmsDO.setCreateTime(DateUtils.now());
    cmsDO.setUpdateTime(cmsDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(cmsDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return cmsDO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#batchSaveCms(java.util.List)
   */
  @Override
  public int batchSaveCms(List<CmsDO> cmsList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_CMS);
    sql.append(" (id, name, ip, serial_number, version, app_key, app_token, ");
    sql.append(" cms_token, superior_cms_serial_number, description, report_state, ");
    sql.append(" report_action, deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :name, :ip, :serialNumber, :version, :appKey, :appToken, ");
    sql.append(" :cmsToken, :superiorCmsSerialNumber, :description, :reportState, ");
    sql.append(" :reportAction, :deleted, :createTime, :updateTime, :operatorId) ");

    cmsList.forEach(cmsDO -> {
      cmsDO.setId(IdGenerator.generateUUID());
      cmsDO.setDeleted(Constants.BOOL_NO);
      cmsDO.setCreateTime(DateUtils.now());
      cmsDO.setUpdateTime(cmsDO.getCreateTime());
    });

    SqlParameterSource[] batchValues = SqlParameterSourceUtils.createBatch(cmsList);
    return Arrays.stream(jdbcTemplate.batchUpdate(sql.toString(), batchValues)).sum();
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#updateCmsStatus(com.machloop.fpc.cms.center.central.data.CmsDO)
   */
  @Override
  public int updateCmsStatus(CmsDO cmsDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_CMS);
    sql.append(" set name = :name, ip = :ip, version = :version, app_key = :appKey, ");
    sql.append(" app_token = :appToken, superior_cms_serial_number = :superiorCmsSerialNumber, ");
    sql.append(" report_state = :reportState, report_action = :reportAction, ");
    sql.append(" update_time = :updateTime ");
    sql.append(" where serial_number = :serialNumber ");

    cmsDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(cmsDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#updateCmsReportState(java.util.List, java.lang.String)
   */
  @Override
  public int updateCmsReportState(List<String> serialNumbers, String reportState) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_CMS);
    sql.append(" set report_state = :reportState ");
    sql.append(" where serial_number in (:serialNumbers) ");

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("reportState", reportState);
    map.put("serialNumbers", serialNumbers);

    return jdbcTemplate.update(sql.toString(), map);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#deleteCms(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCms(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_CMS);
    sql.append(" set report_state = :reportState, report_action = :reportAction, ");
    sql.append(" deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id");

    CmsDO deviceDO = new CmsDO();
    deviceDO.setId(id);
    deviceDO.setDeleted(Constants.BOOL_YES);
    deviceDO.setDeleteTime(DateUtils.now());
    deviceDO.setReportState(Constants.BOOL_NO);
    deviceDO.setReportAction(FpcCmsConstants.SYNC_ACTION_DELETE);
    deviceDO.setOperatorId(operatorId);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(deviceDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.dao.CmsDao#deleteCmsBySerialNumbers(java.util.List, java.lang.String)
   */
  @Override
  public int deleteCmsBySerialNumbers(List<String> serialNumbers, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_CMS);
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
    sql.append("select id, name, ip, serial_number, version, app_key, app_token, ");
    sql.append(" cms_token, superior_cms_serial_number, description, report_state, ");
    sql.append(" report_action, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_CMS);
    return sql;
  }

}
