package com.machloop.fpc.npm.analysis.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.npm.analysis.bo.MitreAttackBO;
import com.machloop.fpc.npm.analysis.bo.SuricataRuleClasstypeBO;
import com.machloop.fpc.npm.analysis.service.MitreAttackService;
import com.machloop.fpc.npm.analysis.service.SuricataRuleClasstypeService;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleClasstypeCreationVO;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleClasstypeModificationVO;

/**
 * @author guosk
 *
 * create at 2022年4月7日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/suricata")
public class SuricataRuleClasstypeController {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(SuricataRuleClasstypeController.class);

  @Value("${file.suricata.classtype.template.path}")
  private String suricataClasstypeTemplatePath;

  @Autowired
  private SuricataRuleClasstypeService suricataRuleClasstypeService;

  @Autowired
  private MitreAttackService mitreAttackService;

  @GetMapping("/rule-classtypes")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> querySuricataRuleClasstypes(
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {
    Date startTimeDate = null;
    Date endTimeDate = null;
    if (StringUtils.isNotBlank(startTime)) {
      startTimeDate = DateUtils.parseISO8601Date(startTime);
    }
    if (StringUtils.isNotBlank(endTime)) {
      endTimeDate = DateUtils.parseISO8601Date(endTime);
    }

    List<SuricataRuleClasstypeBO> suricataRuleClasstypes = suricataRuleClasstypeService
        .querySuricataRuleClasstypes(startTimeDate, endTimeDate);

    return suricataRuleClasstypes.stream()
        .map(suricataRuleClasstype -> suricataRuleClasstype2Map(suricataRuleClasstype))
        .collect(Collectors.toList());
  }

  @GetMapping("/mitre-attacks")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMitreAttacks(
      @RequestParam(required = false) String startTime,
      @RequestParam(required = false) String endTime) {
    Date startTimeDate = null;
    Date endTimeDate = null;
    if (StringUtils.isNotBlank(startTime)) {
      startTimeDate = DateUtils.parseISO8601Date(startTime);
    }
    if (StringUtils.isNotBlank(endTime)) {
      endTimeDate = DateUtils.parseISO8601Date(endTime);
    }

    List<MitreAttackBO> mitreAttacks = mitreAttackService.queryMitreAttacks(startTimeDate,
        endTimeDate);

    return mitreAttacks.stream().map(mitreAttack -> mitreAttack2Map(mitreAttack))
        .collect(Collectors.toList());
  }

  @GetMapping("/rule-classtypes/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySuricataRuleClasstype(@PathVariable String id) {

    return suricataRuleClasstype2Map(suricataRuleClasstypeService.querySuricataRuleClasstype(id));
  }

  @GetMapping("/rule-classtypes/as-export")
  @Secured({"PERM_USER"})
  public void exportSuricataRules(HttpServletRequest request, HttpServletResponse response) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "SuricataClasstype.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      List<String> lineList = suricataRuleClasstypeService.exportSuricataRuleClasstypes();

      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : lineList) {
        out.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export suricata classtype error ", e);
    }
  }

  @GetMapping("/rule-classtypes/as-template")
  @Secured({"PERM_USER"})
  public void downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
    File templateFile = new File(suricataClasstypeTemplatePath);

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "suricata规则分类导入模板.csv"));
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
      LOGGER.warn("export suricata classtype template error ", e);
    }
  }

  @PostMapping("/rule-classtypes/as-import")
  @Secured({"PERM_USER"})
  public void importClasstypes(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
    }

    int importClasstypes = suricataRuleClasstypeService.importClasstypes(file,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.log(LogHelper.LEVEL_NOTICE, LogHelper.CATEGORY_AUDIT,
        "导入" + importClasstypes + "条Suricata规则分类", LoggedUserContext.getCurrentUser().getId());
  }

  @PostMapping("/rule-classtypes")
  @Secured({"PERM_USER"})
  public void saveSuricataRuleClasstype(
      @Validated SuricataRuleClasstypeCreationVO suricataRuleClasstypeCreationVO) {
    SuricataRuleClasstypeBO suricataRuleClasstypeBO = new SuricataRuleClasstypeBO();
    BeanUtils.copyProperties(suricataRuleClasstypeCreationVO, suricataRuleClasstypeBO);

    suricataRuleClasstypeBO = suricataRuleClasstypeService.saveSuricataRuleClasstype(
        suricataRuleClasstypeBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, suricataRuleClasstypeBO);
  }

  @PutMapping("/rule-classtypes/{id}")
  @Secured({"PERM_USER"})
  public void updateAbnormalEventRule(@PathVariable String id,
      @Validated SuricataRuleClasstypeModificationVO suricataRuleClasstypeModificationVO) {
    SuricataRuleClasstypeBO suricataRuleClasstypeBO = new SuricataRuleClasstypeBO();
    BeanUtils.copyProperties(suricataRuleClasstypeModificationVO, suricataRuleClasstypeBO);

    suricataRuleClasstypeBO = suricataRuleClasstypeService.updateSuricataRuleClasstype(id,
        suricataRuleClasstypeBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, suricataRuleClasstypeBO);
  }

  @DeleteMapping("/rule-classtypes/{id}")
  @Secured({"PERM_USER"})
  public void deleteAbnormalEventRule(@PathVariable String id) {
    SuricataRuleClasstypeBO suricataRuleClasstype = suricataRuleClasstypeService
        .deleteSuricataRuleClasstype(id, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, suricataRuleClasstype);
  }

  private Map<String, Object> suricataRuleClasstype2Map(
      SuricataRuleClasstypeBO suricataRuleClasstypeBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", suricataRuleClasstypeBO.getId());
    map.put("name", suricataRuleClasstypeBO.getName());
    map.put("ruleSize", suricataRuleClasstypeBO.getRuleSize());
    map.put("alertSize", suricataRuleClasstypeBO.getAlertSize());
    map.put("createTime", suricataRuleClasstypeBO.getCreateTime());

    return map;
  }

  private Map<String, Object> mitreAttack2Map(MitreAttackBO mitreAttackBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", mitreAttackBO.getId());
    map.put("name", mitreAttackBO.getName());
    map.put("parentId", mitreAttackBO.getParentId());
    map.put("ruleSize", mitreAttackBO.getRuleSize());
    map.put("alertSize", mitreAttackBO.getAlertSize());

    return map;
  }

}
