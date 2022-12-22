package com.machloop.fpc.manager.analysis.dao.postgres;

import java.util.Collection;
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

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao;
import com.machloop.fpc.manager.analysis.data.ThreatIntelligenceDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月16日, fpc-manager
 */
/**
 * @author mazhiyuan
 *
 * create at 2020年6月16日, fpc-manager
 */
/**
 * @author mazhiyuan
 *
 * create at 2020年6月16日, fpc-manager
 */
@Repository
public class ThreatIntelligenceDaoImpl implements ThreatIntelligenceDao {

  private static final String TABLE_ANALYSIS_THREAT_INTELLIGENCE = "fpc_analysis_threat_intelligence";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao#queryIntelligences(com.machloop.alpha.common.base.page.PageRequest, java.lang.String, java.lang.String)
   */
  @Override
  public Page<ThreatIntelligenceDO> queryIntelligences(Pageable page, String type, String content,
      Collection<String> threatCategory) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where 1 = 1 ");
    if (CollectionUtils.isNotEmpty(threatCategory)) {
      whereSql.append(" and threat_category in (:threatCategory) ");
    }
    if (StringUtils.isNotBlank(type)) {
      whereSql.append(" and type = :type ");
    }
    if (StringUtils.isNotBlank(content)) {
      whereSql.append(" and content like :content ");
    }

    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("threatCategory", threatCategory);
    params.put("type", type);
    params.put("content", "%" + content + "%");

    PageUtils.appendPage(sql, page, ThreatIntelligenceDO.class);

    List<ThreatIntelligenceDO> threatIntelligenceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ThreatIntelligenceDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_ANALYSIS_THREAT_INTELLIGENCE);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(threatIntelligenceList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao#queryIntelligences(java.lang.String, java.lang.String)
   */
  @Override
  public List<ThreatIntelligenceDO> queryIntelligences(String type, String content,
      Collection<String> threatCategory) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where 1 = 1 ");
    if (CollectionUtils.isNotEmpty(threatCategory)) {
      whereSql.append(" and threat_category in (:threatCategory) ");
    }
    if (StringUtils.isNotBlank(type)) {
      whereSql.append(" and type = :type ");
    }
    if (StringUtils.isNotBlank(content)) {
      whereSql.append(" and content like :content ");
    }

    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("threatCategory", threatCategory);
    params.put("type", type);
    params.put("content", "%" + content + "%");

    List<ThreatIntelligenceDO> threatIntelligenceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ThreatIntelligenceDO.class));
    return threatIntelligenceList;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao#queryIntelligence(java.lang.String)
   */
  @Override
  public ThreatIntelligenceDO queryIntelligence(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<ThreatIntelligenceDO> intelligenceList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ThreatIntelligenceDO.class));
    return CollectionUtils.isEmpty(intelligenceList) ? new ThreatIntelligenceDO()
        : intelligenceList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao#saveIntelligences(java.util.List)
   */
  @Override
  public int saveIntelligences(List<ThreatIntelligenceDO> intelligences) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_THREAT_INTELLIGENCE);
    sql.append(" (id, type, content, threat_category, description, timestamp ) ");
    sql.append(" values (:id, :type, :content, :threatCategory, :description, :timestamp ) ");

    int i = 0;
    for (ThreatIntelligenceDO intelligenceDO : intelligences) {
      intelligenceDO.setId(StringUtils.replace(IdGenerator.generateUUID(), "-", String.valueOf(i)));
      i++;
    }

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(intelligences);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  /**
   * @return 
   * @see com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao#updateIntelligence(com.machloop.fpc.manager.analysis.data.ThreatIntelligenceDO)
   */
  @Override
  public int updateIntelligence(ThreatIntelligenceDO intelligenceDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_THREAT_INTELLIGENCE);
    sql.append(" set type = :type, content = :content, timestamp = :timestamp");
    sql.append(" where id = :id ");

    // 只能修改自定义的配置
    intelligenceDO.setTimestamp(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(intelligenceDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao#deleteAllIntelligences()
   */
  @Override
  public int deleteIntelligences(Collection<String> threatCategory) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_ANALYSIS_THREAT_INTELLIGENCE);
    sql.append(" where threat_category in (:threatCategory) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("threatCategory", threatCategory);
    return jdbcTemplate.update(sql.toString(), params);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ThreatIntelligenceDao#deleteIntelligence(java.lang.String)
   */
  @Override
  public int deleteIntelligence(String id) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_ANALYSIS_THREAT_INTELLIGENCE);
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);
    return jdbcTemplate.update(sql.toString(), params);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, type, content, threat_category, description, timestamp ");
    sql.append(" from ").append(TABLE_ANALYSIS_THREAT_INTELLIGENCE);
    return sql;
  }
}
