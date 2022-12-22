package com.machloop.fpc.manager.analysis.dao.postgres;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.analysis.dao.TiThreatBookDao;
import com.machloop.fpc.manager.analysis.data.TiThreatBookDO;
import com.machloop.fpc.manager.analysis.vo.TiThreatBookQueryVO;

/**
 * @author ChenXiao
 * create at 2022/9/6
 */
@Repository
public class TiThreatBookSDaoImpl implements TiThreatBookDao {


  private static final String TABLE_ANALYSIS_TI_THREATBOOK = "fpc_analysis_ti_threatbook";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;


  @Override
  public Page<TiThreatBookDO> queryTiThreatBooks(PageRequest page, TiThreatBookQueryVO queryVO) {

    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where 1=1 ");
    if (StringUtils.isNotBlank(queryVO.getBasicTag())) {
      whereSql.append(" and basic_tag = :basicTag ");
      params.put("basicTag", queryVO.getBasicTag());
    }
    if (StringUtils.isNotBlank(queryVO.getTag())) {
      whereSql.append(" and tag like :tag ");
      params.put("tag", "%" + queryVO.getTag() + "%");
    }
    if (StringUtils.isNotBlank(queryVO.getIocType())) {
      whereSql.append(" and ioc_type = :iocType ");
      params.put("iocType", queryVO.getIocType());
    }

    sql.append(whereSql);


    PageUtils.appendPage(sql, page, TiThreatBookDO.class);

    List<TiThreatBookDO> tiThreatBookDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(TiThreatBookDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_ANALYSIS_TI_THREATBOOK);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(tiThreatBookDOList, page, total);
  }

  @Override
  public Map<String, Object> queryTiThreatBook(String id) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<Map<String, Object>> tiThreatBookList = jdbcTemplate.query(sql.toString(), params,
        new ColumnMapRowMapper());
    return CollectionUtils.isEmpty(tiThreatBookList) ? Maps.newHashMapWithExpectedSize(0)
        : tiThreatBookList.get(0);
  }

  @Override
  public List<String> queryTiThreatBooksBasicTags() {

    StringBuilder sql = new StringBuilder();
    sql.append(" select distinct basic_tag ");
    sql.append(" from ").append(TABLE_ANALYSIS_TI_THREATBOOK);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<Map<String, Object>> tiThreatBookList = jdbcTemplate.query(sql.toString(), params,
        new ColumnMapRowMapper());

    return tiThreatBookList.stream().map(map -> MapUtils.getString(map, "basic_tag"))
        .collect(Collectors.toList());

  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, ioc_raw, basic_tag, intel_type, source, tag, time, ioc_type ");
    sql.append(" from ").append(TABLE_ANALYSIS_TI_THREATBOOK);
    return sql;
  }
}
