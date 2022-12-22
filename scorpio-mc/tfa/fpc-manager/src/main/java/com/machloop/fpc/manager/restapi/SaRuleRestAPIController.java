package com.machloop.fpc.manager.restapi;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.knowledge.bo.*;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.manager.knowledge.vo.SaCustomCategoryCreationVO;
import com.machloop.fpc.manager.knowledge.vo.SaCustomCategoryModificationVO;
import com.machloop.fpc.manager.knowledge.vo.SaCustomSubCategoryCreationVO;
import com.machloop.fpc.manager.knowledge.vo.SaCustomSubCategoryModificationVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.restapi.vo.SaCustomApplicationVO;
import com.machloop.fpc.manager.restapi.vo.SaCustomApplicationVO.AppRule;

import reactor.util.function.Tuple3;

/**
 * @author guosk
 *
 * create at 2021年9月8日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class SaRuleRestAPIController {

  private static final int MAX_RULE_NUMBER = 16;
  private static final int MAX_NAME_LENGTH = 30;
  private static final Range<Integer> RANGE_RULE_SIGNATURE_OFFSET = Range.closed(-1, 1500);
  private static final List<String> IP_PROTOCOLS = Lists.newArrayList("TCP", "UDP", "ALL");
  private static final List<String> SIGNATURE_TYPE = Lists.newArrayList("", "hex", "ascii");
  private static final List<String> ILLEGAL_SUBCATEGORY = Lists.newArrayList("30", "31");

  private static final Pattern SIGNATURE_CONTENT_PATTERN = Pattern.compile("-?[0-9a-fA-F]{2,64}$");

  @Autowired
  private SaService saService;

  @Autowired
  private UserService userService;

  @Autowired
  private SaProtocolService saProtocolService;

  @Autowired
  private GlobalSettingService globalSettingService;

  /**
   * SA规则库导入
   */
  @PostMapping("/sa/knowledges")
  @RestApiSecured
  public RestAPIResultVO importKnowledges(@RequestParam MultipartFile file,
      HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    SaKnowledgeInfoBO knowledgeBO = null;
    try {
      knowledgeBO = saService.importKnowledges(file);

      // 获取用户信息
      UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, knowledgeBO, userBO.getFullname(),
          userBO.getName());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

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
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
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
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
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

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customCategoryBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customCategoryBO.getId());
  }

  @DeleteMapping("/sa/custom-categorys/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteSaCustomCategory(
      @PathVariable @NotEmpty(message = "删除自定义SA分类时传入的id不能为空") String id,
      HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
    try {
      customCategoryBO = saService.deleteCustomCategory(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customCategoryBO, userBO.getFullname(),
        userBO.getName());

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
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
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

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customSubCategoryBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customSubCategoryBO.getId());
  }

  @PutMapping("/sa/custom-subcategorys/{id}")
  @RestApiSecured
  public RestAPIResultVO updateSaCustomSubCategory(
      @PathVariable @NotEmpty(message = "修改自定义SA子分类时传入的id不能为空") String id,
      @RequestBody @Validated SaCustomSubCategoryModificationVO customSubCategoryVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
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

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customSubCategoryBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customSubCategoryBO.getId());
  }

  @DeleteMapping("/sa/custom-subcategorys/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteSaCustomSubCategory(
      @PathVariable @NotEmpty(message = "删除自定义SA子分类时传入的id不能为空") String id,
      HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SaCustomSubCategoryBO customSubCategoryBO = new SaCustomSubCategoryBO();
    try {
      customSubCategoryBO = saService.deleteCustomSubCategory(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customSubCategoryBO, userBO.getFullname(),
        userBO.getName());

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
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
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
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的应用层协议ID")
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
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
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
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的应用层协议ID")
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
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
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
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
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
        return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE)
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
      existSubCategoryIds.removeAll(ILLEGAL_SUBCATEGORY);

      List<String> subCategoryIdList = CsvUtils.convertCSVToList(subCategoryIds);
      if (!existSubCategoryIds.containsAll(subCategoryIdList)) {
        subCategoryIdList.removeAll(existSubCategoryIds);
        return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE)
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
        return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE)
            .msg(String.format("不存在的应用ID： %s", appIdList)).build();
      }
    }

    // 校验所选子分类是否属于所选分类
    if (StringUtils.isNotBlank(categoryIds) && StringUtils.isNotBlank(subCategoryIds)) {
      Map<String,
          String> existSubCategorys = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      existSubCategorys.putAll(rules.getT2().stream().collect(
          Collectors.toMap(SaSubCategoryBO::getSubCategoryId, SaSubCategoryBO::getCategoryId)));
      existSubCategorys.putAll(saService.queryCustomSubCategorys().stream().collect(Collectors
          .toMap(SaCustomSubCategoryBO::getSubCategoryId, SaCustomSubCategoryBO::getCategoryId)));

      if (!StringUtils.equals(categoryIds, existSubCategorys.get(subCategoryIds))) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("子分类ID[%s]不属于分类ID[%s]", subCategoryIds, categoryIds)).build();
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
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("规则数量超过最大限制：[%s]", MAX_RULE_NUMBER)).build();
    }

    Set<AppRule> removal = Sets.newHashSet(rules);
    if (removal.size() < rules.size()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("存在重复的规则")
          .build();
    }

    for (AppRule rule : rules) {
      // 字段非空判断
      if (StringUtils.isBlank(rule.getName())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("规则名称不能为空")
            .build();
      }
      if (StringUtils.isBlank(rule.getProtocol())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("规则传输层协议不能为空")
            .build();
      }
      if (StringUtils.isAllBlank(rule.getIpAddress(), rule.getPort(), rule.getSignatureType())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg("规则P地址、端口、签名请至少填写1项").build();
      }
      if (StringUtils.isNotBlank(rule.getSignatureType())
          && StringUtils.isAnyBlank(rule.getSignatureOffset(), rule.getSignatureContent())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("规则签名信息不完整")
            .build();
      }

      // 名称长度校验
      if (rule.getName().length() > MAX_NAME_LENGTH) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("规则名称长度超过最大限制： [%s]", MAX_NAME_LENGTH)).build();
      }

      // IP校验
      if (StringUtils.isNotBlank(rule.getIpAddress())
          && !(NetworkUtils.isInetAddress(rule.getIpAddress())
              || NetworkUtils.isCidr(rule.getIpAddress()))) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("规则IP地址不合法： [%s]", rule.getIpAddress())).build();
      }
      rule.setIpAddress(StringUtils.defaultIfBlank(rule.getIpAddress(), ""));

      // 协议校验
      if (!IP_PROTOCOLS.contains(StringUtils.upperCase(rule.getProtocol()))) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
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
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg(String.format("规则端口格式不合法： [%s]", port)).build();
          }
        } else if (StringUtils.contains(port, ",")) {
          List<String> portList = CsvUtils.convertCSVToList(port);
          for (String onePort : portList) {
            if (!NetworkUtils.isInetAddressPort(onePort)) {
              return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                  .msg(String.format("规则端口不合法： [%s]", onePort)).build();
            }
          }
        } else {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则端口格式不合法： [%s]", port)).build();
        }
      }
      rule.setPort(StringUtils.defaultIfBlank(port, ""));

      // 签名类型校验
      if (StringUtils.isNotBlank(rule.getSignatureType())) {
        if (!SIGNATURE_TYPE.contains(rule.getSignatureType())) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则签名类型不合法： [%s]", rule.getSignatureType())).build();
        }

        // 签名偏移
        try {
          if (!RANGE_RULE_SIGNATURE_OFFSET.contains(Integer.parseInt(rule.getSignatureOffset()))) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg(String.format("规则签名偏移量[有效范围：%s]不合法： [%s]", RANGE_RULE_SIGNATURE_OFFSET,
                    rule.getSignatureOffset()))
                .build();
          }
        } catch (NumberFormatException e) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则签名偏移量格式不合法： [%s]", rule.getSignatureOffset())).build();
        }

        // 签名内容
        if (StringUtils.isNotBlank(rule.getSignatureContent())
            && (rule.getSignatureContent().length() % 2 != 0
                || !SIGNATURE_CONTENT_PATTERN.matcher(rule.getSignatureContent()).matches())) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("规则签名内容格式不合法： [%s]", rule.getSignatureContent())).build();
        }
      } else {
        if (StringUtils.isNotBlank(rule.getSignatureContent())
            || StringUtils.isNotBlank(rule.getSignatureOffset())) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
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
