package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.restapi.vo.DeviceNetifVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.npm.appliance.service.NetworkService;

/**
 * @author guosk
 *
 * create at 2021年9月8日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class DeviceNetifRestAPIController {

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private UserService userService;

  @GetMapping("/device-netifs")
  @RestApiSecured
  public RestAPIResultVO queryDeviceNetifs() {
    List<DeviceNetifBO> deviceNetifList = deviceNetifService.queryDeviceNetifs();

    List<Map<String, Object>> resultList = deviceNetifList.stream().map(deviceNetif -> {
      Map<String,
          Object> resultMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      resultMap.put("id", deviceNetif.getId());
      resultMap.put("name", deviceNetif.getName());
      resultMap.put("state", deviceNetif.getState());
      resultMap.put("category", deviceNetif.getCategory());
      resultMap.put("specification", deviceNetif.getSpecification());
      resultMap.put("description", deviceNetif.getDescription());

      return resultMap;
    }).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @PutMapping("/device-netifs/{id}")
  @RestApiSecured
  public RestAPIResultVO updateDeviceNetifs(@PathVariable String id,
      @RequestBody DeviceNetifVO deviceNetifVO, HttpServletRequest request) {
    // 校验类型
    if (!StringUtils.equalsAny(deviceNetifVO.getCategory(),
        FpcConstants.DEVICE_NETIF_CATEGORY_INGEST, FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的业务接口类型")
          .build();
    }
    // 判断接口是否存在
    Map<String, String> allNetifIdNames = deviceNetifService.queryDeviceNetifs().stream().filter(
        netif -> !StringUtils.equals(netif.getCategory(), FpcConstants.DEVICE_NETIF_CATEGORY_MGMT))
        .collect(Collectors.toMap(DeviceNetifBO::getId, DeviceNetifBO::getName));
    if (!allNetifIdNames.containsKey(id)) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE)
          .msg("不存在的业务接口ID（仅业务口可编辑接口用途）").build();
    }

    // 判断接口是否已配置在网络内
    List<String> configuredNetif = networkService.queryNetworkNetif().stream().map(item -> {
      return MapUtils.getString(item, "netifName");
    }).collect(Collectors.toList());
    if (configuredNetif.contains(allNetifIdNames.get(id))) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("接口已经被网络使用，无法修改接口用途").build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    DeviceNetifBO deviceNetifBO = new DeviceNetifBO();
    try {
      BeanUtils.copyProperties(deviceNetifVO, deviceNetifBO);
      deviceNetifBO.setId(id);
      deviceNetifBO.setDescription(StringUtils.defaultIfBlank(deviceNetifBO.getDescription(), ""));
      deviceNetifService.batchUpdateDeviceNetifs(Lists.newArrayList(deviceNetifBO), userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    StringBuilder logContent = new StringBuilder("修改接口配置：");
    logContent.append(deviceNetifBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(), logContent.toString());

    return RestAPIResultVO.resultSuccess(id);
  }

}
