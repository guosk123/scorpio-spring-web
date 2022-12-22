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

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.StoragePolicyBO;
import com.machloop.fpc.manager.appliance.service.StoragePolicyService;
import com.machloop.fpc.manager.appliance.vo.StoragePolicyModificationVO;

/**
 * @author liyongjun
 *
 * create at 2019年9月5日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class StoragePolicyController {

  @Autowired
  private StoragePolicyService storagePolicyService;

  @GetMapping("/storage-policies")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryStoragePolicy() {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StoragePolicyBO storagePolicy = storagePolicyService.queryStoragePolicy();
    resultMap.put("id", storagePolicy.getId());
    resultMap.put("compressAction", storagePolicy.getCompressAction());
    resultMap.put("encryptAction", storagePolicy.getEncryptAction());
    resultMap.put("encryptAlgorithm", storagePolicy.getEncryptAlgorithm());

    return resultMap;
  }

  @PutMapping("/storage-policies")
  @Secured({"PERM_USER"})
  public void updateStoragePolicy(@Validated StoragePolicyModificationVO storagePolicyVO) {
    StoragePolicyBO storagePolicyBO = new StoragePolicyBO();
    BeanUtils.copyProperties(storagePolicyVO, storagePolicyBO);

    StoragePolicyBO storagePolicy = storagePolicyService.updateStoragePolicy(storagePolicyBO,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, storagePolicy);
  }

}
