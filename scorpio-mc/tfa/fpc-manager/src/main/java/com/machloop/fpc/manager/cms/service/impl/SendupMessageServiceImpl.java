package com.machloop.fpc.manager.cms.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.metric.system.data.MonitorRaid;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.alpha.webapp.system.data.AlarmDO;
import com.machloop.alpha.webapp.system.data.LogDO;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.alpha.webapp.system.vo.LogQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.AlarmEntity;
import com.machloop.fpc.cms.grpc.CentralProto.DiskMonitor;
import com.machloop.fpc.cms.grpc.CentralProto.Entity;
import com.machloop.fpc.cms.grpc.CentralProto.LogEntity;
import com.machloop.fpc.cms.grpc.CentralProto.MetricEntity;
import com.machloop.fpc.cms.grpc.CentralProto.NetifMetric;
import com.machloop.fpc.cms.grpc.CentralProto.NetworkEntity;
import com.machloop.fpc.cms.grpc.CentralProto.RaidMonitor;
import com.machloop.fpc.cms.grpc.CentralProto.SendupReply;
import com.machloop.fpc.cms.grpc.CentralProto.SendupRequest;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.RegistryHeartbeatService;
import com.machloop.fpc.manager.cms.service.SendupMessageService;
import com.machloop.fpc.manager.helper.GrpcClientHelper;
import com.machloop.fpc.manager.metric.service.MetricService;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.dao.DeviceDiskDao;
import com.machloop.fpc.manager.system.dao.MonitorMetricDao;
import com.machloop.fpc.manager.system.dao.SendupMessageDao;
import com.machloop.fpc.manager.system.data.DeviceDiskDO;
import com.machloop.fpc.manager.system.data.MonitorMetricDO;
import com.machloop.fpc.manager.system.data.SendupMessageDO;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.manager.system.service.DeviceRaidService;
import com.machloop.fpc.npm.appliance.dao.NetworkDao;
import com.machloop.fpc.npm.appliance.dao.NetworkNetifDao;
import com.machloop.fpc.npm.appliance.data.NetworkDO;
import com.machloop.fpc.npm.appliance.data.NetworkNetifDO;

import io.grpc.StatusRuntimeException;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author liyongjun
 *
 * create at 2019年12月4日, fpc-manager
 */
