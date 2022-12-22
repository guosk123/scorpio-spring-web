package com.machloop.fpc.cms.center.appliance.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.FilterRuleIpDao;
import com.machloop.fpc.cms.center.appliance.data.FilterRuleNetworkDO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/9/29 1:53 PM,cms
 * @version 1.0
 */
@Repository
public class FilterRuleIpDaoImpl implements FilterRuleIpDao {

  private static final String TABLE_APPLIANCE_FILTER_RULE_NETWORK = "fpccms_appliance_filter_rule_network";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Override
  public FilterRuleNetworkDO queryIPOrIPGroupByFilterRuleId() {
    StringBuilder sql = buildSelectStatement();

    List<FilterRuleNetworkDO> ipGroupDOS = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(FilterRuleNetworkDO.class));

    return ipGroupDOS.size() == 0 ? new FilterRuleNetworkDO() : ipGroupDOS.get(0);
  }

  @Override
  public List<FilterRuleNetworkDO> queryFilterRuleNetworks() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, filter_rule_id, network_id, network_group_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_FILTER_RULE_NETWORK);

    List<FilterRuleNetworkDO> filterRuleNetworkDOList = jdbcTemplate.query(sql.toString(),
        new BeanPropertyRowMapper<>(FilterRuleNetworkDO.class));

    return CollectionUtils.isEmpty(filterRuleNetworkDOList) ? Lists.newArrayListWithCapacity(0)
        : filterRuleNetworkDOList;
  }

  @Override
  public List<FilterRuleNetworkDO> getIP(String id) {
    StringBuilder sql = buildSelectStatement();

    sql.append(" where filter_rule_id = :filterRuleId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("filterRuleId", id);

    List<FilterRuleNetworkDO> query = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleNetworkDO.class));

    return query.isEmpty() ? Lists.newArrayListWithCapacity(0) : query;
  }

  @Override
  public List<FilterRuleNetworkDO> queryFilterRuleNetworkByNetworkGroupId(String netWorkGroupId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, filter_rule_id, network_group_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_FILTER_RULE_NETWORK);
    sql.append(" where 1 = 1 ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(netWorkGroupId)) {
      sql.append(" and network_group_id = :networkGroupId ");
      params.put("networkGroupId", netWorkGroupId);
    }

    List<FilterRuleNetworkDO> filterRuleNetworkDOS = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(FilterRuleNetworkDO.class));

    return CollectionUtils.isEmpty(filterRuleNetworkDOS) ? Lists.newArrayListWithCapacity(0)
        : filterRuleNetworkDOS;
  }

  @Override
  public void saveFilterRuleIp(List<FilterRuleNetworkDO> filterRuleIPOrIPHostGroupDO) {
    // delete
    deleteFilterRuleByFilterRuleId(filterRuleIPOrIPHostGroupDO.get(0).getFilterRuleId());

    // batch insert
    batchSaveFilterRule(filterRuleIPOrIPHostGroupDO);
  }

  @Override
  public int deleteFilterRuleByFilterRuleId(String filterRuleId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_APPLIANCE_FILTER_RULE_NETWORK);
    sql.append(" where filter_rule_id = :filterRuleId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("filterRuleId", filterRuleId);
    return jdbcTemplate.update(sql.toString(), params);
  }

  private void batchSaveFilterRule(List<FilterRuleNetworkDO> filterRuleIPOrIPHostGroupDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ");
    sql.append(TABLE_APPLIANCE_FILTER_RULE_NETWORK);
    sql.append(" (id, filter_rule_id, network_id, network_group_id) ");
    sql.append(" values (:id, :filterRuleId, :networkId, :networkGroupId) ");

    filterRuleIPOrIPHostGroupDO.forEach(serviceRule -> {
      serviceRule.setId(IdGenerator.generateUUID());
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils
        .createBatch(filterRuleIPOrIPHostGroupDO);
    jdbcTemplate.batchUpdate(sql.toString(), batchSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, filter_rule_id, network_id, network_group_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_FILTER_RULE_NETWORK);

    return sql;
  }
}
