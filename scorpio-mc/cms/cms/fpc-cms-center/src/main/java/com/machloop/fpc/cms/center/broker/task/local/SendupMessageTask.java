package com.machloop.fpc.cms.center.broker.task.local;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService;
import com.machloop.fpc.cms.center.broker.service.local.SendupMessageService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
@Component
public class SendupMessageTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendupMessageTask.class);

  @Autowired
  private SendupMessageService sendupMessageService;

  @Autowired
  private LocalRegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Scheduled(fixedRateString = "${task.sendup.schedule.fixedrate.ms}")
  public void run() {

    LOGGER.debug("start sendup cms message.");

    // 开关控制
    if (StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_STATE, false))) {
      LOGGER.debug("cms swtich is off.");
      return;
    }

    // 若parentCmsIp为空将不进行心跳
    String parentCmsIp = registryHeartbeatService.getParentCmsIp();
    String serialNumber = registryHeartbeatService.getSerialNumber();
    if (StringUtils.isBlank(parentCmsIp)) {
      LOGGER.debug("parentCmsIp is empty, end sendup cms message.");
      return;
    }

    synchronized (this) {
      // 获取当前时间
      Date metricDatetime = DateUtils.now();

      // 上报系统信息
      sendupMessageService.sendupMessage(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber,
          FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC, metricDatetime, Constants.ONE_MINUTE_SECONDS,
          Constants.FIVE_SECONDS * 2);

      // 上报日志告警
      sendupMessageService.sendupMessage(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber,
          FpcCmsConstants.SENDUP_TYPE_LOG_ALARM, metricDatetime, Constants.FIVE_SECONDS, 0);

      // 上报网络信息
      sendupMessageService.sendupMessage(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber,
          FpcCmsConstants.SENDUP_TYPE_NETWORK, metricDatetime, Constants.FIVE_SECONDS, 0);

      // 上报本机设备管理的探针设备信息
      sendupMessageService.sendupMessage(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber,
          FpcCmsConstants.SENDUP_TYPE_SENSOR, metricDatetime, Constants.FIVE_SECONDS, 0);

      // 上报本机设备管理的CMS设备信息
      sendupMessageService.sendupMessage(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber,
          FpcCmsConstants.SENDUP_TYPE_CMS, metricDatetime, Constants.FIVE_SECONDS, 0);
    }

    LOGGER.debug("end sendup cms message.");
  }
}
