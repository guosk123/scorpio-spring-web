package com.machloop.fpc.manager.restapi;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.manager.appliance.bo.SendPolicyBO;
import com.machloop.fpc.manager.appliance.service.SendPolicyService;
import com.machloop.fpc.manager.appliance.vo.SendPolicyCreationVO;
import com.machloop.fpc.manager.appliance.vo.SendPolicyModificationVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author ChenXiao
 * create at 2022/10/28
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class SendPolicyRestAPIController {


  @Autowired
  private UserService userService;

  @Autowired
  private SendPolicyService sendPolicyService;


  @GetMapping("/send-policy")
  @RestApiSecured
  public RestAPIResultVO querySendPolicies() {

    return RestAPIResultVO.resultSuccess(sendPolicyService.querySendPolicies());
  }

  @GetMapping("/send-policy/state-on")
  @RestApiSecured
  public RestAPIResultVO querySendPoliciesStateOn() {

    return RestAPIResultVO.resultSuccess(sendPolicyService.querySendPoliciesStateOn());
  }


  @GetMapping("/send-policy/{id}")
  @RestApiSecured
  public RestAPIResultVO querySendPolicy(@PathVariable String id) {

    return RestAPIResultVO.resultSuccess(sendPolicyService.querySendPolicy(id));
  }

  @PostMapping("/send-policy")
  @RestApiSecured
  public RestAPIResultVO saveSendPolicy(
      @RequestBody @Validated SendPolicyCreationVO sendPolicyCreationVO,
      HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    try {
      BeanUtils.copyProperties(sendPolicyCreationVO, sendPolicyBO);

      sendPolicyService.saveSendPolicy(sendPolicyBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, sendPolicyBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(sendPolicyBO.getId());

  }

  @PutMapping("/send-policy/{id}")
  @RestApiSecured
  public RestAPIResultVO updateSendPolicy(
      @PathVariable @NotEmpty(message = "修改外发策略时传入的id不能为空") String id,
      @RequestBody @Validated SendPolicyModificationVO sendPolicyModificationVO,
      HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    try {
      BeanUtils.copyProperties(sendPolicyModificationVO, sendPolicyBO);

      sendPolicyService.updateSendPolicy(sendPolicyBO, id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, sendPolicyBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/send-policy/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteSendPolicy(@PathVariable String id, HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SendPolicyBO result = null;
    try {
      result = sendPolicyService.deleteSendPolicy(id, userBO.getId(), false);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @PutMapping("/send-policy/{id}/state")
  @RestApiSecured
  public RestAPIResultVO changeSendPolicyState(
      @PathVariable @NotEmpty(message = "删除规则时传入的id不能为空") String id, String state,
      HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    SendPolicyBO result = null;
    try {
      result = sendPolicyService.changeSendPolicyState(id, state, userBO.getId());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result, userBO.getFullname(),
        userBO.getName());


    return RestAPIResultVO.resultSuccess(id);
  }


}
