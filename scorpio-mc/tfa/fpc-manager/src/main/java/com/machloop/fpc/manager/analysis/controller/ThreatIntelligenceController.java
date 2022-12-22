package com.machloop.fpc.manager.analysis.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
import com.machloop.fpc.manager.analysis.bo.ThreatIntelligenceBO;
import com.machloop.fpc.manager.analysis.service.ThreatIntelligenceService;
import com.machloop.fpc.manager.analysis.vo.ThreatIntelligenceModificationVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月8日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/analysis")
public class ThreatIntelligenceController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThreatIntelligenceController.class);

  @Value("${file.threat.intelligence.template.path}")
  private String threatIntelligenceTemplatePath;

  @Autowired
  private ThreatIntelligenceService intelligenceService;

  @GetMapping("/threat-intelligences")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryIntelligences(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      String type, String content) {

    Sort sort = new Sort(new Order(Sort.Direction.DESC, "timestamp"),
        new Order(Sort.Direction.ASC, "type"), new Order(Sort.Direction.ASC, "content"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<ThreatIntelligenceBO> threatIntelligencesPage = intelligenceService
        .queryIntelligences(page, type, content);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(threatIntelligencesPage.getSize());
    for (ThreatIntelligenceBO threatIntelligence : threatIntelligencesPage) {
      resultList.add(threatIntelligenceBO2Map(threatIntelligence));
    }

    return new PageImpl<>(resultList, page, threatIntelligencesPage.getTotalElements());
  }

  @GetMapping("/threat-intelligences/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryIntelligence(@PathVariable String id) {
    return threatIntelligenceBO2Map(intelligenceService.queryIntelligence(id));
  }

  @GetMapping("/threat-intelligences/as-export")
  @Secured({"PERM_USER"})
  public void exportIntelligence(HttpServletRequest request, HttpServletResponse response,
      String type, String content) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "intelligences.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      List<String> lineList = intelligenceService.exportIntelligences(type, content);

      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : lineList) {
        out.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export threat intelligences error ", e);
    }
  }

  @GetMapping("/threat-intelligences/as-template")
  @Secured({"PERM_USER"})
  public void downloadIntelligenceTemplate(HttpServletRequest request,
      HttpServletResponse response) {
    File templateFile = new File(threatIntelligenceTemplatePath);

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "威胁情报导入模板.csv"));
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

  @PostMapping("/threat-intelligences/as-import")
  @Secured({"PERM_USER"})
  public void importIntelligence(@RequestParam MultipartFile file, @RequestParam boolean custom) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
    }

    intelligenceService.importIntelligences(file, custom);
  }

  @PutMapping("/threat-intelligences/{id}")
  @Secured({"PERM_USER"})
  public void updateIntelligence(@PathVariable @NotEmpty(message = "修改威胁情报时传入的id不能为空") String id,
      @Validated ThreatIntelligenceModificationVO intelligenceVO) {
    ThreatIntelligenceBO intelligenceBO = new ThreatIntelligenceBO();
    BeanUtils.copyProperties(intelligenceVO, intelligenceBO);

    ThreatIntelligenceBO intelligence = intelligenceService.updateIntelligence(id, intelligenceBO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, intelligence);
  }

  @DeleteMapping("/threat-intelligences/{id}")
  @Secured({"PERM_USER"})
  public void deleteIntelligence(@PathVariable @NotEmpty(message = "删除威胁情报时传入的id不能为空") String id) {
    ThreatIntelligenceBO intelligenceBO = intelligenceService.deleteIntelligence(id);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, intelligenceBO);
  }


  private Map<String, Object> threatIntelligenceBO2Map(ThreatIntelligenceBO threatIntelligence) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", threatIntelligence.getId());
    map.put("type", threatIntelligence.getType());
    map.put("typeText", threatIntelligence.getTypeText());
    map.put("content", threatIntelligence.getContent());
    map.put("threatCategory", threatIntelligence.getThreatCategory());
    map.put("description", threatIntelligence.getDescription());
    map.put("timestamp", threatIntelligence.getTimestamp());

    return map;
  }
}
