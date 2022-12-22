package com.machloop.fpc.cms.center.broker.service.subordinate.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskRecordDO;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.broker.service.subordinate.RegistryHeartbeatService;
import com.machloop.fpc.cms.center.central.bo.CmsBO;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.dao.ClusterDao;
import com.machloop.fpc.cms.center.central.service.CmsService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.helper.ClickhouseRemoteServerHelper;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.HeartbeatRequest;
import com.machloop.fpc.cms.grpc.CentralProto.RegisterRequest;
import com.machloop.fpc.cms.grpc.CentralProto.ReplyMessage;
import com.machloop.fpc.cms.grpc.CentralProto.RequestMessage;
import com.machloop.fpc.cms.grpc.CentralProto.TaskExecution;

/**
 * @author liyongjun
 *
 * create at 2019年12月12日, fpc-cms-center
 */
@Service
public class RegistryHeartbeatServiceImpl implements RegistryHeartbeatService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryHeartbeatServiceImpl.class);

  private static final int REGISTER_FAILED_CODE = 5000;
  private static final int HEARTBEAT_ABNORMAL_CODE = 50001;

  private static final String TFA_REGISTRY = "tfa_registry";
  private static final String CMS_REGISTRY = "cms_registry";

  private String cmsIp;

  @Value("${rocketmq.broker.file-reserved-time}")
  private int fileReservedTime;

  @Autowired
  private AssignmentTaskRecordDao assignmentTaskRecordDao;

  @Autowired
  private ClusterDao clusterDao;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private CmsService cmsService;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private DeviceStatusService deviceStatusService;

  @Autowired
  private SystemServerIpService systemServerIpService;

  @Autowired
  private List<MQAssignmentService> mQAssignmentServices;

  @Autowired
  private ApplicationContext context;

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.RegistryHeartbeatService#registerHeartbeat(com.machloop.fpc.cms.grpc.CentralProto.RequestMessage)
   */
  @Override
  public ReplyMessage registerHeartbeat(RequestMessage requestMessage) {
    if (StringUtils.isBlank(cmsIp)) {
      cmsIp = systemServerIpService.getServerIp();
    }

    ReplyMessage reply = null;

    // 根据类型区分注册或心跳
    String type = requestMessage.getMessageType();
    if (StringUtils.equals(type, FpcCmsConstants.MESSAGE_TYPE_REGISTER)) {
      LOGGER.info("receive register request message, {}", requestMessage);

      reply = register(requestMessage);

      LOGGER.info("reply registry message, {}", reply);
    } else if (StringUtils.equals(type, FpcCmsConstants.MESSAGE_TYPE_HEARTBEAT)) {
      LOGGER.debug("receive heartbeat request message, {}", requestMessage);

      reply = heartbeat(requestMessage);

      LOGGER.debug("reply heartbeat message, {}", reply);
    }

    return reply;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.RegistryHeartbeatService#getMQAssignmentServices()
   */
  @Override
  public List<MQAssignmentService> getMQAssignmentServices() {
    return mQAssignmentServices;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.RegistryHeartbeatService#assignmentFullConfigurations(java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public void assignmentFullConfigurations(String deviceType, String serialNumber,
      Set<String> tags) {
    // 全量下发前，先将指定消费组（探针）的全量topic消费位点置为当前时间
    if (StringUtils.isNotBlank(deviceType) && StringUtils.isNotBlank(serialNumber)) {
      DefaultMQAdminExt mqAdminExt = context.getBean("getDefaultMQAdminExt",
          DefaultMQAdminExt.class);
      String groupPrefix = StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)
          ? "tfa_consumer"
          : "cms_consumer";
      String group = StringUtils.joinWith("_", groupPrefix, serialNumber);
      try {
        Map<MessageQueue, Long> resetResult = mqAdminExt.resetOffsetByTimestamp(
            FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT, group, DateUtils.now().getTime(), true);
        LOGGER.info("topic: [{}], group: [{}], reset offset success: {}",
            FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT, group, resetResult);
      } catch (RemotingException | MQBrokerException | InterruptedException | MQClientException e) {
        LOGGER.warn("topic: [{}], group: [{}], reset offset error.",
            FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT, group, e);
      }
    }

    // 如果指定了tag，则筛选出对应tag的service
    List<MQAssignmentService> currentAssignmentServices = Lists.newArrayList(mQAssignmentServices);
    if (CollectionUtils.isNotEmpty(tags)) {
      Iterator<MQAssignmentService> iterator = currentAssignmentServices.iterator();
      while (iterator.hasNext()) {
        List<String> itemTags = iterator.next().getTags();
        if (!CollectionUtils.containsAny(itemTags, tags)) {
          iterator.remove();
        }
      }
    }
    if (CollectionUtils.isEmpty(currentAssignmentServices)) {
      LOGGER.warn("tag not exists, full assignment failed. tags: {}", tags);
      return;
    }

    // 遍历所有配置类，全量下发
    long offset = DateUtils.now().getTime();
    Iterator<MQAssignmentService> iterator = currentAssignmentServices.iterator();
    while (iterator.hasNext()) {
      MQAssignmentService mqAssignmentService = iterator.next();

      if (iterator.hasNext()) {
        mqAssignmentService.assignmentFullConfiguration(deviceType, serialNumber, tags, 0);
      } else {
        mqAssignmentService.assignmentFullConfiguration(deviceType, serialNumber, tags, offset);
      }
    }
  }

  /**
   * 处理注册消息
   * 
   * @param requestMessage
   * @return
   */
  private ReplyMessage register(RequestMessage requestMessage) {
    ReplyMessage reply = null;

    // 获取注册信息并校验
    RegisterRequest registerRequest = requestMessage.getRegisterRequest();
    String deviceType = registerRequest.getDeviceType();
    String deviceIp = registerRequest.getDeviceIp();
    String deviceName = registerRequest.getDeviceName();
    String serialNumber = registerRequest.getSerialNumber();
    String version = registerRequest.getVersion();
    String cmsToken = registerRequest.getCmsToken();
    String appKey = registerRequest.getAppKey();
    String appToken = registerRequest.getAppToken();
    long timestamp = registerRequest.getTimestamp();
    if (StringUtils.isAnyBlank(deviceType, deviceIp, deviceName, serialNumber, version, cmsToken,
        appKey, appToken, String.valueOf(timestamp))) {
      LOGGER.warn("[register] device({}) incomplete information: {}", deviceType, requestMessage);

      reply = ReplyMessage.newBuilder().setCode(REGISTER_FAILED_CODE).setDetail("设备基本参数不能为空")
          .setIp("").build();
      return reply;
    }

    if (StringUtils.isBlank(serialNumber)) {
      LOGGER.warn("[register] device({}) serialNo is null.", deviceType);
      reply = ReplyMessage.newBuilder().setCode(REGISTER_FAILED_CODE).setDetail("设备序列号不能为空")
          .setIp("").build();
      return reply;
    }

    // 校验cmsToken
    if (!StringUtils.equals(HotPropertiesHelper.getProperty(FpcCmsConstants.CMS_TOKEN), cmsToken)) {
      LOGGER.warn("[register] device({}) cmsToken is illegal.", deviceType);
      reply = ReplyMessage.newBuilder().setCode(REGISTER_FAILED_CODE).setDetail("CMS Token 错误")
          .setIp("").build();
      return reply;
    }

    // 本机设备序列号
    String localSerialNumber = licenseService.queryDeviceSerialNumber();

    String exist = Constants.BOOL_NO;
    if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
      if (StringUtils.isBlank(registerRequest.getSensorType())) {
        LOGGER.warn("[register] device({}) incomplete information: {}", deviceType, requestMessage);

        reply = ReplyMessage.newBuilder().setCode(REGISTER_FAILED_CODE).setDetail("探针设备类型不能为空")
            .setIp("").build();
        return reply;
      }

      // 根据serialNumber查询数据库中是否存在该设备
      FpcBO fpcBO = fpcService.queryFpcBySerialNumber(serialNumber);

      if (StringUtils.isBlank(fpcBO.getId())) {
        saveFpc(registerRequest, localSerialNumber);
      } else {
        exist = Constants.BOOL_YES;
        updateFpc(serialNumber, localSerialNumber, deviceName, deviceIp, version, appKey, appToken);
      }
    } else if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_CMS)) {
      // 根据serialNumber查询数据库中是否存在该设备
      CmsBO cmsBO = cmsService.queryCmsBySerialNumber(serialNumber);

      if (StringUtils.isBlank(cmsBO.getId())) {
        saveCms(registerRequest, localSerialNumber);
      } else {
        exist = Constants.BOOL_YES;
        updateCms(serialNumber, localSerialNumber, deviceName, deviceIp, version, appKey, appToken);
      }
    }

    // 记录日志
    StringBuilder content = new StringBuilder();
    content
        .append(StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA) ? "探针设备" : "CMS设备");
    content.append("注册到本机：");
    content.append("设备序列号=" + serialNumber).append("；");
    content.append("设备IP=" + deviceIp).append("；");
    content.append("设备名称=" + deviceName).append("；");
    content.append("设备软件版本=" + version).append("。");
    LogHelper.systemRuning(LogHelper.LEVEL_NOTICE, content.toString(), deviceType + "/" + deviceIp);

    // 注册成功下发全量配置
    LOGGER.info(
        "new device registration detected, assign all configuration, deviceType:{}, serialNumber:{}.",
        deviceType, serialNumber);
    assignmentFullConfigurations(deviceType, serialNumber, null);

    // 刷新心跳
    DeviceStatusDO deviceStatus = deviceStatusService.queryDeviceStatus(deviceType, serialNumber);
    Date lastInteractiveTime = DateUtils.now();
    deviceStatus.setLastInteractiveTime(lastInteractiveTime);
    deviceStatus.setLastInteractiveLatency(Math.abs(lastInteractiveTime.getTime() - timestamp));
    deviceStatus.setDeviceType(deviceType);
    deviceStatus.setDeviceIp(deviceIp);
    deviceStatus.setDeviceName(deviceName);
    deviceStatus.setSerialNumber(serialNumber);
    deviceStatus.setVersion(version);
    deviceStatus.setTimestamp(DateUtils.now().getTime());
    deviceStatusService.refreshDeviceStatus(deviceStatus);

    // 构造返回信息
    reply = ReplyMessage.newBuilder().setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).setIp(cmsIp).setExist(exist).build();

    return reply;
  }

  /**
   * 处理心跳消息
   * 
   * @param requestMessage
   * @return
   */
  private ReplyMessage heartbeat(RequestMessage requestMessage) {

    ReplyMessage reply = null;

    // 获取心跳信息并校验
    HeartbeatRequest heartbeatRequest = requestMessage.getHeartbeatRequest();
    String deviceType = heartbeatRequest.getDeviceType();
    String deviceIp = heartbeatRequest.getDeviceIp();
    String deviceName = heartbeatRequest.getDeviceName();
    String serialNumber = heartbeatRequest.getSerialNumber();
    String licenseState = heartbeatRequest.getLicenseState();
    String cmsToken = heartbeatRequest.getCmsToken();
    String version = heartbeatRequest.getVersion();
    long upTime = heartbeatRequest.getUptime();
    long timestamp = heartbeatRequest.getTimestamp();
    if (StringUtils.isAnyBlank(deviceType, deviceIp, deviceName, serialNumber, cmsToken,
        licenseState, version, String.valueOf(upTime), String.valueOf(timestamp))) {
      LOGGER.debug("[heartbeat] device({}) incomplete information: {}", deviceType, requestMessage);
      reply = ReplyMessage.newBuilder().setCode(HEARTBEAT_ABNORMAL_CODE).setDetail("设备基本参数不能为空")
          .setIp(cmsIp).build();
      return reply;
    }

    // 查询数据库看该设备是否存在
    FpcBO fpcBO = null;
    CmsBO cmsBO = null;
    boolean exist = false;
    if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
      fpcBO = fpcService.queryFpcBySerialNumber(serialNumber);
      exist = StringUtils.isNotBlank(fpcBO.getId());
    } else {
      cmsBO = cmsService.queryCmsBySerialNumber(serialNumber);
      exist = StringUtils.isNotBlank(cmsBO.getId());
    }

    if (!exist) {
      LOGGER.debug("[heartbeat] cannot found this device({}), serialNumber: {}, ip: {}", deviceType,
          serialNumber, deviceIp);
      reply = ReplyMessage.newBuilder().setCode(FpcCmsConstants.FPC_NOT_FOUND_CODE)
          .setDetail("不存在该设备").setIp(cmsIp).build();
      return reply;
    }

    // 本机设备序列号
    String localSerialNumber = licenseService.queryDeviceSerialNumber();

    // 设备参数是否更新（入库参数）
    DeviceStatusDO deviceStatus = deviceStatusService.queryDeviceStatus(deviceType, serialNumber);
    if (StringUtils.isNotBlank(deviceStatus.getSerialNumber())
        && (!StringUtils.equals(deviceStatus.getDeviceIp(), deviceIp)
            || !StringUtils.equals(deviceStatus.getDeviceName(), deviceName)
            || !StringUtils.equals(deviceStatus.getVersion(), version))) {
      // 修改设备状态
      if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
        updateFpc(serialNumber, localSerialNumber, deviceName, deviceIp, version, fpcBO.getAppKey(),
            fpcBO.getAppToken());
      } else {
        updateCms(serialNumber, localSerialNumber, deviceName, deviceIp, version, cmsBO.getAppKey(),
            cmsBO.getAppToken());
      }
    }

    if (StringUtils.isBlank(deviceStatus.getSerialNumber())
        && StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
      // 探针设备首次心跳
      if (!ClickhouseRemoteServerHelper.queryNodes().contains(deviceIp) && StringUtils.equals(
          clusterDao.queryNodeConnectState(deviceIp), FpcCmsConstants.CONNECT_STATUS_NORMAL)) {
        ClickhouseRemoteServerHelper.addNode(deviceIp);
        LogHelper.systemRuning(LogHelper.LEVEL_NOTICE,
            String.format("检测到设备[%s]首次心跳，将设备添加到数据集群", deviceIp), "system");
      }
    }

    // 处理查询任务下发记录
    processTaskRecord(heartbeatRequest);

    // 处理将当前心跳设备携带的下级设备状态
    if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_CMS)) {
      heartbeatRequest.getDeviceStatusList().forEach(item -> {
        DeviceStatusDO deviceStatusDO = new DeviceStatusDO();
        BeanUtils.copyProperties(item, deviceStatusDO);
        deviceStatusDO.setLastInteractiveTime(new Date(item.getLastInteractiveTime()));
        deviceStatusService.refreshDeviceStatus(deviceStatusDO);
      });
    }

    // 本次心跳据上次心跳间隔大于“消息存储时间”，全量下发配置
    long lastHeartbeat = heartbeatRequest.getLastHeartbeat();
    if (lastHeartbeat > 0
        && ((timestamp - lastHeartbeat) > fileReservedTime * Constants.ONE_HOUR_SECONDS * 1000L)) {
      LOGGER.info(
          "the heartbeat interval is greater than the message storage time, assign full configs. "
              + "lastHeartbeat: {}, currentHeartbeat: {}, message storage time: {}h.",
          DateUtils.toStringYYYYMMDDHHMMSS(new Date(lastHeartbeat)),
          DateUtils.toStringYYYYMMDDHHMMSS(new Date(timestamp)), fileReservedTime);

      assignmentFullConfigurations(deviceType, serialNumber, null);
    }

    // 心跳连接正常将该设备信息更新到心跳map中
    Date lastInteractiveTime = DateUtils.now();
    deviceStatus.setLastInteractiveTime(lastInteractiveTime);
    deviceStatus.setLastInteractiveLatency(Math.abs(lastInteractiveTime.getTime() - timestamp));
    deviceStatus.setDeviceType(deviceType);
    deviceStatus.setDeviceIp(deviceIp);
    deviceStatus.setDeviceName(deviceName);
    deviceStatus.setSerialNumber(serialNumber);
    deviceStatus.setLicenseState(licenseState);
    deviceStatus.setVersion(version);
    deviceStatus.setUpTime(upTime);
    deviceStatus.setTimestamp(DateUtils.now().getTime());
    deviceStatusService.refreshDeviceStatus(deviceStatus);

    // 构造回复消息
    reply = ReplyMessage.newBuilder().setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
        .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL).setIp(cmsIp).build();
    return reply;
  }

  private void saveFpc(RegisterRequest registerRequest, String cmsSerialNumber) {
    FpcBO fpcBO = new FpcBO();
    fpcBO.setIp(registerRequest.getDeviceIp());
    fpcBO.setName(registerRequest.getDeviceName());
    fpcBO.setSerialNumber(registerRequest.getSerialNumber());
    fpcBO.setVersion(registerRequest.getVersion());
    fpcBO.setType(registerRequest.getSensorType());
    fpcBO.setAppKey(registerRequest.getAppKey());
    fpcBO.setAppToken(registerRequest.getAppToken());
    fpcBO.setCmsToken(registerRequest.getCmsToken());
    fpcBO.setCmsSerialNumber(cmsSerialNumber);
    fpcBO.setDescription(TFA_REGISTRY);
    fpcBO.setOperatorId(TFA_REGISTRY);
    fpcService.saveFpc(fpcBO);
  }

  private void updateFpc(String serialNumber, String cmsSerialNumber, String name, String ip,
      String version, String appKey, String appToken) {
    // 修改设备参数
    FpcBO fpcBO = new FpcBO();
    fpcBO.setIp(ip);
    fpcBO.setName(name);
    fpcBO.setSerialNumber(serialNumber);
    fpcBO.setVersion(version);
    fpcBO.setAppKey(appKey);
    fpcBO.setAppToken(appToken);
    fpcBO.setCmsSerialNumber(cmsSerialNumber);

    fpcService.updateFpcStatus(fpcBO);
  }

  private void saveCms(RegisterRequest registerRequest, String superiorCmsSerialNumber) {
    CmsBO cmsBO = new CmsBO();
    cmsBO.setIp(registerRequest.getDeviceIp());
    cmsBO.setName(registerRequest.getDeviceName());
    cmsBO.setSerialNumber(registerRequest.getSerialNumber());
    cmsBO.setVersion(registerRequest.getVersion());
    cmsBO.setAppKey(registerRequest.getAppKey());
    cmsBO.setAppToken(registerRequest.getAppToken());
    cmsBO.setCmsToken(registerRequest.getCmsToken());
    cmsBO.setSuperiorCmsSerialNumber(superiorCmsSerialNumber);
    cmsBO.setDescription(CMS_REGISTRY);
    cmsBO.setOperatorId(CMS_REGISTRY);
    cmsService.saveCms(cmsBO);
  }

  private void updateCms(String serialNumber, String superiorCmsSerialNumber, String name,
      String ip, String version, String appKey, String appToken) {
    CmsBO cmsBO = new CmsBO();
    cmsBO.setIp(ip);
    cmsBO.setName(name);
    cmsBO.setSerialNumber(serialNumber);
    cmsBO.setVersion(version);
    cmsBO.setAppKey(appKey);
    cmsBO.setAppToken(appToken);
    cmsBO.setSuperiorCmsSerialNumber(superiorCmsSerialNumber);

    cmsService.updateCmsStatus(cmsBO);
  }

  /**
   * @param heartbeatRequest
   * @param serialNumber
   */
  private void processTaskRecord(HeartbeatRequest heartbeatRequest) {

    List<TaskExecution> taskExecutionList = heartbeatRequest.getTaskExecutionList();

    Map<String,
        TaskExecution> taskExecutionMap = taskExecutionList.stream().collect(
            Collectors.toMap(taskExecution -> StringUtils.join(taskExecution.getAssignTaskId(),
                taskExecution.getFpcSerialNumber()), taskExecution -> taskExecution));

    // 构造需要更新的下发任务信息
    List<AssignmentTaskRecordDO> taskRecordList = assignmentTaskRecordDao
        .queryAssignmentTaskRecords(null, taskExecutionList.stream()
            .map(TaskExecution::getAssignTaskId).collect(Collectors.toList()));
    List<AssignmentTaskRecordDO> needUpdateTaskRecordList = Lists
        .newArrayListWithCapacity(taskRecordList.size());
    for (AssignmentTaskRecordDO assignmentTaskRecordDO : taskRecordList) {
      String key = StringUtils.join(assignmentTaskRecordDO.getTaskId(),
          assignmentTaskRecordDO.getFpcSerialNumber());
      TaskExecution taskExecution = taskExecutionMap.get(key);

      // cms的任务未上报，不进行更新
      if (taskExecution == null) {
        continue;
      }

      // 上报的下发时间与cms中下发的时间不一致，不进行更新
      if (assignmentTaskRecordDO.getAssignmentTime().getTime() != taskExecution
          .getAssignTaskTime()) {
        continue;
      }

      // 构造执行开始时间和执行结束时间
      Date executionStartTime = null;
      if (taskExecution.getExecutionStartTime() != 0L) {
        executionStartTime = new Date(taskExecution.getExecutionStartTime());
      }
      Date executionEndTime = null;
      if (taskExecution.getExecutionEndTime() != 0L) {
        executionEndTime = new Date(taskExecution.getExecutionEndTime());
      }

      // 拼接下载url
      String executionDownloadUrl = "";
      if (StringUtils.isNotBlank(taskExecution.getExecutionDownloadUrl())) {
        executionDownloadUrl = HotPropertiesHelper.getProperty("download.server.protocol") + "://"
            + cmsIp + ":" + HotPropertiesHelper.getProperty("download.server.port")
            + "/download?downloadUrl=" + taskExecution.getExecutionDownloadUrl();
      }

      // 构造下发任务信息
      assignmentTaskRecordDO.setFpcTaskId(taskExecution.getTaskId());
      assignmentTaskRecordDO.setAssignmentState(CenterConstants.TASK_ASSIGNMENT_STATE_SUCCESS);
      assignmentTaskRecordDO.setExecutionStartTime(executionStartTime);
      assignmentTaskRecordDO.setExecutionEndTime(executionEndTime);
      assignmentTaskRecordDO.setExecutionState(taskExecution.getExecutionState());
      assignmentTaskRecordDO.setExecutionProgress(taskExecution.getExecutionProgress());
      assignmentTaskRecordDO.setExecutionTrace(taskExecution.getExecutionTrace());
      assignmentTaskRecordDO.setExecutionCachePath(taskExecution.getExecutionCachePath());
      assignmentTaskRecordDO.setPcapFileUrl(executionDownloadUrl);

      needUpdateTaskRecordList.add(assignmentTaskRecordDO);
    }

    // 更新下发任务信息
    assignmentTaskRecordDao.updateAssignmentTaskRecords(needUpdateTaskRecordList);
  }

}
