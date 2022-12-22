package com.machloop.fpc.manager.restapi;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.manager.appliance.bo.SendRuleBO;
import com.machloop.fpc.manager.appliance.service.SendRuleService;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.restapi.vo.SendRuleRestAPIVO;

/**
 * @author ChenXiao
 * create at 2022/10/28
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class SendRuleRestAPIController {


  @Autowired
  private UserService userService;


  @Autowired
  private SendRuleService sendRuleService;


  @GetMapping("/send-rule")
  @RestApiSecured
  public RestAPIResultVO querySendRules() {

    return RestAPIResultVO.resultSuccess(sendRuleService.querySendRules());
  }

  @GetMapping("/send-rule/{id}")
  @RestApiSecured
  public RestAPIResultVO querySendRule(@PathVariable String id) {

    return RestAPIResultVO.resultSuccess(sendRuleService.querySendRule(id));
  }


  @GetMapping("/send-rule/clickhouse-tables")
  @RestApiSecured
  public RestAPIResultVO queryClickhouseTables() {

    return RestAPIResultVO.resultSuccess(sendRuleService.queryClickhouseTables());
  }

  @PostMapping("/send-rule")
  @RestApiSecured
  public RestAPIResultVO saveSendRule(@RequestBody @Validated SendRuleRestAPIVO sendRuleRestAPIVO,
      HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SendRuleBO sendRuleBO = new SendRuleBO();

    try {
      sendRuleBO.setName(sendRuleRestAPIVO.getName());
      sendRuleBO
          .setSendRuleContent(CollectionUtils.isEmpty(sendRuleRestAPIVO.getSendRuleContent()) ? ""
              : JsonHelper.serialize(sendRuleRestAPIVO.getSendRuleContent(), false));
      sendRuleService.saveSendRule(sendRuleBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, sendRuleBO, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(sendRuleBO.getId());
  }

  @PutMapping("/send-rule/{id}")
  @RestApiSecured
  public RestAPIResultVO updateSendRule(
      @PathVariable @NotEmpty(message = "修改外发服务器时传入的id不能为空") String id,
      @RequestBody @Validated SendRuleRestAPIVO sendRuleRestAPIVO, HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SendRuleBO sendRuleBO = new SendRuleBO();
    try {
      sendRuleBO.setName(sendRuleRestAPIVO.getName());
      sendRuleBO
          .setSendRuleContent(CollectionUtils.isEmpty(sendRuleRestAPIVO.getSendRuleContent()) ? ""
              : JsonHelper.serialize(sendRuleRestAPIVO.getSendRuleContent(), false));
      sendRuleService.updateSendRule(sendRuleBO, id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, sendRuleBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/send-rule/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteSendRule(@PathVariable String id, HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SendRuleBO result = null;
    try {
      result = sendRuleService.deleteSendRule(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }


}
