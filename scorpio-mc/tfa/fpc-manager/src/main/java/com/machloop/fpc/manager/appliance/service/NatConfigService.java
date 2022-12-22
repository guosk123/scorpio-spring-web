package com.machloop.fpc.manager.appliance.service;

import java.util.Map;

import com.machloop.fpc.manager.appliance.bo.NatConfigBO;

/**
 * @author ChenXiao
 * create at 2022/11/9
 */
public interface NatConfigService {
  Map<String, Object> queryNatConfig();

  NatConfigBO updateNatConfig(NatConfigBO natConfigBO, String operatorId);
}
