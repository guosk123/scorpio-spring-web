package com.machloop.fpc.cms.center.central.service;

import java.util.Map;

/**
 * @author guosk
 *
 * create at 2022年4月29日, fpc-cms-center
 */
public interface ClusterService {

  Map<String, String> queryAbnormalNodesAndRefresh();

}
