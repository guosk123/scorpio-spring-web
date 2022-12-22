package com.machloop.fpc.npm.appliance.service;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.appliance.bo.NetflowConfigBO;
import com.machloop.fpc.npm.appliance.bo.NetflowSourceBO;
import com.machloop.fpc.npm.appliance.vo.NetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月12日, fpc-manager
 */
public interface NetflowService {
  
  List<NetflowConfigBO> queryNetflowConfigs(String keywords);

  Page<NetflowSourceBO> queryNetflowSources(NetflowQueryVO queryVO, Pageable page);
  
  List<NetflowConfigBO> batchUpdateNetflows(List<NetflowConfigBO> netflowBOList, String operatorId);
}
