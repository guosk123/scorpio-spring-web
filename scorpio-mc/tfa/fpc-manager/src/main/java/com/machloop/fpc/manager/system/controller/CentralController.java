package com.machloop.fpc.manager.system.controller;

import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.manager.system.service.CentralService;

@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/system")
public class CentralController {

  @Autowired
  private CentralService centralService;

  @GetMapping("/cms-settings")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public Map<String, Object> queryCmsSetting() {
    return centralService.queryCmsSetting();
  }

  @PutMapping("/cms-settings")
  @Secured({"PERM_SYS_USER"})
  public void updateCmsSetting(@NotEmpty(message = "cmsIp不能为空") String cmsIp,
      @NotEmpty(message = "state不能为空") String state) {
    centralService.updateCmsSetting(cmsIp, state);

    LogHelper.auditOperate("修改集群配置：状态："
        + (StringUtils.equals(state, Constants.BOOL_YES) ? "开启" : "关闭") + ";上级CMS：" + cmsIp);
  }
}
