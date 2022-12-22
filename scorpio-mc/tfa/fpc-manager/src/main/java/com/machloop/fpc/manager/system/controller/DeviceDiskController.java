package com.machloop.fpc.manager.system.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.manager.system.bo.DeviceDiskBO;
import com.machloop.fpc.manager.system.service.DeviceDiskService;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class DeviceDiskController {

  @Autowired
  private DeviceDiskService deviceDiskService;

  @GetMapping("/device-disks")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryDeviceDisks() {

    List<DeviceDiskBO> deviceDiskList = deviceDiskService.queryDeviceDisks();
    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(deviceDiskList.size());
    for (DeviceDiskBO deviceDisk : deviceDiskList) {
      Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      resultMap.put("deviceId", deviceDisk.getDeviceId());
      resultMap.put("slotNo", deviceDisk.getSlotNo());
      resultMap.put("physicalLocation", deviceDisk.getPhysicalLocation());
      resultMap.put("raidNo", deviceDisk.getRaidNo());
      resultMap.put("raidLevel", deviceDisk.getRaidLevel());
      resultMap.put("state", deviceDisk.getState());
      resultMap.put("stateText", deviceDisk.getStateText());
      resultMap.put("medium", deviceDisk.getMedium());
      resultMap.put("mediumText", deviceDisk.getMediumText());
      resultMap.put("capacity", deviceDisk.getCapacity());
      resultMap.put("capacityText", deviceDisk.getCapacityText());
      resultMap.put("rebuildProgress", deviceDisk.getRebuildProgress());
      resultMap.put("rebuildProgressText", deviceDisk.getRebuildProgressText());
      resultMap.put("copybackProgress", deviceDisk.getCopybackProgress());
      resultMap.put("copybackProgressText", deviceDisk.getCopybackProgressText());
      resultMap.put("description", deviceDisk.getDescription());

      resultList.add(resultMap);
    }

    return resultList;
  }
}
