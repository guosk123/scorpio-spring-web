package com.machloop.fpc.npm.analysis.dao.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
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
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.npm.analysis.dao.SuricataRuleDao;
import com.machloop.fpc.npm.analysis.data.SuricataRuleDO;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2022年4月6日, fpc-manager
 */
@Repository
public class SuricataRuleDaoImpl implements SuricataRuleDao {

  private static final String TABLE_ANALYSIS_SURICATA_RULE = "fpc_analysis_suricata_rule";

  private static final String INTERNAL_SOURCE = "0";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#querySuricataRules(com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO)
   */
  @Override
  public Page<SuricataRuleDO> querySuricataRules(Pageable page, SuricataRuleQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    PageUtils.appendPage(sql, page, SuricataRuleDO.class);
    List<SuricataRuleDO> suricataRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_ANALYSIS_SURICATA_RULE);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(suricataRuleList, page, total);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#querySuricataRules(com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO)
   */
  @Override
  public List<SuricataRuleDO> querySuricataRules(SuricataRuleQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    // 过滤
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" order by sid asc ");

    List<SuricataRuleDO> suricataRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleDO.class));

    return CollectionUtils.isEmpty(suricataRuleList) ? Lists.newArrayListWithCapacity(0)
        : suricataRuleList;
  }

  @Override
  public List<Integer> querySuricataRuleSids(SuricataRuleQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();

    sql.append("select sid from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" and source != :defaultSource ");
    params.put("defaultSource", INTERNAL_SOURCE);

    sql.append(" order by sid asc ");

    List<
        Integer> suricataRuleIds = jdbcTemplate.queryForList(sql.toString(), params, Integer.class);

    return CollectionUtils.isEmpty(suricataRuleIds) ? Lists.newArrayListWithCapacity(0)
        : suricataRuleIds;
  }

  @Override
  public List<Integer> querySuricataRuleIds(SuricataRuleQueryVO queryVO) {
    StringBuilder sql = new StringBuilder();

    sql.append("select sid from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(queryVO, whereSql, params);

    sql.append(whereSql);
    sql.append(" and source != :defaultSource ");
    params.put("defaultSource", INTERNAL_SOURCE);

    sql.append(" order by sid asc ");

    List<
        Integer> suricataRuleIds = jdbcTemplate.queryForList(sql.toString(), params, Integer.class);

    return CollectionUtils.isEmpty(suricataRuleIds) ? Lists.newArrayListWithCapacity(0)
        : suricataRuleIds;
  }

  @Override
  public List<SuricataRuleDO> querySuricataRulesBySids(List<Integer> sids) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where source != :source and deleted = :deleted and sid in (:sids) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("source", INTERNAL_SOURCE);
    params.put("sids", CollectionUtils.isEmpty(sids) ? null : sids);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleDO.class));
  }

  @Override
  public Map<Integer, Tuple4<Integer, String, Date, String>> querySuricataRuleTuple4() {
    StringBuilder sql = new StringBuilder();
    sql.append("select sid, rev, MD5(rule) as rule, update_time, state ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" where deleted = :deleted ");
    sql.append(" order by sid ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<SuricataRuleDO> rules = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleDO.class));

    Map<Integer,
        Tuple4<Integer, String, Date, String>> result = rules.stream()
            .collect(Collectors.toMap(item -> item.getSid(), item -> Tuples.of(item.getRev(),
                item.getRule(), item.getUpdateTime(), item.getState())));

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#queryRuleSource()
   */
  @Override
  public List<String> queryRuleSource() {
    StringBuilder sql = new StringBuilder();
    sql.append("select source ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" where deleted = :deleted ");
    sql.append(" group by source ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<Map<String, Object>> statisticsResult = jdbcTemplate.query(sql.toString(), params,
        new ColumnMapRowMapper());

    return statisticsResult.stream().map(item -> MapUtils.getString(item, "source"))
        .collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#statisticsByClasstype()
   */
  @Override
  public Map<String, Integer> statisticsByClasstype() {
    return statisticsSuricataRules("classtype_id");
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#statisticsByMitreTactic()
   */
  @Override
  public Map<String, Integer> statisticsByMitreTactic() {
    return statisticsSuricataRules("mitre_tactic_id");
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#statisticsByMitreTechnique()
   */
  @Override
  public Map<String, Integer> statisticsByMitreTechnique() {
    return statisticsSuricataRules("mitre_technique_id");
  }

  private Map<String, Integer> statisticsSuricataRules(String statistics) {
    StringBuilder sql = new StringBuilder();
    sql.append("select ").append(statistics).append(", count(1) as count ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" where deleted = :deleted ");
    sql.append(" and ").append(statistics).append(" is not null ");
    sql.append(" group by ").append(statistics);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<Map<String, Object>> statisticsResult = jdbcTemplate.query(sql.toString(), params,
        new ColumnMapRowMapper());

    Map<String, Integer> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(statisticsResult)) {
      result = statisticsResult.stream()
          .collect(Collectors.toMap(item -> MapUtils.getString(item, statistics),
              item -> MapUtils.getIntValue(item, "count")));
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#querySuricataRule(int)
   */
  @Override
  public SuricataRuleDO querySuricataRule(int sid) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and sid = :sid ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("sid", sid);

    List<SuricataRuleDO> suricataRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleDO.class));

    return CollectionUtils.isEmpty(suricataRuleList) ? new SuricataRuleDO()
        : suricataRuleList.get(0);
  }

  @Override
  public List<Integer> querySuricataRule(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select sid from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" where deleted = :deleted ");
    sql.append(" and update_time < :beforeTime ");
    sql.append(" order by sid ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<Integer> result = jdbcTemplate.queryForList(sql.toString(), params, Integer.class);

    return CollectionUtils.isNotEmpty(result) ? result : Lists.newArrayListWithCapacity(0);
  }

  @Override
  public List<String> querySuricataRule(List<Integer> sids) {
    StringBuilder sql = new StringBuilder();
    sql.append("select rule from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" where deleted = :deleted and sid in (:sids) ");
    sql.append(" order by sid asc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("sids", sids);

    List<String> rules = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(rules) ? Lists.newArrayListWithCapacity(0) : rules;
  }

  @Override
  public List<SuricataRuleDO> querySuricataRulesByIds(List<String> splitIds) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where source != :source and deleted = :deleted and id in (:ids) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("source", INTERNAL_SOURCE);
    params.put("ids", splitIds);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleDO.class));
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#saveSuricataRule(com.machloop.fpc.npm.analysis.data.SuricataRuleDO)
   */
  @Override
  public SuricataRuleDO saveSuricataRule(SuricataRuleDO suricataRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(
        " (id, suricata_rule_in_cms_id, sid, action, protocol, src_ip, src_port, direction, ");
    sql.append(" dest_ip, dest_port, msg, rev, content, priority, classtype_id, ");
    sql.append(" mitre_tactic_id, mitre_technique_id, cve, cnnvd, signature_severity, ");
    sql.append(" target, threshold, rule, parse_state, state, source, ");
    sql.append(" deleted, create_time, update_time, operator_id) ");
    sql.append(
        " values(:id, :suricataRuleInCmsId, :sid, :action, :protocol, :srcIp, :srcPort, :direction, ");
    sql.append(" :destIp, :destPort, :msg, :rev, :content, :priority, :classtypeId, ");
    sql.append(" :mitreTacticId, :mitreTechniqueId, :cve, :cnnvd, :signatureSeverity, ");
    sql.append(" :target, :threshold, :rule, :parseState, :state, :source, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    if (StringUtils.isBlank(suricataRuleDO.getId())) {
      suricataRuleDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(suricataRuleDO.getSuricataRuleInCmsId())) {
      suricataRuleDO.setSuricataRuleInCmsId("");
    }
    suricataRuleDO.setDeleted(Constants.BOOL_NO);
    suricataRuleDO.setCreateTime(DateUtils.now());
    suricataRuleDO.setUpdateTime(suricataRuleDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(suricataRuleDO);
    jdbcTemplate.update(sql.toString(), paramSource);

    return suricataRuleDO;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#saveSuricataRules(java.util.List)
   */
  @Override
  public int saveSuricataRules(List<SuricataRuleDO> suricataRuleList) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(
        " (id, suricata_rule_in_cms_id, sid, action, protocol, src_ip, src_port, direction, ");
    sql.append(" dest_ip, dest_port, msg, rev, content, priority, classtype_id, ");
    sql.append(" mitre_tactic_id, mitre_technique_id, cve, cnnvd, signature_severity, ");
    sql.append(" target, threshold, rule, parse_state, state, source, ");
    sql.append(" deleted, create_time, update_time, operator_id) ");
    sql.append(
        " values(:id, :suricataRuleInCmsId, :sid, :action, :protocol, :srcIp, :srcPort, :direction, ");
    sql.append(" :destIp, :destPort, :msg, :rev, :content, :priority, :classtypeId, ");
    sql.append(" :mitreTacticId, :mitreTechniqueId, :cve, :cnnvd, :signatureSeverity, ");
    sql.append(" :target, :threshold, :rule, :parseState, :state, :source, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId) ");

    suricataRuleList.forEach(suricataRuleDO -> {
      if (StringUtils.isBlank(suricataRuleDO.getId())) {
        suricataRuleDO.setId(IdGenerator.generateUUID());
      }
      if (StringUtils.isBlank(suricataRuleDO.getSuricataRuleInCmsId())) {
        suricataRuleDO.setSuricataRuleInCmsId("");
      }
      suricataRuleDO.setDeleted(Constants.BOOL_NO);
      suricataRuleDO.setCreateTime(DateUtils.now());
      suricataRuleDO.setUpdateTime(suricataRuleDO.getCreateTime());
    });

    SqlParameterSource[] createBatch = SqlParameterSourceUtils.createBatch(suricataRuleList);
    return Arrays.stream(jdbcTemplate.batchUpdate(sql.toString(), createBatch)).sum();
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#updateSuricataRule(com.machloop.fpc.npm.analysis.data.SuricataRuleDO)
   */
  @Override
  public int updateSuricataRule(SuricataRuleDO suricataRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" set action = :action, protocol = :protocol, src_ip = :srcIp, ");
    sql.append(" src_port = :srcPort, direction = :direction, dest_ip = :destIp, ");
    sql.append(" dest_port = :destPort, msg = :msg, rev = :rev, content = :content, ");
    sql.append(" priority = :priority, classtype_id = :classtypeId, source = :source, ");
    sql.append(" mitre_tactic_id = :mitreTacticId, mitre_technique_id = :mitreTechniqueId, ");
    sql.append(" cve = :cve, cnnvd = :cnnvd, signature_severity = :signatureSeverity, ");
    sql.append(" target = :target, threshold = :threshold, rule = :rule, ");
    sql.append(" parse_state = :parseState, state = :state, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where sid = :sid and deleted = :deleted ");

    suricataRuleDO.setUpdateTime(DateUtils.now());
    suricataRuleDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(suricataRuleDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#updateState(java.util.List, java.lang.String, java.lang.String)
   */
  @Override
  public int updateState(List<Integer> sids, String state, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" set state = :state, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where deleted = :deleted and state = :oldState ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("state", state);
    params.put("oldState",
        StringUtils.equals(state, Constants.BOOL_YES) ? Constants.BOOL_NO : Constants.BOOL_YES);
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (!sids.isEmpty()) {
      sql.append(" and sid in (:sids) ");
      params.put("sids", sids);
    }

    return jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public int updateState(SuricataRuleQueryVO queryVO, String state, String operatorId) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" set state = :newState, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql(queryVO, whereSql, params);
    params.put("newState", state);
    params.put("updateTime", DateUtils.now());
    params.put("operatorId", operatorId);

    sql.append(whereSql);
    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * 批量更新
   * @param suricataRuleDOS
   * @return
   */
  @Override
  public void updateBatchSuricataRule(List<SuricataRuleDO> suricataRuleDOS) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" set action = :action, protocol = :protocol, src_ip = :srcIp, ");
    sql.append(" src_port = :srcPort, direction = :direction, dest_ip = :destIp, ");
    sql.append(" dest_port = :destPort, msg = :msg, rev = :rev, content = :content, ");
    sql.append(" priority = :priority, classtype_id = :classtypeId, source = :source, ");
    sql.append(" mitre_tactic_id = :mitreTacticId, mitre_technique_id = :mitreTechniqueId, ");
    sql.append(" cve = :cve, cnnvd = :cnnvd, signature_severity = :signatureSeverity, ");
    sql.append(" target = :target, threshold = :threshold, rule = :rule, ");
    sql.append(" parse_state = :parseState, state = :state, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where sid = :sid ");

    suricataRuleDOS.forEach(suricataRuleDO -> {
      suricataRuleDO.setUpdateTime(DateUtils.now());
    });
    SqlParameterSource[] batch = SqlParameterSourceUtils.createBatch(suricataRuleDOS);
    jdbcTemplate.batchUpdate(sql.toString(), batch);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleDao#deleteSuricataRule(java.util.List, java.lang.String)
   */
  @Override
  public int deleteSuricataRule(List<Integer> sids, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" set deleted = :deleted, update_time = :updateTime, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where source != :source ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_YES);
    params.put("updateTime", DateUtils.now());
    params.put("deleteTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("source", INTERNAL_SOURCE);

    if (!sids.isEmpty()) {
      sql.append(" and sid in (:sids) ");
      params.put("sids", sids);
    }

    return jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public int deleteSuricataRule(SuricataRuleQueryVO query, String operatorId) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" set deleted = :updateDeleted, update_time = :updateTime, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    whereSql(query, whereSql, params);
    params.put("updateDeleted", Constants.BOOL_YES);
    params.put("updateTime", DateUtils.now());
    params.put("deleteTime", DateUtils.now());
    params.put("operatorId", operatorId);

    // 如果传入的source为空或者为内置，需要特殊处理
    String finalSql = whereSql.toString();
    if (StringUtils.isBlank(query.getSource())) {
      finalSql = whereSql.append("and source != :source ").toString();
      params.put("source", INTERNAL_SOURCE);
    } else if (StringUtils.equals(query.getSource(), INTERNAL_SOURCE)) {
      finalSql = whereSql.toString().replace("source = :source", "source != :source");
      params.put("source", INTERNAL_SOURCE);
    }

    sql.append(finalSql);
    return jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public int deleteSuricataRuleContainsDefault(boolean onlyLocal, Date beforeTime,
      String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" set deleted = :deleted, update_time = :updateTime, ");
    sql.append(" delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where update_time < :beforeTime");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_YES);
    params.put("updateTime", DateUtils.now());
    params.put("deleteTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("beforeTime", beforeTime);

    if (onlyLocal) {
      sql.append(" and suricata_rule_in_cms_id = '' ");
    }

    return jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public int deleteSuricataRuleByIdsTrue(List<Integer> sids) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    sql.append(" where source != :source and sid in (:sids) and deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("source", INTERNAL_SOURCE);
    params.put("sids", sids);
    params.put("deleted", Constants.BOOL_YES);

    return jdbcTemplate.update(sql.toString(), params);
  }

  private void whereSql(SuricataRuleQueryVO queryVO, StringBuilder whereSql,
      Map<String, Object> params) {
    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(queryVO.getSid())) {
      List<Integer> sidList = Lists.newArrayListWithCapacity(0);
      try {
        sidList = CsvUtils.convertCSVToList(queryVO.getSid()).stream()
            .map(sid -> Integer.parseInt(sid)).collect(Collectors.toList());
      } catch (NumberFormatException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "规则ID参数异常");
      }
      whereSql.append(" and sid in (:sid) ");
      params.put("sid", sidList);
    }
    if (StringUtils.isNotBlank(queryVO.getAction())) {
      whereSql.append(" and action = :action ");
      params.put("action", queryVO.getAction());
    }
    if (StringUtils.isNotBlank(queryVO.getProtocol())) {
      whereSql.append(" and protocol = :protocol ");
      params.put("protocol", queryVO.getProtocol());
    }
    if (StringUtils.isNotBlank(queryVO.getSrcIp())) {
      whereSql.append(" and src_ip like :srcIp ");
      params.put("srcIp", "%" + queryVO.getSrcIp() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getSrcPort())) {
      whereSql.append(" and src_port like :srcPort ");
      params.put("srcPort", "%" + queryVO.getSrcPort() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getDirection())) {
      whereSql.append(" and direction = :direction ");
      params.put("direction", queryVO.getDirection());
    }
    if (StringUtils.isNotBlank(queryVO.getDestIp())) {
      whereSql.append(" and dest_ip like :destIp ");
      params.put("destIp", "%" + queryVO.getDestIp() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getDestPort())) {
      whereSql.append(" and dest_port like :destPort ");
      params.put("destPort", "%" + queryVO.getDestPort() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getMsg())) {
      whereSql.append(" and msg like :msg ");
      params.put("msg", "%" + queryVO.getMsg() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getContent())) {
      whereSql.append(" and content like :content ");
      params.put("content", "%" + queryVO.getContent() + "%");
    }
    if (queryVO.getPriority() != null) {
      whereSql.append(" and priority = :priority ");
      params.put("priority", queryVO.getPriority());
    }
    if (StringUtils.isNotBlank(queryVO.getClasstypeIds())) {
      whereSql.append(" and classtype_id in (:classtypeIds) ");
      params.put("classtypeIds", CsvUtils.convertCSVToList(queryVO.getClasstypeIds()));
    }
    if (StringUtils.isNotBlank(queryVO.getMitreTacticIds())) {
      whereSql.append(" and mitre_tactic_id in (:mitreTacticIds) ");

      params.put("mitreTacticIds", CsvUtils.convertCSVToList(queryVO.getMitreTacticIds()));
    }
    if (StringUtils.isNotBlank(queryVO.getMitreTechniqueIds())) {
      whereSql.append(" and mitre_technique_id in (:mitreTechniqueIds) ");
      params.put("mitreTechniqueIds", CsvUtils.convertCSVToList(queryVO.getMitreTechniqueIds()));
    }
    if (StringUtils.isNotBlank(queryVO.getCve())) {
      whereSql.append(" and cve = :cve ");
      params.put("cve", queryVO.getCve());
    }
    if (StringUtils.isNotBlank(queryVO.getCnnvd())) {
      whereSql.append(" and cnnvd = :cnnvd ");
      params.put("cnnvd", queryVO.getCnnvd());
    }
    if (StringUtils.isNotBlank(queryVO.getSignatureSeverity())) {
      whereSql.append(" and signature_severity = :signatureSeverity ");
      params.put("signatureSeverity", queryVO.getSignatureSeverity());
    }
    if (StringUtils.isNotBlank(queryVO.getTarget())) {
      whereSql.append(" and target = :target ");
      params.put("target", queryVO.getTarget());
    }
    if (StringUtils.isNotBlank(queryVO.getState())) {
      whereSql.append(" and state = :state ");
      params.put("state", queryVO.getState());
    }
    if (StringUtils.isNotBlank(queryVO.getSource())) {
      whereSql.append(" and source = :source ");
      params.put("source", queryVO.getSource());
    }
    if (StringUtils.isNotBlank(queryVO.getParseState())) {
      whereSql.append(" and parse_state = :parseState ");
      params.put("parseState", queryVO.getParseState());
    }
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, sid, action, protocol, src_ip, src_port, direction, ");
    sql.append(" dest_ip, dest_port, msg, rev, content, priority, classtype_id, ");
    sql.append(" mitre_tactic_id, mitre_technique_id, cve, cnnvd, signature_severity, ");
    sql.append(" target, threshold, rule, parse_state, parse_log, state, source, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_RULE);
    return sql;
  }

}
