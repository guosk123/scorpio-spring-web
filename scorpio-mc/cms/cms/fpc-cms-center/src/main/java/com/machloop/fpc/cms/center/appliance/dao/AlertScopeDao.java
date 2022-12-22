package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;
import com.machloop.fpc.cms.center.appliance.data.AlertScopeDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
public interface AlertScopeDao {

  List<AlertScopeDO> queryAlertScope();

  List<AlertScopeDO> queryAlertScope(String sourceType, String networkId, String serviceId);

  List<AlertScopeDO> queryAlertScopeByAlertId(String alertId);

  int batchSaveAlertScopes(List<AlertScopeDO> alertScopes);

  int batchUpdateAlertScopes(String alertId, List<AlertScopeDO> alertScopes);

  int deleteAlertScopeByAlertId(String alertId);

  int deleteAlertScope(String alertId, String networkId, String serviceId);

}
