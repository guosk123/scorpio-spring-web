package com.machloop.fpc.cms.center.appliance.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.appliance.bo.SendPolicyBO;
import com.machloop.fpc.cms.center.appliance.service.SendPolicyService;
import com.machloop.fpc.cms.center.appliance.vo.SendPolicyCreationVO;
import com.machloop.fpc.cms.center.appliance.vo.SendPolicyModificationVO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class SendPolicyController {

  @Autowired
  private SendPolicyService sendPolicyService;


  @GetMapping("/send-policy")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> querySendPolicies() {

    return sendPolicyService.querySendPolicies();
  }

  @GetMapping("/send-policy/state-on")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> querySendPoliciesStateOn() {

    return sendPolicyService.querySendPoliciesStateOn();
  }


  @GetMapping("/send-policy/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySendPolicy(@PathVariable String id) {

    return sendPolicyService.querySendPolicy(id);
  }

  @PostMapping("/send-policy")
  @Secured({"PERM_USER"})
  public void saveSendPolicy(@Validated SendPolicyCreationVO sendPolicyCreationVO) {

    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    BeanUtils.copyProperties(sendPolicyCreationVO, sendPolicyBO);

    SendPolicyBO result = sendPolicyService.saveSendPolicy(sendPolicyBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);

  }

  @PutMapping("/send-policy/{id}")
  @Secured({"PERM_USER"})
  public void updateSendPolicy(@PathVariable @NotEmpty(message = "修改外发策略时传入的id不能为空") String id,
      @Validated SendPolicyModificationVO sendPolicyModificationVO) {

    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    BeanUtils.copyProperties(sendPolicyModificationVO, sendPolicyBO);

    SendPolicyBO result = sendPolicyService.updateSendPolicy(sendPolicyBO, id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }

  @DeleteMapping("/send-policy/{id}")
  @Secured({"PERM_USER"})
  public void deleteSendPolicy(@PathVariable String id) {

    SendPolicyBO result = sendPolicyService.deleteSendPolicy(id,
        LoggedUserContext.getCurrentUser().getId(), false);
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result);
  }

  @PutMapping("/send-policy/{id}/state")
  @Secured({"PERM_USER"})
  public void changeSendPolicyState(@PathVariable @NotEmpty(message = "删除规则时传入的id不能为空") String id,
      String state) {

    SendPolicyBO result = sendPolicyService.changeSendPolicyState(id, state,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }
}
