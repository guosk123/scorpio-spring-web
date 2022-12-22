package com.machloop.fpc.npm.analysis.dao.impl;

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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao;
import com.machloop.fpc.npm.analysis.data.AbnormalEventRuleDO;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月16日, fpc-manager
 */
@Repository
public class AbnormalEventRuleDaoImpl implements AbnormalEventRuleDao {

  private static final String TABLE_ANALYSIS_ABNORMAL_EVENT_RULE = "fpc_analysis_abnormal_event_rule";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#queryAbnormalEventRules(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleQueryVO)
   */
  @Override
  public Page<AbnormalEventRuleDO> queryAbnormalEventRules(Pageable page,
      AbnormalEventRuleQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql.append(" where 1 = 1 ");
    if (queryVO.getType() != null) {
      whereSql.append(" and type = :type ");
      params.put("type", queryVO.getType());
    }
    if (StringUtils.isNotBlank(queryVO.getContent())) {
      whereSql.append(" and content like :content ");
      params.put("content", "%" + queryVO.getContent() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getSource())) {
      whereSql.append(" and source = :source ");
      params.put("source", queryVO.getSource());
    }
    if (StringUtils.isNotBlank(queryVO.getStatus())) {
      whereSql.append(" and status = :status ");
      params.put("status", queryVO.getStatus());
    }
    sql.append(whereSql);

    PageUtils.appendPage(sql, page, AbnormalEventRuleDO.class);

    List<AbnormalEventRuleDO> abnormalEventRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AbnormalEventRuleDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_ANALYSIS_ABNORMAL_EVENT_RULE);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(abnormalEventRuleList, page, total);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#queryAbnormalEventRules(java.lang.String)
   */
  @Override
  public List<AbnormalEventRuleDO> queryAbnormalEventRules(String source) {
    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(source)) {
      sql.append(" where source = :source ");
      params.put("source", source);
    }

    sql.append(" order by timestamp desc, type asc");

    List<AbnormalEventRuleDO> abnormalEventRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AbnormalEventRuleDO.class));
    return CollectionUtils.isEmpty(abnormalEventRuleList) ? Lists.newArrayListWithCapacity(0)
        : abnormalEventRuleList;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#queryAbnormalEventRule(java.lang.String)
   */
  @Override
  public AbnormalEventRuleDO queryAbnormalEventRule(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<AbnormalEventRuleDO> abnormalEventRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AbnormalEventRuleDO.class));
    return CollectionUtils.isEmpty(abnormalEventRuleList) ? new AbnormalEventRuleDO()
        : abnormalEventRuleList.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#saveAbnormalEventRule(com.machloop.fpc.npm.analysis.data.AbnormalEventRuleDO)
   */
  @Override
  public AbnormalEventRuleDO saveAbnormalEventRule(AbnormalEventRuleDO abnormalEventRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_ABNORMAL_EVENT_RULE);
    sql.append(" (id, type, content, source, status, ");
    sql.append(" description, operator_id, timestamp) ");
    sql.append(" values (:id, :type, :content, :source, :status, ");
    sql.append(" :description, :operatorId, :timestamp)");

    abnormalEventRuleDO.setId(IdGenerator.generateUUID());
    abnormalEventRuleDO.setTimestamp(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(abnormalEventRuleDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return abnormalEventRuleDO;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#saveAbnormalEventRules(java.util.List)
   */
  @Override
  public int saveAbnormalEventRules(List<AbnormalEventRuleDO> abnormalEventRuleList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_ABNORMAL_EVENT_RULE);
    sql.append(" (id, type, content, source, status, ");
    sql.append(" description, operator_id, timestamp) ");
    sql.append(" values (:id, :type, :content, :source, :status, ");
    sql.append(" :description, :operatorId, :timestamp)");

    for (AbnormalEventRuleDO abnormalEventRuleDO : abnormalEventRuleList) {
      abnormalEventRuleDO.setId(IdGenerator.generateUUID());
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(abnormalEventRuleList);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#updateAbnormalEventRule(com.machloop.fpc.npm.analysis.data.AbnormalEventRuleDO)
   */
  @Override
  public int updateAbnormalEventRule(AbnormalEventRuleDO abnormalEventRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_ABNORMAL_EVENT_RULE);
    sql.append(" set type = :type, content = :content, status = :status, ");
    sql.append(" timestamp = :timestamp, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    abnormalEventRuleDO.setTimestamp(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(abnormalEventRuleDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#updateStatus(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int updateStatus(String id, String status, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_ABNORMAL_EVENT_RULE);
    sql.append(" set status = :status, ");
    sql.append(" timestamp = :timestamp, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("status", status);
    params.put("timestamp", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("id", id);

    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#deleteAbnormalEventRule(java.lang.String)
   */
  @Override
  public int deleteAbnormalEventRule(String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_ANALYSIS_ABNORMAL_EVENT_RULE);
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);
    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.AbnormalEventRuleDao#deleteAbnormalEventRules(java.lang.String)
   */
  @Override
  public int deleteAbnormalEventRules(String source) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_ANALYSIS_ABNORMAL_EVENT_RULE);
    sql.append(" where source = :source ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("source", source);
    return jdbcTemplate.update(sql.toString(), params);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, type, content, source, status, ");
    sql.append(" description, operator_id, timestamp ");
    sql.append(" from ").append(TABLE_ANALYSIS_ABNORMAL_EVENT_RULE);
    return sql;
  }

}
