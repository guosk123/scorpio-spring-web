package com.machloop.fpc.cms.center.sensor.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.alpha.webapp.security.bo.LoggedUser;
import com.machloop.alpha.webapp.system.dao.RoleDao;
import com.machloop.alpha.webapp.system.dao.UserDao;
import com.machloop.alpha.webapp.system.data.RoleDO;
import com.machloop.alpha.webapp.system.data.UserDO;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkPermDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkPermDO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;

/**
 * @author guosk
 *
 * create at 2022年3月10日, fpc-cms-center
 */
@Service
public class SensorNetworkPermServiceImpl implements SensorNetworkPermService {

  @Autowired
  private SensorNetworkPermDao sensorNetworkPermDao;

  @Autowired
  private UserDao userDao;

  @Autowired
  private RoleDao roleDao;

  @Autowired
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private SensorNetworkGroupDao sensorNetworkGroupDao;

  @Autowired
  private SensorLogicalSubnetDao sensorLogicalSubnetDao;

  @Autowired
  private ServiceService serviceService;

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService#querySensorNetworkPerms(com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<SensorNetworkPermBO> querySensorNetworkPerms(Pageable page) {
    // 当前可配置的用户
    String normalRoleId = roleDao.queryRoleByNameEn(WebappConstants.ROLE_USER).getId();
    String serviceRoleId = roleDao.queryRoleByNameEn(WebappConstants.ROLE_SERVICE_USER).getId();
    Page<UserDO> users = userDao.queryUsersByRole(page, normalRoleId, serviceRoleId);
    if (users.getTotalElements() == 0) {
      return new PageImpl<>(Lists.newArrayList(), page, 0);
    }

    long total = users.getTotalElements();
    Map<String, String> userMap = users.getContent().stream()
        .collect(Collectors.toMap(UserDO::getId, UserDO::getFullname));

