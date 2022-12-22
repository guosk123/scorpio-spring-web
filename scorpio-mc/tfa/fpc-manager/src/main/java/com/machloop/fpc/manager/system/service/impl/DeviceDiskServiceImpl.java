package com.machloop.fpc.manager.system.service.impl;

import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.metric.MetricConstants;
import com.machloop.alpha.common.metric.system.data.MonitorRaidDisk;
import com.machloop.alpha.common.metric.system.helper.MonitorRaidDiskHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.UnitConversionUtils;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.fpc.manager.system.bo.DeviceDiskBO;
import com.machloop.fpc.manager.system.dao.DeviceDiskDao;
import com.machloop.fpc.manager.system.data.DeviceDiskDO;
import com.machloop.fpc.manager.system.service.DeviceDiskService;

/**
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
@Service
public class DeviceDiskServiceImpl implements DeviceDiskService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeviceDiskServiceImpl.class);

  @Autowired
  private DeviceDiskDao deviceDiskDao;

  @Autowired
  private DictManager dictManager;

  private final Map<String,
      Date> lastAlarmDiskMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
  private final Map<String,
      Date> lastReportDiskMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceDiskService#queryDeviceDisks()
   */
  @Override
  public List<DeviceDiskBO> queryDeviceDisks() {

    Map<String, String> stateDict = dictManager.getBaseDict().getItemMap("device_disk_state");
    Map<String, String> mediumDict = dictManager.getBaseDict().getItemMap("device_disk_medium");

    List<DeviceDiskDO> deviceDiskDOList = deviceDiskDao.queryDeviceDisks();

    List<DeviceDiskBO> deviceDiskBOList = Lists.newArrayListWithCapacity(deviceDiskDOList.size());
    for (DeviceDiskDO deviceDiskDO : deviceDiskDOList) {
      DeviceDiskBO deviceDiskBO = new DeviceDiskBO();
      BeanUtils.copyProperties(deviceDiskDO, deviceDiskBO);

      deviceDiskBO.setStateText(MapUtils.getString(stateDict, deviceDiskBO.getState(), ""));
      deviceDiskBO.setMediumText(MapUtils.getString(mediumDict, deviceDiskBO.getMedium(), ""));
      deviceDiskBO.setCapacityText(deviceDiskBO.getCapacity());
      deviceDiskBO
          .setRebuildProgressText(StringUtils.isBlank(deviceDiskBO.getRebuildProgress()) ? "N/A"
              : deviceDiskBO.getRebuildProgress());
      deviceDiskBO
          .setCopybackProgressText(StringUtils.isBlank(deviceDiskBO.getCopybackProgress()) ? "N/A"
              : deviceDiskBO.getCopybackProgress());
      deviceDiskBOList.add(deviceDiskBO);
    }
    return deviceDiskBOList;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceDiskService#monitorDiskState()
   */
  @Override
  public int monitorDiskState() {
    int update = 0;

    // 获取热备盘位置
    String hotSparePosition = HotPropertiesHelper.getProperty("device.hot.spare.position");
    List<String> hotSpareList = CsvUtils.convertCSVToList(hotSparePosition);

    String cmdDiskRaid = HotPropertiesHelper.getProperty("file.megacli64.path");
    if (!Paths.get(cmdDiskRaid).toFile().exists()) {
      LOGGER.debug("megacli64 file not found: {}", cmdDiskRaid);
      return update;
    }

    // 提取磁盘RAID组信息
    Map<String, String> deviceRaidMap = MonitorRaidDiskHelper.fetchRaidLevelInfo(cmdDiskRaid);

    // 提取磁盘状态信息
    Map<String, MonitorRaidDisk> deviceDiskMap = MonitorRaidDiskHelper.fetchDiskInfo(cmdDiskRaid);

    // 更新磁盘最新上报时间
    lastReportDiskMap.putAll(
        deviceDiskMap.keySet().stream().collect(Collectors.toMap(key -> key, key -> new Date())));

    // 遍历磁盘状态信息，进一步提取其他信息或产生告警
    for (MonitorRaidDisk monitorDisk : deviceDiskMap.values()) {
      DeviceDiskDO deviceDisk = new DeviceDiskDO();
      BeanUtils.copyProperties(monitorDisk, deviceDisk);
      deviceDisk.setMedium(monitorDisk.getMediaType());
      deviceDisk.setCapacity(monitorDisk.getSize().trim());

      // 根据raidNo查找raidLevel
      deviceDisk.setRaidLevel(MapUtils.getString(deviceRaidMap, monitorDisk.getRaidNo(), ""));

      // 如果提取状态为online或hotspare，需要进一步判断MediaErrorCount、PredictiveFailureCount
      // 错误信息>0，状态显示为“错误”，并产生告警（一小时一条）
      // 忽略OtherErrorCount
      int errorCount = monitorDisk.getMediaErrorCount() + monitorDisk.getPredictiveFailureCount();
      if ((StringUtils.equals(monitorDisk.getState(), MetricConstants.DEVICE_DISK_STATE_ONLINE)
          || StringUtils.equals(monitorDisk.getState(), MetricConstants.DEVICE_DISK_STATE_HOTSPARE))
          && errorCount > 0) {

        String errorMsg = "MediaErrorCount=" + monitorDisk.getMediaErrorCount()
            + " OtherErrorCount=" + monitorDisk.getOtherErrorCount() + " PredictiveFailureCount="
            + monitorDisk.getPredictiveFailureCount();
        deviceDisk.setDescription(errorMsg);

        // 建议用户更换对应槽位的磁盘
        Date lastAlarmTime = lastAlarmDiskMap.get(monitorDisk.getSlotNo());
        if (lastAlarmTime == null || lastAlarmTime.getTime()
            + Constants.ONE_HOUR_SECONDS * 1000 < System.currentTimeMillis()) {
          AlarmHelper.alarm(AlarmHelper.LEVEL_IMPORTANT, AlarmHelper.CATEGORY_SERVER_RESOURCE,
              monitorDisk.getSlotNo(),
              "槽位号：" + monitorDisk.getSlotNo() + "的磁盘存在错误，具体信息为：" + errorMsg);
        }
        lastAlarmDiskMap.put(monitorDisk.getSlotNo(), DateUtils.now());
      }

      // 如果提取状态为Rebuild，需要进一步用命令查询重建进度
      if (StringUtils.equals(monitorDisk.getState(), MetricConstants.DEVICE_DISK_STATE_REBUILD)) {
        String progress = MonitorRaidDiskHelper.fetchRebuildProgress(cmdDiskRaid,
            monitorDisk.getDeviceId(), monitorDisk.getSlotNo());
        deviceDisk.setRebuildProgress(progress);
      } else {
        deviceDisk.setRebuildProgress("");
      }

      // 如果提取状态为Copyback，需要进一步用命令查询回拷进度
      if (StringUtils.equals(deviceDisk.getState(), MetricConstants.DEVICE_DISK_STATE_COPYBACK)) {
        String progress = MonitorRaidDiskHelper.fetchCopybackProgress(cmdDiskRaid,
            deviceDisk.getDeviceId(), deviceDisk.getSlotNo());
        deviceDisk.setCopybackProgress(progress);
      } else {
        deviceDisk.setCopybackProgress("");
      }

      // 检查盘的状态
      String diskPosition = deviceDisk.getDeviceId() + ":" + deviceDisk.getSlotNo();
      if (hotSpareList.contains(diskPosition)) {
        // 判断热备盘是否正常，异常需要恢复热备盘状态
        MonitorRaidDiskHelper.recoveryHotSpare(cmdDiskRaid, deviceDisk.getState(),
            deviceDisk.getForeignState(), deviceDisk.getDeviceId(), deviceDisk.getSlotNo());
      } else {
        // 判断数据盘是否正常，异常需要恢复数据盘状态
        MonitorRaidDiskHelper.recoveryDataDisk(cmdDiskRaid, deviceDisk.getState(),
            deviceDisk.getForeignState(), deviceDisk.getDeviceId(), deviceDisk.getSlotNo());
      }

      String capacity = deviceDisk.getCapacity();
      deviceDisk.setCapacity(UnitConversionUtils.converseCapacity(capacity));

      LOGGER.debug("device disk info is {}", deviceDisk);
      // 与当前数据库对比，saveOrUpdate数据库保存的磁盘状态
      update += deviceDiskDao.saveOrUpdateDeviceDisk(deviceDisk);
    }

    // 重置失效槽位状态
    List<DeviceDiskBO> diskBOList = queryDeviceDisks();
    for (DeviceDiskBO deviceDiskBO : diskBOList) {
      String deviceDiskKey = deviceDiskBO.getDeviceId() + "_" + deviceDiskBO.getSlotNo();
      if (!deviceDiskMap.containsKey(deviceDiskKey)) {
        if (lastReportDiskMap.getOrDefault(deviceDiskKey, new Date(0)).before(
            DateUtils.beforeSecondDate(DateUtils.now(), Constants.ONE_MINUTE_SECONDS * 30))) {
          // 超时移除磁盘信息
          update += deviceDiskDao.deleteDeviceDisk(deviceDiskBO.getDeviceId(),
              deviceDiskBO.getSlotNo());
        } else {
          DeviceDiskDO deviceDiskDO = new DeviceDiskDO();
          deviceDiskDO.setDeviceId(deviceDiskBO.getDeviceId());
          deviceDiskDO.setSlotNo(deviceDiskBO.getSlotNo());
          deviceDiskDO.setRaidNo("");
          deviceDiskDO.setRaidLevel("");
          deviceDiskDO.setMedium("");
          deviceDiskDO.setCapacity("");
          deviceDiskDO.setRebuildProgress("");
          deviceDiskDO.setCopybackProgress("");
          deviceDiskDO.setState(MetricConstants.DEVICE_DISK_STATE_FAILED);
          deviceDiskDO.setForeignState("");
          update += deviceDiskDao.saveOrUpdateDeviceDisk(deviceDiskDO);
        }
      }
    }
    return update;
  }

}
