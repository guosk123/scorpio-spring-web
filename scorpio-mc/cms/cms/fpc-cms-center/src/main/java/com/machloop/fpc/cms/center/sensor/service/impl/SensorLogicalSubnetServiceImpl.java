package com.machloop.fpc.cms.center.sensor.service.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.appliance.bo.MetricSettingBO;
import com.machloop.fpc.cms.center.appliance.dao.MetricSettingDao;
import com.machloop.fpc.cms.center.appliance.dao.NetworkTopologyDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceNetworkDao;
import com.machloop.fpc.cms.center.appliance.data.MetricSettingDO;
import com.machloop.fpc.cms.center.appliance.data.NetworkTopologyDO;
import com.machloop.fpc.cms.center.appliance.service.AlertRuleService;
import com.machloop.fpc.cms.center.appliance.service.BaselineService;
import com.machloop.fpc.cms.center.appliance.service.MetricSettingService;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.central.service.ClusterService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月25日, fpc-cms-center
 */
@Order(3)
@Transactional
@Service
public class SensorLogicalSubnetServiceImpl
    implements SensorLogicalSubnetService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_LOGICALSUBNET);

  @Autowired
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private SensorLogicalSubnetDao sensorLogicalSubnetDao;

  @Autowired
  private ServiceNetworkDao serviceNetworkDao;

  @Autowired
  private MetricSettingDao metricSettingDao;

  @Autowired
  private NetworkTopologyDao networkTopologyDao;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private MetricSettingService metricSettingService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private AlertRuleService alertRuleService;

  @Autowired
  private BaselineService baselineService;

  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @Autowired
  private ClusterService clusterService;

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService#queryLogicalSubnets()
   */
  @Override
  public List<SensorLogicalSubnetBO> querySensorLogicalSubnets() {
    List<SensorLogicalSubnetDO> sensorLogicalSubnetList = sensorLogicalSubnetDao
        .querySensorLogicalSubnets();
    List<SensorLogicalSubnetBO> result = Lists
        .newArrayListWithCapacity(sensorLogicalSubnetList.size());
    if (sensorLogicalSubnetList.size() == 0) {
      return result;
    }

    Map<String,
        Tuple2<String, String>> networkMap = fpcNetworkService.queryAllNetworks().stream()
            .collect(Collectors.toMap(FpcNetworkBO::getFpcNetworkId,
                network -> Tuples.of(network.getFpcNetworkName(), network.getFpcSerialNumber())));

    // 获取连接异常的探针节点
    Map<String, String> abnormalNodes = clusterService.queryAbnormalNodesAndRefresh();

    sensorLogicalSubnetList.forEach(sensorLogicalSubnetDO -> {
      SensorLogicalSubnetBO sensorLogicalSubnetBO = new SensorLogicalSubnetBO();
      BeanUtils.copyProperties(sensorLogicalSubnetDO, sensorLogicalSubnetBO);
      List<String> networkNames = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      List<String> fpcSerialNumbers = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      CsvUtils.convertCSVToList(sensorLogicalSubnetDO.getNetworkInSensorIds())
          .forEach(networkId -> {
            if (networkMap.containsKey(networkId)) {
              Tuple2<String, String> nameaSeri = networkMap.get(networkId);
              networkNames.add(nameaSeri.getT1());
              fpcSerialNumbers.add(nameaSeri.getT2());
            }
          });

      sensorLogicalSubnetBO.setNetworkInSensorNames(CsvUtils.convertCollectionToCSV(networkNames));

      String status = FpcCmsConstants.CONNECT_STATUS_NORMAL;
      String detail = "";
      Set<String> abnormalIps = fpcSerialNumbers.stream()
          .filter(fpcSerialNumber -> abnormalNodes.containsKey(fpcSerialNumber))
          .map(fpcSerialNumber -> abnormalNodes.get(fpcSerialNumber)).collect(Collectors.toSet());
      if (CollectionUtils.isNotEmpty(abnormalIps)) {
        status = FpcCmsConstants.CONNECT_STATUS_ABNORMAL;
        detail = String.format("探针设备[%s]异常或未连接，请检查设备数据节点状态",
            CsvUtils.convertCollectionToCSV(abnormalIps));
      }
      sensorLogicalSubnetBO.setStatus(status);
      sensorLogicalSubnetBO.setStatusDetail(detail);

      result.add(sensorLogicalSubnetBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService#queryLogicalSubnet(java.lang.String)
   */
  @Override
  public SensorLogicalSubnetBO querySensorLogicalSubnet(String id) {
    SensorLogicalSubnetDO sensorLogicalSubnetDO = sensorLogicalSubnetDao
        .querySensorLogicalSubnet(id);

    SensorLogicalSubnetBO sensorLogicalSubnetBO = new SensorLogicalSubnetBO();
    BeanUtils.copyProperties(sensorLogicalSubnetDO, sensorLogicalSubnetBO);
    Map<String, String> networkMap = fpcNetworkService.queryAllNetworks().stream()
        .collect(Collectors.toMap(FpcNetworkBO::getFpcNetworkId, FpcNetworkBO::getFpcNetworkName));
    List<String> networkNames = CsvUtils
        .convertCSVToList(sensorLogicalSubnetDO.getNetworkInSensorIds()).stream()
        .filter(networkId -> networkMap.containsKey(networkId))
        .map(networkId -> networkMap.get(networkId)).collect(Collectors.toList());
    sensorLogicalSubnetBO.setNetworkInSensorNames(CsvUtils.convertCollectionToCSV(networkNames));

    return sensorLogicalSubnetBO;
  }


  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService#updateLogicalSubnet(java.lang.String, com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO, java.lang.String)
   */
  @Transactional
  @Override
  public SensorLogicalSubnetBO updateSensorLogicalSubnet(String id,
      SensorLogicalSubnetBO sensorLogicalSubnetBO, String operatorId) {
    SensorLogicalSubnetDO sensorLogicalSubnetDO = sensorLogicalSubnetDao
        .querySensorLogicalSubnet(id);
    if (StringUtils.isBlank(sensorLogicalSubnetDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "子网不存在");
    }

    // 校验：一个子网不能属于同一个探针上的多个网络
    Map<String,
        String> existNetworkSensorIdMap = sensorNetworkDao.querySensorNetworks().stream().collect(
            Collectors.toMap(SensorNetworkDO::getNetworkInSensorId, SensorNetworkDO::getSensorId));
    // 校验新建的子网所属的多个网络是否属于同一个探针
    List<String> newNetworkList = CsvUtils
        .convertCSVToList(sensorLogicalSubnetBO.getNetworkInSensorIds());
    List<String> rawSensorId = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String networkId : newNetworkList) {
      rawSensorId.add(existNetworkSensorIdMap.get(networkId));
    }
    List<String> sensorId = rawSensorId.stream().distinct().collect(Collectors.toList());
    if (newNetworkList.size() > sensorId.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "子网所属的多个网络不能属于同一探针");
    }

    BeanUtils.copyProperties(sensorLogicalSubnetBO, sensorLogicalSubnetDO);
    sensorLogicalSubnetDao.updateSensorLogicalSubnet(id, sensorLogicalSubnetDO, operatorId);

    // 下发到直属fpc和cms
    sensorLogicalSubnetDO.setId(id);
    List<MetricSettingDO> metricSettings = metricSettingDao.queryMetricSettings(
        FpcCmsConstants.SOURCE_TYPE_NETWORK, sensorLogicalSubnetDO.getId(), null, null);
    List<Map<String, Object>> messageBodys = Lists.newArrayList(sensorLogicalSubnet2MessageBody(
        sensorLogicalSubnetDO, metricSettings, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_LOGICALSUBNET, null);

    return querySensorLogicalSubnet(id);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService#saveLogicalSubnet(com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO, java.lang.String)
   */
  @Transactional
  @Override
  public SensorLogicalSubnetBO saveSensorLogicalSubnet(SensorLogicalSubnetBO sensorLogicalSubnetBO,
      List<MetricSettingBO> metricSettings, String operatorId) {
    SensorLogicalSubnetDO exist = sensorLogicalSubnetDao
        .querySensorLogicalSubnetByName(sensorLogicalSubnetBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "逻辑子网名称不能重复");
    }

    // 校验：一个子网只能属于同一个探针上的某一个网络，不能属于同一个探针上的多个网络
    // 校验新建的与已存在的子网是否属于同一个探针
    List<
        String> existNetworkInSubnetList = CsvUtils.convertCSVToList(exist.getNetworkInSensorIds());
    List<String> newNetworkInSubnetList = CsvUtils
        .convertCSVToList(sensorLogicalSubnetBO.getNetworkInSensorIds());
    Map<String,
        String> existNetworkSensorIdMap = sensorNetworkDao.querySensorNetworks().stream().collect(
            Collectors.toMap(SensorNetworkDO::getNetworkInSensorId, SensorNetworkDO::getSensorId));
    for (String existNetwork : existNetworkInSubnetList) {
      for (String newNetwork : newNetworkInSubnetList) {
        if (StringUtils.equals(existNetworkSensorIdMap.get(existNetwork),
            existNetworkSensorIdMap.get(newNetwork))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "子网所属的多个网络不能属于同一探针");
        }
      }
    }
    // 校验新建的子网所属的多个网络是否属于同一个探针
    List<String> newNetworkList = CsvUtils
        .convertCSVToList(sensorLogicalSubnetBO.getNetworkInSensorIds());
    List<String> rawSensorId = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String networkId : newNetworkList) {
      rawSensorId.add(existNetworkSensorIdMap.get(networkId));
    }
    List<String> sensorId = rawSensorId.stream().distinct().collect(Collectors.toList());
    if (newNetworkList.size() > sensorId.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "子网所属的多个网络不能属于同一探针");
    }

    SensorLogicalSubnetDO sensorLogicalSubnetDO = new SensorLogicalSubnetDO();
    BeanUtils.copyProperties(sensorLogicalSubnetBO, sensorLogicalSubnetDO);
    sensorLogicalSubnetDO.setOperatorId(operatorId);

    sensorLogicalSubnetDO = sensorLogicalSubnetDao
        .saveOrRecoverSensorLogicalSubnet(sensorLogicalSubnetDO);

    if (CollectionUtils.isEmpty(metricSettings)) {
      // 配置子网默认的统计度量值
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setSourceType(FpcCmsConstants.SOURCE_TYPE_NETWORK);
      metricSettingBO.setNetworkId(sensorLogicalSubnetDO.getId());
      metricSettingService.saveDefaultMetricSettings(Lists.newArrayList(metricSettingBO),
          operatorId);
    } else {
      // 配置指定的统计度量值
      for (MetricSettingBO metricSetting : metricSettings) {
        metricSetting.setNetworkId(sensorLogicalSubnetDO.getId());
        metricSettingService.saveMetricSettings(metricSettings, operatorId);
      }
    }

    // 下发到直属fpc和cms
    List<MetricSettingDO> metricSetting = metricSettingDao.queryMetricSettings(
        FpcCmsConstants.SOURCE_TYPE_NETWORK, sensorLogicalSubnetDO.getId(), null, null);
    List<Map<String, Object>> messageBodys = Lists.newArrayList(sensorLogicalSubnet2MessageBody(
        sensorLogicalSubnetDO, metricSetting, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_LOGICALSUBNET, null);

    sensorLogicalSubnetBO.setId(sensorLogicalSubnetDO.getId());
    return sensorLogicalSubnetBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService#removeNetworkFromSubnet(java.lang.String, java.lang.String)
   */
  @Override
  public void removeNetworkFromSubnet(String networkId, String operatorId) {
    List<SensorLogicalSubnetDO> subnets = sensorLogicalSubnetDao
        .querySensorLogicalSubnetsByNetwork(networkId);
    subnets.forEach(subnet -> {
      List<String> networkIds = CsvUtils.convertCSVToList(subnet.getNetworkInSensorIds());
      networkIds.remove(networkId);
      if (networkIds.size() == 0) {
        deleteSensorLogicalSubnet(subnet.getId(), operatorId, true);
      } else {
        SensorLogicalSubnetBO sensorLogicalSubnetBO = querySensorLogicalSubnet(subnet.getId());
        sensorLogicalSubnetBO.setNetworkInSensorIds(CsvUtils.convertCollectionToCSV(networkIds));
        updateSensorLogicalSubnet(subnet.getId(), sensorLogicalSubnetBO, operatorId);
      }
    });
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService#deleteSensorLogicalSubnet(java.lang.String, java.lang.String, boolean)
   */
  @Transactional
  @Override
  public SensorLogicalSubnetBO deleteSensorLogicalSubnet(String id, String operatorId,
      boolean forceDelete) {
    SensorLogicalSubnetDO sensorLogicalSubnetDO = sensorLogicalSubnetDao
        .querySensorLogicalSubnet(id);
    if (StringUtils.isBlank(sensorLogicalSubnetDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "子网不存在");
    }

    // 删除子网时查看该子网是否配置在某个业务下，如果有则不能删除
    List<String> networkIdList = CsvUtils
        .convertCSVToList(sensorLogicalSubnetDO.getNetworkInSensorIds());
    List<String> temp = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String networkId : networkIdList) {
      String subnetId = networkId + "^" + id;
      temp.add(subnetId);
    }
    if (!forceDelete
        && CollectionUtils.isNotEmpty(serviceNetworkDao.queryExistServiceNetworkList(null, temp))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该子网存在相关联的业务，无法删除");
    }

    // 是否有告警作用于该子网
    if (!forceDelete && alertRuleService
        .queryAlertRulesBySource(FpcCmsConstants.SOURCE_TYPE_NETWORK, id, null).size() > 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该子网已作用于告警，无法删除");
    }

    // 该子网是否已配置到网络拓扑中
    NetworkTopologyDO networkTopologyDO = networkTopologyDao.queryNetworkTopologyByNetworkId(id);
    if (!forceDelete && StringUtils.isNotBlank(networkTopologyDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该子网已配置到网络拓扑中，无法删除");
    }

    // 删除网络权限
    sensorNetworkPermService.deleteSensorNetworkPermByNetwork(id);

    // 删除子网
    sensorLogicalSubnetDao.deleteSensorLogicalSubnet(id, operatorId);

    // 删除子网络下基线定义
    baselineService.deleteBaselineSettings(FpcCmsConstants.SOURCE_TYPE_NETWORK, id, null, null);

    // 删除子网络下的统计配置
    metricSettingService.deleteMetricSetting(FpcCmsConstants.SOURCE_TYPE_NETWORK, id, null, null);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists.newArrayList(sensorLogicalSubnet2MessageBody(
        sensorLogicalSubnetDO, null, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_LOGICALSUBNET, null);

    SensorLogicalSubnetBO sensorLogicalSubnetBO = new SensorLogicalSubnetBO();
    BeanUtils.copyProperties(sensorLogicalSubnetDO, sensorLogicalSubnetBO);
    return sensorLogicalSubnetBO;
  }

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
    // 获取子网所属的网络
    Map<String, List<String>> subnetNetworkIdMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<SensorLogicalSubnetDO> logicalSubnetList = sensorLogicalSubnetDao
        .querySensorLogicalSubnets(beforeTime);
    for (SensorLogicalSubnetDO logicalSubnet : logicalSubnetList) {
      List<String> networkList = CsvUtils.convertCSVToList(logicalSubnet.getNetworkInSensorIds());
      subnetNetworkIdMap.put(logicalSubnet.getId(), networkList);
    }

    // 如果指定了下发的设备，并且子网没有包含将要下发设备的网络，则不下发该子网
    if (!StringUtils.isAllBlank(deviceType, serialNumber)) {
      // 下发设备包含的网络
      List<String> fpcNetworkIds = fpcNetworkService.queryNetworks(deviceType, serialNumber)
          .stream().map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());

      Iterator<Entry<String, List<String>>> iterator = subnetNetworkIdMap.entrySet().iterator();
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

    List<String> subnetList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 子网所属网络ID集合
    subnetNetworkIdMap.forEach((subnetId, networkIds) -> {
      networkIds.forEach(networkId -> {
        subnetList.add(StringUtils.joinWith("_", subnetId, networkId));
      });
    });

    // 子网度量指标
    subnetNetworkIdMap.forEach((subnetId, networkIds) -> {
      subnetList.addAll(metricSettingDao.queryMetricSettingIds(FpcCmsConstants.SOURCE_TYPE_NETWORK,
          subnetId, null, beforeTime));
    });

    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_LOGICALSUBNET, subnetList);
    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurations(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_LOGICALSUBNET)) {
      // 获取子网所属的网络
      Map<String, List<String>> subnetNetworkIdMap = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      List<SensorLogicalSubnetDO> logicalSubnetList = sensorLogicalSubnetDao
          .querySensorLogicalSubnets();
      for (SensorLogicalSubnetDO logicalSubnet : logicalSubnetList) {
        List<String> networkList = CsvUtils.convertCSVToList(logicalSubnet.getNetworkInSensorIds());
        subnetNetworkIdMap.put(logicalSubnet.getId(), networkList);
      }

      // 如果指定了下发的设备，并且子网没有包含将要下发设备的网络，则不下发该子网
      if (!StringUtils.isAllBlank(deviceType, serialNumber)) {
        // 下发设备包含的网络
        List<String> fpcNetworkIds = fpcNetworkService.queryNetworks(deviceType, serialNumber)
            .stream().map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());

        Iterator<Entry<String, List<String>>> iterator = subnetNetworkIdMap.entrySet().iterator();
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

      // 当前子网列表
      List<Map<String, Object>> list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      for (String id : Lists.newArrayList(subnetNetworkIdMap.keySet())) {
        SensorLogicalSubnetDO logicalSubnet = sensorLogicalSubnetDao.querySensorLogicalSubnet(id);
        List<MetricSettingDO> metricSettings = metricSettingDao
            .queryMetricSettings(FpcCmsConstants.SOURCE_TYPE_NETWORK, null, id, null);
        list.add(sensorLogicalSubnet2MessageBody(logicalSubnet, metricSettings,
            FpcCmsConstants.SYNC_ACTION_ADD));
      }

      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> sensorLogicalSubnet2MessageBody(
      SensorLogicalSubnetDO sensorLogicalSubnetDO, List<MetricSettingDO> metricSettings,
      String action) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", sensorLogicalSubnetDO.getId());
    map.put("name", sensorLogicalSubnetDO.getName());
    map.put("type", sensorLogicalSubnetDO.getType());
    map.put("configuration", sensorLogicalSubnetDO.getConfiguration());
    map.put("networkIds", sensorLogicalSubnetDO.getNetworkInSensorIds());
    map.put("bandwidth", sensorLogicalSubnetDO.getBandwidth());
    map.put("metricSettings", metricSetting2MessageBody(metricSettings));

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
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_LOGICALSUBNET));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncLogicalSubnet(item)).sum();
    LOGGER.info("current sync subnet total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncLogicalSubnet(Map<String, Object> messageBody) {
    int syncCount = 0;

    List<String> networkIds = CsvUtils
        .convertCSVToList(MapUtils.getString(messageBody, "networkIds"));
    if (CollectionUtils.isEmpty(networkIds)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");
    SensorLogicalSubnetBO logicalSubnetBO = new SensorLogicalSubnetBO();
    logicalSubnetBO.setId(MapUtils.getString(messageBody, "id"));
    logicalSubnetBO.setAssignId(MapUtils.getString(messageBody, "id"));
    logicalSubnetBO.setName(MapUtils.getString(messageBody, "name"));
    logicalSubnetBO.setBandwidth(MapUtils.getIntValue(messageBody, "bandwidth", 0));
    logicalSubnetBO.setType(MapUtils.getString(messageBody, "type"));
    logicalSubnetBO.setConfiguration(MapUtils.getString(messageBody, "configuration"));

    // 判断下发的子网所在的网络是否存在
    List<String> existNetworkIds = sensorNetworkDao.querySensorNetworks().stream()
        .map(SensorNetworkDO::getNetworkInSensorId).collect(Collectors.toList());
    List<String> validNetworkIds = networkIds.stream()
        .filter(networkId -> existNetworkIds.contains(networkId)).collect(Collectors.toList());

    outer: if (CollectionUtils.isEmpty(validNetworkIds)) {
      if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_MODIFY)) {
        // 此类情况为上级编辑子网，删掉了子网所属于的a网络，下发到这里时，应该删掉属于a网络的子网
        action = FpcCmsConstants.SYNC_ACTION_DELETE;
        break outer;
      }
      // 不存在子网所在的网络
      return syncCount;
    }

    logicalSubnetBO.setNetworkInSensorIds(CsvUtils.convertCollectionToCSV(validNetworkIds));

    List<Map<String, Object>> metricSetting = JsonHelper.deserialize(
        JsonHelper.serialize(messageBody.get("metricSettings")),
        new TypeReference<List<Map<String, Object>>>() {
        });

    List<MetricSettingBO> metricSettingList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    metricSetting.forEach(metricSettingMap -> {
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setAssignId(MapUtils.getString(metricSettingMap, "id"));
      metricSettingBO.setSourceType(MapUtils.getString(metricSettingMap, "sourceType"));
      metricSettingBO.setNetworkId(MapUtils.getString(metricSettingMap, "networkId"));
      metricSettingBO.setServiceId(MapUtils.getString(metricSettingMap, "packetFileId"));
      metricSettingBO.setMetric(MapUtils.getString(metricSettingMap, "metric"));
      metricSettingBO.setValue(MapUtils.getString(metricSettingMap, "value"));
      metricSettingList.add(metricSettingBO);
    });

    SensorLogicalSubnetDO exist = sensorLogicalSubnetDao
        .queryLogicalSubnetByAssignId(logicalSubnetBO.getAssignId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateSensorLogicalSubnet(exist.getId(), logicalSubnetBO, CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveSensorLogicalSubnet(logicalSubnetBO, metricSettingList, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteSensorLogicalSubnet(exist.getId(), CMS_ASSIGNMENT, true);
          deleteCount++;
          break;
        default:
          break;
      }
      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync subnet status: [add: {}, modify: {}, delete: {}]", addCount,
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
    // 删除
    int clearCount = 0;
    List<String> subnetIds = sensorLogicalSubnetDao.querySensorLogicalSubnetIds(onlyLocal);
    for (String subnetId : subnetIds) {
      try {
        deleteSensorLogicalSubnet(subnetId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete subnet failed. error msg: {}", e.getMessage());
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
    // 获取上级cms下发的子网
    List<SensorLogicalSubnetDO> logicalSubnetList = sensorLogicalSubnetDao
        .queryAssignLogicalSubnets(beforeTime);

    List<String> assignIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 子网-网络ID集合
    for (SensorLogicalSubnetDO subnet : logicalSubnetList) {
      List<String> networkIds = CsvUtils.convertCSVToList(subnet.getNetworkInSensorIds());
      List<String> subnetNetworkIdList = networkIds.stream()
          .map(networkId -> StringUtils.joinWith("_", subnet.getAssignId(), networkId))
          .collect(Collectors.toList());
      assignIds.addAll(subnetNetworkIdList);
    }
    // 度量指标ID集合
    List<String> settingAssignIds = metricSettingDao
        .queryAssignMetricSettingIds(FpcCmsConstants.SOURCE_TYPE_NETWORK, beforeTime);
    assignIds.addAll(settingAssignIds);
    return assignIds;
  }
}
