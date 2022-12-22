package com.machloop.fpc.cms.center.knowledge.controller;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.knowledge.bo.*;
import com.machloop.fpc.cms.center.knowledge.service.SaService;
import com.machloop.fpc.cms.center.knowledge.vo.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.util.function.Tuple3;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author mazhiyuan
 *
 * create at 2020年5月20日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class SaRuleController {

  private static final Logger LOGGER = LoggerFactory.getLogger(SaRuleController.class);

  @Value("${file.sa.custom.template.path}")
  private String saCustomTemplatePath;

  @Autowired
  private SaService saService;

  @GetMapping("/sa/rules")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySaRules() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> rules = saService.queryKnowledgeRules();
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("categoryList", rules.getT1());
    result.put("subCategoryList", rules.getT2());
    result.put("applicationList", rules.getT3());

    result.put("customCategoryList", saService.queryCustomCategorys());
    result.put("customSubCategoryList", saService.queryCustomSubCategorys());
    result.put("customApplicationList", saService.queryCustomApps());
    return result;
  }

  @GetMapping("/sa/knowledge-infos")
  @Secured({"PERM_USER"})
  public Map<String, String> queryKnowledgeInfo() {
    Map<String, String> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    SaKnowledgeInfoBO infoBO = saService.queryKnowledgeInfos();
    resultMap.put("version", infoBO.getVersion());
    resultMap.put("releaseDate", DateUtils.toStringISO8601(infoBO.getReleaseDate()));
    resultMap.put("uploadDate", DateUtils.toStringISO8601(infoBO.getImportDate()));
    return resultMap;
  }

  /**
   * 
   * SA规则库导入
   */
  @PostMapping("/sa/knowledges")
  @Secured({"PERM_USER"})
  public void importKnowledges(@RequestParam MultipartFile file) {
    SaKnowledgeInfoBO knowledgeBO = saService.importKnowledges(file);
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, knowledgeBO);
  }

  /**
   * 自定义分类、子分类、规则的导入导出
   */
  @GetMapping("/sa/as-export")
  @Secured({"PERM_USER"})
  public void exportCustomRule(HttpServletRequest request, HttpServletResponse response) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "sa-custom.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      List<String> lineList = saService.exportCustomRules();

      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : lineList) {
        out.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export custom rule error ", e);
    }
  }

  @GetMapping("/sa/as-template")
  @Secured({"PERM_USER"})
  public void downloadCustomRuleTemplate(HttpServletRequest request, HttpServletResponse response) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "自定义SA导入模板.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();
        FileInputStream in = FileUtils.openInputStream(new File(saCustomTemplatePath))) {
      int len = 0;
      byte[] buffer = new byte[1024];
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export custom sa template error ", e);
    }
  }

  @PostMapping("/sa/as-import")
  @Secured({"PERM_USER"})
  public void importCustomRule(@RequestParam MultipartFile file) {
    saService.importCustomRules(file, LoggedUserContext.getCurrentUser().getId());
  }

  /**
   * 自定义分类
   */
  @GetMapping("/sa/custom-categorys/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySaCustomCategory(@PathVariable String id) {
    SaCustomCategoryBO customCategory = saService.queryCustomCategory(id);
    return saCustomCategoryBO2Map(customCategory);
  }

  @PostMapping("/sa/custom-categorys")
  @Secured({"PERM_USER"})
  public void saveSaCustomCategory(@Validated SaCustomCategoryCreationVO customCategoryVO) {
    SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
    BeanUtils.copyProperties(customCategoryVO, customCategoryBO);

    SaCustomCategoryBO customCategoryResult = saService.saveCustomCategory(customCategoryBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customCategoryResult);
  }

  @PutMapping("/sa/custom-categorys/{id}")
  @Secured({"PERM_USER"})
  public void updateSaCustomCategory(
      @PathVariable @NotEmpty(message = "修改自定义SA分类时传入的id不能为空") String id,
      @Validated SaCustomCategoryModificationVO customCategoryVO) {
    SaCustomCategoryBO customCategoryBO = new SaCustomCategoryBO();
    BeanUtils.copyProperties(customCategoryVO, customCategoryBO);

    SaCustomCategoryBO customCategoryResult = saService.updateCustomCategory(id, customCategoryBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customCategoryResult);
  }

  @DeleteMapping("/sa/custom-categorys/{id}")
  @Secured({"PERM_USER"})
  public void deleteSaCustomCategory(
      @PathVariable @NotEmpty(message = "删除自定义SA分类时传入的id不能为空") String id) {
    SaCustomCategoryBO customCategoryBO = saService.deleteCustomCategory(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customCategoryBO);
  }

  /**
   * 自定义子分类
   */
  @GetMapping("/sa/custom-subcategorys/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySaCustomSubCategory(@PathVariable String id) {
    SaCustomSubCategoryBO customSubCategoryBO = saService.queryCustomSubCategory(id);
    return saCustomSubCategoryBO2Map(customSubCategoryBO);
  }

  @PostMapping("/sa/custom-subcategorys")
  @Secured({"PERM_USER"})
  public void saveSaCustomSubCategory(
      @Validated SaCustomSubCategoryCreationVO customSubCategoryVO) {
    SaCustomSubCategoryBO customSubCategoryBO = new SaCustomSubCategoryBO();
    BeanUtils.copyProperties(customSubCategoryVO, customSubCategoryBO);

    SaCustomSubCategoryBO customSubCategoryResult = saService
        .saveCustomSubCategory(customSubCategoryBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customSubCategoryResult);
  }

  @PutMapping("/sa/custom-subcategorys/{id}")
  @Secured({"PERM_USER"})
  public void updateSaCustomSubCategory(
      @PathVariable @NotEmpty(message = "修改自定义SA子分类时传入的id不能为空") String id,
      @Validated SaCustomSubCategoryModificationVO customSubCategoryVO) {
    SaCustomSubCategoryBO customSubCategoryBO = new SaCustomSubCategoryBO();
    BeanUtils.copyProperties(customSubCategoryVO, customSubCategoryBO);

    SaCustomSubCategoryBO customSubCategoryResult = saService.updateCustomSubCategory(id,
        customSubCategoryBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customSubCategoryResult);
  }

  @DeleteMapping("/sa/custom-subcategorys/{id}")
  @Secured({"PERM_USER"})
  public void deleteSaCustomSubCategory(
      @PathVariable @NotEmpty(message = "删除自定义SA子分类时传入的id不能为空") String id) {
    SaCustomSubCategoryBO customSubCategoryBO = saService.deleteCustomSubCategory(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customSubCategoryBO);
  }

  /**
   * 自定义应用
   */
  @GetMapping("/sa/custom-applications/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySaCustomApp(@PathVariable String id) {
    SaCustomApplicationBO customAppBO = saService.queryCustomApp(id);
    return saCustomApplicationBO2Map(customAppBO);
  }

  @PostMapping("/sa/custom-applications")
  @Secured({"PERM_USER"})
  public void saveSaCustomRule(@Validated SaCustomApplicationCreationVO customAppVO) {
    SaCustomApplicationBO customAppBO = new SaCustomApplicationBO();
    BeanUtils.copyProperties(customAppVO, customAppBO);

    SaCustomApplicationBO customAppBOResult = saService.saveCustomApp(customAppBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customAppBOResult);
  }

  @PutMapping("/sa/custom-applications/{id}")
  @Secured({"PERM_USER"})
  public void updateSaCustomRule(@PathVariable @NotEmpty(message = "修改自定义SA规则时传入的id不能为空") String id,
      @Validated SaCustomApplicationModificationVO customAppVO) {
    SaCustomApplicationBO customAppBO = new SaCustomApplicationBO();
    BeanUtils.copyProperties(customAppVO, customAppBO);

    SaCustomApplicationBO customApp = saService.updateCustomApp(id, customAppBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, customApp);
  }

  @DeleteMapping("/sa/custom-applications/{id}")
  @Secured({"PERM_USER"})
  public void deleteSaCustomRule(
      @PathVariable @NotEmpty(message = "删除自定义SA规则时传入的id不能为空") String id) {
    SaCustomApplicationBO customAppBO = saService.deleteCustomApp(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customAppBO);
  }

  private Map<String, Object> saCustomCategoryBO2Map(SaCustomCategoryBO customCategoryBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", customCategoryBO.getId());
    map.put("name", customCategoryBO.getName());
    map.put("categoryId", customCategoryBO.getCategoryId());
    map.put("subCategoryIds", customCategoryBO.getSubCategoryIds());
    map.put("description", customCategoryBO.getDescription());
    map.put("createTime", customCategoryBO.getCreateTime());
    map.put("updateTime", customCategoryBO.getUpdateTime());

    return map;
  }

  private Map<String, Object> saCustomSubCategoryBO2Map(SaCustomSubCategoryBO customSubCategoryBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", customSubCategoryBO.getId());
    map.put("name", customSubCategoryBO.getName());
    map.put("categoryId", customSubCategoryBO.getCategoryId());
    map.put("subCategoryId", customSubCategoryBO.getSubCategoryId());
    map.put("applicationIds", customSubCategoryBO.getApplicationIds());
    map.put("description", customSubCategoryBO.getDescription());
    map.put("createTime", customSubCategoryBO.getCreateTime());
    map.put("updateTime", customSubCategoryBO.getUpdateTime());

    return map;
  }

  private Map<String, Object> saCustomApplicationBO2Map(SaCustomApplicationBO customAppBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", customAppBO.getId());
    map.put("name", customAppBO.getName());
    map.put("applicationId", customAppBO.getApplicationId());
    map.put("categoryId", customAppBO.getCategoryId());
    map.put("subCategoryId", customAppBO.getSubCategoryId());
    map.put("l7ProtocolId", customAppBO.getL7ProtocolId());
    map.put("rule", customAppBO.getRule());
    map.put("description", customAppBO.getDescription());
    map.put("createTime", customAppBO.getCreateTime());
    map.put("updateTime", customAppBO.getUpdateTime());

    return map;
  }
}
