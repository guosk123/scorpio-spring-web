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
import com.machloop.fpc.cms.center.appliance.bo.SendRuleBO;
import com.machloop.fpc.cms.center.appliance.service.SendRuleService;
import com.machloop.fpc.cms.center.appliance.vo.SendRuleCreationVO;
import com.machloop.fpc.cms.center.appliance.vo.SendRuleModificationVO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class SendRuleController {

  @Autowired
  private SendRuleService sendRuleService;


  @GetMapping("/send-rule")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> querySendRules() {

    return sendRuleService.querySendRules();
  }

  @GetMapping("/send-rule/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySendRule(@PathVariable String id) {

    return sendRuleService.querySendRule(id);
  }

  @GetMapping("/send-rule/clickhouse-table/{index}")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> querySendRuleTables(@PathVariable String index) {

    return sendRuleService.querySendRuleTables(index);
  }

  @GetMapping("/send-rule/clickhouse-tables")
  @Secured({"PERM_USER"})
  public Map<String, List<Map<String, Object>>> queryClickhouseTables() {

    return sendRuleService.queryClickhouseTables();
  }

  @PostMapping("/send-rule")
  @Secured({"PERM_USER"})
  public void saveSendRule(@Validated SendRuleCreationVO sendRuleCreationVO) {

    SendRuleBO sendRuleBO = new SendRuleBO();
    BeanUtils.copyProperties(sendRuleCreationVO, sendRuleBO);

    SendRuleBO result = sendRuleService.saveSendRule(sendRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);
  }

  @PutMapping("/send-rule/{id}")
  @Secured({"PERM_USER"})
  public void updateSendRule(@PathVariable @NotEmpty(message = "修改外发服务器时传入的id不能为空") String id,
      @Validated SendRuleModificationVO sendRuleModificationVO) {

    SendRuleBO sendRuleBO = new SendRuleBO();
    BeanUtils.copyProperties(sendRuleModificationVO, sendRuleBO);
    SendRuleBO result = sendRuleService.updateSendRule(sendRuleBO, id,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }

  @DeleteMapping("/send-rule/{id}")
  @Secured({"PERM_USER"})
  public void deleteSendRule(@PathVariable String id) {

    SendRuleBO result = sendRuleService.deleteSendRule(id,
        LoggedUserContext.getCurrentUser().getId(), false);
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result);
  }
}
