package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
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

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.metadata.service.CollectPolicyService;
import com.machloop.fpc.manager.metadata.vo.CollectPolicyVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author fengtianyou
 *
 * create at 2021年9月22日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/metadata")
public class CollectPolicyRestAPIController {

  @Autowired
  private UserService userService;

  @Autowired
  private CollectPolicyService collectPolicyService;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private SaProtocolService saProtocolService;

  @GetMapping("/collect-policys")
  @RestApiSecured
  public RestAPIResultVO queryCollectPolicys() {
    List<CollectPolicyVO> collectPolicysList = collectPolicyService.queryCollectPolicys();
    return RestAPIResultVO.resultSuccess(collectPolicysList);
  }

  @GetMapping("/collect-policys/{id}")
  @RestApiSecured
  public RestAPIResultVO queryCollectPolicy(@PathVariable String id) {
    CollectPolicyVO collectPolicy = collectPolicyService.queryCollectPolicy(id);
    if (StringUtils.isBlank(collectPolicy.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("采集策略不存在").build();
    }
    return RestAPIResultVO.resultSuccess(collectPolicy);
  }

  @PostMapping("/collect-policys")
  @RestApiSecured
  public RestAPIResultVO saveCollectPolicy(@RequestBody @Validated CollectPolicyVO collectPolicyVO,
      BindingResult bindingResult, HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, collectPolicyVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    try {
      collectPolicyVO.setOperatorId(userBO.getId());
      collectPolicyVO = collectPolicyService.saveCollectPolicy(collectPolicyVO);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, collectPolicyVO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(collectPolicyVO.getId());
  }

  @PutMapping("/collect-policys/{id}")
  @RestApiSecured
  public RestAPIResultVO updateCollectPolicy(@PathVariable String id,
      @RequestBody @Validated CollectPolicyVO collectPolicyVO, BindingResult bindingResult,
      HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, collectPolicyVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    try {
      collectPolicyVO.setId(id);
      collectPolicyVO.setOperatorId(userBO.getId());
      collectPolicyVO = collectPolicyService.updateCollectPolicy(collectPolicyVO);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, collectPolicyVO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @PutMapping("/collect-policys/{id}/status")
  @RestApiSecured
  public RestAPIResultVO updateCollectPolicystatus(@PathVariable String id,
      @RequestBody Map<String, String> param, BindingResult bindingResult,
      HttpServletRequest request) {
    if (MapUtils.isEmpty(param)
        || !StringUtils.equalsAny(param.get("state"), Constants.BOOL_NO, Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("将要更改的状态值不合法")
          .build();
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    CollectPolicyVO resultVO = new CollectPolicyVO();

    try {
      resultVO = collectPolicyService.changeCollectPolicyState(id, param.get("state"),
          userBO.getId());
    } catch (BusinessException exception) {

      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, resultVO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/collect-policys/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteCollectPolicy(@PathVariable String id, HttpServletRequest request) {
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    CollectPolicyVO resultVO = new CollectPolicyVO();
    try {
      resultVO = collectPolicyService.deleteCollectPolicy(id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, resultVO, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult,
      CollectPolicyVO collectPolicyVO) {

    Map<String,
        String> levelDict = dictManager.getBaseDict().getItemMap("appliance_collect_policy_level");

    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }
    // 校验ip地址
    if (NetworkUtils.isInetAddress(collectPolicyVO.getIpAddress(), NetworkUtils.IpVersion.V4)
        && StringUtils.isNotBlank(collectPolicyVO.getIpAddress())) {
    } else if (StringUtils.isBlank(collectPolicyVO.getIpAddress())) {

    } else {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(collectPolicyVO.getIpAddress() + "格式非法, 请输入正确的IP地址").build();
    }

    // 校验l7协议id
    String[] idList = {};
    idList = StringUtils.split(collectPolicyVO.getL7ProtocolId(), ",");
    List<String> collect = saProtocolService.queryProtocols().stream()
        .filter(item -> MapUtils.getIntValue(item, "protocolId") <= 255)
        .map(item -> MapUtils.getString(item, "protocolId")).collect(Collectors.toList());
    collect.add("645");
    collect.add("flow_log");
    for (String id : idList) {
      if (!collect.contains(id)) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(collectPolicyVO.getL7ProtocolId() + "格式非法, 请输入正确的应用层协议id").build();
      }
    }

    // 校验级别
    if (!levelDict.containsKey(collectPolicyVO.getLevel())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(collectPolicyVO.getLevel() + "格式非法, 请输入正确的级别").build();
    }
    return null;
  }
}
