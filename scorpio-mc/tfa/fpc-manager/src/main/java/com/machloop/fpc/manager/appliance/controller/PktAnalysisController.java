package com.machloop.fpc.manager.appliance.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.clickhouse.client.internal.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.appliance.bo.PktAnalysisBO;
import com.machloop.fpc.manager.appliance.dao.PktAnalysisDao;
import com.machloop.fpc.manager.appliance.service.PktAnalysisService;

/**
 * @author "Minjiajun"
 *
 * create at 2022年4月18日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class PktAnalysisController {

  private static final Logger LOGGER = LoggerFactory.getLogger(PktAnalysisController.class);

  @Value("${file.pktAnalysis.plugins.path}")
  private String pktAnalysisPluginsPath;

  @Autowired
  private PktAnalysisService pktAnalysisService;

  @Autowired
  private PktAnalysisDao pktAnalysisDao;

  @GetMapping("/pkt-analysis")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryPktAnalysis(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {
    Sort sort = new Sort(Sort.Direction.DESC, "create_time");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<PktAnalysisBO> pktAnalysisPage = pktAnalysisService.queryPktAnalysises(page);

    List<Map<String, Object>> resultList = Lists.newArrayListWithCapacity(pageSize);
    for (PktAnalysisBO pktAnalysisBO : pktAnalysisPage) {
      resultList.add(pktAnalysisBO2Map(pktAnalysisBO));
    }

    return new PageImpl<>(resultList, page, pktAnalysisPage.getTotalElements());
  }

  @GetMapping("/pkt-analysis/{id}")
  @Secured({"PERM_USER"})
  public String getPktAnalysisFile(@PathVariable String id) {
    String result = pktAnalysisService.getPktAnalysisFile(id);
    return result;
  }

  @GetMapping("/pkt-analysis/{id}/files")
  @Secured({"PERM_USER"})
  public void downloadPktAnalysisFile(@PathVariable @NotEmpty(message = "任务id不能为空") String id,
      HttpServletRequest request, HttpServletResponse response) {

    String fileName = pktAnalysisDao.queryPktAnalysis(id).getFileName();
    if (StringUtils.isBlank(fileName)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "在线分析脚本不存在");
    }
    String fileUrl = pktAnalysisPluginsPath.concat(fileName);

    File templateFile = new File(fileUrl);

    // 设置下载文件格式
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();
        FileInputStream in = FileUtils.openInputStream(templateFile)) {
      int len = 0;
      byte[] buffer = new byte[1024];
      while ((len = in.read(buffer)) > 0) {
        out.write(buffer, 0, len);
      }
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export pkt-analysis-plugins template error ", e);
    }
  }

  @PostMapping("/pkt-analysis")
  @Secured({"PERM_USER"})
  public void importPktAnalysis(@RequestParam MultipartFile file, String fileName, String protocol,
      @RequestParam(required = false) String description) {
    pktAnalysisService.savePktAnalysis(file, fileName, protocol, description,
        LoggedUserContext.getCurrentUser().getId());
  }

  @DeleteMapping("/pkt-analysis/{id}")
  @Secured({"PERM_USER"})
  public void deletePktAnalysis(@PathVariable @NotEmpty(message = "删除在线分析脚本时传入的id不能为空") String id) {
    PktAnalysisBO pktAnalysisBO = pktAnalysisService.deletePktAnalysis(id,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, pktAnalysisBO);
  }

  private static Map<String, Object> pktAnalysisBO2Map(PktAnalysisBO pktAnalysisBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", pktAnalysisBO.getId());
    map.put("fileName", pktAnalysisBO.getFileName());
    map.put("protocol", pktAnalysisBO.getProtocol());
    map.put("parseStatus", pktAnalysisBO.getParseStatus());
    map.put("parseLog", pktAnalysisBO.getParseLog());
    map.put("description", pktAnalysisBO.getDescription());
    map.put("createTime", pktAnalysisBO.getCreateTime());

    return map;
  }
}
