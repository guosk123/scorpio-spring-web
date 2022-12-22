package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.bo.StandardProtocolBO;
import com.machloop.fpc.manager.analysis.service.StandardProtocolService;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolCreationVO;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolModificationVO;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月18日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/analysis")
public class StandardProtocolRestAPIController {

  @Autowired
  private StandardProtocolService standardProtocolService;

  @Autowired
  private SaProtocolService saProtocolService;

  @Autowired
  private UserService userService;
  @Autowired
  private GlobalSettingService globalSettingService;

  private static final List<String> IP_PROTOCOLS = Lists.newArrayList("TCP", "UDP");


  @GetMapping("/standard-protocols")
  @RestApiSecured
  public RestAPIResultVO queryStandardProtocols() {

    List<Map<String, Object>> standardProtocols = standardProtocolService.queryStandardProtocols()
        .stream().map(item -> standardProtocolBO2Map(item)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(standardProtocols);
  }

  @PostMapping("/standard-protocols")
  @RestApiSecured
  public RestAPIResultVO saveStandardProtocol(
      @Validated StandardProtocolCreationVO standardProtocolVO, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
    BeanUtils.copyProperties(standardProtocolVO, standardProtocolBO);

    List<String> l7ProtocolIds = saProtocolService.queryProtocols().stream()
        .map(item -> (String) item.get("protocolId")).collect(Collectors.toList());

    String l7ProtocolId = standardProtocolBO.getL7ProtocolId();
    if (StringUtils.isNotBlank(l7ProtocolId) && !l7ProtocolIds.contains(l7ProtocolId)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不合法的应用层协议号：" + l7ProtocolId).build();
    }

    String ipProtocol = standardProtocolBO.getIpProtocol();
    if (StringUtils.isNotBlank(ipProtocol) && !IP_PROTOCOLS.contains(ipProtocol.toUpperCase())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不合法的传输层协议号：" + ipProtocol).build();
    }

    String port = standardProtocolBO.getPort();
    if (StringUtils.isNotBlank(port) && !NetworkUtils.isInetAddressPort(port)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的端口：" + port)
          .build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    StandardProtocolBO standardProtocol = new StandardProtocolBO();
    try {
      standardProtocol = standardProtocolService.saveStandardProtocol(standardProtocolBO,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, standardProtocol, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(standardProtocol.getId());
  }

  @PutMapping("/standard-protocols/{id}")
  @RestApiSecured
  public RestAPIResultVO updateStandardProtocol(
      @PathVariable @NotEmpty(message = "修改协议配置时传入的id不能为空") String id,
      @Validated StandardProtocolModificationVO standardProtocolVO, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
    BeanUtils.copyProperties(standardProtocolVO, standardProtocolBO);

    List<String> l7ProtocolIds = saProtocolService.queryProtocols().stream()
        .map(item -> (String) item.get("protocolId")).collect(Collectors.toList());

    String l7ProtocolId = standardProtocolBO.getL7ProtocolId();
    if (StringUtils.isNotBlank(l7ProtocolId) && !l7ProtocolIds.contains(l7ProtocolId)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不合法的应用层协议号：" + l7ProtocolId).build();
    }

    String ipProtocol = standardProtocolBO.getIpProtocol();
    if (StringUtils.isNotBlank(ipProtocol) && !IP_PROTOCOLS.contains(ipProtocol.toUpperCase())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不合法的传输层协议号：" + ipProtocol).build();
    }

    String port = standardProtocolBO.getPort();
    if (StringUtils.isNotBlank(port) && !NetworkUtils.isInetAddressPort(port)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的端口：" + port)
          .build();
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    StandardProtocolBO standardProtocol = new StandardProtocolBO();
    try {
      standardProtocol = standardProtocolService.updateStandardProtocol(id, standardProtocolBO,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, standardProtocol, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/standard-protocols/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteStandardProtocol(
      @PathVariable @NotEmpty(message = "删除协议配置时传入的id不能为空") String id, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }

    StandardProtocolBO standardProtocolBO = new StandardProtocolBO();
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    try {
      standardProtocolBO = standardProtocolService.deleteStandardProtocol(id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, standardProtocolBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);

  }

  private Map<String, Object> standardProtocolBO2Map(StandardProtocolBO standardProtocol) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", standardProtocol.getId());
    map.put("l7ProtocolId", standardProtocol.getL7ProtocolId());
    map.put("ipProtocol", standardProtocol.getIpProtocol());
    map.put("port", standardProtocol.getPort());
    map.put("source", standardProtocol.getSource());
    map.put("sourceText", standardProtocol.getSourceText());

    return map;
  }
}
