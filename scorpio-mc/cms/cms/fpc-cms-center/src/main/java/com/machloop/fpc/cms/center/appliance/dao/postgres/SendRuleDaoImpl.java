package com.machloop.fpc.cms.center.appliance.dao.postgres;

import java.util.ArrayList;
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
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.appliance.dao.SendRuleDao;
import com.machloop.fpc.cms.center.appliance.data.SendRuleDO;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
@Repository
public class SendRuleDaoImpl implements SendRuleDao {

  private static final String TABLE_APPLIANCE_SEND_RULE = "fpccms_appliance_send_rule";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  @Autowired
  private ClickHouseJdbcTemplate clickHouseJdbcTemplate;


  private static final Map<String,
      String> clickHouseTables = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    clickHouseTables.put("mysql", "d_fpc_protocol_mysql_log_record");
    clickHouseTables.put("pgsql", "d_fpc_protocol_postgresql_log_record");
    clickHouseTables.put("arp", "d_fpc_protocol_arp_log_record");
    clickHouseTables.put("sip", "d_fpc_protocol_sip_log_record");
    clickHouseTables.put("dns", "d_fpc_protocol_dns_log_record");
    clickHouseTables.put("icmp", "d_fpc_protocol_icmp_log_record");
    clickHouseTables.put("ssh", "d_fpc_protocol_ssh_log_record");
    clickHouseTables.put("dhcp", "d_fpc_protocol_dhcp_log_record");
    clickHouseTables.put("ssl", "d_fpc_protocol_ssl_log_record");
    clickHouseTables.put("ftp", "d_fpc_protocol_ftp_log_record");
    clickHouseTables.put("telnet", "d_fpc_protocol_telnet_log_record");
    clickHouseTables.put("socks4", "d_fpc_protocol_socks4_log_record");
    clickHouseTables.put("socks5", "d_fpc_protocol_socks5_log_record");
    clickHouseTables.put("mail", "d_fpc_protocol_mail_log_record");
    clickHouseTables.put("tds", "d_fpc_protocol_tds_log_record");
    clickHouseTables.put("tns", "d_fpc_protocol_tns_log_record");
    clickHouseTables.put("db2", "d_fpc_protocol_db2_log_record");
    clickHouseTables.put("http_new", "d_fpc_protocol_http_log_record");
    clickHouseTables.put("ospf", "d_fpc_protocol_ospf_log_record");
    clickHouseTables.put("ldap", "d_fpc_protocol_ldap_log_record");
    clickHouseTables.put("flowlog", "d_fpc_flow_log_record");
    clickHouseTables.put("suricata", "d_fpc_analysis_suricata_alert_message");

