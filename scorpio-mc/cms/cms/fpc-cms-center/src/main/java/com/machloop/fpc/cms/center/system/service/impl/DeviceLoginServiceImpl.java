package com.machloop.fpc.cms.center.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.dao.PlatformDao;
import com.machloop.alpha.webapp.system.dao.PlatformUserDao;
import com.machloop.alpha.webapp.system.dao.UserDao;
import com.machloop.alpha.webapp.system.data.PlatformDO;
import com.machloop.alpha.webapp.system.data.PlatformUserDO;
import com.machloop.alpha.webapp.system.data.UserDO;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.central.dao.CmsDao;
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.data.FpcDO;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.system.bo.DeviceLoginSettingBO;
import com.machloop.fpc.cms.center.system.service.DeviceLoginService;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年11月8日, fpc-cms-center
 */
@Service
public class DeviceLoginServiceImpl
    implements DeviceLoginService, MQAssignmentService, SyncConfigurationService {

  static final Logger LOGGER = LoggerFactory.getLogger(DeviceLoginServiceImpl.class);

  private static final String ALL_DEVICE = "ALL";
  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_SSO);

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private CmsDao cmsDao;

  @Autowired
  private UserDao userDao;

  @Autowired
  private PlatformDao platformDao;

  @Autowired
  private PlatformUserDao platformUserDao;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private ApplicationContext context;

  /**
   * @see com.machloop.fpc.cms.center.system.service.DeviceLoginService#queryDeviceLoginSettings()
   */
  @Override
  public Map<String, Object> queryDeviceLoginSettings() {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    result.put("userId", globalSettingService
        .getValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_PLATFORM_USER_ID, false));
    result.put("deviceSerialNumbers",
        globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_LOGIN_LIST, false));
    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.system.service.DeviceLoginService#updateDeviceLoginSettings(com.machloop.fpc.cms.center.system.bo.DeviceLoginSettingBO)
   */
  @Override
  public void updateDeviceLoginSettings(DeviceLoginSettingBO deviceLoginSetting) {
    String userId = deviceLoginSetting.getUserId();
    UserDO user = userDao.queryUserById(userId);
    if (StringUtils.isBlank(user.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "用户不存在");
    }

    String deviceSerialNumber = deviceLoginSetting.getDeviceSerialNumbers();
    if (!StringUtils.equalsIgnoreCase(deviceSerialNumber, ALL_DEVICE)) {
      List<String> deviceSerialNumberList = CsvUtils.convertCSVToList(deviceSerialNumber);
      List<FpcDO> fpcList = fpcDao.queryFpcsBySerialNumbers(deviceSerialNumberList);
      List<CmsDO> cmsList = cmsDao.queryCmsBySerialNumbers(deviceSerialNumberList);
      List<String> vaildDeviceSerialNumbers = fpcList.stream().map(FpcDO::getSerialNumber)
          .collect(Collectors.toList());
      vaildDeviceSerialNumbers
          .addAll(cmsList.stream().map(CmsDO::getSerialNumber).collect(Collectors.toList()));

      deviceSerialNumberList.removeAll(vaildDeviceSerialNumbers);
      if (!deviceSerialNumberList.isEmpty()) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND,
            String.format("探针设备 %s 不存在", deviceSerialNumberList));
      }
    }

    globalSettingService.setValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_PLATFORM_USER_ID,
        userId);
    globalSettingService.setValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_LOGIN_LIST,
        deviceSerialNumber);

    // 下发单点登录配置
    Map<String, Object> ssoConfiguration = getSsoConfiguration();
    ssoConfiguration.put("vaildDeviceSerialNumbers", deviceSerialNumber);

    assignmentConfiguration(Lists.newArrayList(ssoConfiguration),
        FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_SSO, null);
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
    String deviceSerialNumbers = globalSettingService
        .getValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_LOGIN_LIST, false);

    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serialNumber) || StringUtils.equals(deviceSerialNumbers, ALL_DEVICE)
        || deviceSerialNumbers.contains(serialNumber)) {
      map.put(FpcCmsConstants.MQ_TAG_SSO,
          Lists.newArrayList(DigestUtils.md5Hex(JsonHelper.serialize(getSsoConfiguration()))));
    } else {
      map.put(FpcCmsConstants.MQ_TAG_SSO, Lists.newArrayList());
    }

    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurations(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {
    String deviceSerialNumbers = globalSettingService
        .getValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_LOGIN_LIST, false);

    List<Map<String, Object>> list = Lists.newArrayListWithCapacity(1);
    if (StringUtils.isBlank(serialNumber) || StringUtils.equals(deviceSerialNumbers, ALL_DEVICE)
        || deviceSerialNumbers.contains(serialNumber)) {
      Map<String, Object> ssoConfiguration = getSsoConfiguration();
      ssoConfiguration.put("vaildDeviceSerialNumbers", serialNumber);
      list.add(ssoConfiguration);
    }

    return Tuples.of(true, list, MQMessageHelper.EMPTY);
  }

  private Map<String, Object> getSsoConfiguration() {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("deviceId",
        globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_ID, false));
    map.put("deviceName",
        globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME, false));
    map.put("token", HotPropertiesHelper.getProperty(FpcCmsConstants.CMS_TOKEN));
    map.put("userId", globalSettingService
        .getValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_PLATFORM_USER_ID, false));

    return map;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_SSO));
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#syncConfiguration(org.apache.rocketmq.common.message.Message)
   */
  @Override
  public int syncConfiguration(Message message) {
    Map<String, Object> messageBody = MQMessageHelper.convertToMap(message);

    Map<String, Object> config = messageBody;
    if (MapUtils.getBoolean(messageBody, "batch", false)) {
      List<Map<String, Object>> list = JsonHelper.deserialize(
          JsonHelper.serialize(messageBody.get("data")),
          new TypeReference<List<Map<String, Object>>>() {
          });
      config = CollectionUtils.isEmpty(list) ? null : list.get(0);
    }

    if (MapUtils.isEmpty(config)) {
      return 0;
    }

    // 继续向下发送
    assignmentConfiguration(Lists.newArrayList(config), FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SSO, null);

    // 判断本次下发单点登录配置是否对当前设备生效
    String vaildDeviceSerialNumbers = MapUtils.getString(config, "vaildDeviceSerialNumbers", "");
    if (!vaildDeviceSerialNumbers.contains(licenseService.queryDeviceSerialNumber())
        && !StringUtils.equalsIgnoreCase(vaildDeviceSerialNumbers, "ALL")) {
      return 0;
    }

    int syncTotalCount = assignmentSsoConfig(config);
    LOGGER.info("current sync sso config total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  public int assignmentSsoConfig(Map<String, Object> config) {
    String deviceId = MapUtils.getString(config, "deviceId");
    String deviceName = MapUtils.getString(config, "deviceName");
    String token = MapUtils.getString(config, "token");
    String userId = MapUtils.getString(config, "userId");

    if (StringUtils.isAnyBlank(deviceId, deviceName, token, userId)) {
      LOGGER.warn("sso login msg is empty, assignment failed. msg:{}", config);
      return 0;
    }

    // 配置外部系统
    token = KeyEncUtils.encrypt(HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
        token);
    PlatformDO platformDO = platformDao.queryPlatformByPlatformId(deviceId);
    if (StringUtils.isNotBlank(platformDO.getId())) {
      platformDO.setName(deviceName);
      platformDO.setAppToken(token);
      platformDO.setOperatorId(CMS_ASSIGNMENT);

      platformDao.updatePlatform(platformDO);
    } else {
      platformDO = new PlatformDO();
      platformDO.setPlatformId(deviceId);
      platformDO.setName(deviceName);
      platformDO.setAppToken(token);
      platformDO.setOperatorId(CMS_ASSIGNMENT);
      platformDO.setDescription(CMS_ASSIGNMENT);

      platformDao.savePlatforms(Lists.newArrayList(platformDO));
    }

    // 配置外部用户
    PlatformUserDO platformUserDO = platformUserDao.queryPlatformUser(platformDO.getId(),
        WebappConstants.ANY_PLATFORM_USER);
    if (StringUtils.isBlank(platformUserDO.getId())) {
      // 删除原有配置
      platformUserDao.deleteBySsoPlatformId(platformDO.getId(), CMS_ASSIGNMENT);
      // 增加下发配置
      List<UserDO> userList = userDao.queryUserByType(WebappConstants.USER_TYPE_INTERNAL_SSO);
      if (CollectionUtils.isNotEmpty(userList)) {
        platformUserDO = new PlatformUserDO();
        platformUserDO.setSsoPlatformId(platformDO.getId());
        platformUserDO.setPlatformUserId(userId);
        platformUserDO.setSystemUserId(userList.get(0).getId());
        platformUserDO.setOperatorId(CMS_ASSIGNMENT);
        platformUserDO.setDescription(CMS_ASSIGNMENT);

        platformUserDao.savePlatformUsers(Lists.newArrayList(platformUserDO));

        return 1;
      } else {
        LOGGER.warn("not found internal sso user.");
        return -1;
      }
    }

    return 0;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    int dropCount = 0;
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_SSO)) {
      String token = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
          globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_TOKEN));

      PlatformDO platformDO = platformDao.queryPlatformByToken(token);
      if (StringUtils.isNotBlank(platformDO.getId())) {
        platformDao.deletePlatform(platformDO.getId(), CMS_ASSIGNMENT);
        dropCount = platformUserDao.deleteBySsoPlatformId(platformDO.getId(), CMS_ASSIGNMENT);
      }
    }

    return dropCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    String token = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
        globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_TOKEN));

    List<String> list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    PlatformDO platformDO = platformDao.queryPlatformByToken(token);
    if (StringUtils.isNotBlank(platformDO.getId())) {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("deviceId", platformDO.getPlatformId());
      map.put("deviceName", platformDO.getName());
      map.put("token",
          KeyEncUtils.decrypt(HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
              platformDO.getAppToken()));
      List<PlatformUserDO> platformUsers = platformUserDao
          .queryPlatformUsers(platformDO.getId(), null).stream()
          .filter(item -> StringUtils.equals(item.getOperatorId(), CMS_ASSIGNMENT))
          .collect(Collectors.toList());
      map.put("userId", platformUsers.get(0).getPlatformUserId());

      list.add(DigestUtils.md5Hex(JsonHelper.serialize(map)));
    }

    return list;
  }

}
