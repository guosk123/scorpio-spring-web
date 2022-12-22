package com.machloop.fpc.cms.center.appliance.service;

import com.machloop.fpc.cms.center.appliance.bo.NetworkTopologyBO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年1月20日, fpc-cms-center
 */
public interface NetworkTopologyService {

  NetworkTopologyBO queryNetworkTopology();

  NetworkTopologyBO updateNetworkTopology(NetworkTopologyBO networkTopologyBO, String operatorId);
}
