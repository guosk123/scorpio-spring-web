package com.machloop.fpc.cms.center.broker.task.local;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.bo.DeviceNtpBO;
import com.machloop.alpha.webapp.system.service.DeviceNtpService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.service.local.MQReceiveService;
import com.machloop.fpc.cms.center.broker.service.local.SendupMessageService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.helper.RemoteRocketMQHelper;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * 
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
@Component
public class SyncConfigurationTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyncConfigurationTask.class);

  @Autowired
  private MQReceiveService assignmentService;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private DeviceNtpService deviceNtpService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private SendupMessageService sendupMessageService;

  @Autowired
  private RemoteRocketMQHelper rocketMQHelper;

  @Scheduled(cron = "${task.sync.configuration.schedule.cron}")
  public void run() {
    // 开关控制
    String cmsState = globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_STATE);
    String parentCmsIp = globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_IP);
    if (StringUtils.equals(cmsState, Constants.BOOL_NO) || StringUtils.isBlank(parentCmsIp)) {
      // 未注册到cms
      LOGGER.debug("cms swtich is off or parentCmsIp is null.");
      return;
    }

    Date signatureTime = DateUtils.now();

    // 当前已包含的下发配置，MD5签名
    Map<String,
        List<String>> assignConfiguration = assignmentService.getAssignConfiguration(signatureTime);

    Map<String, Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    messageBody.put("deviceType", FpcCmsConstants.DEVICE_TYPE_CMS);
    messageBody.put("serialNumber", licenseService.queryDeviceSerialNumber());
    messageBody.put("signatureTime", signatureTime.getTime());
    messageBody.put("signature", JsonHelper.serialize(assignConfiguration));

    Message message = MQMessageHelper.convertToMessage(messageBody,
        FpcCmsConstants.MQ_TOPIC_CMS_SENDUP, FpcCmsConstants.MQ_TAG_SIGNATURE);
    SendResult sendMsg = rocketMQHelper.sendMsg(message);
    LOGGER.info("send signature result: " + sendMsg);

    // 上报全部业务基础应用
    sendupMessageService.sendAllApplianceMessage(licenseService.queryDeviceSerialNumber(),
        signatureTime);

    // 检测并配置上级CMS为ntp服务器
    DeviceNtpBO deviceNtpBO = deviceNtpService.queryDeviceNtp();
    if (!deviceNtpBO.isNtpEnabled()
        || !StringUtils.equals(deviceNtpBO.getNtpServer(), parentCmsIp)) {
      deviceNtpBO.setNtpEnabled(true);
      deviceNtpBO.setNtpServer(parentCmsIp);
      deviceNtpService.updateDeviceNtp(deviceNtpBO);
    }
  }

}
