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
 * create at 2019年11月20日, fpc-manager
 */
@Component
public class ResendMessageTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResendMessageTask.class);

  private static final int DAY_AMOUNT_ONE = 1;

  @Autowired
  private SendupMessageService sendupMessageService;

  @Autowired
  private RegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Scheduled(cron = "${task.resend.schedule.cron}")
  public void run() {
    LOGGER.debug("start sendup message.");

    // 开关控制
    if (StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE, false))) {
      LOGGER.debug("cms swtich is off.");
      return;
    }

    String cmsIp = registryHeartbeatService.getCmsIp();
    if (StringUtils.isBlank(cmsIp)) {
      LOGGER.debug("cmsIp is empty, end sendup message, cmsIp is {}.", cmsIp);
      return;
    }

    // 心跳异常将不进行补报操作
    if (!registryHeartbeatService.isAlive()) {
      LOGGER.debug("abnormal heartbeat, stop resend message.");
      return;
    }

    String serialNumber = registryHeartbeatService.getSerialNumber();
    synchronized (this) {
      // 获取当前时间
      Date currentDate = DateUtils.now();

      // 补报系统状态
      /*sendupMessageService.resendMessage(serialNumber, FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC,
          currentDate);*/
      // 补报日志告警
      sendupMessageService.resendMessage(serialNumber, FpcCmsConstants.RESEND_TYPE_LOG_ALARM,
          currentDate);
    }

    // 删除一天前上报统计表中的数据
    sendupMessageService
        .deleteExpireSendupMessage(DateUtils.beforeDayDate(DateUtils.now(), DAY_AMOUNT_ONE));
  }
}
