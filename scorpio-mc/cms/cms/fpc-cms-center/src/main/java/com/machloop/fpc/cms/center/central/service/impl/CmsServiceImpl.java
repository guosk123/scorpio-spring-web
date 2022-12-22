package com.machloop.fpc.cms.center.central.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.central.bo.CentralSystemBO;
import com.machloop.fpc.cms.center.central.bo.CmsBO;
import com.machloop.fpc.cms.center.central.dao.CmsDao;
import com.machloop.fpc.cms.center.central.data.CmsDO;
import com.machloop.fpc.cms.center.central.service.CentralDiskService;
import com.machloop.fpc.cms.center.central.service.CentralNetifService;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;
import com.machloop.fpc.cms.center.central.service.CmsService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2021年12月15日, fpc-cms-center
 */
@Service
public class CmsServiceImpl implements CmsService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CmsServiceImpl.class);

  private static final String FPC_CENTER_SERVLET_CONTEXT = "center";

  @Autowired
  private CmsDao cmsDao;

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private LogDao logDao;

  @Autowired
  private CentralDiskService centralDiskService;

  @Autowired
  private CentralNetifService centralNetifService;

  @Autowired
  private DeviceStatusService deviceStatusService;

  @Autowired
  private CentralSystemService centralSystemService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private SensorNetworkService sensorNetworkService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#queryCms(com.machloop.fpc.cms.center.central.vo.CmsQueryVO)
   */
  @Override
  public List<CmsBO> queryCms(CmsQueryVO query) {
    Map<String,
        String> licenseStateDict = dictManager.getBaseDict().getItemMap("device_license_state");
    Map<String,
        String> connectStateDict = dictManager.getBaseDict().getItemMap("device_connect_state");

    List<CmsDO> cmsDOList = cmsDao.queryCms(query);
    List<CmsBO> cmsBOList = Lists.newArrayListWithCapacity(cmsDOList.size());
    for (CmsDO cmsDO : cmsDOList) {
      CmsBO cmsBO = new CmsBO();
      BeanUtils.copyProperties(cmsDO, cmsBO);

      // 解密appToken
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getAppToken());
      cmsBO.setAppToken(appToken);

      // 解密cmsToken
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getCmsToken());
      cmsBO.setCmsToken(cmsToken);

      // 获取设备的连接状态、license状态、交互时延
      DeviceStatusDO cmsStatusDO = deviceStatusService
          .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_CMS, cmsDO.getSerialNumber());
      cmsBO.setConnectStatus(cmsStatusDO.getCurrentConnectStatus());
      cmsBO.setLicenseStatus(
          StringUtils.equals(FpcCmsConstants.CONNECT_STATUS_ABNORMAL, cmsBO.getConnectStatus())
              ? FpcCmsConstants.LICENSE_ABNORMALITY
              : cmsStatusDO.getLicenseState());
      cmsBO.setLastLoginTime(DateUtils.toStringISO8601(cmsStatusDO.getLastLoginTime()));
      cmsBO.setLastInteractiveTime(DateUtils.toStringISO8601(cmsStatusDO.getLastInteractiveTime()));
      cmsBO.setLastInteractiveLatency(cmsStatusDO.getLastInteractiveLatency());

      // 设备运行时长
      cmsBO.setUpTime(cmsStatusDO.getUpTime());

      // 设备最新状态
      CentralSystemBO centralSystemBO = centralSystemService
          .queryCentralSystem(FpcCmsConstants.DEVICE_TYPE_CMS, cmsDO.getSerialNumber());
      cmsBO.setCpuMetric(centralSystemBO.getCpuMetric());
      cmsBO.setMemoryMetric(centralSystemBO.getMemoryMetric());
      cmsBO.setSystemFsMetric(centralSystemBO.getSystemFsMetric());

      // 页面查询时如果不搜索条件或者搜索的条件与当前设备状态匹配则加入返回列表
      if (StringUtils.equalsAny(query.getConnectStatus(), null, "", cmsBO.getConnectStatus())
          && StringUtils.equalsAny(query.getLicenseStatus(), null, "", cmsBO.getLicenseStatus())) {
        cmsBO.setConnectStatusText(
            MapUtils.getString(connectStateDict, cmsBO.getConnectStatus(), ""));
        cmsBO.setLicenseStatusText(
            MapUtils.getString(licenseStateDict, cmsBO.getLicenseStatus(), ""));
        cmsBO.setCreateTime(DateUtils.toStringISO8601(cmsDO.getCreateTime()));

        cmsBOList.add(cmsBO);
      }
    }

    return cmsBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#queryCmsBySerialNumbers(java.util.List, boolean)
   */
  @Override
  public List<CmsBO> queryCmsBySerialNumbers(List<String> serialNumbers, boolean isSimple) {
    Map<String,
        String> connectStateDict = dictManager.getBaseDict().getItemMap("device_connect_state");

    if (CollectionUtils.isEmpty(serialNumbers)) {
      return Lists.newArrayList();
    }

    List<CmsDO> cmsDOList = cmsDao.queryCmsBySerialNumbers(serialNumbers);

    List<CmsBO> cmsBOList = Lists.newArrayListWithExpectedSize(cmsDOList.size());
    for (CmsDO cmsDO : cmsDOList) {
      CmsBO cmsBO = new CmsBO();
      BeanUtils.copyProperties(cmsDO, cmsBO);
      cmsBO.setCreateTime(DateUtils.toStringISO8601(cmsDO.getCreateTime()));

      // 解密appToken
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getAppToken());
      cmsBO.setAppToken(appToken);

      // 解密cmsToken
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getCmsToken());
      cmsBO.setCmsToken(cmsToken);

      // 判断是否仅需要返回基础信息
      if (!isSimple) {
        DeviceStatusDO fpcStatusDO = deviceStatusService
            .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_TFA, cmsDO.getSerialNumber());
        cmsBO.setConnectStatus(fpcStatusDO.getCurrentConnectStatus());
        cmsBO.setConnectStatusText(
            MapUtils.getString(connectStateDict, cmsBO.getConnectStatus(), ""));
      }
      cmsBOList.add(cmsBO);
    }

    return cmsBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#queryCmsBySuperior(java.lang.String)
   */
  @Override
  public List<CmsBO> queryCmsBySuperior(String superiorCmsSerialNumber) {
    List<CmsDO> cmsDOlist = cmsDao.queryCmsBySuperior(superiorCmsSerialNumber);

    List<CmsBO> cmsBOList = Lists.newArrayListWithExpectedSize(cmsDOlist.size());
    for (CmsDO cmsDO : cmsDOlist) {
      CmsBO cmsBO = new CmsBO();
      BeanUtils.copyProperties(cmsDO, cmsBO);
      cmsBO.setCreateTime(DateUtils.toStringISO8601(cmsDO.getCreateTime()));

      // 解密appToken
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getAppToken());
      cmsBO.setAppToken(appToken);

      // 解密cmsToken
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getCmsToken());
      cmsBO.setCmsToken(cmsToken);

      cmsBOList.add(cmsBO);
    }

    return cmsBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#queryMaxCmsHierarchy(java.lang.String)
   */
  @Override
  public int queryMaxCmsHierarchy(String serialNumber) {
    Map<String, List<CmsDO>> cmsHierarchy = cmsDao.queryCms(new CmsQueryVO()).stream()
        .collect(Collectors.groupingBy(CmsDO::getSuperiorCmsSerialNumber));
    return iterateSubordinateCms(Lists.newArrayList(serialNumber), cmsHierarchy);
  }

  private int iterateSubordinateCms(List<String> outsets, Map<String, List<CmsDO>> cmsHierarchy) {
    int i = 1;

    List<String> list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (String outset : outsets) {
      List<CmsDO> temp = cmsHierarchy.get(outset);
      if (CollectionUtils.isNotEmpty(temp)) {
        list.addAll(temp.stream().map(CmsDO::getSerialNumber).collect(Collectors.toList()));
      }
    }

    if (CollectionUtils.isNotEmpty(list)) {
      i += iterateSubordinateCms(list, cmsHierarchy);
    } else {
      return i;
    }

    return i;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#queryCmsById(java.lang.String)
   */
  @Override
  public CmsBO queryCmsById(String id) {
    Map<String,
        String> licenseStateDict = dictManager.getBaseDict().getItemMap("device_license_state");
    Map<String,
        String> connectStateDict = dictManager.getBaseDict().getItemMap("device_connect_state");

    CmsDO cmsDO = cmsDao.queryCmsById(id);
    if (StringUtils.isBlank(cmsDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备不存在");
    }

    CmsBO cmsBO = new CmsBO();
    BeanUtils.copyProperties(cmsDO, cmsBO);

    // 解密appToken
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getAppToken());
    cmsBO.setAppToken(appToken);

    // 解密cmsToken
    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getCmsToken());
    cmsBO.setCmsToken(cmsToken);

    // 获取设备的连接状态、license状态、交互时延
    DeviceStatusDO fpcStatusDO = deviceStatusService
        .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_TFA, cmsDO.getSerialNumber());
    cmsBO.setConnectStatus(fpcStatusDO.getCurrentConnectStatus());
    cmsBO.setLicenseStatus(
        StringUtils.equals(FpcCmsConstants.CONNECT_STATUS_ABNORMAL, cmsBO.getConnectStatus())
            ? FpcCmsConstants.LICENSE_ABNORMALITY
            : fpcStatusDO.getLicenseState());
    cmsBO.setLastInteractiveTime(DateUtils.toStringISO8601(fpcStatusDO.getLastInteractiveTime()));
    cmsBO.setLastInteractiveLatency(fpcStatusDO.getLastInteractiveLatency());

    // 设备运行时长
    cmsBO.setUpTime(fpcStatusDO.getUpTime());

    // 设备最新状态
    CentralSystemBO centralSystemBO = centralSystemService
        .queryCentralSystem(FpcCmsConstants.DEVICE_TYPE_CMS, cmsDO.getSerialNumber());
    BeanUtils.copyProperties(centralSystemBO, cmsBO);

    cmsBO.setConnectStatusText(MapUtils.getString(connectStateDict, cmsBO.getConnectStatus(), ""));
    cmsBO.setLicenseStatusText(MapUtils.getString(licenseStateDict, cmsBO.getLicenseStatus(), ""));
    cmsBO.setCreateTime(DateUtils.toStringISO8601(cmsDO.getCreateTime()));

    return cmsBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#queryCmsByIp(java.lang.String)
   */
  @Override
  public CmsBO queryCmsByIp(String ip) {
    CmsDO cmsDO = cmsDao.queryCmsByIpOrName(ip, null);

    CmsBO cmsBO = new CmsBO();
    BeanUtils.copyProperties(cmsDO, cmsBO);

    // 解密appToken
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getAppToken());
    cmsBO.setAppToken(appToken);

    // 解密cmsToken
    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getCmsToken());
    cmsBO.setCmsToken(cmsToken);

    return cmsBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#queryCmsBySerialNumber(java.lang.String)
   */
  @Override
  public CmsBO queryCmsBySerialNumber(String serialNumber) {
    CmsDO cmsDO = cmsDao.queryCmsBySerialNumber(serialNumber);

    CmsBO cmsBO = new CmsBO();
    BeanUtils.copyProperties(cmsDO, cmsBO);

    // 解密appToken
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getAppToken());
    cmsBO.setAppToken(appToken);

    // 解密cmsToken
    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getCmsToken());
    cmsBO.setCmsToken(cmsToken);

    return cmsBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#queryCmsLoginUrl(java.lang.String)
   */
  @Override
  public String queryCmsLoginUrl(String serialNumber) {
    String allowLoginDevice = globalSettingService
        .getValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_LOGIN_LIST);
    if (!allowLoginDevice.contains(serialNumber) && !StringUtils.equals(allowLoginDevice, "ALL")) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "当前设备未允许远程登录，请登录系统管理员进行配置");
    }

    String ssoPlatformId = globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_ID);
    String secret = HotPropertiesHelper.getProperty(FpcCmsConstants.CMS_TOKEN);
    String ssoPlatformUserId = globalSettingService
        .getValue(CenterConstants.GLOBAL_SETTING_DEVICE_SSO_PLATFORM_USER_ID);

    String token = "";
    try {
      Date now = new Date();
      Date expire = DateUtils.afterSecondDate(now, Constants.ONE_HOUR_SECONDS);

      Algorithm algorithm = Algorithm.HMAC256(secret);
      token = JWT.create().withClaim("platform_id", ssoPlatformId)
          .withClaim("platform_user_id", ssoPlatformUserId).withExpiresAt(expire).withIssuedAt(now)
          .withNotBefore(now).sign(algorithm);
    } catch (IllegalArgumentException | JWTCreationException e) {
      LOGGER.warn("build sso token failed.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统出现异常");
    }

    CmsDO cmsDO = cmsDao.queryCmsBySerialNumber(serialNumber);
    if (StringUtils.isBlank(cmsDO.getIp())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "未获取到当前设备IP");
    }

    StringBuilder ssoLoginUrl = new StringBuilder();
    ssoLoginUrl.append(HotPropertiesHelper.getProperty("cms.center.server.protocol")).append("://");
    ssoLoginUrl.append(
        HotPropertiesHelper.getProperty("sso.login.api").replace("{deviceIp}", cmsDO.getIp())
            .replace("{devicePort}", HotPropertiesHelper.getProperty("cms.center.server.port"))
            .replace("{servletContext}", FPC_CENTER_SERVLET_CONTEXT).replace("{jwt}", token));

    return ssoLoginUrl.toString();
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#saveCms(com.machloop.fpc.cms.center.central.bo.CmsBO)
   */
  @Override
  public CmsBO saveCms(CmsBO cmsBO) {
    CmsDO cmsDO = new CmsDO();
    BeanUtils.copyProperties(cmsBO, cmsDO);
    cmsDO.setReportState(Constants.BOOL_NO);
    cmsDO.setReportAction(FpcCmsConstants.SYNC_ACTION_ADD);

    // 加密appToken
    cmsDO.setAppToken(KeyEncUtils.encrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsBO.getAppToken()));

    // 加密cmsToken
    cmsDO.setCmsToken(KeyEncUtils.encrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsBO.getCmsToken()));

    cmsDO = cmsDao.saveCms(cmsDO);

    return queryCmsById(cmsDO.getId());
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#updateCmsStatus(com.machloop.fpc.cms.center.central.bo.CmsBO)
   */
  @Override
  public void updateCmsStatus(CmsBO cmsBO) {
    CmsDO cmsDO = new CmsDO();
    BeanUtils.copyProperties(cmsBO, cmsDO);
    cmsDO.setReportState(Constants.BOOL_NO);
    cmsDO.setReportAction(FpcCmsConstants.SYNC_ACTION_MODIFY);

    // 加密appToken
    cmsDO.setAppToken(KeyEncUtils.encrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsBO.getAppToken()));

    cmsDao.updateCmsStatus(cmsDO);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#deleteCms(java.lang.String, java.lang.String)
   */
  @Override
  public CmsBO deleteCms(String id, String operatorId) {
    CmsBO cmsBO = queryCmsById(id);
    String cmsSerialNumber = cmsBO.getSerialNumber();

    if (StringUtils.isBlank(cmsSerialNumber)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备不存在");
    }

    // 判断该CMS所包含探针网络是否被应用
    List<String> fpcNetworkIds = fpcNetworkService
        .queryNetworks(FpcCmsConstants.DEVICE_TYPE_CMS, cmsSerialNumber).stream()
        .map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
    List<String> usedNetworkIds = sensorNetworkService.querySensorNetworks().stream()
        .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList());
    if (CollectionUtils.containsAny(usedNetworkIds, fpcNetworkIds)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "该设备所管理的下级探针设备内网络已经加入到探针网络，无法删除");
    }

    // 删除设备
    cmsDao.deleteCms(id, operatorId);

    // 删除设备上报的所有下级CMS设备
    Map<String, List<CmsDO>> cmsHierarchy = cmsDao.queryCms(new CmsQueryVO()).stream()
        .collect(Collectors.groupingBy(CmsDO::getSuperiorCmsSerialNumber));
    List<String> subordinateCms = iterateSubordinateCms(cmsSerialNumber, cmsHierarchy);
    cmsDao.deleteCmsBySerialNumbers(subordinateCms, operatorId);
    // 删除设备上报的所有下级TFA设备
    subordinateCms.forEach(omeCmsSerialNumber -> {
      fpcService.queryFpcByCms(omeCmsSerialNumber, false)
          .forEach(fpc -> fpcService.deleteFpc(fpc.getId(), operatorId));
    });
    // 清除设备相关的日志
    logDao.deleteLogsByNodeId(cmsSerialNumber);
    // 清除设备相关的告警
    alarmDao.deleteAlarmsByNodeId(cmsSerialNumber);
    // 清除设备相关的磁盘
    centralDiskService.deleteCentralDisk(FpcCmsConstants.DEVICE_TYPE_CMS, cmsSerialNumber);
    // 清除设备相关的系统状态
    centralSystemService.deleteCentralSystem(FpcCmsConstants.DEVICE_TYPE_CMS, cmsSerialNumber);
    // 清除设备相关的接口状态
    centralNetifService.deleteCentralNetifs(FpcCmsConstants.DEVICE_TYPE_CMS, cmsSerialNumber);

    return cmsBO;
  }

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

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#deleteCmsBySerialNumbers(java.util.List, java.lang.String)
   */
  @Override
  public void deleteCmsBySerialNumbers(List<String> serialNumbers, String operatorId) {
    cmsDao.deleteCmsBySerialNumbers(serialNumbers, operatorId);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#exportCmsMessage()
   */
  @Override
  public String exportCmsMessage() {
    List<CmsDO> cmsList = cmsDao.queryCms(new CmsQueryVO());

    StringBuilder content = new StringBuilder(
        "`名称`,`版本`,`IP`,`序列号`,`appKey`,`appToken`,`cmsToken`,`备注`\n");
    for (CmsDO cmsDO : cmsList) {
      // 解密appToken
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getAppToken());

      // 解密cmsToken
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), cmsDO.getCmsToken());

      String rowContent = CsvUtils.spliceRowData(cmsDO.getName(), cmsDO.getVersion(), cmsDO.getIp(),
          cmsDO.getSerialNumber(), cmsDO.getAppKey(), appToken, cmsToken, cmsDO.getDescription());
      content.append(rowContent);
    }

    return content.toString();
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CmsService#exportCmsSerialNumber()
   */
  @Override
  public String exportCmsSerialNumber() {
    List<CmsDO> cmsList = cmsDao.queryCms(new CmsQueryVO());

    StringBuilder content = new StringBuilder();
    for (CmsDO cmsDO : cmsList) {
      if (StringUtils.isNotBlank(cmsDO.getSerialNumber())) {
        content.append(cmsDO.getSerialNumber()).append("\n");
      }
    }

    return content.toString();
  }

}