@Service
public class SendupMessageServiceImpl implements SendupMessageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendupMessageServiceImpl.class);

  @Autowired
  private GrpcClientHelper grpcClientHelper;

  @Autowired
  private RegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private SendupMessageDao sendupMessageDao;

  @Autowired
  private MonitorMetricDao monitorMetricDao;

  @Autowired
  private DeviceDiskDao deviceDiskDao;

  @Autowired
  private LogDao logDao;

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private NetworkDao networkDao;

  @Autowired
  private NetworkNetifDao networkNetifDao;

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private DeviceRaidService deviceRaidService;

  @Autowired
  private MetricService metricService;

  @Autowired
  private GlobalSettingService globalSetting;

  private long previousMetricEntityTime;

  private long previousLogAlarmEntityTime;

  private long previousNetworkEntityTime;

  private Date previousMetricNetifTime;

  /* private Date previousMetricSystemTime; */
  /* private Date previousMetricDiskIOTime; */

  /**
   * @see com.machloop.fpc.manager.cms.service.SendupMessageService#sendupMessage(java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public void sendupMessage(String serialNumber, String messageType, Date metricDatetime) {

    // 根据类型构造上报消息
    switch (messageType) {
      case FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC:
        sendupMetricEntity(serialNumber, metricDatetime);
        break;
      case FpcCmsConstants.SENDUP_TYPE_LOG_ALARM:
        sendupLogAlarmEntity(serialNumber, metricDatetime);
        break;
      case FpcCmsConstants.SENDUP_TYPE_NETWORK:
        sendupNetworkEntity(serialNumber, metricDatetime, false);
        break;
      default:
        LOGGER.warn("not support message type: {}", messageType);
        break;
    }

  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SendupMessageService#resendMessage(java.lang.String, java.lang.String)
   */
  @Override
  public void resendMessage(String serialNumber, String messageType, Date metricDatetime) {

    // 根据类型构造上报消息
    if (StringUtils.equals(messageType, FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC)) {
      resendMetricEntity(serialNumber, metricDatetime);
    } else if (StringUtils.equals(messageType, FpcCmsConstants.RESEND_TYPE_LOG_ALARM)) {
      resendLogAlarmEntity(serialNumber, metricDatetime);
    }
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SendupMessageService#sendAllApplianceMessage(java.lang.String, java.util.Date)
   */
  @Override
  public void sendAllApplianceMessage(String serialNumber, Date metricDatetime) {
    LOGGER.info("send all appliance message.");
    // 网络
    sendupNetworkEntity(serialNumber, metricDatetime, true);
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SendupMessageService#deleteExpireSendupMessage(java.util.Date)
   */
  @Override
  public void deleteExpireSendupMessage(Date expireTime) {
    int amount = sendupMessageDao.deleteExpireMessage(expireTime);
    LOGGER.debug("delete expire sendup entity, amount is {}.", amount);
  }

  /**
   * 上报系统状态
   * @param serialNumber
   * @return
   */
  private void sendupMetricEntity(String serialNumber, Date metricDatetime) {

    // 每30s查询一次
    if (metricDatetime.getTime() - previousMetricEntityTime < Constants.HALF_MINUTE_SECONDS
        * 1000) {
      return;
    }

    // 构造统计的时间段
    long multipleOfInterval = metricDatetime.getTime() / 1000 / Constants.ONE_MINUTE_SECONDS;
    Date endTime = new Date(multipleOfInterval * Constants.ONE_MINUTE_SECONDS * 1000);
    Date startTime = new Date(endTime.getTime() - Constants.ONE_MINUTE_SECONDS * 1000);
    previousMetricEntityTime = endTime.getTime();

    // 构造上报内容
    Entity entity = Entity.newBuilder().setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime())
        .addAllMetricEntity(Lists.newArrayList(generateMetricEntity(startTime, endTime))).build();

    // 构造上报消息
    String messageId = IdGenerator.generateUUID();
    String messageType = FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC;
    SendupRequest request = SendupRequest.newBuilder()
        .setDeviceType(FpcCmsConstants.DEVICE_TYPE_TFA).setSerialNumber(serialNumber)
        .setMessageId(messageId).setMessageType(messageType).setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).addAllEntity(Lists.newArrayList(entity))
        .setTimestamp(DateUtils.now().getTime()).build();

    // 发送消息并更新上报结果
    sendMessageAndUpdateResult(request, null);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("sendup system metric request, {}", request);
    }
  }

  /**
   * 上报日志告警
   * @param serialNumber
   * @return
   */
  private void sendupLogAlarmEntity(String serialNumber, Date metricDatetime) {

    // 整理metricDate为5秒的整倍数
    long multipleOfInterval = metricDatetime.getTime() / 1000 / Constants.FIVE_SECONDS;
    Date endTime = (new Date(multipleOfInterval * Constants.FIVE_SECONDS * 1000));
    Date startTime = new Date(endTime.getTime() - Constants.FIVE_SECONDS * 1000);

    // 未到上报时间
    if (previousLogAlarmEntityTime == endTime.getTime()) {
      return;
    }

    previousLogAlarmEntityTime = endTime.getTime();

    // 查询该段时间的日志
    String logOrderNum = globalSetting.getValue(ManagerConstants.GLOBAL_SETTING_SENDUP_LOG_CURSOR);
    LogQueryVO logQuery = new LogQueryVO();
    logQuery.setOrderNum(logOrderNum);
    logQuery.setCount("100");
    List<LogDO> logDOList = logDao.queryLogsWithoutPage(logQuery);

    // 构造上报日志
    List<LogEntity> logEntityList = Lists.newArrayListWithCapacity(logDOList.size());
    for (LogDO logDO : logDOList) {
      LogEntity logEntity = LogEntity.newBuilder().setId(logDO.getId()).setLevel(logDO.getLevel())
          .setCategory(logDO.getCategory())
          // SYMonitor组件在探针上的编号为001003，在cms上的编号为001004
          .setComponent(
              StringUtils.equals(logDO.getComponent(), "001003") ? "001004" : logDO.getComponent())
          .setAriseTime(logDO.getAriseTime().getTime()).setContent(logDO.getContent())
          .setSource(logDO.getSource()).setNodeId(serialNumber).build();

      logEntityList.add(logEntity);
    }

    // 查询该段时间的告警 引擎端只会写arise_time
    String alarmOrderNum = globalSetting
        .getValue(ManagerConstants.GLOBAL_SETTING_SENDUP_ALARM_CURSOR);
    AlarmQueryVO query = new AlarmQueryVO();
    query.setOrderNum(alarmOrderNum);
    query.setCount("100");
    List<AlarmDO> alarmDOList = alarmDao.queryAlarmsWithoutPage(query);

    // 构造上报告警
    List<AlarmEntity> alarmEntityList = Lists.newArrayListWithCapacity(alarmDOList.size());
    for (AlarmDO alarmDO : alarmDOList) {
      AlarmEntity alarmEntity = AlarmEntity.newBuilder().setId(alarmDO.getId())
          .setLevel(alarmDO.getLevel()).setCategory(alarmDO.getCategory())
          .setKeyword(alarmDO.getKeyword())
          // SYMonitor组件在探针上的编号为001003，在cms上的编号为001004
          .setComponent(StringUtils.equals(alarmDO.getComponent(), "001003") ? "001004"
              : alarmDO.getComponent())
          .setSolverId("").setStatus(Constants.BOOL_NO).setContent(alarmDO.getContent())
          .setReason(alarmDO.getReason()).setNodeId(serialNumber)
          .setAriseTime(alarmDO.getAriseTime().getTime()).setSolveTime(0L).build();

      alarmEntityList.add(alarmEntity);
    }

    // 构造上报内容
    Entity entity = Entity.newBuilder().setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).addAllLogEntity(logEntityList)
        .addAllAlarmEntity(alarmEntityList).build();

    // 构造上报消息
    String messageId = IdGenerator.generateUUID();
    String messageType = FpcCmsConstants.SENDUP_TYPE_LOG_ALARM;
    SendupRequest request = SendupRequest.newBuilder()
        .setDeviceType(FpcCmsConstants.DEVICE_TYPE_TFA).setSerialNumber(serialNumber)
        .setMessageId(messageId).setMessageType(messageType).setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).addAllEntity(Lists.newArrayList(entity))
        .setTimestamp(DateUtils.now().getTime()).build();

    // 发送消息并更新上报结果
    String result = sendMessageAndUpdateResult(request, null);
    if (StringUtils.equals(result, Constants.RES_OK)) {
      if (CollectionUtils.isNotEmpty(alarmDOList)) {
        globalSetting.setValue(ManagerConstants.GLOBAL_SETTING_SENDUP_ALARM_CURSOR,
            alarmDOList.get(alarmDOList.size() - 1).getOrderNum());
      }
      if (CollectionUtils.isNotEmpty(logDOList)) {
        globalSetting.setValue(ManagerConstants.GLOBAL_SETTING_SENDUP_LOG_CURSOR,
            logDOList.get(logDOList.size() - 1).getOrderNum());
      }
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("sendup log and alarm request, {}", request);
    }
  }

  /**
   * 上报网络信息
   * @param serialNumber
   * @return
   */
  private void sendupNetworkEntity(String serialNumber, Date metricDatetime,
      boolean fulVolumeUpdates) {

    // 整理metricDate为5秒的整倍数
    long multipleOfInterval = metricDatetime.getTime() / 1000 / Constants.FIVE_SECONDS;
    Date endTime = (new Date(multipleOfInterval * Constants.FIVE_SECONDS * 1000));
    Date startTime = new Date(endTime.getTime() - Constants.FIVE_SECONDS * 1000);

    // 未到上报时间
    if (previousNetworkEntityTime == endTime.getTime() && !fulVolumeUpdates) {
      return;
    }

    previousNetworkEntityTime = endTime.getTime();

    // 构造上报内容
    List<NetworkDO> networks = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (fulVolumeUpdates) {
      networks = networkDao.queryNetworks();
    } else {
      networks = networkDao.queryNetworksByReportState(Constants.BOOL_NO);
    }
    List<NetworkEntity> networkEntityList = networks.stream().map(network -> {
      int bandwidth = networkNetifDao.queryNetworkNetifs(network.getId()).stream()
          .mapToInt(NetworkNetifDO::getSpecification).sum();
      NetworkEntity networkEntity = NetworkEntity.newBuilder().setId(network.getId())
          .setName(network.getName()).setBandwidth(bandwidth).setFpcSerialNumber(serialNumber)
          .setAction(fulVolumeUpdates ? FpcCmsConstants.SYNC_ACTION_ADD : network.getReportAction())
          .build();

      return networkEntity;
    }).collect(Collectors.toList());

    Entity entity = Entity.newBuilder().setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).addAllNetworkEntity(networkEntityList).build();

    // 构造上报消息
    String messageId = IdGenerator.generateUUID();
    String messageType = FpcCmsConstants.SENDUP_TYPE_NETWORK;
    SendupRequest request = SendupRequest.newBuilder()
        .setDeviceType(FpcCmsConstants.DEVICE_TYPE_TFA).setSerialNumber(serialNumber)
        .setMessageId(messageId).setMessageType(messageType).setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).setAll(fulVolumeUpdates)
        .addAllEntity(Lists.newArrayList(entity)).setTimestamp(DateUtils.now().getTime()).build();

    // 发送消息并更新上报结果
    sendApplianceAndUpdateState(request);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("sendup networks request, {}", request);
    }
  }

  /**
   * 补报系统状态
   * @param serialNumber
   * @param responseObserver
   */
  private void resendMetricEntity(String serialNumber, Date metricDatetime) {

    // 构造一天前向后偏移五分钟的时间范围
    long multipleOfInterval = DateUtils.beforeDayDate(metricDatetime, Constants.ONE_DAYS).getTime()
        / 1000 / Constants.FIVE_MINUTE_SECONDS;
    Date endTime = (new Date(multipleOfInterval * Constants.FIVE_MINUTE_SECONDS * 1000));
    Date startTime = new Date(endTime.getTime() - Constants.FIVE_MINUTE_SECONDS * 1000);

    // 以五分钟为时间段向后偏移，一直偏移到当前时间
    while (!GracefulShutdownHelper.isShutdownNow() && metricDatetime.after(startTime)) {
      // 该五分钟没有上报失败的数据，跳过该五分钟
      List<SendupMessageDO> sendupMessageList = sendupMessageDao.querySendupMessages(
          FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC, Constants.RES_NOK, startTime, endTime);
      if (CollectionUtils.isEmpty(sendupMessageList)) {
        // 构造下一个五分钟的时间段
        startTime = endTime;
        endTime = new Date(endTime.getTime() + Constants.FIVE_MINUTE_SECONDS * 1000);
        continue;
      }

      // 构造该五分钟上报的消息
      List<String> messageIdList = Lists.newArrayListWithCapacity(sendupMessageList.size());
      List<Entity> entityList = Lists.newArrayListWithCapacity(sendupMessageList.size());
      for (SendupMessageDO sendupMessageDO : sendupMessageList) {

        // 构建上报消息
        try {
          SendupRequest sendupRequest = SendupRequest.parseFrom(Base64.getDecoder()
              .decode(sendupMessageDO.getContent().getBytes(StandardCharsets.UTF_8)));

          entityList.addAll(sendupRequest.getEntityList());

          messageIdList.add(sendupMessageDO.getMessageId());
        } catch (InvalidProtocolBufferException e) {
          LOGGER.warn("failed to system metric deserialization, sendup metric id is {}.",
              sendupMessageDO.getId(), e);
        } catch (NullPointerException e) {
          LOGGER.warn("failed to system metric deserialization, sendup metric id is {}.",
              sendupMessageDO.getId(), e);
        }
      }

      // 构造上报消息
      String messageId = IdGenerator.generateUUID();
      SendupRequest request = SendupRequest.newBuilder()
          .setDeviceType(FpcCmsConstants.DEVICE_TYPE_TFA).setSerialNumber(serialNumber)
          .setMessageId(messageId).setMessageType(FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC)
          .setStartTime(startTime.getTime()).setEndTime(endTime.getTime()).addAllEntity(entityList)
          .setTimestamp(DateUtils.now().getTime()).build();

      // 发送消息并更新上报结果
      sendMessageAndUpdateResult(request, messageIdList);

      // 没偏移到当前时间，构造下一个五分钟的时间段
      startTime = endTime;
      endTime = new Date(endTime.getTime() + Constants.FIVE_MINUTE_SECONDS * 1000);
    }
  }

  /**
   * 补报日志告警
   * @param serialNumber
   * @param responseObserver
   */
  private void resendLogAlarmEntity(String serialNumber, Date metricDatetime) {

    // 构造一天前向后偏移五分钟的时间范围
    long multipleOfInterval = (long) DateUtils.beforeDayDate(metricDatetime, Constants.ONE_DAYS)
        .getTime() / 1000 / Constants.FIVE_MINUTE_SECONDS;
    Date endTime = (new Date(multipleOfInterval * Constants.FIVE_MINUTE_SECONDS * 1000));
    Date startTime = new Date(endTime.getTime() - Constants.FIVE_MINUTE_SECONDS * 1000);

    // 以五分钟为时间段向后偏移，一直偏移到当前时间
    while (!GracefulShutdownHelper.isShutdownNow() && metricDatetime.after(startTime)) {
      // 该五分钟没有上报失败的数据，跳过该五分钟
      List<SendupMessageDO> sendupMessageList = sendupMessageDao.querySendupMessages(
          FpcCmsConstants.SENDUP_TYPE_LOG_ALARM, Constants.RES_NOK, startTime, endTime);
      if (CollectionUtils.isEmpty(sendupMessageList)) {
        // 构造下一个五分钟的时间段
        startTime = endTime;
        endTime = new Date(endTime.getTime() + Constants.FIVE_MINUTE_SECONDS * 1000);
        continue;
      }

      // 构造该五分钟上报失败的消息并发送
      List<Entity> entityList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      List<String> messageIdList = Lists.newArrayListWithCapacity(sendupMessageList.size());
      for (SendupMessageDO sendupMessageDO : sendupMessageList) {

        SendupRequest sendupRequest = null;
        try {
          sendupRequest = SendupRequest.parseFrom(Base64.getDecoder()
              .decode(sendupMessageDO.getContent().getBytes(StandardCharsets.UTF_8)));
          entityList.addAll(sendupRequest.getEntityList());

          messageIdList.add(sendupRequest.getMessageId());

        } catch (InvalidProtocolBufferException e) {
          LOGGER.warn("failed to log metric deserialization, sendup metric id is {}.",
              sendupMessageDO.getId(), e);
        } catch (NullPointerException e) {
          LOGGER.warn("failed to log metric deserialization, sendup metric id is {}.",
              sendupMessageDO.getId(), e);
        }

      }

      // 构造上报消息
      String messageId = IdGenerator.generateUUID();
      SendupRequest request = SendupRequest.newBuilder()
          .setDeviceType(FpcCmsConstants.DEVICE_TYPE_TFA).setSerialNumber(serialNumber)
          .setMessageId(messageId).setMessageType(FpcCmsConstants.RESEND_TYPE_LOG_ALARM)
          .setStartTime(startTime.getTime()).setEndTime(endTime.getTime()).addAllEntity(entityList)
          .setTimestamp(DateUtils.now().getTime()).build();

      // 发送消息并更新上报结果
      sendMessageAndUpdateResult(request, messageIdList);

      // 没偏移到当前时间，构造下一个五分钟的时间段
      startTime = endTime;
      endTime = new Date(endTime.getTime() + Constants.FIVE_MINUTE_SECONDS * 1000);
    }
  }

  /**
   * 采集系统状态，网卡流量，RAID信息，硬盘信息
   * @return
   */
  private MetricEntity generateMetricEntity(Date startTime, Date endTime) {
    // 系统资源使用统计（CPU、内存、分区使用率）
    // 2022-04-29之后直接通过clickhouse集群读取，不再上报
    /*
     * previousMetricSystemTime = previousMetricSystemTime == null ? startTime :
     * previousMetricSystemTime; List<MonitorMetricDataDO> systemMonitorMetrics =
     * monitorMetricDataDao .queryMonitorMetricData(previousMetricSystemTime); List<SystemMetric>
     * systemMetrics = systemMonitorMetrics.stream().map(systemMonitorMetric -> { return
     * SystemMetric.newBuilder().setCpuMetric(systemMonitorMetric.getCpuUsedRatio())
     * .setMemoryMetric(systemMonitorMetric.getMemoryUsedRatio())
     * .setSystemFsMetric(systemMonitorMetric.getSystemFsUsedRatio())
     * .setIndexFsMetric(systemMonitorMetric.getIndexFsUsedRatio())
     * .setMetadataFsMetric(systemMonitorMetric.getMetadataFsUsedRatio())
     * .setMetadataHotFsMetric(systemMonitorMetric.getMetadataHotFsUsedRatio())
     * .setPacketFsMetric(systemMonitorMetric.getPacketFsUsedRatio())
     * .setMetricTime(systemMonitorMetric.getTimestamp().getTime()).build();
     * }).collect(Collectors.toList());
     * 
     * previousMetricSystemTime = CollectionUtils.isNotEmpty(systemMonitorMetrics) ?
     * systemMonitorMetrics.get(0).getTimestamp() : previousMetricSystemTime;
     */

    // 管理口、业务口流量
    Map<String,
        Tuple3<String, String, Integer>> netifConfigs = deviceNetifService.queryDeviceNetifs()
            .stream()
            .collect(Collectors.toMap(DeviceNetifBO::getName,
                deviceNetif -> Tuples.of(deviceNetif.getCategory(), deviceNetif.getState(),
                    deviceNetif.getSpecification())));

    previousMetricNetifTime = previousMetricNetifTime == null ? startTime : previousMetricNetifTime;
    List<Map<String, Object>> netifMetrics = metricService
        .queryMetricNetifRawdatas(previousMetricNetifTime);
    List<NetifMetric> netifMetricList = netifMetrics.stream()
        .filter(oneItem -> netifConfigs.containsKey(MapUtils.getString(oneItem, "netifName")))
        .map(oneItem -> {
          String netifName = MapUtils.getString(oneItem, "netifName");
          Tuple3<String, String, Integer> netifConfig = netifConfigs.get(netifName);
          return NetifMetric.newBuilder().setNetifName(netifName).setCategory(netifConfig.getT1())
              .setState(netifConfig.getT2()).setSpecification(netifConfig.getT3())
              .setRxBps(
                  MapUtils.getLongValue(oneItem, "totalBytes", 0) / Constants.ONE_MINUTE_SECONDS)
              .setRxPps(
                  MapUtils.getLongValue(oneItem, "totalPackets", 0) / Constants.ONE_MINUTE_SECONDS)
              .setTxBps(
                  MapUtils.getLongValue(oneItem, "transmitBytes", 0) / Constants.ONE_MINUTE_SECONDS)
              .setTxPps(MapUtils.getLongValue(oneItem, "transmitPackets", 0)
                  / Constants.ONE_MINUTE_SECONDS)
              .setMetricTime(((Date) oneItem.get("timestamp")).getTime()).build();
        }).collect(Collectors.toList());

    previousMetricNetifTime = CollectionUtils.isNotEmpty(netifMetrics)
        ? (Date) netifMetrics.get(0).get("timestamp")
        : previousMetricNetifTime;

    // 构造上报的raid信息
    List<MonitorRaid> monitorRaidList = deviceRaidService.monitorRaidState();
    List<RaidMonitor> raidMonitorList = monitorRaidList.stream().map(monitorRaid -> {
      return RaidMonitor.newBuilder().setRaidNo(monitorRaid.getRaidNo())
          .setState(monitorRaid.getState()).setRaidLevel(monitorRaid.getRaidLevel()).build();
    }).collect(Collectors.toList());

    // 构造上报的硬盘信息
    List<DeviceDiskDO> deviceDiskList = deviceDiskDao.queryDeviceDisks();
    List<DiskMonitor> diskMonitorList = deviceDiskList.stream().map(disk -> {
      return DiskMonitor.newBuilder().setRaidNo(disk.getRaidNo()).setRaidLevel(disk.getRaidLevel())
          .setPhysicalLocation(StringUtils.defaultIfBlank(disk.getPhysicalLocation(), ""))
          .setSlotNo(disk.getSlotNo()).setMedium(disk.getMedium()).setCapacity(disk.getCapacity())
          .setState(disk.getState())
          .setForeignState(StringUtils.defaultIfBlank(disk.getForeignState(), ""))
          .setRebuildProgress(disk.getRebuildProgress())
          .setCopybackProgress(disk.getCopybackProgress()).build();
    }).collect(Collectors.toList());

    // 硬盘IO速率
    // 2022-04-29之后直接通过clickhouse集群读取，不再上报
    /*
     * previousMetricDiskIOTime = previousMetricDiskIOTime == null ? startTime :
     * previousMetricDiskIOTime; List<MetricDiskIODO> metricDiskIOHistograms = metricDiskIODao
     * .queryMetricDiskIOs(previousMetricDiskIOTime); List<DiskIOMetric> diskIOMetrics =
     * metricDiskIOHistograms.stream().map(diskIO -> { DiskIOMetric diskIOMetric =
     * DiskIOMetric.newBuilder()
     * .setPartitionName(diskIO.getPartitionName()).setReadByteps(diskIO.getReadByteps())
     * .setReadBytepsPeak(diskIO.getReadBytepsPeak()).setWriteByteps(diskIO.getWriteByteps())
     * .setWriteBytepsPeak(diskIO.getWriteBytepsPeak())
     * .setMetricTime(diskIO.getTimestamp().getTime()).build();
     * 
     * return diskIOMetric; }).collect(Collectors.toList());
     * 
     * previousMetricDiskIOTime = CollectionUtils.isNotEmpty(metricDiskIOHistograms) ?
     * metricDiskIOHistograms.get(0).getTimestamp() : previousMetricDiskIOTime;
     */

    // 系统指标相关信息(从上到下：数据总存储空间大小、数据存储空间使用率、缓存总空间大小、缓存空间使用率、数据最早存储时间、最近24小时存储总字节数、剩余空间可存储天数、pcap/pcapng缓存文件的平均大小、全包存储空间总大小)
    long fsDataTotalByte = 0L;
    int fsDataUsedPct = 0;
    long fsCacheTotalByte = 0L;
    int fsCacheUsedPct = 0;
    long dataOldestTime = 0L;
    long dataLast24TotalByte = 0L;
    long dataPredictTotalDay = 0;
    long cacheFileAvgByte = 0L;
    long fsStoreTotalByte = 0L;
    long fsSystemTotalByte = 0L;
    long fsIndexTotalByte = 0L;
    long fsMetadataTotalByte = 0L;
    long fsMetadataHotTotalByte = 0L;
    List<MonitorMetricDO> monitorMetricDOList = monitorMetricDao.queryMonitorMetrics();
    for (MonitorMetricDO mnitorMetricDO : monitorMetricDOList) {
      switch (mnitorMetricDO.getMetricName()) {
        case FpcConstants.MONITOR_METRIC_FS_DATA_TOTAL:
          fsDataTotalByte = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_FS_DATA_USED_PCT:
          fsDataUsedPct = Integer
              .parseInt(StringUtils.substringBefore(mnitorMetricDO.getMetricValue(), "%"));
          break;
        case FpcConstants.MONITOR_METRIC_FS_CACHE_TOTAL:
          fsCacheTotalByte = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_FS_CACHE_USED_PCT:
          fsCacheUsedPct = Integer
              .parseInt(StringUtils.substringBefore(mnitorMetricDO.getMetricValue(), "%"));
          break;
        case FpcConstants.MONITOR_METRIC_DATA_OLDEST_TIMESTAMP:
          dataOldestTime = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_DATA_LAST24_TOTAL_BYTE:
          dataLast24TotalByte = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_DATA_PREDICT_TOTAL_DAY:
          dataPredictTotalDay = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_CACHE_FILE_AVG_BYTE:
          cacheFileAvgByte = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_FS_STORE_TOTAL:
          fsStoreTotalByte = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_FS_SYSTEM_TOTAL:
          fsSystemTotalByte = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_FS_INDEX_TOTAL:
          fsIndexTotalByte = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_FS_METADATA_TOTAL:
          fsMetadataTotalByte = Long.parseLong(mnitorMetricDO.getMetricValue());
          break;
        case FpcConstants.MONITOR_METRIC_FS_METADATA_HOT_TOTAL:
          fsMetadataHotTotalByte = Long.parseLong(mnitorMetricDO.getMetricValue());
        default:
          break;
      }
    }

    MetricEntity metricEntity = MetricEntity.newBuilder().setFsDataTotalByte(fsDataTotalByte)
        .setFsDataUsedPct(fsDataUsedPct).setFsCacheTotalByte(fsCacheTotalByte)
        .setFsCacheUsedPct(fsCacheUsedPct).setDataOldestTime(dataOldestTime)
        .setDataLast24TotalByte(dataLast24TotalByte).setDataPredictTotalDay(dataPredictTotalDay)
        .setCacheFileAvgByte(cacheFileAvgByte).setFsStoreTotalByte(fsStoreTotalByte)
        .setFsSystemTotalByte(fsSystemTotalByte).setFsIndexTotalByte(fsIndexTotalByte)
        .setFsMetadataTotalByte(fsMetadataTotalByte)
        .setFsMetadataHotTotalByte(fsMetadataHotTotalByte).setFsPacketTotalByte(fsStoreTotalByte)
        .addAllSystemMetric(Lists.newArrayListWithCapacity(0)).addAllNetifMetric(netifMetricList)
        .addAllRaidMonitor(raidMonitorList).addAllDiskMonitor(diskMonitorList)
        .addAllDiskIoMetric(Lists.newArrayListWithCapacity(0)).setMetricTime(endTime.getTime())
        .build();

    return metricEntity;
  }

  /**
   * 发送消息并更新结果(系统状态，日志告警)
   * @param request
   */
  private String sendMessageAndUpdateResult(SendupRequest request, List<String> messageIdList) {

    // 发送消息并获取结果
    String result = Constants.RES_NOK;
    if (registryHeartbeatService.isAlive()) {
      try {
        SendupReply sendupReply = grpcClientHelper.getGrpcServerBlockingStub()
            .sendupChannel(request);
        result = sendupReply.getCode() == FpcCmsConstants.RESULT_SUCCESS_CODE ? Constants.RES_OK
            : Constants.RES_NOK;
      } catch (StatusRuntimeException e) {
        // 发送失败将该上报结果更新为失败
        result = Constants.RES_NOK;
      }
    }

    // 将上报结果保存到数据库
    if (messageIdList == null) {
      // 发送失败将消息记录到数据库中，等待下次补报
      if (StringUtils.equals(result, Constants.RES_NOK)) {
        // 构造补报信息
        try {
          SendupMessageDO sendupMessage = new SendupMessageDO();
          sendupMessage.setMessageId(request.getMessageId());
          sendupMessage.setType(request.getMessageType());
          sendupMessage.setStartTime(new Date(request.getStartTime()));
          sendupMessage.setEndTime(new Date(request.getEndTime()));
          sendupMessage.setResult(result);
          sendupMessage.setContent(new String(Base64.getEncoder().encode(request.toByteArray()),
              StandardCharsets.UTF_8));

          // 将上报消息存入数据库
          sendupMessageDao.saveSendupMessage(sendupMessage);

          return result;
        } catch (NullPointerException e) {
          LOGGER.warn("failed to system metric sendup content encode, sendup metric request is {}.",
              request, e);
        }
      }
    } else {
      sendupMessageDao.updateSendupMessageResults(messageIdList, result);
      return result;
    }
    return result;
  }

  /**
   * 发送配置并更新状态(网络)
   * @param request
   */
  private void sendApplianceAndUpdateState(SendupRequest request) {

    // 发送消息并获取结果
    if (registryHeartbeatService.isAlive()) {
      try {
        SendupReply sendupReply = grpcClientHelper.getGrpcServerBlockingStub()
            .sendupChannel(request);
        if (sendupReply.getCode() == FpcCmsConstants.RESULT_SUCCESS_CODE) {
          // 网络不为空时更新网络上报结果
          List<NetworkEntity> networkEntityList = request.getEntity(0).getNetworkEntityList();
          if (CollectionUtils.isNotEmpty(networkEntityList)) {
            networkDao.updateNetworkReportState(
                networkEntityList.stream().map(item -> item.getId()).collect(Collectors.toList()),
                Constants.BOOL_YES);
          }
        }
      } catch (StatusRuntimeException e) {
        // 发送失败将该上报结果更新为失败
        LOGGER.warn("send up appliance [{}] failed.", request.getMessageType(), e);
      }
    }
  }

}
