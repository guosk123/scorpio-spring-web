package com.machloop.fpc.cms.center.appliance.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.appliance.bo.NetworkTopologyBO;
import com.machloop.fpc.cms.center.appliance.dao.NetworkTopologyDao;
import com.machloop.fpc.cms.center.appliance.data.NetworkTopologyDO;
import com.machloop.fpc.cms.center.appliance.service.NetworkTopologyService;

/**
 * @author "Minjiajun"
 *
 * create at 2022年1月20日, fpc-cms-center
 */
@Service
public class NetworkTopologyServiceImpl implements NetworkTopologyService {

  @Autowired
  private NetworkTopologyDao networkTopologyDao;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.NetworkTopologyService#queryNetworkTopology()
   */
  @Override
  public NetworkTopologyBO queryNetworkTopology() {
    NetworkTopologyDO networkTopologyDO = networkTopologyDao.queryNetworkTopology();

    NetworkTopologyBO networkTopologyBO = new NetworkTopologyBO();
    BeanUtils.copyProperties(networkTopologyDO, networkTopologyBO);
    networkTopologyBO.setTimestamp(DateUtils.toStringISO8601(networkTopologyDO.getTimestamp()));

    return networkTopologyBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.NetworkTopologyService#updateNetworkTopology(com.machloop.fpc.cms.center.appliance.bo.NetworkTopologyBO, java.lang.String)
   */
  @Override
  public NetworkTopologyBO updateNetworkTopology(NetworkTopologyBO networkTopologyBO,
      String operatorId) {
    NetworkTopologyDO networkTopologyDO = new NetworkTopologyDO();
    BeanUtils.copyProperties(networkTopologyBO, networkTopologyDO);
    networkTopologyDO.setOperatorId(operatorId);

    networkTopologyDao.saveOrUpdateNetworkTopology(networkTopologyDO);

    return queryNetworkTopology();
  }
}
