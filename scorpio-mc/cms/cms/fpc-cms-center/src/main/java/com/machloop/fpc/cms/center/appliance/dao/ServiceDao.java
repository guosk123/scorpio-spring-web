package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.data.ServiceDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface ServiceDao {

  Page<ServiceDO> queryServices(Pageable page, String name, String owner);

  List<ServiceDO> queryServices(String name);

  List<ServiceDO> queryServiceByIds(List<String> serviceIds);

  List<ServiceDO> queryServiceByUser(String owner);

  List<ServiceDO> queryAssignServiceIds(Date beforeTime);

  ServiceDO queryService(String id);

  ServiceDO queryServiceByName(String name);

  ServiceDO queryServiceByAssignId(String assignId);

  List<String> queryServiceIds(boolean onlyLocal);

  /**
   * recover应用场景：
   *  在cms上新建业务s，其中包含a、b、c三个网络，这三个网络分别属于不同的探针a、b、c，业务s下发下去后4台设备中业务s的id均相等。此时执行以下步骤：
   *  步骤1、编辑业务s，删除其中的网络c，下发下去后在探针c上会删除业务s。
   *  步骤2、在cms上又将网络c添加进了业务s，此时在探针c上应该新建一个业务s，但为了保证业务id相等，所以要恢复步骤1探针c已经删除的业务s，此时使用recover方法
   */
  ServiceDO saveOrRecoverService(ServiceDO serviceDO);

  int batchSaveService(List<ServiceDO> services);

  int updateService(ServiceDO serviceDO);

  int deleteService(String id, String operatorId);

}
