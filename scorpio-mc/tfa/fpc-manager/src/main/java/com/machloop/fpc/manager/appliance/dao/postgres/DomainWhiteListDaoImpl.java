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
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.appliance.dao.DomainWhiteListDao;
import com.machloop.fpc.manager.appliance.data.DomainWhiteListDO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/12/8 11:48 AM,cms
 * @version 1.0
 */
@Repository
public class DomainWhiteListDaoImpl implements DomainWhiteListDao {

  private static final String TABLE_APPLIANCE_DOMAIN_WHITE = "fpc_appliance_domain_whitelist";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public Page<DomainWhiteListDO> queryDomainWhiteList(PageRequest page, String name,
      String domain) {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name ");
      params.put("name", "%" + name + "%");
    }
    if (StringUtils.isNotBlank(domain)) {
      whereSql.append(" and domain like :domain ");
      params.put("domain", "%" + domain + "%");
    }
    sql.append(whereSql);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    PageUtils.appendPage(sql, page, DomainWhiteListDO.class);

    List<DomainWhiteListDO> domainWhiteListDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DomainWhiteListDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(1) from ");
    totalSql.append(TABLE_APPLIANCE_DOMAIN_WHITE);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(domainWhiteListDOList, page, total);
  }

  @Override
  public List<DomainWhiteListDO> queryDomainWhiteList() {
    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");

    sql.append(whereSql);
    sql.append(" order by name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<DomainWhiteListDO> domainWhiteListDOS = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DomainWhiteListDO.class));
    return CollectionUtils.isEmpty(domainWhiteListDOS) ? Lists.newArrayListWithCapacity(0)
        : domainWhiteListDOS;
  }

  @Override
  public DomainWhiteListDO queryDomainWhiteList(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<DomainWhiteListDO> domainWhiteListDOS = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DomainWhiteListDO.class));

    return CollectionUtils.isEmpty(domainWhiteListDOS) ? new DomainWhiteListDO()
        : domainWhiteListDOS.get(0);
  }

  @Override
  public DomainWhiteListDO queryDomainWhiteByDomainWhiteListInCmsId(String domainWhiteListInCmsId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and domain_white_list_in_cms_id = :domainWhiteListInCmsId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("domainWhiteListInCmsId", domainWhiteListInCmsId);

    List<DomainWhiteListDO> domainWhiteListDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DomainWhiteListDO.class));
    return CollectionUtils.isEmpty(domainWhiteListDOList) ? new DomainWhiteListDO()
        : domainWhiteListDOList.get(0);
  }

  @Override
  public List<String> queryDomainWhiteListById(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select domain_white_list_in_cms_id from ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" where deleted = :deleted and domain_white_list_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public List<String> queryDomainWhiteListInCmsId() {
    StringBuilder sql = new StringBuilder();
    sql.append("select domain_white_list_in_cms_id from ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" where deleted = :deleted and domain_white_list_in_cms_id != '' ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public DomainWhiteListDO queryDomainWhiteByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<DomainWhiteListDO> domainWhiteListDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DomainWhiteListDO.class));
    return CollectionUtils.isEmpty(domainWhiteListDOList) ? new DomainWhiteListDO()
        : domainWhiteListDOList.get(0);
  }

  @Override
  public List<String> queryDomainWhiteListName() {
    StringBuilder sql = new StringBuilder();
    sql.append("select name from ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public List<DomainWhiteListDO> queryDomainWhiteListByNameAndDomain(String name, String domain) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name ");
    }
    if (StringUtils.isNotBlank(domain)) {
      whereSql.append(" and domain like :domain ");
    }
    sql.append(whereSql);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", "%" + name + "%");
    params.put("domain", "%" + domain + "%");
    List<DomainWhiteListDO> domainWhiteListDOS = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DomainWhiteListDO.class));

    return domainWhiteListDOS;
  }

  @Override
  public int queryCountDomainWhiteList() {
    StringBuilder sql = new StringBuilder();
    sql.append("select count(1) from ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
  }

  @Override
  public List<DomainWhiteListDO> saveDomainWhite(List<DomainWhiteListDO> domainWhiteListDOList,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" (id, domain_white_list_in_cms_id, name, domain, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :domainWhiteListInCmsId, :name, :domain, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    List<DomainWhiteListDO> domainWhiteListDOLists = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    for (DomainWhiteListDO domainWhiteListDO : domainWhiteListDOList) {
      if (StringUtils.isBlank(domainWhiteListDO.getId())) {
        domainWhiteListDO.setId(IdGenerator.generateUUID());
      }
      if (StringUtils.isBlank(domainWhiteListDO.getDomainWhiteListInCmsId())) {
        domainWhiteListDO.setDomainWhiteListInCmsId("");
      }
      domainWhiteListDO.setCreateTime(DateUtils.now());
      domainWhiteListDO.setUpdateTime(DateUtils.now());
      domainWhiteListDO.setDeleted(Constants.BOOL_NO);
      domainWhiteListDO.setOperatorId(operatorId);
      if (StringUtils.isBlank(domainWhiteListDO.getDescription())) {
        domainWhiteListDO.setDescription("");
      }
      domainWhiteListDOLists.add(domainWhiteListDO);
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(domainWhiteListDOList);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);

    return domainWhiteListDOLists;
  }

  @Override
  public DomainWhiteListDO saveOrRecoverDomainWhite(DomainWhiteListDO domainWhiteListDO,
      String operatorId) {
    DomainWhiteListDO exist = queryDomainWhiteListById(
        domainWhiteListDO.getId() == null ? "" : domainWhiteListDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveDomainWhiteList(domainWhiteListDO, operatorId);
    } else {
      recoverAndUpdateDomainWhiteList(domainWhiteListDO, operatorId);
      return queryDomainWhiteListById(domainWhiteListDO.getId());
    }
  }

  @Override
  public int updateDomainWhiteList(DomainWhiteListDO domainWhiteListDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" set name = :name, domain = :domain, ");
    sql.append(" description = :description, update_time = :updateTime, ");
    sql.append(" operator_id = :operatorId ");
    sql.append(" where id = :id ");

    domainWhiteListDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(domainWhiteListDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public int updateBatchDomainWhiteList(List<DomainWhiteListDO> existDomainWhiteListDO,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" set name = :name, domain = :domain, ");
    sql.append(" description = :description, update_time = :updateTime, ");
    sql.append(" operator_id = :operatorId ");
    sql.append(" where id = :id ");

    for (DomainWhiteListDO domainWhiteListDO : existDomainWhiteListDO) {
      domainWhiteListDO.setUpdateTime(DateUtils.now());
      domainWhiteListDO.setOperatorId(operatorId);
    }
    SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(existDomainWhiteListDO);
    return jdbcTemplate.batchUpdate(sql.toString(), batch)[0];
  }

  @Override
  public int deleteDomainWhiteListAll(boolean onlyLocal, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" set deleted = :deleted, update_time = :updateTime, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_YES);
    params.put("updateTime", DateUtils.now());
    params.put("deleteTime", DateUtils.now());
    params.put("operatorId", operatorId);

    if (onlyLocal) {
      sql.append(" where domain_white_list_in_cms_id = '' ");
    }

    return jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public int deleteDomainWhiteList(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    DomainWhiteListDO domainWhiteListDO = new DomainWhiteListDO();
    domainWhiteListDO.setDeleted(Constants.BOOL_YES);
    domainWhiteListDO.setDeleteTime(DateUtils.now());
    domainWhiteListDO.setOperatorId(operatorId);
    domainWhiteListDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(domainWhiteListDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public int deleteDOmainWHiteLIstByNameAndDomain(String name, String domain, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" set deleted = :deleted, update_time = :updateTime, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");

    sql.append(" where 1 = 1 ");
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_YES);
    params.put("updateTime", DateUtils.now());
    params.put("deleteTime", DateUtils.now());
    params.put("operatorId", operatorId);

    if (StringUtils.isNotBlank(name)) {
      sql.append(" and name like :name ");
      params.put("name", "%" + name + "%");
    }
    if (StringUtils.isNotBlank(domain)) {
      sql.append(" and domain like :domain ");
      params.put("domain", "%" + domain + "%");
    }
    return jdbcTemplate.update(sql.toString(), params);
  }

  private int recoverAndUpdateDomainWhiteList(DomainWhiteListDO domainWhiteListDO,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" set name = :name, domain = :domain, ");
    sql.append(" description = :description, update_time = :updateTime, ");
    sql.append(" operator_id = :operatorId, deleted = :deleted ");
    sql.append(" where id = :id ");

    domainWhiteListDO.setUpdateTime(DateUtils.now());
    domainWhiteListDO.setOperatorId(operatorId);
    domainWhiteListDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(domainWhiteListDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private DomainWhiteListDO saveDomainWhiteList(DomainWhiteListDO domainWhiteListDO,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    sql.append(" (id, domain_white_list_in_cms_id, name, domain, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :domainWhiteListInCmsId, :name, :domain, :description, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    if (StringUtils.isBlank(domainWhiteListDO.getId())) {
      domainWhiteListDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(domainWhiteListDO.getDomainWhiteListInCmsId())) {
      domainWhiteListDO.setDomainWhiteListInCmsId("");
    }
    domainWhiteListDO.setCreateTime(DateUtils.now());
    domainWhiteListDO.setUpdateTime(DateUtils.now());
    domainWhiteListDO.setDeleted(Constants.BOOL_NO);
    domainWhiteListDO.setOperatorId(operatorId);
    if (StringUtils.isBlank(domainWhiteListDO.getDescription())) {
      domainWhiteListDO.setDescription("");
    }

    SqlParameterSource parameterSource = new BeanPropertySqlParameterSource(domainWhiteListDO);
    jdbcTemplate.update(sql.toString(), parameterSource);

    return domainWhiteListDO;
  }

  private DomainWhiteListDO queryDomainWhiteListById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<DomainWhiteListDO> domainWhiteListDOS = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(DomainWhiteListDO.class));

    return CollectionUtils.isEmpty(domainWhiteListDOS) ? new DomainWhiteListDO()
        : domainWhiteListDOS.get(0);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, domain, description, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_DOMAIN_WHITE);
    return sql;
  }
}
