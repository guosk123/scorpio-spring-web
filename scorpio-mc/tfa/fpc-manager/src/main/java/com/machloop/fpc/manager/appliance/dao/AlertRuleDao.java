package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.data.AlertRuleDO;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
public interface AlertRuleDao {

  Page<AlertRuleDO> queryAlertRules(Pageable page, String name, String category, String level,
      String networkId, String serviceId);

  List<AlertRuleDO> queryAlertRules(String category);

  int countAlertRule();

  AlertRuleDO queryAlertRule(String id);

  AlertRuleDO queryAlertRuleByName(String name);

  List<String> queryAssignAlertRules(Date beforeTime);

  List<String> queryAlertRuleIds(boolean onlyLocal);

  AlertRuleDO queryAlertRuleByCmsAlertRuleId(String cmsAlertRuleId);

  /**
   * recover应用场景：
   *  在cms上新建告警s，其中包含a、b、c三个网络，这三个网络分别属于不同的探针a、b、c，告警s下发下去后4台设备中告警s的id均相等。此时执行以下步骤：
   *  步骤1、编辑告警s，删除其中的网络c，下发下去后在探针c上会删除告警s。
   *  步骤2、在cms上又将网络c添加进了告警s，此时在探针c上应该新建一个告警s，但为了保证告警id相等，所以要恢复步骤1探针c已经删除的告警s，此时使用recover方法
   */
  AlertRuleDO saveOrRecoverAlertRule(AlertRuleDO alertRuleDO);

  int updateAlertRule(AlertRuleDO alertRuleDO);

  int updateAlertRuleStatus(String id, String status, String operatorId);

  int deleteAlertRule(String id, String operatorId);

  int deleteAlertRule(List<String> ids, String operatorId);

}
