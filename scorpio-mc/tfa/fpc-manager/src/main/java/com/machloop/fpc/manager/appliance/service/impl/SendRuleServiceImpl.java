package com.machloop.fpc.manager.appliance.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.SendRuleBO;
import com.machloop.fpc.manager.appliance.dao.SendPolicyDao;
import com.machloop.fpc.manager.appliance.dao.SendRuleDao;
import com.machloop.fpc.manager.appliance.data.SendPolicyDO;
import com.machloop.fpc.manager.appliance.data.SendRuleDO;
import com.machloop.fpc.manager.appliance.service.SendRuleService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskPolicyDao;
import com.machloop.fpc.npm.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskPolicyDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
@Order(13)
@Service
public class SendRuleServiceImpl implements SendRuleService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendRuleServiceImpl.class);
  @Autowired
  private SendRuleDao sendRuleDao;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private SendPolicyDao sendPolicyDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private PacketAnalysisTaskPolicyDao packetAnalysisTaskPolicyDao;

  private static final Map<String,
      String> clickHouseTables = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    clickHouseTables.put("t_fpc_protocol_mysql_log_record", "mysql");
    clickHouseTables.put("t_fpc_protocol_postgresql_log_record", "pgsql");
    clickHouseTables.put("t_fpc_protocol_arp_log_record", "arp");
    clickHouseTables.put("t_fpc_protocol_sip_log_record", "sip");
    clickHouseTables.put("t_fpc_protocol_dns_log_record", "dns");
    clickHouseTables.put("t_fpc_protocol_icmp_log_record", "icmp");
    clickHouseTables.put("t_fpc_protocol_ssh_log_record", "ssh");
    clickHouseTables.put("t_fpc_protocol_dhcp_log_record", "dhcp");
    clickHouseTables.put("t_fpc_protocol_ssl_log_record", "ssl");
    clickHouseTables.put("t_fpc_protocol_ftp_log_record", "ftp");
    clickHouseTables.put("t_fpc_protocol_telnet_log_record", "telnet");
    clickHouseTables.put("t_fpc_protocol_socks4_log_record", "socks4");
    clickHouseTables.put("t_fpc_protocol_socks5_log_record", "socks5");
    clickHouseTables.put("t_fpc_protocol_mail_log_record", "mail");
    clickHouseTables.put("t_fpc_protocol_tds_log_record", "tds");
    clickHouseTables.put("t_fpc_protocol_tns_log_record", "tns");
    clickHouseTables.put("t_fpc_protocol_db2_log_record", "db2");
    clickHouseTables.put("t_fpc_protocol_http_log_record", "http_new");
    clickHouseTables.put("t_fpc_protocol_ospf_log_record", "ospf");
    clickHouseTables.put("t_fpc_protocol_ldap_log_record", "ldap");
    clickHouseTables.put("t_fpc_flow_log_record", "flowlog");
    clickHouseTables.put("t_fpc_analysis_suricata_alert_message", "suricata");

    clickHouseTables.put("t_fpc_metric_forward_data_record", "statistics_forward");
    clickHouseTables.put("t_fpc_metric_dhcp_data_record", "statistics_dhcp");
    clickHouseTables.put("t_fpc_metric_http_request_data_record", "http_request_info");
    clickHouseTables.put("t_fpc_metric_disk_io_data_record", "metric_diskio");
    clickHouseTables.put("t_fpc_metric_l3device_data_record", "statistics_l3device");
    clickHouseTables.put("t_fpc_metric_port_data_record", "statistics_port");
    clickHouseTables.put("t_fpc_metric_location_data_record", "statistics_location");
    clickHouseTables.put("t_fpc_metric_network_data_record", "statistics_network");
    clickHouseTables.put("t_fpc_metric_l2device_data_record", "statistics_l2device");
    clickHouseTables.put("t_fpc_metric_service_data_record", "statistics_service");
    clickHouseTables.put("t_fpc_metric_dscp_data_record", "statistics_dscp");
    clickHouseTables.put("t_fpc_metric_os_data_record", "http_terminal_info");
    clickHouseTables.put("t_fpc_metric_monitor_data_record", "system_monitor");
    clickHouseTables.put("t_fpc_metric_http_analysis_data_record", "http_status_info");
    clickHouseTables.put("t_fpc_metric_application_data_record", "statistics_application");
    clickHouseTables.put("t_fpc_metric_hostgroup_data_record", "statistics_hostgroup");
    clickHouseTables.put("t_fpc_metric_netif_data_record", "statistics_netif");
    clickHouseTables.put("t_fpc_metric_ip_conversation_data_record", "statistics_ip_conversation");
    clickHouseTables.put("t_fpc_metric_l7protocol_data_record", "statistics_l7protocol");
  }


  @Override
  public List<Map<String, Object>> querySendRules() {

    List<SendRuleDO> sendRuleDOList = sendRuleDao.querySendRules();

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    sendRuleDOList.forEach(sendRuleDO -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("id", sendRuleDO.getId());
      temp.put("name", sendRuleDO.getName());
      temp.put("sendRuleContent", sendRuleDO.getSendRuleContent());
      List<Map<String, Object>> sendRuleContent = JsonHelper.deserialize(
          sendRuleDO.getSendRuleContent(), new TypeReference<List<Map<String, Object>>>() {
          }, false);
      Map<String,
          Object> outputDetailInfo = JsonHelper.deserialize(
              JsonHelper.serialize(sendRuleContent.get(0).get("output_detail_info")),
              new TypeReference<Map<String, Object>>() {
              }, false);
      temp.put("sendingMethod",
          outputDetailInfo.get("sending_method") instanceof String ? "实时发送" : "定时发送");

      List<String> indexList = sendRuleContent.stream().map(map -> {
        String index = MapUtils.getString(map, "index");
        String originIndex = MapUtils.getString(map, "originIndex");
        if (StringUtils.equals(index, "mail")) {
          index = originIndex.split("-")[1];
        }
        if (StringUtils.equals(index, "dhcp")) {
          index = originIndex;
        }
        return index;
      }).collect(Collectors.toList());
      String index = CsvUtils.convertCollectionToCSV(indexList);
      temp.put("description", index);
      result.add(temp);
    });
    return result;
  }

  @Override
  public Map<String, Object> querySendRule(String id) {

    SendRuleDO sendRuleDO = sendRuleDao.querySendRule(id);
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("id", sendRuleDO.getId());
    result.put("name", sendRuleDO.getName());
    List<Map<String, Object>> sendRuleContent = JsonHelper.deserialize(
        sendRuleDO.getSendRuleContent(), new TypeReference<List<Map<String, Object>>>() {
        }, false);
    Map<String,
        Object> outputDetailInfo = JsonHelper.deserialize(
            JsonHelper.serialize(sendRuleContent.get(0).get("output_detail_info")),
            new TypeReference<Map<String, Object>>() {
            }, false);
    result.put("sendingMethod",
        outputDetailInfo.get("sending_method") instanceof String ? "实时发送" : "定时发送");
    List<String> indexList = sendRuleContent.stream().map(map -> {
      String index = MapUtils.getString(map, "index");
      String originIndex = MapUtils.getString(map, "originIndex");
      if (StringUtils.equals(index, "mail")) {
        index = originIndex.split("-")[1];
      }
      if (StringUtils.equals(index, "dhcp")) {
        index = originIndex;
      }
      return index;
    }).collect(Collectors.toList());
    String index = CsvUtils.convertCollectionToCSV(indexList);
    result.put("description", index);
    result.put("sendRuleContent", sendRuleDO.getSendRuleContent());

    return result;
  }

  @Override
  public List<Map<String, Object>> querySendRuleTables(String index) {

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> tableColumnList = sendRuleDao.querySendRuleTables(index);
    tableColumnList.forEach(tableColumn -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("name", MapUtils.getString(tableColumn, "name"));
      temp.put("type", MapUtils.getString(tableColumn, "type"));
      temp.put("comment", MapUtils.getString(tableColumn, "comment"));
      result.add(temp);
    });
    return addEnumForTables(result, index);
  }

  @Override
  public Map<String, List<Map<String, Object>>> queryClickhouseTables() {

    Map<String, List<Map<String, Object>>> res = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<Map<String, Object>>> tableMapList = sendRuleDao.queryClickhouseTables();
    Set<String> tableNames = tableMapList.keySet();
    for (String table : tableNames) {
      String index = clickHouseTables.get(table);
      List<Map<String, Object>> tableList = tableMapList.get(table);
      res.put(table, addEnumForTables(tableList, index));
    }
    return res;
  }

  protected List<Map<String, Object>> addEnumForTables(List<Map<String, Object>> tableColumnList,
      String index) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    switch (index) {
      case "mysql":
      case "pgsql":
      case "dns":
      case "ssh":
      case "ftp":
      case "telnet":
      case "socks4":
      case "socks5":
      case "tds":
      case "tns":
      case "db2":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "arp":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "type")) {
            Map<String, String> messageTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_arp_message_type");
            map.put("enum", JsonHelper.serialize(messageTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "icmp":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "version")) {
            Map<String,
                String> versionDict = dictManager.getBaseDict().getItemMap("protocol_icmp_version");
            map.put("enum", JsonHelper.serialize(versionDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "dhcp":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "version")) {
            Map<String,
                String> versionDict = dictManager.getBaseDict().getItemMap("protocol_dhcp_version");
            map.put("enum", JsonHelper.serialize(versionDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "message_type")) {
            Map<String, String> v4MessageTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_dhcp_message_type");
            Map<String, String> v6MessageTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_dhcpv6_message_type");
            map.put("enum-V4", JsonHelper.serialize(v4MessageTypeDict));
            map.put("enum-V6", JsonHelper.serialize(v6MessageTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "sip":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "ip_protocol")) {
            Map<String, String> ipProtocolList = Maps
                .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            ipProtocolList.put("tcp", "TCP");
            ipProtocolList.put("udp", "UDP");
            map.put("enum", JsonHelper.serialize(ipProtocolList));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "type")) {
            Map<String,
                String> typeList = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            typeList.put("REGISTER", "REGISTER");
            typeList.put("INVITE", "INVITE");
            typeList.put("ACK", "ACK");
            typeList.put("CANCEL", "CANCEL");
            typeList.put("BYE", "BYE");
            typeList.put("OPTIONS", "OPTIONS");
            typeList.put("MESSAGE", "MESSAGE");
            map.put("enum", JsonHelper.serialize(typeList));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "ssl":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "auth_type")) {
            Map<String, String> authTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_ssl_auth_type");
            map.put("enum", JsonHelper.serialize(authTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "mail":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "auth_type")) {
            Map<String, String> mailTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_mail_protocol");
            map.put("enum", JsonHelper.serialize(mailTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "http_new":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "auth_type")) {
            Map<String, String> authTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_http_auth_type");
            map.put("enum", JsonHelper.serialize(authTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "ospf":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "message_type")) {
            Map<String, String> messageTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_ospf_message_type");
            map.put("enum", JsonHelper.serialize(messageTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "ldap":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "level")) {
            Map<String, String> policyLevelDict = dictManager.getBaseDict()
                .getItemMap("appliance_collect_policy_level");
            map.put("enum", JsonHelper.serialize(policyLevelDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "op_type")) {
            Map<String,
                String> opType = dictManager.getBaseDict().getItemMap("protocol_ldap_op_type");
            map.put("enum", JsonHelper.serialize(opType));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "res_status")) {
            Map<String, String> resStatus = dictManager.getBaseDict()
                .getItemMap("protocol_ldap_res_status");
            map.put("enum", JsonHelper.serialize(resStatus));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "flowlog":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "tcp_session_state")) {
            Map<String, String> tcpSessionStateDict = dictManager.getBaseDict()
                .getItemMap("flow_log_tcp_session_state");
            map.put("enum", JsonHelper.serialize(tcpSessionStateDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "ethernet_type")) {
            Map<String, String> ethernetTypeDict = dictManager.getBaseDict()
                .getItemMap("flow_log_ethernet_type");
            map.put("enum", JsonHelper.serialize(ethernetTypeDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "ethernet_protocol")) {
            Map<String, String> ethernetProtocolDict = dictManager.getBaseDict()
                .getItemMap("flow_log_ethernet_protocol");
            map.put("enum", JsonHelper.serialize(ethernetProtocolDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "ip_locality_initiator")) {
            Map<String, String> ipLocalityDict = dictManager.getBaseDict()
                .getItemMap("flow_log_ip_address_locality");
            map.put("enum", JsonHelper.serialize(ipLocalityDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "ip_locality_responder")) {
            Map<String, String> ipLocalityDict = dictManager.getBaseDict()
                .getItemMap("flow_log_ip_address_locality");
            map.put("enum", JsonHelper.serialize(ipLocalityDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "suricata":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "signature_severity")) {
            Map<String, String> severityDict = dictManager.getBaseDict()
                .getItemMap("analysis_suricata_signature_severity");
            map.put("enum", JsonHelper.serialize(severityDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "source")) {
            Map<String, String> sourceTypeDict = dictManager.getBaseDict()
                .getItemMap("analysis_suricata_rule_source");
            map.put("enum", JsonHelper.serialize(sourceTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "statistics_dhcp":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "dhcp_version")) {
            Map<String,
                String> versionDict = dictManager.getBaseDict().getItemMap("protocol_dhcp_version");
            map.put("enum", JsonHelper.serialize(versionDict));
          }
          if (StringUtils.equals(MapUtils.getString(map, "name"), "message_type")) {
            Map<String, String> v4MessageTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_dhcp_message_type");
            Map<String, String> v6MessageTypeDict = dictManager.getBaseDict()
                .getItemMap("protocol_dhcpv6_message_type");
            map.put("enum-V4", JsonHelper.serialize(v4MessageTypeDict));
            map.put("enum-V6", JsonHelper.serialize(v6MessageTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "statistics_l3device":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "ip_locality")) {
            Map<String, String> ipLocalityDict = dictManager.getBaseDict()
                .getItemMap("flow_log_ip_address_locality");
            map.put("enum", JsonHelper.serialize(ipLocalityDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "statistics_port":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "ip_protocol")) {
            Map<String, String> ipProtocolList = Maps
                .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            ipProtocolList.put("tcp", "TCP");
            ipProtocolList.put("udp", "UDP");
            map.put("enum", JsonHelper.serialize(ipProtocolList));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      case "statistics_l2device":
        result = tableColumnList.stream().map(map -> {
          if (StringUtils.equals(MapUtils.getString(map, "name"), "ethernet_type")) {
            Map<String, String> ethernetTypeDict = dictManager.getBaseDict()
                .getItemMap("flow_log_ethernet_type");
            map.put("enum", JsonHelper.serialize(ethernetTypeDict));
          }
          return map;
        }).collect(Collectors.toList());
        break;
      default:
        result = tableColumnList;
        break;
    }
    return result;
  }


  @Override
  public SendRuleBO saveSendRule(SendRuleBO sendRuleBO, String operatorId) {

    SendRuleDO exist = sendRuleDao.querySendRuleByName(sendRuleBO.getName());

    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发规则名称不能重复");
    }
    List<Map<String, Object>> sendRuleContentList = JsonHelper.deserialize(
        sendRuleBO.getSendRuleContent(), new TypeReference<List<Map<String, Object>>>() {
        }, false);
    List<String> indexList = sendRuleContentList.stream()
        .map(sendRuleContent -> MapUtils.getString(sendRuleContent, "index"))
        .collect(Collectors.toList());
    Map<String,
        Object> outputDetailInfo = JsonHelper.deserialize(
            JsonHelper.serialize(sendRuleContentList.get(0).get("output_detail_info")),
            new TypeReference<Map<String, Object>>() {
            }, false);
    Object sendingMethod = outputDetailInfo.get("sending_method");
    boolean sendNow = false;
    if (sendingMethod instanceof String) {
      sendNow = true;
    }
    List<String> temp = Arrays.asList("suricata", "alert", "systemAlert", "systemLog");
    if (indexList.stream().anyMatch(x -> !temp.contains(x)) && !sendNow) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "抱歉，日志类型中包含元数据、流日志或统计类型，请重新选择发送方式为实时发送");
    }

    SendRuleDO sendRuleDO = new SendRuleDO();
    BeanUtils.copyProperties(sendRuleBO, sendRuleDO);
    sendRuleDO.setOperatorId(operatorId);
    sendRuleDao.saveSendRule(sendRuleDO);

    BeanUtils.copyProperties(sendRuleDO, sendRuleBO);

    return sendRuleBO;
  }

  @Override
  public SendRuleBO updateSendRule(SendRuleBO sendRuleBO, String id, String operatorId) {

    SendRuleDO exist = sendRuleDao.querySendRule(id);

    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发规则不存在");
    }

    SendRuleDO sendRuleDO = new SendRuleDO();
    BeanUtils.copyProperties(sendRuleBO, sendRuleDO);
    sendRuleDO.setOperatorId(operatorId);
    sendRuleDO.setId(id);

    // 先保存完外发规则的更改配置，再修改外发策略的时间
    sendRuleDao.updateSendRule(sendRuleDO);

    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPoliciesBySendRuleId(id);
    if (!sendPolicyDOList.isEmpty()) {
      sendPolicyDao.updateSendPolicyTimeBySendRuleId(id, operatorId);
    }

    BeanUtils.copyProperties(sendRuleDO, sendRuleBO);
    return sendRuleBO;
  }

  @Transactional
  @Override
  public SendRuleBO deleteSendRule(String id, String operatorId, boolean forceDelete) {


    SendRuleDO exist = sendRuleDao.querySendRule(id);

    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发规则不存在");
    }
    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPoliciesBySendRuleId(id);
    if (!forceDelete && !sendPolicyDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "外发规则正在被外发策略使用，无法删除");
    }

    sendRuleDao.deleteSendRule(id, operatorId);

    SendRuleBO sendRuleBO = new SendRuleBO();
    BeanUtils.copyProperties(exist, sendRuleBO);

    return sendRuleBO;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_SENDRULE));
  }

  @Override
  public int syncConfiguration(Message message) {
    Map<String, Object> messageBody = MQMessageHelper.convertToMap(message);

    List<Map<String, Object>> messages = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (MapUtils.getBoolean(messageBody, "batch", false)) {
      messages.addAll(JsonHelper.deserialize(JsonHelper.serialize(messageBody.get("data")),
          new TypeReference<List<Map<String, Object>>>() {
          }));
    } else {
      messages.add(messageBody);
    }

    int syncTotalCount = messages.stream().mapToInt(item -> syncSendRule(item)).sum();
    LOGGER.info("current sync sendRule total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncSendRule(Map<String, Object> messageBody) {
    int syncCount = 0;
    String sendRuleInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(sendRuleInCmsId)) {
      return syncCount;
    }
    String action = MapUtils.getString(messageBody, "action");
    // 下发的规则与本地规则名称冲突，添加后缀
    String name = MapUtils.getString(messageBody, "name");
    SendRuleDO existName = sendRuleDao.querySendRuleByName(name);
    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && StringUtils.isNotBlank(existName.getId())) {
      name = name + "_" + "CMS";
    }
    SendRuleBO sendRuleBO = new SendRuleBO();
    sendRuleBO.setId(sendRuleInCmsId);
    sendRuleBO.setSendRuleInCmsId(sendRuleInCmsId);
    sendRuleBO.setName(name);
    sendRuleBO.setSendRuleContent(MapUtils.getString(messageBody, "sendRuleContent"));

    SendRuleDO exist = sendRuleDao.querySendRuleBySendRuleInCmsId(sendRuleBO.getSendRuleInCmsId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateSendRule(sendRuleBO, exist.getId(), CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                sendRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveSendRule(sendRuleBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                sendRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteSendRule(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              sendRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync sendRule status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync failed. error msg: {}", e.getMessage());
      return syncCount;
    }

    return syncCount;
  }

  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    int clearCount = 0;
    // 本地的外发规则策略
    List<String> sendRuleIds = sendRuleDao.querySendRuleIds(onlyLocal);
    // 本地正在被网络或者离线分析任务使用的外发规则
    Set<String> policyIds = Sets.newHashSetWithExpectedSize(0);
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    policyIds.addAll(
        networkPolicyDOList.stream().map(NetworkPolicyDO::getPolicyId).collect(Collectors.toSet()));
    List<PacketAnalysisTaskPolicyDO> packetAnalysisTaskPolicyDOList = packetAnalysisTaskPolicyDao
        .queryPolicyIdsByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    policyIds.addAll(packetAnalysisTaskPolicyDOList.stream()
        .map(PacketAnalysisTaskPolicyDO::getPolicyId).collect(Collectors.toSet()));

    Set<String> existSendRuleIds = sendPolicyDao.querySendPolicies().stream()
        .filter(sendPolicyDO -> policyIds.contains(sendPolicyDO.getId()))
        .map(SendPolicyDO::getSendRuleId).collect(Collectors.toSet());
    for (String sendRuleId : sendRuleIds) {
      if (existSendRuleIds.contains(sendRuleId)) {
        LOGGER.warn("外发服务器已被网络或离线任务使用，不能删除");
        continue;
      }
      try {
        deleteSendRule(sendRuleId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete sendRule failed. error msg: {}", e.getMessage());
      }

    }
    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return sendRuleDao.queryAssignSendRuleIds(beforeTime);
  }
}
