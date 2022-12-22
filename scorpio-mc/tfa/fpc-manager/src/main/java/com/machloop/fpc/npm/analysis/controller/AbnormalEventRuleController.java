package com.machloop.fpc.npm.analysis.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.npm.analysis.bo.AbnormalEventRuleBO;
import com.machloop.fpc.npm.analysis.service.AbnormalEventRuleService;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleCreationVO;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleModificationVO;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月16日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/analysis")
public class AbnormalEventRuleController {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbnormalEventRuleController.class);

  @Value("${file.abnormal.event.template.path}")
  private String abnormalEventTemplatePath;

  @Autowired
  private AbnormalEventRuleService abnormalEventRuleService;

  @GetMapping("/abnormal-event-rules")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryAbnormalEventRules(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      AbnormalEventRuleQueryVO queryVO) {
    Sort sort = new Sort(new Order(Sort.Direction.DESC, "timestamp"),
        new Order(Sort.Direction.ASC, "type"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<AbnormalEventRuleBO> abnormalEventRulesPage = abnormalEventRuleService
        .queryAbnormalEventRules(page, queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(abnormalEventRulesPage.getSize());
    for (AbnormalEventRuleBO abnormalEventRule : abnormalEventRulesPage) {
      resultList.add(abnormalEventRuleBO2Map(abnormalEventRule));
    }

    return new PageImpl<>(resultList, page, abnormalEventRulesPage.getTotalElements());
  }

  @GetMapping("/abnormal-event-rules/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryAbnormalEventRule(@PathVariable String id) {
    return abnormalEventRuleBO2Map(abnormalEventRuleService.queryAbnormalEventRule(id));
  }

  @GetMapping("/abnormal-event-rules/as-export")
  @Secured({"PERM_USER"})
  public void exportAbnormalEventRules(HttpServletRequest request, HttpServletResponse response,
      @RequestParam(
          required = false,
          defaultValue = FpcConstants.ANALYSIS_ABNORMAL_EVENT_SOURCE_CUSTOM) String source) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "abnormalEvent.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      List<String> lineList = abnormalEventRuleService.exportAbnormalEventRules(source);

      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : lineList) {
        out.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export threat abnormal event error ", e);
    }
  }

  @GetMapping("/abnormal-event-rules/as-template")
  @Secured({"PERM_USER"})
  public void downloadIntelligenceTemplate(HttpServletRequest request,
      HttpServletResponse response) {
    File templateFile = new File(abnormalEventTemplatePath);

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "异常事件导入模板.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();
        FileInputStream in = FileUtils.openInputStream(templateFile)) {
      int len = 0;
      byte[] buffer = new byte[1024];
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export threat intelligences template error ", e);
    }
  }

  @PostMapping("/abnormal-event-rules/as-import")
  @Secured({"PERM_USER"})
  public void importAbnormalEventRules(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
    }

    abnormalEventRuleService.importAbnormalEventRules(file,
        LoggedUserContext.getCurrentUser().getId());
  }

  @PostMapping("/abnormal-event-rules")
  @Secured({"PERM_USER"})
  public void saveAbnormalEventRule(
      @Validated AbnormalEventRuleCreationVO abnormalEventRuleCreationVO) {
    AbnormalEventRuleBO abnormalEventRuleBO = new AbnormalEventRuleBO();
    BeanUtils.copyProperties(abnormalEventRuleCreationVO, abnormalEventRuleBO);

    AbnormalEventRuleBO abnormalEventRule = abnormalEventRuleService
        .saveAbnormalEventRule(abnormalEventRuleBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, abnormalEventRule);
  }

  @PutMapping("/abnormal-event-rules/{id}")
  @Secured({"PERM_USER"})
  public void updateAbnormalEventRule(@PathVariable String id,
      @Validated AbnormalEventRuleModificationVO abnormalEventRuleModificationVO) {
    AbnormalEventRuleBO abnormalEventRuleBO = new AbnormalEventRuleBO();
    BeanUtils.copyProperties(abnormalEventRuleModificationVO, abnormalEventRuleBO);

    AbnormalEventRuleBO abnormalEventRule = abnormalEventRuleService.updateAbnormalEventRule(id,
        abnormalEventRuleBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, abnormalEventRule);
  }

  @PutMapping("/abnormal-event-rules/{id}/status")
  @Secured({"PERM_USER"})
  public void updateStatus(@PathVariable String id, String status) {
    AbnormalEventRuleBO abnormalEventRule = abnormalEventRuleService.updateStatus(id, status,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, abnormalEventRule);
  }

  @DeleteMapping("/abnormal-event-rules/{id}")
  @Secured({"PERM_USER"})
  public void deleteAbnormalEventRule(@PathVariable String id) {
    AbnormalEventRuleBO abnormalEventRule = abnormalEventRuleService.deleteAbnormalEventRule(id);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, abnormalEventRule);
  }

  private Map<String, Object> abnormalEventRuleBO2Map(AbnormalEventRuleBO abnormalEventRule) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", abnormalEventRule.getId());
    map.put("type", abnormalEventRule.getType());
    map.put("typeText", abnormalEventRule.getTypeText());
    map.put("content", abnormalEventRule.getContent());
    map.put("source", abnormalEventRule.getSource());
    map.put("status", abnormalEventRule.getStatus());
    map.put("description", abnormalEventRule.getDescription());
    map.put("timestamp", abnormalEventRule.getTimestamp());

    return map;
  }

}
