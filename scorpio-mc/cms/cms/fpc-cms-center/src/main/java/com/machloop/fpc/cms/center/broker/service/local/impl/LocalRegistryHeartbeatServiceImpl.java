package com.machloop.fpc.cms.center.broker.service.local.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.metric.system.helper.MonitorSystemHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.dao.UserDao;
import com.machloop.alpha.webapp.system.data.UserDO;
import com.machloop.alpha.webapp.system.service.DeviceNetifCallback;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskBO;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskRecordBO;
import com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService;
import com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.helper.GrpcClientHelper;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.DeviceStatus;
import com.machloop.fpc.cms.grpc.CentralProto.HeartbeatRequest;
import com.machloop.fpc.cms.grpc.CentralProto.RegisterRequest;
import com.machloop.fpc.cms.grpc.CentralProto.ReplyMessage;
import com.machloop.fpc.cms.grpc.CentralProto.RequestMessage;
import com.machloop.fpc.cms.grpc.CentralProto.TaskExecution;

import io.grpc.StatusRuntimeException;

/**
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
@Service
public class LocalRegistryHeartbeatServiceImpl implements LocalRegistryHeartbeatService {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(LocalRegistryHeartbeatServiceImpl.class);

  private static final int REGISTE_DEFAULT_SIZE = 2;

  private static final String SN_NOT_SPECIFIED = "Not Specified";

  @Autowired
  private GrpcClientHelper grpcClientHelper;

  @Autowired
  private BuildProperties buildProperties;

  @Autowired
  private UserDao userDao;

  @Autowired
  private AssignmentTaskService assignmentTaskService;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private SystemServerIpService systemServerIpService;

  @Autowired
  private DeviceStatusService deviceStatusService;

  // 保证在init之前执行注册
  @SuppressWarnings("unused")
  @Autowired
  private DeviceNetifCallback deviceNetifCallback;

  private Date heartbeatReplyTime;

  private Date lastHeartbeatPersistence;

  private volatile String parentCmsIp;

  private String localCmsIp;

  private String serialNumber;

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService#init()
   */
  @Override
  public void init() {
    serialNumber = licenseService.queryDeviceSerialNumber();
    parentCmsIp = globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_IP);
    localCmsIp = systemServerIpService.getServerIp();
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService#register()
   */
  @Override
  public Map<String, String> register() {

    Map<String, String> registerMap = Maps.newHashMapWithExpectedSize(REGISTE_DEFAULT_SIZE);

    // 注册时现将parentCmsIp置为空，停止心跳
    this.parentCmsIp = "";

    // 检查设备序列号是否存在
    if (StringUtils.isBlank(serialNumber) || StringUtils.equals(serialNumber, SN_NOT_SPECIFIED)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "未获取到本机序列号");
    }

    // version
    String version = StringUtils.defaultIfBlank(HotPropertiesHelper.getProperty("product.version"),
        buildProperties.get("machloop.prod.version"));

    // deviceName
    String deviceName = globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME);
    if (StringUtils.isBlank(deviceName)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备名称未配置");
    }

    // cmsToken
    String cmsToken = "";
    try {
      cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
          globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_TOKEN));

      if (StringUtils.isBlank(cmsToken)) {
        LOGGER.warn("cannot found cmsToken, please check configuration file.");
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "系统出现异常");
      }
    } catch (UnsupportedOperationException e) {
      LOGGER.warn("cmsToken decode error, please check configuration file.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统出现异常");
    }

    // 获取内置rest用户
    String appKey = "";
    String appToken = "";
    List<UserDO> restUsers = userDao.queryUserByType(WebappConstants.USER_TYPE_INTERNAL_REST);
    if (CollectionUtils.isNotEmpty(restUsers)) {
      UserDO restUser = restUsers.get(0);
      appKey = restUser.getAppKey();
      appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
          restUser.getAppToken());
    } else {
      LOGGER.warn("cannot found internal rest user.");
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "系统出现异常");
    }

    // 构造注册消息
    Date registerTime = DateUtils.now();
    RegisterRequest registerRequest = RegisterRequest.newBuilder()
        .setDeviceType(FpcCmsConstants.DEVICE_TYPE_CMS).setDeviceIp(localCmsIp)
        .setDeviceName(deviceName).setSerialNumber(serialNumber).setVersion(version)
        .setCmsToken(cmsToken).setAppKey(appKey).setAppToken(appToken)
        .setTimestamp(registerTime.getTime()).build();
    RequestMessage request = RequestMessage.newBuilder()
        .setMessageType(FpcCmsConstants.MESSAGE_TYPE_REGISTER).setRegisterRequest(registerRequest)
        .build();

    // 发送注册消息并回应
    try {
      LOGGER.debug("start to registe to cms, {}", request);
      ReplyMessage reply = grpcClientHelper.getGrpcServerBlockingStub().registerHeartbeat(request);
      String replyCmsIp = reply.getIp();

      // 注册成功刷新心跳时间
      if (reply.getCode() == FpcCmsConstants.RESULT_SUCCESS_CODE
          && StringUtils.isNotBlank(replyCmsIp)) {
        this.heartbeatReplyTime = DateUtils.now();
        LOGGER.info("success to registe to cms, cmsIp is {}", replyCmsIp);
      } else {
        // 注册失败
        this.heartbeatReplyTime = null;
        LOGGER.warn("fail to registe to cms, cmsIp is null. detail: {}", reply.getDetail());

        registerMap.put("detail", reply.getDetail());
        return registerMap;
      }

      // 刷新数据库
      this.parentCmsIp = replyCmsIp;

      registerMap.put("serialNumber", serialNumber);
      registerMap.put("registerTime", String.valueOf(registerTime.getTime()));
      registerMap.put("detail", reply.getDetail());
      registerMap.put("exist", reply.getExist());
    } catch (StatusRuntimeException e) {
      LOGGER.warn("failed to connect the server.", e);
    }

    return registerMap;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService#heartbeat()
   */
  @Override
  public void heartbeat() {
    // 查询最近一天下发任务
    Date endTime = DateUtils.now();
    Date startTime = DateUtils.beforeDayDate(endTime, 1);
    List<AssignmentTaskBO> assignmentTaskList = assignmentTaskService
        .queryHigherAssignmentTasks(startTime, endTime);

    List<TaskExecution> taskExecutionList = Lists
        .newArrayListWithCapacity(assignmentTaskList.size());
    for (AssignmentTaskBO assignmentTask : assignmentTaskList) {
      List<AssignmentTaskRecordBO> taskRecords = assignmentTaskService
          .queryAssignmentTaskRecordsWithoutPage(assignmentTask.getId());
      for (AssignmentTaskRecordBO taskRecord : taskRecords) {
        TaskExecution taskExecution = TaskExecution.newBuilder()
            .setTaskId(taskRecord.getFpcTaskId()).setAssignTaskId(assignmentTask.getAssignTaskId())
            .setAssignTaskTime(assignmentTask.getAssignTaskTime().getTime())
            .setExecutionStartTime(StringUtils.isNotBlank(taskRecord.getExecutionStartTime())
                ? DateUtils.parseISO8601Date(taskRecord.getExecutionStartTime()).getTime()
                : 0L)
            .setExecutionEndTime(StringUtils.isNotBlank(taskRecord.getExecutionEndTime())
                ? DateUtils.parseISO8601Date(taskRecord.getExecutionEndTime()).getTime()
                : 0L)
            .setExecutionProgress(taskRecord.getExecutionProgress())
            .setExecutionState(taskRecord.getExecutionState())
            .setExecutionTrace(taskRecord.getExecutionTrace())
            .setState(taskRecord.getExecutionState()).build();

        taskExecutionList.add(taskExecution);
      }
    }

    // cmsToken
    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
        globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_TOKEN));

    // 获取版本号
    String productVersion = StringUtils.defaultIfBlank(
        HotPropertiesHelper.getProperty("product.version"),
        buildProperties.get("machloop.prod.version"));

    // deviceName
    String deviceName = globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME,
        false);

    // 获取运行时长
    long upTime = MonitorSystemHelper.fetchSystemRuntimeSecond();

    // 获取license状态，CMS未支持license，状态默认为正常
    String licenseState = FpcCmsConstants.LICENSE_NORMAL;

    // 获取上次心跳
    long lastHeartbeat = 0;
    if (heartbeatReplyTime == null) {
      String value = globalSettingService
          .getValue(CenterConstants.GLOBAL_SETTING_LOCAL_LAST_HEARTBEAT, false);
      lastHeartbeat = StringUtils.isNotBlank(value) ? Long.parseLong(value) : 0;
    } else {
      lastHeartbeat = heartbeatReplyTime.getTime();
    }

    // 获取所有下级设备状态
    List<DeviceStatus> deviceStatusList = deviceStatusService.queryDeviceStatus().stream()
        .map(deviceStatusDO -> {
          return DeviceStatus.newBuilder().setDeviceType(deviceStatusDO.getDeviceType())
              .setDeviceIp(deviceStatusDO.getDeviceIp())
              .setDeviceName(deviceStatusDO.getDeviceName())
              .setSerialNumber(deviceStatusDO.getSerialNumber())
              .setVersion(deviceStatusDO.getVersion())
              .setLicenseState(deviceStatusDO.getLicenseState())
              .setUpTime(deviceStatusDO.getUpTime())
              .setLastInteractiveTime(deviceStatusDO.getLastInteractiveTime().getTime())
              .setLastInteractiveLatency(deviceStatusDO.getLastInteractiveLatency())
              .setTimestamp(deviceStatusDO.getTimestamp()).build();
        }).collect(Collectors.toList());

    // 构造心跳消息
    HeartbeatRequest heartbeat = HeartbeatRequest.newBuilder()
        .setDeviceType(FpcCmsConstants.DEVICE_TYPE_CMS).setDeviceIp(localCmsIp)
        .setDeviceName(deviceName).setSerialNumber(serialNumber).setLicenseState(licenseState)
        .setCmsToken(cmsToken).setVersion(productVersion).setUptime(upTime)
        .setLastHeartbeat(lastHeartbeat).setTimestamp(DateUtils.now().getTime())
        .addAllTaskExecution(taskExecutionList).addAllDeviceStatus(deviceStatusList).build();
    RequestMessage request = RequestMessage.newBuilder()
        .setMessageType(FpcCmsConstants.MESSAGE_TYPE_HEARTBEAT).setHeartbeatRequest(heartbeat)
        .build();

    LOGGER.debug("start heartbeat request message, {}", request);

    // 发送消息并获得回应
    ReplyMessage reply = grpcClientHelper.getGrpcServerBlockingStub().registerHeartbeat(request);
    if (reply.getCode() == FpcCmsConstants.RESULT_SUCCESS_CODE) {
      this.heartbeatReplyTime = DateUtils.now();
    } else if (reply.getCode() == FpcCmsConstants.FPC_NOT_FOUND_CODE) {
      LOGGER.warn("The current device has been removed by cms, please registry again.");
      // 产生告警
      AlarmHelper.alarm(AlarmHelper.LEVEL_IMPORTANT, FpcCmsConstants.ALARM_CATEGORY_CMS,
          "cms_heartbeat", "当前设备已被上级CMS移除，请重新注册或关闭CMS集群配置");

      // 停止心跳
      this.parentCmsIp = "";
    }

    // 持久化心跳
    if (lastHeartbeatPersistence == null || lastHeartbeatPersistence.getTime()
        + Constants.ONE_MINUTE_SECONDS * 1000 < System.currentTimeMillis()) {
      globalSettingService.setValue(CenterConstants.GLOBAL_SETTING_LOCAL_LAST_HEARTBEAT,
          String.valueOf(this.heartbeatReplyTime != null ? this.heartbeatReplyTime.getTime()
              : System.currentTimeMillis()));
      lastHeartbeatPersistence = DateUtils.now();
    }

    LOGGER.debug("receive heartbeat reply message, {}", reply);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService#getParentCmsIp()
   */
  public String getParentCmsIp() {
    return parentCmsIp;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService#setParentCmsIp(java.lang.String)
   */
  public void setParentCmsIp(String parentCmsIp) {
    this.parentCmsIp = parentCmsIp;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService#getLocalCmsIp()
   */
  public String getLocalCmsIp() {
    return localCmsIp;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService#getSerialNumber()
   */
  public String getSerialNumber() {
    return serialNumber;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService#isAlive()
   */
  @Override
  public boolean isAlive() {
    return this.heartbeatReplyTime != null && (DateUtils.now().getTime()
        - this.heartbeatReplyTime.getTime() <= FpcCmsConstants.HEARTBEAT_INACTIVATION_MILLISECOND);
  }

}
