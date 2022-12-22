package com.machloop.fpc.cms.center.appliance.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
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
import com.machloop.fpc.cms.center.appliance.bo.NetworkPolicyBO;
import com.machloop.fpc.cms.center.appliance.service.NetworkPolicyService;
import com.machloop.fpc.cms.center.appliance.vo.NetworkPolicyCreationVO;
import com.machloop.fpc.cms.center.appliance.vo.NetworkPolicyModificationVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;

/**
 * @author "Minjiajun"
 *
 * create at 2021年12月1日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class NetworkPolicyController {

  @Autowired
  private NetworkPolicyService networkPolicyService;

  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @GetMapping("/network-policies")
  @Secured({"PERM_USER"})
  public List<Map<String, String>> queryNetworkPolicys(
      @RequestParam(name = "policyType") String policyType) {
    List<Map<String, String>> networkPolicys = networkPolicyService.queryNetworkPolicys(policyType);

    SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
        .queryCurrentUserNetworkPerms();
    if (currentUserNetworkPerms.getServiceUser()) {
      return networkPolicys;
    } else {
      List<
          String> networkPerms = CsvUtils.convertCSVToList(currentUserNetworkPerms.getNetworkIds());
      return networkPolicys.stream()
          .filter(item -> networkPerms.contains(MapUtils.getString(item, "networkId")))
          .collect(Collectors.toList());
    }
  }

  @GetMapping("/network-policies/{id}")
  @Secured({"PERM_USER"})
  public NetworkPolicyBO queryNetworkPolicy(@PathVariable String id) {
    NetworkPolicyBO networkPolicyBO = networkPolicyService.queryNetworkPolicy(id);

    return networkPolicyBO;
  }

  @PostMapping("/networks-policies")
  @Secured({"PERM_USER"})
  public void saveNetworkPolicy(@Validated NetworkPolicyCreationVO networkPolicyVO) {

    NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
    BeanUtils.copyProperties(networkPolicyVO, networkPolicyBO);
    NetworkPolicyBO result = networkPolicyService.saveNetworkPolicy(networkPolicyBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);
  }

  @PutMapping("/networks-policies/{policyId}")
  @Secured({"PERM_USER"})
  public void updateNetworkPolicy(@Validated NetworkPolicyModificationVO networkPolicyVO) {

    NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
    BeanUtils.copyProperties(networkPolicyVO, networkPolicyBO);
    NetworkPolicyBO result = networkPolicyService.updateNetworkPolicy(networkPolicyBO.getPolicyId(),
        networkPolicyBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result);
  }
}
