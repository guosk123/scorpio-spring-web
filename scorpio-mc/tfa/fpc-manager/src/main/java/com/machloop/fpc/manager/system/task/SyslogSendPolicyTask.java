package com.machloop.fpc.manager.system.task;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.cloudbees.syslog.MessageFormat;
import com.cloudbees.syslog.SyslogMessage;
import com.cloudbees.syslog.sender.TcpSyslogMessageSender;
import com.cloudbees.syslog.sender.UdpSyslogMessageSender;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.service.SyslogService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.dao.ExternalReceiverDao;
import com.machloop.fpc.manager.appliance.dao.SendPolicyDao;
import com.machloop.fpc.manager.appliance.dao.SendRuleDao;
import com.machloop.fpc.manager.appliance.data.ExternalReceiverDO;
import com.machloop.fpc.manager.appliance.data.SendPolicyDO;
import com.machloop.fpc.manager.appliance.data.SendRuleDO;
import com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskPolicyDao;
import com.machloop.fpc.npm.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskPolicyDO;

/**
 * @author ChenXiao
 * create at 2022/9/2
 */
@Component
public class SyslogSendPolicyTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyslogSendPolicyTask.class);


  private final Map<String, UdpSyslogMessageSender> syslogUdpClientMap = Maps
      .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  private final Map<String, TcpSyslogMessageSender> syslogTcpClientMap = Maps
      .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);


  private TcpSyslogMessageSender tcpSyslogMessageSender;

  private UdpSyslogMessageSender udpSyslogMessageSender;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private SyslogService syslogService;

  @Autowired
  private ExternalReceiverDao externalReceiverDao;

  @Autowired
  private SendRuleDao sendRuleDao;

  @Autowired
  private SendPolicyDao sendPolicyDao;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private PacketAnalysisTaskPolicyDao packetAnalysisTaskPolicyDao;

  private Date lastSendSyslogTime = null;


  @Scheduled(cron = "${task.system.send.syslog.schedule.cron}")
  public void run() {
    LOGGER.debug("start execute log and alarm sendup task.");

    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPoliciesStateOn();
    List<SendPolicyDO> sendSyslogPolicyList = sendPolicyDOList.stream().filter(sendPolicyDO -> {
      ExternalReceiverDO externalReceiverDO = externalReceiverDao
          .queryExternalReceiver(sendPolicyDO.getExternalReceiverId());
      String receiverType = externalReceiverDO.getReceiverType();
      return StringUtils.equals(receiverType, FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_SYSLOG);
    }).collect(Collectors.toList());
    // 过滤掉没有被网络和离线任务引用的syslog相关的外发策略，与引擎端保持一致
    List<String> policyIdsOfNetworkPolicy = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND).stream()
        .map(NetworkPolicyDO::getPolicyId).collect(Collectors.toList());
    List<String> policyIdsOfPacketAnalysisTaskPolicy = packetAnalysisTaskPolicyDao
        .queryPolicyIdsByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND).stream()
        .map(PacketAnalysisTaskPolicyDO::getPolicyId).collect(Collectors.toList());
    sendSyslogPolicyList = sendSyslogPolicyList.stream()
        .filter(sendPolicyDO -> policyIdsOfNetworkPolicy.contains(sendPolicyDO.getId())
            || policyIdsOfPacketAnalysisTaskPolicy.contains(sendPolicyDO.getId()))
        .collect(Collectors.toList());
    List<Map<String, Object>> sendSyslogList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    sendSyslogPolicyList.forEach(sendPolicyDO -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      ExternalReceiverDO externalReceiverDO = externalReceiverDao
          .queryExternalReceiver(sendPolicyDO.getExternalReceiverId());
      SendRuleDO sendRuleDO = sendRuleDao.querySendRule(sendPolicyDO.getSendRuleId());
      Map<String, Object> receiverContent = JsonHelper.deserialize(
          externalReceiverDO.getReceiverContent(), new TypeReference<Map<String, Object>>() {
          }, false);
      String syslogServerAddress = MapUtils.getString(receiverContent, "syslogServerIpAddress")
          + ":" + MapUtils.getString(receiverContent, "syslogServerPort");
      temp.put("syslogServerAddress", syslogServerAddress);
      temp.put("protocol", MapUtils.getString(receiverContent, "protocol"));
      temp.put("severity", MapUtils.getString(receiverContent, "severity"));
      temp.put("facility", MapUtils.getString(receiverContent, "facility"));
      temp.put("encodeType", MapUtils.getString(receiverContent, "encodeType"));
      temp.put("separator",
          receiverContent.containsKey("separator")
              ? MapUtils.getString(receiverContent, "separator")
              : "，");
      String systemAlarmContent = null, systemLogContent = null;
      List<Map<String, Object>> sendRuleContent = JsonHelper.deserialize(
          sendRuleDO.getSendRuleContent(), new TypeReference<List<Map<String, Object>>>() {
          }, false);
      for (Map<String, Object> sendRule : sendRuleContent) {
        List<Map<String, Object>> properties = JsonHelper.deserialize(
            JsonHelper.serialize(sendRule.get("properties")),
            new TypeReference<List<Map<String, Object>>>() {
            }, false);
        List<String> propertiesList = properties.stream()
            .map(property -> MapUtils.getString(property, "field_name"))
            .collect(Collectors.toList());
        String propertiesContent = CsvUtils.convertCollectionToCSV(propertiesList);
        if (StringUtils.equals(MapUtils.getString(sendRule, "index"), "systemAlert")) {
          systemAlarmContent = propertiesContent;
        } else if (StringUtils.equals(MapUtils.getString(sendRule, "index"), "systemLog")) {
          systemLogContent = propertiesContent;
        }
      }
      temp.put("systemAlarmContent", systemAlarmContent);
      temp.put("systemLogContent", systemLogContent);
      Map<String,
          Object> outputDetailInfo = JsonHelper.deserialize(
              JsonHelper.serialize(sendRuleContent.get(0).get("output_detail_info")),
              new TypeReference<Map<String, Object>>() {
              }, false);
      Object sendingMethod = outputDetailInfo.get("sending_method");
      if (sendingMethod instanceof String) {
        temp.put("interval", 0);
      } else {
        temp.put("interval", sendingMethod);
      }
      sendSyslogList.add(temp);
    });

    String latestSendupTimeStr = globalSettingService
        .getValue(WebappConstants.GLOBAL_SETTING_SYSLOG_SENDUP_LATEST_TIME);
    Map<String,
        Object> lastSendupTimeMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(latestSendupTimeStr)) {
      lastSendupTimeMap = JsonHelper.deserialize(latestSendupTimeStr,
          new TypeReference<Map<String, Object>>() {
          }, false);
    }

    List<Map<String, Object>> result = updateSyslogClientAndLastSendupTime(sendSyslogList,
        lastSendupTimeMap);

    for (Map<String, Object> sendPolicy : result) {
      String syslogId = MapUtils.getString(sendPolicy, "syslogServerAddress").replace(":", "");

      Date now = DateUtils.now();
      double multipleOfInterval = ((double) now.getTime() / 1000) / (double) Constants.FIVE_SECONDS;
      long roundValue = Math.round(multipleOfInterval);
      now = new Date(roundValue * Constants.FIVE_SECONDS * 1000);

      long actualInterval = 0L;
      long interval = 0L;
      long lastSendTimeValue = (roundValue - 1) * Constants.FIVE_SECONDS * 1000;

      Date lastSendTime = StringUtils.isBlank(MapUtils.getString(lastSendupTimeMap, syslogId))
          ? new Date(lastSendTimeValue)
          : DateUtils.parseISO8601Date(MapUtils.getString(lastSendupTimeMap, syslogId));

      actualInterval = now.getTime() - lastSendTime.getTime();
      Integer intervalNum = MapUtils.getInteger(sendPolicy, "interval");
      interval = intervalNum == 0 ? Constants.FIVE_SECONDS
          : intervalNum * Constants.ONE_MINUTE_SECONDS * 1000;

      if (actualInterval >= interval) {
        List<SyslogMessage> messageList = syslogService.collectMessage(lastSendTime, now,
            MapUtils.getString(sendPolicy, "facility"), MapUtils.getString(sendPolicy, "severity"),
            MapUtils.getString(sendPolicy, "separator"),
            MapUtils.getString(sendPolicy, "systemAlarmContent"),
            MapUtils.getString(sendPolicy, "systemLogContent"));
        String protocol = MapUtils.getString(sendPolicy, "protocol");
        messageList.forEach(message -> {
          try {
            if (StringUtils.equals(protocol, "TCP")) {
              syslogTcpClientMap.get(syslogId).sendMessage(message);
            } else if (StringUtils.equals(protocol, "UDP")) {
              syslogUdpClientMap.get(syslogId).sendMessage(message);
            }
          } catch (IOException e) {
            String exceptionMessage = e.getMessage();
            if (lastSendSyslogTime == null || lastSendSyslogTime.getTime()
                + Constants.ONE_HOUR_SECONDS * 1000 < System.currentTimeMillis()) {
              AlarmHelper.alarm(AlarmHelper.LEVEL_NORMAL, AlarmHelper.CATEGORY_ARCHIVE,
                  "syslog_action", "syslog发送失败，原因是：" + exceptionMessage);
              LOGGER.warn("syslog发送失败.", e);
              lastSendSyslogTime = DateUtils.now();
            }
          }
        });
        // 写入下次开始外发的时间
        lastSendupTimeMap.remove(syslogId);
        lastSendupTimeMap.put(syslogId, DateUtils.toStringISO8601(now));

        LOGGER.debug("end execute log and alarm send task, this time send {} data.",
            messageList.size());
      }
    }
    globalSettingService.setValue(WebappConstants.GLOBAL_SETTING_SYSLOG_SENDUP_LATEST_TIME,
        JsonHelper.serialize(lastSendupTimeMap));
  }

  private UdpSyslogMessageSender initSyslogUdpClient(String ipAddress, int port) {
    // 关闭连接
    if (udpSyslogMessageSender != null) {
      try {
        udpSyslogMessageSender.close();
      } catch (IOException e) {
        LOGGER.warn("syslogUDP客户端初始化失败.", e);
      }
    }

    // 初始化udp
    UdpSyslogMessageSender messageSender = new UdpSyslogMessageSender();
    messageSender.setDefaultMessageHostname(ipAddress);
    messageSender.setSyslogServerHostname(ipAddress);
    messageSender.setSyslogServerPort(port);
    messageSender.setMessageFormat(MessageFormat.RFC_5424);

    return messageSender;
  }

  private TcpSyslogMessageSender initSyslogTcpClient(String ipAddress, int port) {
    // 关闭连接
    if (tcpSyslogMessageSender != null) {
      try {
        tcpSyslogMessageSender.close();
      } catch (IOException e) {
        LOGGER.warn("syslogTCP客户端初始化失败.", e);
      }
    }

    // 初始化udp
    TcpSyslogMessageSender messageSender = new TcpSyslogMessageSender();
    messageSender.setDefaultMessageHostname(ipAddress);
    messageSender.setSyslogServerHostname(ipAddress);
    messageSender.setSyslogServerPort(port);
    messageSender.setMessageFormat(MessageFormat.RFC_5424);

    return messageSender;
  }


  private List<Map<String, Object>> updateSyslogClientAndLastSendupTime(
      List<Map<String, Object>> sendSyslogList, Map<String, Object> lastSendupTimeMap) {

    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    for (Map<String, Object> sendSyslog : sendSyslogList) {
      if (StringUtils.isAllBlank(MapUtils.getString(sendSyslog, "systemAlarmContent"),
          MapUtils.getString(sendSyslog, "systemLogContent"))) {
        continue;
      }
      String[] server = MapUtils.getString(sendSyslog, "syslogServerAddress").split(":");
      String host = server[0];
      int port = Integer.parseInt(server[1]);
      String protocol = MapUtils.getString(sendSyslog, "protocol");
      if (StringUtils.equals(protocol, "TCP")) {
        TcpSyslogMessageSender tcpSyslogClient = syslogTcpClientMap.get(host + port);
        if (tcpSyslogClient == null
            || !StringUtils.equals(tcpSyslogClient.getSyslogServerHostname(), host)
            || port != tcpSyslogClient.getSyslogServerPort()) {
          TcpSyslogMessageSender newTcpSyslogClient = initSyslogTcpClient(host, port);
          syslogTcpClientMap.put(host + port, newTcpSyslogClient);
        }
      } else if (StringUtils.equals(protocol, "UDP")) {
        UdpSyslogMessageSender udpSyslogClient = syslogUdpClientMap.get(host + port);
        if (udpSyslogClient == null
            || !StringUtils.equals(udpSyslogClient.getSyslogServerHostname(), host)
            || port != udpSyslogClient.getSyslogServerPort()) {
          UdpSyslogMessageSender newUdpSyslogClient = initSyslogUdpClient(host, port);
          syslogUdpClientMap.put(host + port, newUdpSyslogClient);
        }
      }
      result.add(sendSyslog);
    }
    List<String> currentSyslogIds = sendSyslogList.stream()
        .map(map -> MapUtils.getString(map, "syslogServerAddress").replace(":", ""))
        .collect(Collectors.toList());
    List<String> deletedUdpIds = syslogUdpClientMap.keySet().stream()
        .filter(e -> !currentSyslogIds.contains(e)).collect(Collectors.toList());
    for (String deleteId : deletedUdpIds) {
      syslogUdpClientMap.remove(deleteId);
      lastSendupTimeMap.remove(deleteId);
    }
    List<String> deletedTcpIds = syslogTcpClientMap.keySet().stream()
        .filter(e -> !currentSyslogIds.contains(e)).collect(Collectors.toList());
    for (String deleteId : deletedTcpIds) {
      syslogTcpClientMap.remove(deleteId);
      lastSendupTimeMap.remove(deleteId);
    }

    return result;
  }
}
