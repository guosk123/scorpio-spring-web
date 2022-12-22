package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.Date;
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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.appliance.dao.IngestPolicyDao;
import com.machloop.fpc.manager.appliance.data.IngestPolicyDO;

/**
 * 
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@Repository
public class IngestPolicyDaoImpl implements IngestPolicyDao {

  private static final String TABLE_APPLIANCE_INGEST_POLICY = "fpc_appliance_ingest_policy";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#queryIngestPolicys(com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<IngestPolicyDO> queryIngestPolicys(Pageable page) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    PageUtils.appendPage(sql, page, IngestPolicyDO.class);

    List<IngestPolicyDO> ingestPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IngestPolicyDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_INGEST_POLICY);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(ingestPolicyList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#queryIngestPolicys()
   */
  @Override
  public List<IngestPolicyDO> queryIngestPolicys() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" order by create_time asc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<IngestPolicyDO> ingestPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IngestPolicyDO.class));

    return CollectionUtils.isEmpty(ingestPolicyList) ? Lists.newArrayListWithCapacity(0)
        : ingestPolicyList;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#queryIngestPolicy(java.lang.String)
   */
  @Override
  public IngestPolicyDO queryIngestPolicy(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<IngestPolicyDO> ingestPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IngestPolicyDO.class));
    return CollectionUtils.isEmpty(ingestPolicyList) ? new IngestPolicyDO()
        : ingestPolicyList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#queryIngestPolicyByName(java.lang.String)
   */
  @Override
  public IngestPolicyDO queryIngestPolicyByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<IngestPolicyDO> ingestPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IngestPolicyDO.class));
    return CollectionUtils.isEmpty(ingestPolicyList) ? new IngestPolicyDO()
        : ingestPolicyList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#queryIngestPolicys(java.util.Date)
   */
  @Override
  public List<String> queryAssignIngestPolicys(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select ingest_policy_in_cms_id from ").append(TABLE_APPLIANCE_INGEST_POLICY);
    // id=1表示默认规则，默认规则不会上报给cms，所以排除该项
    sql.append(" where deleted = :deleted and ingest_policy_in_cms_id != '' and id != '1' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#queryIngestPolicyIds(java.util.Date, boolean)
   */
  @Override
  public List<String> queryIngestPolicyIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_INGEST_POLICY);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and ingest_policy_in_cms_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#queryIngestPolicyByCmsIngestPolicyId(java.lang.String)
   */
  @Override
  public IngestPolicyDO queryIngestPolicyByCmsIngestPolicyId(String cmsIngestPolicyId) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and ingest_policy_in_cms_id = :cmsIngestPolicyId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("cmsIngestPolicyId", cmsIngestPolicyId);

    List<IngestPolicyDO> ingestPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IngestPolicyDO.class));

    return CollectionUtils.isEmpty(ingestPolicyList) ? new IngestPolicyDO()
        : ingestPolicyList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#saveIngestPolicy(com.machloop.fpc.manager.appliance.data.IngestPolicyDO)
   */
  @Override
  public IngestPolicyDO saveOrRecoverIngestPolicy(IngestPolicyDO ingestPolicyDO) {
    IngestPolicyDO exist = queryIngestPolicyById(
        ingestPolicyDO.getId() == null ? "" : ingestPolicyDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveIngestPolicy(ingestPolicyDO);
    } else {
      recoverAndUpdateIngestPolicy(ingestPolicyDO);
      return queryIngestPolicyById(ingestPolicyDO.getId());
    }
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#updateIngestPolicy(com.machloop.fpc.manager.appliance.data.IngestPolicyDO)
   */
  @Override
  public int updateIngestPolicy(IngestPolicyDO ingestPolicyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_INGEST_POLICY);
    sql.append(" set name = :name, default_action = :defaultAction, ");
    sql.append(" except_bpf = :exceptBpf, except_tuple = :exceptTuple, ");
    sql.append(" deduplication = :deduplication, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ingestPolicyDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(ingestPolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.IngestPolicyDao#deleteIngestPolicy(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteIngestPolicy(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_INGEST_POLICY);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    IngestPolicyDO ingestPolicyDO = new IngestPolicyDO();
    ingestPolicyDO.setDeleted(Constants.BOOL_YES);
    ingestPolicyDO.setDeleteTime(DateUtils.now());
    ingestPolicyDO.setOperatorId(operatorId);
    ingestPolicyDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(ingestPolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private IngestPolicyDO queryIngestPolicyById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<IngestPolicyDO> ingestPolicyList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(IngestPolicyDO.class));

    return CollectionUtils.isEmpty(ingestPolicyList) ? new IngestPolicyDO()
        : ingestPolicyList.get(0);
  }

  /**
   * 为了避免主键冲突的情况，此方法会将deleted=1的数据更新为deleted=0，并对数据做相应的更新
   */
  private int recoverAndUpdateIngestPolicy(IngestPolicyDO ingestPolicyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_INGEST_POLICY);
    sql.append(" set name = :name, default_action = :defaultAction, ");
    sql.append(" except_bpf = :exceptBpf, except_tuple = :exceptTuple, ");
    sql.append(" deduplication = :deduplication, description = :description, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId, ");
    sql.append(" deleted = :deleted ");
    sql.append(" where id = :id ");

    ingestPolicyDO.setUpdateTime(DateUtils.now());
    ingestPolicyDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(ingestPolicyDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private IngestPolicyDO saveIngestPolicy(IngestPolicyDO ingestPolicyDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_INGEST_POLICY);
    sql.append(" (id, name, default_action, except_bpf, except_tuple, ");
    sql.append(" deduplication, ingest_policy_in_cms_id, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :defaultAction, :exceptBpf, :exceptTuple, ");
    sql.append(" :deduplication, :ingestPolicyInCmsId, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(ingestPolicyDO.getId())) {
      ingestPolicyDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(ingestPolicyDO.getIngestPolicyInCmsId())) {
      ingestPolicyDO.setIngestPolicyInCmsId("");
    }
    ingestPolicyDO.setCreateTime(DateUtils.now());
    ingestPolicyDO.setUpdateTime(ingestPolicyDO.getCreateTime());
    ingestPolicyDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(ingestPolicyDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return ingestPolicyDO;
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, default_action, except_bpf, except_tuple, ");
    sql.append(" deduplication, ingest_policy_in_cms_id, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_INGEST_POLICY);

    return sql;
  }

}
