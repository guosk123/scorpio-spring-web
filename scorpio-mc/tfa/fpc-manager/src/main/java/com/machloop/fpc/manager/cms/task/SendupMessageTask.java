package com.machloop.fpc.manager.cms.task;

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
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.RegistryHeartbeatService;
import com.machloop.fpc.manager.cms.service.SendupMessageService;

/**
 * @author liyongjun
 *
 * create at 2019年12月4日, fpc-manager
 */
@Component
public class SendupMessageTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendupMessageTask.class);

  @Autowired
  private SendupMessageService sendupMessageService;

  @Autowired
  private RegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Scheduled(fixedRateString = "${task.sendup.schedule.fixedrate.ms}")
  public void run() {

    LOGGER.debug("start sendup message.");

    // 开关控制
    if (StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE, false))) {
      LOGGER.debug("cms swtich is off.");
      return;
    }

    // 数据库中cmsIp和fpcId都不为空进行上报
    String cmsIp = registryHeartbeatService.getCmsIp();
    if (StringUtils.isBlank(cmsIp)) {
      LOGGER.debug("cmsIp is empty, end sendup message.");
      return;
    }

    String serialNumber = registryHeartbeatService.getSerialNumber();
    synchronized (this) {
      // 获取当前时间
      Date metricDatetime = DateUtils.now();

      // 上报系统信息
      sendupMessageService.sendupMessage(serialNumber, FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC,
          metricDatetime);

      // 上报日志告警
      sendupMessageService.sendupMessage(serialNumber, FpcCmsConstants.SENDUP_TYPE_LOG_ALARM,
          metricDatetime);

      // 上报网络信息
      sendupMessageService.sendupMessage(serialNumber, FpcCmsConstants.SENDUP_TYPE_NETWORK,
          metricDatetime);
    }

    LOGGER.debug("end sendup message.");
  }
}
