package com.machloop.fpc.cms.center.sensor.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
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

import com.clickhouse.client.internal.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.center.sensor.vo.SensorNetworkCreationVO;
import com.machloop.fpc.cms.center.sensor.vo.SensorNetworkModificationVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月22日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class SensorNetworkController {

  @Autowired
  private FpcService fpcService;

  @Autowired
  private SensorNetworkService sensorNetworkService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @GetMapping("/sensor-networks")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public List<SensorNetworkBO> querySensorNetworks() {
    List<SensorNetworkBO> sensorNetworks = sensorNetworkService.querySensorNetworks();

    SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
        .queryCurrentUserNetworkPerms();
    if (currentUserNetworkPerms.getServiceUser()) {
      return sensorNetworks;
    } else {
      List<
          String> networkPerms = CsvUtils.convertCSVToList(currentUserNetworkPerms.getNetworkIds());
      return sensorNetworks.stream()
          .filter(item -> networkPerms.contains(item.getNetworkInSensorId()))
          .collect(Collectors.toList());
    }
  }

  @GetMapping("/sensor-networks/{id}")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public SensorNetworkBO querySensorNetwork(
      @PathVariable @NotEmpty(message = "网络id不能为空") String id) {

    return sensorNetworkService.querySensorNetwork(id);
  }

  @GetMapping("/networks-in-sensor/{deviceSerialNumber}")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public List<FpcNetworkBO> queryNetworksInSensor(
      @PathVariable @NotEmpty(message = "deviceSerialNumber不能为空") String deviceSerialNumber) {
    List<FpcNetworkBO> rawNetworks = fpcNetworkService
        .queryNetworks(FpcCmsConstants.DEVICE_TYPE_TFA, deviceSerialNumber);

    List<FpcNetworkBO> networks = getNetworksByFpcStatus(rawNetworks);

    SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
        .queryCurrentUserNetworkPerms();
    if (currentUserNetworkPerms.getServiceUser()) {
      return networks;
    } else {
      List<
          String> networkPerms = CsvUtils.convertCSVToList(currentUserNetworkPerms.getNetworkIds());
      return networks.stream().filter(item -> networkPerms.contains(item.getFpcNetworkId()))
          .collect(Collectors.toList());
    }
  }

  @GetMapping("/networks-in-sensor")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public List<SensorNetworkBO> queryNetworksInSensor() {
    List<SensorNetworkBO> networks = sensorNetworkService.getNetworksInSensorList();

    SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
        .queryCurrentUserNetworkPerms();
    if (currentUserNetworkPerms.getServiceUser()) {
      return networks;
    } else {
      List<
          String> networkPerms = CsvUtils.convertCSVToList(currentUserNetworkPerms.getNetworkIds());
      return networks.stream().filter(item -> networkPerms.contains(item.getNetworkInSensorId()))
          .collect(Collectors.toList());
    }
  }

  @PutMapping("/sensor-networks/{id}")
  @Secured({"PERM_SERVICE_USER"})
  public void updateSensorNetworks(@PathVariable @NotEmpty(message = "修改网络时传入的id不能为空") String id,
      SensorNetworkModificationVO modificationVO) {

    SensorNetworkBO sensorNetworkBO = new SensorNetworkBO();
    BeanUtils.copyProperties(modificationVO, sensorNetworkBO);
    sensorNetworkService.updateSensorNetwork(id, sensorNetworkBO,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, sensorNetworkBO);
  }

  @PostMapping("/sensor-networks-all")
  @Secured({"PERM_SERVICE_USER"})
  public void saveAllSensorNetworks() {
    List<SensorNetworkBO> networksInSensorList = sensorNetworkService.getNetworksInSensorList();

    List<SensorNetworkBO> sensorNetworkList = sensorNetworkService
        .batchSaveSensorNetworks(networksInSensorList, LoggedUserContext.getCurrentUser().getId());

    StringBuilder logContent = new StringBuilder("添加探针网络：");
    sensorNetworkList.forEach(sensorNetwork -> logContent
        .append(sensorNetwork.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE)));

    LogHelper.auditOperate(logContent.toString());
  }

  @PostMapping("/sensor-networks")
  @Secured({"PERM_SERVICE_USER"})
  public void saveSensorNetwork(SensorNetworkCreationVO creationVO) {
    SensorNetworkBO sensorNetworkBO = new SensorNetworkBO();
    BeanUtils.copyProperties(creationVO, sensorNetworkBO);

    sensorNetworkBO = sensorNetworkService.saveSensorNetwork(sensorNetworkBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, sensorNetworkBO);
  }

  @DeleteMapping("/sensor-networks/{id}")
  @Secured({"PERM_SERVICE_USER"})
  public void deleteSensorNetworks(@PathVariable @NotEmpty(message = "删除网络时传入的id不能为空") String id) {

    SensorNetworkBO sensorNetworkBO = sensorNetworkService.deleteSensorNetwork(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, sensorNetworkBO);
  }

  private List<FpcNetworkBO> getNetworksByFpcStatus(List<FpcNetworkBO> rawSensorNetworks) {
    List<FpcNetworkBO> result = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    Map<String, String> fpcSerialNumStatusMap = fpcService.queryAllFpc().stream()
        .collect(Collectors.toMap(FpcBO::getSerialNumber, FpcBO::getConnectStatus));
    // 当npmd的状态为在线时，才将其下的网络保存进数据库
    for (FpcNetworkBO fpcNetworkBO : rawSensorNetworks) {
      if (fpcSerialNumStatusMap.keySet().contains(fpcNetworkBO.getFpcSerialNumber())
          && StringUtils.equals(fpcSerialNumStatusMap.get(fpcNetworkBO.getFpcSerialNumber()),
              FpcCmsConstants.CONNECT_STATUS_NORMAL)) {
        result.add(fpcNetworkBO);
      }
    }
    return result;
  }
}
