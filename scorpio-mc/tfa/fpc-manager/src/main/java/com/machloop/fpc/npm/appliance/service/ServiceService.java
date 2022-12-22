package com.machloop.fpc.npm.appliance.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.appliance.bo.MetricSettingBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.bo.ServiceFollowBO;
import com.machloop.fpc.npm.appliance.bo.ServiceLinkBO;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */
public interface ServiceService {

  Page<ServiceBO> queryServices(Pageable page, String name);

  List<ServiceBO> queryServices();

  List<ServiceBO> queryServicesBasicInfo();

  List<ServiceBO> queryServiceByAppId(String applicationId);

  ServiceBO queryService(String id);

  List<String> exportServices();

  int importServices(MultipartFile file, String operatorId);

  ServiceBO saveService(ServiceBO serviceBO, List<MetricSettingBO> metricSettings,
      String operatorId);

  ServiceBO updateService(String id, ServiceBO serviceBO, String operatorId);

  ServiceBO deleteService(String id, String operatorId, boolean forceDelete);

  /************************************************************
  *
  *************************************************************/

  List<ServiceFollowBO> queryUserFollowService(String userId);

  void changeUserFollowState(ServiceFollowBO serviceFollowBO);

  /************************************************************
  *
  *************************************************************/

  ServiceLinkBO queryServiceLink(String serviceId);

  ServiceLinkBO updateServiceLink(ServiceLinkBO serviceLinkBO, String operatorId);

}
