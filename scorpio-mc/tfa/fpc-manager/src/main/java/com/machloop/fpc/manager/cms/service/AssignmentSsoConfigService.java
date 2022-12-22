package com.machloop.fpc.manager.cms.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
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
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.manager.system.service.LicenseService;

/**
 * @author guosk
 *
 * create at 2021年11月9日, fpc-manager
 */
@Service
public class AssignmentSsoConfigService implements SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentSsoConfigService.class);

  @Autowired
  private PlatformDao platformDao;

  @Autowired
  private PlatformUserDao platformUserDao;

  @Autowired
  private UserDao userDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private LicenseService licenseService;

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_SSO));
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#syncConfiguration(org.apache.rocketmq.common.message.Message)
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

  private int assignmentSsoConfig(Map<String, Object> config) {
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
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date, boolean)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    int dropCount = 0;
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_SSO)) {
      String token = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET),
          globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_TOKEN));

      PlatformDO platformDO = platformDao.queryPlatformByToken(token);
      if (StringUtils.isNotBlank(platformDO.getId())) {
        platformDao.deletePlatform(platformDO.getId(), CMS_ASSIGNMENT);
        dropCount = platformUserDao.deleteBySsoPlatformId(platformDO.getId(), CMS_ASSIGNMENT);
      }
    }

    return dropCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {

    String token = globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_TOKEN);
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
