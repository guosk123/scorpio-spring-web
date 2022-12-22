package com.machloop.fpc.manager.appliance.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.ExternalStorageBO;
import com.machloop.fpc.manager.appliance.service.ExternalStorageService;
import com.machloop.fpc.manager.appliance.vo.ExternalStorageCreationVO;
import com.machloop.fpc.manager.appliance.vo.ExternalStorageModificationVO;

/**
 * @author guosk
 *
 * create at 2020年11月13日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class ExternalStorageController {

  @Autowired
  private ExternalStorageService externalStorageService;

  @GetMapping("/external-storages")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryExternalStorages(
      @RequestParam(required = false) String usage, @RequestParam(required = false) String type) {
    List<ExternalStorageBO> externalStorages = externalStorageService.queryExternalStorages(usage,
        type);

    return externalStorages.stream().map(item -> externalStorage2Map(item))
        .collect(Collectors.toList());
  }

  @GetMapping("/external-storages/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryExternalStorage(@PathVariable String id) {
    ExternalStorageBO externalStorageBO = externalStorageService.queryExternalStorage(id);

    return externalStorage2Map(externalStorageBO);
  }

  @PostMapping("/external-storages")
  @Secured({"PERM_USER"})
  public void saveExternalStorage(@Validated ExternalStorageCreationVO externalStorageVO) {
    ExternalStorageBO externalStorageBO = new ExternalStorageBO();
    BeanUtils.copyProperties(externalStorageVO, externalStorageBO);

    ExternalStorageBO externalStorage = externalStorageService
        .saveExternalStorage(externalStorageBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, externalStorage);
  }

  @PutMapping("/external-storages/{id}")
  @Secured({"PERM_USER"})
  public void updateExternalStorage(@PathVariable @NotEmpty(message = "修改存储服务时传入的id不能为空") String id,
      @Validated ExternalStorageModificationVO externalStorageVO) {
    ExternalStorageBO externalStorageBO = new ExternalStorageBO();
    BeanUtils.copyProperties(externalStorageVO, externalStorageBO);

    externalStorageService.updateExternalStorage(id, externalStorageBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, externalStorageBO);
  }

  @DeleteMapping("/external-storages/{id}")
  @Secured({"PERM_USER"})
  public void deleteExternalStorage(
      @PathVariable @NotEmpty(message = "删除存储服务时传入的id不能为空") String id) {
    ExternalStorageBO externalStorageBO = externalStorageService.deleteExternalStorage(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, externalStorageBO);
  }

  private Map<String, Object> externalStorage2Map(ExternalStorageBO externalStorageBO) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    resultMap.put("id", externalStorageBO.getId());
    resultMap.put("name", externalStorageBO.getName());
    resultMap.put("state", externalStorageBO.getState());
    resultMap.put("usage", externalStorageBO.getUsage());
    resultMap.put("type", externalStorageBO.getType());
    resultMap.put("ipAddress", externalStorageBO.getIpAddress());
    resultMap.put("port", externalStorageBO.getPort());
    resultMap.put("username", externalStorageBO.getUsername());
    resultMap.put("directory", externalStorageBO.getDirectory());
    resultMap.put("capacity", externalStorageBO.getCapacity());
    resultMap.put("description", externalStorageBO.getDescription());
    resultMap.put("updateTime", externalStorageBO.getUpdateTime());

    return resultMap;
  }

}
