package com.machloop.fpc.manager.knowledge.service.impl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.googlecode.ipv6.IPv6Network;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.*;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.manager.knowledge.bo.*;
import com.machloop.fpc.manager.knowledge.dao.GeoCustomCountryDao;
import com.machloop.fpc.manager.knowledge.dao.GeoIpSettingDao;
import com.machloop.fpc.manager.knowledge.dao.GeoKnowledgeDao;
import com.machloop.fpc.manager.knowledge.data.*;
import com.machloop.fpc.manager.knowledge.service.GeoService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2020年12月31日, fpc-manager
 */
@Order(6)
@Service
public class GeoServiceImpl implements GeoService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(GeoServiceImpl.class);

  private static final int MAXIMUM_AMOUNT_COUNTRY = 200;

  private static final int CUSTOM_COUNTRY_INITIATION_ID = 300;

  private static final String DEFAULT_LOCATION = "0";

  private static final int MAX_NAME_LENGTH = 30;
  private static final int MAX_DESCRIPTION_LENGTH = 255;

  private static final Range<Double> RANGE_LONGITUDE = Range.closed(-180.00, 180.00);
  private static final Range<Double> RANGE_LATITUDE = Range.closed(-90.00, 90.00);

  private static final String CUSTOM_COUNTRY_CSV_TITLE = "`名称`,`描述`,`经度`,`纬度`,`IP地址`\n";

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private GeoKnowledgeDao knowledgeDao;

  @Autowired
  private GeoCustomCountryDao geoCustomCountryDao;

  @Autowired
  private GeoIpSettingDao geoIpSettingDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#queryGeoKnowledgeInfos()
   */
  @Override
  public GeoKnowledgeInfoBO queryGeoKnowledgeInfos() {
    GeoKnowledgeInfoBO knowledgeBO = new GeoKnowledgeInfoBO();

    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.geoip.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      return knowledgeBO;
    }

    GeoKnowledgeInfoDO knowledgeDO = knowledgeDao.queryGeoKnowledgeInfos(knowledgeFilePath);
    knowledgeBO.setImportDate(knowledgeDO.getImportDate());
    knowledgeBO.setReleaseDate(knowledgeDO.getReleaseDate());
    knowledgeBO.setVersion(knowledgeDO.getVersion());
    return knowledgeBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#queryGeolocations()
   */
  @Override
  public Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>, List<GeoCityBO>> queryGeolocations() {
    List<GeoCountryBO> countryList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<GeoProvinceBO> provinceList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<GeoCityBO> cityList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> result = Tuples.of(countryList, provinceList, cityList);

    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.geoip.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      return result;
    }

    Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>,
        List<GeoCityDO>> geolocations = knowledgeDao.queryGeolocations(knowledgeFilePath);

    // 地区IP字典<countryId_provinceId_cityId, IPAddress>
    Map<String, String> locationIpDict = geoIpSettingDao.queryGeoIpSettings().stream()
        .collect(Collectors.toMap(setting -> {
          StringBuilder key = new StringBuilder(setting.getCountryId());
          if (!StringUtils.equals(setting.getProvinceId(), DEFAULT_LOCATION)) {
            key.append("_").append(setting.getProvinceId());
          }
          if (!StringUtils.equals(setting.getCityId(), DEFAULT_LOCATION)) {
            key.append("_").append(setting.getCityId());
          }

          return key.toString();
        }, GeoIpSettingDO::getIpAddress));

    for (GeoCountryDO countryDO : geolocations.getT1()) {
      GeoCountryBO countryBO = new GeoCountryBO();
      BeanUtils.copyProperties(countryDO, countryBO);
      countryBO.setIpAddress(locationIpDict.get(countryBO.getCountryId()));
      countryList.add(countryBO);
    }
    for (GeoProvinceDO provinceDO : geolocations.getT2()) {
      GeoProvinceBO provinceBO = new GeoProvinceBO();
      BeanUtils.copyProperties(provinceDO, provinceBO);
      provinceBO.setIpAddress(locationIpDict
          .get(StringUtils.joinWith("_", provinceBO.getCountryId(), provinceBO.getProvinceId())));
      provinceList.add(provinceBO);
    }
    for (GeoCityDO cityDO : geolocations.getT3()) {
      GeoCityBO cityBO = new GeoCityBO();
      BeanUtils.copyProperties(cityDO, cityBO);
      cityBO.setIpAddress(locationIpDict.get(StringUtils.joinWith("_", cityBO.getCountryId(),
          cityBO.getProvinceId(), cityBO.getCityId())));
      cityList.add(cityBO);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#importGeoKnowledges(org.springframework.web.multipart.MultipartFile)
   */
  @Override
  public GeoKnowledgeInfoBO importGeoKnowledges(MultipartFile file) {
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    Path tempPath = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID());
    Path knowledgePath = Paths.get(HotPropertiesHelper.getProperty("file.geoip.knowledge.path"));
    try {
      // 文件上传为临时文件
      file.transferTo(tempPath.toFile());
      GeoKnowledgeInfoDO infoDO = knowledgeDao.queryGeoKnowledgeInfos(tempPath.toString());
      if (StringUtils.isBlank(infoDO.getVersion())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "上传地区库文件非法, 请上传正确的地区库文件");
      }

      // 替换原文件
      Files.move(tempPath.toFile(), knowledgePath.toFile());

      GeoKnowledgeInfoBO knowledgeBO = new GeoKnowledgeInfoBO();
      knowledgeBO.setImportDate(infoDO.getImportDate());
      knowledgeBO.setReleaseDate(infoDO.getReleaseDate());
      knowledgeBO.setVersion(infoDO.getVersion());
      return knowledgeBO;
    } catch (IllegalStateException | IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "上传地区库文件失败");
    }
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#exportCustomCountrys()
   */
  @Override
  public List<String> exportCustomCountrys() {
    // 地区IP字典<countryId, IPAddress>
    Map<String, String> locationIpDict = geoIpSettingDao.queryGeoCountryIpSettings().stream()
        .collect(Collectors.toMap(GeoIpSettingDO::getCountryId, GeoIpSettingDO::getIpAddress));

    List<String> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    result.add(CUSTOM_COUNTRY_CSV_TITLE);
    geoCustomCountryDao.queryGeoCustomCountrys(Constants.BOOL_NO).forEach(country -> {
      result.add(CsvUtils.spliceRowData(country.getName(), country.getDescription(),
          country.getLongitude(), country.getLatitude(),
          locationIpDict.getOrDefault(country.getCountryId(), "")));
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#importCustomCountrys(org.springframework.web.multipart.MultipartFile, java.lang.String)
   */
  @Transactional
  @Override
  public int importCustomCountrys(MultipartFile file, String operatorId) {
    LOGGER.info("begin to import cutsom country, file name :{}", file.getOriginalFilename());

    int currentCustomSize = geoCustomCountryDao.countGeoCustomCountrys();
    if (currentCustomSize >= MAXIMUM_AMOUNT_COUNTRY) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION,
          "最多只支持" + MAXIMUM_AMOUNT_COUNTRY + "个自定义地区");
    }

    // 规则库
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.geoip.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "未找到地区规则库");
    }
    Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>,
        List<GeoCityDO>> geolocations = knowledgeDao.queryGeolocations(knowledgeFilePath);

    // 地区字典<name,ip>
    Map<String, String> locationNameIpDict = getLocationNameIpDict(geolocations);
    // 已存在的countryId
    List<String> existCountryIds = geoCustomCountryDao.queryGeoCustomCountrys(Constants.BOOL_NO)
        .stream().map(customCountry -> customCountry.getCountryId()).collect(Collectors.toList());

    List<GeoCustomCountryDO> countryList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<GeoIpSettingDO> geoIpSettingList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    String line = "";
    int lineNumber = 0;
    int countrySurplus = MAXIMUM_AMOUNT_COUNTRY - currentCustomSize;
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;
        if (lineNumber - 1 > countrySurplus) {
          LOGGER.warn("import file error, limit exceeded.");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容超出剩余可添加自定义地区数量[" + countrySurplus + "]");
        }

        // 跳过首行
        if (lineNumber == 1) {
          LOGGER.info("pass title, line: [{}]", line);
          continue;
        }

        // 解析每一列数据
        List<String> contents = CsvUtils.splitRowData(line);
        if (contents.size() == 0) {
          LOGGER.info("skip blank line, line number: [{}]", lineNumber);
          continue;
        } else if (contents.size() != CsvUtils.convertCSVToList(CUSTOM_COUNTRY_CSV_TITLE).size()) {
          LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber + ", 内容: " + line);
        }

        String name = StringUtils.trim(contents.get(0));
        String description = StringUtils.trim(contents.get(1));
        String longitude = StringUtils.trim(contents.get(2));
        String latitude = StringUtils.trim(contents.get(3));
        String ipAddress = StringUtils.trim(contents.get(4));
        if (StringUtils.isAnyBlank(name, longitude, latitude)) {
          LOGGER.warn("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber);
        }

        // 校验名称长度和是否重复
        if (name.length() > MAX_NAME_LENGTH) {
          LOGGER.warn("import file error, name length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 地区名称长度超出限制：" + MAX_NAME_LENGTH + ", 行号: " + lineNumber);
        }
        if (locationNameIpDict.containsKey(name)) {
          LOGGER.warn("import file error, name exist, lineNumber: {}, content: {}", lineNumber,
              line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 地区名称已存在, 行号: " + lineNumber);
        }

        // 校验描述长度
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
          LOGGER.warn(
              "import file error, description length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 描述长度超出限制：" + MAX_DESCRIPTION_LENGTH + ", 行号: " + lineNumber);
        }

        // 经度
        if (!RANGE_LONGITUDE.contains(Double.valueOf(longitude))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 地区经度不在有效范围内：" + RANGE_LONGITUDE.toString() + ", 行号: " + lineNumber);
        }

        // 纬度
        if (!RANGE_LATITUDE.contains(Double.valueOf(latitude))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 地区经度不在有效范围内：" + RANGE_LATITUDE.toString() + ", 行号: " + lineNumber);
        }

        if (StringUtils.isNotBlank(ipAddress)) {
          // IP格式校验
          checkIpAddressThrowExcption(ipAddress, lineNumber);

          // IP重复校验
          checkIpAddresssDuplicate(ipAddress, locationNameIpDict, lineNumber);
        }

        // 保存自定义地区信息
        GeoCustomCountryDO customCountryDO = new GeoCustomCountryDO();
        customCountryDO.setName(name);
        customCountryDO.setCountryId(generateCountryId(existCountryIds));
        customCountryDO.setLongitude(longitude);
        customCountryDO.setLatitude(latitude);
        customCountryDO.setDescription(StringUtils.defaultIfBlank(description, ""));
        customCountryDO.setOperatorId(operatorId);
        countryList.add(customCountryDO);

        // 保存地区包含的IP地址
        GeoIpSettingDO geoIpSetting = new GeoIpSettingDO();
        geoIpSetting.setCountryId(customCountryDO.getCountryId());
        geoIpSetting.setProvinceId(DEFAULT_LOCATION);
        geoIpSetting.setCityId(DEFAULT_LOCATION);
        geoIpSetting.setIpAddress(ipAddress);
        geoIpSetting.setOperatorId(operatorId);
        geoIpSettingList.add(geoIpSetting);

        locationNameIpDict.put(name, ipAddress);
        existCountryIds.add(customCountryDO.getCountryId());
      }
    } catch (IOException e) {
      LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    if (countryList.isEmpty() || geoIpSettingList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件导入失败，未找到地区数据");
    }

    // 保存地区信息
    int importCount = geoCustomCountryDao.batchSaveGeoCustomCountrys(countryList);

    // 保存地区IP信息
    geoIpSettingDao.batchSaveGeoIpSettings(geoIpSettingList);

    LOGGER.info("success to import custom country. total: [{}]", importCount);
    return importCount;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#queryCustomCountrys()
   */
  @Override
  public List<GeoCustomCountryBO> queryCustomCountrys() {
    // 地区IP字典<countryId, IPAddress>
    Map<String, String> locationIpDict = geoIpSettingDao.queryGeoCountryIpSettings().stream()
        .collect(Collectors.toMap(GeoIpSettingDO::getCountryId, GeoIpSettingDO::getIpAddress));

    List<GeoCustomCountryDO> customCountryDOList = geoCustomCountryDao
        .queryGeoCustomCountrys(Constants.BOOL_NO);
    List<GeoCustomCountryBO> customCountryBOList = customCountryDOList.stream()
        .map(customCountryDO -> {
          GeoCustomCountryBO customCountryBO = new GeoCustomCountryBO();
          BeanUtils.copyProperties(customCountryDO, customCountryBO);
          customCountryBO
              .setIpAddress(locationIpDict.getOrDefault(customCountryBO.getCountryId(), ""));
          customCountryBO.setCreateTime(DateUtils.toStringISO8601(customCountryDO.getCreateTime()));
          customCountryBO.setUpdateTime(DateUtils.toStringISO8601(customCountryDO.getUpdateTime()));

          return customCountryBO;
        }).collect(Collectors.toList());

    return customCountryBOList;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#queryCustomCountry(java.lang.String)
   */
  @Override
  public GeoCustomCountryBO queryCustomCountry(String id) {
    GeoCustomCountryDO customCountryDO = geoCustomCountryDao.queryGeoCustomCountry(id);

    GeoCustomCountryBO customCountryBO = new GeoCustomCountryBO();
    BeanUtils.copyProperties(customCountryDO, customCountryBO);
    String ipAddress = geoIpSettingDao.queryGeoIpSettingByCountryId(customCountryBO.getCountryId())
        .getIpAddress();
    customCountryBO.setIpAddress(ipAddress);
    customCountryBO.setCreateTime(DateUtils.toStringISO8601(customCountryDO.getCreateTime()));
    customCountryBO.setUpdateTime(DateUtils.toStringISO8601(customCountryDO.getUpdateTime()));

    return customCountryBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#queryCustomCountryByCmsCustomCountryId(java.lang.String)
   */
  @Override
  public GeoCustomCountryBO queryCustomCountryByCmsCustomCountryId(String cmsCustomCountryId) {

    GeoCustomCountryDO customCountryDO = geoCustomCountryDao
        .queryGeoCustomCountryByCmsCustomCountryId(cmsCustomCountryId);

    GeoCustomCountryBO customCountryBO = new GeoCustomCountryBO();
    BeanUtils.copyProperties(customCountryDO, customCountryBO);

    return customCountryBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#saveCustomCountry(com.machloop.fpc.manager.knowledge.bo.GeoCustomCountryBO, java.lang.String)
   */
  @Transactional
  @Override
  public GeoCustomCountryBO saveCustomCountry(GeoCustomCountryBO customCountryBO,
      String operatorId) {
    if (geoCustomCountryDao.countGeoCustomCountrys() >= MAXIMUM_AMOUNT_COUNTRY) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION,
          "最多只支持" + MAXIMUM_AMOUNT_COUNTRY + "个自定义地区");
    }

    // 规则库
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.geoip.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "未找到地区规则库");
    }
    Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>,
        List<GeoCityDO>> geolocations = knowledgeDao.queryGeolocations(knowledgeFilePath);

    // 校验名称是否重复
    GeoCustomCountryDO existName = geoCustomCountryDao
        .queryGeoCustomCountryByName(customCountryBO.getName());
    if (StringUtils.isNotBlank(existName.getId())
        || isNameRepetition(geolocations, customCountryBO.getName())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "地区名称不能重复，请修改地区名称");
    }

    String ipAddress = customCountryBO.getIpAddress();
    if (StringUtils.isNotBlank(ipAddress)) {
      // IP格式校验
      checkIpAddressThrowExcption(ipAddress, null);

      // IP重复校验
      checkIpAddresssDuplicate(ipAddress, getLocationNameIpDict(geolocations), null);
    }

    List<String> existCountryIds = geoCustomCountryDao.queryGeoCustomCountrys(Constants.BOOL_NO)
        .stream().map(customCountry -> customCountry.getCountryId()).collect(Collectors.toList());

    // 保存自定义地区信息
    GeoCustomCountryDO customCountryDO = new GeoCustomCountryDO();
    BeanUtils.copyProperties(customCountryBO, customCountryDO);
    // 区分本机新建或上级cms下发
    if (StringUtils.isBlank(customCountryDO.getCountryId())) {
      customCountryDO.setCountryId(generateCountryId(existCountryIds));
    }
    customCountryDO
        .setDescription(StringUtils.defaultIfBlank(customCountryBO.getDescription(), ""));
    customCountryDO.setOperatorId(operatorId);
    GeoCustomCountryDO geoCustomCountry = geoCustomCountryDao
        .saveOrRecoverGeoCustomCountry(customCountryDO);

    // 保存地区包含的IP地址
    if (StringUtils.isNotBlank(ipAddress)) {
      GeoIpSettingDO geoIpSetting = new GeoIpSettingDO();
      geoIpSetting.setCountryId(geoCustomCountry.getCountryId());
      geoIpSetting.setProvinceId(DEFAULT_LOCATION);
      geoIpSetting.setCityId(DEFAULT_LOCATION);
      geoIpSetting.setIpAddress(ipAddress);
      geoIpSetting.setOperatorId(operatorId);
      geoIpSettingDao.saveGeoIpSetting(geoIpSetting);
    }

    return queryCustomCountry(geoCustomCountry.getId());
  }

  private String generateCountryId(List<String> existCountryIds) {
    String countryId = globalSettingService
        .generateSequence(ManagerConstants.GLOBAL_SETTING_GEOIP_CUSTOM_COUNTRY_SEQ_KEY, "");
    if (Integer.parseInt(countryId) == (CUSTOM_COUNTRY_INITIATION_ID + MAXIMUM_AMOUNT_COUNTRY)) {
      globalSettingService.setValue(ManagerConstants.GLOBAL_SETTING_GEOIP_CUSTOM_COUNTRY_SEQ_KEY,
          String.valueOf(CUSTOM_COUNTRY_INITIATION_ID));
      countryId = globalSettingService
          .generateSequence(ManagerConstants.GLOBAL_SETTING_GEOIP_CUSTOM_COUNTRY_SEQ_KEY, "");
    }

    while (true) {
      if (!existCountryIds.contains(countryId)) {
        break;
      }
      countryId = globalSettingService
          .generateSequence(ManagerConstants.GLOBAL_SETTING_GEOIP_CUSTOM_COUNTRY_SEQ_KEY, "");
    }

    return countryId;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#updateCustomCountry(java.lang.String, com.machloop.fpc.manager.knowledge.bo.GeoCustomCountryBO, java.lang.String)
   */
  @Transactional
  @Override
  public GeoCustomCountryBO updateCustomCountry(String id, GeoCustomCountryBO customCountryBO,
      String operatorId) {
    GeoCustomCountryDO exist = geoCustomCountryDao.queryGeoCustomCountry(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "地区不存在");
    }

    // 规则库
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.geoip.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "未找到地区规则库");
    }
    Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>,
        List<GeoCityDO>> geolocations = knowledgeDao.queryGeolocations(knowledgeFilePath);

    // 校验名称是否重复
    GeoCustomCountryDO existName = geoCustomCountryDao
        .queryGeoCustomCountryByName(customCountryBO.getName());
    if ((StringUtils.isNotBlank(existName.getId()) && !StringUtils.equals(existName.getId(), id))
        || isNameRepetition(geolocations, customCountryBO.getName())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "地区名称不能重复，请修改地区名称");
    }

    // 校验 IP
    String ipAddress = customCountryBO.getIpAddress();
    if (StringUtils.isNotBlank(ipAddress)) {
      List<String> newIpList = CsvUtils.convertCSVToList(ipAddress);
      List<String> oldIpList = CsvUtils.convertCSVToList(
          geoIpSettingDao.queryGeoIpSettingByCountryId(exist.getCountryId()).getIpAddress());
      newIpList.removeAll(oldIpList);
      if (CollectionUtils.isNotEmpty(newIpList)) {
        String newIpAddress = CsvUtils.convertCollectionToCSV(newIpList);
        // IP格式校验
        checkIpAddressThrowExcption(newIpAddress, null);

        // IP重复校验
        checkIpAddresssDuplicate(newIpAddress, getLocationNameIpDict(geolocations), null);
      }
    }

    // 保存自定义地区信息
    GeoCustomCountryDO customCountryDO = new GeoCustomCountryDO();
    BeanUtils.copyProperties(customCountryBO, customCountryDO);
    customCountryDO.setId(id);
    customCountryDO
        .setDescription(StringUtils.defaultIfBlank(customCountryBO.getDescription(), ""));
    customCountryDO.setOperatorId(operatorId);
    geoCustomCountryDao.updateGeoCustomCountry(customCountryDO);

    // 修改IP地址
    if (StringUtils.isBlank(ipAddress)) {
      geoIpSettingDao.deleteGeoIpSetting(exist.getCountryId(), DEFAULT_LOCATION, DEFAULT_LOCATION,
          operatorId);
    } else {
      GeoIpSettingDO geoIpSetting = new GeoIpSettingDO();
      geoIpSetting.setCountryId(exist.getCountryId());
      geoIpSetting.setProvinceId(DEFAULT_LOCATION);
      geoIpSetting.setCityId(DEFAULT_LOCATION);
      geoIpSetting.setIpAddress(ipAddress);
      geoIpSetting.setOperatorId(operatorId);
      geoIpSettingDao.saveOrUpdateGeoIpSetting(geoIpSetting);
    }

    return queryCustomCountry(id);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#deleteCustomCountry(java.lang.String, java.lang.String)
   */
  @Transactional
  @Override
  public GeoCustomCountryBO deleteCustomCountry(String id, String operatorId, boolean forceDelete) {
    GeoCustomCountryDO exist = geoCustomCountryDao.queryGeoCustomCountry(id);
    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "地区不存在");
    }

    // 删除自定义地区
    geoCustomCountryDao.deleteGeoCustomCountry(Lists.newArrayList(id), operatorId);
    // 删除地区IP配置
    geoIpSettingDao.deleteGeoIpSetting(exist.getCountryId(), DEFAULT_LOCATION, DEFAULT_LOCATION,
        operatorId);

    return queryCustomCountry(id);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#batchDeleteCustomCountry(java.util.List, java.lang.String)
   */
  @Transactional
  @Override
  public int batchDeleteCustomCountry(List<String> ids, String operatorId) {
    Map<String,
        String> deleteCountrys = geoCustomCountryDao.queryGeoCustomCountrys(Constants.BOOL_NO)
            .stream().filter(item -> ids.contains("ALL") || ids.contains(item.getId()))
            .collect(Collectors.toMap(GeoCustomCountryDO::getId, GeoCustomCountryDO::getCountryId));
    List<String> deleteIds = Lists.newArrayList(deleteCountrys.keySet());
    List<String> deleteCountryIds = Lists.newArrayList(deleteCountrys.values());

    // 删除自定义地区
    int deleteCount = geoCustomCountryDao.deleteGeoCustomCountry(deleteIds, operatorId);
    // 删除地区IP配置
    geoIpSettingDao.deleteByCountryIds(deleteCountryIds, operatorId);

    return deleteCount;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#updateGeoIpSetting(com.machloop.fpc.manager.knowledge.bo.GeoIpSettingBO, java.lang.String)
   */
  @Override
  public int updateGeoIpSetting(GeoIpSettingBO geoIpSetting, String operatorId) {
    GeoIpSettingDO geoIpSettingDO = new GeoIpSettingDO();
    geoIpSettingDO
        .setCountryId(StringUtils.defaultString(geoIpSetting.getCountryId(), DEFAULT_LOCATION));
    geoIpSettingDO
        .setProvinceId(StringUtils.defaultString(geoIpSetting.getProvinceId(), DEFAULT_LOCATION));
    geoIpSettingDO.setCityId(StringUtils.defaultString(geoIpSetting.getCityId(), DEFAULT_LOCATION));
    geoIpSettingDO.setIpAddress(geoIpSetting.getIpAddress());
    geoIpSettingDO.setOperatorId(operatorId);

    if (StringUtils.isBlank(geoIpSetting.getIpAddress())) {
      return geoIpSettingDao.deleteGeoIpSetting(geoIpSettingDO.getCountryId(),
          geoIpSettingDO.getProvinceId(), geoIpSettingDO.getCityId(), operatorId);
    } else {
      // 规则库
      String knowledgeFilePath = HotPropertiesHelper.getProperty("file.geoip.knowledge.path");
      if (!Paths.get(knowledgeFilePath).toFile().exists()) {
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "未找到地区规则库");
      }
      Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>,
          List<GeoCityDO>> geolocations = knowledgeDao.queryGeolocations(knowledgeFilePath);

      // 校验 IP
      String ipAddress = geoIpSettingDO.getIpAddress();
      List<String> newIpList = CsvUtils.convertCSVToList(ipAddress);
      List<String> oldIpList = CsvUtils
          .convertCSVToList(geoIpSettingDao.queryGeoIpSetting(geoIpSettingDO.getCountryId(),
              geoIpSettingDO.getProvinceId(), geoIpSettingDO.getCityId()).getIpAddress());
      newIpList.removeAll(oldIpList);
      if (CollectionUtils.isNotEmpty(newIpList)) {
        String newIpAddress = CsvUtils.convertCollectionToCSV(newIpList);
        // IP格式校验
        checkIpAddressThrowExcption(newIpAddress, null);

        // IP重复校验
        checkIpAddresssDuplicate(newIpAddress, getLocationNameIpDict(geolocations), null);
      }

      return geoIpSettingDao.saveOrUpdateGeoIpSetting(geoIpSettingDO);
    }
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.GeoService#queryAllLocationIdNameMapping()
   */
  @Override
  public Map<String, String> queryAllLocationIdNameMapping() {
    Map<String, String> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = queryGeolocations();

    result.putAll(geolocations.getT1().stream()
        .collect(Collectors.toMap(GeoCountryBO::getCountryId, GeoCountryBO::getNameText)));
    result.putAll(geoCustomCountryDao.queryGeoCustomCountrys(Constants.BOOL_NO).stream()
        .collect(Collectors.toMap(GeoCustomCountryDO::getCountryId, GeoCustomCountryDO::getName)));
    result.putAll(geolocations.getT2().stream().collect(Collectors.toMap(
        province -> StringUtils.joinWith("_", province.getCountryId(), province.getProvinceId()),
        GeoProvinceBO::getNameText)));
    result.putAll(
        geolocations.getT3().stream().collect(Collectors.toMap(city -> StringUtils.joinWith("_",
            city.getCountryId(), city.getProvinceId(), city.getCityId()), GeoCityBO::getNameText)));

    return result;
  }

  private Map<String, String> getLocationNameIpDict(
      Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>, List<GeoCityDO>> geolocations) {
    Map<String,
        String> locationNameMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    locationNameMap.putAll(geolocations.getT1().stream()
        .collect(Collectors.toMap(GeoCountryDO::getCountryId, GeoCountryDO::getName)));
    locationNameMap.putAll(geolocations.getT2().stream().collect(Collectors.toMap(
        province -> StringUtils.joinWith("_", province.getCountryId(), province.getProvinceId()),
        GeoProvinceDO::getName)));
    locationNameMap.putAll(
        geolocations.getT3().stream().collect(Collectors.toMap(city -> StringUtils.joinWith("_",
            city.getCountryId(), city.getProvinceId(), city.getCityId()), GeoCityDO::getName)));
    locationNameMap.putAll(geoCustomCountryDao.queryGeoCustomCountrys(Constants.BOOL_NO).stream()
        .collect(Collectors.toMap(GeoCustomCountryDO::getCountryId, GeoCustomCountryDO::getName)));

    Map<String, String> locationNameIpDict = geoIpSettingDao.queryGeoIpSettings().stream()
        .collect(Collectors.toMap(ipSetting -> {
          StringBuilder key = new StringBuilder(ipSetting.getCountryId());
          if (!StringUtils.equals(ipSetting.getProvinceId(), DEFAULT_LOCATION)) {
            key.append("_").append(ipSetting.getProvinceId());
          }
          if (!StringUtils.equals(ipSetting.getCityId(), DEFAULT_LOCATION)) {
            key.append("_").append(ipSetting.getCityId());
          }

          return locationNameMap.get(key.toString());
        }, GeoIpSettingDO::getIpAddress));

    return locationNameIpDict;
  }

  private boolean isNameRepetition(
      Tuple3<List<GeoCountryDO>, List<GeoProvinceDO>, List<GeoCityDO>> geolocations, String name) {
    if (geolocations.getT1().stream().map(GeoCountryDO::getNameText).collect(Collectors.toSet())
        .contains(name)) {
      return true;
    }
    if (geolocations.getT2().stream().map(GeoProvinceDO::getNameText).collect(Collectors.toSet())
        .contains(name)) {
      return true;
    }
    if (geolocations.getT3().stream().map(GeoCityDO::getNameText).collect(Collectors.toSet())
        .contains(name)) {
      return true;
    }

    return false;
  }

  private static void checkIpAddressThrowExcption(String ipAddress, Integer lineNumber) {
    List<String> ips = CsvUtils.convertCSVToList(ipAddress);
    String supplementMessage = lineNumber != null ? ", 行号: " + lineNumber : "";

    if (Sets.newHashSet(ips).size() < ips.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "存在重复的IP地址" + supplementMessage);
    }

    for (String ip : ips) {
      if (StringUtils.contains(ip, "-")) {
        String[] ipRange = StringUtils.split(ip, "-");
        // 起止都是正确的ip
        if (ipRange.length != 2 || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]))
            || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址" + supplementMessage);
        }
        if (!(NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V4)
            && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V4)
            || (NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V6)
                && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V6)))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              ip + "格式非法, 请输入正确的IP地址" + supplementMessage);
        }
      } else if (StringUtils.contains(ip, "/")) {
        if (!NetworkUtils.isCidr(ip)) {
          String[] ipAndMask = StringUtils.split(ip, "/");
          if (ipAndMask.length != 2
              || (ipAndMask.length == 2 && !(NetworkUtils.isInetAddress(ipAndMask[0])
                  && NetworkUtils.isInetAddress(ipAndMask[1])))) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                ip + "格式非法, 请输入正确的IP地址" + supplementMessage);
          }
        }
      } else if (!NetworkUtils.isInetAddress(ip)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            ip + "格式非法, 请输入正确的IP地址" + supplementMessage);
      }
    }
  }

  private void checkIpAddresssDuplicate(String pendingIpAddress,
      Map<String, String> locationNameIpDict, Integer lineNumber) {
    String supplementMessage = lineNumber != null ? ", 行号: " + lineNumber : "";

    // 获取已存在IP地址的范围
    List<Tuple2<String, Range<Long>>> existIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> existIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    locationNameIpDict.forEach((locationName, ipAddress) -> {
      CsvUtils.convertCSVToList(ipAddress).forEach(ip -> {
        ip = simplifyIpMask(ip);
        if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"), IpVersion.V4)
            || NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "-"), IpVersion.V4)) {
          Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
          existIpv4RangeList
              .add(Tuples.of(locationName, Range.closed(ip2Range.getT1(), ip2Range.getT2())));
        } else {// ipv6
          existIpv6RangeList.add(Tuples.of(locationName, ipv6ToRange(ip)));
        }
      });
    });

    // 获取本次受检IP地址的范围
    List<Tuple2<String, Range<Long>>> pendingIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> pendingIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    CsvUtils.convertCSVToList(pendingIpAddress).forEach(ip -> {
      ip = simplifyIpMask(ip);
      if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"), IpVersion.V4)
          || NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "-"), IpVersion.V4)) {
        Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
        pendingIpv4RangeList.add(Tuples.of(ip, Range.closed(ip2Range.getT1(), ip2Range.getT2())));
      } else {// ipv6
        pendingIpv6RangeList.add(Tuples.of(ip, ipv6ToRange(ip)));
      }
    });

    // 校验IPV4是否重复
    for (Tuple2<String, Range<Long>> pendingIpRange : pendingIpv4RangeList) {
      for (Tuple2<String, Range<Long>> existIpRange : existIpv4RangeList) {
        if (pendingIpRange.getT2().isConnected(existIpRange.getT2())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              String.format("IP地址/段 [%s]与已有的地区IP地址[%s]重复%s", pendingIpRange.getT1(),
                  existIpRange.getT1(), supplementMessage));
        }
      }
    }

    // 校验IPV6是否重复
    for (Tuple2<String, IPv6AddressRange> pendingIpRange : pendingIpv6RangeList) {
      for (Tuple2<String, IPv6AddressRange> existIpRange : existIpv6RangeList) {
        if (pendingIpRange.getT2().contains(existIpRange.getT2())) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              String.format("IP地址/段 [%s]与已有的地区IP地址[%s]重复%s", pendingIpRange.getT1(),
                  existIpRange.getT1(), supplementMessage));
        }
      }
    }
  }

  private String simplifyIpMask(String ip) {
    String newIp = ip;
    String[] ipAndMask = StringUtils.split(ip, "/");
    if (ipAndMask.length == 2 && NetworkUtils.isInetAddress(ipAndMask[1])) {
      int ipMask = NetworkUtils.isInetAddress(ipAndMask[1], IpVersion.V4)
          ? NetworkUtils.getNetMask(ipAndMask[1])
          : IPv6Address.fromString(ipAndMask[1]).numberOfTrailingZeroes();
      newIp = StringUtils.joinWith("/", ipAndMask[0], ipMask);
    }

    return newIp;
  }

  private static IPv6AddressRange ipv6ToRange(String ipv6) {
    if (StringUtils.contains(ipv6, "-")) {
      String[] ipRange = StringUtils.split(ipv6, "-");
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipRange[0]),
          IPv6Address.fromString(ipRange[1]));
    } else if (StringUtils.contains(ipv6, "/")) {
      return IPv6Network.fromString(ipv6);
    } else {
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipv6),
          IPv6Address.fromString(ipv6));
    }
  }

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_GEOCUSTOM,
        FpcCmsConstants.MQ_TAG_GEOKNOWLEDGE, FpcCmsConstants.MQ_TAG_GEOIPSETTING));
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#syncConfiguration(org.apache.rocketmq.common.message.Message)
   */
  @Override
  public int syncConfiguration(Message message) {
    int syncTotalCount = 0;
    if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_GEOKNOWLEDGE)) {
      syncTotalCount = syncGeoKnowledge(message);
      LOGGER.info("current sync geoKnowledge total: {}.", syncTotalCount);

      return syncTotalCount;
    } else {
      Map<String, Object> messageBody = MQMessageHelper.convertToMap(message);

      List<Map<String, Object>> messages = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (MapUtils.getBoolean(messageBody, "batch", false)) {
        messages.addAll(JsonHelper.deserialize(JsonHelper.serialize(messageBody.get("data")),
            new TypeReference<List<Map<String, Object>>>() {
            }));
      } else {
        messages.add(messageBody);
      }
      if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_GEOCUSTOM)) {
        syncTotalCount = messages.stream().mapToInt(item -> syncCustomCountry(item)).sum();
        LOGGER.info("current sync geoCustom total: {}.", syncTotalCount);
      }
      if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_GEOIPSETTING)) {
        syncTotalCount = messages.stream().mapToInt(item -> syncGeoIpSetting(item)).sum();
        LOGGER.info("current sync geoIpSetting total: {}.", syncTotalCount);
      }

      return syncTotalCount;
    }
  }

  private int syncCustomCountry(Map<String, Object> messageBody) {

    int syncCount = 0;

    String customCountryInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(customCountryInCmsId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");
    GeoCustomCountryBO customCountryBO = new GeoCustomCountryBO();
    customCountryBO.setId(customCountryInCmsId);
    customCountryBO.setCustomCountryInCmsId(customCountryInCmsId);
    customCountryBO.setName(MapUtils.getString(messageBody, "name"));
    customCountryBO.setCountryId(MapUtils.getString(messageBody, "countryId"));
    customCountryBO.setLongitude(MapUtils.getString(messageBody, "longitude"));
    customCountryBO.setLatitude(MapUtils.getString(messageBody, "latitude"));
    customCountryBO.setIpAddress(MapUtils.getString(messageBody, "ipAddress"));
    customCountryBO.setDescription(CMS_ASSIGNMENT);

    // 本次下发的GeoCustomCountry是否存在
    GeoCustomCountryBO exist = queryCustomCountryByCmsCustomCountryId(
        customCountryBO.getCustomCountryInCmsId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateCustomCountry(exist.getId(), customCountryBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                customCountryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveCustomCountry(customCountryBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                customCountryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteCustomCountry(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              customCountryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync geoCustom status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  private int syncGeoIpSetting(Map<String, Object> messageBody) {

    int syncCount = 0;

    String countryId = MapUtils.getString(messageBody, "countryId");
    if (StringUtils.isBlank(countryId)) {
      return syncCount;
    }

    GeoIpSettingBO geoIpSettingBO = new GeoIpSettingBO();
    geoIpSettingBO.setCountryId(MapUtils.getString(messageBody, "countryId"));
    geoIpSettingBO.setProvinceId(MapUtils.getString(messageBody, "provinceId"));
    geoIpSettingBO.setCityId(MapUtils.getString(messageBody, "cityId"));
    geoIpSettingBO.setIpAddress(MapUtils.getString(messageBody, "ipAddress"));

    try {
      updateGeoIpSetting(geoIpSettingBO, CMS_ASSIGNMENT);
      LogHelper.auditAssignOperate(
          globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
          geoIpSettingBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
      syncCount++;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync geoIpSetting status: [sync: {}]", syncCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  private int syncGeoKnowledge(Message message) {

    int syncCount = 0;
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    Path tempPath = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID());
    Path knowledgePath = Paths.get(HotPropertiesHelper.getProperty("file.geoip.knowledge.path"));
    try {
      FileUtils.writeByteArrayToFile(new File(tempPath.toString()), message.getBody());
      // 文件上传为临时文件
      GeoKnowledgeInfoDO infoDO = knowledgeDao.queryGeoKnowledgeInfos(tempPath.toString());
      if (StringUtils.isBlank(infoDO.getVersion())) {
        LOGGER.warn("地区库文件解析失败");
        return syncCount;
      }
      // 替换原文件
      Files.move(tempPath.toFile(), knowledgePath.toFile());
      syncCount++;
    } catch (IllegalStateException | IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "上传地区库文件失败");
    }
    return syncCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // 只能删除自定义地区，地区库不可删除
    int clearCount = 0;
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_GEOCUSTOM)) {
      List<String> customCountryIds = geoCustomCountryDao.queryGeoCustomCountryIds(onlyLocal);
      List<GeoIpSettingDO> geoIpSettings = geoIpSettingDao.queryGeoCountryIpSettingIds(onlyLocal);
      for (GeoIpSettingDO geoIpSetting : geoIpSettings) {
        geoIpSettingDao.deleteGeoIpSetting(geoIpSetting.getCountryId(),
            geoIpSetting.getProvinceId(), geoIpSetting.getCityId(), CMS_ASSIGNMENT);
      }
      for (String customCountryId : customCountryIds) {
        try {
          deleteCustomCountry(customCountryId, CMS_ASSIGNMENT, true);
          clearCount++;
        } catch (BusinessException e) {
          LOGGER.warn("delete customCountry failed. error msg: {}", e.getMessage());
          continue;
        }
      }
      return clearCount;
    }
    return clearCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_GEOCUSTOM)) {
      List<String> assignIds = geoCustomCountryDao.queryAssignGeoCustomCountryIds(beforeTime);
      List<String> geoIpAssignIds = geoIpSettingDao.queryGeoCountryIpSettingIds(beforeTime);
      assignIds.addAll(geoIpAssignIds);
      return assignIds;
    }

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_GEOKNOWLEDGE)) {
      try {
        String knowledgeFilePath = HotPropertiesHelper.getProperty("file.geoip.knowledge.path");
        return Lists
            .newArrayList(DigestUtils.md5Hex(new FileInputStream(new File(knowledgeFilePath))));
      } catch (IllegalStateException | IOException e) {
        LOGGER.warn("加密地区库文件失败.", e);
      }
    }
    return Lists.newArrayListWithCapacity(0);

  }

}
