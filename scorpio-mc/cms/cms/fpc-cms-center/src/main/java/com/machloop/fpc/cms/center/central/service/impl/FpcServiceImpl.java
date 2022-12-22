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
import org.springframework.transaction.annotation.Transactional;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.KeyEncUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.central.bo.CentralDeviceBO;
import com.machloop.fpc.cms.center.central.bo.CentralSystemBO;
import com.machloop.fpc.cms.center.central.bo.CmsBO;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.dao.FpcDao;
import com.machloop.fpc.cms.center.central.data.FpcDO;
import com.machloop.fpc.cms.center.central.service.CentralDiskService;
import com.machloop.fpc.cms.center.central.service.CentralNetifService;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;
import com.machloop.fpc.cms.center.central.service.CmsService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;
import com.machloop.fpc.cms.center.helper.ClickhouseRemoteServerHelper;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Service
public class FpcServiceImpl implements FpcService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FpcServiceImpl.class);

  private static final String CURRENT_CMS = "本机";
  private static final String NEXT_HIERARCHY_CMS = "下级";

  private static final String FPC_MANAGER_SERVLET_CONTEXT = "manager";

  @Autowired
  private FpcDao fpcDao;

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private LogDao logDao;

  @Autowired
  private CentralDiskService centralDiskService;

  @Autowired
  private CentralSystemService centralSystemService;

  @Autowired
  private CentralNetifService centralNetifService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private SensorNetworkService sensorNetworkService;

  @Autowired
  private CmsService cmsService;

  @Autowired
  private DeviceStatusService deviceStatusService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryFpcs(com.machloop.fpc.cms.center.central.vo.FpcQueryBO)
   */
  @Override
  public List<FpcBO> queryFpcs(FpcQueryVO query) {
    Map<String,
        String> licenseStateDict = dictManager.getBaseDict().getItemMap("device_license_state");
    Map<String,
        String> connectStateDict = dictManager.getBaseDict().getItemMap("device_connect_state");

    // 本机设备序列号
    String serialNumber = licenseService.queryDeviceSerialNumber();

    List<FpcDO> fpcDOList = fpcDao.queryFpcs(query);
    List<FpcBO> fpcBOList = Lists.newArrayListWithCapacity(fpcDOList.size());
    for (FpcDO fpcDO : fpcDOList) {
      FpcBO fpcBO = new FpcBO();
      BeanUtils.copyProperties(fpcDO, fpcBO);
      fpcBO.setCmsName(StringUtils.equals(fpcDO.getCmsSerialNumber(), serialNumber) ? CURRENT_CMS
          : NEXT_HIERARCHY_CMS);

      // 解密appToken
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
      fpcBO.setAppToken(appToken);

      // 解密cmsToken
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getCmsToken());
      fpcBO.setCmsToken(cmsToken);

      // 获取设备的连接状态、license状态、交互时延
      DeviceStatusDO fpcStatusDO = deviceStatusService
          .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_TFA, fpcDO.getSerialNumber());
      fpcBO.setConnectStatus(fpcStatusDO.getCurrentConnectStatus());
      fpcBO.setLicenseStatus(
          StringUtils.equals(FpcCmsConstants.CONNECT_STATUS_ABNORMAL, fpcBO.getConnectStatus())
              ? FpcCmsConstants.LICENSE_ABNORMALITY
              : fpcStatusDO.getLicenseState());
      fpcBO.setLastLoginTime(DateUtils.toStringISO8601(fpcStatusDO.getLastLoginTime()));
      fpcBO.setLastInteractiveTime(DateUtils.toStringISO8601(fpcStatusDO.getLastInteractiveTime()));
      fpcBO.setLastInteractiveLatency(fpcStatusDO.getLastInteractiveLatency());

      // 设备运行时长
      fpcBO.setUpTime(fpcStatusDO.getUpTime());

      // 设备最新状态
      CentralSystemBO centralSystemBO = centralSystemService
          .queryCentralSystem(FpcCmsConstants.DEVICE_TYPE_TFA, fpcDO.getSerialNumber());
      fpcBO.setCpuMetric(centralSystemBO.getCpuMetric());
      fpcBO.setMemoryMetric(centralSystemBO.getMemoryMetric());
      fpcBO.setSystemFsMetric(centralSystemBO.getSystemFsMetric());
      fpcBO.setIndexFsMetric(centralSystemBO.getIndexFsMetric());
      fpcBO.setMetadataFsMetric(centralSystemBO.getMetadataFsMetric());
      fpcBO.setMetadataHotFsMetric(centralSystemBO.getMetadataHotFsMetric());
      fpcBO.setPacketFsMetric(centralSystemBO.getPacketFsMetric());

      AlarmQueryVO alarmQuery = new AlarmQueryVO();
      alarmQuery.setNodeId(fpcDO.getSerialNumber());
      int alarmCount = alarmDao.queryAlarmsWithoutPage(alarmQuery).size();
      fpcBO.setAlarmCount(alarmCount);

      // 页面查询时如果不搜索条件或者搜索的条件与当前设备状态匹配则加入返回列表
      if (StringUtils.equalsAny(query.getConnectStatus(), null, "", fpcBO.getConnectStatus())
          && StringUtils.equalsAny(query.getLicenseStatus(), null, "", fpcBO.getLicenseStatus())) {
        fpcBO.setRaidList(centralDiskService.queryCentralRaids(FpcCmsConstants.DEVICE_TYPE_TFA,
            fpcDO.getSerialNumber()));
        fpcBO.setConnectStatusText(
            MapUtils.getString(connectStateDict, fpcBO.getConnectStatus(), ""));
        fpcBO.setLicenseStatusText(
            MapUtils.getString(licenseStateDict, fpcBO.getLicenseStatus(), ""));
        fpcBO.setCreateTime(DateUtils.toStringISO8601(fpcDO.getCreateTime()));
        fpcBO.setNetifList(centralNetifService.queryCentralNetifProfiles(fpcDO.getSerialNumber(),
            FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT,
            FpcCmsConstants.DEVICE_NETIF_CATEGORY_INGEST,
            FpcCmsConstants.DEVICE_NETIF_CATEGORY_TRANSMIT));

        fpcBOList.add(fpcBO);
      }
    }

    return fpcBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryAllFpc()
   */
  @Override
  public List<FpcBO> queryAllFpc() {
    List<FpcDO> fpcDOList = fpcDao.queryFpcs(new FpcQueryVO());

    return fpcDOList.stream().map(fpcDO -> {
      FpcBO fpcBO = new FpcBO();
      BeanUtils.copyProperties(fpcDO, fpcBO);

      // 获取设备的连接状态、license状态、交互时延
      DeviceStatusDO fpcStatusDO = deviceStatusService
          .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_TFA, fpcDO.getSerialNumber());
      fpcBO.setConnectStatus(fpcStatusDO.getCurrentConnectStatus());

      return fpcBO;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryFpcBySerialNumbers(java.util.List, boolean)
   */
  @Override
  public List<FpcBO> queryFpcBySerialNumbers(List<String> serialNumbers, boolean isSimple) {
    Map<String,
        String> connectStateDict = dictManager.getBaseDict().getItemMap("device_connect_state");

    if (CollectionUtils.isEmpty(serialNumbers)) {
      return Lists.newArrayList();
    }

    List<FpcDO> fpcDOList = fpcDao.queryFpcsBySerialNumbers(serialNumbers);

    // 本机设备序列号
    String serialNumber = licenseService.queryDeviceSerialNumber();

    List<FpcBO> fpcBOList = Lists.newArrayListWithExpectedSize(fpcDOList.size());
    for (FpcDO fpcDO : fpcDOList) {
      FpcBO fpcBO = new FpcBO();
      BeanUtils.copyProperties(fpcDO, fpcBO);
      fpcBO.setCmsName(StringUtils.equals(fpcDO.getCmsSerialNumber(), serialNumber) ? CURRENT_CMS
          : NEXT_HIERARCHY_CMS);
      fpcBO.setCreateTime(DateUtils.toStringISO8601(fpcDO.getCreateTime()));

      // 解密appToken
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
      fpcBO.setAppToken(appToken);

      // 解密cmsToken
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getCmsToken());
      fpcBO.setCmsToken(cmsToken);

      // 判断是否仅需要返回基础信息
      if (!isSimple) {
        DeviceStatusDO fpcStatusDO = deviceStatusService
            .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_TFA, fpcDO.getSerialNumber());
        fpcBO.setConnectStatus(fpcStatusDO.getCurrentConnectStatus());
        fpcBO.setConnectStatusText(
            MapUtils.getString(connectStateDict, fpcBO.getConnectStatus(), ""));
      }
      fpcBOList.add(fpcBO);
    }

    return fpcBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryFpcByCms(java.lang.String, boolean)
   */
  @Override
  public List<FpcBO> queryFpcByCms(String cmsSerialNumber, boolean drilldown) {
    List<String> cmsSerialNumbers = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (drilldown) {
      Map<String, List<CmsBO>> cmsHierarchy = cmsService.queryCms(new CmsQueryVO()).stream()
          .collect(Collectors.groupingBy(CmsBO::getSuperiorCmsSerialNumber));
      cmsSerialNumbers.addAll(iterateSubordinateCms(cmsSerialNumber, cmsHierarchy));
    } else {
      cmsSerialNumbers.add(cmsSerialNumber);
    }

    List<FpcDO> fpcDOList = fpcDao.queryFpcByCms(cmsSerialNumbers);

    // 本机设备序列号
    String serialNumber = licenseService.queryDeviceSerialNumber();

    List<FpcBO> fpcBOList = Lists.newArrayListWithExpectedSize(fpcDOList.size());
    for (FpcDO fpcDO : fpcDOList) {
      FpcBO fpcBO = new FpcBO();
      BeanUtils.copyProperties(fpcDO, fpcBO);
      fpcBO.setCmsName(StringUtils.equals(fpcDO.getCmsSerialNumber(), serialNumber) ? CURRENT_CMS
          : NEXT_HIERARCHY_CMS);
      fpcBO.setCreateTime(DateUtils.toStringISO8601(fpcDO.getCreateTime()));

      // 解密appToken
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
      fpcBO.setAppToken(appToken);

      // 解密cmsToken
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getCmsToken());
      fpcBO.setCmsToken(cmsToken);

      fpcBOList.add(fpcBO);
    }

    return fpcBOList;
  }

  private List<String> iterateSubordinateCms(String outset, Map<String, List<CmsBO>> cmsHierarchy) {
    List<String> allSubordinateCms = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    allSubordinateCms.add(outset);

    List<CmsBO> list = cmsHierarchy.get(outset);
    if (CollectionUtils.isNotEmpty(list)) {
      list.forEach(cms -> {
        allSubordinateCms.addAll(iterateSubordinateCms(cms.getSerialNumber(), cmsHierarchy));
      });
    }

    return allSubordinateCms;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryFpcById(java.lang.String)
   */
  @Override
  public FpcBO queryFpcById(String id) {
    Map<String,
        String> licenseStateDict = dictManager.getBaseDict().getItemMap("device_license_state");
    Map<String,
        String> connectStateDict = dictManager.getBaseDict().getItemMap("device_connect_state");

    FpcDO fpcDO = fpcDao.queryFpcById(id);
    if (StringUtils.isBlank(fpcDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备不存在");
    }

    FpcBO fpcBO = new FpcBO();
    BeanUtils.copyProperties(fpcDO, fpcBO);
    fpcBO.setCmsName(
        StringUtils.equals(fpcDO.getCmsSerialNumber(), licenseService.queryDeviceSerialNumber())
            ? CURRENT_CMS
            : NEXT_HIERARCHY_CMS);

    // 解密appToken
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
    fpcBO.setAppToken(appToken);

    // 解密cmsToken
    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getCmsToken());
    fpcBO.setCmsToken(cmsToken);

    // 获取设备的连接状态、license状态、交互时延
    DeviceStatusDO fpcStatusDO = deviceStatusService
        .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_TFA, fpcDO.getSerialNumber());
    fpcBO.setConnectStatus(fpcStatusDO.getCurrentConnectStatus());
    fpcBO.setLicenseStatus(
        StringUtils.equals(FpcCmsConstants.CONNECT_STATUS_ABNORMAL, fpcBO.getConnectStatus())
            ? FpcCmsConstants.LICENSE_ABNORMALITY
            : fpcStatusDO.getLicenseState());
    fpcBO.setLastInteractiveTime(DateUtils.toStringISO8601(fpcStatusDO.getLastInteractiveTime()));
    fpcBO.setLastInteractiveLatency(fpcStatusDO.getLastInteractiveLatency());

    // 设备运行时长
    fpcBO.setUpTime(fpcStatusDO.getUpTime());

    // 设备最新状态
    CentralSystemBO centralSystemBO = centralSystemService
        .queryCentralSystem(FpcCmsConstants.DEVICE_TYPE_TFA, fpcDO.getSerialNumber());
    BeanUtils.copyProperties(centralSystemBO, fpcBO);

    fpcBO.setRaidList(centralDiskService.queryCentralRaids(FpcCmsConstants.DEVICE_TYPE_TFA,
        fpcDO.getSerialNumber()));
    fpcBO.setConnectStatusText(MapUtils.getString(connectStateDict, fpcBO.getConnectStatus(), ""));
    fpcBO.setLicenseStatusText(MapUtils.getString(licenseStateDict, fpcBO.getLicenseStatus(), ""));
    fpcBO.setCreateTime(DateUtils.toStringISO8601(fpcDO.getCreateTime()));
    fpcBO.setNetifList(centralNetifService.queryCentralNetifProfiles(fpcDO.getSerialNumber(),
        FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT, FpcCmsConstants.DEVICE_NETIF_CATEGORY_INGEST,
        FpcCmsConstants.DEVICE_NETIF_CATEGORY_TRANSMIT));

    return fpcBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryFpcByIp(java.lang.String)
   */
  @Override
  public FpcBO queryFpcByIp(String ip) {
    FpcDO fpcDO = fpcDao.queryFpcByIpOrName(ip, null);

    FpcBO fpcBO = new FpcBO();
    BeanUtils.copyProperties(fpcDO, fpcBO);
    fpcBO.setCmsName(
        StringUtils.equals(fpcDO.getCmsSerialNumber(), licenseService.queryDeviceSerialNumber())
            ? CURRENT_CMS
            : NEXT_HIERARCHY_CMS);

    // 解密appToken
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
    fpcBO.setAppToken(appToken);

    // 解密cmsToken
    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getCmsToken());
    fpcBO.setCmsToken(cmsToken);

    return fpcBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryFpcBySerialNumber(java.lang.String)
   */
  @Override
  public FpcBO queryFpcBySerialNumber(String serialNumber) {
    FpcDO fpcDO = fpcDao.queryFpcBySerialNumber(serialNumber);

    FpcBO fpcBO = new FpcBO();
    BeanUtils.copyProperties(fpcDO, fpcBO);
    fpcBO.setCmsName(
        StringUtils.equals(fpcDO.getCmsSerialNumber(), licenseService.queryDeviceSerialNumber())
            ? CURRENT_CMS
            : NEXT_HIERARCHY_CMS);

    // 解密appToken
    String appToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());
    fpcBO.setAppToken(appToken);

    // 解密cmsToken
    String cmsToken = KeyEncUtils.decrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getCmsToken());
    fpcBO.setCmsToken(cmsToken);

    return fpcBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryCentralDevices()
   */
  @Override
  public CentralDeviceBO queryCentralDevices() {
    CentralDeviceBO root = new CentralDeviceBO();
    root.setDeviceSerialNumber(licenseService.queryDeviceSerialNumber());
    root.setDeviceName(
        globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME, false));
    root.setDeviceType(CenterConstants.CENTRAL_DEVICE_CMS);
    root.setChild(Lists.newArrayList());

    // 本机设备序列号
    String deviceSerialNumber = licenseService.queryDeviceSerialNumber();

    // 查询所有探针设备
    List<FpcDO> fpcList = fpcDao.queryFpcs(new FpcQueryVO());
    Map<String,
        List<CentralDeviceBO>> fpcMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    fpcList.forEach(fpc -> {
      List<CentralDeviceBO> list = fpcMap.getOrDefault(fpc.getCmsSerialNumber(),
          Lists.newArrayList());

      CentralDeviceBO centralDeviceBO = new CentralDeviceBO();
      centralDeviceBO.setDeviceSerialNumber(fpc.getSerialNumber());
      centralDeviceBO.setDeviceName(fpc.getName());
      centralDeviceBO.setDeviceType(CenterConstants.CENTRAL_DEVICE_FPC);
      centralDeviceBO
          .setOwner(StringUtils.equals(fpc.getCmsSerialNumber(), deviceSerialNumber) ? CURRENT_CMS
              : NEXT_HIERARCHY_CMS);
      centralDeviceBO.setSensorType(FpcCmsConstants.SENSOR_TYPE_NORMAL);
      list.add(centralDeviceBO);

      fpcMap.put(fpc.getCmsSerialNumber(), list);
    });

    // 查询所有下级cms
    List<CmsBO> cmsList = cmsService.queryCms(new CmsQueryVO());
    Map<String,
        List<CentralDeviceBO>> cmsMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    cmsList.forEach(cms -> {
      List<CentralDeviceBO> list = cmsMap.getOrDefault(cms.getSuperiorCmsSerialNumber(),
          Lists.newArrayList());

      CentralDeviceBO centralDeviceBO = new CentralDeviceBO();
      centralDeviceBO.setDeviceSerialNumber(cms.getSerialNumber());
      centralDeviceBO.setDeviceName(cms.getName());
      centralDeviceBO.setDeviceType(CenterConstants.CENTRAL_DEVICE_CMS);
      centralDeviceBO.setOwner(
          StringUtils.equals(cms.getSuperiorCmsSerialNumber(), deviceSerialNumber) ? CURRENT_CMS
              : NEXT_HIERARCHY_CMS);
      centralDeviceBO.setChild(Lists.newArrayList());
      list.add(centralDeviceBO);

      cmsMap.put(cms.getSuperiorCmsSerialNumber(), list);
    });

    traverseDevice(root, cmsMap, fpcMap);

    return root;
  }

  private void traverseDevice(CentralDeviceBO root, Map<String, List<CentralDeviceBO>> cmsMap,
      Map<String, List<CentralDeviceBO>> fpcMap) {
    String deviceSerialNumber = root.getDeviceSerialNumber();

    List<CentralDeviceBO> child = root.getChild();
    // 探针
    List<CentralDeviceBO> fpcList = fpcMap.get(deviceSerialNumber);
    if (CollectionUtils.isNotEmpty(fpcList)) {
      child.addAll(fpcList);
    }
    // cms
    List<CentralDeviceBO> cmsList = cmsMap.get(deviceSerialNumber);
    if (CollectionUtils.isNotEmpty(cmsList)) {
      child.addAll(cmsList);
      cmsList.forEach(cms -> {
        traverseDevice(cms, cmsMap, fpcMap);
      });
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#queryFpcLoginUrl(java.lang.String)
   */
  @Override
  public String queryFpcLoginUrl(String serialNumber) {
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

    FpcDO fpcDO = fpcDao.queryFpcBySerialNumber(serialNumber);
    if (StringUtils.isBlank(fpcDO.getIp())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "未获取到当前设备IP");
    }

    StringBuilder ssoLoginUrl = new StringBuilder();
    ssoLoginUrl.append(HotPropertiesHelper.getProperty("fpc.manager.server.protocol"))
        .append("://");
    ssoLoginUrl.append(
        HotPropertiesHelper.getProperty("sso.login.api").replace("{deviceIp}", fpcDO.getIp())
            .replace("{devicePort}", HotPropertiesHelper.getProperty("fpc.manager.server.port"))
            .replace("{servletContext}", FPC_MANAGER_SERVLET_CONTEXT).replace("{jwt}", token));

    return ssoLoginUrl.toString();
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#saveFpc(com.machloop.fpc.cms.center.central.bo.FpcBO)
   */
  @Override
  public synchronized FpcBO saveFpc(FpcBO fpcBO) {
    FpcDO fpcDO = new FpcDO();
    BeanUtils.copyProperties(fpcBO, fpcDO);
    fpcDO.setReportState(Constants.BOOL_NO);
    fpcDO.setReportAction(FpcCmsConstants.SYNC_ACTION_ADD);

    // 加密appToken
    fpcDO.setAppToken(KeyEncUtils.encrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcBO.getAppToken()));

    // 加密cmsToken
    fpcDO.setCmsToken(KeyEncUtils.encrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcBO.getCmsToken()));

    fpcDO = fpcDao.saveFpc(fpcDO);

    // ck集群中增加节点
    ClickhouseRemoteServerHelper.addNode(fpcDO.getIp());

    return queryFpcById(fpcDO.getId());
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#updateFpcStatus(com.machloop.fpc.cms.center.central.bo.FpcBO)
   */
  @Override
  public void updateFpcStatus(FpcBO fpcBO) {
    FpcDO fpcDO = new FpcDO();
    BeanUtils.copyProperties(fpcBO, fpcDO);
    fpcDO.setReportState(Constants.BOOL_NO);
    fpcDO.setReportAction(FpcCmsConstants.SYNC_ACTION_MODIFY);

    // 加密appToken
    fpcDO.setAppToken(KeyEncUtils.encrypt(
        HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcBO.getAppToken()));

    // 设备IP变更，修改集群节点
    String sourceHost = fpcDao.queryFpcBySerialNumber(fpcBO.getSerialNumber()).getIp();
    ClickhouseRemoteServerHelper.updateNode(sourceHost, fpcBO.getIp());

    fpcDao.updateFpcStatus(fpcDO);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#deleteFpc(java.lang.String, java.lang.String)
   */
  @Transactional
  @Override
  public FpcBO deleteFpc(String id, String operatorId) {
    FpcBO fpcBO = queryFpcById(id);
    String fpcSerialNumber = fpcBO.getSerialNumber();

    if (StringUtils.isBlank(fpcSerialNumber)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "设备不存在");
    }

    // 判断探针所包含网络是否被应用
    List<String> fpcNetworkIds = fpcNetworkService
        .queryNetworks(FpcCmsConstants.DEVICE_TYPE_TFA, fpcSerialNumber).stream()
        .map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
    List<String> usedNetworkIds = sensorNetworkService.querySensorNetworks().stream()
        .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList());
    if (CollectionUtils.containsAny(usedNetworkIds, fpcNetworkIds)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "该设备网络已经加入到探针网络，无法删除");
    }

    // 删除设备
    fpcDao.deleteFpc(id, operatorId);

    // 删除设备上报的网络
    fpcNetworkService.deleteNetworkByFpc(fpcSerialNumber);

    // 清除设备相关的日志
    logDao.deleteLogsByNodeId(fpcSerialNumber);
    // 清除设备相关的告警
    alarmDao.deleteAlarmsByNodeId(fpcSerialNumber);
    // 清除设备相关的磁盘
    centralDiskService.deleteCentralDisk(FpcCmsConstants.DEVICE_TYPE_TFA, fpcSerialNumber);
    // 清除设备相关的接口状态
    centralNetifService.deleteCentralNetifs(FpcCmsConstants.DEVICE_TYPE_TFA, fpcSerialNumber);

    // 删除集群节点
    ClickhouseRemoteServerHelper.deleteNode(fpcBO.getIp());
    LogHelper.systemRuning(LogHelper.LEVEL_NOTICE,
        String.format("探针设备[%s]被删除，将设备移出数据集群", fpcBO.getIp()), "system");

    return fpcBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#exportFpcMessage()
   */
  @Override
  public String exportFpcMessage() {
    List<FpcDO> fpcList = fpcDao.queryFpcs(new FpcQueryVO());

    StringBuilder content = new StringBuilder(
        "`名称`,`版本`,`IP`,`序列号`,`appKey`,`appToken`,`cmsToken`,`备注`\n");
    for (FpcDO fpcDO : fpcList) {
      // 解密appToken
      String appToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getAppToken());

      // 解密cmsToken
      String cmsToken = KeyEncUtils.decrypt(
          HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_SECRET), fpcDO.getCmsToken());

      String rowContent = CsvUtils.spliceRowData(fpcDO.getName(), fpcDO.getVersion(), fpcDO.getIp(),
          fpcDO.getSerialNumber(), fpcDO.getAppKey(), appToken, cmsToken, fpcDO.getDescription());
      content.append(rowContent);
    }

    return content.toString();
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.FpcService#exportFpcSerialNumber()
   */
  @Override
  public String exportFpcSerialNumber() {
    List<FpcDO> fpcList = fpcDao.queryFpcs(new FpcQueryVO());

    StringBuilder content = new StringBuilder();
    for (FpcDO fpcDO : fpcList) {
      if (StringUtils.isNotBlank(fpcDO.getSerialNumber())) {
        content.append(fpcDO.getSerialNumber()).append("\n");
      }
    }

    return content.toString();
  }

}
