package com.machloop.fpc.manager.appliance.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.DomainWhiteListBO;
import com.machloop.fpc.manager.appliance.service.DomainWhiteListService;
import com.machloop.fpc.manager.appliance.vo.DomainWhiteListVO;

/**
 * @author chenshimiao
 *
 * create at 2022/12/8 9:17 AM,cms
 * @version 1.0
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class DomainWhiteListController {

  private static final Logger LOGGER = LoggerFactory.getLogger(DomainWhiteListController.class);

  @Value("${file.DomainWhiteList.template.path}")
  private String domainWhiteListTemplatePath;

  @Autowired
  private DomainWhiteListService domainWhiteListService;

  @GetMapping("/domain-white")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryDomainWhiteList(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(required = false, defaultValue = "") String name,
      @RequestParam(required = false, defaultValue = "") String domain) {

    Sort sort = new Sort(Sort.Direction.DESC, "create_time");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<DomainWhiteListBO> domainWhiteListPage = domainWhiteListService.queryDomainWhiteList(page,
        name, domain);

    List<
        Map<String, Object>> result = Lists.newArrayListWithCapacity(domainWhiteListPage.getSize());
    for (DomainWhiteListBO domainWhiteListBO : domainWhiteListPage) {
      result.add(domainWhiteBO2Map(domainWhiteListBO, true));
    }

    return new PageImpl<>(result, page, domainWhiteListPage.getTotalElements());
  }

  @GetMapping("/domain-white/as-list")
  @Secured({"PREM_USER"})
  public List<Map<String, Object>> queryDomainWhiteList() {
    List<DomainWhiteListBO> domainWhiteListBOS = domainWhiteListService.queryDomainWhiteList();
    List<
        Map<String, Object>> resultList = Lists.newArrayListWithCapacity(domainWhiteListBOS.size());
    domainWhiteListBOS
        .forEach(domainWhiteListBO -> resultList.add(domainWhiteBO2Map(domainWhiteListBO, false)));
    return resultList;
  }

  @GetMapping("/domain-white/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryDomainWhite(@PathVariable("id") String id) {
    return domainWhiteBO2Map(domainWhiteListService.queryDomainWhite(id), true);
  }

  @GetMapping("/domain-white/as-template")
  @Secured({"PERM_USER"})
  public void downLoadDomainWhiteListTemplate(HttpServletRequest request,
      HttpServletResponse response) {
    File templateFile = new File(domainWhiteListTemplatePath);

    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "域名白名单导入模版.csv"));
    response.resetBuffer();

    try (ServletOutputStream outputStream = response.getOutputStream();
        FileInputStream in = FileUtils.openInputStream(templateFile)) {
      int len = 0;
      byte[] buffer = new byte[1024];
      outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      while ((len = in.read(buffer)) > 0) {
        outputStream.write(buffer, 0, len);
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export domainWhiteList template error ", e);
    }
  }

  @GetMapping("/domain-white/as-export")
  @Secured({"PERM_USER"})
  public void exportDomainWhiteList(@RequestParam(required = false) String name,
      @RequestParam(required = false) String domain, HttpServletRequest request,
      HttpServletResponse response) {
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "domainWhiteList.csv"));
    response.resetBuffer();
    try (ServletOutputStream outputStream = response.getOutputStream();) {
      List<String> lineList = domainWhiteListService.exportDomainWhiteList(name, domain);

      outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      for (String line : lineList) {
        outputStream.write(line.getBytes(StandardCharsets.UTF_8));
      }
      response.flushBuffer();
    } catch (IOException exception) {
      LOGGER.warn("export domainWhiteList error", exception);
    }
  }

  @PostMapping("/domain-white/as-import")
  @Secured({"PERM_USER"})
  public void importDomainWhiteList(@RequestParam MultipartFile file) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名存在非法字符：[%s]", matchingIllegalCharacters));
    }

    int count = domainWhiteListService.importDomainWhiteList(file,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate("导入" + count + "域名白名单");
  }

  @PostMapping("/domain-white")
  @Secured({"PERM_USER"})
  public void saveDomainWhiteList(@Validated DomainWhiteListVO domainWhiteListVO) {
    DomainWhiteListBO domainWhiteListBO = new DomainWhiteListBO();
    BeanUtils.copyProperties(domainWhiteListVO, domainWhiteListBO);

    DomainWhiteListBO domainWhiteList = domainWhiteListService
        .saveDomainWhiteList(domainWhiteListBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, domainWhiteList);
  }

  @PutMapping("/domain-white/{id}")
  @Secured({"PERM_USER"})
  public void updateDomainWhiteList(
      @PathVariable @NotEmpty(message = "修改域名白名单时传入的id不能为空") String id,
      @Validated DomainWhiteListVO domainWhiteListVO) {
    DomainWhiteListBO domainWhiteListBO = new DomainWhiteListBO();
    BeanUtils.copyProperties(domainWhiteListVO, domainWhiteListBO);

    DomainWhiteListBO domainWhiteList = domainWhiteListService.updateDomainWhiteList(id,
        domainWhiteListBO, LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, domainWhiteList);
  }

  @DeleteMapping("/domain-white/{id}")
  @Secured({"PERM_USER"})
  public void deleteDomainWhite(@PathVariable @NotEmpty(message = "删除域名白名单时传入的id不能为空") String id) {

    DomainWhiteListBO domainWhiteListBO = domainWhiteListService.deleteDomainWhite(id,
        LoggedUserContext.getCurrentUser().getId(), false);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, domainWhiteListBO);
  }

  @DeleteMapping("/domain-white")
  @Secured({"PERM_USER"})
  public void deleteDomainWhiteByNameAndDomain(
      @RequestParam(required = false, defaultValue = "") String name,
      @RequestParam(required = false, defaultValue = "") String domain) {
    List<DomainWhiteListBO> domainWhiteListBOList = domainWhiteListService
        .deleteDomainWhiteByNameAndDomain(name, domain, LoggedUserContext.getCurrentUser().getId());

    for (DomainWhiteListBO domainWhiteListBO : domainWhiteListBOList) {
      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, domainWhiteListBO);
    }
  }

  private static Map<String, Object> domainWhiteBO2Map(DomainWhiteListBO domainWhiteListBO,
      boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", domainWhiteListBO.getId());
    map.put("name", domainWhiteListBO.getName());
    map.put("domain", domainWhiteListBO.getDomain());
    map.put("description", domainWhiteListBO.getDescription());

    if (isDetail) {
      map.put("createTime", domainWhiteListBO.getCreateTime());
      map.put("updateTime", domainWhiteListBO.getUpdateTime());
    }

    return map;
  }
}
