package com.machloop.fpc.npm.appliance.controller;

import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.vo.LogicalSubnetCreationVO;
import com.machloop.fpc.npm.appliance.vo.LogicalSubnetModificationVO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class LogicalSubnetController {

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @GetMapping("/logical-subnets")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryLogicalSubnets() {
    List<LogicalSubnetBO> logicalSubnets = logicalSubnetService.queryLogicalSubnets();

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(logicalSubnets.size());
    for (LogicalSubnetBO logicalSubnetBO : logicalSubnets) {
      resultList.add(logicalSubnetBO2Map(logicalSubnetBO));
    }

    return resultList;
  }

  @GetMapping("/logical-subnets/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryLogicalSubnet(
      @NotEmpty(message = "子网ID不能为空") @PathVariable String id) {
    LogicalSubnetBO logicalSubnet = logicalSubnetService.queryLogicalSubnet(id);

    return logicalSubnetBO2Map(logicalSubnet);
  }

  @PostMapping("/logical-subnets")
  @Secured({"PERM_USER"})
  public void saveLogicalSubnet(@Validated LogicalSubnetCreationVO logicalSubnetCreationVO) {
    LogicalSubnetBO logicalSubnetBO = new LogicalSubnetBO();
    BeanUtils.copyProperties(logicalSubnetCreationVO, logicalSubnetBO);

    logicalSubnetBO = logicalSubnetService.saveLogicalSubnet(logicalSubnetBO, null,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, logicalSubnetBO);
  }

  @PutMapping("/logical-subnets/{id}")
  @Secured({"PERM_USER"})
  public void updateLogicalSubnet(@PathVariable @NotEmpty(message = "修改子网时传入的id不能为空") String id,
      @Validated LogicalSubnetModificationVO logicalSubnetModificationVO) {
    LogicalSubnetBO logicalSubnetBO = new LogicalSubnetBO();
    BeanUtils.copyProperties(logicalSubnetModificationVO, logicalSubnetBO);

    logicalSubnetBO = logicalSubnetService.updateLogicalSubnet(id, logicalSubnetBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, logicalSubnetBO);
  }

  @DeleteMapping("/logical-subnets/{id}")
  @Secured({"PERM_USER"})
  public void deleteLogicalSubnet(@PathVariable @NotEmpty(message = "删除子网时传入的id不能为空") String id) {
    LogicalSubnetBO logicalSubnetBO = logicalSubnetService.deleteLogicalSubnet(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, logicalSubnetBO);
  }

  private static Map<String, Object> logicalSubnetBO2Map(LogicalSubnetBO logicalSubnetBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", logicalSubnetBO.getId());
    map.put("name", logicalSubnetBO.getName());
    map.put("networkId", logicalSubnetBO.getNetworkId());
    map.put("networkName", logicalSubnetBO.getNetworkName());
    map.put("bandwidth", logicalSubnetBO.getBandwidth());
    map.put("type", logicalSubnetBO.getType());
    map.put("typeText", logicalSubnetBO.getTypeText());
    map.put("configuration", logicalSubnetBO.getConfiguration());
    map.put("description", logicalSubnetBO.getDescription());
    map.put("createTime", logicalSubnetBO.getCreateTime());

    return map;
  }

}
