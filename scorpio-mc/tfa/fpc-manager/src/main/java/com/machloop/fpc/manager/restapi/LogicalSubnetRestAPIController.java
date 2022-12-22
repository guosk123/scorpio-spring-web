package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.vo.LogicalSubnetCreationVO;
import com.machloop.fpc.npm.appliance.vo.LogicalSubnetModificationVO;

/**
 * @author guosk
 *
 * create at 2021年7月30日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class LogicalSubnetRestAPIController {

  private static final int MAX_IP_NUMBER = 50;
  private static final int MAX_MAC_NUMBER = 50;
  private static final int MAX_VLAN_LENGTH = 256;
  private static final int MAX_MPLS_LENGTH = 256;
  private static final int MAX_VXLAN_LENGTH = 256;
  private static final int MAX_GRE_KEYWORD_LENGTH = 256;

  private static final Pattern MAC_PATTERN = Pattern
      .compile("^[A-Fa-f0-9]{2}([-,:][A-Fa-f0-9]{2}){5}$", Pattern.MULTILINE);
  private static final Pattern GRE_KEYWORD_PATTERN = Pattern.compile("\\w{1,256}");

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private UserService userService;

  @Autowired
  private DictManager dictManager;

  @GetMapping("/logical-subnets")
  @RestApiSecured
  public RestAPIResultVO queryLogicalSubnets() {
    List<LogicalSubnetBO> logicalSubnets = logicalSubnetService.queryLogicalSubnets();
    List<Map<String, Object>> resultList = logicalSubnets.stream()
        .map(logicalSubnet -> logicalSubnetBO2Map(logicalSubnet)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @GetMapping("/logical-subnets/{id}")
  @RestApiSecured
  public RestAPIResultVO queryLogicalSubnet(@PathVariable String id) {
    LogicalSubnetBO logicalSubnetBO = logicalSubnetService.queryLogicalSubnet(id);

    if (StringUtils.isBlank(logicalSubnetBO.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("逻辑子网不存在").build();
    }

    return RestAPIResultVO.resultSuccess(logicalSubnetBO2Map(logicalSubnetBO));
  }

  @PostMapping("/logical-subnets")
  @RestApiSecured
  public RestAPIResultVO saveLogicalSubnet(
      @RequestBody @Validated LogicalSubnetCreationVO logicalSubnetVO, BindingResult bindingResult,
      HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    LogicalSubnetBO logicalSubnetBO = new LogicalSubnetBO();
    BeanUtils.copyProperties(logicalSubnetVO, logicalSubnetBO);
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, logicalSubnetBO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    try {
      logicalSubnetBO
          .setDescription(StringUtils.defaultIfBlank(logicalSubnetBO.getDescription(), ""));
      logicalSubnetBO = logicalSubnetService.saveLogicalSubnet(logicalSubnetBO, null,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, logicalSubnetBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(logicalSubnetBO.getId());
  }

  @PutMapping("/logical-subnets/{id}")
  @RestApiSecured
  public RestAPIResultVO updateLogicalSubnet(@PathVariable String id,
      @RequestBody @Validated LogicalSubnetModificationVO logicalSubnetVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    LogicalSubnetBO logicalSubnetBO = new LogicalSubnetBO();
    BeanUtils.copyProperties(logicalSubnetVO, logicalSubnetBO);
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, logicalSubnetBO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    try {
      logicalSubnetBO
          .setDescription(StringUtils.defaultIfBlank(logicalSubnetBO.getDescription(), ""));
      logicalSubnetBO = logicalSubnetService.updateLogicalSubnet(id, logicalSubnetBO,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, logicalSubnetBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/logical-subnets/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteLogicalSubnet(@PathVariable String id, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    LogicalSubnetBO logicalSubnetBO = new LogicalSubnetBO();
    try {
      logicalSubnetBO = logicalSubnetService.deleteLogicalSubnet(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, logicalSubnetBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult,
      LogicalSubnetBO logicalSubnetBO) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 校验子网类型
    Map<String,
        String> subnetTypeDict = dictManager.getBaseDict().getItemMap("appliance_subnet_type");
    if (!subnetTypeDict.containsKey(logicalSubnetBO.getType())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的子网类型")
          .build();
    }

    // 校验子网配置
    String configuration = logicalSubnetBO.getConfiguration();
    switch (logicalSubnetBO.getType()) {
      case FpcConstants.APPLIANCE_SUBNET_TYPE_IP:
      {
        List<String> ipList = CsvUtils.convertCSVToList(configuration);
        if (ipList.size() > MAX_IP_NUMBER) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("IP子网络最多可配置[%s]个IP地址", MAX_IP_NUMBER)).build();
        }
        for (String ip : ipList) {
          if (StringUtils.contains(ip, "-")) {
            String[] ipRange = StringUtils.split(ip, "-");
            // 起止都是正确的ip
            if (ipRange.length != 2 || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]))
                || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]))) {
              return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                  .msg(ip + "格式非法, 请输入正确的IP地址").build();
            }
            if (!(NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V4)
                && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V4)
                || (NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V6)
                    && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V6)))) {
              return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                  .msg(ip + "格式非法, 请输入正确的IP地址").build();
            }
          } else if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg(ip + "格式非法, 请输入正确的IP地址").build();
          }
        }
      }
        break;
      case FpcConstants.APPLIANCE_SUBNET_TYPE_MAC:
      {
        List<String> macList = CsvUtils.convertCSVToList(configuration);
        if (macList.size() > MAX_MAC_NUMBER) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("MAC子网络最多可配置[%s]个MAC地址", MAX_MAC_NUMBER)).build();
        }
        for (String mac : macList) {
          if (!MAC_PATTERN.matcher(mac).matches()) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg(mac + "格式非法, 请输入正确的MAC地址").build();
          }
        }
      }
        break;
      case FpcConstants.APPLIANCE_SUBNET_TYPE_VLAN:
      {
        if (configuration.length() > MAX_VLAN_LENGTH) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("VLAN配置允许输入的最大长度为[%s]", MAX_VLAN_LENGTH)).build();
        }
        List<String> vlanList = CsvUtils.convertCSVToList(configuration);
        for (String vlanId : vlanList) {
          if (!StringUtils.isNumeric(vlanId)) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg(vlanId + "非数字格式").build();
          }
        }
      }
        break;
      case FpcConstants.APPLIANCE_SUBNET_TYPE_MPLS:
      {
        if (configuration.length() > MAX_MPLS_LENGTH) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("MPLS配置允许输入的最大长度为[%s]", MAX_MPLS_LENGTH)).build();
        }
        List<String> mplsList = CsvUtils.convertCSVToList(configuration);
        for (String mplsId : mplsList) {
          if (!StringUtils.isNumeric(mplsId)) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg(mplsId + "非数字格式").build();
          }
        }
      }
        break;
      case FpcConstants.APPLIANCE_SUBNET_TYPE_GRE:
      {
        try {
          Map<String, String> greConfiguration = JsonHelper.deserialize(configuration,
              new TypeReference<Map<String, String>>() {
              }, false);
          if (greConfiguration.containsKey("greKey")) {
            if (greConfiguration.get("greKey").length() > MAX_GRE_KEYWORD_LENGTH) {
              return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                  .msg(String.format("GRE隧道识别关键字配置允许输入的最大长度为[%s]", MAX_GRE_KEYWORD_LENGTH)).build();
            }
            List<String> keyList = CsvUtils.convertCSVToList(configuration);
            for (String key : keyList) {
              if (!GRE_KEYWORD_PATTERN.matcher(key).matches()) {
                return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                    .msg(key + "格式非法, 请输入正确的隧道识别关键字").build();
              }
            }
          } else if (greConfiguration.containsKey("greIp")) {
            String ip = greConfiguration.get("greIp");
            if (StringUtils.contains(ip, "-")) {
              String[] ipRange = StringUtils.split(ip, "-");
              // 起止都是正确的ip
              if (ipRange.length != 2 || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]))
                  || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]))) {
                return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                    .msg(ip + "格式非法, 请输入正确的隧道IP地址").build();
              }
              if (!(NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V4)
                  && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V4)
                  || (NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V6)
                      && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V6)))) {
                return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                    .msg(ip + "格式非法, 请输入正确的隧道IP地址").build();
              }
            } else if (!NetworkUtils.isInetAddress(ip)) {
              return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                  .msg(ip + "格式非法, 请输入正确的隧道IP地址").build();
            }
          } else {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg("不合法的GRE通道配置").build();
          }
        } catch (UnsupportedOperationException e) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的GRE通道配置")
              .build();
        }
      }
        break;
      case FpcConstants.APPLIANCE_SUBNET_TYPE_VXLAN:
      {
        if (configuration.length() > MAX_VXLAN_LENGTH) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("VXLAN配置允许输入的最大长度为[%s]", MAX_VXLAN_LENGTH)).build();
        }
        List<String> vxlanList = CsvUtils.convertCSVToList(configuration);
        for (String vxlanId : vxlanList) {
          if (!StringUtils.isNumeric(vxlanId)) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg(vxlanId + "非数字格式").build();
          }
        }
      }
        break;
    }

    return null;
  }

  private Map<String, Object> logicalSubnetBO2Map(LogicalSubnetBO logicalSubnetBO) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", logicalSubnetBO.getId());
    map.put("name", logicalSubnetBO.getName());
    map.put("networkId", logicalSubnetBO.getNetworkId());
    map.put("networkName", logicalSubnetBO.getNetworkName());
    map.put("bandwidth", logicalSubnetBO.getBandwidth());
    map.put("type", logicalSubnetBO.getType());
    map.put("typeText", logicalSubnetBO.getTypeText());
    map.put("configuration", logicalSubnetBO.getConfiguration());
    map.put("description", logicalSubnetBO.getDescription());
    map.put("createTime", logicalSubnetBO.getCreateTime());

    return map;
  }

}
