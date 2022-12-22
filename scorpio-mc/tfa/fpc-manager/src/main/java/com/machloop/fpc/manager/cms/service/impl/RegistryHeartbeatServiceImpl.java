package com.machloop.fpc.manager.cms.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.dao.UserDao;
import com.machloop.alpha.webapp.system.data.UserDO;
import com.machloop.alpha.webapp.system.service.DeviceNetifCallback;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.HeartbeatRequest;
import com.machloop.fpc.cms.grpc.CentralProto.RegisterRequest;
import com.machloop.fpc.cms.grpc.CentralProto.ReplyMessage;
import com.machloop.fpc.cms.grpc.CentralProto.RequestMessage;
import com.machloop.fpc.cms.grpc.CentralProto.TaskExecution;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.dao.TransmitTaskDao;
import com.machloop.fpc.manager.appliance.data.TransmitTaskDO;
import com.machloop.fpc.manager.cms.service.RegistryHeartbeatService;
import com.machloop.fpc.manager.helper.GrpcClientHelper;
import com.machloop.fpc.manager.system.bo.LicenseBO;
import com.machloop.fpc.manager.system.service.LicenseService;

import io.grpc.StatusRuntimeException;

/**
 * @author liyongjun
 *
 * create at 2019年12月4日, fpc-manager
 */
