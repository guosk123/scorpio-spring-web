package com.machloop.fpc.npm.appliance.service.impl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.machloop.alpha.common.util.*;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.service.AlertRuleService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.npm.appliance.bo.MetricSettingBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.bo.ServiceFollowBO;
import com.machloop.fpc.npm.appliance.bo.ServiceLinkBO;
import com.machloop.fpc.npm.appliance.dao.*;
import com.machloop.fpc.npm.appliance.data.*;
import com.machloop.fpc.npm.appliance.service.BaselineService;
import com.machloop.fpc.npm.appliance.service.MetricSettingService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author guosk
 *
 * create at 2020年11月12日, fpc-manager
 */
@Order(2)
@Transactional
@Service
public class ServiceServiceImpl implements ServiceService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceServiceImpl.class);

  private static final Pattern SPLIT_PATTERN = Pattern.compile("`((?:[^`])+)`|``",
      Pattern.MULTILINE);

  private static final String CSV_TITLE = "`名称`,`网络`,`应用配置`,`业务路径`,`描述`\n";

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
  private NetworkDao networkDao;

  @Autowired
  private SaService saService;

  @Autowired
  private LogicalSubnetDao logicalSubnetDao;

  @Autowired
  private MetricSettingService metricSettingService;

  @Autowired
  private MetricSettingDao metricSettingDao;

  @Autowired
  private BaselineService baselineService;

  @Autowired
  private AlertRuleService alertRuleService;

  @Autowired
  private GlobalSettingService globalSettingService;

  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#queryServices(com.machloop.alpha.common.base.page.Pageable, java.lang.String)
   */
  @Override
  public Page<ServiceBO> queryServices(Pageable page, String name) {
    Map<String, String> networkNameMap = networkDao.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkDO::getId, NetworkDO::getName));
    Map<String, String> subnetNameMap = logicalSubnetDao.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetDO::getId, LogicalSubnetDO::getName));

    Map<String, List<String>> serviceNetworkIds = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkNames = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
      // 网络ID
      List<String> list = serviceNetworkIds.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(list)) {
        list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      list.add(serviceNetwork.getNetworkId());
      serviceNetworkIds.put(serviceNetwork.getServiceId(), list);
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

      networkNames.add(networkName);
      serviceNetworkNames.put(serviceNetwork.getServiceId(), networkNames);
    });

    Page<ServiceDO> services = serviceDao.queryServices(page, name);

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

      return serviceBO;
    }).collect(Collectors.toList());

    return new PageImpl<>(serviceBOList, page, services.getTotalElements());
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#queryServices()
   */
  @Override
  public List<ServiceBO> queryServices() {
    Map<String, String> networkNameMap = networkDao.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkDO::getId, NetworkDO::getName));
    Map<String, String> subnetNameMap = logicalSubnetDao.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetDO::getId, LogicalSubnetDO::getName));

    Map<String, List<String>> serviceNetworkIds = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> serviceNetworkNames = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
      // 网络ID
      List<String> list = serviceNetworkIds.get(serviceNetwork.getServiceId());
      if (CollectionUtils.isEmpty(list)) {
        list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      }
      list.add(serviceNetwork.getNetworkId());
      serviceNetworkIds.put(serviceNetwork.getServiceId(), list);
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

      networkNames.add(networkName);
      serviceNetworkNames.put(serviceNetwork.getServiceId(), networkNames);
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
      result.add(serviceBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService# queryServicesWithRules()
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
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#queryServiceByAppId(java.lang.String)
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
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#queryService(java.lang.String)
   */
  @Override
  public ServiceBO queryService(String id) {
    ServiceDO serviceDO = serviceDao.queryService(id);

    ServiceBO serviceBO = new ServiceBO();
    BeanUtils.copyProperties(serviceDO, serviceBO);
    serviceBO.setCreateTime(DateUtils.toStringISO8601(serviceDO.getCreateTime()));
    List<String> networkIds = serviceNetworkDao.queryServiceNetworks(id, null).stream()
        .map(serviceNetwork -> serviceNetwork.getNetworkId()).collect(Collectors.toList());
    serviceBO.setNetworkIds(StringUtils.join(networkIds, ","));

    return serviceBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#exportServices()
   */
  @Override
  public List<String> exportServices() {
    Map<String, String> networkDict = networkDao.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkDO::getId, NetworkDO::getName));
    Map<String, String> subnetDict = logicalSubnetDao.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetDO::getId, LogicalSubnetDO::getName));
    Map<Integer, String> appDict = saService.queryAllAppsIdNameMapping();

    // 业务关联网络
    Map<String,
        List<String>> serviceNetworks = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    serviceNetworkDao.queryServiceNetworks().forEach(serviceNetwork -> {
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
    List<String> result = Lists.newArrayListWithCapacity(services.size() + 1);
    result.add(CSV_TITLE);
    services.forEach(service -> {
      List<String> appNames = CsvUtils.convertCSVToList(service.getApplication()).stream()
          .map(appId -> appDict.get(Integer.parseInt(appId))).collect(Collectors.toList());
      String oneItem = CsvUtils.spliceRowData(service.getName(),
          StringUtils.join(serviceNetworks.get(service.getId()), "|"),
          StringUtils.join(appNames, "|"), serviceLinks.getOrDefault(service.getId(), ""),
          service.getDescription());
      result.add(oneItem);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#importServices(org.springframework.web.multipart.MultipartFile, java.lang.String)
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
    Map<String, String> networkDict = networkDao.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkDO::getName, NetworkDO::getId));

    // 子网络<name:id>
    Map<String, String> subnetDict = logicalSubnetDao.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetDO::getName, LogicalSubnetDO::getId));

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
        String applications = StringUtils.trim(contents.get(2));
        String serviceLink = StringUtils.trim(contents.get(3));
        String description = StringUtils.trim(contents.get(4));
        if (StringUtils.isAnyBlank(name, networks, applications)) {
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

    // 保存业务配置的网络关联关系
    serviceNetworkDao.batchSaveServiceNetwork(serviceNetworkList);

    // 配置业务默认的统计度量值
    List<MetricSettingBO> metricSettings = serviceNetworkList.stream().map(serviceNetwork -> {
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setSourceType(FpcConstants.SOURCE_TYPE_SERVICE);
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
    return importCount;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#saveService(com.machloop.fpc.npm.appliance.bo.ServiceBO, java.util.List, java.lang.String)
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

    if (CollectionUtils.isNotEmpty(serviceNetworks)) {
      serviceNetworkDao.mergeServiceNetworks(serviceNetworks);
    }

    if (CollectionUtils.isEmpty(metricSettings)) {
      // 配置业务默认的统计度量值
      metricSettings = serviceNetworks.stream().map(serviceNetwork -> {
        MetricSettingBO metricSettingBO = new MetricSettingBO();
        metricSettingBO.setSourceType(FpcConstants.SOURCE_TYPE_SERVICE);
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

    return queryService(serviceDO.getId());
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#updateService(java.lang.String, com.machloop.fpc.npm.appliance.bo.ServiceBO, java.lang.String)
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
    List<String> oldNetworkIds = serviceNetworkDao.queryServiceNetworks(id, null).stream()
        .map(ServiceNetworkDO::getNetworkId).collect(Collectors.toList());

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

    if (CollectionUtils.isNotEmpty(serviceNetworks)) {
      serviceNetworkDao.mergeServiceNetworks(serviceNetworks);
    }

    // 业务移除的网络
    List<String> removeNetworkIds = Lists.newArrayList(oldNetworkIds);
    removeNetworkIds.removeAll(newNetworkIds);
    // 业务新增的网络
    newNetworkIds.removeAll(oldNetworkIds);

    // 业务配置网络变更时，变更统计度量值
    List<MetricSettingBO> newMetricSettings = newNetworkIds.stream().map(networkId -> {
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setSourceType(FpcConstants.SOURCE_TYPE_SERVICE);
      metricSettingBO.setNetworkId(networkId);
      metricSettingBO.setServiceId(id);

      return metricSettingBO;
    }).collect(Collectors.toList());
    // 新增
    if (CollectionUtils.isNotEmpty(newMetricSettings)) {
      metricSettingService.saveDefaultMetricSettings(newMetricSettings, operatorId);
    }
    // 删除
    removeNetworkIds.forEach(netwrokId -> {
      metricSettingService.deleteMetricSetting(FpcConstants.SOURCE_TYPE_SERVICE, netwrokId, id,
          null);
    });

    // 业务配置网络变更时，删除已经移除网络的关注
    removeNetworkIds.forEach(networkId -> {
      serviceFollowDao.deleteServiceFollow(operatorId, id, networkId);
    });

    return serviceBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService# deleteService(java.lang.String, java.lang.String)
   */
  @Transactional
  @Override
  public ServiceBO deleteService(String id, String operatorId, boolean forceDelete) {
    ServiceDO exist = serviceDao.queryService(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "业务不存在");
    }

    // 是否有告警作用于该业务
    if (!forceDelete && alertRuleService
        .queryAlertRulesBySource(FpcConstants.SOURCE_TYPE_SERVICE, null, id).size() > 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该业务已作用于告警，无法删除");
    }

    // 删除业务
    serviceDao.deleteService(id, operatorId);

    // 删除业务配置网络
    serviceNetworkDao.deleteServiceNetwork(id);

    // 删除用户关注
    serviceFollowDao.deleteServiceFollow(id);

    // 删除业务下基线定义
    baselineService.deleteBaselineSettings(FpcConstants.SOURCE_TYPE_SERVICE, null, id);

    // 删除业务下的统计配置
    metricSettingService.deleteMetricSetting(FpcConstants.SOURCE_TYPE_SERVICE, null, id, null);

    // 删除业务路径
    serviceLinkDao.deleteServiceLink(id);

    ServiceBO serviceBO = new ServiceBO();
    BeanUtils.copyProperties(exist, serviceBO);
    return serviceBO;
  }

  /**
   * 用户关注
   */
  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#queryUserFollowService(java.lang.String)
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
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#changeUserFollowState(com.machloop.fpc.npm.appliance.bo.ServiceFollowBO)
   */
  @Override
  public void changeUserFollowState(ServiceFollowBO serviceFollowBO) {
    if (StringUtils.equals(serviceFollowBO.getState(), Constants.BOOL_YES)) {
      ServiceFollowDO serviceFollowDO = new ServiceFollowDO();
      BeanUtils.copyProperties(serviceFollowBO, serviceFollowDO);

      serviceFollowDao.saveServiceFollow(serviceFollowDO);
    } else {
      serviceFollowDao.deleteServiceFollow(serviceFollowBO.getUserId(),
          serviceFollowBO.getServiceId(), serviceFollowBO.getNetworkId());
    }
  }

  /**
   * 业务路径
   */
  /**
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#queryServiceLink(java.lang.String)
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
   * @see com.machloop.fpc.npm.appliance.service.ServiceService#updateServiceLink(com.machloop.fpc.npm.appliance.bo.ServiceLinkBO, java.lang.String)
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

    return queryServiceLink(serviceLinkBO.getServiceId());
  }

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_SERVICE, FpcCmsConstants.MQ_TAG_SERVICE_LINK));
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService# syncConfiguration(java.util.Map)
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

    String serviceLinkInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(serviceLinkInCmsId)) {
      return syncCount;
    }

    ServiceLinkBO serviceLinkBO = new ServiceLinkBO();
    serviceLinkBO.setId(serviceLinkInCmsId);
    serviceLinkBO.setServiceLinkInCmsId(serviceLinkInCmsId);
    serviceLinkBO.setLink(MapUtils.getString(messageBody, "link"));
    serviceLinkBO.setMetric(MapUtils.getString(messageBody, "metric"));
    serviceLinkBO.setServiceId(MapUtils.getString(messageBody, "serviceId"));

    // 判断下发的业务路径所属的业务是否存在
    List<String> validServiceIds = queryServices().stream().map(e -> e.getId())
        .collect(Collectors.toList());
    if (!validServiceIds.contains(serviceLinkBO.getServiceId())) {
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

    String serviceInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(serviceInCmsId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    ServiceBO serviceBO = new ServiceBO();
    serviceBO.setId(serviceInCmsId);
    serviceBO.setServiceInCmsId(serviceInCmsId);
    serviceBO.setName(MapUtils.getString(messageBody, "name"));
    serviceBO.setApplication(MapUtils.getString(messageBody, "application"));
    serviceBO.setDescription(CMS_ASSIGNMENT);

    // 判断下发的业务所包含网络是否存在
    List<String> vaildNetworkIds = networkDao.queryNetworks().stream().map(NetworkDO::getId)
        .collect(Collectors.toList());
    Map<String, String> validSubnetIdMap = logicalSubnetDao.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetDO::getSubnetInCmsId, LogicalSubnetDO::getId));

    List<String> serviceNetworkIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    // 将networkId^subnetId的情况进行拆解，取subnetId与物理网络合并
    List<String> networkSubnetIdList = CsvUtils
        .convertCSVToList(MapUtils.getString(messageBody, "networkIds"));
    for (String networkSubnetId : networkSubnetIdList) {
      if (StringUtils.contains(networkSubnetId, "^")) {
        String[] networkIdSubnetId = StringUtils.split(networkSubnetId, "^");
        serviceNetworkIds.add(networkIdSubnetId[1]);
      } else {
        serviceNetworkIds.add(networkSubnetId);
      }
    }

    ServiceDO exist = serviceDao.queryServiceByCmsServiceId(serviceBO.getServiceInCmsId());

    List<String> serviceNetworkList = serviceNetworkIds.stream().distinct().filter(
        networkId -> validSubnetIdMap.containsKey(networkId) || vaildNetworkIds.contains(networkId))
        .map(networkId -> validSubnetIdMap.getOrDefault(networkId, networkId))
        .collect(Collectors.toList());

    outer: if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_MODIFY)) {
      if (CollectionUtils.isEmpty(serviceNetworkList) && StringUtils.isNotBlank(exist.getId())) {
        // 此类情况为上级存在某个网络组（包含a网络）所在的业务，此时编辑了该网络组(删除a网络)，下发后探针上仍然存在包含a网络的业务，所以要删除该业务
        action = FpcCmsConstants.SYNC_ACTION_DELETE;
        break outer;
      }
      if (StringUtils.isBlank(exist.getId()) && CollectionUtils.isNotEmpty(serviceNetworkList)) {
        // 此类情况为上级存在某个网络组（不包含b网络）所在的业务，此时编辑了该网络组(新增b网络)，下发后探针上不存在包含b网络的业务，所以要新增该业务
        action = FpcCmsConstants.SYNC_ACTION_ADD;
        break outer;
      }
    }

    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && CollectionUtils.isEmpty(serviceNetworkList)) {
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
        metricSettingBO.setMetricSettingInCmsId(MapUtils.getString(metricSettingMap, "id"));
        metricSettingBO.setId(MapUtils.getString(metricSettingMap, "id"));
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
        case FpcConstants.SYNC_ACTION_ADD:
        case FpcConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateService(exist.getId(), serviceBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                serviceBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveService(serviceBO, metricSettingList, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                serviceBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcConstants.SYNC_ACTION_DELETE:
          deleteService(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              serviceBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
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
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date, boolean)
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
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService# getAssignConfigurationIds(java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tags, Date beforeTime) {

    if (StringUtils.equals(tags, FpcCmsConstants.MQ_TAG_SERVICE_LINK)) {
      return serviceLinkDao.queryAssignServiceLinks(beforeTime);
    }
    if (StringUtils.equals(tags, FpcCmsConstants.MQ_TAG_SERVICE)) {
      // 获取上级cms下发的业务
      List<ServiceDO> services = serviceDao.queryAssignServiceIds(beforeTime);
      Map<String, String> serviceIdMap = services.stream()
          .collect(Collectors.toMap(ServiceDO::getId, ServiceDO::getServiceInCmsId));

      // 获取子网ID
      Map<String,
          String> subnetIdMap = logicalSubnetDao.queryAssignLogicalSubnets(beforeTime).stream()
              .collect(Collectors.toMap(LogicalSubnetDO::getId, LogicalSubnetDO::getSubnetInCmsId));

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
          .queryAssignMetricSettingIds(FpcConstants.SOURCE_TYPE_SERVICE, null, null, beforeTime);

      List<String> assignIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      assignIds.addAll(serviceIdMap.values());
      assignIds.addAll(serviceNetworkIds);
      assignIds.addAll(settingAssignIds);
      return assignIds;
    }

    return Lists.newArrayListWithCapacity(0);
  }
}
