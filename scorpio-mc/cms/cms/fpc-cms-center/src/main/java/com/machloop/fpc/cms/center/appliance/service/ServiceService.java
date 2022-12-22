package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.bo.MetricSettingBO;
import com.machloop.fpc.cms.center.appliance.bo.ServiceBO;
import com.machloop.fpc.cms.center.appliance.bo.ServiceFollowBO;
import com.machloop.fpc.cms.center.appliance.bo.ServiceLinkBO;


/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
public interface ServiceService {

  Page<ServiceBO> queryServices(Pageable page, String name);

  List<ServiceBO> queryServices();

  List<ServiceBO> queryServicesBasicInfo();
  
  List<ServiceBO> queryServiceByAppId(String applicationId);

  List<ServiceBO> queryServicesWithNetwork();

  ServiceBO queryService(String id);

  List<String> exportServices();

  int importServices(MultipartFile file, String operatorId);

  ServiceBO saveService(ServiceBO serviceBO, List<MetricSettingBO> metricSettings,
      String operatorId);

  ServiceBO updateService(String id, ServiceBO serviceBO, String operatorId);

  /**
   * 更改某个用户创建的业务所关联的网络（组）
   * @param userId
   * @param removeNetworkIds
   * @param removeNetworkGroupIds
   * @param operatorId
   */
  void updateServiceNetworks(String userId, List<String> removeNetworkIds,
      List<String> removeNetworkGroupIds, String operatorId);

  ServiceBO deleteService(String id, String operatorId, boolean forceDelete);

  int deleteServiceByUser(String userId, String operatorId);

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