@Service
public class RegistryHeartbeatServiceImpl implements RegistryHeartbeatService {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryHeartbeatServiceImpl.class);

  private static final int REGISTE_DEFAULT_SIZE = 2;

  private static final String SN_NOT_SPECIFIED = "Not Specified";

  @Autowired
  private GrpcClientHelper grpcClientHelper;

  @Autowired
  private BuildProperties buildProperties;

  @Autowired
  private TransmitTaskDao transmitTaskDao;

  @Autowired
  private UserDao userDao;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private SystemServerIpService systemServerIpService;

  // 保证在init之前执行注册
  @SuppressWarnings("unused")
  @Autowired
  private DeviceNetifCallback deviceNetifCallback;

  private Date heartbeatReplyTime;

  private Date lastHeartbeatPersistence;

  private volatile String cmsIp;

  private String fpcIp;

  private String serialNumber;

  @Value("${fpc.engine.rest.server.open.port}")
  private String engineRestOpenPort;

  /**
   * @see com.machloop.fpc.manager.cms.service.RegistryHeartbeatService#init()
   */
  @Override
  public void init() {
    cmsIp = globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP);
    fpcIp = systemServerIpService.getServerIp();
    serialNumber = licenseService.queryDeviceSerialNumber();
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.RegistryHeartbeatService#register()
   */
  @Override
  public Map<String, String> register() {

    Map<String, String> registerMap = Maps.newHashMapWithExpectedSize(REGISTE_DEFAULT_SIZE);

    // 注册时现将cmsIp置为空，停止心跳
    this.cmsIp = "";

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
          globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_TOKEN));

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
        .setDeviceType(FpcCmsConstants.DEVICE_TYPE_TFA).setDeviceIp(fpcIp).setDeviceName(deviceName)
        .setSerialNumber(serialNumber).setVersion(version)
        .setSensorType(FpcCmsConstants.SENSOR_TYPE_NORMAL).setCmsToken(cmsToken).setAppKey(appKey)
        .setAppToken(appToken).setTimestamp(registerTime.getTime()).build();
    RequestMessage request = RequestMessage.newBuilder()
        .setMessageType(FpcCmsConstants.MESSAGE_TYPE_REGISTER).setRegisterRequest(registerRequest)
        .build();

    // 发送注册消息并回应
    try {
      LOGGER.debug("start to registe to cms, {}", request);
      ReplyMessage reply = grpcClientHelper.getGrpcServerBlockingStub().registerHeartbeat(request);
      String replyCmsIp = reply.getIp();

      // 注册成功刷新心跳时间
      if (StringUtils.isNotBlank(replyCmsIp)) {
        this.heartbeatReplyTime = DateUtils.now();

        LOGGER.info("success to registe to cms, cmsIp is {}", replyCmsIp);
      } else {
        this.heartbeatReplyTime = null;
        LOGGER.warn("fail to registe to cms, cmsIp is null. detail: {}", reply.getDetail());

        registerMap.put("detail", reply.getDetail());
        return registerMap;
      }

      // 刷新数据库
      this.cmsIp = replyCmsIp;

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
   * @see com.machloop.fpc.manager.cms.service.RegistryHeartbeatService#heartbeat()
   */
  @Override
  public void heartbeat() {
    // 查询所有下发任务
    List<TransmitTaskDO> assignmentTaskList = transmitTaskDao.queryAssignmentTasks();
    List<TaskExecution> taskExecutionList = Lists
        .newArrayListWithCapacity(assignmentTaskList.size());
    for (TransmitTaskDO transmitTaskDO : assignmentTaskList) {
      TaskExecution.Builder taskExecutionBuilder = TaskExecution.newBuilder();
      taskExecutionBuilder.setTaskId(transmitTaskDO.getId())
          .setAssignTaskId(transmitTaskDO.getAssignTaskId())
          .setAssignTaskTime(transmitTaskDO.getAssignTaskTime().getTime())
          .setExecutionStartTime(transmitTaskDO.getExecutionStartTime() != null
              ? transmitTaskDO.getExecutionStartTime().getTime()
              : 0L)
          .setExecutionEndTime(transmitTaskDO.getExecutionEndTime() != null
              ? transmitTaskDO.getExecutionEndTime().getTime()
              : 0L)
          .setExecutionProgress(transmitTaskDO.getExecutionProgress())
          .setExecutionState(transmitTaskDO.getState())
          .setExecutionCachePath(transmitTaskDO.getExecutionCachePath())
          .setExecutionTrace(transmitTaskDO.getExecutionTrace()).setState(transmitTaskDO.getState())
          .setFpcSerialNumber(serialNumber).build();

      // 任务状态是已完成并且文件地址不为空并且下载文件url为空，生成下载url
      Date currentTime = DateUtils.now();
      String executionDownloadUrl = transmitTaskDO.getExecutionDownloadUrl();
      if (StringUtils.equals(FpcConstants.APPLIANCE_TRANSMITTASK_STATE_FINISH,
          transmitTaskDO.getState())
          && StringUtils.isNotBlank(transmitTaskDO.getExecutionCachePath())
          && StringUtils.isBlank(transmitTaskDO.getExecutionDownloadUrl())) {
        executionDownloadUrl = generateAndEncryptDownloadUrl(transmitTaskDO.getId(),
            DateUtils.toStringISO8601(currentTime));
        transmitTaskDao.updateTransmitTaskExecutionDownloadUrl(transmitTaskDO.getId(),
            executionDownloadUrl);
      }

      // 任务执行结束时间与当前时间超过下载url有效时间并且文件地址不为空重新生成下载url
      if (StringUtils.isNotBlank(transmitTaskDO.getExecutionCachePath())
          && transmitTaskDO.getExecutionEndTime() != null && currentTime.getTime() - transmitTaskDO
              .getExecutionEndTime().getTime() > Constants.DOWNLOAD_MAX_GAP_MILLSEC) {
        executionDownloadUrl = generateAndEncryptDownloadUrl(transmitTaskDO.getId(),
            DateUtils.toStringISO8601(currentTime));
        transmitTaskDao.updateTransmitTaskExecutionDownloadUrl(transmitTaskDO.getId(),
            executionDownloadUrl);
      }

      taskExecutionBuilder.setExecutionDownloadUrl(executionDownloadUrl);
      TaskExecution taskExecution = taskExecutionBuilder.build();
      taskExecutionList.add(taskExecution);
    }

    // cmsToken
    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
        globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_TOKEN));

    // 获取版本号
    String productVersion = StringUtils.defaultIfBlank(
        HotPropertiesHelper.getProperty("product.version"),
        buildProperties.get("machloop.prod.version"));

    // deviceName
    String deviceName = globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME,
        false);

    // 获取运行时长
    long upTime = MonitorSystemHelper.fetchSystemRuntimeSecond();

    // 获取license状态
    String licenseState = FpcCmsConstants.LICENSE_ABNORMALITY;
    LicenseBO license = licenseService.queryCacheLicense();
    String expiryTime = license.getExpiryTime();
    if (StringUtils.isNotBlank(expiryTime)
        && DateUtils.parseISO8601Date(expiryTime).after(DateUtils.now())) {
      licenseState = FpcCmsConstants.LICENSE_NORMAL;
    }

    // 获取上次心跳
    long lastHeartbeat = 0;
    if (heartbeatReplyTime == null) {
      String value = globalSettingService
          .getValue(ManagerConstants.GLOBAL_SETTING_LOCAL_LAST_HEARTBEAT, false);
      lastHeartbeat = StringUtils.isNotBlank(value) ? Long.parseLong(value) : 0;
    } else {
      lastHeartbeat = heartbeatReplyTime.getTime();
    }

    // 构造心跳消息
    HeartbeatRequest heartbeat = HeartbeatRequest.newBuilder()
        .setDeviceType(FpcCmsConstants.DEVICE_TYPE_TFA).setDeviceIp(fpcIp).setDeviceName(deviceName)
        .setSerialNumber(serialNumber).setLicenseState(licenseState).setCmsToken(cmsToken)
        .setVersion(productVersion).setUptime(upTime).setLastHeartbeat(lastHeartbeat)
        .setTimestamp(DateUtils.now().getTime()).addAllTaskExecution(taskExecutionList).build();
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
      AlarmHelper.alarm(AlarmHelper.LEVEL_IMPORTANT, FpcConstants.ALARM_CATEGORY_CMS,
          "cms_heartbeat", "当前设备已被上级CMS移除，请重新注册或关闭CMS集群配置");

      // 停止心跳
      this.cmsIp = "";
    }

    // 持久化心跳
    if (lastHeartbeatPersistence == null || lastHeartbeatPersistence.getTime()
        + Constants.ONE_MINUTE_SECONDS * 1000 < System.currentTimeMillis()) {
      globalSettingService.setValue(ManagerConstants.GLOBAL_SETTING_LOCAL_LAST_HEARTBEAT,
          String.valueOf(this.heartbeatReplyTime != null ? this.heartbeatReplyTime.getTime()
              : System.currentTimeMillis()));
      lastHeartbeatPersistence = DateUtils.now();
    }

    LOGGER.debug("receive heartbeat reply message, {}", reply);
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.RegistryHeartbeatService#setCmsIp(java.lang.String)
   */
  @Override
  public void setCmsIp(String cmsIp) {
    this.cmsIp = cmsIp;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.RegistryHeartbeatService#getCmsIp()
   */
  @Override
  public String getCmsIp() {
    return this.cmsIp;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.RegistryHeartbeatService#getFpcIp()
   */
  @Override
  public String getFpcIp() {
    return this.fpcIp;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.RegistryHeartbeatService#getSerialNumber()
   */
  @Override
  public String getSerialNumber() {
    return this.serialNumber;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.RegistryHeartbeatService#isAlive()
   */
  @Override
  public boolean isAlive() {
    return this.heartbeatReplyTime != null && (DateUtils.now().getTime()
        - this.heartbeatReplyTime.getTime() <= FpcCmsConstants.HEARTBEAT_INACTIVATION_MILLISECOND);
  }

  /**
   * 生成任务下载url
   * @param id
   * @param date
   * @return
   * @throws UnsupportedEncodingException
   */
  private String generateAndEncryptDownloadUrl(String id, String date) {
    String path = String.format(ManagerConstants.REST_ENGINE_TASK_PACKET_DOWNLOAD, id);

    // 使用UUID作为凭证，并取token进行签名
    String credential = IdGenerator.generateUUID();
    String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);

    // 拼接文件下载地址
    String downloadUrl = "";
    try {
      StringBuilder fileUrl = new StringBuilder();
      fileUrl.append("https://");
      fileUrl.append(fpcIp);
      fileUrl.append(":");
      fileUrl.append(HotPropertiesHelper.getProperty("fpc.engine.rest.server.open.port"));
      fileUrl.append(path);
      fileUrl.append("?X-Machloop-Date=");
      fileUrl.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      fileUrl.append("&X-Machloop-Credential=");
      fileUrl.append(credential);
      fileUrl.append("&X-Machloop-Signature=");
      fileUrl.append(TokenUtils.makeSignature(token, credential, "GET", date, path));

      // 对生成好的url加密
      String encryptedUrl = KeyEncUtils.encrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fileUrl.toString());

      // 将加密后的url在进行URL编码
      downloadUrl = URLEncoder.encode(encryptedUrl, StandardCharsets.UTF_8.name());

    } catch (UnsupportedEncodingException e) {
      LOGGER.warn("failed to generate download URL. ", e);
    }

    return downloadUrl;
  }

}
