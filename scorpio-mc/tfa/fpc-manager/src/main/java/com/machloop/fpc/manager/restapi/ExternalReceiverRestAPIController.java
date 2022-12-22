package com.machloop.fpc.manager.restapi;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.manager.appliance.bo.ExternalReceiverBO;
import com.machloop.fpc.manager.appliance.service.ExternalReceiverService;
import com.machloop.fpc.manager.restapi.vo.ExternalReceiverRestAPIVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author ChenXiao
 * create at 2022/10/28
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class ExternalReceiverRestAPIController {


  @Autowired
  private ExternalReceiverService externalReceiverService;

  @Autowired
  private UserService userService;


  @GetMapping("/external-receiver")
  @RestApiSecured
  public RestAPIResultVO queryExternalReceiver() {

    return RestAPIResultVO.resultSuccess(externalReceiverService.queryExternalReceivers());
  }


  @GetMapping("/external-receiver/{id}")
  @RestApiSecured
  public RestAPIResultVO queryExternalReceiver(@PathVariable String id) {

    return RestAPIResultVO.resultSuccess(externalReceiverService.queryExternalReceiver(id));
  }

  @PostMapping("/external-receiver")
  @RestApiSecured
  public RestAPIResultVO saveExternalReceiver(
      @RequestBody @Validated ExternalReceiverRestAPIVO externalReceiverRestAPIVO,
      HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    ExternalReceiverBO externalReceiverBO = new ExternalReceiverBO();


    try {
      externalReceiverBO.setName(externalReceiverRestAPIVO.getName());
      externalReceiverBO.setReceiverType(externalReceiverRestAPIVO.getReceiverType());
      externalReceiverBO
          .setReceiverContent(MapUtils.isEmpty(externalReceiverRestAPIVO.getReceiverContent()) ? ""
              : JsonHelper.serialize(externalReceiverRestAPIVO.getReceiverContent(), false));
      externalReceiverService.saveExternalReceiver(externalReceiverBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, externalReceiverBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(externalReceiverBO.getId());
  }

  @PutMapping("/external-receiver/{id}")
  @RestApiSecured
  public RestAPIResultVO updateExternalReceiver(
      @PathVariable @NotEmpty(message = "修改外发服务器时传入的id不能为空") String id,
      @RequestBody @Validated ExternalReceiverRestAPIVO externalReceiverRestAPIVO,
      HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    ExternalReceiverBO externalReceiverBO = new ExternalReceiverBO();
    try {
      externalReceiverBO.setName(externalReceiverRestAPIVO.getName());
      externalReceiverBO.setReceiverType(externalReceiverRestAPIVO.getReceiverType());
      externalReceiverBO
          .setReceiverContent(MapUtils.isEmpty(externalReceiverRestAPIVO.getReceiverContent()) ? ""
              : JsonHelper.serialize(externalReceiverRestAPIVO.getReceiverContent(), false));
      externalReceiverService.updateExternalReceiver(externalReceiverBO, id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, externalReceiverBO, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/external-receiver/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteExternalReceiver(@PathVariable String id,
      HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    ExternalReceiverBO result = null;
    try {
      result = externalReceiverService.deleteExternalReceiver(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(id);
  }


}
