package com.machloop.fpc.manager.appliance.service.impl;

import java.time.ZoneId;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.bo.NatConfigBO;
import com.machloop.fpc.manager.appliance.dao.NatConfigDao;
import com.machloop.fpc.manager.appliance.data.NatConfigDO;
import com.machloop.fpc.manager.appliance.service.NatConfigService;

/**
 * @author ChenXiao
 * create at 2022/11/9
 */
@Service
public class NatConfigServiceImpl implements NatConfigService {


  @Autowired
  private NatConfigDao natConfigDao;


  @Override
  public Map<String, Object> queryNatConfig() {

    NatConfigDO natConfigDO = natConfigDao.queryNatConfig();

    Map<String, Object> res = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    res.put("id", natConfigDO.getId());
    res.put("natAction", natConfigDO.getNatAction());
    res.put("updateTime",
        DateUtils.toStringYYYYMMDDHHMMSS(natConfigDO.getUpdateTime(), ZoneId.systemDefault()));
    res.put("operatorId", natConfigDO.getOperatorId());


    return res;
  }

  @Override
  public NatConfigBO updateNatConfig(NatConfigBO natConfigBO, String operatorId) {
    NatConfigDO natConfigDO = natConfigDao.queryNatConfig();

    natConfigDO.setNatAction(natConfigBO.getNatAction());
    natConfigDO.setOperatorId(operatorId);

    natConfigDao.updateNatConfig(natConfigDO);
    BeanUtils.copyProperties(natConfigDO, natConfigBO);

    return natConfigBO;
  }
}
