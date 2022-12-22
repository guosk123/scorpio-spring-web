package com.machloop.fpc.manager.cms.task;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.service.DeviceNetifCallback;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.RegistryHeartbeatService;

import io.grpc.StatusRuntimeException;

/**
 * @author liyongjun
 *
 * create at 2019年12月2日, fpc-manager
 */
@Component
public class HeartbeatTask {
  private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatTask.class);

  @Autowired
  private RegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private GlobalSettingService globalSettingService;

  // init可能会调用获取管理口IP的方法，该方法依赖该接口，需要提前注入
  @SuppressWarnings("unused")
  @Autowired
  private DeviceNetifCallback deviceNetifCallback;

  @PostConstruct
  public void init() {
    registryHeartbeatService.init();
  }

  @Scheduled(fixedRateString = "${task.heartbeat.schedule.fixedrate.ms}")
  public void run() {
    LOGGER.debug("heartbeat start...");

    // 开关控制
    if (StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE, false))) {
      LOGGER.debug("cms swtich is off.");
      return;
    }

    // 若cmsIp为空将不进行心跳
    String cmsIp = registryHeartbeatService.getCmsIp();
    if (StringUtils.isBlank(cmsIp)) {
      LOGGER.debug("cmsIp is empty, end heartbeat, cmsIp is {}.", cmsIp);
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
