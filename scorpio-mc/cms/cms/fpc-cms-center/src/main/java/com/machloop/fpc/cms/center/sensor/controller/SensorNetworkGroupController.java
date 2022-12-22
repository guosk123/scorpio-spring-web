package com.machloop.fpc.cms.center.sensor.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkGroupBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;
import com.machloop.fpc.cms.center.sensor.vo.SensorNetworkGroupCreationVO;
import com.machloop.fpc.cms.center.sensor.vo.SensorNetworkGroupModificationVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class SensorNetworkGroupController {

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;

  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @GetMapping("/sensor-network-groups")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public List<SensorNetworkGroupBO> querySensorNetworkGroups() {
    List<SensorNetworkGroupBO> networkGroups = sensorNetworkGroupService.querySensorNetworkGroups();

    SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
        .queryCurrentUserNetworkPerms();
    if (currentUserNetworkPerms.getServiceUser()) {
      return networkGroups;
    } else {
      List<String> networkGroupPerms = CsvUtils
          .convertCSVToList(currentUserNetworkPerms.getNetworkGroupIds());
      return networkGroups.stream().filter(item -> networkGroupPerms.contains(item.getId()))
          .collect(Collectors.toList());
    }
  }

  @GetMapping("/sensor-network-groups/{id}")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public SensorNetworkGroupBO querySensorNetworkGroup(
      @PathVariable @NotEmpty(message = "网络组id不能为空") String id) {

    return sensorNetworkGroupService.querySensorNetworkGroup(id);
  }

  @PutMapping("/sensor-networks-groups/{id}")
  @Secured({"PERM_SERVICE_USER"})
  public void updateSensorNetworkGroup(
      @PathVariable @NotEmpty(message = "修改网络组时传入的id不能为空") String id,
      SensorNetworkGroupModificationVO modificationVO) {

    SensorNetworkGroupBO sensorNetworkGroupBO = new SensorNetworkGroupBO();
    BeanUtils.copyProperties(modificationVO, sensorNetworkGroupBO);
    sensorNetworkGroupService.updateSensorNetworkGroup(id, sensorNetworkGroupBO,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, sensorNetworkGroupBO);
  }

  @PostMapping("/sensor-networks-groups")
  @Secured({"PERM_SERVICE_USER"})
  public void saveSensorNetworkGroup(SensorNetworkGroupCreationVO creationVO) {
    SensorNetworkGroupBO sensorNetworkGroupBO = new SensorNetworkGroupBO();
    BeanUtils.copyProperties(creationVO, sensorNetworkGroupBO);

    sensorNetworkGroupBO = sensorNetworkGroupService.saveSensorNetworkGroup(sensorNetworkGroupBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, sensorNetworkGroupBO);
  }

  @DeleteMapping("/sensor-network-groups/{id}")
  @Secured({"PERM_SERVICE_USER"})
  public void deleteSensorNetworkGroups(
      @PathVariable @NotEmpty(message = "删除网络组时传入的id不能为空") String id) {

    SensorNetworkGroupBO sensorNetworkGroupsBO = sensorNetworkGroupService
        .deleteSensorNetworkGroup(id, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, sensorNetworkGroupsBO);
  }

  @PostMapping("/sensor-network-groups/batch")
  @Secured({"PERM_SERVICE_USER"})
  public void batchDeleteNetworkGroup(@RequestParam @NotEmpty(message = "网络id不能为空") String ids) {

    List<String> deleteIdList = Arrays.asList(ids.split(","));
    deleteIdList.forEach(id -> {
      SensorNetworkGroupBO sensorNetworkGroupsBO = sensorNetworkGroupService
          .deleteSensorNetworkGroup(id, LoggedUserContext.getCurrentUser().getId());

      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, sensorNetworkGroupsBO);
    });
  }
}
