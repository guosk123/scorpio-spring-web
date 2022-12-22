package com.machloop.fpc.cms.center.system.service.impl;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.metric.system.helper.MonitorSystemHelper;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.service.DeviceNtpService;
import com.machloop.fpc.cms.center.system.service.SystemMetricService;

/**
 * @author guosk
 *
 * create at 2021年11月4日, fpc-cms-center
 */
@Service
public class SystemMetricServiceImpl implements SystemMetricService {

  @Autowired
  private DeviceNtpService deviceNtpService;

  @Autowired
  private GlobalSettingService globalSettingService;

  /**
   * @see com.machloop.fpc.cms.center.system.service.SystemMetricService#queryRuntimeEnvironment()
   */
  @Override
  public Map<String, Object> queryRuntimeEnvironment() {
    Map<String, Object> restMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    restMap.put("uptime", MonitorSystemHelper.fetchSystemRuntimeSecond());

    Map<String, String> timeMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    timeMap.put("dateTime", deviceNtpService.queryDeviceNtp().getDateTime());
    timeMap.put("timeZone", deviceNtpService.queryDeviceNtp().getTimeZone());
    restMap.put("systemTime", timeMap);

    restMap.put("systemFsUsedRatio", MonitorSystemHelper
        .fetchFilesystemUsagePctLong(HotPropertiesHelper.getProperty("filesys.mount.root.system")));
    restMap.put("indexFsUsedRatio", MonitorSystemHelper
        .fetchFilesystemUsagePctLong(HotPropertiesHelper.getProperty("filesys.mount.root.index")));
    restMap.put("metadataFsUsedRatio", MonitorSystemHelper.fetchFilesystemUsagePctLong(
        HotPropertiesHelper.getProperty("filesys.mount.root.metadata")));

    return restMap;
  }

  /**
   * @see com.machloop.fpc.cms.center.system.service.SystemMetricService#queryDeviceCustomInfo()
   */
  @Override
  public Map<String, Object> queryDeviceCustomInfo() {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String deviceId = globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_ID);
    if (StringUtils.isBlank(deviceId)) {
      deviceId = IdGenerator.generateUUID().replace("-", "_");
      globalSettingService.setValue(WebappConstants.GLOBAL_SETTING_DEVICE_ID, deviceId);
    }
    result.put(WebappConstants.GLOBAL_SETTING_DEVICE_ID, deviceId);
    result.put(WebappConstants.GLOBAL_SETTING_DEVICE_NAME,
        globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME, false));

    return result;
  }

}
