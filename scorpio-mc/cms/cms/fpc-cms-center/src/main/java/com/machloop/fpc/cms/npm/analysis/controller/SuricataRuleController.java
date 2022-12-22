package com.machloop.fpc.cms.npm.analysis.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections.MapUtils;
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

import com.fasterxml.jackson.core.type.TypeReference;
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
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleBO;
import com.machloop.fpc.cms.npm.analysis.service.SuricataRuleService;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleCreationVO;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleModificationVO;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author chenshimiao
 * 
 * create at 2022/10/10 2:44 PM,cms
 * @version 1.0
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/suricata")
public class SuricataRuleController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SuricataRuleController.class);

  @Autowired
  private SuricataRuleService suricataRuleService;

  @Value("${file.suricata.rule.template.path}")
  private String suricataRuleTemplatePath;

  @GetMapping("/rules")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> querySuricataRules(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      SuricataRuleQueryVO queryVO) {
    Sort sort = new Sort(new Sort.Order(Sort.Direction.DESC, "create_time"),
        new Sort.Order(Sort.Direction.ASC, "sid"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    Page<SuricataRuleBO> suricataRulesPage = suricataRuleService.querySuricataRules(page, queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(suricataRulesPage.getSize());

    for (SuricataRuleBO suricataRuleBO : suricataRulesPage) {
      resultList.add(suricataRule2Map(suricataRuleBO));
    }

    return new PageImpl<>(resultList, page, suricataRulesPage.getTotalElements());
  }

  @GetMapping("/rules/sources")
  @Secured({"PERM_USER"})
  public Map<String, String> queryRuleSource() {
    return suricataRuleService.queryRuleSource();
  }

  @GetMapping("/rules/{sid}")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySuricataRule(@PathVariable String sid) {
    return suricataRule2Map(suricataRuleService.querySuricataRule(Integer.parseInt(sid)));
  }

  @GetMapping("/rules/as-export")
  @Secured({"PERM_USER"})
  public void exportSuricataRules(HttpServletRequest request, HttpServletResponse response,
      SuricataRuleQueryVO queryVO, String sids) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "Rule.rules"));
    response.resetBuffer();

    try (ServletOutputStream out = response.getOutputStream();) {
      suricataRuleService.exportSuricataRules(queryVO, CsvUtils.convertCSVToList(sids), out);

      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.info("export suricata rules error ", e);
    }
  }

  @GetMapping("/rules/as-template")
  @Secured({"PERM_USER"})
  public void downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
    File templateFile = new File(suricataRuleTemplatePath);

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "suricata规则导入模板.csv"));
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
      LOGGER.warn("export suricata rule template error ", e);
    }
  }

  @PostMapping("/rules/as-import")
  @Secured({"PERM_USER"})
  public void importSuricataRules(@RequestParam MultipartFile file, String classtypeId,
      String source) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
    }

    int importSuricataRules = suricataRuleService.importSuricataRules(file, classtypeId, source,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("导入" + importSuricataRules + "条Suricata规则");
  }

  @PostMapping("/rules")
  @Secured({"PERM_USER"})
  public void saveSuricataRule(@Validated SuricataRuleCreationVO suricataRuleCreationVO) {
    SuricataRuleBO suricataRuleBO = new SuricataRuleBO();
    BeanUtils.copyProperties(suricataRuleCreationVO, suricataRuleBO);

    suricataRuleBO = suricataRuleService.saveSuricataRule(suricataRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, suricataRuleBO);
  }

  @PostMapping("/rules/update")
  @Secured({"PERM_USER"})
  public void updateRules(@RequestParam(required = false, defaultValue = "") String query,
      @RequestParam(required = false, defaultValue = "") String sids,
      @RequestParam(required = false, defaultValue = "") String state,
      @RequestParam(required = false, defaultValue = "") String classtypeId,
      @RequestParam(required = false, defaultValue = "") String source,
      @RequestParam(required = false, defaultValue = "") String mitreTacticId,
      @RequestParam(required = false, defaultValue = "") String mitreTechniqueId) {

    SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(query)) {
      param = JsonHelper.deserialize(query, new TypeReference<Map<String, Object>>() {

      });
    }
    queryVO = MapToSuricataRule(param);

    suricataRuleService.batchUpdateSuricataRule(queryVO,
        CsvUtils.convertCSVToList(sids).stream().map(sid -> Integer.parseInt(sid))
            .collect(Collectors.toList()),
        state, classtypeId, source, mitreTacticId, mitreTechniqueId,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("修改过滤条件 " + query + "，修改"
        + (StringUtils.isNotBlank(state) ? "state为" + state : "")
        + (StringUtils.isNotBlank(classtypeId) ? "classtypeIds为" + classtypeId : "")
        + (StringUtils.isNotBlank(source) ? "source为" + source : "")
        + (StringUtils.isNotBlank(mitreTacticId) ? "mitreTacticIds为" + mitreTacticId : "")
        + (StringUtils.isNotBlank(mitreTechniqueId) ? "mitreTechniqueId为" + mitreTechniqueId : ""));
  }

  @PutMapping("/rules/{sids}/state")
  @Secured({"PERM_USER"})
  public void updateStatus(@PathVariable String sids, String state) {
    suricataRuleService.updateState(CsvUtils.convertCSVToList(sids), state,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(
        StringUtils.equals(state, Constants.BOOL_YES) ? "启用suricata规则：" : "禁用suricata规则：" + sids);
  }

  @PutMapping("/rules/{sid}")
  @Secured({"PERM_USER"})
  public void updateSuricataRule(@PathVariable String sid,
      @Validated SuricataRuleModificationVO suricataRuleModificationVO) {
    SuricataRuleBO suricataRuleBO = new SuricataRuleBO();
    BeanUtils.copyProperties(suricataRuleModificationVO, suricataRuleBO);

    suricataRuleBO = suricataRuleService.updateSuricataRule(Integer.parseInt(sid), suricataRuleBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, suricataRuleBO);
  }

  @DeleteMapping("/rules")
  @Secured({"PERM_USER"})
  public void deleteSuricataRule(@Validated SuricataRuleQueryVO query) {
    suricataRuleService.deleteSuricataRule(Lists.newArrayListWithCapacity(0), query,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("删除suricata规则：" + query);
  }

  @DeleteMapping("/rules/{sids}")
  @Secured({"PERM_USER"})
  public void deleteSuricataRule(@NotEmpty(message = "删除规则ID不能为空") @PathVariable String sids) {
    suricataRuleService.deleteSuricataRule(CsvUtils.convertCSVToList(sids), null,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate("删除suricata规则：" + sids);
  }

  private Map<String, Object> suricataRule2Map(SuricataRuleBO suricataRuleBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", suricataRuleBO.getId());
    map.put("sid", suricataRuleBO.getSid());
    map.put("action", suricataRuleBO.getAction());
    map.put("protocol", suricataRuleBO.getProtocol());
    map.put("srcIp", suricataRuleBO.getSrcIp());
    map.put("srcPort", suricataRuleBO.getSrcPort());
    map.put("direction", suricataRuleBO.getDirection());
    map.put("destIp", suricataRuleBO.getDestIp());
    map.put("destPort", suricataRuleBO.getDestPort());
    map.put("msg", suricataRuleBO.getMsg());
    map.put("rev", suricataRuleBO.getRev());
    map.put("content", suricataRuleBO.getContent());
    map.put("priority", suricataRuleBO.getPriority());
    map.put("classtypeId", suricataRuleBO.getClasstypeId());
    map.put("mitreTacticId", suricataRuleBO.getMitreTacticId());
    map.put("mitreTechniqueId", suricataRuleBO.getMitreTechniqueId());
    map.put("cve", suricataRuleBO.getCve());
    map.put("cnnvd", suricataRuleBO.getCnnvd());
    map.put("signatureSeverity", suricataRuleBO.getSignatureSeverity());
    map.put("signatureSeverityText", suricataRuleBO.getSignatureSeverityText());
    map.put("target", suricataRuleBO.getTarget());
    map.put("threshold", suricataRuleBO.getThreshold());
    map.put("parseState", suricataRuleBO.getParseState());
    map.put("parseStateText", suricataRuleBO.getParseStateText());
    map.put("parseLog", suricataRuleBO.getParseLog());
    map.put("state", suricataRuleBO.getState());
    map.put("source", suricataRuleBO.getSource());
    map.put("sourceText", suricataRuleBO.getSourceText());
    map.put("createTime", suricataRuleBO.getCreateTime());

    return map;
  }

  private SuricataRuleQueryVO MapToSuricataRule(Map<String, Object> param) {
    SuricataRuleQueryVO queryVO = new SuricataRuleQueryVO();
    queryVO.setSrcIp(MapUtils.getString(param, "srcIp"));
    queryVO.setSrcPort(MapUtils.getString(param, "srcPort"));
    queryVO.setDestIp(MapUtils.getString(param, "destIp"));
    queryVO.setDestPort(MapUtils.getString(param, "destPort"));
    queryVO.setProtocol(MapUtils.getString(param, "protocol"));
    queryVO.setDirection(MapUtils.getString(param, "direction"));
    queryVO.setSid(MapUtils.getString(param, "sid"));
    queryVO.setClasstypeIds(MapUtils.getString(param, "classtypeIds"));
    queryVO.setMitreTacticIds(MapUtils.getString(param, "mitreTacticIds"));
    queryVO.setMitreTechniqueIds(MapUtils.getString(param, "mitreTechniqueIds"));
    queryVO.setCve(MapUtils.getString(param, "cve"));
    queryVO.setCnnvd(MapUtils.getString(param, "cnnvd"));
    queryVO.setPriority(MapUtils.getIntValue(param, "priority") == 0 ? null
        : MapUtils.getIntValue(param, "priority"));
    queryVO.setSignatureSeverity(MapUtils.getString(param, "signatureSeverity"));
    queryVO.setTarget(MapUtils.getString(param, "target"));
    queryVO.setState(MapUtils.getString(param, "state"));
    queryVO.setSource(MapUtils.getString(param, "source"));
    queryVO.setParseState(MapUtils.getString(param, "parseState"));

    return queryVO;
  }
}
