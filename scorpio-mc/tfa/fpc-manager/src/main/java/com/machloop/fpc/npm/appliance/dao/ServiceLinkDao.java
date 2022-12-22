package com.machloop.fpc.npm.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.npm.appliance.data.ServiceLinkDO;

/**
 * @author guosk
 *
 * create at 2021年7月2日, fpc-manager
 */
public interface ServiceLinkDao {

  List<ServiceLinkDO> queryServiceLinks();

  ServiceLinkDO queryServiceLink(String serviceId);

  List<String> queryAssignServiceLinks(Date beforeTime);
  
  int saveOrUpdateServiceLink(ServiceLinkDO serviceLinkDO);

  int batchSaveServiceLink(List<ServiceLinkDO> serviceLinks);

  int deleteServiceLink(String serviceId);

}
