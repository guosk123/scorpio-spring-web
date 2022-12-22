package com.machloop.fpc.npm.analysis.dao.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.machloop.fpc.npm.analysis.dao.MitreAttackDao;
import com.machloop.fpc.npm.analysis.data.MitreAttackDO;

/**
 * @author guosk
 *
 * create at 2022年4月6日, fpc-manager
 */
@Repository
public class MitreAttackDaoImpl implements MitreAttackDao {

  private static final String TABLE_ANALYSIS_MITRE_ATTACK = "fpc_analysis_mitre_attack";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.analysis.dao.MitreAttackDao#queryMitreAttacks()
   */
  @Override
  public List<MitreAttackDO> queryMitreAttacks() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, parent_id ");
    sql.append(" from ").append(TABLE_ANALYSIS_MITRE_ATTACK);

    List<MitreAttackDO> list = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(MitreAttackDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

}
