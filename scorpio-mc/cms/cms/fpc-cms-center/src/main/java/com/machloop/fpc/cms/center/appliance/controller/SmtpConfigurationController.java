package com.machloop.fpc.cms.center.appliance.controller;

import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.appliance.bo.SmtpConfigurationBO;
import com.machloop.fpc.cms.center.appliance.service.SmtpConfigurationService;
import com.machloop.fpc.cms.center.appliance.vo.SmtpConfigurationModificationVO;
import com.machloop.fpc.cms.center.appliance.vo.SmtpConfigurationVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年11月11日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class SmtpConfigurationController {

  @Autowired
  SmtpConfigurationService smtpConfigurationService;

  @GetMapping("/smtp-configuration")
  @Secured("PERM_USER")
  public Map<String, Object> querySmtpConfigurations() {

    SmtpConfigurationBO smtpConfigurationBO = smtpConfigurationService.querySmtpConfigurations();
    Map<String, Object> result = smtpConfigurationBO2Map(smtpConfigurationBO);

    return result;
  }

  @PutMapping("/smtp-configuration")
  @Secured("PERM_USER")
  public void updateSmtpConfiguration(
      @Validated SmtpConfigurationModificationVO smptConfigurationModificationVO) {

    SmtpConfigurationBO smtpConfigurationBO = new SmtpConfigurationBO();
    BeanUtils.copyProperties(smptConfigurationModificationVO, smtpConfigurationBO);

    smtpConfigurationService.updateSmtpConfiguration(smtpConfigurationBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, smtpConfigurationBO);
  }

  // SMTP测试接口
  @GetMapping("/smtp-test-connection")
  public void testConnection(@Validated SmtpConfigurationVO smtpConfigurationVO){ 
    
    SmtpConfigurationBO smtpConfigurationBO = new SmtpConfigurationBO();
    BeanUtils.copyProperties(smtpConfigurationVO, smtpConfigurationBO);
    
    smtpConfigurationService.smtpTestConnection(smtpConfigurationBO);    
  }
  
  private Map<String, Object> smtpConfigurationBO2Map(SmtpConfigurationBO smtpConfigurationBO){
    
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    resultMap.put("id", smtpConfigurationBO.getId());
    resultMap.put("mailUsername", smtpConfigurationBO.getMailUsername());
    resultMap.put("mailAddress", smtpConfigurationBO.getMailAddress());
    resultMap.put("smtpServer", smtpConfigurationBO.getSmtpServer());
    resultMap.put("serverPort", smtpConfigurationBO.getServerPort());
    resultMap.put("encrypt", smtpConfigurationBO.getEncrypt());
    resultMap.put("loginUser", smtpConfigurationBO.getLoginUser());
    resultMap.put("loginPassword", smtpConfigurationBO.getLoginPassword());
    return resultMap;
  }
}
