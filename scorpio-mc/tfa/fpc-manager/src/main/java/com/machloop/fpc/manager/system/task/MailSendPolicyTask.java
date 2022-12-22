package com.machloop.fpc.manager.system.task;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.alpha.webapp.system.service.MailService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.SmtpConfigurationBO;
import com.machloop.fpc.manager.appliance.dao.ExternalReceiverDao;
import com.machloop.fpc.manager.appliance.dao.SendPolicyDao;
import com.machloop.fpc.manager.appliance.dao.SendRuleDao;
import com.machloop.fpc.manager.appliance.data.ExternalReceiverDO;
import com.machloop.fpc.manager.appliance.data.SendPolicyDO;
import com.machloop.fpc.manager.appliance.data.SendRuleDO;
import com.machloop.fpc.manager.appliance.service.SmtpConfigurationService;
import com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskPolicyDao;
import com.machloop.fpc.npm.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskPolicyDO;

/**
 * @author ChenXiao
 * create at 2022/9/2
 */
@Component
public class MailSendPolicyTask {


  private static final Logger LOGGER = LoggerFactory.getLogger(MailSendPolicyTask.class);

  @Autowired
  private SmtpConfigurationService smtpConfigurationService;

  @Autowired
  private MailService mailService;

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


  // Map<邮件配置id, 上次发送时间>
  private Map<String, Date> sendMail = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  private Date lastSendFailTime = null;


