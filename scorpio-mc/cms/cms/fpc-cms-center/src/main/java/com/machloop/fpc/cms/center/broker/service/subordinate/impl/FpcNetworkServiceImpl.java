package com.machloop.fpc.cms.center.broker.service.subordinate.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.fpc.cms.center.appliance.service.AlertRuleService;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao;
import com.machloop.fpc.cms.center.broker.data.FpcNetworkDO;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.central.dao.CmsDao;
import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月21日, fpc-cms-center
 */
@Service
public class FpcNetworkServiceImpl implements FpcNetworkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FpcNetworkServiceImpl.class);

  @Autowired
  private FpcNetworkDao fpcNetworkDao;

  @Autowired
  private CmsDao cmsDao;

  @Autowired
  private AlertRuleService alertRuleService;
  @Autowired
  private ServiceService serviceService;
  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;
  @Autowired
  private SensorLogicalSubnetService sensorLogicalSubnetService;
  @Autowired
  private SensorNetworkService sensorNetworkService;

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService#queryAllNetworks()
   */
  @Override
  public List<FpcNetworkBO> queryAllNetworks() {
    List<FpcNetworkDO> networkDOList = fpcNetworkDao.queryFpcNetworks(null);

    List<FpcNetworkBO> result = Lists.newArrayListWithCapacity(networkDOList.size());
    networkDOList.forEach(networkDO -> {
      FpcNetworkBO networkBO = new FpcNetworkBO();
      BeanUtils.copyProperties(networkDO, networkBO);
      result.add(networkBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService#queryNetworks(java.lang.String, java.lang.String)
   */
  @Override
  public List<FpcNetworkBO> queryNetworks(String deviceType, String deviceSerialNumber) {
    List<FpcNetworkDO> networks = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
      networks.addAll(fpcNetworkDao.queryFpcNetworks(deviceSerialNumber));
    } else if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_CMS)) {
      Map<String, List<CmsDO>> cmsHierarchy = cmsDao.queryCms(new CmsQueryVO()).stream()
          .collect(Collectors.groupingBy(CmsDO::getSuperiorCmsSerialNumber));
      List<String> subordinateCms = iterateSubordinateCms(deviceSerialNumber, cmsHierarchy);
      networks.addAll(fpcNetworkDao.queryFpcNetworkByCms(subordinateCms));
    }

    List<FpcNetworkBO> result = Lists.newArrayListWithCapacity(networks.size());
    networks.forEach(networkDO -> {
      FpcNetworkBO networkBO = new FpcNetworkBO();
      BeanUtils.copyProperties(networkDO, networkBO);
      result.add(networkBO);
    });

    return result;
  }

  private List<String> iterateSubordinateCms(String outset, Map<String, List<CmsDO>> cmsHierarchy) {
    List<String> allSubordinateCms = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    allSubordinateCms.add(outset);

    List<CmsDO> list = cmsHierarchy.get(outset);
    if (CollectionUtils.isNotEmpty(list)) {
      list.forEach(cms -> {
        allSubordinateCms.addAll(iterateSubordinateCms(cms.getSerialNumber(), cmsHierarchy));
      });
    }

    return allSubordinateCms;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService#deleteNetworkByFpc(java.lang.String)
   */
  @Override
  public void deleteNetworkByFpc(String fpcSerialNumber) {
    fpcNetworkDao.deleteFpcNetwork(fpcSerialNumber);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService#deleteNetworkByLinkage(java.util.List, java.lang.String)
   */
  @Transactional
  @Override
  public int deleteNetworkByLinkage(List<String> fpcNetworkIds, String operatorId) {
    fpcNetworkIds.forEach(networkId -> {
      // 从告警的作用域中移除该网络
      try {
        alertRuleService.updateAlertRuleScope(networkId, operatorId);
      } catch (BusinessException e) {
        LOGGER.warn("[alert] remove fpc network failed.", e);
      }

      // 从业务总移除该网络
      try {
        serviceService.updateServiceNetworks(null, Lists.newArrayList(networkId),
            Lists.newArrayListWithCapacity(0), operatorId);
      } catch (BusinessException e) {
        LOGGER.warn("[service] remove fpc network failed.", e);
      }

      // 从网络组中移除该网络
      try {
        sensorNetworkGroupService.removeNetworkFromGroup(networkId, operatorId);
      } catch (BusinessException e) {
        LOGGER.warn("[networkGroup] remove fpc network failed.", e);
      }

      // 从子网中移除该网络
      try {
        sensorLogicalSubnetService.removeNetworkFromSubnet(networkId, operatorId);
      } catch (BusinessException e) {
        LOGGER.warn("[subnet] remove fpc network failed.", e);
      }

      // 删除网络对应的探针网络
      try {
        sensorNetworkService.deleteSensorNetworkByFpcNetworkId(networkId, operatorId);
      } catch (BusinessException e) {
        LOGGER.warn("[sensorNetwork] remove fpc network failed.", e);
      }
    });

    // 删除网络
    return fpcNetworkDao.deleteFpcNetwork(fpcNetworkIds);
  }

}