    clickHouseTables.put("statistics_forward", "d_fpc_metric_forward_data_record");
    clickHouseTables.put("statistics_dhcp", "d_fpc_metric_dhcp_data_record");
    clickHouseTables.put("http_request_info", "d_fpc_metric_http_request_data_record");
    clickHouseTables.put("metric_diskio", "d_fpc_metric_disk_io_data_record");
    clickHouseTables.put("statistics_l3device", "d_fpc_metric_l3device_data_record");
    clickHouseTables.put("statistics_port", "d_fpc_metric_port_data_record");
    clickHouseTables.put("statistics_location", "d_fpc_metric_location_data_record");
    clickHouseTables.put("statistics_network", "d_fpc_metric_network_data_record");
    clickHouseTables.put("statistics_l2device", "d_fpc_metric_l2device_data_record");
    clickHouseTables.put("statistics_service", "d_fpc_metric_service_data_record");
    clickHouseTables.put("statistics_dscp", "d_fpc_metric_dscp_data_record");
    clickHouseTables.put("http_terminal_info", "d_fpc_metric_os_data_record");
    clickHouseTables.put("system_monitor", "d_fpc_metric_monitor_data_record");
    clickHouseTables.put("http_status_info", "d_fpc_metric_http_analysis_data_record");
    clickHouseTables.put("statistics_application", "d_fpc_metric_application_data_record");
    clickHouseTables.put("statistics_hostgroup", "d_fpc_metric_hostgroup_data_record");
    clickHouseTables.put("statistics_netif", "d_fpc_metric_netif_data_record");
    clickHouseTables.put("statistics_ip_conversation", "d_fpc_metric_ip_conversation_data_record");
    clickHouseTables.put("statistics_l7protocol", "d_fpc_metric_l7protocol_data_record");
  }

  @Override
  public List<SendRuleDO> querySendRules() {

    StringBuilder sql = buildSelectStatement();

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendRuleDO.class));
  }

  @Override
  public SendRuleDO querySendRule(String id) {

    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where id = :id and deleted = :deleted ");
    params.put("id", id);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    sql.append(whereSql);

    List<SendRuleDO> sendRuleDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendRuleDO.class));

    return CollectionUtils.isEmpty(sendRuleDOList) ? new SendRuleDO() : sendRuleDOList.get(0);

  }

  @Override
  public List<String> querySendRuleIds(boolean onlyLocal) {

    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_SEND_RULE);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and assign_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public SendRuleDO querySendRuleByAssignId(String assignId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and assign_id = :assignId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("assignId", assignId);

    List<SendRuleDO> sendRuleDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendRuleDO.class));

    return CollectionUtils.isEmpty(sendRuleDOList) ? new SendRuleDO() : sendRuleDOList.get(0);
  }

  @Override
  public List<SendRuleDO> queryAssignSendRuleIds(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, assign_id from ").append(TABLE_APPLIANCE_SEND_RULE);
    sql.append(" where deleted = :deleted and assign_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<SendRuleDO> sendRuleDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendRuleDO.class));

    return CollectionUtils.isEmpty(sendRuleDOList) ? Lists.newArrayListWithCapacity(0)
        : sendRuleDOList;
  }

  @Override
  public List<Map<String, Object>> querySendRuleTables(String index) {

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    StringBuilder sql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String tableName = clickHouseTables.get(index);
    sql.append(" desc ").append(tableName);
    result = clickHouseJdbcTemplate.getJdbcTemplate().query(sql.toString(), params,
        new ColumnMapRowMapper());

    return result;
  }

  @Override
  public Map<String, List<Map<String, Object>>> queryClickhouseTables() {

    Map<String, List<Map<String, Object>>> result = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder sql = new StringBuilder();
    sql.append(" select table , name, type, comment from system.columns ");
    List<String> clickHouseTableNames = new ArrayList<>(clickHouseTables.values());
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where database='fpc' ");
    whereSql.append(" and table in (");
    clickHouseTableNames.forEach(clickHouseTable -> {
      whereSql.append("'").append(clickHouseTable).append("', ");
    });
    sql.append(whereSql.substring(0, whereSql.length() - 2)).append(" ) ");
    List<Map<String, Object>> clickHouseTables = clickHouseJdbcTemplate.getJdbcTemplate()
        .query(sql.toString(), params, new ColumnMapRowMapper());


    Map<String, List<Map<String, Object>>> chTables = clickHouseTables.stream()
        .collect(Collectors.groupingBy(map -> MapUtils.getString(map, "table")));
    result.putAll(chTables);

    return result;
  }

  @Override
  public SendRuleDO querySendRuleByName(String name) {

    StringBuilder sql = buildSelectStatement();
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where name = :name and deleted = :deleted ");
    params.put("name", name);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    sql.append(whereSql);

    List<SendRuleDO> sendRuleDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SendRuleDO.class));

    return CollectionUtils.isEmpty(sendRuleDOList) ? new SendRuleDO() : sendRuleDOList.get(0);
  }

  @Override
  public void saveSendRule(SendRuleDO sendRuleDO) {

    StringBuilder sql = new StringBuilder();
    sql.append(" insert into ").append(TABLE_APPLIANCE_SEND_RULE);
    sql.append(" (id, name, send_rule_content, assign_id, ");
    sql.append(" update_time, create_time, operator_id) ");
    sql.append(" values (:id, :name, :sendRuleContent, :assignId,");
    sql.append(" :updateTime, :createTime, :operatorId) ");

    if (StringUtils.isBlank(sendRuleDO.getId())) {
      sendRuleDO.setId(IdGenerator.generateUUID());
    }

    sendRuleDO.setCreateTime(DateUtils.now());
    sendRuleDO.setUpdateTime(sendRuleDO.getCreateTime());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sendRuleDO);
    jdbcTemplate.update(sql.toString(), paramSource);

  }

  @Override
  public void updateSendRule(SendRuleDO sendRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SEND_RULE);
    sql.append(" set name = :name, send_rule_content = :sendRuleContent, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    sendRuleDO.setUpdateTime(DateUtils.now());
    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sendRuleDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  @Override
  public void deleteSendRule(String id, String operatorId) {

    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SEND_RULE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    SendRuleDO sendRuleDO = new SendRuleDO();
    sendRuleDO.setDeleted(Constants.BOOL_YES);
    sendRuleDO.setDeleteTime(DateUtils.now());
    sendRuleDO.setOperatorId(operatorId);
    sendRuleDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(sendRuleDO);
    jdbcTemplate.update(sql.toString(), paramSource);
  }

  private StringBuilder buildSelectStatement() {

    StringBuilder sql = new StringBuilder();
    sql.append(" select id, name, send_rule_content, assign_id, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SEND_RULE);

    return sql;
  }


}
