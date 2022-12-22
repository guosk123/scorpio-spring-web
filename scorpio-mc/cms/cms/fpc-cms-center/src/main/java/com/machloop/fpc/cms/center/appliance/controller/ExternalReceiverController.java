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
import com.machloop.fpc.cms.center.appliance.bo.ExternalReceiverBO;
import com.machloop.fpc.cms.center.appliance.service.ExternalReceiverService;
import com.machloop.fpc.cms.center.appliance.vo.ExternalReceiverCreationVO;
import com.machloop.fpc.cms.center.appliance.vo.ExternalReceiverModificationVO;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class ExternalReceiverController {


  @Autowired
  private ExternalReceiverService externalReceiverService;

  @GetMapping("/external-receiver")
  @Secured({"PERM_USER"})
  public Map<String, List<Map<String, Object>>> queryExternalReceiver() {

    return externalReceiverService.queryExternalReceivers();
  }

  @GetMapping("/external-receiver/type")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryExternalReceiversByType(@RequestParam String receiverType) {

    return externalReceiverService.queryExternalReceiversByType(receiverType);
  }

  @GetMapping("/external-receiver/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryExternalReceiver(@PathVariable String id) {

    return externalReceiverService.queryExternalReceiver(id);
  }

  @PostMapping("/external-receiver")
  @Secured({"PERM_USER"})
  public void saveExternalReceiver(
      @Validated ExternalReceiverCreationVO externalReceiverCreationVO) {

    ExternalReceiverBO externalReceiverBO = new ExternalReceiverBO();
    BeanUtils.copyProperties(externalReceiverCreationVO, externalReceiverBO);
    ExternalReceiverBO result = externalReceiverService.saveExternalReceiver(externalReceiverBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);
  }

  @PutMapping("/external-receiver/{id}")
  @Secured({"PERM_USER"})
  public void updateExternalReceiver(
      @PathVariable @NotEmpty(message = "修改外发服务器时传入的id不能为空") String id,
      @Validated ExternalReceiverModificationVO externalReceiverModificationVO) {

    ExternalReceiverBO externalReceiverBO = new ExternalReceiverBO();
    BeanUtils.copyProperties(externalReceiverModificationVO, externalReceiverBO);
    ExternalReceiverBO result = externalReceiverService.updateExternalReceiver(externalReceiverBO,
        id, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }

  @DeleteMapping("/external-receiver/{id}")
  @Secured({"PERM_USER"})
  public void deleteExternalReceiver(@PathVariable String id) {

    ExternalReceiverBO result = externalReceiverService.deleteExternalReceiver(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result);
  }

}
