package com.machloop.fpc.manager.metadata.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.metadata.service.CollectPolicyService;
import com.machloop.fpc.manager.metadata.vo.CollectPolicyVO;

@RestController
@RequestMapping("/webapi/fpc-v1/metadata")
public class CollectPolicyController {

  @Autowired
  private CollectPolicyService collectPolicyService;

  @GetMapping("/collect-policys")
  @Secured({"PERM_USER"})
  public List<CollectPolicyVO> queryCollectPolicys() {
    return collectPolicyService.queryCollectPolicys();
  }

  @GetMapping("/collect-policys/{id}")
  @Secured({"PERM_USER"})
  public CollectPolicyVO queryCollectPolicy(@PathVariable String id) {
    return collectPolicyService.queryCollectPolicy(id);
  }

  @PostMapping("/collect-policys")
  @Secured({"PERM_USER"})
  public void saveCollectPolicy(CollectPolicyVO collectPolicyVO) {

    collectPolicyVO.setOperatorId(LoggedUserContext.getCurrentUser().getId());
    collectPolicyVO = collectPolicyService.saveCollectPolicy(collectPolicyVO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, collectPolicyVO);
  }

  @PutMapping("/collect-policys/{id}")
  @Secured({"PERM_USER"})
  public void updateCollectPolicy(@PathVariable String id, CollectPolicyVO collectPolicyVO) {

    collectPolicyVO.setId(id);
    collectPolicyVO.setOperatorId(LoggedUserContext.getCurrentUser().getId());
    collectPolicyVO = collectPolicyService.updateCollectPolicy(collectPolicyVO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, collectPolicyVO);
  }

  @PutMapping("/collect-policys/{id}/state")
  @Secured({"PERM_USER"})
  public void changeCollectPolicyState(@PathVariable String id, String state) {

    CollectPolicyVO collectPolicyVO = collectPolicyService.changeCollectPolicyState(id, state,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, collectPolicyVO);
  }

  @DeleteMapping("/collect-policys/{id}")
  @Secured({"PERM_USER"})
  public void deleteCollectPolicy(@PathVariable String id) {

    CollectPolicyVO collectPolicyVO = collectPolicyService.deleteCollectPolicy(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, collectPolicyVO);
  }

}
