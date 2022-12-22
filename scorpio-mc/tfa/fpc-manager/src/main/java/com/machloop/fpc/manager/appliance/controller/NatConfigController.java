package com.machloop.fpc.manager.appliance.controller;

import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.NatConfigBO;
import com.machloop.fpc.manager.appliance.service.NatConfigService;
import com.machloop.fpc.manager.appliance.vo.NatConfigModificationVO;

/**
 * @author ChenXiao
 * create at 2022/11/9
 */
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class NatConfigController {

  @Autowired
  private NatConfigService natConfigService;


  @GetMapping("/nat-config")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryNatConfig() {

    return natConfigService.queryNatConfig();
  }

  @PutMapping("/nat-config")
  @Secured({"PERM_USER"})
  public void updateNatConfig(@Validated NatConfigModificationVO natConfigVO) {
    NatConfigBO natConfigBO = new NatConfigBO();
    BeanUtils.copyProperties(natConfigVO, natConfigBO);

    NatConfigBO natConfig = natConfigService.updateNatConfig(natConfigBO,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, natConfig);
  }

}
