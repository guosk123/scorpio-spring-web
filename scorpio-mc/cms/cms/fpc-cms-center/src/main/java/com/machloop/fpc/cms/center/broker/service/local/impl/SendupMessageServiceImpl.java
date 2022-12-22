package com.machloop.fpc.cms.center.broker.service.local.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.protobuf.InvalidProtocolBufferException;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.alpha.webapp.system.data.AlarmDO;
import com.machloop.alpha.webapp.system.data.LogDO;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.alpha.webapp.system.vo.LogQueryVO;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao;
import com.machloop.fpc.cms.center.broker.dao.SendupMessageDao;
import com.machloop.fpc.cms.center.broker.data.FpcNetworkDO;
import com.machloop.fpc.cms.center.broker.data.SendupMessageDO;
import com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService;
import com.machloop.fpc.cms.center.broker.service.local.SendupMessageService;
import com.machloop.fpc.cms.center.central.dao.CentralNetifDao;
import com.machloop.fpc.cms.center.central.dao.CentralSystemDao;
import com.machloop.fpc.cms.center.central.dao.CmsDao;
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.data.CentralNetifDO;
import com.machloop.fpc.cms.center.central.data.CentralSystemDO;
import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.data.FpcDO;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;
import com.machloop.fpc.cms.center.helper.GrpcClientHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.AlarmEntity;
import com.machloop.fpc.cms.grpc.CentralProto.CmsEntity;
import com.machloop.fpc.cms.grpc.CentralProto.Entity;
import com.machloop.fpc.cms.grpc.CentralProto.LogEntity;
import com.machloop.fpc.cms.grpc.CentralProto.MetricEntity;
import com.machloop.fpc.cms.grpc.CentralProto.NetifMetric;
import com.machloop.fpc.cms.grpc.CentralProto.NetworkEntity;
import com.machloop.fpc.cms.grpc.CentralProto.SendupReply;
import com.machloop.fpc.cms.grpc.CentralProto.SendupRequest;
import com.machloop.fpc.cms.grpc.CentralProto.SensorEntity;
import com.machloop.fpc.cms.grpc.CentralProto.SystemMetric;

import io.grpc.StatusRuntimeException;

