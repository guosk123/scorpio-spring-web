package com.machloop.fpc.cms.center.appliance.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.appliance.bo.NetworkTopologyBO;
import com.machloop.fpc.cms.center.appliance.service.NetworkTopologyService;

/**
 * @author "Minjiajun"
 *
 * create at 2022年1月20日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class NetworkTopologyController {

  @Autowired
  private NetworkTopologyService networkTopologyService;

  @GetMapping("/network-topologys")
  @Secured({"PERM_USER", "PERM_SERVICE_USER"})
  public Map<String, Object> queryNetworkTopology() {
    NetworkTopologyBO networkTopologyBO = networkTopologyService.queryNetworkTopology();

    Map<String,
        Object> networkTopologyMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkTopologyMap.put("topology", networkTopologyBO.getTopology());
    networkTopologyMap.put("metric", networkTopologyBO.getMetric());
    return networkTopologyMap;
  }

  @PutMapping("/network-topologys")
  @Secured({"PERM_SERVICE_USER"})
  public void updateNetworkTopology(@RequestParam String topology, @RequestParam String metric) {
    NetworkTopologyBO networkTopologyBO = new NetworkTopologyBO();
    networkTopologyBO.setTopology(topology);
    networkTopologyBO.setMetric(metric);

    networkTopologyBO = networkTopologyService.updateNetworkTopology(networkTopologyBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("修改网络拓扑图：网络拓扑配置=" + networkTopologyBO.getTopology() + ";指标配置="
        + networkTopologyBO.getMetric());
  }

}
