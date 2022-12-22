package com.machloop.fpc.cms.center.appliance.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.appliance.bo.FilterRuleBO;
import com.machloop.fpc.cms.center.appliance.service.FilterRuleService;
import com.machloop.fpc.cms.center.appliance.vo.FilterRuleCreationVO;

/**
 * @author chenshimiao
 *
 * create at 2022/8/18 10:50 AM,cms
 * @version 1.0
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class FilterRuleController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FilterRuleController.class);

  @Value("${file.filterRule.template.path}")
  private String filterRuleTemplatePath;

  @Autowired
  private FilterRuleService filterRuleService;

  @GetMapping("/filter-rules")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryFilterRules(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {
    Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "priority"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<FilterRuleBO> filterRuleBOS = filterRuleService.queryFilterRules(page);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(filterRuleBOS.getSize());
    filterRuleBOS.forEach(filterRuleBO -> {
      Map<String, Object> filterRuleBOTOMap = filterRuleBOTOMap(filterRuleBO);
      result.add(filterRuleBOTOMap);
    });

    return new PageImpl<>(result, page, filterRuleBOS.getTotalElements());
  }

  @GetMapping("/filter-rules/as-list")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryFilterRules() {
    List<FilterRuleBO> filterRuleBOS = filterRuleService.queryFilterRule();

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    filterRuleBOS.forEach(filterRuleBO -> {
      result.add(filterRuleBOTOMap(filterRuleBO));
    });

    return result;
  }

  @GetMapping("/filter-rules/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryFilterRule(@PathVariable String id) {
    FilterRuleBO filterRuleBO = filterRuleService.queryFilterRule(id);

    return filterRuleBOTOMap(filterRuleBO);
  }

  @GetMapping("/filter-rules/as-export")
  @Secured({"PERM_USER"})
  public void exportLogRecords(HttpServletRequest request, HttpServletResponse response) {

    String agent = request.getHeader("User-Agent");
    String fileName = "filter_rules_log.csv";
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, fileName));
    response.resetBuffer();

    try (OutputStream outputStream = response.getOutputStream()) {
      List<String> lineList = filterRuleService.exportFilterRules();
      outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

      for (String line : lineList) {
        outputStream.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export filterRules logs error ", e);
    }
  }

  @GetMapping("/filter-rules/as-template")
  @Secured({"PERM_USER"})
  public void downloadFilterRuleTemplate(HttpServletRequest request, HttpServletResponse response) {
    File filterRuleTemplateFile = new File(filterRuleTemplatePath);

    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "存储过滤规则导入模版.csv"));
    response.resetBuffer();

    try (ServletOutputStream outputStream = response.getOutputStream();
        FileInputStream in = FileUtils.openInputStream(filterRuleTemplateFile)) {
      int len = 0;
      byte[] buffer = new byte[1024];
      outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      while ((len = in.read(buffer)) > 0) {
        outputStream.write(buffer, 0, len);
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.info("export filterRule template error ", e);
    }
  }

  @PostMapping("/filter-rules/as-import")
  @Secured({"PERM_USER"})
  public void importFilterRule(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符：[%s]", matchingIllegalCharacters));
    }

    filterRuleService.importFilterRule(file, LoggedUserContext.getCurrentUser().getId());
  }

  @PutMapping("/filter-rules/change/priority")
  @Secured({"PERM_USER"})
  public void exchangePriority(@RequestParam @NotEmpty(message = "改变优先级id不能为空") String idList,
      @RequestParam Integer page, @RequestParam Integer pageSize,
      @RequestParam(required = false, defaultValue = "") String operator) {

    List<String> priorityIdList = CsvUtils.convertCSVToList(idList);
    List<FilterRuleBO> filterRuleBOS = filterRuleService.updateFilterRulePriority(priorityIdList,
        page, pageSize, operator, LoggedUserContext.getCurrentUser().getId());
    for (FilterRuleBO filterRuleBO : filterRuleBOS) {
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, filterRuleBO);
    }
  }

  @PutMapping("/filter-rules/change/state")
  @Secured({"PERM_USER"})
  public void changeState(@RequestParam @NotEmpty(message = "更改状态Id不能为空") String idList,
      @RequestParam @NotEmpty(message = "状态码不能为空") String state) {

    List<String> stateIdList = CsvUtils.convertCSVToList(idList);
    List<FilterRuleBO> filterRuleBOS = filterRuleService.updateFilterRuleState(stateIdList, state,
        LoggedUserContext.getCurrentUser().getId());
    for (FilterRuleBO filterRuleBO : filterRuleBOS) {
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, filterRuleBO);
    }
  }

  @PostMapping("/filter-rules")
  @Secured({"PERM_USER"})
  public void saveFilterRule(@Validated FilterRuleCreationVO filterRuleVO,
      @RequestParam(defaultValue = "") String before) {
    FilterRuleBO filterRuleBO = new FilterRuleBO();
    filterRuleBO.setNetworkId(CsvUtils.convertCSVToList(filterRuleVO.getNetworkId()));
    filterRuleBO.setNetworkGroupId(CsvUtils.convertCSVToList(filterRuleVO.getNetworkGroupId()));
    BeanUtils.copyProperties(filterRuleVO, filterRuleBO);

    FilterRuleBO result = filterRuleService.saveFilterRule(filterRuleBO, before,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, result);
  }

  @PutMapping("/filter-rules/{id}")
  @Secured({"PERM_USER"})
  public void updateFilterRule(@PathVariable @NotEmpty(message = "传入的id不能为空") String id,
      @Validated FilterRuleCreationVO filterRuleVO) {
    FilterRuleBO filterRuleBO = new FilterRuleBO();
    filterRuleBO.setNetworkId(CsvUtils.convertCSVToList(filterRuleVO.getNetworkId()));
    filterRuleBO.setNetworkGroupId(CsvUtils.convertCSVToList(filterRuleVO.getNetworkGroupId()));
    BeanUtils.copyProperties(filterRuleVO, filterRuleBO);

    FilterRuleBO result = filterRuleService.updateFilterRule(id, filterRuleBO,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, result);
  }

  @DeleteMapping("/filter-rules/deleted/batch")
  @Secured({"PERM_USER"})
  public void deleteFilterRuleById(@RequestParam @NotEmpty(message = "删除过滤id不能为空") String idList) {

    List<String> deletedIdList = CsvUtils.convertCSVToList(idList);
    List<FilterRuleBO> filterRuleBOS = filterRuleService.deleteFilterRule(deletedIdList,
        LoggedUserContext.getCurrentUser().getId(), false);
    for (FilterRuleBO filterRuleBO : filterRuleBOS) {
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, filterRuleBO);
    }
  }

  private Map<String, Object> filterRuleBOTOMap(FilterRuleBO filterRuleBO) {

    HashMap<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", filterRuleBO.getId());
    map.put("name", filterRuleBO.getName());
    map.put("description", filterRuleBO.getDescription());
    map.put("tuple", filterRuleBO.getTuple());
    map.put("state", filterRuleBO.getState());
    map.put("priority", filterRuleBO.getPriority());
    map.put("networkId", StringUtils.join(filterRuleBO.getNetworkId(), ","));
    map.put("networkGroupId", StringUtils.join(filterRuleBO.getNetworkGroupId(), ","));
    map.put("createTime", filterRuleBO.getCreateTime());
    map.put("updateTime", filterRuleBO.getUpdateTime());
    map.put("deletedTime", filterRuleBO.getDeletedTime());
    return map;
  }
}
