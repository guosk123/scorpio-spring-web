package com.machloop.fpc.cms.center.broker.task.local;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService;

import io.grpc.StatusRuntimeException;

/**
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
@Component
public class HeartbeatTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatTask.class);

  @Autowired
  private LocalRegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @PostConstruct
  public void init() {
    registryHeartbeatService.init();
  }

  @Scheduled(fixedRateString = "${task.heartbeat.schedule.fixedrate.ms}")
  public void run() {
    LOGGER.debug("heartbeat start...");

    // 开关控制
    if (StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_STATE, false))) {
      LOGGER.debug("cms swtich is off.");
      return;
    }

    // 若parentCmsIp为空将不进行心跳
    String parentCmsIp = registryHeartbeatService.getParentCmsIp();
    if (StringUtils.isBlank(parentCmsIp)) {
      LOGGER.debug("parentCmsIp is empty, end heartbeat, parentCmsIp is {}.", parentCmsIp);
      return;
    }

    try {
      registryHeartbeatService.heartbeat();
    } catch (StatusRuntimeException e) {
      LOGGER.warn("failed to connect the server." + e);
    }

    LOGGER.debug("heartbeat end...");
  }

}
