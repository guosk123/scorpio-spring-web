package com.machloop.fpc.manager.restapi;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.StoragePolicyBO;
import com.machloop.fpc.manager.appliance.service.StoragePolicyService;
import com.machloop.fpc.manager.appliance.vo.StoragePolicyModificationVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author guosk
 *
 * create at 2021年6月26日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class StoragePolicyRestAPIController {

  private static final String STORAGE_POLICY_ENCYRPT = "0";

  @Autowired
  private StoragePolicyService storagePolicyService;
  @Autowired
  private UserService userService;

  @Autowired
  private DictManager dictManager;

  @GetMapping("/storage-policies")
  @RestApiSecured
  public RestAPIResultVO queryStoragePolicy() {
    StoragePolicyBO storagePolicy = storagePolicyService.queryStoragePolicy();

    if (StringUtils.isBlank(storagePolicy.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("流量存储策略不存在")
          .build();
    }

    return RestAPIResultVO.resultSuccess(storagePolicyBO2Map(storagePolicy));
  }

  @PutMapping("/storage-policies")
  @RestApiSecured
  public RestAPIResultVO updateStoragePolicy(
      @RequestBody @Validated StoragePolicyModificationVO storagePolicyVO,
      BindingResult bindingResult, HttpServletRequest request) {
    StoragePolicyBO storagePolicyBO = new StoragePolicyBO();
    BeanUtils.copyProperties(storagePolicyVO, storagePolicyBO);
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, storagePolicyBO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    // 修改存储策略
    StoragePolicyBO storagePolicy = storagePolicyService.updateStoragePolicy(storagePolicyBO,
        userBO.getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, storagePolicy, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(storagePolicyBO2Map(storagePolicy));
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult,
      StoragePolicyBO storagePolicyBO) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    Map<String, String> algorithmDict = dictManager.getBaseDict()
        .getItemMap("appliance_storage_encrypt_algorithm");
    String encryptAlgorithm = storagePolicyBO.getEncryptAlgorithm();
    if (StringUtils.equals(storagePolicyBO.getEncryptAction(), STORAGE_POLICY_ENCYRPT)
        && !algorithmDict.keySet().contains(encryptAlgorithm)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的加密算法")
          .build();
    }
    storagePolicyBO.setEncryptAlgorithm(
        StringUtils.equals(storagePolicyBO.getEncryptAction(), STORAGE_POLICY_ENCYRPT)
            ? encryptAlgorithm
            : "");

    return null;
  }

  private Map<String, Object> storagePolicyBO2Map(StoragePolicyBO storagePolicy) {
    Map<String,
        Object> resultMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    resultMap.put("compressAction", storagePolicy.getCompressAction());
    resultMap.put("encryptAction", storagePolicy.getEncryptAction());
    resultMap.put("encryptAlgorithm", storagePolicy.getEncryptAlgorithm());

    return resultMap;
  }

}
