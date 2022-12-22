package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.ServiceLinkDO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月20日, fpc-cms-center
 */
public interface ServiceLinkDao {

  List<ServiceLinkDO> queryServiceLinks();

  ServiceLinkDO queryServiceLink(String serviceId);

  List<ServiceLinkDO> queryAssignServiceLinkIds(Date beforeTime);

  List<String> queryServiceLinkIds(Date beforeTime);

  int saveOrUpdateServiceLink(ServiceLinkDO serviceLinkDO);

  int batchSaveServiceLink(List<ServiceLinkDO> serviceLinks);

  int deleteServiceLink(String serviceId);

}
