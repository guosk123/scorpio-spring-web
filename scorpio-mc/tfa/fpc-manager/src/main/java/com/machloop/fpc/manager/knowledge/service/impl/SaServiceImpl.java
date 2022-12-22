package com.machloop.fpc.manager.knowledge.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.manager.knowledge.bo.SaApplicationBO;
import com.machloop.fpc.manager.knowledge.bo.SaCategoryBO;
import com.machloop.fpc.manager.knowledge.bo.SaCustomApplicationBO;
import com.machloop.fpc.manager.knowledge.bo.SaCustomCategoryBO;
import com.machloop.fpc.manager.knowledge.bo.SaCustomSubCategoryBO;
import com.machloop.fpc.manager.knowledge.bo.SaKnowledgeInfoBO;
import com.machloop.fpc.manager.knowledge.bo.SaSubCategoryBO;
import com.machloop.fpc.manager.knowledge.dao.SaCustomApplicationDao;
import com.machloop.fpc.manager.knowledge.dao.SaCustomCategoryDao;
import com.machloop.fpc.manager.knowledge.dao.SaCustomSubCategoryDao;
import com.machloop.fpc.manager.knowledge.dao.SaHierarchyDao;
import com.machloop.fpc.manager.knowledge.dao.SaKnowledgeDao;
import com.machloop.fpc.manager.knowledge.data.SaApplicationDO;
import com.machloop.fpc.manager.knowledge.data.SaCategoryDO;
import com.machloop.fpc.manager.knowledge.data.SaCustomApplicationDO;
import com.machloop.fpc.manager.knowledge.data.SaCustomCategoryDO;
import com.machloop.fpc.manager.knowledge.data.SaCustomSubCategoryDO;
import com.machloop.fpc.manager.knowledge.data.SaHierarchyDO;
import com.machloop.fpc.manager.knowledge.data.SaKnowledgeInfoDO;
import com.machloop.fpc.manager.knowledge.data.SaSubCategoryDO;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.ServiceService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月18日, fpc-manager
 */
