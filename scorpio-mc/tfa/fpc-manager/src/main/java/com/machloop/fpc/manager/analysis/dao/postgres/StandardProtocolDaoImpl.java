package com.machloop.fpc.manager.analysis.dao.postgres;

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

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.analysis.dao.StandardProtocolDao;
import com.machloop.fpc.manager.analysis.data.StandardProtocolDO;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
@Repository
public class StandardProtocolDaoImpl implements StandardProtocolDao {

  private static final String TABLE_ANALYSIS_STANDARD_PROTOCOL = "fpc_analysis_standard_protocol";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.analysis.dao.StandardProtocolDao#queryStandardProtocols(com.machloop.alpha.common.base.page.PageRequest, com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO)
   */
  @Override
  public Page<StandardProtocolDO> queryStandardProtocols(Pageable page,
      StandardProtocolQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    if (StringUtils.isNotBlank(queryVO.getProtocol())) {
      whereSql.append(" and (l7_protocol_id = :protocol ");
      whereSql.append(" or ip_protocol = :protocol) ");
    }
    if (StringUtils.isNotBlank(queryVO.getPort())) {
      whereSql.append("and port = :port ");
    }
    if (StringUtils.isNotBlank(queryVO.getSource())) {
      whereSql.append("and source = :source ");
    }

    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("protocol", queryVO.getProtocol());
    params.put("port", queryVO.getPort());
    params.put("source", queryVO.getSource());
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    PageUtils.appendPage(sql, page, StandardProtocolDO.class);

    List<StandardProtocolDO> standardProtocolList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(StandardProtocolDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_ANALYSIS_STANDARD_PROTOCOL);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(standardProtocolList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.StandardProtocolDao#queryStandardProtocols(com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO)
   */
  @Override
  public List<StandardProtocolDO> queryStandardProtocols(StandardProtocolQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    if (StringUtils.isNotBlank(queryVO.getProtocol())) {
      whereSql.append(" and (l7_protocol_id = :protocol ");
      whereSql.append(" or ip_protocol = :protocol) ");
    }
    if (StringUtils.isNotBlank(queryVO.getPort())) {
      whereSql.append("and port = :port ");
    }
    if (StringUtils.isNotBlank(queryVO.getSource())) {
      whereSql.append("and source = :source ");
    }

    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("protocol", queryVO.getProtocol());
    params.put("port", queryVO.getPort());
    params.put("source", queryVO.getSource());
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<StandardProtocolDO> standardProtocolList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(StandardProtocolDO.class));

    return standardProtocolList;
  }
  
  @Override
  public List<StandardProtocolDO> queryStandardProtocols() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<StandardProtocolDO> standardProtocolList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(StandardProtocolDO.class));

    return standardProtocolList;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.StandardProtocolDao#queryStandardProtocol(java.lang.String)
   */
  @Override
  public StandardProtocolDO queryStandardProtocol(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<StandardProtocolDO> standardProtocolList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(StandardProtocolDO.class));
    return CollectionUtils.isEmpty(standardProtocolList) ? new StandardProtocolDO()
        : standardProtocolList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.StandardProtocolDao#queryStandardProtocol(java.lang.String, java.lang.String)
   */
  @Override
  public StandardProtocolDO queryStandardProtocol(String l7ProtocolId, String port) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and l7_protocol_id = :l7ProtocolId and port = :port ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("l7ProtocolId", l7ProtocolId);
    params.put("port", port);

    List<StandardProtocolDO> standardProtocolList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(StandardProtocolDO.class));
    return CollectionUtils.isEmpty(standardProtocolList) ? new StandardProtocolDO()
        : standardProtocolList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.StandardProtocolDao#saveStandardProtocol(com.machloop.fpc.manager.analysis.data.StandardProtocolDO)
   */
  @Override
  public StandardProtocolDO saveStandardProtocol(StandardProtocolDO standardProtocolDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_STANDARD_PROTOCOL);
    sql.append(" (id, l7_protocol_id, ip_protocol, port, ");
    sql.append(" source, description, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :l7ProtocolId, :ipProtocol, :port, ");
    sql.append(" :source, :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    standardProtocolDO.setId(IdGenerator.generateUUID());
    standardProtocolDO.setSource(FpcConstants.ANALYSIS_CONFIG_PROTOCOL_SOURCE_CUSTOM);
    standardProtocolDO.setDescription(
        standardProtocolDO.getDescription() == null ? "" : standardProtocolDO.getDescription());
    standardProtocolDO.setCreateTime(DateUtils.now());
    standardProtocolDO.setUpdateTime(standardProtocolDO.getCreateTime());
    standardProtocolDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(standardProtocolDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return standardProtocolDO;
  }

  /**
   * @return 
   * @see com.machloop.fpc.manager.analysis.dao.StandardProtocolDao#updateStandardProtocol(com.machloop.fpc.manager.analysis.data.StandardProtocolDO)
   */
  @Override
  public int updateStandardProtocol(StandardProtocolDO standardProtocolDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_STANDARD_PROTOCOL);
    sql.append(" set l7_protocol_id = :l7ProtocolId, ");
    sql.append(" ip_protocol = :ipProtocol, port = :port, ");
    sql.append(" description = :description, update_time = :updateTime, ");
    sql.append(" operator_id = :operatorId ");
    sql.append(" where id = :id and source = :source ");

    // 只能修改自定义的配置
    standardProtocolDO.setSource(FpcConstants.ANALYSIS_CONFIG_PROTOCOL_SOURCE_CUSTOM);
    standardProtocolDO.setDescription(
        standardProtocolDO.getDescription() == null ? "" : standardProtocolDO.getDescription());
    standardProtocolDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(standardProtocolDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.StandardProtocolDao#deleteStandardProtocol(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteStandardProtocol(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_STANDARD_PROTOCOL);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id and source = :source ");

    StandardProtocolDO standardProtocolDO = new StandardProtocolDO();
    standardProtocolDO.setDeleted(Constants.BOOL_YES);
    standardProtocolDO.setDeleteTime(DateUtils.now());
    standardProtocolDO.setOperatorId(operatorId);

    standardProtocolDO.setId(id);
    // 只能删除自定义的配置
    standardProtocolDO.setSource(FpcConstants.ANALYSIS_CONFIG_PROTOCOL_SOURCE_CUSTOM);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(standardProtocolDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, l7_protocol_id, ip_protocol, port, ");
    sql.append(" source, description, deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_ANALYSIS_STANDARD_PROTOCOL);
    return sql;
  }
}
