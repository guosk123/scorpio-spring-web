package com.machloop.fpc.npm.appliance.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.SendupMessageService;
import com.machloop.fpc.manager.system.service.LicenseService;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.NetworkTopologyBO;
import com.machloop.fpc.npm.appliance.data.NetworkNetifDO;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.vo.NetworkCreationVO;
import com.machloop.fpc.npm.appliance.vo.NetworkModificationVO;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class NetworkController {

  @Autowired
  private NetworkService networkService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private SendupMessageService sendupMessageService;

  @Autowired
  private LicenseService licenseService;

  @GetMapping("/networks")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetworks() {
    List<NetworkBO> networks = networkService.queryNetworks();

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(networks.size());
    for (NetworkBO network : networks) {
      resultList.add(networkBO2Map(network, false));
    }

    return resultList;
  }

  @GetMapping("/network-policies")
  @Secured({"PERM_USER"})
  public List<Map<String, String>> queryNetworkPolicys(
      @RequestParam(name = "policyType") String policyType) {

    return networkService.queryNetworkPolicy(policyType);
  }

  @GetMapping("/networks/forward-policies")
  @Secured({"PERM_USER"})
  public Map<String, List<String>> queryNetworkPolicies() {
    return networkService.queryNetworkPolicies();
  }


  @GetMapping("/network-netifs")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetworkNetifs() {

    return networkService.queryNetworkNetif();
  }

  @GetMapping("/network-topologys")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryNetworkTopology() {
    NetworkTopologyBO networkTopologyBO = networkService.queryNetworkTopology();

    Map<String,
        Object> networkTopologyMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkTopologyMap.put("topology", networkTopologyBO.getTopology());
    networkTopologyMap.put("metric", networkTopologyBO.getMetric());
    return networkTopologyMap;
  }

  @GetMapping("/networks/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryNetwork(@NotEmpty(message = "网络ID不能为空") @PathVariable String id) {
    NetworkBO networkBO = networkService.queryNetwork(id);

    return networkBO2Map(networkBO, true);
  }

  @PostMapping("/networks")
  @Secured({"PERM_USER"})
  public void saveNetwork(@Validated NetworkCreationVO networkCreationVO) {
    NetworkBO networkBO = new NetworkBO();
    BeanUtils.copyProperties(networkCreationVO, networkBO);
    List<NetworkNetifDO> netif = JsonHelper.deserialize(networkCreationVO.getNetif(),
        new TypeReference<List<NetworkNetifDO>>() {
        }, false);
    networkBO.setNetif(netif);

    networkBO = networkService.saveNetwork(networkBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, networkBO);
  }

  @PutMapping("/networks/{id}")
  @Secured({"PERM_USER"})
  public void updateNetwork(@PathVariable @NotEmpty(message = "修改网络时传入的id不能为空") String id,
      @Validated NetworkModificationVO networkModificationVO) {
    NetworkBO networkBO = new NetworkBO();
    BeanUtils.copyProperties(networkModificationVO, networkBO);
    List<NetworkNetifDO> netif = JsonHelper.deserialize(networkModificationVO.getNetif(),
        new TypeReference<List<NetworkNetifDO>>() {
        }, false);
    networkBO.setNetif(netif);

    networkBO = networkService.updateNetwork(id, networkBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, networkBO);
  }

  @PutMapping("/networks/{id}/policies")
  @Secured({"PERM_USER"})
  public void updateNetworkPolicy(@PathVariable @NotEmpty(message = "网络id不能为空") String id,
      @NotEmpty(message = "策略id不能为空") String policyId,
      @NotEmpty(message = "策略类型不能为空") String policyType) {
    networkService.updateNetworkPolicy(id, policyId, policyType,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("修改网络策略：网络ID=" + id + ";策略类型=" + policyType + ";策略ID=" + policyId);
  }

  @PutMapping("/network-topologys")
  @Secured({"PERM_USER"})
  public void updateNetworkTopology(@RequestParam String topology, @RequestParam String metric) {
    NetworkTopologyBO networkTopologyBO = new NetworkTopologyBO();
    networkTopologyBO.setTopology(topology);
    networkTopologyBO.setMetric(metric);

    networkTopologyBO = networkService.updateNetworkTopology(networkTopologyBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("修改网络拓扑图：网络拓扑配置=" + networkTopologyBO.getTopology() + ";指标配置="
        + networkTopologyBO.getMetric());
  }

  @PutMapping("/network-reports")
  @Secured({"PERM_USER"})
  public void reportAllNetwork() {
    // 检测设备是否注册
    if (StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE, false))
        || StringUtils.isBlank(
            globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP, false))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备未注册到CMS");
    }

    sendupMessageService.sendAllApplianceMessage(licenseService.queryDeviceSerialNumber(),
        DateUtils.now());
  }

  @DeleteMapping("/networks/{id}")
  @Secured({"PERM_USER"})
  public void deleteNetwork(@PathVariable @NotEmpty(message = "删除网络时传入的id不能为空") String id) {
    NetworkBO networkBO = networkService.deleteNetwork(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, networkBO);
  }

  private static Map<String, Object> networkBO2Map(NetworkBO networkBO, boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", networkBO.getId());
    map.put("name", networkBO.getName());
    map.put("netifType", networkBO.getNetifType());
    map.put("netifTypeText", networkBO.getNetifTypeText());
    map.put("netif", networkBO.getNetif());
    if (isDetail) {
      map.put("extraSettings", networkBO.getExtraSettings());
      map.put("insideIpAddress", networkBO.getInsideIpAddress());
      map.put("ingestPolicyId", networkBO.getIngestPolicyId());
      map.put("filterRuleIds", networkBO.getFilterRuleIds());
      map.put("createTime", networkBO.getCreateTime());
      map.put("sendPolicyIds", networkBO.getSendPolicyIds());
    }

    return map;
  }

}
