package com.machloop.fpc.cms.center.broker.service.subordinate.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.alpha.webapp.system.data.AlarmDO;
import com.machloop.alpha.webapp.system.data.LogDO;
import com.machloop.fpc.cms.center.broker.bo.CollectMetricBO;
import com.machloop.fpc.cms.center.broker.dao.CollectMetricDao;
import com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao;
import com.machloop.fpc.cms.center.broker.data.CollectMetricDO;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.data.FpcNetworkDO;
import com.machloop.fpc.cms.center.broker.service.local.SendupMessageService;
import com.machloop.fpc.cms.center.broker.service.subordinate.CollectMetricService;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.central.dao.CentralDiskDao;
import com.machloop.fpc.cms.center.central.dao.CentralNetifDao;
import com.machloop.fpc.cms.center.central.dao.CentralRaidDao;
import com.machloop.fpc.cms.center.central.dao.CentralSystemDao;
import com.machloop.fpc.cms.center.central.dao.CmsDao;
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.data.CentralDiskDO;
import com.machloop.fpc.cms.center.central.data.CentralNetifDO;
import com.machloop.fpc.cms.center.central.data.CentralRaidDO;
import com.machloop.fpc.cms.center.central.data.CentralSystemDO;
import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.data.FpcDO;
import com.machloop.fpc.cms.center.central.service.CentralNetifService;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;
import com.machloop.fpc.cms.center.central.service.ClusterService;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.AlarmEntity;
import com.machloop.fpc.cms.grpc.CentralProto.CmsEntity;
import com.machloop.fpc.cms.grpc.CentralProto.DiskIOMetric;
import com.machloop.fpc.cms.grpc.CentralProto.DiskMonitor;
import com.machloop.fpc.cms.grpc.CentralProto.Entity;
import com.machloop.fpc.cms.grpc.CentralProto.LogEntity;
import com.machloop.fpc.cms.grpc.CentralProto.MetricEntity;
import com.machloop.fpc.cms.grpc.CentralProto.NetifMetric;
import com.machloop.fpc.cms.grpc.CentralProto.NetworkEntity;
import com.machloop.fpc.cms.grpc.CentralProto.RaidMonitor;
import com.machloop.fpc.cms.grpc.CentralProto.SendupReply;
import com.machloop.fpc.cms.grpc.CentralProto.SendupRequest;
import com.machloop.fpc.cms.grpc.CentralProto.SensorEntity;
import com.machloop.fpc.cms.grpc.CentralProto.SystemMetric;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author liyongjun
 *
 * create at 2019年11月8日, fpc-cms-center
 */
