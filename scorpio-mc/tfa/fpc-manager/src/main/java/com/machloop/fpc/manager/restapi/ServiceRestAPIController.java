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
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;
import com.machloop.fpc.npm.appliance.vo.ServiceCreationVO;
import com.machloop.fpc.npm.appliance.vo.ServiceModificationVO;

/**
 * @author guosk
 *
 * create at 2021年3月29日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class ServiceRestAPIController {

  @Autowired
  private ServiceService serviceService;
  @Autowired
  private NetworkService networkService;
  @Autowired
  private LogicalSubnetService logicalSubnetService;
  @Autowired
  private SaService saService;
  @Autowired
  private UserService userService;
  @Autowired
  private GlobalSettingService globalSettingService;

  @GetMapping("/services")
  @RestApiSecured
  public RestAPIResultVO queryServices() {
    List<ServiceBO> services = serviceService.queryServicesBasicInfo();
    List<Map<String, Object>> resultList = services.stream().map(service -> serviceBO2Map(service))
        .collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @GetMapping("/services/{id}")
  @RestApiSecured
  public RestAPIResultVO queryService(@PathVariable String id) {
    ServiceBO serviceBO = serviceService.queryService(id);

    if (StringUtils.isBlank(serviceBO.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("业务不存在").build();
    }

    return RestAPIResultVO.resultSuccess(serviceBO2Map(serviceBO));
  }

  @PostMapping("/services")
  @RestApiSecured
  public RestAPIResultVO saveService(@RequestBody @Validated ServiceCreationVO serviceCreationVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    ServiceBO serviceBO = new ServiceBO();
    BeanUtils.copyProperties(serviceCreationVO, serviceBO);
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, serviceBO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    try {
      serviceBO.setDescription(StringUtils.defaultIfBlank(serviceBO.getDescription(), ""));
      serviceBO = serviceService.saveService(serviceBO, null, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, serviceBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(serviceBO.getId());
  }

  @PutMapping("/services/{id}")
  @RestApiSecured
  public RestAPIResultVO updateService(@PathVariable String id,
      @RequestBody @Validated ServiceModificationVO serviceModificationVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    ServiceBO serviceBO = new ServiceBO();
    BeanUtils.copyProperties(serviceModificationVO, serviceBO);
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, serviceBO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    try {
      serviceBO = serviceService.updateService(id, serviceBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, serviceBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/services/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteService(@PathVariable String id, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    ServiceBO serviceBO = null;
    try {
      serviceBO = serviceService.deleteService(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, serviceBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult, ServiceBO serviceBO) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 网络配置
    List<String> networkIds = networkService.queryNetworks().stream().map(NetworkBO::getId)
        .collect(Collectors.toList());
    networkIds.addAll(logicalSubnetService.queryLogicalSubnets().stream()
        .map(LogicalSubnetBO::getId).collect(Collectors.toList()));
    List<String> selectedNetworkIds = CsvUtils.convertCSVToList(serviceBO.getNetworkIds());
    if (!networkIds.containsAll(selectedNetworkIds)) {
      selectedNetworkIds.removeAll(networkIds);
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("网络配置中包含不存在的网络：[%s]", StringUtils.join(selectedNetworkIds, ",")))
          .build();
    }

    // 应用配置
    List<String> appIds = saService.queryAllAppsIdNameMapping().keySet().stream()
        .map(appId -> String.valueOf(appId)).collect(Collectors.toList());
    List<String> selectedAppIds = CsvUtils.convertCSVToList(serviceBO.getApplication());
    if (!appIds.containsAll(selectedAppIds)) {
      selectedAppIds.removeAll(appIds);
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("应用配置中包含不存在的应用：[%s]", StringUtils.join(selectedAppIds, ","))).build();
    }

    return null;
  }

  private static Map<String, Object> serviceBO2Map(ServiceBO service) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", service.getId());
    map.put("name", service.getName());
    map.put("networkIds", service.getNetworkIds());
    map.put("application", service.getApplication());
    map.put("description", service.getDescription());
    map.put("createTime", service.getCreateTime());

    return map;
  }

}
