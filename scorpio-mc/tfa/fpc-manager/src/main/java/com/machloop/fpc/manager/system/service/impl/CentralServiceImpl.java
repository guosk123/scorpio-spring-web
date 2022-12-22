package com.machloop.fpc.manager.system.service.impl;

import java.util.Date;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.dao.GlobalSettingDao;
import com.machloop.alpha.webapp.global.data.GlobalSettingDO;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.bo.DeviceNtpBO;
import com.machloop.alpha.webapp.system.service.DeviceNtpService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.MQAssignmentService;
import com.machloop.fpc.manager.cms.service.RegistryHeartbeatService;
import com.machloop.fpc.manager.cms.service.SendupMessageService;
import com.machloop.fpc.manager.helper.GrpcClientHelper;
import com.machloop.fpc.manager.helper.RocketMQHelper;
import com.machloop.fpc.manager.system.service.CentralService;

/**
 * @author liyongjun
 *
 * create at 2019年12月4日, fpc-manager
 */
@Service
public class CentralServiceImpl implements CentralService {

  private static final int CMS_SETTING_SIZE = 3;

  @Value("${rocketmq.broker.file-reserved-time}")
  private int reservedTime;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private GlobalSettingDao globalSettingDao;

  @Autowired
  private RegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private SendupMessageService sendupMessageService;

  @Autowired
  private MQAssignmentService assignmentService;

  @Autowired
  private DeviceNtpService deviceNtpService;

  @Autowired
  private GrpcClientHelper grpcClientHelper;

  @Autowired
  private RocketMQHelper rocketMQHelper;

  /**
   * @see com.machloop.fpc.manager.system.service.CentralService#queryCmsSetting()
   */
  @Override
  public Map<String, Object> queryCmsSetting() {

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(CMS_SETTING_SIZE);
    resultMap.put("cmsIp", globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP));

    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
        globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_TOKEN));
    resultMap.put("cmsToken", cmsToken);

    String cmsState = globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE);
    resultMap.put("state", cmsState);

    String connectStatus = FpcCmsConstants.CONNECT_STATUS_ABNORMAL;
    if (registryHeartbeatService.isAlive()) {
      connectStatus = FpcCmsConstants.CONNECT_STATUS_NORMAL;
    }
    resultMap.put("connectStatus", connectStatus);
    return resultMap;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.CentralService#updateCmsSetting(java.lang.String, java.lang.String)
   */
  @Override
  public void updateCmsSetting(String cmsIp, String state) {

    // 校验cmsIp格式是否正确
    if (!NetworkUtils.isInetAddress(cmsIp)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "cmsIp格式有误");
    }

    globalSettingService.setValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE, state);
    globalSettingService.setValue(ManagerConstants.GLOBAL_SETTING_CMS_TOKEN,
        KeyEncUtils.encrypt(HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
            HotPropertiesHelper.getProperty(FpcCmsConstants.CMS_TOKEN)));

    if (StringUtils.equals(state, Constants.BOOL_YES)) {
      // 尝试连接
      registryHeartbeatService.setCmsIp(cmsIp);
      grpcClientHelper.reconnectGrpcServer();

      // 尝试注册
      Map<String, String> registerMap = registryHeartbeatService.register();
      String serialNumber = registerMap.get("serialNumber");
      if (StringUtils.isBlank(serialNumber)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR,
            "注册失败:" + registerMap.getOrDefault("detail", "请求超时"));
      }

      // 获取上次注册成功的cmsIp相关信息
      GlobalSettingDO globalSettingDO = globalSettingDao
          .getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP);
      long timeGap = DateUtils.now().getTime() - globalSettingDO.getUpdateTime().getTime();

      // 注册成功后，将cmsIp存入数据库中
      globalSettingService.setValue(ManagerConstants.GLOBAL_SETTING_CMS_IP, cmsIp);

      // 上次注册的设备非本设备，清除全部配置
      boolean onlyLocal = true;
      if (StringUtils.isNotBlank(globalSettingDO.getSettingValue())
          && (!StringUtils.equals(cmsIp, globalSettingDO.getSettingValue())
              || timeGap > reservedTime * Constants.ONE_HOUR_SECONDS * 1000L)) {
        onlyLocal = false;
      }

      // 清除本地多实例配置
      assignmentService.clearLocalConfiguration(onlyLocal,
          new Date(Long.parseLong(registerMap.get("registerTime"))));

      // 注册mq连接
      rocketMQHelper.initCmsRocketMQ();

      // 配置ntp服务器为上级cms
      DeviceNtpBO deviceNtpBO = new DeviceNtpBO();
      deviceNtpBO.setNtpEnabled(true);
      deviceNtpBO.setNtpServer(cmsIp);
      Map<String, Object> updateMsg = deviceNtpService.updateLocalDeviceNtp(deviceNtpBO);
      monitorNtpState(MapUtils.getString(updateMsg, "id"));

      // 上报全量应用配置
      sendupMessageService.sendAllApplianceMessage(serialNumber, DateUtils.now());
    } else {
      grpcClientHelper.shutdownNow();
      rocketMQHelper.shutdown();
    }
  }

  private void monitorNtpState(String id) {
    if (StringUtils.isBlank(id)) {
      return;
    }

    new Thread() {

      @Override
      public void run() {
        String state = "running";
        while (StringUtils.equals(state, "running")) {
          state = deviceNtpService.queryStateById(id);

          // 配置结束后记录系统日志
          if (!StringUtils.equals(state, "running")) {
            DeviceNtpBO deviceNtpBO = deviceNtpService.queryDeviceNtp();

            String logContent = "";
            if (StringUtils.equals(state, "success")) {
              logContent = deviceNtpBO.isNtpEnabled()
                  ? String.format("与NTP服务器[%s]时间同步成功，当前时间：[%s]", deviceNtpBO.getNtpServer(),
                      deviceNtpBO.getDateTime())
                  : String.format("时间设置成功，当前时区：[%s]，当前时间：[%s]", deviceNtpBO.getTimeZone(),
                      deviceNtpBO.getDateTime());
            } else {
              logContent = StringUtils.substringAfter(state, "fail:");
            }

            if (StringUtils.isNotBlank(logContent)) {
              LogHelper.systemRuning(StringUtils.equals(state, "success") ? LogHelper.LEVEL_NOTICE
                  : LogHelper.LEVEL_WARN, logContent, "system");
            }
          }
        }
      }

    }.start();

  }

}
