package com.machloop.fpc.npm.appliance.dao;

import java.util.List;
import com.machloop.fpc.npm.appliance.data.NetflowConfigDO;
import com.machloop.fpc.npm.appliance.vo.NetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月13日, fpc-manager
 */
public interface NetflowConfigDao {
  
  List<NetflowConfigDO> queryNetflowConfigs(String keyword);
  
  List<NetflowConfigDO> queryNetflowConfigsByName(List<String> deviceNameList);
  
  List<NetflowConfigDO> queryNetflowConfigsGroupByDevAndNif(NetflowQueryVO queryVO);
  
  int updateNetflowDevice(List<NetflowConfigDO> netflowDeviceList);
  
  int updateNetflowNetif(List<NetflowConfigDO> netflowNetifList);
}
