package com.machloop.fpc.cms.center.sensor.controller;

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
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;
import com.machloop.fpc.cms.center.sensor.vo.SensorLogicalSubnetCreationVO;
import com.machloop.fpc.cms.center.sensor.vo.SensorLogicalSubnetModificationVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class SensorLogicalSubnetController {

  @Autowired
  private SensorLogicalSubnetService sensorLogicalSubnetService;

  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @GetMapping("/logical-subnets")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public List<SensorLogicalSubnetBO> querySensorLogicalSubnets() {
    List<SensorLogicalSubnetBO> sensorLogicalSubnets = sensorLogicalSubnetService
        .querySensorLogicalSubnets();

    SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
        .queryCurrentUserNetworkPerms();
    if (currentUserNetworkPerms.getServiceUser()) {
      return sensorLogicalSubnets;
    } else {
      List<
          String> networkPerms = CsvUtils.convertCSVToList(currentUserNetworkPerms.getNetworkIds());
      return sensorLogicalSubnets.stream().filter(item -> networkPerms.contains(item.getId()))
          .collect(Collectors.toList());
    }
  }

  @GetMapping("/logical-subnets/{id}")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public SensorLogicalSubnetBO querySensorLogicalSubnet(
      @PathVariable @NotEmpty(message = "逻辑子网id不能为空") String id) {

    return sensorLogicalSubnetService.querySensorLogicalSubnet(id);
  }

  @PutMapping("/logical-subnets/{id}")
  @Secured({"PERM_SERVICE_USER"})
  public void updateSensorLogicalSubnet(
      @PathVariable @NotEmpty(message = "修改网络组时传入的id不能为空") String id,
      SensorLogicalSubnetModificationVO modificationVO) {

    SensorLogicalSubnetBO sensorLogicalSubnetBO = new SensorLogicalSubnetBO();
    BeanUtils.copyProperties(modificationVO, sensorLogicalSubnetBO);
    sensorLogicalSubnetService.updateSensorLogicalSubnet(id, sensorLogicalSubnetBO,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, sensorLogicalSubnetBO);
  }

  @PostMapping("/logical-subnets")
  @Secured({"PERM_SERVICE_USER"})
  public void saveSensorLogicalSubnet(SensorLogicalSubnetCreationVO creationVO) {
    SensorLogicalSubnetBO sensorLogicalSubnetBO = new SensorLogicalSubnetBO();
    BeanUtils.copyProperties(creationVO, sensorLogicalSubnetBO);

    sensorLogicalSubnetBO = sensorLogicalSubnetService.saveSensorLogicalSubnet(
        sensorLogicalSubnetBO, null, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, sensorLogicalSubnetBO);
  }

  @DeleteMapping("/logical-subnets/{id}")
  @Secured({"PERM_SERVICE_USER"})
  public void deleteSensorLogicalSubnet(
      @PathVariable @NotEmpty(message = "删除网络组时传入的id不能为空") String id) {

    SensorLogicalSubnetBO sensorLogicalSubnetBO = sensorLogicalSubnetService
        .deleteSensorLogicalSubnet(id, LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, sensorLogicalSubnetBO);
  }
}
