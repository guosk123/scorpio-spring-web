package com.machloop.fpc.npm.appliance.dao;

import java.util.List;
import com.machloop.fpc.npm.appliance.data.NetflowStatisticDO;
import com.machloop.fpc.npm.appliance.vo.NetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月13日, fpc-manager
 */
public interface NetflowStatisticDao {  
        
  List<NetflowStatisticDO> queryNetflowStatsGroupByDevAndNif(NetflowQueryVO queryVO);

}
