package com.machloop.fpc.manager.appliance.controller;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.ForwardPolicyBO;
import com.machloop.fpc.manager.appliance.service.ForwardPolicyService;
import com.machloop.fpc.manager.appliance.vo.ForwardPolicyCreationVO;
import com.machloop.fpc.manager.appliance.vo.ForwardPolicyModificationVO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/11 23:01,IntelliJ IDEA
 *
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class ForwardPolicyController {

  @Autowired
  private ForwardPolicyService forwardPolicyService;

  @GetMapping("/forward-policies")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryForwardPolicies(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {
    Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "create_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<ForwardPolicyBO> forwardPolicies = forwardPolicyService.queryForwardPolicies(page);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(forwardPolicies.getSize());
    forwardPolicies.forEach(forwardPolicy -> {
      Map<String, Object> forwardPolicyMap = forwardPolicyBO2Map(forwardPolicy, true);
      forwardPolicyMap.put("networkId", forwardPolicy.getNetworkId());
      forwardPolicyMap.put("totalBandWidth",
          Integer.parseInt(forwardPolicy.getState()) == 1 ? forwardPolicy.getTotalBandWidth() : 0);
      forwardPolicyMap.put("state", forwardPolicy.getState());
      result.add(forwardPolicyMap);
    });

    return new PageImpl<>(result, page, forwardPolicies.getTotalElements());
  }

  @GetMapping("/forward-policies/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryForwardPolicy(@PathVariable String id) {
    ForwardPolicyBO forwardPolicy = forwardPolicyService.queryForwardPolicy(id);

    Map<String, Object> forwardPolicyMap = forwardPolicyBO2Map(forwardPolicy, true);
    forwardPolicyMap.put("networkId", forwardPolicy.getNetworkId());
    forwardPolicyMap.put("totalBandWidth",
        Integer.parseInt(forwardPolicy.getState()) == 1 ? forwardPolicy.getTotalBandWidth() : 0);
    forwardPolicyMap.put("state", forwardPolicy.getState());
    return forwardPolicyMap;
  }

  @PostMapping("/forward-policies")
  @Secured({"PERM_USER"})
  public void saveForwardPolicy(@Validated ForwardPolicyCreationVO forwardPolicyVO) {
    ForwardPolicyBO forwardPolicyBO = new ForwardPolicyBO();
    BeanUtils.copyProperties(forwardPolicyVO, forwardPolicyBO);

    ForwardPolicyBO result = forwardPolicyService.saveForwardPolicy(forwardPolicyBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);
  }

  @PutMapping("/forward-policies/{id}")
  @Secured({"PERM_USER"})
  public void updateForwardPolicy(@PathVariable String id,
      @Validated ForwardPolicyModificationVO forwardPolicyVO) {
    ForwardPolicyBO forwardPolicyBO = new ForwardPolicyBO();
    BeanUtils.copyProperties(forwardPolicyVO, forwardPolicyBO);

    ForwardPolicyBO result = forwardPolicyService.updateForwardPolicy(id, forwardPolicyBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }

  @DeleteMapping("/forward-policies/{id}")
  @Secured({"PERM_USER"})
  public void deleteForwardPolicy(@PathVariable @NotEmpty(message = "删除规则时传入的id不能为空") String id) {
    ForwardPolicyBO result = forwardPolicyService.deleteForwardPolicy(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result);
  }

  @PutMapping("/forward-policies/{id}/{state}")
  @Secured({"PERM_USER"})
  public void changeForwardPolicy(@PathVariable @NotEmpty(message = "删除规则时传入的id不能为空") String id,
      @RequestParam(name = "state", required = false, defaultValue = "1") String state) {
    ForwardPolicyBO result = forwardPolicyService.changeForwardPolicy(id, state,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }

  private Map<String, Object> forwardPolicyBO2Map(ForwardPolicyBO forwardPolicyBO,
      boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", forwardPolicyBO.getId());
    map.put("name", forwardPolicyBO.getName());
    map.put("ruleId", forwardPolicyBO.getRuleId());
    map.put("netifName", forwardPolicyBO.getNetifName());
    map.put("ipTunnel", forwardPolicyBO.getIpTunnel());
    map.put("loadBalance", forwardPolicyBO.getLoadBalance());
    map.put("description", forwardPolicyBO.getDescription());
    if (isDetail) {
      map.put("createTime", forwardPolicyBO.getCreateTime());
      map.put("updateTime", forwardPolicyBO.getUpdateTime());
      map.put("metricTime", forwardPolicyBO.getMetricTime());
    }
    return map;
  }


}
