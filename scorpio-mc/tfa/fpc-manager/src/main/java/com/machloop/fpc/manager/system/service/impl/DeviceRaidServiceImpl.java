package com.machloop.fpc.manager.system.service.impl;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.metric.system.data.MonitorRaid;
import com.machloop.alpha.common.metric.system.helper.MonitorRaidDiskHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.system.service.DeviceRaidService;

@Service
public class DeviceRaidServiceImpl implements DeviceRaidService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeviceRaidServiceImpl.class);

  private Date previousWarnTime;

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceRaidService#monitorRaidState()
   */
  @Override
  public List<MonitorRaid> monitorRaidState() {
    String megacliPath = HotPropertiesHelper.getProperty("file.megacli64.path");
    if (!Paths.get(megacliPath).toFile().exists()) {
      Date now = DateUtils.now();
      if (previousWarnTime == null
          || now.after(DateUtils.afterSecondDate(previousWarnTime, Constants.ONE_MINUTE_SECONDS))) {
        LOGGER.warn("megacli64 file not found: {}", megacliPath);
        previousWarnTime = now;
      }

      return Lists.newArrayList();
    }

    // 获取RAID信息
    Map<String, MonitorRaid> deviceRaidMap = MonitorRaidDiskHelper.fetchRaidInfo(megacliPath);

    List<MonitorRaid> monitorRaidList = Lists.newArrayListWithCapacity(deviceRaidMap.size());
    for (MonitorRaid monitorRaid : deviceRaidMap.values()) {
      monitorRaidList.add(monitorRaid);
    }

    return monitorRaidList;
  }


}