@Service
public class CollectMetricServiceImpl implements CollectMetricService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectMetricServiceImpl.class);

  private static final String SENDUP = "sendup";

  @Autowired
  private CollectMetricDao collectMetricDao;

  @Autowired
  private CentralSystemDao centralSystemDao;

  @Autowired
  private CentralNetifDao centralNetifDao;

  @Autowired
  private CentralRaidDao centralRaidDao;

  @Autowired
  private CentralDiskDao centralDiskDao;

  @Autowired
  private LogDao logDao;

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private FpcNetworkDao fpcNetworkDao;

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private CmsDao cmsDao;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private CentralSystemService centralSystemService;

  @Autowired
  private CentralNetifService centralNetifService;

  @Autowired
  private SendupMessageService sendupMessageService;

  @Autowired
  private DeviceStatusService deviceStatusService;

  @Autowired
  private ClusterService clusterService;

  private Map<String, Tuple3<Long, Integer, Integer>> collectMetricMap = Maps
      .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.CollectMetricService#queryCollectMetrics(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<CollectMetricBO> queryCollectMetrics(String deviceType, String serialNumber,
      String type, String startTimeStr, String endTimeStr) {
    // 构造查询范围
    Date startTime = DateUtils.parseISO8601Date(startTimeStr);
    Date endTime = DateUtils.parseISO8601Date(endTimeStr);

    // 获取统计记录
    List<CollectMetricDO> collectMetricDOList = collectMetricDao.queryCollectMetrics(deviceType,
        serialNumber, type, startTime, endTime);
    List<CollectMetricBO> collectMetricBOList = Lists
        .newArrayListWithExpectedSize(collectMetricDOList.size());
    for (CollectMetricDO collectMetricDO : collectMetricDOList) {
      CollectMetricBO collectMetricBO = new CollectMetricBO();
      BeanUtils.copyProperties(collectMetricDO, collectMetricBO);

      collectMetricBO.setStartTime(DateUtils.toStringISO8601(collectMetricDO.getStartTime()));
      collectMetricBO.setEndTime(DateUtils.toStringISO8601(collectMetricDO.getEndTime()));
      collectMetricBOList.add(collectMetricBO);
    }

    return collectMetricBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.CollectMetricService#processMessage(com.machloop.fpc.cms.grpc.CentralProto.SendupRequest)
   */
  @Transactional
  @Override
  public SendupReply processMessage(SendupRequest sendupRequest) {
    SendupReply messageReply = null;

    switch (sendupRequest.getMessageType()) {
      case FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC:
        messageReply = processMetricMessage(sendupRequest);
        break;
      case FpcCmsConstants.SENDUP_TYPE_LOG_ALARM:
        messageReply = processLogAndAlarmMessage(sendupRequest);
        break;
      case FpcCmsConstants.SENDUP_TYPE_NETWORK:
        messageReply = processNetworkMessage(sendupRequest);
        break;
      case FpcCmsConstants.SENDUP_TYPE_SENSOR:
        messageReply = processSensorMessage(sendupRequest);
        break;
      case FpcCmsConstants.SENDUP_TYPE_CMS:
        messageReply = processCmsMessage(sendupRequest);
        break;
      case FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC:
        messageReply = processResendMetricMessage(sendupRequest);
        break;
      case FpcCmsConstants.RESEND_TYPE_LOG_ALARM:
        messageReply = processResendLogAndAlarmMessage(sendupRequest);
        break;
      default:
        break;
    }

    // 刷新交互时间及本次交互时延
    DeviceStatusDO deviceStatus = deviceStatusService
        .queryDeviceStatus(sendupRequest.getDeviceType(), sendupRequest.getSerialNumber());
    deviceStatus.setDeviceType(sendupRequest.getDeviceType());
    deviceStatus.setSerialNumber(sendupRequest.getSerialNumber());
    deviceStatus.setLastInteractiveTime(new Date(sendupRequest.getTimestamp()));
    deviceStatus.setLastInteractiveLatency(
        Math.abs(DateUtils.now().getTime() - sendupRequest.getTimestamp()));
    deviceStatusService.refreshDeviceStatus(deviceStatus);

    return messageReply;
  }

  @Transactional
  private SendupReply processMetricMessage(SendupRequest sendupRequest) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("receive sendup system metric request, {}", sendupRequest);
    }

    String deviceType = sendupRequest.getDeviceType();
    String deviceSerialNumber = sendupRequest.getSerialNumber();

    MetricEntity metricEntity = sendupRequest.getEntity(0).getMetricEntity(0);

    // 构造CPU、内存、分区使用率、系统指标
    List<CentralSystemDO> centralSystems = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_CMS)) {
      List<SystemMetric> systemMetricList = metricEntity.getSystemMetricList();
      centralSystems = systemMetricList.stream().map(systemMetric -> {
        CentralSystemDO centralSystemDO = new CentralSystemDO();
        BeanUtils.copyProperties(metricEntity, centralSystemDO);
        BeanUtils.copyProperties(systemMetric, centralSystemDO);
        centralSystemDO.setDeviceType(deviceType);
        centralSystemDO.setMonitoredSerialNumber(deviceSerialNumber);
        centralSystemDO.setMetricTime(new Date(systemMetric.getMetricTime()));

        return centralSystemDO;
      }).collect(Collectors.toList());
    } else if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
      // 探针只需上报系统指标，系统状态统计通过查询clickhouse集群获取
      CentralSystemDO centralSystemDO = new CentralSystemDO();
      BeanUtils.copyProperties(metricEntity, centralSystemDO);
      centralSystemDO.setDeviceType(deviceType);
      centralSystemDO.setMonitoredSerialNumber(deviceSerialNumber);
      centralSystemDO.setMetricTime(new Date(metricEntity.getMetricTime()));
      centralSystems.add(centralSystemDO);
    }

    // 将CPU、内存、分区使用率、系统指标对象存入数据库
    if (CollectionUtils.isNotEmpty(centralSystems)) {
      centralSystemService.collectCentralSystem(deviceType, centralSystems);
    }

    // 构造网卡流量信息
    List<NetifMetric> netifMetricList = metricEntity.getNetifMetricList();
    List<CentralNetifDO> netifMetricDOList = Lists.newArrayListWithCapacity(netifMetricList.size());
    for (NetifMetric netifMetric : netifMetricList) {
      CentralNetifDO centralNetifDO = new CentralNetifDO();
      BeanUtils.copyProperties(netifMetric, centralNetifDO);
      centralNetifDO.setDeviceType(deviceType);
      centralNetifDO.setMonitoredSerialNumber(deviceSerialNumber);
      centralNetifDO.setMetricTime(new Date(netifMetric.getMetricTime()));

      netifMetricDOList.add(centralNetifDO);
    }

    // 将网卡流量存入数据库
    if (CollectionUtils.isNotEmpty(netifMetricDOList)) {
      centralNetifService.collectCentralNetifs(netifMetricDOList);
    }

    if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
      // 构造raid信息对象
      List<RaidMonitor> raidMonitorList = metricEntity.getRaidMonitorList();
      List<
          CentralRaidDO> centralRaidDOList = Lists.newArrayListWithCapacity(raidMonitorList.size());
      for (RaidMonitor raidMonitor : raidMonitorList) {
        CentralRaidDO centralRaidDO = new CentralRaidDO();
        BeanUtils.copyProperties(raidMonitor, centralRaidDO);
        centralRaidDO.setDeviceType(deviceType);
        centralRaidDO.setDeviceSerialNumber(deviceSerialNumber);

        centralRaidDOList.add(centralRaidDO);
      }

      // 将raid信息对象存入数据库
      if (CollectionUtils.isNotEmpty(centralRaidDOList)) {
        centralRaidDao.batchSaveOrUpdateCentralRaids(centralRaidDOList);
      }

      // 构造硬盘信息对象
      List<DiskMonitor> diskMonitorList = metricEntity.getDiskMonitorList();
      List<
          CentralDiskDO> centralDiskDOList = Lists.newArrayListWithCapacity(diskMonitorList.size());
      for (DiskMonitor diskMonitor : diskMonitorList) {
        CentralDiskDO centralDiskDO = new CentralDiskDO();
        BeanUtils.copyProperties(diskMonitor, centralDiskDO);
        centralDiskDO.setDeviceType(deviceType);
        centralDiskDO.setDeviceSerialNumber(deviceSerialNumber);
        centralDiskDO.setDescription("");

        centralDiskDOList.add(centralDiskDO);
      }

      // 将硬盘信息对象存入数据库
      if (CollectionUtils.isNotEmpty(centralDiskDOList)) {
        centralDiskDao.batchSaveOrUpdateCentralDisks(centralDiskDOList);
      }

      // 硬盘IO速率
      /*List<DiskIOMetric> diskIOMetricList = metricEntity.getDiskIoMetricList();
      List<CentralDiskIODO> centralDiskIOList = Lists
          .newArrayListWithCapacity(diskIOMetricList.size());
      diskIOMetricList.forEach(diskIOMetric -> {
        CentralDiskIODO centralDiskIODO = new CentralDiskIODO();
        BeanUtils.copyProperties(diskIOMetric, centralDiskIODO);
        centralDiskIODO.setDeviceType(deviceType);
        centralDiskIODO.setMonitoredSerialNumber(deviceSerialNumber);
        centralDiskIODO.setMetricTime(new Date(diskIOMetric.getMetricTime()));
      
        centralDiskIOList.add(centralDiskIODO);
      });
      
      if (CollectionUtils.isNotEmpty(centralDiskIOList)) {
        centralDiskIOService.collectCentralDiskIOs(centralDiskIOList);
      }*/
    }

    // 上报到上级CMS
    sendupMessageService.sendupMessage(sendupRequest);

    // 构造回复消息
    String messageId = sendupRequest.getMessageId();
    String messageType = sendupRequest.getMessageType();
    SendupReply reply = SendupReply.newBuilder().setMessageId(messageId).setMessageType(messageType)
        .setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).build();
    return reply;
  }

  @Transactional
  private SendupReply processLogAndAlarmMessage(SendupRequest sendupRequest) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("receive collect log and alarm request, {}", sendupRequest);
    }

    // 构造日志对象
    List<LogEntity> logEntityList = sendupRequest.getEntity(0).getLogEntityList();
    List<LogDO> logDOList = Lists.newArrayListWithCapacity(logEntityList.size());
    for (LogEntity logEntity : logEntityList) {
      LogDO logDO = new LogDO();
      BeanUtils.copyProperties(logEntity, logDO);
      logDO.setAriseTime(new Date(logEntity.getAriseTime()));

      logDOList.add(logDO);
    }

    // 将日志对象存入数据库
    if (CollectionUtils.isNotEmpty(logDOList)) {
      // 判断是否为重复上报
      Map<String, String> logMap = logDOList.stream()
          .collect(Collectors.toMap(LogDO::getId, LogDO::getNodeId));
      List<LogDO> existedLogList = logDao.queryLogs(Lists.newArrayList(logMap.keySet()));
      if (CollectionUtils.isNotEmpty(existedLogList)) {
        List<String> duplicateLogIds = existedLogList.stream()
            .filter(log -> StringUtils.equals(log.getNodeId(), logMap.get(log.getId())))
            .map(LogDO::getId).collect(Collectors.toList());

        // 移除重复的日志
        if (CollectionUtils.isNotEmpty(duplicateLogIds)) {
          logDOList = logDOList.stream().filter(log -> !duplicateLogIds.contains(log.getId()))
              .collect(Collectors.toList());
        }
        // 排查与本机日志重复的ID
        List<String> resetIdLogs = existedLogList.stream()
            .filter(log -> !duplicateLogIds.contains(log.getId())).map(LogDO::getId)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(resetIdLogs)) {
          logDOList = logDOList.stream().map(log -> {
            if (resetIdLogs.contains(log.getId())) {
              log.setId(null);
            }

            return log;
          }).collect(Collectors.toList());
        }
      }

      logDao.saveLogs(logDOList);
    }

    // 构造告警对象
    List<AlarmEntity> alarmEntityList = sendupRequest.getEntity(0).getAlarmEntityList();
    List<AlarmDO> alarmDOList = Lists.newArrayListWithCapacity(alarmEntityList.size());
    for (AlarmEntity alarmEntity : alarmEntityList) {
      AlarmDO alarmDO = new AlarmDO();
      BeanUtils.copyProperties(alarmEntity, alarmDO);
      alarmDO.setAriseTime(new Date(alarmEntity.getAriseTime()));
      if (alarmEntity.getSolveTime() != 0L) {
        alarmDO.setSolveTime(new Date(alarmEntity.getSolveTime()));
      }

      alarmDOList.add(alarmDO);
    }

    // 将告警对象存入数据库
    if (CollectionUtils.isNotEmpty(alarmDOList)) {
      // 判断是否为重复上报
      Map<String, String> alarmMap = alarmDOList.stream()
          .collect(Collectors.toMap(AlarmDO::getId, AlarmDO::getNodeId));
      List<AlarmDO> existedAlarmList = alarmDao.queryAlarms(Lists.newArrayList(alarmMap.keySet()));
      if (CollectionUtils.isNotEmpty(existedAlarmList)) {
        List<String> duplicateAlarmIds = existedAlarmList.stream()
            .filter(alarm -> StringUtils.equals(alarm.getNodeId(), alarmMap.get(alarm.getId())))
            .map(AlarmDO::getId).collect(Collectors.toList());

        // 移除重复的告警
        if (CollectionUtils.isNotEmpty(duplicateAlarmIds)) {
          alarmDOList = alarmDOList.stream()
              .filter(alarm -> !duplicateAlarmIds.contains(alarm.getId()))
              .collect(Collectors.toList());
        }
        // 排查与本机告警重复的ID
        List<String> resetIdAlarms = existedAlarmList.stream()
            .filter(alarm -> !duplicateAlarmIds.contains(alarm.getId())).map(AlarmDO::getId)
            .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(resetIdAlarms)) {
          alarmDOList = alarmDOList.stream().map(alarm -> {
            if (resetIdAlarms.contains(alarm.getId())) {
              alarm.setId(null);
            }

            return alarm;
          }).collect(Collectors.toList());
        }
      }

      alarmDao.saveAlarms(alarmDOList);
    }

    // 上报到上级CMS
    sendupMessageService.sendupMessage(sendupRequest);

    // 构造回复消息
    String messageId = sendupRequest.getMessageId();
    String messageType = sendupRequest.getMessageType();
    SendupReply reply = SendupReply.newBuilder().setMessageId(messageId).setMessageType(messageType)
        .setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).build();
    return reply;
  }

  @Transactional
  private SendupReply processNetworkMessage(SendupRequest sendupRequest) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("receive collect fpc network request, {}", sendupRequest);
    }

    // 构造对象
    List<NetworkEntity> networkEntityList = sendupRequest.getEntity(0).getNetworkEntityList();

    List<String> existFpcNetworkIds = fpcNetworkDao.queryFpcNetworks(null).stream()
        .map(network -> network.getFpcNetworkId()).collect(Collectors.toList());

    List<FpcNetworkDO> addNetworks = Lists.newArrayListWithCapacity(networkEntityList.size());
    List<FpcNetworkDO> modifyNetworks = Lists.newArrayListWithCapacity(networkEntityList.size());
    List<String> deleteNetworkIds = Lists.newArrayListWithCapacity(networkEntityList.size());
    networkEntityList.forEach(networkEntity -> {
      switch (networkEntity.getAction()) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          boolean exist = existFpcNetworkIds.contains(networkEntity.getId());

          FpcNetworkDO network = new FpcNetworkDO();
          network.setFpcNetworkId(networkEntity.getId());
          network.setFpcNetworkName(networkEntity.getName());
          network.setBandwidth(networkEntity.getBandwidth());
          network.setFpcSerialNumber(networkEntity.getFpcSerialNumber());
          network.setReportState(Constants.BOOL_NO);
          network.setReportAction(
              exist ? FpcCmsConstants.SYNC_ACTION_MODIFY : FpcCmsConstants.SYNC_ACTION_ADD);
          network.setOperatorId(SENDUP);
          if (exist) {
            modifyNetworks.add(network);
          } else {
            addNetworks.add(network);
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteNetworkIds.add(networkEntity.getId());
          break;
        default:
          break;
      }
    });

    // 本次采集到的数据为下级设备全量上报
    if (sendupRequest.getAll()) {
      // 下级设备已经上报的所有网络集合
      List<String> currenDeviceIncludeNetworks = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (StringUtils.equals(sendupRequest.getDeviceType(), FpcCmsConstants.DEVICE_TYPE_CMS)) {
        Map<String, List<CmsDO>> cmsHierarchy = cmsDao.queryCms(new CmsQueryVO()).stream()
            .collect(Collectors.groupingBy(CmsDO::getSuperiorCmsSerialNumber));
        List<String> subordinateCms = iterateSubordinateCms(sendupRequest.getSerialNumber(),
            cmsHierarchy);
        currenDeviceIncludeNetworks.addAll(fpcNetworkDao.queryFpcNetworkByCms(subordinateCms)
            .stream().map(FpcNetworkDO::getFpcNetworkId).collect(Collectors.toList()));
      } else if (StringUtils.equals(sendupRequest.getDeviceType(),
          FpcCmsConstants.DEVICE_TYPE_TFA)) {
        currenDeviceIncludeNetworks
            .addAll(fpcNetworkDao.queryFpcNetworks(sendupRequest.getSerialNumber()).stream()
                .map(FpcNetworkDO::getFpcNetworkId).collect(Collectors.toList()));
      }

      // 根据本次全量上报的结果，判断已存在的网络哪些已经被移除
      currenDeviceIncludeNetworks.removeAll(
          networkEntityList.stream().map(NetworkEntity::getId).collect(Collectors.toList()));
      if (CollectionUtils.isNotEmpty(currenDeviceIncludeNetworks)) {
        deleteNetworkIds.addAll(currenDeviceIncludeNetworks);
      }
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;

    // 新增网络
    if (CollectionUtils.isNotEmpty(addNetworks)) {
      addCount = fpcNetworkDao.batchSaveFpcNetworks(addNetworks);
    }

    // 修改网络
    modifyNetworks.forEach(modifyNetwork -> {
      fpcNetworkDao.updateFpcNetwork(modifyNetwork);
    });
    modifyCount = modifyNetworks.size();

    // 删除网络
    if (CollectionUtils.isNotEmpty(deleteNetworkIds)) {
      // 网络删除时，需要联动删除和网络相关的所有配置（FPC网络、告警、网络关联的业务、网络组、子网、探针网络）
      deleteCount = fpcNetworkService.deleteNetworkByLinkage(deleteNetworkIds, SENDUP);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("current sync network status: [add: {}, modify: {}, delete: {}]", addCount,
          modifyCount, deleteCount);
    }

    // 构造回复消息
    String messageId = sendupRequest.getMessageId();
    String messageType = sendupRequest.getMessageType();
    SendupReply reply = SendupReply.newBuilder().setMessageId(messageId).setMessageType(messageType)
        .setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).build();
    return reply;
  }

  @Transactional
  private SendupReply processSensorMessage(SendupRequest sendupRequest) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("receive collect cms request, {}", sendupRequest);
    }

    // 构造对象
    List<SensorEntity> sensorEntityList = sendupRequest.getEntity(0).getSensorEntityList();

    // 查询所有探针设备，防止探针的上级更换
    List<String> existFpcSerialNumbers = fpcDao.queryFpcs(new FpcQueryVO()).stream()
        .map(FpcDO::getSerialNumber).collect(Collectors.toList());

    List<FpcDO> addFpcs = Lists.newArrayListWithCapacity(sensorEntityList.size());
    List<FpcDO> modifyFpcs = Lists.newArrayListWithCapacity(sensorEntityList.size());
    List<String> deleteFpcSerialNumbers = Lists.newArrayListWithCapacity(sensorEntityList.size());
    sensorEntityList.forEach(sensor -> {
      switch (sensor.getAction()) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          boolean exist = existFpcSerialNumbers.contains(sensor.getSerialNumber());

          FpcDO fpcDO = new FpcDO();
          BeanUtils.copyProperties(sensor, fpcDO);
          fpcDO.setAppToken(KeyEncUtils.encrypt(
              HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
              sensor.getAppToken()));
          fpcDO.setCmsToken(KeyEncUtils.encrypt(
              HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
              sensor.getCmsToken()));
          fpcDO.setDescription("");
          fpcDO.setReportState(Constants.BOOL_NO);
          fpcDO.setReportAction(
              exist ? FpcCmsConstants.SYNC_ACTION_MODIFY : FpcCmsConstants.SYNC_ACTION_ADD);
          fpcDO.setOperatorId(SENDUP);
          if (exist) {
            modifyFpcs.add(fpcDO);
          } else {
            addFpcs.add(fpcDO);
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteFpcSerialNumbers.add(sensor.getSerialNumber());
          break;
        default:
          break;
      }
    });

    // 本次采集到的数据为下级设备全量上报
    if (sendupRequest.getAll()) {
      // 下级CMS设备已经上报的所有探针集合
      Map<String, List<CmsDO>> cmsHierarchy = cmsDao.queryCms(new CmsQueryVO()).stream()
          .collect(Collectors.groupingBy(CmsDO::getSuperiorCmsSerialNumber));
      List<String> subordinateCms = iterateSubordinateCms(sendupRequest.getSerialNumber(),
          cmsHierarchy);
      List<String> currenReportCmsIncludeFpcs = fpcDao.queryFpcByCms(subordinateCms).stream()
          .map(FpcDO::getSerialNumber).collect(Collectors.toList());

      // 根据本次全量上报的结果，判断已存在的探针哪些已经被移除
      currenReportCmsIncludeFpcs.removeAll(sensorEntityList.stream()
          .map(SensorEntity::getSerialNumber).collect(Collectors.toList()));
      if (CollectionUtils.isNotEmpty(currenReportCmsIncludeFpcs)) {
        deleteFpcSerialNumbers.addAll(currenReportCmsIncludeFpcs);
      }
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;

    // 新增探针设备
    if (CollectionUtils.isNotEmpty(addFpcs)) {
      addCount = fpcDao.batchSaveFpcs(addFpcs);
    }

    // 修改探针设备
    for (FpcDO fpcDO : modifyFpcs) {
      modifyCount += fpcDao.updateFpcStatus(fpcDO);
    }

    // 删除探针设备
    if (CollectionUtils.isNotEmpty(deleteFpcSerialNumbers)) {
      deleteCount = fpcDao.deleteFpcBySerialNumbers(deleteFpcSerialNumbers, SENDUP);
    }

    // 集群节点与当前实际管理的探针设备保持一致
    clusterService.queryAbnormalNodesAndRefresh();

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("current sync sensor status: [add: {}, modify: {}, delete: {}]", addCount,
          modifyCount, deleteCount);
    }

    // 构造回复消息
    String messageId = sendupRequest.getMessageId();
    String messageType = sendupRequest.getMessageType();
    SendupReply reply = SendupReply.newBuilder().setMessageId(messageId).setMessageType(messageType)
        .setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).build();
    return reply;
  }

  @Transactional
  private SendupReply processCmsMessage(SendupRequest sendupRequest) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("receive collect cms request, {}", sendupRequest);
    }

    // 构造对象
    List<CmsEntity> cmsEntityList = sendupRequest.getEntity(0).getCmsEntityList();

    // 查询所有下级CMS设备，防止CMS设备的上级更换
    List<String> existCmsSerialNumbers = cmsDao.queryCms(new CmsQueryVO()).stream()
        .map(CmsDO::getSerialNumber).collect(Collectors.toList());

    List<CmsDO> addCmsList = Lists.newArrayListWithCapacity(cmsEntityList.size());
    List<CmsDO> modifyCmsList = Lists.newArrayListWithCapacity(cmsEntityList.size());
    List<String> deleteCmsSerialNumbers = Lists.newArrayListWithCapacity(cmsEntityList.size());
    cmsEntityList.forEach(cms -> {
      switch (cms.getAction()) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          boolean exist = existCmsSerialNumbers.contains(cms.getSerialNumber());

          CmsDO cmsDO = new CmsDO();
          BeanUtils.copyProperties(cms, cmsDO);
          cmsDO.setAppToken(KeyEncUtils.encrypt(
              HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
              cms.getAppToken()));
          cmsDO.setCmsToken(KeyEncUtils.encrypt(
              HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
              cms.getCmsToken()));
          cmsDO.setDescription("");
          cmsDO.setReportState(Constants.BOOL_NO);
          cmsDO.setReportAction(
              exist ? FpcCmsConstants.SYNC_ACTION_MODIFY : FpcCmsConstants.SYNC_ACTION_ADD);
          cmsDO.setOperatorId(SENDUP);
          if (exist) {
            modifyCmsList.add(cmsDO);
          } else {
            addCmsList.add(cmsDO);
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteCmsSerialNumbers.add(cms.getSerialNumber());
          break;
        default:
          break;
      }
    });

    // 本次采集到的数据为下级设备全量上报
    if (sendupRequest.getAll()) {
      // 下级CMS设备已经上报的所有CMS集合
      Map<String, List<CmsDO>> cmsHierarchy = cmsDao.queryCms(new CmsQueryVO()).stream()
          .collect(Collectors.groupingBy(CmsDO::getSuperiorCmsSerialNumber));
      List<String> currenReportCmsIncludeCmss = iterateSubordinateCms(
          sendupRequest.getSerialNumber(), cmsHierarchy);
      currenReportCmsIncludeCmss.remove(sendupRequest.getSerialNumber());

      // 根据本次全量上报的结果，判断已存在的CMS哪些已经被移除
      currenReportCmsIncludeCmss.removeAll(
          cmsEntityList.stream().map(CmsEntity::getSerialNumber).collect(Collectors.toList()));
      if (CollectionUtils.isNotEmpty(currenReportCmsIncludeCmss)) {
        deleteCmsSerialNumbers.addAll(currenReportCmsIncludeCmss);
      }
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;

    // 新增CMS设备
    if (CollectionUtils.isNotEmpty(addCmsList)) {
      addCount = cmsDao.batchSaveCms(addCmsList);
    }

    // 修改CMS设备
    for (CmsDO cmsDO : modifyCmsList) {
      modifyCount += cmsDao.updateCmsStatus(cmsDO);
    }

    // 删除CMS设备
    if (CollectionUtils.isNotEmpty(deleteCmsSerialNumbers)) {
      deleteCount = cmsDao.deleteCmsBySerialNumbers(deleteCmsSerialNumbers, SENDUP);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("current sync cms device status: [add: {}, modify: {}, delete: {}]", addCount,
          modifyCount, deleteCount);
    }

    // 构造回复消息
    String messageId = sendupRequest.getMessageId();
    String messageType = sendupRequest.getMessageType();
    SendupReply reply = SendupReply.newBuilder().setMessageId(messageId).setMessageType(messageType)
        .setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).build();
    return reply;
  }

  /**
   * 遍历单个CMS所管理的下级CMS
   * @param outset
   * @param cmsHierarchy
   * @return
   */
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

  @Transactional
  private SendupReply processResendMetricMessage(SendupRequest sendupRequest) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("receive supplement collect system metric request, {}", sendupRequest);
    }

    String deviceType = sendupRequest.getDeviceType();
    String deviceSerialNumber = sendupRequest.getSerialNumber();

    // 构造CPU、内存、网口流量、缓存统计对象
    List<Entity> entityList = sendupRequest.getEntityList();
    List<CentralSystemDO> centralSystemDOList = Lists.newArrayListWithCapacity(entityList.size());
    List<Date> monitorMetricTimeList = Lists.newArrayListWithCapacity(entityList.size());
    List<NetifMetric> netifMetricList = Lists.newArrayListWithCapacity(entityList.size());
    List<DiskIOMetric> diskIOMetricList = Lists.newArrayListWithCapacity(entityList.size());
    for (Entity entity : entityList) {
      List<MetricEntity> metricEntityList = entity.getMetricEntityList();
      for (MetricEntity metricEntity : metricEntityList) {
        Date metricTime = new Date(metricEntity.getMetricTime());
        monitorMetricTimeList.add(metricTime);

        CentralSystemDO centralSystemDO = new CentralSystemDO();
        BeanUtils.copyProperties(metricEntity, centralSystemDO);
        centralSystemDO.setDeviceType(deviceType);
        centralSystemDO.setMonitoredSerialNumber(deviceSerialNumber);
        centralSystemDO.setMetricTime(metricTime);
        centralSystemDOList.add(centralSystemDO);

        netifMetricList.addAll(metricEntity.getNetifMetricList());

        diskIOMetricList.addAll(metricEntity.getDiskIoMetricList());
      }
    }

    // 查询数据库已存在的统计记录
    List<Date> existedMetricTimeList = centralSystemDao.queryCentralSystemsMetricTime(deviceType,
        deviceSerialNumber, monitorMetricTimeList);

    // 将数据库中已存在的统计记录过滤掉
    if (CollectionUtils.isNotEmpty(existedMetricTimeList)) {
      Iterator<CentralSystemDO> centralSystemIterator = centralSystemDOList.iterator();
      while (centralSystemIterator.hasNext()) {
        CentralSystemDO centralSystemDO = centralSystemIterator.next();
        if (existedMetricTimeList.contains(centralSystemDO.getMetricTime())) {
          centralSystemIterator.remove();
        }
      }
    }

    // 将数据库中没有的统计记录存入数据库
    if (CollectionUtils.isNotEmpty(centralSystemDOList)) {
      centralSystemDao.saveCentralSystems(centralSystemDOList);
    }

    // 构造网卡流量对象
    List<CentralNetifDO> netifMetricDOList = Lists.newArrayListWithCapacity(netifMetricList.size());
    Set<Date> netifMetricTimeSet = Sets.newHashSetWithExpectedSize(netifMetricList.size());
    for (NetifMetric netifMetric : netifMetricList) {
      Date netifMetricTime = new Date(netifMetric.getMetricTime());
      netifMetricTimeSet.add(netifMetricTime);

      CentralNetifDO centralNetifDO = new CentralNetifDO();
      BeanUtils.copyProperties(netifMetric, centralNetifDO);
      centralNetifDO.setDeviceType(deviceType);
      centralNetifDO.setMonitoredSerialNumber(deviceSerialNumber);
      centralNetifDO.setMetricTime(netifMetricTime);
      netifMetricDOList.add(centralNetifDO);
    }

    // 查询数据库已存在的网卡流量统计记录
    List<Date> existedNetifMetricTimeList = centralNetifDao.queryCentralNetifMetricTime(deviceType,
        deviceSerialNumber, Lists.newArrayList(netifMetricTimeSet));

    // 将数据库中已存在的网卡流量记录过滤掉
    if (CollectionUtils.isNotEmpty(existedNetifMetricTimeList)) {
      Iterator<CentralNetifDO> netifMetricIterator = netifMetricDOList.iterator();
      while (netifMetricIterator.hasNext()) {
        CentralNetifDO centralNetifDO = netifMetricIterator.next();
        if (existedNetifMetricTimeList.contains(centralNetifDO.getMetricTime())) {
          netifMetricIterator.remove();
        }
      }
    }

    // 写入数据库中不存在的网卡流量
    if (CollectionUtils.isNotEmpty(netifMetricDOList)) {
      centralNetifDao.saveCentralNetifs(netifMetricDOList);
    }

    // 汇总五分钟的记录，重新更新到5分钟表里
    reRollup(deviceType, deviceSerialNumber, new Date(sendupRequest.getStartTime()),
        new Date(sendupRequest.getEndTime()));

    // 上报到上级CMS
    sendupMessageService.sendupMessage(sendupRequest);

    // 构造回复消息
    String messageId = sendupRequest.getMessageId();
    String messageType = sendupRequest.getMessageType();
    SendupReply reply = SendupReply.newBuilder().setMessageId(messageId).setMessageType(messageType)
        .setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).build();
    return reply;
  }

  @Transactional
  private SendupReply processResendLogAndAlarmMessage(SendupRequest sendupRequest) {
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("receive supplement collect log and alarm request, {}", sendupRequest);
    }

    // 构造日志对象
    List<Entity> entityList = sendupRequest.getEntityList();
    Set<String> logIdSet = Sets.newHashSetWithExpectedSize(entityList.size());
    List<LogDO> logDOList = Lists.newArrayListWithCapacity(entityList.size());
    for (Entity entity : entityList) {
      for (LogEntity logEntity : entity.getLogEntityList()) {
        LogDO fpcLogDO = new LogDO();
        BeanUtils.copyProperties(logEntity, fpcLogDO);
        fpcLogDO.setAriseTime(new Date(logEntity.getAriseTime()));

        if (!logIdSet.contains(logEntity.getId())) {
          logIdSet.add(logEntity.getId());
          logDOList.add(fpcLogDO);
        }
      }
    }

    // 过滤掉数据库已经存在的日志
    List<LogDO> existedLogList = logDao.queryLogs(Lists.newArrayList(logIdSet));
    if (CollectionUtils.isNotEmpty(existedLogList)) {

      // 查询已经存在的日志的id
      List<String> existedIdList = Lists.newArrayListWithCapacity(existedLogList.size());
      for (LogDO logDO : existedLogList) {
        existedIdList.add(logDO.getId());
      }

      // 根据id过滤日志
      Iterator<LogDO> logIterator = logDOList.iterator();
      while (logIterator.hasNext()) {
        LogDO logDO = logIterator.next();
        if (existedIdList.contains(logDO.getId())) {
          logIterator.remove();
        }
      }
    }

    // 将日志保存到数据库
    if (CollectionUtils.isNotEmpty(logDOList)) {
      logDao.saveLogs(logDOList);
    }

    // 构造告警对象
    Set<String> alarmIdSet = Sets.newHashSetWithExpectedSize(entityList.size());
    List<AlarmDO> alarmDOList = Lists.newArrayListWithCapacity(entityList.size());
    for (Entity entity : entityList) {
      for (AlarmEntity alarmEntity : entity.getAlarmEntityList()) {
        AlarmDO alarmDO = new AlarmDO();
        BeanUtils.copyProperties(alarmEntity, alarmDO);
        alarmDO.setAriseTime(new Date(alarmEntity.getAriseTime()));
        if (alarmEntity.getSolveTime() != 0L) {
          alarmDO.setSolveTime(new Date(alarmEntity.getSolveTime()));
        }

        if (!alarmIdSet.contains(alarmEntity.getId())) {
          alarmIdSet.add(alarmEntity.getId());
          alarmDOList.add(alarmDO);
        }
      }
    }

    // 过滤掉数据库已经存在的告警
    List<AlarmDO> existedAlarmList = alarmDao.queryAlarms(Lists.newArrayList(alarmIdSet));
    if (CollectionUtils.isNotEmpty(existedAlarmList)) {

      // 查询已经存在的告警的entityId
      List<String> existedIdList = Lists.newArrayListWithCapacity(existedAlarmList.size());
      for (AlarmDO alarmDO : existedAlarmList) {
        existedIdList.add(alarmDO.getId());
      }

      // 根据entityId过滤告警
      Iterator<AlarmDO> alarmIterator = alarmDOList.iterator();
      while (alarmIterator.hasNext()) {
        AlarmDO alarmDO = alarmIterator.next();
        if (existedIdList.contains(alarmDO.getId())) {
          alarmIterator.remove();
        }
      }
    }

    // 将告警存入数据库
    if (CollectionUtils.isNotEmpty(alarmDOList)) {
      alarmDao.saveAlarms(alarmDOList);
    }

    // 上报到上级CMS
    sendupMessageService.sendupMessage(sendupRequest);

    // 构造回复信息
    String messageId = sendupRequest.getMessageId();
    String messageType = sendupRequest.getMessageType();
    SendupReply reply = SendupReply.newBuilder().setMessageId(messageId).setMessageType(messageType)
        .setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).build();
    return reply;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.CollectMetricService#metricSendupMessage(com.machloop.fpc.cms.grpc.CentralProto.SendupRequest)
   */
  @Override
  public void metricSendupMessage(SendupRequest sendupRequest) {
    // 构造当前60秒为时间段统计
    long multipleOfInterval = sendupRequest.getStartTime() / 1000 / Constants.ONE_MINUTE_SECONDS;
    Date metricStartTime = new Date(multipleOfInterval * Constants.ONE_MINUTE_SECONDS * 1000);
    Date metricEndTime = (new Date(
        metricStartTime.getTime() + Constants.ONE_MINUTE_SECONDS * 1000));

    // 获取统计类型并初始化上报数量和内容数量
    int collectAmount = 1;
    int entityAmount = 1;
    String statisticType = "";
    String messageType = sendupRequest.getMessageType();
    Entity entity = sendupRequest.getEntity(0);
    if (StringUtils.equals(messageType, FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC)) {
      statisticType = FpcCmsConstants.STATISTIC_TYPE_SYSTEM_METRIC;
    } else if (StringUtils.equals(messageType, FpcCmsConstants.SENDUP_TYPE_LOG_ALARM)) {
      entityAmount = entity.getLogEntityCount() + entity.getAlarmEntityCount();
      statisticType = FpcCmsConstants.STATISTIC_TYPE_LOG_ALARM;
    }

    // 从统计map中获取上一次统计数据元组
    String metricKey = StringUtils.joinWith("_", sendupRequest.getDeviceType(),
        sendupRequest.getSerialNumber(), statisticType);
    Tuple3<Long, Integer, Integer> collectMetricTuple = collectMetricMap.get(metricKey);

    // 初始化第一次统计或者下一个60秒开始统计
    if (collectMetricTuple == null || metricStartTime.getTime() > collectMetricTuple.getT1()) {
      collectMetricTuple = Tuples.of(metricStartTime.getTime(), collectAmount, entityAmount);
      collectMetricMap.put(metricKey, collectMetricTuple);
    } else if (collectMetricTuple.getT1() == metricStartTime.getTime()) {
      // 本次60秒钟累加统计次数
      collectAmount = collectMetricTuple.getT2() + 1;
      entityAmount = entityAmount + collectMetricTuple.getT3();
      collectMetricTuple = Tuples.of(metricStartTime.getTime(), collectAmount, entityAmount);
      collectMetricMap.put(metricKey, collectMetricTuple);
    }

    // 构造统计对象
    CollectMetricDO collectMetricDO = new CollectMetricDO();
    collectMetricDO.setDeviceType(sendupRequest.getDeviceType());
    collectMetricDO.setDeviceSerialNumber(sendupRequest.getSerialNumber());
    collectMetricDO.setType(statisticType);
    collectMetricDO.setStartTime(metricStartTime);
    collectMetricDO.setEndTime(metricEndTime);
    collectMetricDO.setCollectAmount(collectAmount);
    collectMetricDO.setEntityAmount(entityAmount);

    // 将统计对象存入数据库
    collectMetricDao.saveOrUpdateCollectMetric(collectMetricDO);
    LOGGER.debug("receive sendup message collect metric is {}.", collectMetricDO);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.CollectMetricService#metricResendMessage(com.machloop.fpc.cms.grpc.CentralProto.SendupRequest)
   */
  @Override
  public void metricResendMessage(SendupRequest sendupRequest) {

    // 获取统计类型并初始化内容数量
    String deviceType = sendupRequest.getDeviceType();
    String serialNumber = sendupRequest.getSerialNumber();
    String messageType = sendupRequest.getMessageType();
    List<Entity> entityList = sendupRequest.getEntityList();
    if (StringUtils.equals(messageType, FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC)) {
      // 每个60秒的统计数据是一个Entity
      for (Entity entity : entityList) {
        mergeCollectMetric(deviceType, serialNumber, FpcCmsConstants.STATISTIC_TYPE_SYSTEM_METRIC,
            1, 1, new Date(entity.getStartTime()), new Date(entity.getEndTime()));
      }

    } else if (StringUtils.equals(messageType, FpcCmsConstants.RESEND_TYPE_LOG_ALARM)) {

      Date startTime = new Date(sendupRequest.getStartTime());
      Date finishTime = new Date(sendupRequest.getEndTime());
      Date endTime = new Date(startTime.getTime() + Constants.ONE_MINUTE_SECONDS * 1000);
      while (!GracefulShutdownHelper.isShutdownNow() && finishTime.after(startTime)) {

        // 统计该60秒钟日志告警上报数量和上报内容数量
        int collectAmount = 0;
        int entityAmount = 0;

        // 每隔5秒的日志告警数据组成一个Entity
        for (Entity entity : entityList) {
          if (startTime.getTime() <= entity.getStartTime()
              && entity.getStartTime() < endTime.getTime()) {
            collectAmount++;
            entityAmount = entityAmount + entity.getLogEntityCount() + entity.getAlarmEntityCount();
          }
        }

        // 保存统计记录
        mergeCollectMetric(deviceType, serialNumber, FpcCmsConstants.STATISTIC_TYPE_LOG_ALARM,
            collectAmount, entityAmount, startTime, endTime);

        // 构造下一个60秒
        startTime = endTime;
        endTime = new Date(startTime.getTime() + Constants.ONE_MINUTE_SECONDS * 1000);
      }

    }
  }

  /**
   * 将补报的统计数据重新汇总
   * @param metricStartTime
   * @param metricEndTime
   */
  private void reRollup(String deviceType, String serialNumber, Date metricStartTime,
      Date metricEndTime) {
    // 系统状态
    centralSystemDao.deleteCentralSystem(deviceType, serialNumber, metricStartTime,
        Constants.FIVE_MINUTE_SECONDS);
    centralSystemDao.rollupCentralSystem(metricStartTime, metricEndTime, deviceType, serialNumber);
    // 接口状态
    centralNetifDao.deleteCentralNetifs(deviceType, serialNumber, metricStartTime,
        Constants.FIVE_MINUTE_SECONDS);
    centralNetifDao.rollupCentralNetifs(metricStartTime, metricEndTime, deviceType, serialNumber);
  }

  /**
   * 从数据库获取上报数量以及内容数量进行累加操作
   * @param serialNumber
   * @param statisticType
   * @param collectAmount
   * @param entityAmount
   * @param startTime
   * @param endTime
   */
  private void mergeCollectMetric(String deviceType, String serialNumber, String statisticType,
      int collectAmount, int entityAmount, Date startTime, Date endTime) {

    // 从数据库获取统计记录
    CollectMetricDO collectMetricDO = collectMetricDao.queryCollectMetric(deviceType, serialNumber,
        statisticType, startTime);
    if (StringUtils.isNotBlank(collectMetricDO.getId())) {
      collectAmount = collectMetricDO.getCollectAmount() + collectAmount;
      entityAmount = collectMetricDO.getEntityAmount() + entityAmount;
    }

    // 构造统计记录
    collectMetricDO.setDeviceType(deviceType);
    collectMetricDO.setDeviceSerialNumber(serialNumber);
    collectMetricDO.setType(statisticType);
    collectMetricDO.setStartTime(startTime);
    collectMetricDO.setEndTime(endTime);
    collectMetricDO.setCollectAmount(collectAmount);
    collectMetricDO.setEntityAmount(entityAmount);

    // 保存或更新统计记录
    collectMetricDao.saveOrUpdateCollectMetric(collectMetricDO);

    LOGGER.debug("receive resend message collect metric is {}.", collectMetricDO);
  }

}
