package com.machloop.fpc.manager.system.controller;

import java.util.Map;

import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.manager.system.service.SystemUpgradeService;

/**
 * @author guosk
 *
 * create at 2021年1月14日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class SystemUpgradeController {

  @Autowired
  private SystemUpgradeService systemUpgradeService;

  @GetMapping("/upgrade/infos")
  @Secured({"PERM_SYS_USER"})
  public Map<String, String> queryCurrentUpgradeVersion() {

    return systemUpgradeService.queryCurrentUpgradeVersion();
  }

  @GetMapping("/upgrade/logs")
  @Secured({"PERM_SYS_USER"})
  public Map<String, Object> queryUpgradeLogs(
      @RequestParam(defaultValue = "0") @Min(value = 0, message = "游标最小为0") long cursor) {

    return systemUpgradeService.queryUpgradeLogs(cursor);
  }

  @PostMapping("/upgrade")
  @Secured({"PERM_SYS_USER"})
  public void systemUpgrade(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
    }

    systemUpgradeService.systemUpgrade(file);

    LogHelper.auditOperate("执行系统升级。");
  }

}
