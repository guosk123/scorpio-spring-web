package com.machloop.fpc.cms.center.restapi;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.cms.center.knowledge.bo.SaApplicationBO;
import com.machloop.fpc.cms.center.knowledge.bo.SaCategoryBO;
import com.machloop.fpc.cms.center.knowledge.bo.SaCustomApplicationBO;
import com.machloop.fpc.cms.center.knowledge.bo.SaCustomCategoryBO;
import com.machloop.fpc.cms.center.knowledge.bo.SaCustomSubCategoryBO;
import com.machloop.fpc.cms.center.knowledge.bo.SaKnowledgeInfoBO;
import com.machloop.fpc.cms.center.knowledge.bo.SaSubCategoryBO;
import com.machloop.fpc.cms.center.knowledge.service.SaProtocolService;
import com.machloop.fpc.cms.center.knowledge.service.SaService;
import com.machloop.fpc.cms.center.knowledge.vo.SaCustomCategoryCreationVO;
import com.machloop.fpc.cms.center.knowledge.vo.SaCustomCategoryModificationVO;
import com.machloop.fpc.cms.center.knowledge.vo.SaCustomSubCategoryCreationVO;
import com.machloop.fpc.cms.center.knowledge.vo.SaCustomSubCategoryModificationVO;
import com.machloop.fpc.cms.center.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.cms.center.restapi.vo.SaCustomApplicationVO;
import com.machloop.fpc.cms.center.restapi.vo.SaCustomApplicationVO.AppRule;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;

