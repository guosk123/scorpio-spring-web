package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
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
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.npm.analysis.bo.AbnormalEventRuleBO;
import com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleCreationVO;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleModificationVO;

/**
 * @author guosk
 *
 * create at 2021年9月9日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-v1/analysis")
public class AbnormalEventRuleRestAPIController {

  @Autowired
  private AbnormalEventRuleService abnormalEventRuleService;

  @Autowired
  private UserService userService;

  @GetMapping("/abnormal-event-rules")
  @RestApiSecured
  public RestAPIResultVO queryAbnormalEventRules() {
    List<AbnormalEventRuleBO> abnormalEventRules = abnormalEventRuleService
        .queryAbnormalEventRules();

    List<Map<String, Object>> result = abnormalEventRules.stream()
        .map(abnormalEventRule -> abnormalEventRuleBO2Map(abnormalEventRule))
        .collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(result);
  }

  @GetMapping("/abnormal-event-rules/{id}")
  @RestApiSecured
  public RestAPIResultVO queryAbnormalEventRule(@PathVariable String id) {
    AbnormalEventRuleBO abnormalEventRule = abnormalEventRuleService.queryAbnormalEventRule(id);

    if (StringUtils.isBlank(abnormalEventRule.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("异常事件规则不存在")
          .build();
    }

    return RestAPIResultVO.resultSuccess(abnormalEventRuleBO2Map(abnormalEventRule));
  }

  @PostMapping("/abnormal-event-rules")
  @RestApiSecured
  public RestAPIResultVO saveAbnormalEventRule(
      @RequestBody @Validated AbnormalEventRuleCreationVO abnormalEventRuleCreationVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    AbnormalEventRuleBO abnormalEventRuleBO = new AbnormalEventRuleBO();
    try {
      BeanUtils.copyProperties(abnormalEventRuleCreationVO, abnormalEventRuleBO);
      abnormalEventRuleBO
          .setDescription(StringUtils.defaultIfBlank(abnormalEventRuleBO.getDescription(), ""));
      abnormalEventRuleBO = abnormalEventRuleService.saveAbnormalEventRule(abnormalEventRuleBO,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, abnormalEventRuleBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(abnormalEventRuleBO.getId());
  }

  @PutMapping("/abnormal-event-rules/{id}")
  @RestApiSecured
  public RestAPIResultVO updateAbnormalEventRule(@PathVariable String id,
      @RequestBody @Validated AbnormalEventRuleModificationVO abnormalEventRuleModificationVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    AbnormalEventRuleBO abnormalEventRuleBO = new AbnormalEventRuleBO();
    try {
      BeanUtils.copyProperties(abnormalEventRuleModificationVO, abnormalEventRuleBO);
      abnormalEventRuleBO
          .setDescription(StringUtils.defaultIfBlank(abnormalEventRuleBO.getDescription(), ""));
      abnormalEventRuleBO = abnormalEventRuleService.updateAbnormalEventRule(id,
          abnormalEventRuleBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, abnormalEventRuleBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @PutMapping("/abnormal-event-rules/{id}/status")
  @RestApiSecured
  public RestAPIResultVO updateStatus(@PathVariable String id,
      @RequestBody Map<String, String> param, HttpServletRequest request) {
    if (MapUtils.isEmpty(param)
        || !StringUtils.equalsAny(param.get("status"), Constants.BOOL_NO, Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("将要更改的状态值不合法")
          .build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    AbnormalEventRuleBO abnormalEventRule = null;
    try {
      abnormalEventRule = abnormalEventRuleService.updateStatus(id, param.get("status"),
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, abnormalEventRule, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/abnormal-event-rules/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteAbnormalEventRule(@PathVariable String id,
      HttpServletRequest request) {
    AbnormalEventRuleBO abnormalEventRule = new AbnormalEventRuleBO();
    try {
      abnormalEventRule = abnormalEventRuleService.deleteAbnormalEventRule(id);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, abnormalEventRule, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private Map<String, Object> abnormalEventRuleBO2Map(AbnormalEventRuleBO abnormalEventRule) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", abnormalEventRule.getId());
    map.put("type", abnormalEventRule.getType());
    map.put("content", abnormalEventRule.getContent());
    map.put("source", abnormalEventRule.getSource());
    map.put("status", abnormalEventRule.getStatus());
    map.put("description", abnormalEventRule.getDescription());
    map.put("timestamp", abnormalEventRule.getTimestamp());

    return map;
  }

}
