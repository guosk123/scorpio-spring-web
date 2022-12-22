package com.machloop.fpc.cms.center.appliance.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.alpha.webapp.security.bo.LoggedUser;
import com.machloop.alpha.webapp.system.data.RoleDO;
import com.machloop.fpc.cms.center.appliance.bo.MetricSettingBO;
import com.machloop.fpc.cms.center.appliance.bo.ServiceBO;
import com.machloop.fpc.cms.center.appliance.bo.ServiceFollowBO;
import com.machloop.fpc.cms.center.appliance.bo.ServiceLinkBO;
import com.machloop.fpc.cms.center.appliance.dao.MetricSettingDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceFollowDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceLinkDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceNetworkDao;
import com.machloop.fpc.cms.center.appliance.data.MetricSettingDO;
import com.machloop.fpc.cms.center.appliance.data.ServiceDO;
import com.machloop.fpc.cms.center.appliance.data.ServiceFollowDO;
import com.machloop.fpc.cms.center.appliance.data.ServiceLinkDO;
import com.machloop.fpc.cms.center.appliance.data.ServiceNetworkDO;
import com.machloop.fpc.cms.center.appliance.service.AlertRuleService;
import com.machloop.fpc.cms.center.appliance.service.BaselineService;
import com.machloop.fpc.cms.center.appliance.service.MetricSettingService;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.knowledge.service.SaService;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
@Order(2)
@Transactional
@Service
public class ServiceServiceImpl
    implements ServiceService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_SERVICE,
      FpcCmsConstants.MQ_TAG_SERVICE_LINK);

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServiceImpl.class);

  private static final Pattern SPLIT_PATTERN = Pattern.compile("`((?:[^`])+)`|``",
      Pattern.MULTILINE);

  private static final String CSV_TITLE = "`名称`,`网络`,`网络组`,`应用配置`,`业务路径`,`描述`\n";

  private static final int MAX_SERVICE_NUMBER = 1000;

  private static final int IMPORT_LINE_LIMIT = 1000;

  private static final int MAX_NUMBER_APPLICATION_USED = 4;

  private static final int MAX_NAME_LENGTH = 30;
  private static final int MAX_DESCRIPTION_LENGTH = 255;

  @Autowired
  private ServiceDao serviceDao;

  @Autowired
  private ServiceNetworkDao serviceNetworkDao;

  @Autowired
  private ServiceFollowDao serviceFollowDao;

  @Autowired
  private ServiceLinkDao serviceLinkDao;

  @Autowired
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private SensorLogicalSubnetDao sensorLogicalSubnetDao;

  @Autowired
  private SensorNetworkGroupDao sensorNetworkGroupDao;

  @Autowired
  private SaService saService;

  @Autowired
  private MetricSettingService metricSettingService;

  @Autowired
  private MetricSettingDao metricSettingDao;

  @Autowired
  private BaselineService baselineService;

  @Autowired
  private AlertRuleService alertRuleService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private ApplicationContext context;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#queryServices(com.machloop.alpha.common.base.page.Pageable, java.lang.String)
   */
  @Override
  public Page<ServiceBO> queryServices(Pageable page, String name) {

    Map<String,
        String> networkNameMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<SensorNetworkDO> sensorNetworkList = sensorNetworkDao.querySensorNetworks();
    for (SensorNetworkDO sensorNetwork : sensorNetworkList) {
      networkNameMap.put(sensorNetwork.getNetworkInSensorId(),
          sensorNetwork.getName() == null ? sensorNetwork.getNetworkInSensorName()
              : sensorNetwork.getName());
    }

    Map<String, String> subnetNameMap = sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetDO::getId, SensorLogicalSubnetDO::getName));

    Map<String, String> networkGroupMap = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
        .collect(Collectors.toMap(SensorNetworkGroupDO::getId, SensorNetworkGroupDO::getName));

    Map<String, List<String>> serviceNetworkIds = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkNames = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkGroupIds = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkGroupNames = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
      // 网络ID
      List<String> list = serviceNetworkIds.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(list)) {
        list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      if (StringUtils.isNotEmpty(serviceNetwork.getNetworkId())) {
        list.add(serviceNetwork.getNetworkId());
        serviceNetworkIds.put(serviceNetwork.getServiceId(), list);
      }

      // 网络名称
      List<String> networkNames = serviceNetworkNames.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(networkNames)) {
        networkNames = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      String networkName = "";
      if (StringUtils.isBlank(
          networkName = MapUtils.getString(networkNameMap, serviceNetwork.getNetworkId(), ""))) {
        networkName = MapUtils.getString(subnetNameMap, serviceNetwork.getNetworkId(), "");
      }
      if (StringUtils.isNotBlank(networkName)) {
        networkNames.add(networkName);
        serviceNetworkNames.put(serviceNetwork.getServiceId(), networkNames);
      }

      // 网络组
      List<String> groupList = serviceNetworkGroupIds.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(groupList)) {
        groupList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      if (StringUtils.isNotBlank(serviceNetwork.getNetworkGroupId())) {
        groupList.add(serviceNetwork.getNetworkGroupId());
        serviceNetworkGroupIds.put(serviceNetwork.getServiceId(), groupList);
      }

      // 网络组名称
      List<String> networkGroupNames = serviceNetworkGroupNames.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(networkGroupNames)) {
        networkGroupNames = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      String networkGroupName = MapUtils.getString(networkGroupMap,
          serviceNetwork.getNetworkGroupId());
      if (StringUtils.isNotBlank(networkGroupName)) {
        networkGroupNames.add(networkGroupName);
        serviceNetworkGroupNames.put(serviceNetwork.getServiceId(), networkGroupNames);
      }

    });

    LoggedUser currentUser = LoggedUserContext.getCurrentUser();
    boolean serviceUser = currentUser.getRoles().stream().map(RoleDO::getNameEn)
        .anyMatch(role -> StringUtils.equals(role, WebappConstants.ROLE_SERVICE_USER));
    Page<ServiceDO> services = serviceDao.queryServices(page, name,
        serviceUser ? "" : currentUser.getId());

    List<ServiceBO> serviceBOList = services.getContent().stream().map(serviceDO -> {
      ServiceBO serviceBO = new ServiceBO();
      BeanUtils.copyProperties(serviceDO, serviceBO);
      serviceBO.setCreateTime(DateUtils.toStringISO8601(serviceDO.getCreateTime()));

      List<String> networkIds = serviceNetworkIds.get(serviceBO.getId());
      serviceBO.setNetworkIds(
          CollectionUtils.isEmpty(networkIds) ? "" : CsvUtils.convertCollectionToCSV(networkIds));
      List<String> networkNames = serviceNetworkNames.get(serviceBO.getId());
      serviceBO.setNetworkNames(CollectionUtils.isEmpty(networkNames) ? ""
          : CsvUtils.convertCollectionToCSV(networkNames));

      List<String> networkGroupIds = serviceNetworkGroupIds.get(serviceBO.getId());
      serviceBO.setNetworkGroupIds(CollectionUtils.isEmpty(networkGroupIds) ? ""
          : CsvUtils.convertCollectionToCSV(networkGroupIds));
      List<String> networkGroupNames = serviceNetworkGroupNames.get(serviceBO.getId());
      serviceBO.setNetworkGroupNames(CollectionUtils.isEmpty(networkGroupNames) ? ""
          : CsvUtils.convertCollectionToCSV(networkGroupNames));
      return serviceBO;
    }).collect(Collectors.toList());

    return new PageImpl<>(serviceBOList, page, services.getTotalElements());
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#queryServicesWithNetwork()
   */
  @Override
  public List<ServiceBO> queryServicesWithNetwork() {

    Map<String, String> serviceIdNameMap = serviceDao.queryServices(null).stream()
        .collect(Collectors.toMap(ServiceDO::getId, ServiceDO::getName));

    Map<String,
        String> networkNameMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<SensorNetworkDO> sensorNetworkList = sensorNetworkDao.querySensorNetworks();
    for (SensorNetworkDO sensorNetwork : sensorNetworkList) {
      networkNameMap.put(sensorNetwork.getNetworkInSensorId(),
          sensorNetwork.getName() == null ? sensorNetwork.getNetworkInSensorName()
              : sensorNetwork.getName());
    }
    Map<String, String> subnetNameMap = sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetDO::getId, SensorLogicalSubnetDO::getName));

    List<ServiceBO> result = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
      if (StringUtils.contains(serviceNetwork.getNetworkId(), "^")) {
        // 此类情况network_id格式为：network_id^subnet_id
        String[] networkIdSubnetId = StringUtils.split(serviceNetwork.getNetworkId(), "^");
        ServiceBO serviceWithSubnetBO = new ServiceBO();
        serviceWithSubnetBO.setId(serviceNetwork.getServiceId());
        serviceWithSubnetBO.setName(MapUtils.getString(serviceIdNameMap, serviceNetwork.getServiceId()));
        serviceWithSubnetBO.setNetworkIds(networkIdSubnetId[1]);
        serviceWithSubnetBO
            .setNetworkNames(MapUtils.getString(subnetNameMap, networkIdSubnetId[1]));
        result.add(serviceWithSubnetBO);
      } else {
        // 此类情况network_id格式为：network_id
        ServiceBO serviceWithNetworkBO = new ServiceBO();
        serviceWithNetworkBO.setId(serviceNetwork.getServiceId());
        serviceWithNetworkBO.setName(MapUtils.getString(serviceIdNameMap, serviceNetwork.getServiceId()));
        serviceWithNetworkBO.setNetworkIds(serviceNetwork.getNetworkId());
        serviceWithNetworkBO
            .setNetworkNames(MapUtils.getString(networkNameMap, serviceNetwork.getNetworkId()));
        result.add(serviceWithNetworkBO);
      }
    });
    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#queryServices()
   */
  @Override
  public List<ServiceBO> queryServices() {

    Map<String,
        String> networkNameMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<SensorNetworkDO> sensorNetworkList = sensorNetworkDao.querySensorNetworks();
    for (SensorNetworkDO sensorNetwork : sensorNetworkList) {
      networkNameMap.put(sensorNetwork.getNetworkInSensorId(),
          sensorNetwork.getName() == null ? sensorNetwork.getNetworkInSensorName()
              : sensorNetwork.getName());
    }

    Map<String, String> subnetNameMap = sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetDO::getId, SensorLogicalSubnetDO::getName));

    Map<String, String> networkGroupMap = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
        .collect(Collectors.toMap(SensorNetworkGroupDO::getId, SensorNetworkGroupDO::getName));

    Map<String, List<String>> serviceNetworkIds = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkNames = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkGroupIds = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkGroupNames = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
      // 网络ID
      List<String> list = serviceNetworkIds.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(list)) {
        list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      if (StringUtils.isNotEmpty(serviceNetwork.getNetworkId())) {
        list.add(serviceNetwork.getNetworkId());
        serviceNetworkIds.put(serviceNetwork.getServiceId(), list);
      }

      // 网络名称
      List<String> networkNames = serviceNetworkNames.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(networkNames)) {
        networkNames = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      String networkName = "";
      if (StringUtils.isBlank(
          networkName = MapUtils.getString(networkNameMap, serviceNetwork.getNetworkId(), ""))) {
        networkName = MapUtils.getString(subnetNameMap, serviceNetwork.getNetworkId(), "");
      }
      if (StringUtils.isNotBlank(networkName)) {
        networkNames.add(networkName);
        serviceNetworkNames.put(serviceNetwork.getServiceId(), networkNames);
      }

      // 网络组
      List<String> groupList = serviceNetworkGroupIds.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(groupList)) {
        groupList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      if (StringUtils.isNotBlank(serviceNetwork.getNetworkGroupId())) {
        groupList.add(serviceNetwork.getNetworkGroupId());
        serviceNetworkGroupIds.put(serviceNetwork.getServiceId(), groupList);
      }

      // 网络组名称
      List<String> networkGroupNames = serviceNetworkGroupNames.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(networkGroupNames)) {
        networkGroupNames = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      String networkGroupName = MapUtils.getString(networkGroupMap,
          serviceNetwork.getNetworkGroupId());
      if (StringUtils.isNotBlank(networkGroupName)) {
        networkGroupNames.add(networkGroupName);
        serviceNetworkGroupNames.put(serviceNetwork.getServiceId(), networkGroupNames);
      }

    });

    List<ServiceDO> services = serviceDao.queryServices(null);
    List<ServiceBO> result = Lists.newArrayListWithCapacity(services.size());
    services.forEach(serviceDO -> {
      ServiceBO serviceBO = new ServiceBO();
      BeanUtils.copyProperties(serviceDO, serviceBO);
      serviceBO.setCreateTime(DateUtils.toStringISO8601(serviceDO.getCreateTime()));

      List<String> networkIds = serviceNetworkIds.get(serviceBO.getId());
      serviceBO.setNetworkIds(
          CollectionUtils.isEmpty(networkIds) ? "" : CsvUtils.convertCollectionToCSV(networkIds));
      List<String> networkNames = serviceNetworkNames.get(serviceBO.getId());
      serviceBO.setNetworkNames(CollectionUtils.isEmpty(networkNames) ? ""
          : CsvUtils.convertCollectionToCSV(networkNames));
      List<String> networkGroupIds = serviceNetworkGroupIds.get(serviceBO.getId());
      serviceBO.setNetworkGroupIds(CollectionUtils.isEmpty(networkGroupIds) ? ""
          : CsvUtils.convertCollectionToCSV(networkGroupIds));
      List<String> networkGroupNames = serviceNetworkGroupNames.get(serviceBO.getId());
      serviceBO.setNetworkGroupNames(CollectionUtils.isEmpty(networkGroupNames) ? ""
          : CsvUtils.convertCollectionToCSV(networkGroupNames));
      result.add(serviceBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#queryServicesBasicInfo()
   */
  @Override
  public List<ServiceBO> queryServicesBasicInfo() {
    List<ServiceDO> services = serviceDao.queryServices(null);
    Map<String,
        List<String>> serviceNetworks = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
      List<String> list = serviceNetworks.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(list)) {
        list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      list.add(serviceNetwork.getNetworkId());
      serviceNetworks.put(serviceNetwork.getServiceId(), list);
    });

    List<ServiceBO> result = Lists.newArrayListWithCapacity(services.size());
    services.forEach(serviceDO -> {
      ServiceBO serviceBO = new ServiceBO();
      BeanUtils.copyProperties(serviceDO, serviceBO);
      serviceBO.setCreateTime(DateUtils.toStringISO8601(serviceDO.getCreateTime()));
      List<String> networkIds = serviceNetworks.get(serviceBO.getId());
      serviceBO.setNetworkIds(
          CollectionUtils.isEmpty(networkIds) ? "" : CsvUtils.convertCollectionToCSV(networkIds));

      result.add(serviceBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#queryServiceByAppId(java.lang.String)
   */
  @Override
  public List<ServiceBO> queryServiceByAppId(String applicationId) {
    List<ServiceDO> services = serviceDao.queryServices(null);
    List<ServiceBO> list = services.stream()
        .filter(
            service -> CsvUtils.convertCSVToList(service.getApplication()).contains(applicationId))
        .map(serviceDO -> {
          ServiceBO serviceBO = new ServiceBO();
          BeanUtils.copyProperties(serviceDO, serviceBO);

          return serviceBO;
        }).collect(Collectors.toList());

    return list;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#queryService(java.lang.String)
   */
  @Override
  public ServiceBO queryService(String id) {
    ServiceDO serviceDO = serviceDao.queryService(id);

    ServiceBO serviceBO = new ServiceBO();
    BeanUtils.copyProperties(serviceDO, serviceBO);
    serviceBO.setCreateTime(DateUtils.toStringISO8601(serviceDO.getCreateTime()));

    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> networkGroupIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<ServiceNetworkDO> serviceNetworks = serviceNetworkDao.queryServiceNetworks(id, null);
    serviceNetworks.forEach(serviceNetwork -> {
      if (StringUtils.isNotBlank(serviceNetwork.getNetworkId())) {
        networkIds.add(serviceNetwork.getNetworkId());
      } else {
        networkGroupIds.add(serviceNetwork.getNetworkGroupId());
      }
    });

    serviceBO.setNetworkIds(StringUtils.join(networkIds, ","));
    serviceBO.setNetworkGroupIds(StringUtils.join(networkGroupIds, ","));

    return serviceBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#exportServices()
   */
  @Override
  public List<String> exportServices() {

    Map<String, String> networkDict = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<SensorNetworkDO> sensorNetworkList = sensorNetworkDao.querySensorNetworks();
    for (SensorNetworkDO sensorNetwork : sensorNetworkList) {
      networkDict.put(sensorNetwork.getNetworkInSensorId(),
          StringUtils.isBlank(sensorNetwork.getName()) ? sensorNetwork.getNetworkInSensorName()
              : sensorNetwork.getName());
    }
    Map<String, String> subnetDict = sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetDO::getId, SensorLogicalSubnetDO::getName));
    Map<String, String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
        .collect(Collectors.toMap(SensorNetworkGroupDO::getId, SensorNetworkGroupDO::getName));
    Map<Integer, String> appDict = saService.queryAllAppsIdNameMapping();

    // 业务关联网络|网络组
    Map<String,
        List<String>> serviceNetworks = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkGroups = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
      if (StringUtils.isBlank(serviceNetwork.getNetworkId())) {
        // 关联网络组
        List<String> networkGroupNames = serviceNetworkGroups.getOrDefault(
            serviceNetwork.getServiceId(),
            Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
        String networkGroupName = MapUtils.getString(networkGroupDict,
            serviceNetwork.getNetworkGroupId(), "");
        if (StringUtils.isNotBlank(networkGroupName)) {
          networkGroupNames.add(networkGroupName);
          serviceNetworkGroups.put(serviceNetwork.getServiceId(), networkGroupNames);
        }
      } else {
        // 关联网络|子网
        List<String> networkNames = serviceNetworks.getOrDefault(serviceNetwork.getServiceId(),
            Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
        String networkName = MapUtils.getString(networkDict, serviceNetwork.getNetworkId(), "");
        networkName = StringUtils.isBlank(networkName)
            ? MapUtils.getString(subnetDict, serviceNetwork.getNetworkId(), "")
            : networkName;
        if (StringUtils.isNotBlank(networkName)) {
          networkNames.add(networkName);
          serviceNetworks.put(serviceNetwork.getServiceId(), networkNames);
        }
      }
    });

    // 业务关联业务路径
    Map<String, String> serviceLinks = serviceLinkDao.queryServiceLinks().stream()
        .collect(Collectors.toMap(ServiceLinkDO::getServiceId, serviceLink -> {
          Map<String,
              String> serviceLinkMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          serviceLinkMap.put("link", serviceLink.getLink());
          serviceLinkMap.put("metric", serviceLink.getMetric());

          return Base64Utils.encode(JsonHelper.serialize(serviceLinkMap, false));
        }));

    List<ServiceDO> services = serviceDao.queryServices(null);
    LoggedUser currentUser = LoggedUserContext.getCurrentUser();
    boolean serviceUser = currentUser.getRoles().stream().map(RoleDO::getNameEn)
        .anyMatch(role -> StringUtils.equals(role, WebappConstants.ROLE_SERVICE_USER));
    if (!serviceUser) {
      services = services.stream()
          .filter(service -> StringUtils.equals(service.getCreaterId(), currentUser.getId()))
          .collect(Collectors.toList());
    }

    List<String> result = Lists.newArrayListWithCapacity(services.size() + 1);
    result.add(CSV_TITLE);
    services.forEach(service -> {
      List<String> appNames = CsvUtils.convertCSVToList(service.getApplication()).stream()
          .map(appId -> appDict.get(Integer.parseInt(appId))).collect(Collectors.toList());
      String oneItem = CsvUtils.spliceRowData(service.getName(),
          StringUtils.join(serviceNetworks.get(service.getId()), "|"),
          StringUtils.join(serviceNetworkGroups.get(service.getId()), "|"),
          StringUtils.join(appNames, "|"), serviceLinks.getOrDefault(service.getId(), ""),
          service.getDescription());
      result.add(oneItem);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#importServices(org.springframework.web.multipart.MultipartFile, java.lang.String)
   */
  @Transactional
  @Override
  public synchronized int importServices(MultipartFile file, String operatorId) {
    LOGGER.info("begin to import service, file name :{}", file.getOriginalFilename());

    List<ServiceDO> existServices = serviceDao.queryServices(null);
    List<String> existServiceNames = existServices.stream().map(service -> service.getName())
        .collect(Collectors.toList());
    int existServiceNumber = existServices.size();

    // 主网络<name:id>
    Map<String, String> networkDict = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<SensorNetworkDO> sensorNetworkList = sensorNetworkDao.querySensorNetworks();
    for (SensorNetworkDO sensorNetwork : sensorNetworkList) {
      networkDict.put(sensorNetwork.getName() == null ? sensorNetwork.getNetworkInSensorName()
          : sensorNetwork.getName(), sensorNetwork.getNetworkInSensorId());
    }
    // 子网络<name:id>
    Map<String, String> subnetDict = sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetDO::getName, SensorLogicalSubnetDO::getId));
    // 网络组<name:id>
    Map<String, String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
        .collect(Collectors.toMap(SensorNetworkGroupDO::getName, SensorNetworkGroupDO::getId));

    // 应用<name:id>
    Map<String, Integer> appDict = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<Integer, String> appIdNames = saService.queryAllAppsIdNameMapping();
    appIdNames.entrySet().forEach(entry -> {
      appDict.put(entry.getValue(), entry.getKey());
    });

    // 查询各个应用出现次数
    Map<String, Integer> appCounts = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    existServices.forEach(service -> {
      CsvUtils.convertCSVToList(service.getApplication()).forEach(appId -> {
        String appName = appIdNames.get(Integer.parseInt(appId));
        appCounts.put(appName, appCounts.getOrDefault(appName, 0) + 1);
      });
    });

    List<ServiceDO> serviceList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<ServiceNetworkDO> serviceNetworkList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<
        ServiceLinkDO> serviceLinkList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    String line = "";
    int lineNumber = 0;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;

        if (lineNumber > (IMPORT_LINE_LIMIT + 1)) {
          LOGGER.warn("import file error, limit exceeded.");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容超出" + IMPORT_LINE_LIMIT + "条");
        }

        // 跳过首行
        if (lineNumber == 1) {
          LOGGER.info("pass title, line: [{}]", line);
          continue;
        }

        // 解析每一列数据
        List<String> contents = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        Matcher matcher = SPLIT_PATTERN.matcher(line);
        while (matcher.find()) {
          String fieldContext = StringUtils
              .substringBeforeLast(StringUtils.substringAfter(matcher.group(), "`"), "`");
          contents.add(
              StringUtils.replace(StringUtils.replace(fieldContext, "\\`", "`"), "\\r\\n", "\r\n"));
        }

        if (contents.size() == 0) {
          LOGGER.info("skip blank line, line number: [{}]", lineNumber);
          continue;
        } else if (contents.size() != CsvUtils.convertCSVToList(CSV_TITLE).size()) {
          LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber + ", 内容: " + line);
        }

        String name = StringUtils.trim(contents.get(0));
        String networks = StringUtils.trim(contents.get(1));
        String networkGroups = StringUtils.trim(contents.get(2));
        String applications = StringUtils.trim(contents.get(3));
        String serviceLink = StringUtils.trim(contents.get(4));
        String description = StringUtils.trim(contents.get(5));
        if (StringUtils.isAnyBlank(name, applications)
            || StringUtils.isAllBlank(networks, networkGroups)) {
          LOGGER.warn("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber);
        }

        // 校验名称是否重复
        if (name.length() > MAX_NAME_LENGTH) {
          LOGGER.warn("import file error, name length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 业务名称长度超出限制：" + MAX_NAME_LENGTH + ", 行号: " + lineNumber);
        }
        if (existServiceNames.contains(name)) {
          LOGGER.warn("import file error, name exist, lineNumber: {}, content: {}", lineNumber,
              line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 业务名称已存在, 行号: " + lineNumber);
        }
        existServiceNames.add(name);

        // 校验网络是否存在
        List<String> selectedNetworks = Lists.newArrayList(StringUtils.split(networks, "|"));
        List<String> selectedNetworkBack = Lists.newArrayList(selectedNetworks);
        selectedNetworks.removeAll(networkDict.keySet());
        selectedNetworks.removeAll(subnetDict.keySet());
        if (CollectionUtils.isNotEmpty(selectedNetworks)) {
          LOGGER.warn("import file error, network not exist, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 不存在的网络[" + StringUtils.join(selectedNetworks, "|") + "], 行号: " + lineNumber);
        }

        // 校验网络组是否存在
        List<String> selectedNetworkGroups = Lists
            .newArrayList(StringUtils.split(networkGroups, "|"));
        List<String> selectedNetworkGroupBack = Lists.newArrayList(selectedNetworkGroups);
        selectedNetworkGroups.removeAll(networkGroupDict.keySet());
        if (CollectionUtils.isNotEmpty(selectedNetworkGroups)) {
          LOGGER.warn("import file error, networkGroup not exist, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "导入失败, 不存在的网络组["
              + StringUtils.join(selectedNetworkGroups, "|") + "], 行号: " + lineNumber);
        }

        // 校验应用是否存在
        List<String> selectedAppNames = Lists.newArrayList(StringUtils.split(applications, "|"));
        List<String> selectedAppNameBack = Lists.newArrayList(selectedAppNames);
        selectedAppNames.removeAll(appDict.keySet());
        if (CollectionUtils.isNotEmpty(selectedAppNames)) {
          LOGGER.warn("import file error, application not exist, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 不存在的应用[" + StringUtils.join(selectedAppNames, "|") + "], 行号: " + lineNumber);
        }

        // 校验应用是否超过可配置业务数量
        selectedAppNameBack.forEach(appName -> {
          if (appCounts.getOrDefault(appName, 0) >= MAX_NUMBER_APPLICATION_USED) {
            throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
                String.format("应用[%s]已超过最大可配置业务数量[%s]", appName, MAX_NUMBER_APPLICATION_USED));
          }
          appCounts.put(appName, appCounts.getOrDefault(appName, 0) + 1);
        });

        // 校验描述长度
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
          LOGGER.warn(
              "import file error, description length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 描述长度超出限制：" + MAX_DESCRIPTION_LENGTH + ", 行号: " + lineNumber);
        }

        // 业务基本属性
        ServiceDO serviceDO = new ServiceDO();
        serviceDO.setId(IdGenerator.generateUUID());
        serviceDO.setName(name);
        serviceDO.setApplication(StringUtils.join(selectedAppNameBack.stream()
            .map(appName -> appDict.get(appName)).collect(Collectors.toList()), ","));
        serviceDO.setDescription(description);
        serviceDO.setCreaterId(operatorId);
        serviceDO.setOperatorId(operatorId);
        serviceList.add(serviceDO);

        // 业务网络关联关系
        selectedNetworkBack.forEach(networkName -> {
          ServiceNetworkDO serviceNetwork = new ServiceNetworkDO();
          String networkId = networkDict.get(networkName);
          String subnetId = subnetDict.get(networkName);
          serviceNetwork.setNetworkId(StringUtils.isNotBlank(networkId) ? networkId : subnetId);
          serviceNetwork.setServiceId(serviceDO.getId());
          serviceNetworkList.add(serviceNetwork);
        });

        // 业务网络组关联关系
        selectedNetworkGroupBack.forEach(networkGroupName -> {
          ServiceNetworkDO serviceNetwork = new ServiceNetworkDO();
          String networkGroupId = networkGroupDict.get(networkGroupName);
          serviceNetwork.setNetworkGroupId(networkGroupId);
          serviceNetwork.setServiceId(serviceDO.getId());
          serviceNetworkList.add(serviceNetwork);
        });

        // 业务路径
        if (StringUtils.isNotBlank(serviceLink)) {
          Map<String, String> serviceLinkMap = JsonHelper.deserialize(
              Base64Utils.decode(serviceLink), new TypeReference<Map<String, String>>() {
              }, false);
          String link = serviceLinkMap.get("link");
          String metric = serviceLinkMap.get("metric");
          if (StringUtils.isNotBlank(link)) {
            ServiceLinkDO serviceLinkDO = new ServiceLinkDO();
            serviceLinkDO.setServiceId(serviceDO.getId());
            serviceLinkDO.setLink(link);
            serviceLinkDO.setMetric(metric);
            serviceLinkDO.setOperatorId(operatorId);
            serviceLinkList.add(serviceLinkDO);
          } else {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "业务路径配置缺失");
          }
        }
      }
    } catch (IOException e) {
      LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    if (serviceList.isEmpty() || serviceNetworkList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件导入失败，未找到业务数据");
    }

    if (serviceList.size() > (MAX_SERVICE_NUMBER - existServiceNumber)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          String.format("业务数量已超过最大上限[%s]", MAX_SERVICE_NUMBER));
    }

    // 保存业务信息
    int importCount = serviceDao.batchSaveService(serviceList);

    // 保存业务配置的网络与网络组关联关系
    serviceNetworkDao.batchSaveServiceNetwork(serviceNetworkList);

    // 配置业务默认的统计度量值
    List<MetricSettingBO> metricSettings = serviceNetworkList.stream().map(serviceNetwork -> {
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setSourceType(FpcCmsConstants.SOURCE_TYPE_SERVICE);
      metricSettingBO.setNetworkId(serviceNetwork.getNetworkId());
      metricSettingBO.setServiceId(serviceNetwork.getServiceId());

      return metricSettingBO;
    }).collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(metricSettings)) {
      metricSettingService.saveDefaultMetricSettings(metricSettings, operatorId);
    }

    // 保存业务路径
    if (CollectionUtils.isNotEmpty(serviceLinkList)) {
      serviceLinkDao.batchSaveServiceLink(serviceLinkList);
    }

    LOGGER.info("success to import service.total: [{}]", importCount);

    // 业务和网络、网络组关联集合
    Map<String, List<ServiceNetworkDO>> serviceNetworkMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceNetworkList.forEach(serviceNetwork -> {
      List<ServiceNetworkDO> list = serviceNetworkMap.getOrDefault(serviceNetwork.getServiceId(),
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
      list.add(serviceNetwork);

      serviceNetworkMap.put(serviceNetwork.getServiceId(), list);
    });

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = serviceList.stream().map(service -> {
      List<MetricSettingDO> metricSettingList = metricSettingDao
          .queryMetricSettings(FpcCmsConstants.SOURCE_TYPE_SERVICE, null, service.getId(), null);

      return service2MessageBody(service, serviceNetworkMap.get(service.getId()), metricSettingList,
          FpcCmsConstants.SYNC_ACTION_ADD);
    }).collect(Collectors.toList());
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SERVICE, null);

    return importCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#saveService(com.machloop.fpc.cms.center.appliance.bo.ServiceBO, java.util.List, java.lang.String)
   */
  @Transactional
  @Override
  public ServiceBO saveService(ServiceBO serviceBO, List<MetricSettingBO> metricSettings,
      String operatorId) {
    ServiceDO exist = serviceDao.queryServiceByName(serviceBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "业务名称不能重复");
    }

    List<ServiceDO> services = serviceDao.queryServices(null);
    if (services.size() >= MAX_SERVICE_NUMBER) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          String.format("业务数量已超过最大上限[%s]", MAX_SERVICE_NUMBER));
    }

    // 查询各个应用出现次数
    Map<String, Integer> appCounts = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    services.forEach(service -> {
      CsvUtils.convertCSVToList(service.getApplication()).forEach(appId -> {
        appCounts.put(appId, appCounts.getOrDefault(appId, 0) + 1);
      });
    });
    Map<Integer, String> appDict = saService.queryAllAppsIdNameMapping();
    CsvUtils.convertCSVToList(serviceBO.getApplication()).forEach(appId -> {
      if (!appDict.containsKey(Integer.parseInt(appId))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND,
            String.format("应用ID[%s]不存在", appId));
      }
      if (appCounts.getOrDefault(appId, 0) >= MAX_NUMBER_APPLICATION_USED) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            String.format("应用[%s]已超过最大可配置业务数量[%s]", appDict.get(Integer.parseInt(appId)),
                MAX_NUMBER_APPLICATION_USED));
      }
      appCounts.put(appId, appCounts.getOrDefault(appId, 0) + 1);
    });

    ServiceDO serviceDO = new ServiceDO();
    BeanUtils.copyProperties(serviceBO, serviceDO);
    serviceDO.setCreaterId(operatorId);
    serviceDO.setOperatorId(operatorId);
    // 保存业务基本属性
    serviceDao.saveOrRecoverService(serviceDO);

    // 保存配置的网络关联关系
    List<ServiceNetworkDO> serviceNetworks = CsvUtils.convertCSVToList(serviceBO.getNetworkIds())
        .stream().map(networkId -> {
          ServiceNetworkDO serviceNetworkDO = new ServiceNetworkDO();
          serviceNetworkDO.setNetworkId(networkId);
          serviceNetworkDO.setServiceId(serviceDO.getId());

          return serviceNetworkDO;
        }).collect(Collectors.toList());

    // 保存配置的网络组关联关系
    List<ServiceNetworkDO> serviceNetworkGroups = CsvUtils
        .convertCSVToList(serviceBO.getNetworkGroupIds()).stream().map(networkGroupId -> {
          ServiceNetworkDO serviceNetworkDO = new ServiceNetworkDO();
          serviceNetworkDO.setNetworkGroupId(networkGroupId);
          serviceNetworkDO.setServiceId(serviceDO.getId());

          return serviceNetworkDO;
        }).collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(serviceNetworks)) {
      serviceNetworkDao.mergeServiceNetworks(serviceNetworks);
    } else {
      serviceNetworkDao.mergeServiceNetworks(serviceNetworkGroups);
    }

    if (CollectionUtils.isEmpty(metricSettings)) {
      // 配置业务默认的统计度量值
      metricSettings = serviceNetworks.stream().map(serviceNetwork -> {
        MetricSettingBO metricSettingBO = new MetricSettingBO();
        metricSettingBO.setSourceType(FpcCmsConstants.SOURCE_TYPE_SERVICE);
        metricSettingBO.setNetworkId(serviceNetwork.getNetworkId());
        metricSettingBO.setServiceId(serviceNetwork.getServiceId());

        return metricSettingBO;
      }).collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(metricSettings)) {
        metricSettingService.saveDefaultMetricSettings(metricSettings, operatorId);
      }
    } else {
      // 配置指定的统计度量值
      metricSettings.forEach(metricSetting -> {
        metricSetting.setServiceId(serviceDO.getId());
      });

      metricSettingService.saveMetricSettings(metricSettings, operatorId);
    }

    // 下发到直属fpc和cms
    List<MetricSettingDO> metricSetting = metricSettingDao
        .queryMetricSettings(FpcCmsConstants.SOURCE_TYPE_SERVICE, null, serviceDO.getId(), null);
    List<ServiceNetworkDO> serviceNetworkList = serviceNetworkDao
        .queryServiceNetworks(Lists.newArrayList(serviceDO.getId()));
    List<Map<String, Object>> messageBodys = Lists.newArrayList(service2MessageBody(serviceDO,
        serviceNetworkList, metricSetting, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SERVICE, null);

    return queryService(serviceDO.getId());
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#updateService(java.lang.String, com.machloop.fpc.cms.center.appliance.bo.ServiceBO, java.lang.String)
   */
  @Transactional
  @Override
  public ServiceBO updateService(String id, ServiceBO serviceBO, String operatorId) {
    ServiceDO exist = serviceDao.queryService(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "业务不存在");
    }

    ServiceDO serviceByName = serviceDao.queryServiceByName(serviceBO.getName());
    if (StringUtils.isNotBlank(serviceByName.getId())
        && !StringUtils.equals(id, serviceByName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "业务名称不能重复");
    }

    // 查询各个应用出现次数
    Map<String, Integer> appCounts = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceDao.queryServices(null).forEach(service -> {
      CsvUtils.convertCSVToList(service.getApplication()).forEach(appId -> {
        appCounts.put(appId, appCounts.getOrDefault(appId, 0) + 1);
      });
    });

    Map<Integer, String> appDict = saService.queryAllAppsIdNameMapping();
    List<String> currentAppIds = CsvUtils.convertCSVToList(serviceBO.getApplication());
    currentAppIds.removeAll(CsvUtils.convertCSVToList(exist.getApplication()));
    currentAppIds.forEach(appId -> {
      if (!appDict.containsKey(Integer.parseInt(appId))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND,
            String.format("应用ID[%s]不存在", appId));
      }
      if (appCounts.getOrDefault(appId, 0) >= MAX_NUMBER_APPLICATION_USED) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            String.format("应用[%s]已超过最大可配置业务数量[%s]", appDict.get(Integer.parseInt(appId)),
                MAX_NUMBER_APPLICATION_USED));
      }
      appCounts.put(appId, appCounts.getOrDefault(appId, 0) + 1);
    });

    // 原业务所在网络集合
    List<String> oldNetworkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> oldNetworkGroupIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    serviceNetworkDao.queryServiceNetworks(id, null).forEach(serviceNetwork -> {
      if (StringUtils.isNotBlank(serviceNetwork.getNetworkId())) {
        oldNetworkIds.add(serviceNetwork.getNetworkId());
      } else {
        oldNetworkGroupIds.add(serviceNetwork.getNetworkGroupId());
      }
    });

    ServiceDO serviceDO = new ServiceDO();
    serviceBO.setId(id);
    BeanUtils.copyProperties(serviceBO, serviceDO);
    serviceDO.setOperatorId(operatorId);
    // 保存业务基本属性
    serviceDao.updateService(serviceDO);

    // 保存配置的网络关联关系
    List<String> newNetworkIds = CsvUtils.convertCSVToList(serviceBO.getNetworkIds());
    List<ServiceNetworkDO> serviceNetworks = newNetworkIds.stream().map(networkId -> {
      ServiceNetworkDO serviceNetworkDO = new ServiceNetworkDO();
      serviceNetworkDO.setNetworkId(networkId);
      serviceNetworkDO.setServiceId(id);

      return serviceNetworkDO;
    }).collect(Collectors.toList());

    // 保存配置的网络组关联关系
    List<String> newNetworkGroupIds = CsvUtils.convertCSVToList(serviceBO.getNetworkGroupIds());
    List<
        ServiceNetworkDO> serviceNetworkGroups = newNetworkGroupIds.stream().map(networkGroupId -> {
          ServiceNetworkDO serviceNetworkDO = new ServiceNetworkDO();
          serviceNetworkDO.setNetworkGroupId(networkGroupId);
          serviceNetworkDO.setServiceId(id);

          return serviceNetworkDO;
        }).collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(serviceNetworks)) {
      serviceNetworkDao.mergeServiceNetworks(serviceNetworks);
    } else {
      serviceNetworkDao.mergeServiceNetworks(serviceNetworkGroups);
    }

    // 业务移除的网络
    List<String> removeNetworkIds = Lists.newArrayList(oldNetworkIds);
    removeNetworkIds.removeAll(newNetworkIds);
    // 业务新增的网络
    newNetworkIds.removeAll(oldNetworkIds);
    // 业务移除的网络组
    List<String> removeNetworkGroupIds = Lists.newArrayList(oldNetworkGroupIds);
    removeNetworkGroupIds.removeAll(newNetworkGroupIds);
    // 业务新增的网络组
    newNetworkGroupIds.removeAll(oldNetworkGroupIds);

    // 业务配置网络（组）变更时，删除已经移除网络（组）的关注
    removeNetworkIds.forEach(networkId -> {
      serviceFollowDao.deleteServiceFollow(null, id, networkId, null);
    });
    removeNetworkGroupIds.forEach(networkGroupId -> {
      serviceFollowDao.deleteServiceFollow(null, id, null, networkGroupId);
    });

    // 业务配置网络变更时，变更统计度量值
    List<MetricSettingBO> newMetricSettings = newNetworkIds.stream().map(networkId -> {
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setSourceType(FpcCmsConstants.SOURCE_TYPE_SERVICE);
      metricSettingBO.setNetworkId(networkId);
      metricSettingBO.setServiceId(id);

      return metricSettingBO;
    }).collect(Collectors.toList());
    // 新增
    if (CollectionUtils.isNotEmpty(newMetricSettings)) {
      metricSettingService.saveDefaultMetricSettings(newMetricSettings, operatorId);
    }
    // 删除
    removeNetworkIds.forEach(networkId -> {
      metricSettingService.deleteMetricSetting(FpcCmsConstants.SOURCE_TYPE_SERVICE, networkId, id,
          null);
    });

    // 下发到直属fpc和cms
    List<MetricSettingDO> metricSettings = metricSettingDao
        .queryMetricSettings(FpcCmsConstants.SOURCE_TYPE_SERVICE, null, serviceDO.getId(), null);
    List<ServiceNetworkDO> serviceNetworkList = serviceNetworkDao
        .queryServiceNetworks(Lists.newArrayList(serviceDO.getId()));
    List<Map<String, Object>> messageBodys = Lists.newArrayList(service2MessageBody(serviceDO,
        serviceNetworkList, metricSettings, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SERVICE, null);

    return serviceBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#updateServiceNetworks(java.lang.String, java.util.List, java.util.List, java.lang.String)
   */
  @Override
  public void updateServiceNetworks(String userId, List<String> removeNetworkIds,
      List<String> removeNetworkGroupIds, String operatorId) {
    List<ServiceDO> services = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(userId)) {
      // 查看该用户创建的所有业务
      services.addAll(serviceDao.queryServiceByUser(userId));
    } else {
      services.addAll(serviceDao.queryServices(null));
    }
    if (services.isEmpty()) {
      return;
    }

    // 业务关联的网络
    List<String> serviceIds = services.stream().map(ServiceDO::getId).collect(Collectors.toList());
    Map<String,
        List<ServiceNetworkDO>> serviceNetworks = serviceNetworkDao.queryServiceNetworks(serviceIds)
            .stream().collect(Collectors.groupingBy(ServiceNetworkDO::getServiceId));

    services.stream().filter(service -> serviceNetworks.containsKey(service.getId()))
        .forEach(service -> {
          List<ServiceNetworkDO> list = serviceNetworks.get(service.getId());
          String networkGroupId = list.get(0).getNetworkGroupId();
          if (StringUtils.isNotBlank(networkGroupId)) {
            List<String> serviceNetworkGroupIds = list.stream()
                .map(ServiceNetworkDO::getNetworkGroupId).collect(Collectors.toList());
            int size = serviceNetworkGroupIds.size();
            serviceNetworkGroupIds.removeAll(removeNetworkGroupIds);
            if (serviceNetworkGroupIds.size() == 0) {
              deleteService(service.getId(), operatorId, true);
            } else if (serviceNetworkGroupIds.size() < size) {
              ServiceBO serviceBO = new ServiceBO();
              BeanUtils.copyProperties(service, serviceBO);
              serviceBO.setNetworkGroupIds(CsvUtils.convertCollectionToCSV(serviceNetworkGroupIds));
              updateService(service.getId(), serviceBO, operatorId);
            }
          } else {
            List<String> serviceNetworkIds = list.stream().map(ServiceNetworkDO::getNetworkId)
                .collect(Collectors.toList());
            int size = serviceNetworkIds.size();
            serviceNetworkIds.removeAll(removeNetworkIds);
            if (serviceNetworkIds.size() == 0) {
              deleteService(service.getId(), operatorId, true);
            } else if (serviceNetworkIds.size() < size) {
              ServiceBO serviceBO = new ServiceBO();
              BeanUtils.copyProperties(service, serviceBO);
              serviceBO.setNetworkIds(CsvUtils.convertCollectionToCSV(serviceNetworkIds));
              updateService(service.getId(), serviceBO, operatorId);
            }
          }
        });
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#deleteService(java.lang.String, java.lang.String, boolean)
   */
  @Transactional
  @Override
  public ServiceBO deleteService(String id, String operatorId, boolean forceDelete) {
    ServiceDO exist = serviceDao.queryService(id);
    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "业务不存在");
    }

    // 是否有告警作用于该业务
    if (!forceDelete && alertRuleService
        .queryAlertRulesBySource(FpcCmsConstants.SOURCE_TYPE_SERVICE, null, id).size() > 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该业务已作用于告警，无法删除");
    }

    // 删除业务
    serviceDao.deleteService(id, operatorId);

    // 删除业务配置网络
    serviceNetworkDao.deleteServiceNetwork(id);

    // 删除用户关注
    serviceFollowDao.deleteServiceFollow(id);

    // 删除业务下基线定义
    baselineService.deleteBaselineSettings(FpcCmsConstants.SOURCE_TYPE_SERVICE, null, null, id);

    // 删除业务下的统计配置
    metricSettingService.deleteMetricSetting(FpcCmsConstants.SOURCE_TYPE_SERVICE, null, id, null);

    // 删除业务路径
    serviceLinkDao.deleteServiceLink(id);

    ServiceBO serviceBO = new ServiceBO();
    BeanUtils.copyProperties(exist, serviceBO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(service2MessageBody(exist, null, null, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SERVICE, null);

    return serviceBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#deleteServiceByUser(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteServiceByUser(String userId, String operatorId) {
    List<ServiceDO> services = serviceDao.queryServiceByUser(userId);
    services.forEach(service -> deleteService(service.getId(), operatorId, true));

    return services.size();
  }

  /**
   * 用户关注
   */
  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#queryUserFollowService(java.lang.String)
   */
  @Override
  public List<ServiceFollowBO> queryUserFollowService(String userId) {
    List<ServiceFollowDO> userFollowServices = serviceFollowDao.queryUserFollowService(userId);

    List<ServiceFollowBO> result = userFollowServices.stream().map(serviceFollowDO -> {
      ServiceFollowBO serviceFollowBO = new ServiceFollowBO();
      BeanUtils.copyProperties(serviceFollowDO, serviceFollowBO);
      serviceFollowBO.setFollowTime(DateUtils.toStringISO8601(serviceFollowDO.getFollowTime()));

      return serviceFollowBO;
    }).collect(Collectors.toList());

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#changeUserFollowState(com.machloop.fpc.cms.center.appliance.bo.ServiceFollowBO)
   */
  @Override
  public void changeUserFollowState(ServiceFollowBO serviceFollowBO) {
    if (StringUtils.equals(serviceFollowBO.getState(), Constants.BOOL_YES)) {
      ServiceFollowDO serviceFollowDO = new ServiceFollowDO();
      BeanUtils.copyProperties(serviceFollowBO, serviceFollowDO);
      serviceFollowDO.setNetworkId(StringUtils.defaultIfBlank(serviceFollowDO.getNetworkId(), ""));
      serviceFollowDO
          .setNetworkGroupId(StringUtils.defaultIfBlank(serviceFollowDO.getNetworkGroupId(), ""));

      serviceFollowDao.saveServiceFollow(serviceFollowDO);
    } else {
      serviceFollowDao.deleteServiceFollow(serviceFollowBO.getUserId(),
          serviceFollowBO.getServiceId(), serviceFollowBO.getNetworkId(),
          serviceFollowBO.getNetworkGroupId());
    }
  }

  /**
   * 业务路径
   */
  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#queryServiceLink(java.lang.String)
   */
  @Override
  public ServiceLinkBO queryServiceLink(String serviceId) {
    ServiceLinkDO serviceLinkDO = serviceLinkDao.queryServiceLink(serviceId);

    ServiceLinkBO serviceLinkBO = new ServiceLinkBO();
    BeanUtils.copyProperties(serviceLinkDO, serviceLinkBO);
    serviceLinkBO.setTimestamp(DateUtils.toStringISO8601(serviceLinkDO.getTimestamp()));

    return serviceLinkBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.ServiceService#updateServiceLink(com.machloop.fpc.cms.center.appliance.bo.ServiceLinkBO, java.lang.String)
   */
  @Override
  public ServiceLinkBO updateServiceLink(ServiceLinkBO serviceLinkBO, String operatorId) {
    ServiceDO exist = serviceDao.queryService(serviceLinkBO.getServiceId());
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "业务不存在");
    }

    ServiceLinkDO serviceLinkDO = new ServiceLinkDO();
    BeanUtils.copyProperties(serviceLinkBO, serviceLinkDO);
    serviceLinkDO.setOperatorId(operatorId);

    serviceLinkDao.saveOrUpdateServiceLink(serviceLinkDO);
    ServiceLinkBO result = queryServiceLink(serviceLinkBO.getServiceId());

    // 下发到直属fpc和cms
    serviceLinkDO.setId(result.getId());
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(serviceLink2MessageBody(serviceLinkDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SERVICE_LINK, null);

    return result;
  }

  /********************************************************************************************************
   * 下发模块
   *******************************************************************************************************/

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getProducer()
   */
  @Override
  public DefaultMQProducer getProducer() {
    return context.getBean("getRocketMQProducer", DefaultMQProducer.class);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getTags()
   */
  @Override
  public List<String> getTags() {
    return TAGS;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurationIds(java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNumber,
      Date beforeTime) {
    // 网络组字典
    Map<String,
        String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
            .collect(Collectors.toMap(SensorNetworkGroupDO::getId,
                SensorNetworkGroupDO::getNetworkInSensorIds));

    // 获取业务包含的网络
    Map<String, List<String>> serviceNetworkIdMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> serviceIds = serviceDao.queryServiceIds(false);
    if (CollectionUtils.isEmpty(serviceIds)) {
      return Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    }
    serviceNetworkDao.queryServiceNetworks(serviceIds).forEach(serviceNetwork -> {
      List<String> list = serviceNetworkIdMap.getOrDefault(serviceNetwork.getServiceId(),
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));

      if (StringUtils.isNotBlank(serviceNetwork.getNetworkId())) {
        list.add(serviceNetwork.getNetworkId());
      } else {
        list.addAll(
            CsvUtils.convertCSVToList(networkGroupDict.get(serviceNetwork.getNetworkGroupId())));
      }

      serviceNetworkIdMap.put(serviceNetwork.getServiceId(), list);
    });

    // 如果指定了下发的设备，并且业务没有包含将要下发设备的网络，则不下发该业务
    if (!StringUtils.isAllBlank(deviceType, serialNumber)) {
      // 下发设备包含的主网络
      List<String> fpcNetworkIds = fpcNetworkService.queryNetworks(deviceType, serialNumber)
          .stream().map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
      // 下发设备包含的子网
      fpcNetworkIds.addAll(sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
          .filter(item -> fpcNetworkIds.contains(item.getNetworkInSensorIds()))
          .map(SensorLogicalSubnetDO::getId).collect(Collectors.toList()));

      Iterator<Entry<String, List<String>>> iterator = serviceNetworkIdMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, List<String>> entry = iterator.next();
        List<String> bak = Lists.newArrayList(entry.getValue());
        bak.removeAll(fpcNetworkIds);
        entry.getValue().removeAll(bak);

        if (CollectionUtils.isEmpty(entry.getValue())) {
          iterator.remove();
        }
      }
    }

    List<String> serviceList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    // 业务ID集合
    serviceList.addAll(serviceNetworkIdMap.keySet());
    // 业务网络ID集合
    serviceNetworkIdMap.forEach((serviceId, networkIds) -> {
      networkIds.forEach(networkId -> {
        serviceList.add(StringUtils.joinWith("_", serviceId, networkId));
      });
    });
    // 业务度量指标
    serviceNetworkIdMap.forEach((serviceId, networkIds) -> {
      networkIds.forEach(networkId -> {
        serviceList.addAll(metricSettingDao.queryMetricSettingIds(
            FpcCmsConstants.SOURCE_TYPE_SERVICE, networkId, serviceId, beforeTime));
      });
    });

    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_SERVICE, serviceList);
    map.put(FpcCmsConstants.MQ_TAG_SERVICE_LINK, serviceLinkDao.queryServiceLinkIds(beforeTime));

    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurations(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_SERVICE)) {
      // 网络组字典
      Map<String,
          String> networkGroupDict = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
              .collect(Collectors.toMap(SensorNetworkGroupDO::getId,
                  SensorNetworkGroupDO::getNetworkInSensorIds));

      // 各个业务对应的网络ID集合
      Map<String, List<String>> serviceNetworkIdMap = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
        List<String> list = serviceNetworkIdMap.getOrDefault(serviceNetwork.getServiceId(),
            Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));

        if (StringUtils.isNotBlank(serviceNetwork.getNetworkId())) {
          list.add(serviceNetwork.getNetworkId());
        } else {
          list.addAll(
              CsvUtils.convertCSVToList(networkGroupDict.get(serviceNetwork.getNetworkGroupId())));
        }

        serviceNetworkIdMap.put(serviceNetwork.getServiceId(), list);
      });

      // 如果指定了下发的设备，并且业务没有包含将要下发设备的网络，则不下发该业务
      if (!StringUtils.isAllBlank(deviceType, serialNumber)) {
        // 下发设备包含的主网络
        List<String> fpcNetworkIds = fpcNetworkService.queryNetworks(deviceType, serialNumber)
            .stream().map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
        // 下发设备包含的子网
        fpcNetworkIds.addAll(sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
            .filter(item -> fpcNetworkIds.contains(item.getNetworkInSensorIds()))
            .map(SensorLogicalSubnetDO::getId).collect(Collectors.toList()));

        Iterator<Entry<String, List<String>>> iterator = serviceNetworkIdMap.entrySet().iterator();
        while (iterator.hasNext()) {
          Entry<String, List<String>> entry = iterator.next();
          List<String> bak = Lists.newArrayList(entry.getValue());
          bak.removeAll(fpcNetworkIds);
          entry.getValue().removeAll(bak);

          if (CollectionUtils.isEmpty(entry.getValue())) {
            iterator.remove();
          }
        }
      }

      if (MapUtils.isEmpty(serviceNetworkIdMap)) {
        return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
      }

      // 根据筛选后的有效网络业务组合，查询将要下发的业务
      List<ServiceDO> serviceList = serviceDao
          .queryServiceByIds(Lists.newArrayList(serviceNetworkIdMap.keySet()));

      // 当前业务列表
      List<Map<String, Object>> list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      for (ServiceDO service : serviceList) {
        // 当前业务下发设备上有效的网络ID
        List<String> networkIds = serviceNetworkIdMap.get(service.getId());

        // 当前业务下发设备上有效的网络
        List<ServiceNetworkDO> serviceNetworkList = networkIds.stream().map(networkId -> {
          ServiceNetworkDO serviceNetworkDO = new ServiceNetworkDO();
          serviceNetworkDO.setNetworkId(networkId);

          return serviceNetworkDO;
        }).collect(Collectors.toList());

        // 当前业务下发设备上有效的度量指标
        List<MetricSettingDO> metricSettings = metricSettingDao
            .queryMetricSettings(FpcCmsConstants.SOURCE_TYPE_SERVICE, null, service.getId(), null)
            .stream().filter(setting -> networkIds.contains(setting.getNetworkId()))
            .collect(Collectors.toList());

        list.add(service2MessageBody(service, serviceNetworkList, metricSettings,
            FpcCmsConstants.SYNC_ACTION_ADD));
      }

      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_SERVICE_LINK)) {
      List<Map<String, Object>> serviceLinks = serviceLinkDao.queryServiceLinks().stream()
          .map(serviceLink -> serviceLink2MessageBody(serviceLink, FpcCmsConstants.SYNC_ACTION_ADD))
          .collect(Collectors.toList());
      return Tuples.of(true, serviceLinks, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> service2MessageBody(ServiceDO serviceDO,
      List<ServiceNetworkDO> serviceNetworkDOList, List<MetricSettingDO> metricSettings,
      String action) {
    // 获取当前业务所包含的网络ID集合
    List<String> serviceNetworkIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (serviceNetworkDOList != null) {
      for (ServiceNetworkDO serviceNetworkDO : serviceNetworkDOList) {
        // serviceNetworkList包含子网id与网络id
        if (StringUtils.isNotBlank(serviceNetworkDO.getNetworkId())) {
          serviceNetworkIds.add(serviceNetworkDO.getNetworkId());
        }
        // 根据网络组id得到其中的网络ids
        if (StringUtils.isNotBlank(serviceNetworkDO.getNetworkGroupId())) {
          SensorNetworkGroupDO sensorNetworkGroup = sensorNetworkGroupDao
              .querySensorNetworkGroup(serviceNetworkDO.getNetworkGroupId());
          serviceNetworkIds
              .addAll(CsvUtils.convertCSVToList(sensorNetworkGroup.getNetworkInSensorIds()));
        }
      }
    }

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", serviceDO.getId());
    map.put("name", serviceDO.getName());
    map.put("networkIds", CsvUtils.convertCollectionToCSV(serviceNetworkIds));
    map.put("application", serviceDO.getApplication());
    map.put("metricSettings", metricSetting2MessageBody(metricSettings));
    map.put("action", action);

    return map;
  }

  private Map<String, Object> serviceLink2MessageBody(ServiceLinkDO serviceLinkDO, String action) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", serviceLinkDO.getId());
    map.put("link", serviceLinkDO.getLink());
    map.put("serviceId", serviceLinkDO.getServiceId());
    map.put("metric", serviceLinkDO.getMetric());
    map.put("action", action);

    return map;
  }

  private List<Map<String, Object>> metricSetting2MessageBody(
      List<MetricSettingDO> metricSettings) {

    List<Map<String, Object>> list = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    if (CollectionUtils.isEmpty(metricSettings)) {
      return list;
    }
    for (MetricSettingDO metricSetting : metricSettings) {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

      map.put("id", metricSetting.getId());
      map.put("sourceType", metricSetting.getSourceType());
      map.put("networkId", metricSetting.getNetworkId());
      map.put("serviceId", metricSetting.getServiceId());
      map.put("packetFileId", metricSetting.getPacketFileId());
      map.put("metric", metricSetting.getMetric());
      map.put("value", metricSetting.getValue());
      list.add(map);
    }
    return list;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_SERVICE, FpcCmsConstants.MQ_TAG_SERVICE_LINK));
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#syncConfiguration(org.apache.rocketmq.common.message.Message)
   */
  @Override
  public int syncConfiguration(Message message) {
    Map<String, Object> messageBody = MQMessageHelper.convertToMap(message);

    List<Map<String, Object>> messages = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (MapUtils.getBoolean(messageBody, "batch", false)) {
      messages.addAll(JsonHelper.deserialize(JsonHelper.serialize(messageBody.get("data")),
          new TypeReference<List<Map<String, Object>>>() {
          }));
    } else {
      messages.add(messageBody);
    }
    int syncTotalCount = 0;
    if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_SERVICE)) {
      syncTotalCount = messages.stream().mapToInt(item -> syncService(item)).sum();
      LOGGER.info("current sync service total: {}.", syncTotalCount);
    }
    if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_SERVICE_LINK)) {
      syncTotalCount = messages.stream().mapToInt(item -> syncServiceLink(item)).sum();
      LOGGER.info("current sync serviceLink total: {}.", syncTotalCount);
    }

    return syncTotalCount;
  }

  private int syncServiceLink(Map<String, Object> messageBody) {
    int syncCount = 0;

    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }

    ServiceLinkBO serviceLinkBO = new ServiceLinkBO();
    serviceLinkBO.setId(assignId);
    serviceLinkBO.setAssignId(assignId);
    serviceLinkBO.setLink(MapUtils.getString(messageBody, "link"));
    serviceLinkBO.setMetric(MapUtils.getString(messageBody, "metric"));
    serviceLinkBO.setServiceId(MapUtils.getString(messageBody, "serviceId"));

    // 判断下发的业务路径所属的业务是否存在
    List<String> validServiceIds = queryServices().stream().map(e -> e.getId())
        .collect(Collectors.toList());
    if (!validServiceIds.contains(serviceLinkBO.getId())) {
      // 不存在业务路径所属的业务
      return syncCount;
    }

    try {
      updateServiceLink(serviceLinkBO, CMS_ASSIGNMENT);
      syncCount++;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync serviceLink status: [syncCount: {}]", syncCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return 0;
    }
    return syncCount;
  }

  private int syncService(Map<String, Object> messageBody) {
    int syncCount = 0;

    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    ServiceBO serviceBO = new ServiceBO();
    serviceBO.setId(assignId);
    serviceBO.setAssignId(assignId);
    serviceBO.setName(MapUtils.getString(messageBody, "name"));
    serviceBO.setApplication(MapUtils.getString(messageBody, "application"));
    serviceBO.setDescription(CMS_ASSIGNMENT);

    // 获取网络ID，非空判断
    String networkIds = MapUtils.getString(messageBody, "networkIds");

    // 判断下发的业务所包含网络是否存在
    List<String> vaildNetworkIds = fpcNetworkService.queryAllNetworks().stream()
        .map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
    Map<String,
        String> validSubnetIdMap = sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
            .collect(
                Collectors.toMap(SensorLogicalSubnetDO::getAssignId, SensorLogicalSubnetDO::getId));

    List<String> serviceNetworkIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    // 将networkId^subnetId的情况进行拆解，取subnetId与物理网络合并
    List<String> networkSubnetIdList = CsvUtils.convertCSVToList(networkIds);
    for (String networkSubnetId : networkSubnetIdList) {
      if (StringUtils.contains(networkSubnetId, "^")) {
        String[] networkIdSubnetId = StringUtils.split(networkSubnetId, "^");
        serviceNetworkIds.add(networkIdSubnetId[1]);
      } else {
        serviceNetworkIds.add(networkSubnetId);
      }
    }

    ServiceDO exist = serviceDao.queryServiceByAssignId(serviceBO.getAssignId());

    List<String> serviceNetworkList = serviceNetworkIds.stream().distinct().filter(
        networkId -> validSubnetIdMap.containsKey(networkId) || vaildNetworkIds.contains(networkId))
        .map(networkId -> validSubnetIdMap.getOrDefault(networkId, networkId))
        .collect(Collectors.toList());
    outer: if (!StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_DELETE)
        && CollectionUtils.isEmpty(serviceNetworkList)) {
      if (StringUtils.isNotBlank(exist.getId())) {
        // 此类情况为上级编辑了网络组后导致下发的业务中网络组中网络为空的情况
        action = FpcCmsConstants.SYNC_ACTION_DELETE;
        break outer;
      }
      // 不存在业务所包含的网络
      return syncCount;
    }

    serviceBO.setNetworkIds(CsvUtils.convertCollectionToCSV(serviceNetworkList));

    List<Map<String, Object>> metricSettings = JsonHelper.deserialize(
        JsonHelper.serialize(messageBody.get("metricSettings")),
        new TypeReference<List<Map<String, Object>>>() {
        });

    List<MetricSettingBO> metricSettingList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    metricSettings.forEach(metricSettingMap -> {
      if (serviceNetworkList.contains(MapUtils.getString(metricSettingMap, "networkId"))) {
        MetricSettingBO metricSettingBO = new MetricSettingBO();
        metricSettingBO.setAssignId(MapUtils.getString(metricSettingMap, "id"));
        metricSettingBO.setSourceType(MapUtils.getString(metricSettingMap, "sourceType"));
        metricSettingBO.setNetworkId(MapUtils.getString(metricSettingMap, "networkId"));
        // serviceId在调用saveService时添加
        metricSettingBO.setMetric(MapUtils.getString(metricSettingMap, "metric"));
        metricSettingBO.setValue(MapUtils.getString(metricSettingMap, "value"));
        metricSettingList.add(metricSettingBO);
      }
    });

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateService(exist.getId(), serviceBO, CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveService(serviceBO, metricSettingList, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteService(exist.getId(), CMS_ASSIGNMENT, true);
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync service status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }

    return syncCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // 导出所有业务
    try {
      StringBuilder content = new StringBuilder();
      exportServices().forEach(item -> content.append(item));
      File tempFile = Paths
          .get(HotPropertiesHelper.getProperty("file.runtime.path"), "services.csv").toFile();
      FileUtils.writeByteArrayToFile(tempFile, content.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      LOGGER.warn("backup service msg failed.", e);
    }

    // 删除
    int clearCount = 0;
    List<String> serviceIds = serviceDao.queryServiceIds(onlyLocal);
    for (String serviceId : serviceIds) {
      try {
        deleteService(serviceId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete serivce failed. error msg: {}", e.getMessage());
        continue;
      }
    }

    return clearCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_SERVICE_LINK)) {
      return serviceLinkDao.queryAssignServiceLinkIds(beforeTime).stream().map(e -> e.getAssignId())
          .collect(Collectors.toList());
    }
    List<String> assignIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    // 获取上级cms下发的业务
    List<ServiceDO> services = serviceDao.queryAssignServiceIds(beforeTime);
    Map<String, String> serviceIdMap = services.stream()
        .collect(Collectors.toMap(ServiceDO::getId, ServiceDO::getAssignId));

    // 获取子网ID
    Map<String,
        String> subnetIdMap = sensorLogicalSubnetDao.queryAssignLogicalSubnets(beforeTime).stream()
            .collect(
                Collectors.toMap(SensorLogicalSubnetDO::getId, SensorLogicalSubnetDO::getAssignId));

    if (CollectionUtils.isEmpty(serviceIdMap.keySet())) {
      return assignIds;
    }
    // 业务包含的网络ID集合
    List<String> serviceNetworkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(services)) {
      List<ServiceNetworkDO> serviceNetworks = serviceNetworkDao
          .queryServiceNetworks(Lists.newArrayList(serviceIdMap.keySet()));
      serviceNetworkIds = serviceNetworks.stream()
          .filter(serviceNetwork -> serviceIdMap.containsKey(serviceNetwork.getServiceId()))
          .map(serviceNetwork -> StringUtils.joinWith("_",
              serviceIdMap.get(serviceNetwork.getServiceId()), subnetIdMap
                  .getOrDefault(serviceNetwork.getNetworkId(), serviceNetwork.getNetworkId())))
          .collect(Collectors.toList());
    }

    // 度量指标ID集合
    List<String> settingAssignIds = metricSettingDao
        .queryAssignMetricSettingIds(FpcCmsConstants.SOURCE_TYPE_SERVICE, beforeTime);

    assignIds.addAll(serviceIdMap.values());
    assignIds.addAll(serviceNetworkIds);
    assignIds.addAll(settingAssignIds);
    return assignIds;
  }

}
