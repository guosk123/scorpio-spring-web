package com.machloop.fpc.manager.statistics.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.statistics.dao.RrdDao;
import com.machloop.fpc.manager.statistics.data.RrdDO;

/**
 * @author liumeng
 *
 * create at 2018年12月21日, fpc-manager
 */
@Repository
public class RrdDaoImpl implements RrdDao {

  private static final String TABLE_STATISTICS_RRD = "fpc_statistics_rrd";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.statistics.dao.RrdDao#queryRrdByName(java.lang.String)
   */
  @Override
  public RrdDO queryRrdByName(String name) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, last_time, last_position ");
    sql.append(" from ").append(TABLE_STATISTICS_RRD);
    sql.append(" where name = :name ");

    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("name", name);

    List<RrdDO> rrdDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(RrdDO.class));
    return CollectionUtils.isEmpty(rrdDOList) ? new RrdDO() : rrdDOList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.RrdDao#saveRrd(com.machloop.fpc.manager.statistics.data.RrdDO)
   */
  @Override
  public RrdDO saveRrd(RrdDO rrdDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("INSERT INTO ").append(TABLE_STATISTICS_RRD);
    sql.append(" ( id, name, last_time, last_position )");
    sql.append(" VALUES ( :id, :name, :lastTime, :lastPosition ) ");

    rrdDO.setId(IdGenerator.generateUUID());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(rrdDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return rrdDO;
  }

  /**
   * @see com.machloop.fpc.manager.statistics.dao.RrdDao#updateRrd(com.machloop.fpc.manager.statistics.data.RrdDO)
   */
  @Override
  public int updateRrd(RrdDO rrdDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_STATISTICS_RRD);
    sql.append(" set last_time = :lastTime, last_position = :lastPosition ");
    sql.append(" where name = :name ");

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(rrdDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }


}
