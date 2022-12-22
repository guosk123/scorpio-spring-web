package com.machloop.fpc.manager.appliance.dao;

import java.util.List;

import com.machloop.fpc.manager.appliance.data.AlertScopeDO;

/**
 * @author guosk
 *
 * create at 2021年5月17日, fpc-manager
 */
public interface AlertScopeDao {

  List<AlertScopeDO> queryAlertScope();

  List<AlertScopeDO> queryAlertScope(String sourceType, String networkId, String serviceId);

  List<AlertScopeDO> queryAlertScopeByAlertId(String alertId);

  int batchSaveAlertScopes(List<AlertScopeDO> alertScopes);

  int batchUpdateAlertScopes(String alertId, List<AlertScopeDO> alertScopes);

  int deleteAlertScopeByAlertId(String alertId);

}
