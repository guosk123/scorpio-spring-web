package com.machloop.fpc.manager.appliance.dao.postgres;

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

import com.clickhouse.client.internal.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.appliance.dao.PktAnalysisDao;
import com.machloop.fpc.manager.appliance.data.HostGroupDO;
import com.machloop.fpc.manager.appliance.data.PktAnalysisDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年4月18日, fpc-manager
 */
@Repository
public class PktAnalysisDaoImpl implements PktAnalysisDao {

  private static final String TABLE_APPLIANCE_PKT_ANALYSIS_PLUGINS = "fpc_analysis_pktanalysis_plugins";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.PktAnalysisDao#queryPktAnalysises(com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<PktAnalysisDO> queryPktAnalysises(Pageable page) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    PageUtils.appendPage(sql, page, HostGroupDO.class);

    List<PktAnalysisDO> pktAnalysisList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PktAnalysisDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_PKT_ANALYSIS_PLUGINS);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(pktAnalysisList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.PktAnalysisDao#queryPktAnalysises()
   */
  @Override
  public List<PktAnalysisDO> queryPktAnalysises() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PktAnalysisDO.class));
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.PktAnalysisDao#queryPktAnalysis(java.lang.String)
   */
  @Override
  public PktAnalysisDO queryPktAnalysis(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<PktAnalysisDO> pktAnalysisList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(PktAnalysisDO.class));
    return CollectionUtils.isEmpty(pktAnalysisList) ? new PktAnalysisDO() : pktAnalysisList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.PktAnalysisDao#savePktAnalysis(com.machloop.fpc.manager.appliance.data.PktAnalysisDO)
   */
  @Override
  public int savePktAnalysis(PktAnalysisDO pktAnalysisDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_PKT_ANALYSIS_PLUGINS);
    sql.append(" (id, protocol, file_name, description, operator_id, ");
    sql.append(" deleted, create_time, update_time) ");
    sql.append(" values(:id, :protocol, :fileName, :description, :operatorId, ");
    sql.append(" :deleted, :createTime, :updateTime) ");

    if (StringUtils.isBlank(pktAnalysisDO.getId())) {
      pktAnalysisDO.setId(IdGenerator.generateUUID());
    }
    pktAnalysisDO.setCreateTime(DateUtils.now());
    pktAnalysisDO.setUpdateTime(pktAnalysisDO.getCreateTime());
    pktAnalysisDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(pktAnalysisDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.PktAnalysisDao#deletePktAnalysis(java.lang.String, java.lang.String)
   */
  @Override
  public int deletePktAnalysis(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_PKT_ANALYSIS_PLUGINS);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    PktAnalysisDO pktAnalysisDO = new PktAnalysisDO();
    pktAnalysisDO.setDeleted(Constants.BOOL_YES);
    pktAnalysisDO.setDeleteTime(DateUtils.now());
    pktAnalysisDO.setOperatorId(operatorId);
    pktAnalysisDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(pktAnalysisDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, protocol, file_name, parse_status, parse_log, ");
    sql.append(" description, operator_id, create_time, update_time ");
    sql.append(" from ").append(TABLE_APPLIANCE_PKT_ANALYSIS_PLUGINS);
    return sql;
  }

}
