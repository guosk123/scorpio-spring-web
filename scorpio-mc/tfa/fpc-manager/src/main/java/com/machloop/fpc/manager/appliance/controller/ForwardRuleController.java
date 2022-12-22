package com.machloop.fpc.manager.appliance.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.ForwardRuleBO;
import com.machloop.fpc.manager.appliance.service.ForwardRuleService;
import com.machloop.fpc.manager.appliance.vo.ForwardRuleCreationVO;
import com.machloop.fpc.manager.appliance.vo.ForwardRuleModificationVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;

/**
 * @author ChenXiao
 *
 * create at 2022/5/7 17:14,IntelliJ IDEA
 *
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class ForwardRuleController {

  @Autowired
  private ForwardRuleService forwardRuleService;

  @GetMapping("/forward-rules")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryForwardRules(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {
    Sort sort = new Sort(new Sort.Order(Sort.Direction.ASC, "create_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<ForwardRuleBO> forwardRules = forwardRuleService.queryForwardRules(page);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(forwardRules.getSize());
    forwardRules.forEach(forwardRule -> {
      Map<String, Object> forwardRuleMap = forwardRuleBO2Map(forwardRule, false);
      forwardRuleMap.put("referenceCount", forwardRule.getReferenceCount());
      result.add(forwardRuleMap);
    });

    return new PageImpl<>(result, page, forwardRules.getTotalElements());
  }

  @GetMapping("/forward-rules/as-list")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryForwardRules() {
    List<ForwardRuleBO> forwardRules = forwardRuleService.queryForwardRules();

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(forwardRules.size());
    forwardRules.forEach(forwardRule -> {
      result.add(forwardRuleBO2Map(forwardRule, false));
    });

    return result;
  }

  @GetMapping("/forward-rules/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryForwardRule(@PathVariable String id) {
    ForwardRuleBO forwardRule= forwardRuleService.queryForwardRule(id);
    return forwardRuleBO2Map(forwardRule, true);
  }

  @PostMapping("/forward-rules")
  @Secured({"PERM_USER"})
  public void saveForwardRule(@Validated ForwardRuleCreationVO forwardRuleVO) {
    ForwardRuleBO forwardRuleBO = new ForwardRuleBO();
    BeanUtils.copyProperties(forwardRuleVO, forwardRuleBO);

    ForwardRuleBO result = forwardRuleService.saveForwardRule(forwardRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);
  }

  @PutMapping("/forward-rules/{id}")
  @Secured({"PERM_USER"})
  public void updateForwardRule(@PathVariable String id,
      @Validated ForwardRuleModificationVO forwardRuleVO) {
    ForwardRuleBO forwardRuleBO = new ForwardRuleBO();
    BeanUtils.copyProperties(forwardRuleVO, forwardRuleBO);

    ForwardRuleBO result = forwardRuleService.updateForwardRule(id, forwardRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }

  @DeleteMapping("/forward-rules/{id}")
  @Secured({"PERM_USER"})
  public void deleteForwardRule(@PathVariable @NotEmpty(message = "删除规则时传入的id不能为空") String id) {
    ForwardRuleBO result = forwardRuleService.deleteForwardRule(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, result);
  }

  private Map<String, Object> forwardRuleBO2Map(ForwardRuleBO forwardRuleBO, boolean isDetail) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", forwardRuleBO.getId());
    map.put("name", forwardRuleBO.getName());
    map.put("description", forwardRuleBO.getDescription());
    if (isDetail) {
      map.put("defaultAction", forwardRuleBO.getDefaultAction());
      map.put("defaultActionText", forwardRuleBO.getDefaultActionText());
      map.put("exceptBpf", forwardRuleBO.getExceptBpf());
      map.put("exceptTuple", forwardRuleBO.getExceptTuple());
      map.put("createTime", forwardRuleBO.getCreateTime());
      map.put("updateTime", forwardRuleBO.getUpdateTime());
    }
    return map;

  }


}