  @Scheduled(cron = "${task.system.send.mail.schedule.cron}")
  public void run() {

    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPoliciesStateOn();
    List<SendPolicyDO> sendMailPolicyList = sendPolicyDOList.stream().filter(sendPolicyDO -> {
      ExternalReceiverDO externalReceiverDO = externalReceiverDao
          .queryExternalReceiver(sendPolicyDO.getExternalReceiverId());
      String receiverType = externalReceiverDO.getReceiverType();
      return StringUtils.equals(receiverType, FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL);
    }).collect(Collectors.toList());
    // 过滤掉没有被网络和离线任务引用的邮件相关的外发策略，与引擎端保持一致
    List<String> policyIdsOfNetworkPolicy = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND).stream()
        .map(NetworkPolicyDO::getPolicyId).collect(Collectors.toList());
    List<String> policyIdsOfPacketAnalysisTaskPolicy = packetAnalysisTaskPolicyDao
        .queryPolicyIdsByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND).stream()
        .map(PacketAnalysisTaskPolicyDO::getPolicyId).collect(Collectors.toList());
    sendMailPolicyList = sendMailPolicyList.stream()
        .filter(sendPolicyDO -> policyIdsOfNetworkPolicy.contains(sendPolicyDO.getId())
            || policyIdsOfPacketAnalysisTaskPolicy.contains(sendPolicyDO.getId()))
        .collect(Collectors.toList());
    MimeMessage message = getSmtpConfiguration();
    if (!sendMailPolicyList.isEmpty()) {
      if (message == null) {
        LOGGER.warn("smtp configuration does not exist.");
        return;
      }
    }
    List<Map<String, Object>> sendMailList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    sendMailPolicyList.forEach(sendPolicyDO -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("id", sendPolicyDO.getId());
      ExternalReceiverDO externalReceiverDO = externalReceiverDao
          .queryExternalReceiver(sendPolicyDO.getExternalReceiverId());
      SendRuleDO sendRuleDO = sendRuleDao.querySendRule(sendPolicyDO.getSendRuleId());
      Map<String, Object> receiverContent = JsonHelper.deserialize(
          externalReceiverDO.getReceiverContent(), new TypeReference<Map<String, Object>>() {
          }, false);
      temp.put("mailTitle", MapUtils.getString(receiverContent, "mailTitle"));
      temp.put("receiver", MapUtils.getString(receiverContent, "receiver"));
      temp.put("cc", MapUtils.getString(receiverContent, "cc"));
      temp.put("bcc", MapUtils.getString(receiverContent, "bcc"));
      String networkAlertContent = null, serviceAlertContent = null, systemAlarmContent = null,
          systemLogContent = null;
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
        if (StringUtils.equals(MapUtils.getString(sendRule, "index"), "alert")) {
          if (StringUtils.contains(MapUtils.getString(sendRule, "filter_info"), "network")) {
            networkAlertContent = propertiesContent;
          } else {
            serviceAlertContent = propertiesContent;
          }
        } else if (StringUtils.equals(MapUtils.getString(sendRule, "index"), "systemAlert")) {
          systemAlarmContent = propertiesContent;
        } else if (StringUtils.equals(MapUtils.getString(sendRule, "index"), "systemLog")) {
          systemLogContent = propertiesContent;
        }
      }
      temp.put("networkAlertContent", networkAlertContent);
      temp.put("serviceAlertContent", serviceAlertContent);
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
      sendMailList.add(temp);
    });

    for (Map<String, Object> sendMailRule : sendMailList) {

      Date now = DateUtils.now();
      double multipleOfInterval = ((double) now.getTime() / 1000) / (double) Constants.FIVE_SECONDS;
      long roundValue = Math.round(multipleOfInterval);
      now = new Date(roundValue * Constants.FIVE_SECONDS * 1000);

      Date sendTime = (Date) MapUtils.getObject(sendMail, MapUtils.getString(sendMailRule, "id"));
      long actualInterval = 0L;
      long interval = 0L;
      if (sendTime != null) {
        actualInterval = now.getTime() - sendTime.getTime();
        Integer intervalNum = MapUtils.getInteger(sendMailRule, "interval");
        interval = intervalNum == 0 ? Constants.FIVE_SECONDS
            : intervalNum * Constants.ONE_MINUTE_SECONDS * 1000;
      }
      if (sendTime == null || actualInterval >= interval) {
        try {
          // 拼接收件人
          List<String> receivers = CsvUtils
              .convertCSVToList(MapUtils.getString(sendMailRule, "receiver"));
          InternetAddress[] to = new InternetAddress[receivers.size()];
          for (int i = 0; i < receivers.size(); i++) {
            to[i] = new InternetAddress(receivers.get(i));
          }
          message.setRecipients(MimeMessage.RecipientType.TO, to);

          // 拼接抄送人
          if (StringUtils.isNotBlank(MapUtils.getString(sendMailRule, "cc"))) {
            List<String> ccs = CsvUtils.convertCSVToList(MapUtils.getString(sendMailRule, "cc"));
            InternetAddress[] cc = new InternetAddress[ccs.size()];
            for (int i = 0; i < ccs.size(); i++) {
              cc[i] = new InternetAddress(ccs.get(i));
            }
            message.setRecipients(MimeMessage.RecipientType.CC, cc);
          }

          // 拼接密送人
          if (StringUtils.isNotBlank(MapUtils.getString(sendMailRule, "bcc"))) {
            List<String> bccs = CsvUtils.convertCSVToList(MapUtils.getString(sendMailRule, "bcc"));
            InternetAddress[] bcc = new InternetAddress[bccs.size()];
            for (int i = 0; i < bccs.size(); i++) {
              bcc[i] = new InternetAddress(bccs.get(i));
            }
            message.setRecipients(MimeMessage.RecipientType.BCC, bcc);
          }

          // 设置邮件主题
          message.setSubject(MapUtils.getString(sendMailRule, "mailTitle"));

          // 拼接发送内容
          Date startTime = new Date();
          Date endTime = new Date();
          Integer intervalNum = MapUtils.getInteger(sendMailRule, "interval");
          long intervalTime = intervalNum == 0 ? Constants.FIVE_SECONDS
              : intervalNum * Constants.ONE_MINUTE_SECONDS * 1000;
          if (sendTime == null) {
            endTime = now;
            long startTimeNum = endTime.getTime() - intervalTime;
            startTime = new Date(startTimeNum);
          } else {
            startTime = sendTime;
            long endTimeNum = sendTime.getTime() + intervalTime;
            endTime = new Date(endTimeNum);
          }
          StringBuilder sendupContent = new StringBuilder();
          sendupContent.append(mailService.collectSystemMessage(startTime, endTime,
              MapUtils.getString(sendMailRule, "systemAlarmContent"),
              MapUtils.getString(sendMailRule, "systemLogContent")));
          sendupContent.append(mailService.collectAlertMessage(startTime, endTime,
              MapUtils.getString(sendMailRule, "networkAlertContent"),
              MapUtils.getString(sendMailRule, "serviceAlertContent")));
          String mailSendUpContent = sendupContent.toString();
          if (interval == Constants.FIVE_SECONDS && StringUtils.isBlank(mailSendUpContent)) {
            continue;
          }
          message.setContent(mailSendUpContent, "text/html;charset=UTF-8");
          Transport.send(message);
        } catch (MessagingException e) {
          String exceptionMessage = e.getMessage();
          if (lastSendFailTime == null || lastSendFailTime.getTime()
              + Constants.ONE_HOUR_SECONDS * 1000 < System.currentTimeMillis()) {
            AlarmHelper.alarm(AlarmHelper.LEVEL_NORMAL, AlarmHelper.CATEGORY_ARCHIVE, "send_action",
                "邮件发送失败，原因是：" + exceptionMessage);
            LOGGER.warn("email sending failed.", e);
            lastSendFailTime = DateUtils.now();
          }
        }
        sendMail.put(MapUtils.getString(sendMailRule, "id"), now);
      }
    }

  }

  private MimeMessage getSmtpConfiguration() {

    SmtpConfigurationBO smtpConfiguration = smtpConfigurationService.querySmtpConfigurations();
    if (StringUtils.isBlank(smtpConfiguration.getId())) {
      return null;
    }
    final Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.host", smtpConfiguration.getSmtpServer());
    props.put("mail.user", smtpConfiguration.getLoginUser());
    props.put("mail.password", smtpConfiguration.getLoginPassword());
    if (StringUtils.equals(smtpConfiguration.getEncrypt(), Constants.BOOL_YES)) {
      props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
      props.put("mail.smtp.socketFactory.port", smtpConfiguration.getServerPort());
    }
    props.put("mail.smtp.port", smtpConfiguration.getServerPort());

    // 构建授权信息，用于进行SMTP身份验证
    Authenticator authenticator = new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        String userName = props.getProperty("mail.user");
        String password = props.getProperty("mail.password");
        return new PasswordAuthentication(userName, password);
      }
    };
    Session mailSession = Session.getInstance(props, authenticator);
    MimeMessage message = new MimeMessage(mailSession);
    InternetAddress from;
    try {
      from = new InternetAddress(props.getProperty("mail.user"));
      message.setFrom(from);
    } catch (MessagingException e) {
      LOGGER.warn("smtp configuration error.", e);
    }
    return message;
  }

}
