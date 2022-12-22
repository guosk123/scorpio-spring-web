package com.machloop.fpc.cms.center.central.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.cms.center.central.bo.CentralDiskBO;
import com.machloop.fpc.cms.center.central.bo.CentralRaidBO;
import com.machloop.fpc.cms.center.central.service.CentralDiskService;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/central")
public class CentralDiskController {

  @Autowired
  private CentralDiskService centralDiskService;

  @GetMapping("/devices/{deviceSerialNumber}/disk")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryCentralDisks(
      @PathVariable @NotEmpty(message = "设备类型不能为空") String deviceType,
      @PathVariable @NotEmpty(message = "设备编号不能为空") String deviceSerialNumber,
      @RequestParam(required = false, defaultValue = "") String raidNo) {
    List<CentralDiskBO> centralDisks = centralDiskService.queryCentralDisks(deviceType,
        deviceSerialNumber, raidNo);

    List<Map<String, Object>> centralDiskList = Lists
        .newArrayListWithExpectedSize(centralDisks.size());
    centralDisks.forEach(disk -> centralDiskList.add(diskToMap(disk)));

    return centralDiskList;
  }

  @GetMapping("/devices/{deviceSerialNumber}/raid")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryCentralRaids(
      @PathVariable @NotEmpty(message = "设备类型不能为空") String deviceType,
      @PathVariable @NotEmpty(message = "设备编号不能为空") String deviceSerialNumber) {
    List<CentralRaidBO> centralRaids = centralDiskService.queryCentralRaids(deviceType,
        deviceSerialNumber);

    List<Map<String, Object>> centralRaidList = Lists
        .newArrayListWithExpectedSize(centralRaids.size());
    centralRaids.forEach(raid -> centralRaidList.add(raidToMap(raid)));

    return centralRaidList;
  }

  @GetMapping("/disks/group-by-state")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> countDiskByState() {

    return centralDiskService.countCentralDiskByState();
  }

  private Map<String, Object> diskToMap(CentralDiskBO centralDiskBO) {
    Map<String, Object> diskMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    diskMap.put("id", centralDiskBO.getId());
    diskMap.put("deviceType", centralDiskBO.getDeviceType());
    diskMap.put("deviceSerialNumber", centralDiskBO.getDeviceSerialNumber());
    diskMap.put("physicalLocation", centralDiskBO.getPhysicalLocation());
    diskMap.put("slotNo", centralDiskBO.getSlotNo());
    diskMap.put("raidNo", centralDiskBO.getRaidNo());
    diskMap.put("raidLevel", centralDiskBO.getRaidLevel());
    diskMap.put("state", centralDiskBO.getState());
    diskMap.put("stateText", centralDiskBO.getStateText());
    diskMap.put("medium", centralDiskBO.getMedium());
    diskMap.put("mediumText", centralDiskBO.getMediumText());
    diskMap.put("capacity", centralDiskBO.getCapacity());
    diskMap.put("rebuildProgress", centralDiskBO.getRebuildProgress());
    diskMap.put("rebuildProgressText", centralDiskBO.getRebuildProgressText());
    diskMap.put("copybackProgress", centralDiskBO.getCopybackProgress());
    diskMap.put("copybackProgressText", centralDiskBO.getCopybackProgressText());
    diskMap.put("foreignState", centralDiskBO.getForeignState());
    diskMap.put("description", centralDiskBO.getDescription());

    return diskMap;
  }

  private Map<String, Object> raidToMap(CentralRaidBO centralRaidBO) {
    Map<String, Object> raidMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    raidMap.put("id", centralRaidBO.getId());
    raidMap.put("deviceType", centralRaidBO.getDeviceType());
    raidMap.put("deviceSerialNumber", centralRaidBO.getDeviceSerialNumber());
    raidMap.put("raidNo", centralRaidBO.getRaidNo());
    raidMap.put("raidLevel", centralRaidBO.getRaidLevel());
    raidMap.put("state", centralRaidBO.getState());
    raidMap.put("stateText", centralRaidBO.getStateText());

    return raidMap;
  }

}