/**
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
@Service
public class SendupMessageServiceImpl implements SendupMessageService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendupMessageServiceImpl.class);

  @Autowired
  private GlobalSettingService globalSetting;

  @Autowired
  private GrpcClientHelper grpcClientHelper;

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private CmsDao cmsDao;

  @Autowired
  private LogDao logDao;

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private FpcNetworkDao networkDao;

  @Autowired
  private CentralNetifDao centralNetifDao;

  @Autowired
  private CentralSystemDao centralSystemDao;

  @Autowired
  private SendupMessageDao sendupMessageDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private LocalRegistryHeartbeatService registryHeartbeatService;

  private long previousMetricEntityTime;

  private long previousLogAlarmEntityTime;

  private long previousNetworkEntityTime;

  private long previousSensorEntityTime;

  private long previousCmsEntityTime;

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SendupMessageService#sendupMessage(java.lang.String, java.lang.String, java.lang.String, java.util.Date, int, int)
   */
  @Override
  public void sendupMessage(String deviceType, String serialNumber, String messageType,
      Date metricDatetime, int interval, int latency) {

    // 根据类型构造上报消息
    switch (messageType) {
      case FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC:
        sendupMetricEntity(deviceType, serialNumber, metricDatetime, interval, latency);
        break;
      case FpcCmsConstants.SENDUP_TYPE_LOG_ALARM:
        sendupLogAlarmEntity(deviceType, serialNumber, metricDatetime, interval, latency);
        break;
      case FpcCmsConstants.SENDUP_TYPE_NETWORK:
        sendupNetworkEntity(deviceType, serialNumber, metricDatetime, false, interval, latency);
        break;
      case FpcCmsConstants.SENDUP_TYPE_SENSOR:
        sendupSensorEntity(deviceType, serialNumber, metricDatetime, false, interval, latency);
        break;
      case FpcCmsConstants.SENDUP_TYPE_CMS:
        sendupCmsEntity(deviceType, serialNumber, metricDatetime, false, interval, latency);
        break;
      default:
        LOGGER.warn("not support message type: {}", messageType);
        break;
    }

  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SendupMessageService#resendMessage(java.lang.String, java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public void resendMessage(String deviceType, String serialNumber, String messageType,
      Date metricDatetime) {

    // 根据类型构造上报消息
    if (StringUtils.equals(messageType, FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC)) {
      resendMetricEntity(deviceType, serialNumber, metricDatetime);
    } else if (StringUtils.equals(messageType, FpcCmsConstants.RESEND_TYPE_LOG_ALARM)) {
      resendLogAlarmEntity(deviceType, serialNumber, metricDatetime);
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SendupMessageService#sendupMessage(com.machloop.fpc.cms.grpc.CentralProto.SendupRequest)
   */
  @Override
  public void sendupMessage(SendupRequest request) {
    // 是否存在上级
    if (StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_STATE, false))) {
      LOGGER.debug("cms swtich is off.");
      return;
    }

    // 若parentCmsIp为空将不进行心跳
    String parentCmsIp = registryHeartbeatService.getParentCmsIp();
    if (StringUtils.isBlank(parentCmsIp)) {
      LOGGER.debug("parentCmsIp is empty, end sendup msg.");
      return;
    }

    sendMessageAndUpdateResult(request, null);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SendupMessageService#sendAllApplianceMessage(java.lang.String, java.util.Date)
   */
  @Override
  public void sendAllApplianceMessage(String serialNumber, Date metricDatetime) {
    // cms列表
    sendupCmsEntity(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber, metricDatetime, true,
        Constants.FIVE_SECONDS, 0);
    // 探针列表
    sendupSensorEntity(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber, metricDatetime, true,
        Constants.FIVE_SECONDS, 0);
    // 网络
    sendupNetworkEntity(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber, metricDatetime, true,
        Constants.FIVE_SECONDS, 0);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SendupMessageService#deleteExpireSendupMessage(java.util.Date)
   */
  @Override
  public void deleteExpireSendupMessage(Date expireTime) {
    int amount = sendupMessageDao.deleteExpireMessage(expireTime);
    LOGGER.debug("delete expire sendup entity, amount is {}.", amount);
  }

  /**
   * 上报系统状态
   * @param deviceType
   * @param serialNumber
   * @param metricDatetime
   */
  private void sendupMetricEntity(String deviceType, String serialNumber, Date metricDatetime,
      int interval, int latency) {

    // 向后偏移30秒，避免数据还未写入
    if (metricDatetime.getTime() - previousMetricEntityTime < (interval + latency) * 1000) {
      return;
    }

    // 构造统计的时间段(数据间隔：1min)
    long multipleOfInterval = metricDatetime.getTime() / 1000 / interval;
    Date endTime = new Date(multipleOfInterval * interval * 1000);
    Date startTime = new Date(endTime.getTime() - interval * 1000);
    previousMetricEntityTime = endTime.getTime();

    // 构造上报内容
    MetricEntity metricEntity = generateMetricEntity(deviceType, serialNumber, startTime, endTime);
    if (metricEntity == null) {
      LOGGER.info(
          "can't found device system metric, maybe not yet reported. deviceType: {}, serialNumber: {}, time: {}",
          deviceType, serialNumber, StringUtils.joinWith("-", DateUtils.toStringISO8601(startTime),
              DateUtils.toStringISO8601(endTime)));
      return;
    }

    // 构造上报消息
    String messageId = IdGenerator.generateUUID();
    String messageType = FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC;

    Entity entity = Entity.newBuilder().setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).addAllMetricEntity(Lists.newArrayList(metricEntity)).build();

    SendupRequest request = SendupRequest.newBuilder().setMessageId(messageId)
        .setMessageType(messageType).setDeviceType(deviceType).setSerialNumber(serialNumber)
        .setStartTime(startTime.getTime()).setEndTime(endTime.getTime())
        .addAllEntity(Lists.newArrayList(entity)).setTimestamp(DateUtils.now().getTime()).build();

    // 发送消息并更新上报结果
    sendMessageAndUpdateResult(request, null);

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("sendup system metric request, {}", request);
    }
  }

  /**
   * 上报日志告警
   * @param deviceType
   * @param serialNumber
   * @param metricDatetime
   */
  private void sendupLogAlarmEntity(String deviceType, String serialNumber, Date metricDatetime,
      int interval, int latency) {

    // 延时读取数据,避免还未上报
    if (metricDatetime.getTime() - previousLogAlarmEntityTime < (interval + latency) * 1000) {
      return;
    }

    // 整理metricDate为5秒的整倍数
    long multipleOfInterval = metricDatetime.getTime() / 1000 / interval;
    Date endTime = (new Date(multipleOfInterval * interval * 1000));
    Date startTime = new Date(endTime.getTime() - interval * 1000);
    previousLogAlarmEntityTime = endTime.getTime();

    // 查询该段时间的日志
    String logOrderNum = globalSetting.getValue(CenterConstants.GLOBAL_SETTING_SENDUP_LOG_CURSOR);
    LogQueryVO logQuery = new LogQueryVO();
    logQuery.setOrderNum(logOrderNum);
    logQuery.setCount("100");
    logQuery.setNodeId(serialNumber);
    List<LogDO> logDOList = logDao.queryLogsWithoutPage(logQuery);

    // 构造上报日志
    List<LogEntity> logEntityList = Lists.newArrayListWithCapacity(logDOList.size());
    for (LogDO logDO : logDOList) {
      LogEntity logEntity = LogEntity.newBuilder().setId(logDO.getId()).setLevel(logDO.getLevel())
          .setCategory(logDO.getCategory()).setComponent(logDO.getComponent())
          .setAriseTime(logDO.getAriseTime().getTime()).setContent(logDO.getContent())
          .setSource(logDO.getSource()).setNodeId(logDO.getNodeId()).build();

      logEntityList.add(logEntity);
    }

    // 查询该段时间的告警
    String alarmOrderNum = globalSetting
        .getValue(CenterConstants.GLOBAL_SETTING_SENDUP_ALARM_CURSOR);
    AlarmQueryVO alarmQuery = new AlarmQueryVO();
    alarmQuery.setNodeId(serialNumber);
    alarmQuery.setOrderNum(alarmOrderNum);
    alarmQuery.setCount("100");
    List<AlarmDO> alarmDOList = alarmDao.queryAlarmsWithoutPage(alarmQuery);

    // 构造上报告警
    List<AlarmEntity> alarmEntityList = Lists.newArrayListWithCapacity(alarmDOList.size());
    for (AlarmDO alarmDO : alarmDOList) {
      AlarmEntity alarmEntity = AlarmEntity.newBuilder().setId(alarmDO.getId())
          .setLevel(alarmDO.getLevel()).setCategory(alarmDO.getCategory())
          .setKeyword(alarmDO.getKeyword()).setComponent(alarmDO.getComponent()).setSolverId("")
          .setStatus(Constants.BOOL_NO).setContent(alarmDO.getContent())
          .setReason(alarmDO.getReason()).setNodeId(alarmDO.getNodeId())
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
    SendupRequest request = SendupRequest.newBuilder().setMessageId(messageId)
        .setMessageType(messageType).setDeviceType(deviceType).setSerialNumber(serialNumber)
        .setStartTime(startTime.getTime()).setEndTime(endTime.getTime())
        .addAllEntity(Lists.newArrayList(entity)).setTimestamp(DateUtils.now().getTime()).build();

    // 发送消息并更新上报结果
    String result = sendMessageAndUpdateResult(request, null);
    if (StringUtils.equals(result, Constants.RES_OK)) {
      if (CollectionUtils.isNotEmpty(alarmDOList)) {
        globalSetting.setValue(CenterConstants.GLOBAL_SETTING_SENDUP_ALARM_CURSOR,
            alarmDOList.get(alarmDOList.size() - 1).getOrderNum());
      }
      if (CollectionUtils.isNotEmpty(logDOList)) {
        globalSetting.setValue(CenterConstants.GLOBAL_SETTING_SENDUP_LOG_CURSOR,
            logDOList.get(logDOList.size() - 1).getOrderNum());
      }
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("sendup log and alarm request, {}", request);
    }
  }

  /**
   * 上报网络信息
   * @param deviceType
   * @param serialNumber
   * @param metricDatetime
   * @param fulVolumeUpdates
   */
  private void sendupNetworkEntity(String deviceType, String serialNumber, Date metricDatetime,
      boolean fulVolumeUpdates, int interval, int latency) {

    // 整理metricDate为5秒的整倍数
    long multipleOfInterval = metricDatetime.getTime() / 1000 / interval;
    Date endTime = (new Date(multipleOfInterval * interval * 1000));
    Date startTime = new Date(endTime.getTime() - interval * 1000);

    // 未到上报时间
    if (previousNetworkEntityTime == endTime.getTime() && !fulVolumeUpdates) {
      return;
    }

    previousNetworkEntityTime = endTime.getTime();

    // 构造上报内容
    List<FpcNetworkDO> networks = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (fulVolumeUpdates) {
      networks = networkDao.queryFpcNetworks(null);
    } else {
      networks = networkDao.queryFpcNetworksByReportState(Constants.BOOL_NO);
    }
    List<NetworkEntity> networkEntityList = networks.stream().map(network -> {
      NetworkEntity networkEntity = NetworkEntity.newBuilder().setId(network.getFpcNetworkId())
          .setName(network.getFpcNetworkName()).setBandwidth(network.getBandwidth())
          .setFpcSerialNumber(network.getFpcSerialNumber())
          .setAction(fulVolumeUpdates ? FpcCmsConstants.SYNC_ACTION_ADD : network.getReportAction())
          .build();

      return networkEntity;
    }).collect(Collectors.toList());

    Entity entity = Entity.newBuilder().setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).addAllNetworkEntity(networkEntityList).build();

    // 构造上报消息
    String messageId = IdGenerator.generateUUID();
    String messageType = FpcCmsConstants.SENDUP_TYPE_NETWORK;
    SendupRequest request = SendupRequest.newBuilder().setMessageId(messageId)
        .setMessageType(messageType).setDeviceType(deviceType).setSerialNumber(serialNumber)
        .setStartTime(startTime.getTime()).setEndTime(endTime.getTime()).setAll(fulVolumeUpdates)
        .addAllEntity(Lists.newArrayList(entity)).setTimestamp(DateUtils.now().getTime()).build();

    // 发送消息并更新上报结果
    sendApplianceAndUpdateState(request);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("sendup networks request, {}", request);
    }
  }

  /**
   * 上报探针信息
   * @param deviceType
   * @param serialNumber
   * @param metricDatetime
   * @param fulVolumeUpdates
   */
  private void sendupSensorEntity(String deviceType, String serialNumber, Date metricDatetime,
      boolean fulVolumeUpdates, int interval, int latency) {

    // 整理metricDate为5秒的整倍数
    long multipleOfInterval = metricDatetime.getTime() / 1000 / interval;
    Date endTime = (new Date(multipleOfInterval * interval * 1000));
    Date startTime = new Date(endTime.getTime() - interval * 1000);

    // 未到上报时间
    if (previousSensorEntityTime == endTime.getTime() && !fulVolumeUpdates) {
      return;
    }

    previousSensorEntityTime = endTime.getTime();

    // 构造上报内容
    List<FpcDO> fpcList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (fulVolumeUpdates) {
      fpcList = fpcDao.queryFpcs(new FpcQueryVO());
    } else {
      fpcList = fpcDao.queryFpcByReportState(Constants.BOOL_NO);
    }
    List<SensorEntity> sensorEntityList = fpcList.stream().map(fpc -> {
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpc.getAppToken());
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpc.getCmsToken());

      SensorEntity sensorEntity = SensorEntity.newBuilder().setName(fpc.getName())
          .setIp(fpc.getIp()).setSerialNumber(fpc.getSerialNumber()).setVersion(fpc.getVersion())
          .setType(fpc.getType()).setAppKey(fpc.getAppKey()).setAppToken(appToken)
          .setCmsToken(cmsToken).setCmsSerialNumber(fpc.getCmsSerialNumber())
          .setAction(fulVolumeUpdates ? FpcCmsConstants.SYNC_ACTION_ADD : fpc.getReportAction())
          .build();

      return sensorEntity;
    }).collect(Collectors.toList());

    Entity entity = Entity.newBuilder().setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).addAllSensorEntity(sensorEntityList).build();

    // 构造上报消息
    String messageId = IdGenerator.generateUUID();
    String messageType = FpcCmsConstants.SENDUP_TYPE_SENSOR;
    SendupRequest request = SendupRequest.newBuilder().setMessageId(messageId)
        .setMessageType(messageType).setDeviceType(deviceType).setSerialNumber(serialNumber)
        .setStartTime(startTime.getTime()).setEndTime(endTime.getTime()).setAll(fulVolumeUpdates)
        .addAllEntity(Lists.newArrayList(entity)).setTimestamp(DateUtils.now().getTime()).build();

    // 发送消息并更新上报结果
    sendApplianceAndUpdateState(request);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("sendup sensor request, {}", request);
    }
  }

  /**
   * 上报本机管理的CMS信息
   * @param deviceType
   * @param serialNumber
   * @param metricDatetime
   * @param fulVolumeUpdates
   */
  private void sendupCmsEntity(String deviceType, String serialNumber, Date metricDatetime,
      boolean fulVolumeUpdates, int interval, int latency) {

    // 整理metricDate为5秒的整倍数
    long multipleOfInterval = metricDatetime.getTime() / 1000 / interval;
    Date endTime = (new Date(multipleOfInterval * interval * 1000));
    Date startTime = new Date(endTime.getTime() - interval * 1000);

    // 未到上报时间
    if (previousCmsEntityTime == endTime.getTime() && !fulVolumeUpdates) {
      return;
    }

    previousCmsEntityTime = endTime.getTime();

    // 构造上报内容
    List<CmsDO> cmsList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (fulVolumeUpdates) {
      cmsList = cmsDao.queryCms(new CmsQueryVO());
    } else {
      cmsList = cmsDao.queryCmsByReportState(Constants.BOOL_NO);
    }
    List<CmsEntity> cmsEntityList = cmsList.stream().map(cms -> {
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cms.getAppToken());
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cms.getCmsToken());

      CmsEntity cmsEntity = CmsEntity.newBuilder().setName(cms.getName()).setIp(cms.getIp())
          .setSerialNumber(cms.getSerialNumber()).setVersion(cms.getVersion())
          .setAppKey(cms.getAppKey()).setAppToken(appToken).setCmsToken(cmsToken)
          .setSuperiorCmsSerialNumber(cms.getSuperiorCmsSerialNumber())
          .setAction(fulVolumeUpdates ? FpcCmsConstants.SYNC_ACTION_ADD : cms.getReportAction())
          .build();

      return cmsEntity;
    }).collect(Collectors.toList());

    Entity entity = Entity.newBuilder().setStartTime(startTime.getTime())
        .setEndTime(endTime.getTime()).addAllCmsEntity(cmsEntityList).build();

    // 构造上报消息
    String messageId = IdGenerator.generateUUID();
    String messageType = FpcCmsConstants.SENDUP_TYPE_CMS;
    SendupRequest request = SendupRequest.newBuilder().setMessageId(messageId)
        .setMessageType(messageType).setDeviceType(deviceType).setSerialNumber(serialNumber)
        .setStartTime(startTime.getTime()).setEndTime(endTime.getTime()).setAll(fulVolumeUpdates)
        .addAllEntity(Lists.newArrayList(entity)).setTimestamp(DateUtils.now().getTime()).build();

    // 发送消息并更新上报结果
    sendApplianceAndUpdateState(request);
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("sendup cms request, {}", request);
    }
  }

  /**
   * 补报系统状态
   * @param deviceType
   * @param serialNumber
   * @param metricDatetime
   */
  private void resendMetricEntity(String deviceType, String serialNumber, Date metricDatetime) {

    // 构造一天前向后偏移五分钟的时间范围
    long multipleOfInterval = DateUtils.beforeDayDate(metricDatetime, Constants.ONE_DAYS).getTime()
        / 1000 / Constants.FIVE_MINUTE_SECONDS;
    Date endTime = (new Date(multipleOfInterval * Constants.FIVE_MINUTE_SECONDS * 1000));
    Date startTime = new Date(endTime.getTime() - Constants.FIVE_MINUTE_SECONDS * 1000);

    // 以五分钟为时间段向后偏移，一直偏移到当前时间
    while (!GracefulShutdownHelper.isShutdownNow() && metricDatetime.after(startTime)) {
      // 该五分钟没有上报失败的数据，跳过该五分钟
      List<SendupMessageDO> sendupMessageList = sendupMessageDao.querySendupMessages(deviceType,
          serialNumber, FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC, Constants.RES_NOK, startTime,
          endTime);
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
      SendupRequest request = SendupRequest.newBuilder().setDeviceType(deviceType)
          .setSerialNumber(serialNumber).setMessageId(messageId)
          .setMessageType(FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC)
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
   * @param deviceType
   * @param serialNumber
   * @param metricDatetime
   */
  private void resendLogAlarmEntity(String deviceType, String serialNumber, Date metricDatetime) {

    // 构造一天前向后偏移五分钟的时间范围
    long multipleOfInterval = (long) DateUtils.beforeDayDate(metricDatetime, Constants.ONE_DAYS)
        .getTime() / 1000 / Constants.FIVE_MINUTE_SECONDS;
    Date endTime = (new Date(multipleOfInterval * Constants.FIVE_MINUTE_SECONDS * 1000));
    Date startTime = new Date(endTime.getTime() - Constants.FIVE_MINUTE_SECONDS * 1000);

    // 以五分钟为时间段向后偏移，一直偏移到当前时间
    while (!GracefulShutdownHelper.isShutdownNow() && metricDatetime.after(startTime)) {
      // 该五分钟没有上报失败的数据，跳过该五分钟
      List<SendupMessageDO> sendupMessageList = sendupMessageDao.querySendupMessages(deviceType,
          serialNumber, FpcCmsConstants.SENDUP_TYPE_LOG_ALARM, Constants.RES_NOK, startTime,
          endTime);
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
      SendupRequest request = SendupRequest.newBuilder().setDeviceType(deviceType)
          .setSerialNumber(serialNumber).setMessageId(messageId)
          .setMessageType(FpcCmsConstants.RESEND_TYPE_LOG_ALARM).setStartTime(startTime.getTime())
          .setEndTime(endTime.getTime()).addAllEntity(entityList)
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
  private MetricEntity generateMetricEntity(String deviceType, String serialNumber, Date startTime,
      Date endTime) {
    // 系统状态
    CentralSystemDO centralSystemDO = centralSystemDao.queryCentralSystem(deviceType, serialNumber);
    SystemMetric systemMetric = SystemMetric.newBuilder()
        .setCpuMetric(centralSystemDO.getCpuMetric())
        .setMemoryMetric(centralSystemDO.getMemoryMetric())
        .setSystemFsMetric(centralSystemDO.getSystemFsMetric()).setIndexFsMetric(0)
        .setMetadataFsMetric(0).setMetadataHotFsMetric(0).setPacketFsMetric(0)
        .setMetricTime(centralSystemDO.getMetricTime().getTime()).build();

    // 管理口、业务口流量
    List<CentralNetifDO> deviceNetifList = centralNetifDao.queryCentralNetifProfiles(deviceType,
        serialNumber, null);

    List<NetifMetric> netifMetricList = Lists.newArrayListWithCapacity(deviceNetifList.size());
    deviceNetifList.forEach(deviceNetif -> {
      centralNetifDao
          .queryCentralNetifs(deviceType, deviceNetif.getMonitoredSerialNumber(),
              deviceNetif.getNetifName(), null, Constants.ONE_MINUTE_SECONDS, startTime, endTime)
          .forEach(oneItem -> {
            NetifMetric netifMetric = NetifMetric.newBuilder().setNetifName(oneItem.getNetifName())
                .setCategory(deviceNetif.getCategory()).setState(deviceNetif.getState())
                .setSpecification(deviceNetif.getSpecification()).setRxBps(oneItem.getRxBps())
                .setRxPps(oneItem.getRxPps()).setTxBps(oneItem.getTxBps())
                .setTxPps(oneItem.getTxPps()).setMetricTime(oneItem.getMetricTime().getTime())
                .build();
            netifMetricList.add(netifMetric);
          });
    });

    MetricEntity metricEntity = MetricEntity.newBuilder()
        .setFsDataTotalByte(centralSystemDO.getFsDataTotalByte())
        .setFsDataUsedPct(centralSystemDO.getFsDataUsedPct())
        .setFsCacheTotalByte(centralSystemDO.getFsCacheTotalByte())
        .setFsCacheUsedPct(centralSystemDO.getFsCacheUsedPct())
        .setDataOldestTime(centralSystemDO.getDataOldestTime())
        .setDataLast24TotalByte(centralSystemDO.getDataLast24TotalByte())
        .setDataPredictTotalDay(centralSystemDO.getDataPredictTotalDay())
        .setCacheFileAvgByte(centralSystemDO.getCacheFileAvgByte())
        .setFsStoreTotalByte(centralSystemDO.getFsStoreTotalByte())
        .setFsSystemTotalByte(centralSystemDO.getFsSystemTotalByte())
        .setFsIndexTotalByte(centralSystemDO.getFsIndexTotalByte())
        .setFsMetadataTotalByte(centralSystemDO.getFsMetadataTotalByte())
        .setFsMetadataHotTotalByte(centralSystemDO.getFsMetadataHotTotalByte())
        .setFsPacketTotalByte(centralSystemDO.getFsPacketTotalByte())
        .addAllSystemMetric(Lists.newArrayList(systemMetric)).addAllNetifMetric(netifMetricList)
        .setMetricTime(endTime.getTime()).build();

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
          sendupMessage.setDeviceType(request.getDeviceType());
          sendupMessage.setDeviceSerialNumber(request.getSerialNumber());
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
   * 发送配置并更新状态(网络、探针)
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
            networkDao.updateFpcNetworkReportState(
                networkEntityList.stream().map(item -> item.getId()).collect(Collectors.toList()),
                Constants.BOOL_YES);
          }

          // 探针不为空时更新探针上报结果
          List<SensorEntity> sensorEntityList = request.getEntity(0).getSensorEntityList();
          if (CollectionUtils.isNotEmpty(sensorEntityList)) {
            fpcDao.updateFpcReportState(sensorEntityList.stream()
                .map(item -> item.getSerialNumber()).collect(Collectors.toList()),
                Constants.BOOL_YES);
          }

          // cms不为空时更新cms上报结果
          List<CmsEntity> cmsEntityList = request.getEntity(0).getCmsEntityList();
          if (CollectionUtils.isNotEmpty(cmsEntityList)) {
            cmsDao.updateCmsReportState(cmsEntityList.stream().map(item -> item.getSerialNumber())
                .collect(Collectors.toList()), Constants.BOOL_YES);
          }
        }
      } catch (StatusRuntimeException e) {
        // 发送失败将该上报结果更新为失败
        LOGGER.warn("send up appliance [{}] failed.", request.getMessageType(), e);
      }
    }
  }

}
