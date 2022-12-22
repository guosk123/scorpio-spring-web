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
import com.machloop.fpc.manager.appliance.bo.MailRuleBO;
import com.machloop.fpc.manager.appliance.service.MailRuleService;
import com.machloop.fpc.manager.appliance.vo.MailRuleCreationVO;
import com.machloop.fpc.manager.appliance.vo.MailRuleModificationVO;
import com.machloop.fpc.manager.appliance.vo.MailRuleQueryVO;

/**
 * @author minjiajun
 *
 * create at 2022年10月27日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class MailRuleController {

  @Autowired
  private MailRuleService mailRuleService;

  @GetMapping("/mail-rules")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryMailRules(MailRuleQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {

    Sort sort = new Sort(Sort.Direction.DESC, "create_time");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<MailRuleBO> mailRulePage = mailRuleService.queryMailRules(queryVO, page);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(mailRulePage.getSize());
    for (MailRuleBO mailRule : mailRulePage) {
      resultList.add(mailRuleBO2Map(mailRule));
    }

    return new PageImpl<>(resultList, page, mailRulePage.getTotalElements());
  }

  @GetMapping("/mail-rule/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryMailRule(@PathVariable String id) {
    return mailRuleBO2Map(mailRuleService.queryMailRule(id));
  }

  @PutMapping("/mail-rule/{id}")
  @Secured({"PERM_USER"})
  public void saveMailRule(@Validated MailRuleModificationVO modificationVO,
      @PathVariable @NotEmpty(message = "修改邮件规则时传入的id不能为空") String id) {
    MailRuleBO mailRuleBO = new MailRuleBO();
    BeanUtils.copyProperties(modificationVO, mailRuleBO);

    MailRuleBO mailRule = mailRuleService.updateMailRule(id, mailRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, mailRule);
  }

  @PostMapping("/mail-rule")
  @Secured({"PERM_USER"})
  public void saveMailRule(@Validated MailRuleCreationVO creationVO) {
    MailRuleBO mailRuleBO = new MailRuleBO();
    BeanUtils.copyProperties(creationVO, mailRuleBO);

    MailRuleBO mailRule = mailRuleService.saveMailRule(mailRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, mailRule);
  }

  @DeleteMapping("/mail-rule/{id}")
  @Secured({"PERM_USER"})
  public void deleteMailRule(@PathVariable @NotEmpty(message = "删除邮件规则时传入的id不能为空") String id) {

    MailRuleBO mailRule = mailRuleService.deleteMailRule(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, mailRule);
  }

  @PutMapping("/mail-rule/{id}/state")
  @Secured({"PERM_USER"})
  public void updateMailRuleState(@PathVariable @NotEmpty(message = "更新邮件规则状态时传入的id不能为空") String id,
      @RequestParam String state) {
    MailRuleBO mailRuleBO = mailRuleService.updateMailRuleState(id, state,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, mailRuleBO);
  }

  private static Map<String, Object> mailRuleBO2Map(MailRuleBO mailRule) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", mailRule.getId());
    map.put("mailAddress", mailRule.getMailAddress());
    map.put("countryId", mailRule.getCountryId());
    map.put("provinceId", mailRule.getProvinceId());
    map.put("cityId", mailRule.getCityId());
    map.put("startTime", mailRule.getStartTime());
    map.put("endTime", mailRule.getEndTime());
    map.put("action", mailRule.getAction());
    map.put("period", mailRule.getPeriod());
    map.put("state", mailRule.getState());

    return map;
  }
}