@Order(1)
@Service
public class SaServiceImpl implements SaService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaServiceImpl.class);

  private static final int MAXIMUM_AMOUNT_CATEGORY = 50;
  private static final int MAXIMUM_AMOUNT_SUBCATEGORY = 100;
  private static final int MAXIMUM_AMOUNT_APPLICATION = 256;

  private static final int CUSTOM_CATEGORY_INITIATION_ID = 101;
  private static final int CUSTOM_SUBCATEGORY_INITIATION_ID = 101;

  private static final String SA_TYPE_SUBCATEGORY = String
      .valueOf(FpcConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY);
  private static final String SA_TYPE_APPLICATION = String
      .valueOf(FpcConstants.METRIC_TYPE_APPLICATION_APP);

  private static final String CUSTOM_CSV_TITLE = "`数据类型`,`名称`,`配置`,`描述`\n";

  private static final int MAX_NAME_LENGTH = 30;
  private static final int MAX_DESCRIPTION_LENGTH = 255;

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private SaKnowledgeDao knowledgeDao;

  @Autowired
  private SaCustomCategoryDao customCategoryDao;

  @Autowired
  private SaCustomSubCategoryDao customSubCategoryDao;

  @Autowired
  private SaCustomApplicationDao customAppDao;

  @Autowired
  private SaHierarchyDao hierarchyDao;

  @Autowired
  private SaProtocolService saProtocolService;

  @Autowired
  private ServiceService serviceService;

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryKnowledgeInfos()
   */
  @Override
  public SaKnowledgeInfoBO queryKnowledgeInfos() {
    SaKnowledgeInfoBO knowledgeBO = new SaKnowledgeInfoBO();

    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      return knowledgeBO;
    }

    SaKnowledgeInfoDO knowledgeDO = knowledgeDao.queryKnowledgeInfos(knowledgeFilePath);
    knowledgeBO.setImportDate(knowledgeDO.getImportDate());
    knowledgeBO.setReleaseDate(knowledgeDO.getReleaseDate());
    knowledgeBO.setVersion(knowledgeDO.getVersion());
    return knowledgeBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryKnowledgeRules()
   */
  @Override
  public Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
      List<SaApplicationBO>> queryKnowledgeRules() {
    List<SaCategoryBO> categoryList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SaSubCategoryBO> subCatList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SaApplicationBO> appList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> result = Tuples.of(categoryList, subCatList, appList);

    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      return result;
    }

    Tuple3<List<SaCategoryDO>, List<SaSubCategoryDO>,
        List<SaApplicationDO>> rules = knowledgeDao.queryKnowledgeRules(knowledgeFilePath);

    for (SaCategoryDO categoryDO : rules.getT1()) {
      SaCategoryBO categoryBO = new SaCategoryBO();
      BeanUtils.copyProperties(categoryDO, categoryBO);
      categoryList.add(categoryBO);
    }

    Map<String,
        String> subCategoryHierarchy = hierarchyDao
            .querySaHierarchys(SA_TYPE_SUBCATEGORY, null, null).stream().collect(
                Collectors.toMap(SaHierarchyDO::getSubCategoryId, SaHierarchyDO::getCategoryId));
    for (SaSubCategoryDO subCategoryDO : rules.getT2()) {
      SaSubCategoryBO subCategoryBO = new SaSubCategoryBO();
      BeanUtils.copyProperties(subCategoryDO, subCategoryBO);
      subCategoryBO.setCategoryId(MapUtils.getString(subCategoryHierarchy,
          subCategoryBO.getSubCategoryId(), subCategoryBO.getCategoryId()));
      subCatList.add(subCategoryBO);
    }

    Map<String,
        Tuple2<String, String>> appHierarchy = hierarchyDao
            .querySaHierarchys(SA_TYPE_APPLICATION, null, null).stream()
            .collect(Collectors.toMap(SaHierarchyDO::getApplicationId,
                hierarchy -> Tuples.of(hierarchy.getCategoryId(), hierarchy.getSubCategoryId())));
    for (SaApplicationDO applicationDO : rules.getT3()) {
      SaApplicationBO applicationBO = new SaApplicationBO();
      BeanUtils.copyProperties(applicationDO, applicationBO);
      Tuple2<String, String> tuple2 = appHierarchy.get(applicationBO.getApplicationId());
      if (tuple2 != null) {
        applicationBO.setCategoryId(tuple2.getT1());
        applicationBO.setSubCategoryId(tuple2.getT2());
      }
      appList.add(applicationBO);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#importKnowledges(org.springframework.web.multipart.MultipartFile)
   */
  @Override
  public synchronized SaKnowledgeInfoBO importKnowledges(MultipartFile file) {
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    Path tempPath = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID());
    Path knowledgePath = Paths.get(HotPropertiesHelper.getProperty("file.sa.knowledge.path"));
    try {
      // 文件上传为临时文件
      file.transferTo(tempPath.toFile());
      SaKnowledgeInfoDO infoDO = knowledgeDao.queryKnowledgeInfos(tempPath.toString());
      if (StringUtils.isBlank(infoDO.getVersion())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "上传SA知识库文件非法, 请上传正确的知识库文件");
      }

      // 替换原文件
      Files.move(tempPath.toFile(), knowledgePath.toFile());

      SaKnowledgeInfoBO knowledgeBO = new SaKnowledgeInfoBO();
      knowledgeBO.setImportDate(infoDO.getImportDate());
      knowledgeBO.setReleaseDate(infoDO.getReleaseDate());
      knowledgeBO.setVersion(infoDO.getVersion());
      return knowledgeBO;
    } catch (IllegalStateException | IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "上传SA知识库文件失败");
    }
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#exportCustomRules()
   */
  @Override
  public List<String> exportCustomRules() {
    List<String> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    result.add(CUSTOM_CSV_TITLE);

    // 预定义分类、子分类、应用 <id:name>
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = queryKnowledgeRules();
    Map<String, String> predefinedCategory = knowledgeRules.getT1().stream()
        .collect(Collectors.toMap(SaCategoryBO::getCategoryId, SaCategoryBO::getNameText));
    Map<String, String> predefinedSubCategory = knowledgeRules.getT2().stream()
        .collect(Collectors.toMap(SaSubCategoryBO::getSubCategoryId, SaSubCategoryBO::getNameText));
    Map<String, String> predefinedApplication = knowledgeRules.getT3().stream()
        .collect(Collectors.toMap(SaApplicationBO::getApplicationId, SaApplicationBO::getNameText));

    // 自定义分类、子分类、应用 <id:name>
    List<SaCustomCategoryDO> saCustomCategorys = customCategoryDao.querySaCustomCategorys();
    saCustomCategorys.forEach(customCategory -> {
      predefinedCategory.put(customCategory.getCategoryId(), customCategory.getName());
    });
    List<SaCustomSubCategoryDO> saCustomSubCategorys = customSubCategoryDao
        .querySaCustomSubCategorys();
    saCustomSubCategorys.forEach(customSubCategory -> {
      predefinedSubCategory.put(customSubCategory.getSubCategoryId(), customSubCategory.getName());
    });
    List<SaCustomApplicationDO> saCustomApps = customAppDao.querySaCustomApps();

    /**
     * 自定义分类集合
     */
    // 自定义分类包含子分类关系映射
    Map<String, List<String>> categorySubCategorys = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    saCustomSubCategorys.forEach(customSubCategory -> {
      List<String> subCategoryNames = categorySubCategorys.getOrDefault(
          customSubCategory.getCategoryId(),
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
      subCategoryNames.add(customSubCategory.getName());
      categorySubCategorys.put(customSubCategory.getCategoryId(), subCategoryNames);
    });
    hierarchyDao.querySaHierarchys(SA_TYPE_SUBCATEGORY, null, null).forEach(hierarchy -> {
      List<String> subCategoryNames = categorySubCategorys.getOrDefault(hierarchy.getCategoryId(),
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
      subCategoryNames.add(predefinedSubCategory.get(hierarchy.getSubCategoryId()));
      categorySubCategorys.put(hierarchy.getCategoryId(), subCategoryNames);
    });

    // 填充自定义分类配置
    saCustomCategorys.forEach(customCategory -> {
      result.add(
          CsvUtils.spliceRowData(FpcConstants.SA_TYPE_CUSTOM_CATEGORY, customCategory.getName(),
              StringUtils.join(categorySubCategorys.get(customCategory.getCategoryId()), "/"),
              customCategory.getDescription()));
    });

    /**
     * 自定义子分类集合
     */
    // 自定义子分类包含应用关系映射
    Map<String, List<String>> subCategoryApplications = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    saCustomApps.forEach(customApp -> {
      List<String> appNames = subCategoryApplications.getOrDefault(customApp.getSubCategoryId(),
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
      appNames.add(customApp.getName());
      subCategoryApplications.put(customApp.getSubCategoryId(), appNames);
    });
    hierarchyDao.querySaHierarchys(SA_TYPE_APPLICATION, null, null).forEach(hierarchy -> {
      List<String> appNames = subCategoryApplications.getOrDefault(hierarchy.getSubCategoryId(),
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
      appNames.add(predefinedApplication.get(hierarchy.getApplicationId()));
      subCategoryApplications.put(hierarchy.getSubCategoryId(), appNames);
    });

    // 填充自定义子分类配置
    saCustomSubCategorys.forEach(customSubCategory -> {
      Map<String, String> configMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      configMap.put("category", predefinedCategory.get(customSubCategory.getCategoryId()));
      configMap.put("applications",
          StringUtils.join(subCategoryApplications.get(customSubCategory.getSubCategoryId()), "/"));

      result.add(CsvUtils.spliceRowData(FpcConstants.SA_TYPE_CUSTOM_SUBCATEGORY,
          customSubCategory.getName(), JsonHelper.serialize(configMap, false),
          customSubCategory.getDescription()));
    });

    /**
     * 自定义应用集合
     */
    // 应用层协议<id:name>
    Map<String, String> protocols = saProtocolService.queryProtocols().stream().collect(Collectors
        .toMap(map -> (String) map.get("protocolId"), map -> (String) map.get("nameText")));

    // 填充自定义应用配置
    saCustomApps.forEach(customApp -> {
      Map<String, String> configMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      configMap.put("category", predefinedCategory.get(customApp.getCategoryId()));
      configMap.put("subcategory", predefinedSubCategory.get(customApp.getSubCategoryId()));
      configMap.put("l7Protocol", protocols.get(customApp.getL7ProtocolId()));
      configMap.put("rule", customApp.getRule());

      result.add(CsvUtils.spliceRowData(FpcConstants.SA_TYPE_CUSTOM_APPLICATION,
          customApp.getName(), JsonHelper.serialize(configMap, false), customApp.getDescription()));
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#importCustomRules(org.springframework.web.multipart.MultipartFile, java.lang.String)
   */
  @Transactional
  @Override
  public synchronized int importCustomRules(MultipartFile file, String operatorId) {
    LOGGER.info("begin to import sa custom rule, file name :{}", file.getOriginalFilename());

    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = queryKnowledgeRules();
    // 分类
    Map<String, String> categorys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    knowledgeRules.getT1().forEach(category -> {
      categorys.put(category.getNameText(), category.getCategoryId());
    });
    List<SaCustomCategoryDO> saCustomCategorys = customCategoryDao.querySaCustomCategorys();
    int customCategorySize = saCustomCategorys.size();
    saCustomCategorys.forEach(customCategory -> {
      categorys.put(customCategory.getName(), customCategory.getCategoryId());
    });
    // 子分类
    Map<String, String> subCategorys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    knowledgeRules.getT2().forEach(subCategory -> {
      subCategorys.put(subCategory.getNameText(), subCategory.getSubCategoryId());
    });
    List<SaCustomSubCategoryDO> saCustomSubCategorys = customSubCategoryDao
        .querySaCustomSubCategorys();
    int customSubCategorySize = saCustomSubCategorys.size();
    saCustomSubCategorys.forEach(customSubCategory -> {
      subCategorys.put(customSubCategory.getName(), customSubCategory.getSubCategoryId());
    });
    // 应用
    Map<String, String> applications = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    knowledgeRules.getT3().forEach(application -> {
      applications.put(application.getNameText(), application.getApplicationId());
    });
    List<SaCustomApplicationDO> saCustomApps = customAppDao.querySaCustomApps();
    int customAppSize = saCustomApps.size();
    saCustomApps.forEach(customApp -> {
      applications.put(customApp.getName(), customApp.getApplicationId());
    });

    // 解析文件内容
    String line = "";
    int lineNumber = 0;
    Map<Integer,
        List<String>> categoryLines = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<Integer, List<String>> subcategoryLines = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<Integer,
        List<String>> appLines = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    try (InputStream stream = file.getInputStream();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        lineNumber++;

        // 跳过首行
        if (lineNumber == 1) {
          LOGGER.info("pass title, line: [{}]", line);
          continue;
        }

        List<String> contents = CsvUtils.splitRowData(line);
        if (contents.size() == 0) {
          LOGGER.info("skip blank line, line number: [{}]", lineNumber);
          continue;
        } else if (contents.size() != CsvUtils.convertCSVToList(CUSTOM_CSV_TITLE).size()) {
          LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber + ", 内容: " + line);
        }

        switch (contents.get(0)) {
          case FpcConstants.SA_TYPE_CUSTOM_CATEGORY:
            categoryLines.put(lineNumber, contents);
            break;
          case FpcConstants.SA_TYPE_CUSTOM_SUBCATEGORY:
            subcategoryLines.put(lineNumber, contents);
            break;
          case FpcConstants.SA_TYPE_CUSTOM_APPLICATION:
            appLines.put(lineNumber, contents);
            break;
          default:
            throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的数据类型");
        }
      }
    } catch (IOException e) {
      LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, line);
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "文件导入失败，错误行号：" + lineNumber);
    }

    int successCount = 0;
    /**
     * newSubcategorys：当前csv文件中新增分类中包含的新的子分类集合
     * newApps：当前csv文件中新增子分类中包含的新的应用集合
     */
    Map<String,
        String> newSubcategorys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, String> newApps = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (MapUtils.isNotEmpty(categoryLines)) {
      successCount = analyseCustomCategoryFile(categoryLines, categorys, subCategorys,
          newSubcategorys, customCategorySize, customSubCategorySize, operatorId);
    }
    if (MapUtils.isNotEmpty(subcategoryLines)) {
      successCount = analyseCustomSubCategoryFile(subcategoryLines, categorys, subCategorys,
          newSubcategorys, applications, newApps, customSubCategorySize, customAppSize, operatorId);
    }
    if (MapUtils.isNotEmpty(appLines)) {
      successCount = analyseCustomApplicationFile(appLines, categorys, subCategorys, applications,
          newApps, customAppSize, operatorId);
    }

    LOGGER.info("success to import sa custom rule.total: [{}]", successCount);
    return successCount;
  }

  /**
   * 导入自定义分类
   * @param categoryLines
   * @param categorys
   * @param subCategorys
   * @param newSubcategorys
   * @param customCategorySize
   * @param customSubCategorySize
   * @param operatorId
   * @return
   */
  private int analyseCustomCategoryFile(Map<Integer, List<String>> categoryLines,
      Map<String, String> categorys, Map<String, String> subCategorys,
      Map<String, String> newSubcategorys, int customCategorySize, int customSubCategorySize,
      String operatorId) {
    // 分类剩余可新增数量
    int categorySurplus = MAXIMUM_AMOUNT_CATEGORY - customCategorySize;
    if (categorySurplus <= 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "自定义分类数量已达到上限,最多添加" + MAXIMUM_AMOUNT_CATEGORY + "个");
    }
    // 子分类剩余可新增数量
    int subcategorySurplus = MAXIMUM_AMOUNT_SUBCATEGORY - customCategorySize;

    Set<String> existNames = Sets.newHashSet(categorys.keySet());
    List<
        SaCustomCategoryBO> importData = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    int size = 0;
    for (Entry<Integer, List<String>> line : categoryLines.entrySet()) {
      Integer lineNumber = line.getKey();
      List<String> contents = line.getValue();
      try {
        size++;
        if (size > categorySurplus) {
          LOGGER.warn("import file error, limit exceeded.");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容超出剩余可添加自定义分类数量[" + categorySurplus + "]");
        }

        String name = StringUtils.trim(contents.get(1));
        String subCategoryNames = StringUtils.trim(contents.get(2));
        String description = StringUtils.trim(contents.get(3));

        // 整行内容非空校验
        if (StringUtils.isBlank(name)) {
          LOGGER.warn("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber);
        }

        // 名称校验
        if (name.length() > MAX_NAME_LENGTH) {
          LOGGER.warn("import file error, name length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 分类名称长度超出限制：" + MAX_NAME_LENGTH + ", 行号: " + lineNumber);
        }
        if (existNames.contains(name)) {
          LOGGER.warn("import file error, name exist, lineNumber: {}, content: {}", lineNumber,
              contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 分类名称已存在, 行号: " + lineNumber);
        }
        existNames.add(name);

        // 校验子分类
        List<String> selectedSubCategoryBack = Lists.newArrayList();
        if (StringUtils.isNotBlank(subCategoryNames)) {
          List<String> selectedSubCategorys = Lists
              .newArrayList(StringUtils.split(subCategoryNames, "/"));
          selectedSubCategoryBack = Lists.newArrayList(selectedSubCategorys);
          selectedSubCategorys.removeAll(subCategorys.keySet());
          selectedSubCategorys.removeAll(newSubcategorys.keySet());
          for (String subCategoryName : selectedSubCategorys) {
            if (subcategorySurplus > 0) {
              newSubcategorys.put(subCategoryName, generateSubCategoryId());
              subcategorySurplus--;
            } else {
              throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                  "自定义子分类数量已达到上限,最多添加" + MAXIMUM_AMOUNT_SUBCATEGORY + "个");
            }
          }
        }

        // 校验描述长度
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
          LOGGER.warn(
              "import file error, description length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 描述长度超出限制：" + MAX_DESCRIPTION_LENGTH + ", 行号: " + lineNumber);
        }

        SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
        customCategoryBO.setName(name);
        customCategoryBO.setSubCategoryIds(
            StringUtils.join(selectedSubCategoryBack.stream().map(subCategoryName -> {
              String subCategoryId = subCategorys.get(subCategoryName);
              return StringUtils.isNotBlank(subCategoryId) ? subCategoryId
                  : newSubcategorys.get(subCategoryName);
            }).collect(Collectors.toList()), ","));
        customCategoryBO.setDescription(description);
        importData.add(customCategoryBO);

      } catch (Exception e) {
        LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, contents);
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "文件导入失败，错误行号：" + lineNumber);
      }
    }

    importData.forEach(customCategoryBO -> {
      saveCustomCategory(customCategoryBO, operatorId);
      categorys.put(customCategoryBO.getName(), customCategoryBO.getCategoryId());
    });

    return importData.size();
  }

  /**
   * 导入自定义子分类
   * @param subcategoryLines
   * @param categorys
   * @param subCategorys
   * @param newSubcategorys
   * @param applications
   * @param newApps
   * @param customSubcategorySize
   * @param customAppSize
   * @param operatorId
   * @return
   */
  private int analyseCustomSubCategoryFile(Map<Integer, List<String>> subcategoryLines,
      Map<String, String> categorys, Map<String, String> subCategorys,
      Map<String, String> newSubcategorys, Map<String, String> applications,
      Map<String, String> newApps, int customSubcategorySize, int customAppSize,
      String operatorId) {
    // 子分类剩余可新增数量
    int subcategorySurplus = MAXIMUM_AMOUNT_SUBCATEGORY - customSubcategorySize;
    if (subcategorySurplus <= 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "自定义子分类数量已达到上限,最多添加" + MAXIMUM_AMOUNT_SUBCATEGORY + "个");
    }
    // 应用剩余可新增数量
    int appSurplus = MAXIMUM_AMOUNT_APPLICATION - customAppSize;

    Set<String> existNames = Sets.newHashSet(subCategorys.keySet());
    List<SaCustomSubCategoryBO> importData = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    int size = 0;
    for (Entry<Integer, List<String>> line : subcategoryLines.entrySet()) {
      Integer lineNumber = line.getKey();
      List<String> contents = line.getValue();
      try {
        size++;

        if (size > subcategorySurplus) {
          LOGGER.warn("import file error, limit exceeded.");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容超出剩余可添加自定义子分类数量[" + subcategorySurplus + "]");
        }

        String name = StringUtils.trim(contents.get(1));
        Map<String, String> configMap = JsonHelper.deserialize(StringUtils.trim(contents.get(2)),
            new TypeReference<Map<String, String>>() {
            }, false);
        String categoryName = configMap.get("category");
        String applicationNames = configMap.get("applications");
        String description = StringUtils.trim(contents.get(3));
        // 整行内容非空校验
        if (StringUtils.isAnyBlank(name, categoryName)) {
          LOGGER.warn("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber);
        }

        // 名称校验
        if (name.length() > MAX_NAME_LENGTH) {
          LOGGER.warn("import file error, name length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 子分类名称长度超出限制：" + MAX_NAME_LENGTH + ", 行号: " + lineNumber);
        }
        if (existNames.contains(name)) {
          LOGGER.warn("import file error, name exist, lineNumber: {}, content: {}", lineNumber,
              contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 子分类名称已存在, 行号: " + lineNumber);
        }
        existNames.add(name);

        // 校验分类
        if (!categorys.keySet().contains(categoryName)) {
          LOGGER.warn("import file error, category not exist, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 不存在的分类[" + categoryName + "], 行号: " + lineNumber);
        }

        // 校验应用
        List<String> selectedAppBack = Lists.newArrayList();
        if (StringUtils.isNotBlank(applicationNames)) {
          List<String> selectedApps = Lists.newArrayList(StringUtils.split(applicationNames, "/"));
          selectedAppBack = Lists.newArrayList(selectedApps);
          selectedApps.removeAll(applications.keySet());
          selectedApps.removeAll(newApps.keySet());
          for (String appName : selectedApps) {
            if (appSurplus > 0) {
              String applicationId = globalSettingService.generateSequence(
                  ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_APPLICATION_SEQ_KEY, "");
              newApps.put(appName, applicationId);
              appSurplus--;
            } else {
              throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
                  "自定义应用数量已达到上限,最多添加" + MAXIMUM_AMOUNT_APPLICATION + "个");
            }
          }
        }

        // 校验描述长度
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
          LOGGER.warn(
              "import file error, description length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 描述长度超出限制：" + MAX_DESCRIPTION_LENGTH + ", 行号: " + lineNumber);
        }

        SaCustomSubCategoryBO customSubCategoryBO = new SaCustomSubCategoryBO();
        if (newSubcategorys.containsKey(name)) {
          customSubCategoryBO.setSubCategoryId(newSubcategorys.get(name));
        }
        customSubCategoryBO.setName(name);
        customSubCategoryBO.setCategoryId(categorys.get(categoryName));
        customSubCategoryBO
            .setApplicationIds(StringUtils.join(selectedAppBack.stream().map(appName -> {
              String appId = applications.get(appName);
              return StringUtils.isNotBlank(appId) ? appId : newApps.get(appName);
            }).collect(Collectors.toList()), ","));
        customSubCategoryBO.setDescription(description);
        importData.add(customSubCategoryBO);
      } catch (Exception e) {
        LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, contents);
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "文件导入失败，错误行号：" + lineNumber);
      }
    }

    importData.forEach(customSubCategoryBO -> {
      saveCustomSubCategory(customSubCategoryBO, operatorId);
      subCategorys.put(customSubCategoryBO.getName(), customSubCategoryBO.getSubCategoryId());
    });

    return importData.size();
  }

  /**
   * 导入自定义应用
   * @param appLines
   * @param categorys
   * @param subCategorys
   * @param applications
   * @param currentCustomSize
   * @param operatorId
   * @return
   */
  private int analyseCustomApplicationFile(Map<Integer, List<String>> appLines,
      Map<String, String> categorys, Map<String, String> subCategorys,
      Map<String, String> applications, Map<String, String> newApps, int currentCustomSize,
      String operatorId) {
    int surplus = MAXIMUM_AMOUNT_APPLICATION - currentCustomSize;
    if (surplus <= 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "自定义应用数量已达到上限,最多添加" + MAXIMUM_AMOUNT_APPLICATION + "个");
    }

    Map<String, String> protocols = saProtocolService.queryProtocols().stream().collect(Collectors
        .toMap(map -> (String) map.get("nameText"), map -> (String) map.get("protocolId")));

    Set<String> existNames = Sets.newHashSet(applications.keySet());
    List<SaCustomApplicationBO> importData = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    int size = 0;
    for (Entry<Integer, List<String>> line : appLines.entrySet()) {
      Integer lineNumber = line.getKey();
      List<String> contents = line.getValue();
      try {
        size++;

        if (size > surplus) {
          LOGGER.warn("import file error, limit exceeded.");
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容超出剩余可添加自定义应用数量[" + surplus + "]");
        }

        String name = StringUtils.trim(contents.get(1));
        Map<String, Object> configMap = JsonHelper.deserialize(StringUtils.trim(contents.get(2)),
            new TypeReference<Map<String, String>>() {
            }, false);
        String categoryName = MapUtils.getString(configMap, "category");
        String subCategoryName = MapUtils.getString(configMap, "subcategory");
        String l7ProtocolName = MapUtils.getString(configMap, "l7Protocol");
        String rule = MapUtils.getString(configMap, "rule");
        String description = StringUtils.trim(contents.get(3));
        // 整行内容非空校验
        if (StringUtils.isAnyBlank(name, categoryName, subCategoryName, l7ProtocolName)) {
          LOGGER.warn("import file error, contain null value, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 文件内容解析错误, 行号: " + lineNumber);
        }

        // 名称校验
        if (name.length() > MAX_NAME_LENGTH) {
          LOGGER.warn("import file error, name length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 应用名称长度超出限制：" + MAX_NAME_LENGTH + ", 行号: " + lineNumber);
        }
        if (existNames.contains(name)) {
          LOGGER.warn("import file error, name exist, lineNumber: {}, content: {}", lineNumber,
              contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 应用名称已存在, 行号: " + lineNumber);
        }
        existNames.add(name);

        // 校验分类
        if (!categorys.keySet().contains(categoryName)) {
          LOGGER.warn("import file error, category not exist, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 不存在的分类[" + categoryName + "], 行号: " + lineNumber);
        }

        // 校验子分类
        if (!subCategorys.keySet().contains(subCategoryName)) {
          LOGGER.warn("import file error, subCategory not exist, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 不存在的子分类[" + subCategoryName + "], 行号: " + lineNumber);
        }

        // 校验协议
        if (!protocols.keySet().contains(l7ProtocolName)) {
          LOGGER.warn("import file error, l7protocol not exist, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 不存在的应用层协议[" + l7ProtocolName + "], 行号: " + lineNumber);
        }

        // 校验描述长度
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
          LOGGER.warn(
              "import file error, description length exceeds limit, lineNumber: {}, content: {}",
              lineNumber, contents);
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "导入失败, 描述长度超出限制：" + MAX_DESCRIPTION_LENGTH + ", 行号: " + lineNumber);
        }

        SaCustomApplicationBO customApplicationBO = new SaCustomApplicationBO();
        if (newApps.containsKey(name)) {
          customApplicationBO.setApplicationId(newApps.get(name));
        }
        customApplicationBO.setName(name);
        customApplicationBO.setCategoryId(categorys.get(categoryName));
        customApplicationBO.setSubCategoryId(subCategorys.get(subCategoryName));
        customApplicationBO.setL7ProtocolId(protocols.get(l7ProtocolName));
        customApplicationBO.setRule(rule);
        customApplicationBO.setDescription(description);
        importData.add(customApplicationBO);
      } catch (Exception e) {
        LOGGER.warn("import file error, lineNumber: {}, content: {}", lineNumber, contents);
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "文件导入失败，错误行号：" + lineNumber);
      }
    }

    importData.forEach(customApplicationBO -> {
      saveCustomApp(customApplicationBO, operatorId);
      applications.put(customApplicationBO.getName(), customApplicationBO.getApplicationId());
    });

    return importData.size();
  }

  /*
   * 自定义分类
   */
  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomCategorys()
   */
  @Override
  public List<SaCustomCategoryBO> queryCustomCategorys() {
    List<SaCustomCategoryDO> categoryDOList = customCategoryDao.querySaCustomCategorys();
    List<SaCustomCategoryBO> categoryBOList = Lists.newArrayListWithCapacity(categoryDOList.size());
    for (SaCustomCategoryDO categoryDO : categoryDOList) {
      SaCustomCategoryBO categoryBO = new SaCustomCategoryBO();
      BeanUtils.copyProperties(categoryDO, categoryBO);
      categoryBO.setCreateTime(DateUtils.toStringISO8601(categoryDO.getCreateTime()));
      categoryBO.setUpdateTime(DateUtils.toStringISO8601(categoryDO.getUpdateTime()));
      categoryBOList.add(categoryBO);
    }
    return categoryBOList;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomCategory(java.lang.String)
   */
  @Override
  public SaCustomCategoryBO queryCustomCategory(String id) {
    SaCustomCategoryDO categoryDO = customCategoryDao.querySaCustomCategory(id);
    SaCustomCategoryBO categoryBO = new SaCustomCategoryBO();
    BeanUtils.copyProperties(categoryDO, categoryBO);
    categoryBO.setCreateTime(DateUtils.toStringISO8601(categoryDO.getCreateTime()));
    categoryBO.setUpdateTime(DateUtils.toStringISO8601(categoryDO.getUpdateTime()));

    List<SaCustomSubCategoryDO> customSubCategorys = customSubCategoryDao
        .querySubCategoryByCategoryId(categoryDO.getCategoryId());
    // 分类所包含的自定义子分类
    List<String> subCategoryIds = customSubCategorys.stream()
        .map(subCategory -> subCategory.getSubCategoryId()).collect(Collectors.toList());
    // 分类所包含的预定义子分类
    subCategoryIds.addAll(
        hierarchyDao.querySaHierarchys(SA_TYPE_SUBCATEGORY, categoryDO.getCategoryId(), null)
            .stream().map(SaHierarchyDO::getSubCategoryId).collect(Collectors.toList()));

    categoryBO.setSubCategoryIds(StringUtils.join(subCategoryIds, ","));

    return categoryBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomCategoryByCmsCategoryId(java.lang.String)
   */
  @Override
  public SaCustomCategoryBO queryCustomCategoryByCmsCategoryId(String cmsCategoryId) {

    SaCustomCategoryDO customCategoryDO = customCategoryDao
        .queryCustomCategoryByCmsCategoryId(cmsCategoryId);

    SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
    BeanUtils.copyProperties(customCategoryDO, customCategoryBO);

    return customCategoryBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#saveCustomCategory(com.machloop.fpc.manager.knowledge.bo.SaCustomCategoryBO, java.lang.String)
   */
  @Transactional
  @Override
  public synchronized SaCustomCategoryBO saveCustomCategory(SaCustomCategoryBO customCategoryBO,
      String operatorId) {
    if (customCategoryDao.countSaCustomCategorys() >= MAXIMUM_AMOUNT_CATEGORY) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION,
          "最多只支持" + MAXIMUM_AMOUNT_CATEGORY + "条自定义分类");
    }

    // 校验名称是否重复
    SaCustomCategoryDO existNameDO = customCategoryDao
        .querySaCustomCategoryByName(customCategoryBO.getName());
    if (StringUtils.isNotBlank(existNameDO.getId()) || isNameRepetition(customCategoryBO.getName(),
        FpcConstants.METRIC_TYPE_APPLICATION_CATEGORY)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "分类名称不能重复，请修改分类名称");
    }

    // 生成分类ID
    // 区分本机新建或上级cms下发
    if (StringUtils.isBlank(customCategoryBO.getCategoryId())) {
      String categoryId = generateCategoryId();
      customCategoryBO.setCategoryId(categoryId);
    }
    customCategoryBO.setOperatorId(operatorId);
    SaCustomCategoryDO customCategoryDO = new SaCustomCategoryDO();
    BeanUtils.copyProperties(customCategoryBO, customCategoryDO);

    // 保存分类
    customCategoryDO = customCategoryDao.saveOrRecoverSaCustomCategory(customCategoryDO);

    // 预定义子分类集合
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "SA规则库获取失败");
    }
    List<String> predefinedSubCategoryIds = knowledgeDao.queryKnowledgeRules(knowledgeFilePath)
        .getT2().stream().map(SaSubCategoryDO::getSubCategoryId).collect(Collectors.toList());

    // 更新配置的子分类相关属性
    List<
        String> subCategoryIdList = CsvUtils.convertCSVToList(customCategoryBO.getSubCategoryIds());
    List<String> variationSubCategoryIds = Lists.newArrayList(subCategoryIdList);
    // 该分类包含的自定义子分类
    List<String> customSubCategoryIds = subCategoryIdList.stream()
        .filter(subCategoryId -> !predefinedSubCategoryIds.contains(subCategoryId))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(customSubCategoryIds)) {
      // 修改子分类的所属关系
      customSubCategoryDao.batchUpdateCategoryId(customSubCategoryIds,
          customCategoryDO.getCategoryId(), operatorId);
    }
    // 该分类包含的预定义子分类
    subCategoryIdList.removeAll(customSubCategoryIds);
    if (CollectionUtils.isNotEmpty(subCategoryIdList)) {
      // 删除原有关系
      hierarchyDao.batchDeleteBySubCategoryIds(SA_TYPE_SUBCATEGORY, subCategoryIdList);

      // 添加到预定义关系变更表
      final String fCategoryId = customCategoryDO.getCategoryId();
      List<SaHierarchyDO> saHierarchys = subCategoryIdList.stream().map(subCategoryId -> {
        SaHierarchyDO saHierarchyDO = new SaHierarchyDO();
        saHierarchyDO.setType(SA_TYPE_SUBCATEGORY);
        saHierarchyDO.setCategoryId(fCategoryId);
        saHierarchyDO.setSubCategoryId(subCategoryId);
        saHierarchyDO.setApplicationId("");
        saHierarchyDO.setOperatorId(operatorId);

        return saHierarchyDO;
      }).collect(Collectors.toList());
      hierarchyDao.batchSaveSaHierarchy(saHierarchys);
    }

    // 更新配置的子分类下所属应用的相关属性
    if (CollectionUtils.isNotEmpty(variationSubCategoryIds)) {
      customAppDao.batchUpdateAppsBySubCategory(variationSubCategoryIds,
          customCategoryDO.getCategoryId(), operatorId);
      hierarchyDao.batchUpdateAppsBySubCategory(variationSubCategoryIds,
          customCategoryDO.getCategoryId(), operatorId);
    }

    return queryCustomCategory(customCategoryDO.getId());
  }

  private String generateCategoryId() {
    String categoryId = globalSettingService
        .generateSequence(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_CATEGORY_SEQ_KEY, "");
    if (Integer.parseInt(categoryId) == (CUSTOM_CATEGORY_INITIATION_ID + MAXIMUM_AMOUNT_CATEGORY)) {
      globalSettingService.setValue(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_CATEGORY_SEQ_KEY,
          CUSTOM_CATEGORY_INITIATION_ID + "");
      categoryId = globalSettingService
          .generateSequence(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_CATEGORY_SEQ_KEY, "");
    }

    List<String> existCategoryIds = customCategoryDao.querySaCustomCategorys().stream()
        .map(customCategory -> customCategory.getCategoryId()).collect(Collectors.toList());
    while (true) {
      if (!existCategoryIds.contains(categoryId)) {
        break;
      }
      categoryId = globalSettingService
          .generateSequence(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_CATEGORY_SEQ_KEY, "");
    }

    return categoryId;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#updateCustomCategory(java.lang.String, com.machloop.fpc.manager.knowledge.bo.SaCustomCategoryBO, java.lang.String)
   */
  @Transactional
  @Override
  public SaCustomCategoryBO updateCustomCategory(String id, SaCustomCategoryBO customCategoryBO,
      String operatorId) {
    SaCustomCategoryDO existCustomCategory = customCategoryDao.querySaCustomCategory(id);
    if (StringUtils.isBlank(existCustomCategory.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该记录不存在");
    }

    // 校验名称是否重复
    SaCustomCategoryDO existNameDO = customCategoryDao
        .querySaCustomCategoryByName(customCategoryBO.getName());
    if ((StringUtils.isNotBlank(existNameDO.getId())
        && !StringUtils.equals(existNameDO.getId(), id))
        || isNameRepetition(customCategoryBO.getName(),
            FpcConstants.METRIC_TYPE_APPLICATION_CATEGORY)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "分类名称不能重复，请修改分类名称");
    }

    // 修改分类
    SaCustomCategoryDO customCategoryDO = new SaCustomCategoryDO();
    BeanUtils.copyProperties(existCustomCategory, customCategoryDO);
    customCategoryDO.setName(customCategoryBO.getName());
    customCategoryDO.setDescription(customCategoryBO.getDescription());
    customCategoryDO.setOperatorId(operatorId);
    customCategoryDao.updateSaCustomCategory(customCategoryDO);

    // 如果该分类移除了所有预定义子分类（如果该分类下有自定义子分类不能全部移除）
    List<
        String> subCategoryIdList = CsvUtils.convertCSVToList(customCategoryBO.getSubCategoryIds());
    if (CollectionUtils.isEmpty(subCategoryIdList)) {
      hierarchyDao.batchDeleteByCategoryId(customCategoryDO.getCategoryId());
      return queryCustomCategory(id);
    }

    // 预定义子分类集合
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "SA规则库获取失败");
    }
    List<String> predefinedSubCategoryIds = knowledgeDao.queryKnowledgeRules(knowledgeFilePath)
        .getT2().stream().map(SaSubCategoryDO::getSubCategoryId).collect(Collectors.toList());

    // 更新配置的子分类相关属性
    List<String> variationSubCategoryIds = Lists.newArrayList(subCategoryIdList);
    // 该分类包含的自定义子分类
    List<String> customSubCategoryIds = subCategoryIdList.stream()
        .filter(subCategoryId -> !predefinedSubCategoryIds.contains(subCategoryId))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(customSubCategoryIds)) {
      customSubCategoryDao.batchUpdateCategoryId(customSubCategoryIds,
          customCategoryDO.getCategoryId(), operatorId);
    }

    // 该分类包含的预定义子分类
    subCategoryIdList.removeAll(customSubCategoryIds);

    // 删除旧数据
    List<String> existSubCategoryIdList = hierarchyDao
        .querySaHierarchys(SA_TYPE_SUBCATEGORY, customCategoryDO.getCategoryId(), null).stream()
        .map(SaHierarchyDO::getSubCategoryId).collect(Collectors.toList());
    existSubCategoryIdList.addAll(subCategoryIdList);
    if (CollectionUtils.isNotEmpty(existSubCategoryIdList)) {
      hierarchyDao.batchDeleteBySubCategoryIds(SA_TYPE_SUBCATEGORY, existSubCategoryIdList);
    }

    // 添加新的预定义子分类关系数据
    List<SaHierarchyDO> saHierarchys = subCategoryIdList.stream().map(subCategoryId -> {
      SaHierarchyDO saHierarchyDO = new SaHierarchyDO();
      saHierarchyDO.setType(SA_TYPE_SUBCATEGORY);
      saHierarchyDO.setCategoryId(customCategoryDO.getCategoryId());
      saHierarchyDO.setSubCategoryId(subCategoryId);
      saHierarchyDO.setApplicationId("");
      saHierarchyDO.setOperatorId(operatorId);

      return saHierarchyDO;
    }).collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(saHierarchys)) {
      hierarchyDao.batchSaveSaHierarchy(saHierarchys);
    }

    // 更新配置的子分类下所属应用的相关属性
    if (CollectionUtils.isNotEmpty(variationSubCategoryIds)) {
      customAppDao.batchUpdateAppsBySubCategory(variationSubCategoryIds,
          customCategoryDO.getCategoryId(), operatorId);
      hierarchyDao.batchUpdateAppsBySubCategory(variationSubCategoryIds,
          customCategoryDO.getCategoryId(), operatorId);
    }

    return queryCustomCategory(id);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#deleteCustomCategory(java.lang.String, java.lang.String)
   */
  @Override
  public SaCustomCategoryBO deleteCustomCategory(String id, String operatorId,
      boolean forceDelete) {
    SaCustomCategoryBO customCategory = queryCustomCategory(id);

    if (!forceDelete && StringUtils.isBlank(customCategory.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "自定义分类不存在");
    }

    if (!forceDelete && StringUtils.isNotBlank(customCategory.getSubCategoryIds())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "该自定义分类下包含子分类，无法删除");
    }

    customCategoryDao.deleteSaCustomCategory(id, operatorId);

    return customCategory;
  }

  /**
   * 自定义子分类
   */
  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomSubCategorys()
   */
  @Override
  public List<SaCustomSubCategoryBO> queryCustomSubCategorys() {
    List<
        SaCustomSubCategoryDO> subCategoryDOList = customSubCategoryDao.querySaCustomSubCategorys();
    List<SaCustomSubCategoryBO> subCategoryBOList = Lists
        .newArrayListWithCapacity(subCategoryDOList.size());
    for (SaCustomSubCategoryDO subCategoryDO : subCategoryDOList) {
      SaCustomSubCategoryBO subCategoryBO = new SaCustomSubCategoryBO();
      BeanUtils.copyProperties(subCategoryDO, subCategoryBO);
      subCategoryBO.setCreateTime(DateUtils.toStringISO8601(subCategoryDO.getCreateTime()));
      subCategoryBO.setUpdateTime(DateUtils.toStringISO8601(subCategoryDO.getUpdateTime()));
      subCategoryBOList.add(subCategoryBO);
    }
    return subCategoryBOList;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomSubCategory(java.lang.String)
   */
  @Override
  public SaCustomSubCategoryBO queryCustomSubCategory(String id) {
    SaCustomSubCategoryDO subCategoryDO = customSubCategoryDao.querySaCustomSubCategory(id);
    SaCustomSubCategoryBO subcategoryBO = new SaCustomSubCategoryBO();
    BeanUtils.copyProperties(subCategoryDO, subcategoryBO);
    subcategoryBO.setCreateTime(DateUtils.toStringISO8601(subCategoryDO.getCreateTime()));
    subcategoryBO.setUpdateTime(DateUtils.toStringISO8601(subCategoryDO.getUpdateTime()));

    List<SaCustomApplicationDO> customApps = customAppDao
        .querySaCustomAppBySubCategoryId(subCategoryDO.getSubCategoryId());

    // 子分类所包含的自定义应用
    List<String> appIds = customApps.stream().map(app -> app.getApplicationId())
        .collect(Collectors.toList());
    // 子分类所包含的预定义应用
    appIds.addAll(hierarchyDao
        .querySaHierarchys(SA_TYPE_APPLICATION, subCategoryDO.getCategoryId(),
            subCategoryDO.getSubCategoryId())
        .stream().map(SaHierarchyDO::getApplicationId).collect(Collectors.toList()));
    subcategoryBO.setApplicationIds(StringUtils.join(appIds, ","));

    return subcategoryBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomSubCategoryByCmsSubCategoryId(java.lang.String)
   */
  @Override
  public SaCustomSubCategoryBO queryCustomSubCategoryByCmsSubCategoryId(String cmsSubCategoryId) {

    SaCustomSubCategoryDO subCategoryDO = customSubCategoryDao
        .querySubCategoryByCmsSubCategoryId(cmsSubCategoryId);

    SaCustomSubCategoryBO subCategoryBO = new SaCustomSubCategoryBO();
    BeanUtils.copyProperties(subCategoryDO, subCategoryBO);

    return subCategoryBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#saveCustomSubCategory(com.machloop.fpc.manager.knowledge.bo.SaCustomSubCategoryBO, java.lang.String)
   */
  @Transactional
  @Override
  public synchronized SaCustomSubCategoryBO saveCustomSubCategory(
      SaCustomSubCategoryBO customSubCategoryBO, String operatorId) {
    if (customSubCategoryDao.countSaCustomSubCategorys() >= MAXIMUM_AMOUNT_SUBCATEGORY) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION,
          "最多只支持" + MAXIMUM_AMOUNT_SUBCATEGORY + "条自定义子分类");
    }

    // 校验名称是否重复
    SaCustomSubCategoryDO existNameDO = customSubCategoryDao
        .querySaCustomSubCategoryByName(customSubCategoryBO.getName());
    if (StringUtils.isNotBlank(existNameDO.getId()) || isNameRepetition(
        customSubCategoryBO.getName(), FpcConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "子分类名称不能重复，请修改子分类名称");
    }

    // 生成子分类ID
    if (StringUtils.isBlank(customSubCategoryBO.getSubCategoryId())) {
      String subCategoryId = StringUtils.defaultIfBlank(customSubCategoryBO.getSubCategoryId(),
          generateSubCategoryId());
      customSubCategoryBO.setSubCategoryId(subCategoryId);
    }
    customSubCategoryBO.setOperatorId(operatorId);
    SaCustomSubCategoryDO customSubCategoryDO = new SaCustomSubCategoryDO();
    BeanUtils.copyProperties(customSubCategoryBO, customSubCategoryDO);

    // 保存子分类
    customSubCategoryDO = customSubCategoryDao
        .saveOrRecoverSaCustomSubCategory(customSubCategoryDO);

    // 更新配置的应用相关属性
    List<String> appIds = CsvUtils.convertCSVToList(customSubCategoryBO.getApplicationIds());

    // 预定义应用集合
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "SA规则库获取失败");
    }
    List<String> predefinedAppIds = knowledgeDao.queryKnowledgeRules(knowledgeFilePath).getT3()
        .stream().map(SaApplicationDO::getApplicationId).collect(Collectors.toList());

    // 该子分类包含的自定义应用
    List<String> customAppIds = appIds.stream().filter(appId -> !predefinedAppIds.contains(appId))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(customAppIds)) {
      customAppDao.batchUpdateApps(customAppIds, customSubCategoryDO.getCategoryId(),
          customSubCategoryDO.getSubCategoryId(), operatorId);
    }
    // 该子分类包含的预定义应用
    appIds.removeAll(customAppIds);
    if (CollectionUtils.isNotEmpty(appIds)) {
      hierarchyDao.batchDeleteByApplicationIds(SA_TYPE_APPLICATION, appIds);

      // 添加到预定义关系变更表
      String categoryId = customSubCategoryDO.getCategoryId();
      final String fSubCategoryId = customSubCategoryDO.getSubCategoryId();
      List<SaHierarchyDO> saHierarchys = appIds.stream().map(appId -> {
        SaHierarchyDO saHierarchyDO = new SaHierarchyDO();
        saHierarchyDO.setType(SA_TYPE_APPLICATION);
        saHierarchyDO.setCategoryId(categoryId);
        saHierarchyDO.setSubCategoryId(fSubCategoryId);
        saHierarchyDO.setApplicationId(appId);
        saHierarchyDO.setOperatorId(operatorId);

        return saHierarchyDO;
      }).collect(Collectors.toList());
      hierarchyDao.batchSaveSaHierarchy(saHierarchys);
    }

    return queryCustomSubCategory(customSubCategoryDO.getId());
  }

  private String generateSubCategoryId() {
    // 生成子分类ID
    String subCategoryId = globalSettingService
        .generateSequence(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_SUBCATEGORY_SEQ_KEY, "");
    if (Integer.parseInt(
        subCategoryId) == (CUSTOM_SUBCATEGORY_INITIATION_ID + MAXIMUM_AMOUNT_SUBCATEGORY)) {
      globalSettingService.setValue(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_SUBCATEGORY_SEQ_KEY,
          CUSTOM_SUBCATEGORY_INITIATION_ID + "");
      subCategoryId = globalSettingService
          .generateSequence(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_SUBCATEGORY_SEQ_KEY, "");
    }

    List<String> existSubCategoryIds = customSubCategoryDao.querySaCustomSubCategorys().stream()
        .map(customSubCategory -> customSubCategory.getSubCategoryId())
        .collect(Collectors.toList());
    while (true) {
      if (!existSubCategoryIds.contains(subCategoryId)) {
        break;
      }
      subCategoryId = globalSettingService
          .generateSequence(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_SUBCATEGORY_SEQ_KEY, "");
    }

    return subCategoryId;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#updateCustomSubCategory(java.lang.String, com.machloop.fpc.manager.knowledge.bo.SaCustomSubCategoryBO, java.lang.String)
   */
  @Transactional
  @Override
  public SaCustomSubCategoryBO updateCustomSubCategory(String id,
      SaCustomSubCategoryBO customSubCategoryBO, String operatorId) {
    SaCustomSubCategoryDO existCustomSubCategory = customSubCategoryDao
        .querySaCustomSubCategory(id);
    if (StringUtils.isBlank(existCustomSubCategory.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该记录不存在");
    }

    // 校验名称是否重复
    SaCustomSubCategoryDO existNameDO = customSubCategoryDao
        .querySaCustomSubCategoryByName(customSubCategoryBO.getName());
    if ((StringUtils.isNotBlank(existNameDO.getId())
        && !StringUtils.equals(existNameDO.getId(), id))
        || isNameRepetition(customSubCategoryBO.getName(),
            FpcConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "子分类名称不能重复，请修改子分类名称");
    }

    // 修改子分类
    SaCustomSubCategoryDO customSubCategoryDO = new SaCustomSubCategoryDO();
    BeanUtils.copyProperties(existCustomSubCategory, customSubCategoryDO);
    customSubCategoryDO.setName(customSubCategoryBO.getName());
    customSubCategoryDO.setCategoryId(customSubCategoryBO.getCategoryId());
    customSubCategoryDO.setDescription(customSubCategoryBO.getDescription());
    customSubCategoryDO.setOperatorId(operatorId);
    customSubCategoryDao.updateSaCustomSubCategory(customSubCategoryDO);

    // 预定义应用集合
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "SA规则库获取失败");
    }
    List<String> predefinedAppIds = knowledgeDao.queryKnowledgeRules(knowledgeFilePath).getT3()
        .stream().map(SaApplicationDO::getApplicationId).collect(Collectors.toList());

    // 更新配置的应用相关属性
    List<String> appIds = CsvUtils.convertCSVToList(customSubCategoryBO.getApplicationIds());
    // 该子分类包含的自定义应用
    List<String> customAppIds = appIds.stream().filter(appId -> !predefinedAppIds.contains(appId))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(customAppIds)) {
      customAppDao.batchUpdateApps(customAppIds, customSubCategoryDO.getCategoryId(),
          customSubCategoryDO.getSubCategoryId(), operatorId);
    }

    // 该子分类包含的预定义应用
    appIds.removeAll(customAppIds);
    List<String> existAppIdList = hierarchyDao
        .querySaHierarchys(SA_TYPE_APPLICATION, customSubCategoryDO.getCategoryId(),
            customSubCategoryDO.getSubCategoryId())
        .stream().map(SaHierarchyDO::getApplicationId).collect(Collectors.toList());
    existAppIdList.addAll(appIds);
    // 删除旧数据
    if (CollectionUtils.isNotEmpty(existAppIdList)) {
      hierarchyDao.batchDeleteByApplicationIds(SA_TYPE_APPLICATION, existAppIdList);
    }

    // 添加新的预定义应用关系数据
    List<SaHierarchyDO> saHierarchys = appIds.stream().map(appId -> {
      SaHierarchyDO saHierarchyDO = new SaHierarchyDO();
      saHierarchyDO.setType(SA_TYPE_APPLICATION);
      saHierarchyDO.setCategoryId(customSubCategoryDO.getCategoryId());
      saHierarchyDO.setSubCategoryId(customSubCategoryDO.getSubCategoryId());
      saHierarchyDO.setApplicationId(appId);
      saHierarchyDO.setOperatorId(operatorId);

      return saHierarchyDO;
    }).collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(saHierarchys)) {
      hierarchyDao.batchSaveSaHierarchy(saHierarchys);
    }

    return queryCustomSubCategory(id);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#deleteCustomSubCategory(java.lang.String, java.lang.String)
   */
  @Override
  public SaCustomSubCategoryBO deleteCustomSubCategory(String id, String operatorId,
      boolean forceDelete) {
    SaCustomSubCategoryBO customSubCategory = queryCustomSubCategory(id);

    if (!forceDelete && StringUtils.isBlank(customSubCategory.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "自定义子分类不存在");
    }

    if (!forceDelete && StringUtils.isNotBlank(customSubCategory.getApplicationIds())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "该自定义子分类下包含应用，无法删除");
    }

    customSubCategoryDao.deleteSaCustomSubCategory(id, operatorId);

    return customSubCategory;
  }

  /*
   * 自定义规则
   */
  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomApps()
   */
  @Override
  public List<SaCustomApplicationBO> queryCustomApps() {
    List<SaCustomApplicationDO> appList = customAppDao.querySaCustomApps();
    List<SaCustomApplicationBO> appBOList = Lists.newArrayListWithCapacity(appList.size());
    for (SaCustomApplicationDO appDO : appList) {
      SaCustomApplicationBO appBO = new SaCustomApplicationBO();
      BeanUtils.copyProperties(appDO, appBO);
      appBO.setCreateTime(DateUtils.toStringISO8601(appDO.getCreateTime()));
      appBO.setUpdateTime(DateUtils.toStringISO8601(appDO.getUpdateTime()));
      appBOList.add(appBO);
    }
    return appBOList;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomApp(java.lang.String)
   */
  @Override
  public SaCustomApplicationBO queryCustomApp(String id) {
    SaCustomApplicationDO appDO = customAppDao.querySaCustomApp(id);
    SaCustomApplicationBO appBO = new SaCustomApplicationBO();
    BeanUtils.copyProperties(appDO, appBO);
    appBO.setCreateTime(DateUtils.toStringISO8601(appDO.getCreateTime()));
    appBO.setUpdateTime(DateUtils.toStringISO8601(appDO.getUpdateTime()));
    return appBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryCustomAppByCmsApplicationId(java.lang.String)
   */
  @Override
  public SaCustomApplicationBO queryCustomAppByCmsApplicationId(String cmsApplicationId) {

    SaCustomApplicationDO customAppDO = customAppDao
        .queryCustomAppByCmsApplicationId(cmsApplicationId);

    SaCustomApplicationBO customAppBO = new SaCustomApplicationBO();
    BeanUtils.copyProperties(customAppDO, customAppBO);

    return customAppBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#saveCustomApp(com.machloop.fpc.manager.knowledge.bo.SaCustomApplicationBO, java.lang.String)
   */
  @Override
  public synchronized SaCustomApplicationBO saveCustomApp(SaCustomApplicationBO customAppBO,
      String operatorId) {

    if (customAppDao.countSaCustomApps() >= MAXIMUM_AMOUNT_APPLICATION) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION,
          "最多只支持" + MAXIMUM_AMOUNT_APPLICATION + "条自定义应用");
    }

    // 校验名称是否重复
    SaCustomApplicationDO existDO = customAppDao.querySaCustomAppByName(customAppBO.getName());
    if (StringUtils.isNotBlank(existDO.getId())
        || isNameRepetition(customAppBO.getName(), FpcConstants.METRIC_TYPE_APPLICATION_APP)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "应用名称不能重复，请修改应用名称");
    }

    // 写入数据库
    if (StringUtils.isBlank(customAppBO.getApplicationId())) {
      String applicationId = StringUtils.defaultIfBlank(customAppBO.getApplicationId(),
          globalSettingService
              .generateSequence(ManagerConstants.GLOBAL_SETTING_SA_CUSTOM_APPLICATION_SEQ_KEY, ""));
      customAppBO.setApplicationId(applicationId);
    }
    customAppBO.setOperatorId(operatorId);
    SaCustomApplicationDO customAppDO = new SaCustomApplicationDO();
    BeanUtils.copyProperties(customAppBO, customAppDO);

    customAppDO = customAppDao.saveOrRecoverSaCustomApp(customAppDO);

    return queryCustomApp(customAppDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#updateCustomApp(java.lang.String, com.machloop.fpc.manager.knowledge.bo.SaCustomApplicationBO, java.lang.String)
   */
  @Override
  public SaCustomApplicationBO updateCustomApp(String id, SaCustomApplicationBO customAppBO,
      String operatorId) {

    SaCustomApplicationDO existSaCustomRule = customAppDao.querySaCustomApp(id);
    if (StringUtils.isBlank(existSaCustomRule.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "该记录不存在");
    }

    // 校验名称是否重复
    SaCustomApplicationDO existNameDO = customAppDao.querySaCustomAppByName(customAppBO.getName());
    if ((StringUtils.isNotBlank(existNameDO.getId())
        && !StringUtils.equals(existNameDO.getId(), id))
        || isNameRepetition(customAppBO.getName(), FpcConstants.METRIC_TYPE_APPLICATION_APP)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "应用名称不能重复，请修改应用名称");
    }

    // 写入数据库
    SaCustomApplicationDO customAppDO = new SaCustomApplicationDO();
    BeanUtils.copyProperties(existSaCustomRule, customAppDO);
    customAppDO.setName(customAppBO.getName());
    customAppDO.setCategoryId(customAppBO.getCategoryId());
    customAppDO.setSubCategoryId(customAppBO.getSubCategoryId());
    customAppDO.setL7ProtocolId(customAppBO.getL7ProtocolId());
    customAppDO.setRule(customAppBO.getRule());
    customAppDO.setDescription(customAppBO.getDescription());
    customAppDO.setOperatorId(operatorId);
    customAppDao.updateSaCustomApp(customAppDO);

    return queryCustomApp(id);
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#deleteCustomApp(java.lang.String, java.lang.String)
   */
  @Override
  public SaCustomApplicationBO deleteCustomApp(String id, String operatorId, boolean forceDelete) {
    SaCustomApplicationBO customAppBO = queryCustomApp(id);

    if (!forceDelete && StringUtils.isBlank(customAppBO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "自定义应用不存在");
    }

    List<ServiceBO> list = serviceService.queryServiceByAppId(customAppBO.getApplicationId());
    if (!forceDelete && CollectionUtils.isNotEmpty(list)) {
      List<
          String> serviceNames = list.stream().map(ServiceBO::getName).collect(Collectors.toList());
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("应用已被业务[%s]使用", CsvUtils.convertCollectionToCSV(serviceNames)));
    }

    customAppDao.deleteSaCustomApp(id, operatorId);

    return customAppBO;
  }

  /**
   * @see com.machloop.fpc.manager.knowledge.service.SaService#queryAllAppsIdNameMapping()
   */
  @Override
  public Map<Integer, String> queryAllAppsIdNameMapping() {
    List<SaApplicationBO> application = queryKnowledgeRules().getT3();
    List<SaCustomApplicationBO> custom = queryCustomApps();
    Map<Integer, String> map = Maps.newHashMapWithExpectedSize(application.size() + custom.size());
    application
        .forEach(item -> map.put(Integer.parseInt(item.getApplicationId()), item.getNameText()));
    custom.forEach(item -> map.put(Integer.parseInt(item.getApplicationId()), item.getName()));
    return map;
  }

  /**
   * 判断自定义规则名称是否和预定义重复
   * @param name
   * @param type
   * @return
   */
  private boolean isNameRepetition(String name, int type) {
    String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
    if (!Paths.get(knowledgeFilePath).toFile().exists()) {
      return true;
    }

    Tuple3<List<SaCategoryDO>, List<SaSubCategoryDO>,
        List<SaApplicationDO>> rules = knowledgeDao.queryKnowledgeRules(knowledgeFilePath);
    boolean isNameRepetition = true;
    switch (type) {
      case FpcConstants.METRIC_TYPE_APPLICATION_CATEGORY:
      {
        isNameRepetition = rules.getT1().stream().map(SaCategoryDO::getNameText)
            .collect(Collectors.toList()).contains(name);
      }
        break;
      case FpcConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
      {
        isNameRepetition = rules.getT2().stream().map(SaSubCategoryDO::getNameText)
            .collect(Collectors.toList()).contains(name);
      }
        break;
      case FpcConstants.METRIC_TYPE_APPLICATION_APP:
      {
        isNameRepetition = rules.getT3().stream().map(SaApplicationDO::getNameText)
            .collect(Collectors.toList()).contains(name);
      }
        break;
      default:
        isNameRepetition = true;
        break;
    }

    return isNameRepetition;
  }

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_CUSTOMAPPLICATION,
            FpcCmsConstants.MQ_TAG_CUSTOMSUBCATEGORY, FpcCmsConstants.MQ_TAG_CUSTOMCATEGORY,
            FpcCmsConstants.MQ_TAG_SAKNOWLEDGE));
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#syncConfiguration(java.util.Map)
   */
  @Override
  public int syncConfiguration(Message message) {

    int syncTotalCount = 0;
    if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_SAKNOWLEDGE)) {
      syncTotalCount = syncSaKonwledge(message);
      LOGGER.info("current sync saKnowledge total: {}.", syncTotalCount);

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
      if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_CUSTOMCATEGORY)) {
        syncTotalCount = messages.stream().mapToInt(item -> syncCustomCategory(item)).sum();
        LOGGER.info("current sync customCategory total: {}.", syncTotalCount);
      }
      if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_CUSTOMSUBCATEGORY)) {
        syncTotalCount = messages.stream().mapToInt(item -> syncCustomSubCategory(item)).sum();
        LOGGER.info("current sync customSubCategory total: {}.", syncTotalCount);
      }
      if (StringUtils.equals(message.getTags(), FpcCmsConstants.MQ_TAG_CUSTOMAPPLICATION)) {
        syncTotalCount = messages.stream().mapToInt(item -> syncCustomApplication(item)).sum();
        LOGGER.info("current sync customApplication total: {}.", syncTotalCount);
      }

      return syncTotalCount;
    }
  }

  private int syncSaKonwledge(Message message) {

    int syncCount = 0;
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    Path tempPath = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID());
    Path knowledgePath = Paths.get(HotPropertiesHelper.getProperty("file.sa.knowledge.path"));
    try {

      FileUtils.writeByteArrayToFile(new File(tempPath.toString()), message.getBody());
      // 文件上传为临时文件
      SaKnowledgeInfoDO infoDO = knowledgeDao.queryKnowledgeInfos(tempPath.toString());
      if (StringUtils.isBlank(infoDO.getVersion())) {
        LOGGER.warn("SA规则库文件解析失败");
        return syncCount;
      }
      // 替换原文件
      Files.move(tempPath.toFile(), knowledgePath.toFile());
      syncCount++;
    } catch (IllegalStateException | IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "上传SA知识库文件失败");
    }
    return syncCount;
  }

  private int syncCustomCategory(Map<String, Object> messageBody) {

    int syncCount = 0;

    String customCategoryInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(customCategoryInCmsId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");
    SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
    customCategoryBO.setId(customCategoryInCmsId);
    customCategoryBO.setCategoryInCmsId(customCategoryInCmsId);
    customCategoryBO.setName(MapUtils.getString(messageBody, "name"));
    customCategoryBO.setCategoryId(MapUtils.getString(messageBody, "categoryId"));
    customCategoryBO.setSubCategoryIds(MapUtils.getString(messageBody, "subCategoryIds"));
    customCategoryBO.setDescription(CMS_ASSIGNMENT);

    // 本次下发的SaCustomCategory是否存在
    SaCustomCategoryBO exist = queryCustomCategoryByCmsCategoryId(
        customCategoryBO.getCategoryInCmsId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateCustomCategory(exist.getId(), customCategoryBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                customCategoryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveCustomCategory(customCategoryBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                customCategoryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteCustomCategory(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              customCategoryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync customCategory status: [add: {}, modify: {}, delete: {}]",
            addCount, modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  private int syncCustomSubCategory(Map<String, Object> messageBody) {
    int syncCount = 0;

    String subCategoryInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(subCategoryInCmsId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    SaCustomSubCategoryBO customSubCategoryBO = new SaCustomSubCategoryBO();
    customSubCategoryBO.setId(subCategoryInCmsId);
    customSubCategoryBO.setSubCategoryInCmsId(subCategoryInCmsId);
    customSubCategoryBO.setName(MapUtils.getString(messageBody, "name"));
    customSubCategoryBO.setSubCategoryId(MapUtils.getString(messageBody, "subCategoryId"));
    customSubCategoryBO.setCategoryId(MapUtils.getString(messageBody, "categoryId"));
    customSubCategoryBO.setApplicationIds(MapUtils.getString(messageBody, "applicationIds"));
    customSubCategoryBO.setDescription(CMS_ASSIGNMENT);

    // 本次下发的SaCustomSubCategory是否存在
    SaCustomSubCategoryBO exist = queryCustomSubCategoryByCmsSubCategoryId(
        customSubCategoryBO.getSubCategoryInCmsId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateCustomSubCategory(exist.getId(), customSubCategoryBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                customSubCategoryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveCustomSubCategory(customSubCategoryBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                customSubCategoryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteCustomSubCategory(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              customSubCategoryBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync customSubCategory status: [add: {}, modify: {}, delete: {}]",
            addCount, modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  private int syncCustomApplication(Map<String, Object> messageBody) {
    int syncCount = 0;

    String applicationInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(applicationInCmsId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");
    SaCustomApplicationBO customApplicationBO = new SaCustomApplicationBO();
    customApplicationBO.setId(applicationInCmsId);
    customApplicationBO.setApplicationInCmsId(applicationInCmsId);
    customApplicationBO.setName(MapUtils.getString(messageBody, "name"));
    customApplicationBO.setApplicationId(MapUtils.getString(messageBody, "applicationId"));
    customApplicationBO.setCategoryId(MapUtils.getString(messageBody, "categoryId"));
    customApplicationBO.setSubCategoryId(MapUtils.getString(messageBody, "subCategoryId"));
    customApplicationBO.setL7ProtocolId(MapUtils.getString(messageBody, "l7ProtocolId"));
    customApplicationBO.setRule(MapUtils.getString(messageBody, "rule"));
    customApplicationBO.setDescription(CMS_ASSIGNMENT);

    // 本次下发的SaCustomApplication是否存在
    SaCustomApplicationBO exist = queryCustomAppByCmsApplicationId(
        customApplicationBO.getApplicationInCmsId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateCustomApp(exist.getId(), customApplicationBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                customApplicationBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveCustomApp(customApplicationBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                customApplicationBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteCustomApp(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              customApplicationBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync customApplication status: [add: {}, modify: {}, delete: {}]",
            addCount, modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date, boolean)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // 只能删除自定义数据，SA规则库不可删除
    // 删除
    int clearCount = 0;
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_CUSTOMCATEGORY)) {
      List<SaCustomCategoryDO> customCategoryList = customCategoryDao
          .querySaCustomCategoryIdAndNumIds(onlyLocal);
      for (SaCustomCategoryDO customCategory : customCategoryList) {
        try {
          hierarchyDao.batchDeleteByCategoryId(customCategory.getCategoryId());
          customAppDao.deleteSaCustomAppByCategoryId(customCategory.getCategoryId(),
              CMS_ASSIGNMENT);
          customSubCategoryDao.deleteSaCustomSubCategoryByCategoryId(customCategory.getCategoryId(),
              CMS_ASSIGNMENT);
          deleteCustomCategory(customCategory.getId(), CMS_ASSIGNMENT, true);
          clearCount++;
        } catch (BusinessException e) {
          LOGGER.warn("delete customCategory failed. error msg: {}", e.getMessage());
          continue;
        }
      }
      return clearCount;
    }

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_CUSTOMSUBCATEGORY)) {
      List<SaCustomSubCategoryDO> customSubCategoryList = customSubCategoryDao
          .querySaCustomSubCategoryIdAndNumIds(onlyLocal);
      for (SaCustomSubCategoryDO customSubCategory : customSubCategoryList) {
        try {
          hierarchyDao.batchDeleteBySubCategoryIds(SA_TYPE_SUBCATEGORY,
              Lists.newArrayList(customSubCategory.getSubCategoryId()));
          customAppDao.deleteSaCustomAppBySubCategoryId(customSubCategory.getSubCategoryId(),
              CMS_ASSIGNMENT);
          deleteCustomSubCategory(customSubCategory.getId(), CMS_ASSIGNMENT, true);
          clearCount++;
        } catch (BusinessException e) {
          LOGGER.warn("delete customSubCategory failed. error msg: {}", e.getMessage());
          continue;
        }
      }
      return clearCount;
    }

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_CUSTOMAPPLICATION)) {
      List<SaCustomApplicationDO> customAppList = customAppDao
          .querySaCustomAppIdAndNumIds(onlyLocal);
      for (SaCustomApplicationDO customApp : customAppList) {
        try {
          hierarchyDao.batchDeleteByApplicationIds(SA_TYPE_APPLICATION,
              Lists.newArrayList(customApp.getApplicationId()));
          deleteCustomApp(customApp.getId(), CMS_ASSIGNMENT, true);
          clearCount++;
        } catch (BusinessException e) {
          LOGGER.warn("delete customApp failed. error msg: {}", e.getMessage());
          continue;
        }
      }
      return clearCount;
    }

    return clearCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_CUSTOMCATEGORY)) {
      return customCategoryDao.queryAssignSaCustomCategorys(beforeTime);
    }
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_CUSTOMSUBCATEGORY)) {
      return customSubCategoryDao.queryAssignSaCustomSubCategorys(beforeTime);
    }
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_CUSTOMAPPLICATION)) {
      return customAppDao.queryAssignSaCustomApps(beforeTime);
    }

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_SAKNOWLEDGE)) {
      try {
        String knowledgeFilePath = HotPropertiesHelper.getProperty("file.sa.knowledge.path");
        return Lists
            .newArrayList(DigestUtils.md5Hex(new FileInputStream(new File(knowledgeFilePath))));
      } catch (IllegalStateException | IOException e) {
        LOGGER.warn("加密SA知识库文件失败");
      }
    }
    return Lists.newArrayListWithCapacity(0);
  }

}