    // 网络ID名称映射
    Map<String, String> networkMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkMap.putAll(sensorNetworkDao.querySensorNetworks().stream()
        .collect(Collectors.toMap(SensorNetworkDO::getNetworkInSensorId,
            network -> StringUtils.isNotBlank(network.getName()) ? network.getName()
                : network.getNetworkInSensorName())));
    networkMap.putAll(sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetDO::getId, SensorLogicalSubnetDO::getName)));
    // 网络组ID名称映射
    Map<String, String> networkGroupMap = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
        .collect(Collectors.toMap(SensorNetworkGroupDO::getId, SensorNetworkGroupDO::getName));

    // 查询当前页用户的网络（组）权限
    List<SensorNetworkPermDO> sensorNetworkPerms = sensorNetworkPermDao
        .querySensorNetworkPerms(Lists.newArrayList(userMap.keySet()));
    Map<String,
        Map<String, String>> userNetworkPerms = Maps.newHashMapWithExpectedSize(userMap.size());
    Map<String, Map<String, String>> userNetworkGroupPerms = Maps
        .newHashMapWithExpectedSize(userMap.size());
    sensorNetworkPerms.forEach(sensorNetworkPerm -> {
      if (StringUtils.isNotBlank(sensorNetworkPerm.getNetworkId())) {
        Map<String,
            String> networkList = userNetworkPerms.getOrDefault(sensorNetworkPerm.getUserId(),
                Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE));
        networkList.put(sensorNetworkPerm.getNetworkId(),
            networkMap.get(sensorNetworkPerm.getNetworkId()));

        userNetworkPerms.put(sensorNetworkPerm.getUserId(), networkList);
      } else {
        Map<String,
            String> networkGroupList = userNetworkGroupPerms.getOrDefault(
                sensorNetworkPerm.getUserId(),
                Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE));
        networkGroupList.put(sensorNetworkPerm.getNetworkGroupId(),
            networkGroupMap.get(sensorNetworkPerm.getNetworkGroupId()));

        userNetworkGroupPerms.put(sensorNetworkPerm.getUserId(), networkGroupList);
      }
    });

    List<SensorNetworkPermBO> list = userMap.entrySet().stream().map(user -> {
      SensorNetworkPermBO sensorNetworkPermBO = new SensorNetworkPermBO();
      sensorNetworkPermBO.setUserId(user.getKey());
      sensorNetworkPermBO.setUserName(user.getValue());
      Map<String, String> networkPerms = userNetworkPerms.get(user.getKey());
      if (MapUtils.isNotEmpty(networkPerms)) {
        sensorNetworkPermBO.setNetworkIds(CsvUtils.convertCollectionToCSV(networkPerms.keySet()));
        sensorNetworkPermBO.setNetworkNames(CsvUtils.convertCollectionToCSV(networkPerms.values()));
      }
      Map<String, String> networkGroupPerms = userNetworkGroupPerms.get(user.getKey());
      if (MapUtils.isNotEmpty(networkGroupPerms)) {
        sensorNetworkPermBO
            .setNetworkGroupIds(CsvUtils.convertCollectionToCSV(networkGroupPerms.keySet()));
        sensorNetworkPermBO
            .setNetworkGroupNames(CsvUtils.convertCollectionToCSV(networkGroupPerms.values()));
      }

      return sensorNetworkPermBO;
    }).collect(Collectors.toList());
    return new PageImpl<>(list, page, total);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService#queryCurrentUserNetworkPerms()
   */
  @Override
  public SensorNetworkPermBO queryCurrentUserNetworkPerms() {
    LoggedUser currentUser = LoggedUserContext.getCurrentUser();
    String userId = currentUser.getId();
    String fullname = currentUser.getFullname();
    boolean serviceUser = currentUser.getRoles().stream().map(RoleDO::getNameEn)
        .anyMatch(role -> StringUtils.equals(role, WebappConstants.ROLE_SERVICE_USER));

    SensorNetworkPermBO sensorNetworkPermBO = new SensorNetworkPermBO();
    sensorNetworkPermBO.setUserId(userId);
    sensorNetworkPermBO.setUserName(fullname);
    sensorNetworkPermBO.setServiceUser(serviceUser);
    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> networkGroupIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    sensorNetworkPermDao.querySensorNetworkPerms(Lists.newArrayList(userId))
        .forEach(sensorNetworkPerm -> {
          if (StringUtils.isNotBlank(sensorNetworkPerm.getNetworkId())) {
            networkIds.add(sensorNetworkPerm.getNetworkId());
          } else {
            networkGroupIds.add(sensorNetworkPerm.getNetworkGroupId());
          }
        });
    sensorNetworkPermBO.setNetworkIds(CsvUtils.convertCollectionToCSV(networkIds));
    sensorNetworkPermBO.setNetworkGroupIds(CsvUtils.convertCollectionToCSV(networkGroupIds));

    return sensorNetworkPermBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService#updateSensorNetworkPerms(com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO, java.lang.String)
   */
  @Override
  @Transactional
  public int updateSensorNetworkPerms(SensorNetworkPermBO sensorNetworkPerm, String operatorId) {
    String userId = sensorNetworkPerm.getUserId();
    if (StringUtils.isAllBlank(sensorNetworkPerm.getNetworkIds(),
        sensorNetworkPerm.getNetworkGroupIds())) {
      // 删除用户所创建的所有业务
      serviceService.deleteServiceByUser(userId, operatorId);
      // 删除用户的所有网络（组）权限
      return sensorNetworkPermDao.deleteSensorNetworkPermByUser(userId);
    }

    // 用户原有的网络（组）权限
    List<String> orignNetworkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> orignNetworkGroupIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    sensorNetworkPermDao.querySensorNetworkPerms(Lists.newArrayList(userId))
        .forEach(networkPerm -> {
          if (StringUtils.isNotBlank(networkPerm.getNetworkId())) {
            orignNetworkIds.add(networkPerm.getNetworkId());
          } else {
            orignNetworkGroupIds.add(networkPerm.getNetworkGroupId());
          }
        });

    // 本次为用户配置的网络（组）权限
    List<String> currentNetworkIds = CsvUtils.convertCSVToList(sensorNetworkPerm.getNetworkIds());
    List<String> currentNetworkGroupIds = CsvUtils
        .convertCSVToList(sensorNetworkPerm.getNetworkGroupIds());

    // 用户被删减网络（组）权限时，需要同步清除用户创建的业务所关联的网络
    orignNetworkIds.removeAll(currentNetworkIds);
    orignNetworkGroupIds.removeAll(currentNetworkGroupIds);
    if (CollectionUtils.isNotEmpty(orignNetworkIds)
        || CollectionUtils.isNotEmpty(orignNetworkGroupIds)) {
      serviceService.updateServiceNetworks(userId, orignNetworkIds, orignNetworkGroupIds,
          operatorId);
    }

    // 更新用户网络（组）权限
    List<SensorNetworkPermDO> list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    list.addAll(currentNetworkIds.stream().map(networkId -> {
      SensorNetworkPermDO sensorNetworkPermDO = new SensorNetworkPermDO();
      sensorNetworkPermDO.setUserId(userId);
      sensorNetworkPermDO.setNetworkId(networkId);

      return sensorNetworkPermDO;
    }).collect(Collectors.toList()));
    list.addAll(currentNetworkGroupIds.stream().map(networkGroupId -> {
      SensorNetworkPermDO sensorNetworkPermDO = new SensorNetworkPermDO();
      sensorNetworkPermDO.setUserId(userId);
      sensorNetworkPermDO.setNetworkGroupId(networkGroupId);

      return sensorNetworkPermDO;
    }).collect(Collectors.toList()));

    int updateCount = 0;
    if (CollectionUtils.isNotEmpty(list)) {
      updateCount = sensorNetworkPermDao.updateSensorNetworkPerms(list);
    }

    return updateCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService#deleteSensorNetworkPermByNetwork(java.lang.String)
   */
  @Override
  public int deleteSensorNetworkPermByNetwork(String networkId) {
    return sensorNetworkPermDao.deleteSensorNetworkPermByNetwork(networkId);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService#deleteSensorNetworkPermByNetworkGroup(java.lang.String)
   */
  @Override
  public int deleteSensorNetworkPermByNetworkGroup(String networkGroupId) {
    return sensorNetworkPermDao.deleteSensorNetworkPermByNetworkGroup(networkGroupId);
  }

}
