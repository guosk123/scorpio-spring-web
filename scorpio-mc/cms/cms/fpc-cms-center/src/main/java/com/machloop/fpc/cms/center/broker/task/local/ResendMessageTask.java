package com.machloop.fpc.cms.center.broker.task.local;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService;
import com.machloop.fpc.cms.center.broker.service.local.SendupMessageService;
import com.machloop.fpc.cms.center.central.dao.CmsDao;
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年12月16日, fpc-cms-center
 */
@Component
public class ResendMessageTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResendMessageTask.class);

  private static final int DAY_AMOUNT_ONE = 1;

  @Autowired
  private SendupMessageService sendupMessageService;

  @Autowired
  private LocalRegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private CmsDao cmsDao;

  @Scheduled(cron = "${task.resend.schedule.cron}")
  public void run() {
    LOGGER.debug("start sendup message.");

    // 开关控制
    if (StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_STATE, false))) {
      LOGGER.debug("cms swtich is off.");
      return;
    }

    String parentCmsIp = registryHeartbeatService.getParentCmsIp();
    if (StringUtils.isBlank(parentCmsIp)) {
      LOGGER.debug("parentCmsIp is empty, end sendup message.");
      return;
    }

    // 心跳异常将不进行补报操作
    if (!registryHeartbeatService.isAlive()) {
      LOGGER.debug("abnormal heartbeat, stop resend message.");
      return;
    }

    List<Tuple2<String, String>> deviceSerialNumbers = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    fpcDao.queryFpcs(new FpcQueryVO()).forEach(fpc -> deviceSerialNumbers
        .add(Tuples.of(FpcCmsConstants.DEVICE_TYPE_TFA, fpc.getSerialNumber())));
    cmsDao.queryCms(new CmsQueryVO()).forEach(cms -> deviceSerialNumbers
        .add(Tuples.of(FpcCmsConstants.DEVICE_TYPE_CMS, cms.getSerialNumber())));
    deviceSerialNumbers.add(
        Tuples.of(FpcCmsConstants.DEVICE_TYPE_CMS, registryHeartbeatService.getSerialNumber()));
    synchronized (this) {
      // 获取当前时间
      Date currentDate = DateUtils.now();

      deviceSerialNumbers.forEach(deviceSerialNumber -> {

        // 补报系统状态
        sendupMessageService.resendMessage(deviceSerialNumber.getT1(), deviceSerialNumber.getT2(),
            FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC, currentDate);
        // 补报日志告警
        sendupMessageService.resendMessage(deviceSerialNumber.getT1(), deviceSerialNumber.getT2(),
            FpcCmsConstants.RESEND_TYPE_LOG_ALARM, currentDate);
      });
    }

    // 删除一天前上报统计表中的数据
    sendupMessageService
        .deleteExpireSendupMessage(DateUtils.beforeDayDate(DateUtils.now(), DAY_AMOUNT_ONE));
  }
}