/**
 * @author guosk
 *
 * create at 2021年9月8日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-cms-v1/appliance")
public class SaRuleRestAPIController {

  private static final int MAX_RULE_NUMBER = 16;
  private static final int MAX_NAME_LENGTH = 30;
  private static final Range<Integer> RANGE_RULE_SIGNATURE_OFFSET = Range.closed(-1, 1500);
  private static final List<String> IP_PROTOCOLS = Lists.newArrayList("TCP", "UDP", "ALL");
  private static final List<String> SIGNATURE_TYPE = Lists.newArrayList("", "hex", "ascii");

  private static final Pattern SIGNATURE_CONTENT_PATTERN = Pattern.compile("-?[0-9a-fA-F]{2,64}$");

  private static final List<String> ILLEGAL_SUBCATEGORY = Lists.newArrayList("30", "31");

  @Autowired
  private SaService saService;

  @Autowired
  private UserService userService;

  @Autowired
  private SaProtocolService saProtocolService;

  @GetMapping("/l7-protocols")
  @RestApiSecured
  public RestAPIResultVO queryL7Protocols() {
    List<Map<String, String>> protocols = saProtocolService.queryProtocols();

    List<Map<String, Object>> protocolList = protocols.stream().map(protocol -> {
      Map<String, Object> protocolMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      protocolMap.put("id", protocol.get("protocolId"));
      protocolMap.put("name", protocol.get("nameText"));

      return protocolMap;
    }).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(protocolList);
  }

  @GetMapping("/applications")
  @RestApiSecured
  public RestAPIResultVO queryAppRules() {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> rules = saService.queryKnowledgeRules();

    // 分类
    List<
        Map<String, Object>> categorys = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    rules.getT1().forEach(item -> {
      Map<String, Object> category = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      category.put("categoryId", item.getCategoryId());
      category.put("name", item.getNameText());
      categorys.add(category);
    });
    saService.queryCustomCategorys().forEach(item -> {
      Map<String, Object> category = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      category.put("categoryId", item.getCategoryId());
      category.put("name", item.getName());
      categorys.add(category);
    });
    result.put("categorys", categorys);

    // 子分类
    List<Map<String, Object>> subCategorys = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    rules.getT2().forEach(item -> {
      if (!ILLEGAL_SUBCATEGORY.contains(item.getSubCategoryId())) {
        Map<String,
            Object> subCategory = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        subCategory.put("subCategoryId", item.getSubCategoryId());
        subCategory.put("categoryId", item.getCategoryId());
        subCategory.put("name", item.getNameText());
        subCategorys.add(subCategory);
      }
    });
    saService.queryCustomSubCategorys().forEach(item -> {
      Map<String, Object> subCategory = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      subCategory.put("subCategoryId", item.getSubCategoryId());
      subCategory.put("categoryId", item.getCategoryId());
      subCategory.put("name", item.getName());
      subCategorys.add(subCategory);
    });
    result.put("subCategorys", subCategorys);

    // 应用
    List<Map<String, Object>> applications = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    rules.getT3().forEach(item -> {
      Map<String, Object> application = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      application.put("applicationId", item.getApplicationId());
      application.put("subCategoryId", item.getSubCategoryId());
      application.put("categoryId", item.getCategoryId());
      application.put("name", item.getNameText());
      applications.add(application);
    });
    saService.queryCustomApps().forEach(item -> {
      Map<String, Object> application = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      application.put("applicationId", item.getApplicationId());
      application.put("subCategoryId", item.getSubCategoryId());
      application.put("categoryId", item.getCategoryId());
      application.put("name", item.getName());
      applications.add(application);
    });
    result.put("applications", applications);

    return RestAPIResultVO.resultSuccess(result);
  }

  /**
   * SA规则库导入
   */
  @PostMapping("/sa/knowledges")
  @RestApiSecured
  public RestAPIResultVO importKnowledges(@RequestParam MultipartFile file,
      HttpServletRequest request) {
    SaKnowledgeInfoBO knowledgeBO = saService.importKnowledges(file);

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, knowledgeBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(knowledgeBO);
  }

  /**
   * 自定义分类
   */
  @GetMapping("/sa/custom-categorys")
  @RestApiSecured
  public RestAPIResultVO querySaCustomCategory() {
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    List<Map<String, Object>> resultList = customCategorys.stream()
        .map(category -> categoryBO2Map(category)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @PostMapping("/sa/custom-categorys")
  @RestApiSecured
  public RestAPIResultVO saveSaCustomCategory(
      @RequestBody @Validated SaCustomCategoryCreationVO customCategoryVO,
      BindingResult bindingResult, HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkRuleId(bindingResult, null,
        customCategoryVO.getSubCategoryIds(), null);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
    try {
      BeanUtils.copyProperties(customCategoryVO, customCategoryBO);
      customCategoryBO
          .setDescription(StringUtils.defaultIfBlank(customCategoryBO.getDescription(), ""));
      customCategoryBO = saService.saveCustomCategory(customCategoryBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customCategoryBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customCategoryBO.getId());
  }

  @PutMapping("/sa/custom-categorys/{id}")
  @RestApiSecured
  public RestAPIResultVO updateSaCustomCategory(
      @PathVariable @NotEmpty(message = "修改自定义SA分类时传入的id不能为空") String id,
      @RequestBody @Validated SaCustomCategoryModificationVO customCategoryVO,
      BindingResult bindingResult, HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkRuleId(bindingResult, null,
        customCategoryVO.getSubCategoryIds(), null);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
    try {
      BeanUtils.copyProperties(customCategoryVO, customCategoryBO);
      customCategoryBO
          .setDescription(StringUtils.defaultIfBlank(customCategoryBO.getDescription(), ""));
      customCategoryBO = saService.updateCustomCategory(id, customCategoryBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customCategoryBO,
        userBO.getFullname(), userBO.getName());

    return RestAPIResultVO.resultSuccess(customCategoryBO.getId());
  }

  @DeleteMapping("/sa/custom-categorys/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteSaCustomCategory(
      @PathVariable @NotEmpty(message = "删除自定义SA分类时传入的id不能为空") String id,
      HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
    try {
      customCategoryBO = saService.deleteCustomCategory(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customCategoryBO,
        userBO.getFullname(), userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  /**
   * 自定义子分类
   */
  @GetMapping("/sa/custom-subcategorys")
  @RestApiSecured
  public RestAPIResultVO querySaCustomSubCategory() {
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    List<Map<String, Object>> resultList = customSubCategorys.stream()
        .map(subCategory -> subCategoryBO2Map(subCategory)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @PostMapping("/sa/custom-subcategorys")
  @RestApiSecured
  public RestAPIResultVO saveSaCustomSubCategory(
      @RequestBody @Validated SaCustomSubCategoryCreationVO customSubCategoryVO,
      BindingResult bindingResult, HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkRuleId(bindingResult,
        customSubCategoryVO.getCategoryId(), null, customSubCategoryVO.getApplicationIds());
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomSubCategoryBO customSubCategoryBO = new SaCustomSubCategoryBO();
    try {
      BeanUtils.copyProperties(customSubCategoryVO, customSubCategoryBO);
      customSubCategoryBO
          .setDescription(StringUtils.defaultIfBlank(customSubCategoryBO.getDescription(), ""));
      customSubCategoryBO = saService.saveCustomSubCategory(customSubCategoryBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customSubCategoryBO,
        userBO.getFullname(), userBO.getName());

    return RestAPIResultVO.resultSuccess(customSubCategoryBO.getId());
  }

  @PutMapping("/sa/custom-subcategorys/{id}")
  @RestApiSecured
  public RestAPIResultVO updateSaCustomSubCategory(
      @PathVariable @NotEmpty(message = "修改自定义SA子分类时传入的id不能为空") String id,
      @RequestBody @Validated SaCustomSubCategoryModificationVO customSubCategoryVO,
      BindingResult bindingResult, HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkRuleId(bindingResult,
        customSubCategoryVO.getCategoryId(), null, customSubCategoryVO.getApplicationIds());
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomSubCategoryBO customSubCategoryBO = new SaCustomSubCategoryBO();
    try {
      BeanUtils.copyProperties(customSubCategoryVO, customSubCategoryBO);
      customSubCategoryBO
          .setDescription(StringUtils.defaultIfBlank(customSubCategoryBO.getDescription(), ""));
      customSubCategoryBO = saService.updateCustomSubCategory(id, customSubCategoryBO,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customSubCategoryBO,
        userBO.getFullname(), userBO.getName());

    return RestAPIResultVO.resultSuccess(customSubCategoryBO.getId());
  }

  @DeleteMapping("/sa/custom-subcategorys/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteSaCustomSubCategory(
      @PathVariable @NotEmpty(message = "删除自定义SA子分类时传入的id不能为空") String id,
      HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomSubCategoryBO customSubCategoryBO = new SaCustomSubCategoryBO();
    try {
      customSubCategoryBO = saService.deleteCustomSubCategory(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customSubCategoryBO,
        userBO.getFullname(), userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  /**
   * 自定义应用
   */
  @GetMapping("/sa/custom-applications")
  @RestApiSecured
  public RestAPIResultVO querySaCustomRule() {
    List<SaCustomApplicationBO> customApplications = saService.queryCustomApps();
    List<Map<String, Object>> resultList = customApplications.stream()
        .map(applicationBO -> applicationBO2Map(applicationBO)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @PostMapping("/sa/custom-applications")
  @RestApiSecured
  public RestAPIResultVO saveSaCustomRule(@RequestBody @Validated SaCustomApplicationVO customAppVO,
      BindingResult bindingResult, HttpServletRequest request) {
    // 校验分类ID、子分类ID
    RestAPIResultVO restAPIResultVO = checkRuleId(bindingResult, customAppVO.getCategoryId(),
        customAppVO.getSubCategoryId(), null);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }
    // 校验应用层协议
    List<String> protocolIds = saProtocolService.queryProtocols().stream()
        .map(map -> (String) map.get("protocolId")).collect(Collectors.toList());
    if (!protocolIds.contains(customAppVO.getL7ProtocolId())) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的应用层协议ID")
          .build();
    }
    // 校验规则
    RestAPIResultVO checkAppRuleResult = checkAppRule(customAppVO);
    if (checkAppRuleResult != null) {
      return checkAppRuleResult;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomApplicationBO customAppBO = new SaCustomApplicationBO();
    try {
      BeanUtils.copyProperties(customAppVO, customAppBO);
      customAppBO.setRule(CollectionUtils.isEmpty(customAppVO.getRule()) ? ""
          : JsonHelper.serialize(customAppVO.getRule(), false));
      customAppBO.setDescription(StringUtils.defaultIfBlank(customAppBO.getDescription(), ""));
      customAppBO = saService.saveCustomApp(customAppBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customAppBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customAppBO.getId());
  }

  @PutMapping("/sa/custom-applications/{id}")
  @RestApiSecured
  public RestAPIResultVO updateSaCustomRule(
      @PathVariable @NotEmpty(message = "修改自定义SA规则时传入的id不能为空") String id,
      @RequestBody @Validated SaCustomApplicationVO customAppVO, BindingResult bindingResult,
      HttpServletRequest request) {
    // 校验分类ID、子分类ID
    RestAPIResultVO restAPIResultVO = checkRuleId(bindingResult, customAppVO.getCategoryId(),
        customAppVO.getSubCategoryId(), null);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }
    // 校验应用层协议
    List<String> protocolIds = saProtocolService.queryProtocols().stream()
        .map(map -> (String) map.get("protocolId")).collect(Collectors.toList());
    if (!protocolIds.contains(customAppVO.getL7ProtocolId())) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的应用层协议ID")
          .build();
    }
    // 校验规则
    RestAPIResultVO checkAppRuleResult = checkAppRule(customAppVO);
    if (checkAppRuleResult != null) {
      return checkAppRuleResult;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomApplicationBO customAppBO = new SaCustomApplicationBO();
    try {
      BeanUtils.copyProperties(customAppVO, customAppBO);
      customAppBO.setRule(CollectionUtils.isEmpty(customAppVO.getRule()) ? ""
          : JsonHelper.serialize(customAppVO.getRule(), false));
      customAppBO.setDescription(StringUtils.defaultIfBlank(customAppBO.getDescription(), ""));
      customAppBO = saService.updateCustomApp(id, customAppBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customAppBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customAppBO.getId());
  }

  @DeleteMapping("/sa/custom-applications/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteSaCustomRule(
      @PathVariable @NotEmpty(message = "删除自定义SA规则时传入的id不能为空") String id,
      HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomApplicationBO customAppBO = new SaCustomApplicationBO();
    try {
      customAppBO = saService.deleteCustomApp(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customAppBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkRuleId(BindingResult bindingResult, String categoryIds,
      String subCategoryIds, String appIds) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    if (StringUtils.isAllBlank(categoryIds, subCategoryIds, appIds)) {
      return null;
    }

    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> rules = saService.queryKnowledgeRules();

    if (StringUtils.isNotBlank(categoryIds)) {
      // 已有分类ID集合
      List<String> existCategoryIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      existCategoryIds.addAll(
          rules.getT1().stream().map(SaCategoryBO::getCategoryId).collect(Collectors.toList()));
      existCategoryIds.addAll(saService.queryCustomCategorys().stream()
          .map(SaCustomCategoryBO::getCategoryId).collect(Collectors.toList()));

      List<String> categoryIdList = CsvUtils.convertCSVToList(categoryIds);
      if (!existCategoryIds.containsAll(categoryIdList)) {
        categoryIdList.removeAll(existCategoryIds);
        return new RestAPIResultVO.Builder(FpcCmsConstants.OBJECT_NOT_FOUND_CODE)
            .msg(String.format("不存在的分类ID： %s", categoryIdList)).build();
      }
    }

    if (StringUtils.isNotBlank(subCategoryIds)) {
      // 已有子分类ID集合
      List<String> existSubCategoryIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      existSubCategoryIds.addAll(rules.getT2().stream().map(SaSubCategoryBO::getSubCategoryId)
          .collect(Collectors.toList()));
      existSubCategoryIds.addAll(saService.queryCustomSubCategorys().stream()
          .map(SaCustomSubCategoryBO::getSubCategoryId).collect(Collectors.toList()));

      List<String> subCategoryIdList = CsvUtils.convertCSVToList(subCategoryIds);
      if (!existSubCategoryIds.containsAll(subCategoryIdList)) {
        subCategoryIdList.removeAll(existSubCategoryIds);
        return new RestAPIResultVO.Builder(FpcCmsConstants.OBJECT_NOT_FOUND_CODE)
            .msg(String.format("不存在的子分类ID： %s", subCategoryIdList)).build();
      }
    }

    if (StringUtils.isNotBlank(appIds)) {
      // 已有应用ID集合
      List<String> existApplicationIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      existApplicationIds.addAll(rules.getT3().stream().map(SaApplicationBO::getApplicationId)
          .collect(Collectors.toList()));
      existApplicationIds.addAll(saService.queryCustomApps().stream()
          .map(SaCustomApplicationBO::getApplicationId).collect(Collectors.toList()));

      List<String> appIdList = CsvUtils.convertCSVToList(appIds);
      if (!existApplicationIds.containsAll(appIdList)) {
        appIdList.removeAll(existApplicationIds);
        return new RestAPIResultVO.Builder(FpcCmsConstants.OBJECT_NOT_FOUND_CODE)
            .msg(String.format("不存在的应用ID： %s", appIdList)).build();
      }
    }

    return null;
  }

  private RestAPIResultVO checkAppRule(SaCustomApplicationVO customAppVO) {
    List<AppRule> rules = customAppVO.getRule();

    if (CollectionUtils.isEmpty(rules)) {
      return null;
    }

    if (rules.size() > MAX_RULE_NUMBER) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("规则数量超过最大限制：[%s]", MAX_RULE_NUMBER)).build();
    }

    Set<AppRule> removal = Sets.newHashSet(rules);
    if (removal.size() < rules.size()) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE).msg("存在重复的规则")
          .build();
    }

    for (AppRule rule : rules) {
      // 字段非空判断
      if (StringUtils.isBlank(rule.getName())) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE).msg("规则名称不能为空")
            .build();
      }
      if (StringUtils.isBlank(rule.getProtocol())) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg("规则传输层协议不能为空").build();
      }
      if (StringUtils.isAllBlank(rule.getIpAddress(), rule.getPort(), rule.getSignatureType())) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg("规则P地址、端口、签名请至少填写1项").build();
      }
      if (StringUtils.isNotBlank(rule.getSignatureType())
          && StringUtils.isAnyBlank(rule.getSignatureOffset(), rule.getSignatureContent())) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE).msg("规则签名信息不完整")
            .build();
      }

      // 名称长度校验
      if (rule.getName().length() > MAX_NAME_LENGTH) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("规则名称长度超过最大限制： [%s]", MAX_NAME_LENGTH)).build();
      }

      // IP校验
      if (StringUtils.isNotBlank(rule.getIpAddress())
          && !(NetworkUtils.isInetAddress(rule.getIpAddress())
              || NetworkUtils.isCidr(rule.getIpAddress()))) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("规则IP地址不合法： [%s]", rule.getIpAddress())).build();
      }
      rule.setIpAddress(StringUtils.defaultIfBlank(rule.getIpAddress(), ""));

      // 协议校验
      if (!IP_PROTOCOLS.contains(StringUtils.upperCase(rule.getProtocol()))) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("规则传输层协议不合法： [%s]", rule.getProtocol())).build();
      }
      rule.setProtocol(StringUtils.lowerCase(rule.getProtocol()));

      // 端口号校验
      String port = rule.getPort();
      if (StringUtils.isNotBlank(port) && !NetworkUtils.isInetAddressPort(port)) {
        if (StringUtils.contains(port, "-")) {
          String[] range = StringUtils.split(rule.getPort(), "-");
          if (range.length != 2 || !NetworkUtils.isInetAddressPort(range[0])
              || !NetworkUtils.isInetAddressPort(range[1])
              || (Integer.parseInt(range[0]) >= Integer.parseInt(range[1]))) {
            return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
                .msg(String.format("规则端口格式不合法： [%s]", port)).build();
          }
        } else if (StringUtils.contains(port, ",")) {
          List<String> portList = CsvUtils.convertCSVToList(port);
          for (String onePort : portList) {
            if (!NetworkUtils.isInetAddressPort(onePort)) {
              return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
                  .msg(String.format("规则端口不合法： [%s]", onePort)).build();
            }
          }
        } else {
          return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则端口格式不合法： [%s]", port)).build();
        }
      }
      rule.setPort(StringUtils.defaultIfBlank(port, ""));

      // 签名类型校验
      if (StringUtils.isNotBlank(rule.getSignatureType())) {
        if (!SIGNATURE_TYPE.contains(rule.getSignatureType())) {
          return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则签名类型不合法： [%s]", rule.getSignatureType())).build();
        }

        // 签名偏移
        try {
          if (!RANGE_RULE_SIGNATURE_OFFSET.contains(Integer.parseInt(rule.getSignatureOffset()))) {
            return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
                .msg(String.format("规则签名偏移量[有效范围：%s]不合法： [%s]", RANGE_RULE_SIGNATURE_OFFSET,
                    rule.getSignatureOffset()))
                .build();
          }
        } catch (NumberFormatException e) {
          return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则签名偏移量格式不合法： [%s]", rule.getSignatureOffset())).build();
        }

        // 签名内容
        if (StringUtils.isNotBlank(rule.getSignatureContent())
            && (rule.getSignatureContent().length() % 2 != 0
                || !SIGNATURE_CONTENT_PATTERN.matcher(rule.getSignatureContent()).matches())) {
          return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则签名内容格式不合法： [%s]", rule.getSignatureContent())).build();
        }
      } else {
        if (StringUtils.isNotBlank(rule.getSignatureContent())
            || StringUtils.isNotBlank(rule.getSignatureOffset())) {
          return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则签名偏移量或内容格式不合法： [%s]", rule.getSignatureContent())).build();
        }

      }
      rule.setSignatureType(StringUtils.defaultIfBlank(rule.getSignatureType(), ""));
      rule.setSignatureOffset(StringUtils.defaultIfBlank(rule.getSignatureOffset(), ""));
      rule.setSignatureContent(StringUtils.defaultIfBlank(rule.getSignatureContent(), ""));
    }
    customAppVO.setRule(rules);

    return null;
  }

  private static Map<String, Object> categoryBO2Map(SaCustomCategoryBO categoryBO) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", categoryBO.getId());
    map.put("name", categoryBO.getName());
    map.put("categoryId", categoryBO.getCategoryId());
    map.put("subCategoryIds", categoryBO.getSubCategoryIds());
    map.put("description", categoryBO.getDescription());
    map.put("createTime", categoryBO.getCreateTime());
    map.put("updateTime", categoryBO.getUpdateTime());
    return map;
  }

  private static Map<String, Object> subCategoryBO2Map(SaCustomSubCategoryBO subCategoryBO) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", subCategoryBO.getId());
    map.put("name", subCategoryBO.getName());
    map.put("categoryId", subCategoryBO.getCategoryId());
    map.put("subCategoryId", subCategoryBO.getSubCategoryId());
    map.put("applicationIds", subCategoryBO.getApplicationIds());
    map.put("description", subCategoryBO.getDescription());
    map.put("createTime", subCategoryBO.getCreateTime());
    map.put("updateTime", subCategoryBO.getUpdateTime());
    return map;
  }

  private static Map<String, Object> applicationBO2Map(SaCustomApplicationBO applicationBO) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", applicationBO.getId());
    map.put("name", applicationBO.getName());
    map.put("categoryId", applicationBO.getCategoryId());
    map.put("subCategoryId", applicationBO.getSubCategoryId());
    map.put("l7ProtocolId", applicationBO.getL7ProtocolId());
    map.put("applicationId", applicationBO.getApplicationId());
    map.put("description", applicationBO.getDescription());
    map.put("createTime", applicationBO.getCreateTime());
    map.put("updateTime", applicationBO.getUpdateTime());
    List<Map<String, Object>> rule = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    rule.addAll(JsonHelper.deserialize(applicationBO.getRule(),
        new TypeReference<List<Map<String, Object>>>() {
        }, false));

    map.put("rule", rule);
    return map;
  }
}
