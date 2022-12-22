package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.HostGroupBO;
import com.machloop.fpc.manager.appliance.service.HostGroupService;
import com.machloop.fpc.manager.appliance.vo.HostGroupCreationVO;
import com.machloop.fpc.manager.appliance.vo.HostGroupModificationVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author guosk
 *
 * create at 2021年3月29日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class HostGroupRestAPIController {

  @Autowired
  private HostGroupService hostGroupService;
  @Autowired
  private UserService userService;
  @Autowired
  private GlobalSettingService globalSettingService;

  @GetMapping("/host-groups")
  @RestApiSecured
  public RestAPIResultVO queryHostGroups() {
    List<HostGroupBO> hostGroupBOs = hostGroupService.queryHostGroups();

    List<Map<String, Object>> resultList = hostGroupBOs.stream()
        .map(hostGroup -> hostGroupBO2Map(hostGroup)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @GetMapping("/host-groups/{id}")
  @RestApiSecured
  public RestAPIResultVO queryHostGroup(@PathVariable String id) {
    HostGroupBO hostGroupBO = hostGroupService.queryHostGroup(id);

    if (StringUtils.isBlank(hostGroupBO.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("地址组不存在").build();
    }

    return RestAPIResultVO.resultSuccess(hostGroupBO2Map(hostGroupBO));
  }

  @PostMapping("/host-groups")
  @RestApiSecured
  public RestAPIResultVO saveHostGroup(
      @RequestBody @Validated HostGroupCreationVO hostGroupCreationVO, BindingResult bindingResult,
      HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    // 校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    HostGroupBO hostGroupBO = new HostGroupBO();
    try {
      BeanUtils.copyProperties(hostGroupCreationVO, hostGroupBO);
      hostGroupBO.setDescription(StringUtils.defaultIfBlank(hostGroupBO.getDescription(), ""));
      hostGroupBO = hostGroupService.saveHostGroup(hostGroupBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, hostGroupBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(hostGroupBO.getId());
  }

  @PutMapping("/host-groups/{id}")
  @RestApiSecured
  public RestAPIResultVO updateHostGroup(@PathVariable String id,
      @RequestBody @Validated HostGroupModificationVO serviceModificationVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    // 校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    HostGroupBO hostGroupBO = new HostGroupBO();
    try {
      BeanUtils.copyProperties(serviceModificationVO, hostGroupBO);
      hostGroupBO = hostGroupService.updateHostGroup(id, hostGroupBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, hostGroupBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/host-groups/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteHostGroup(@PathVariable String id, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    HostGroupBO hostGroupBO = null;
    try {
      hostGroupBO = hostGroupService.deleteHostGroup(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, hostGroupBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private static Map<String, Object> hostGroupBO2Map(HostGroupBO hostGroup) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", hostGroup.getId());
    map.put("name", hostGroup.getName());
    map.put("ipAddress", hostGroup.getIpAddress());
    map.put("description", hostGroup.getDescription());

    return map;
  }

}
