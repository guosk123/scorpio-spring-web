package com.machloop.fpc.cms.center.central.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.invoker.FpcManagerInvoker;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.ClusterService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.central.vo.FpcQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/central")
public class FpcController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FpcController.class);

  @Autowired
  private FpcService fpcService;

  @Autowired
  private DeviceStatusService deviceStatusService;

  @Autowired
  private ClusterService clusterService;

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private FpcManagerInvoker fpcManagerInvoker;

  @GetMapping("/fpc-devices")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryFpcs(FpcQueryVO fpcQueryVO) {
    List<FpcBO> fpcBOs = fpcService.queryFpcs(fpcQueryVO);

    List<Map<String, Object>> fpcList = Lists.newArrayListWithExpectedSize(fpcBOs.size());
    fpcBOs.forEach(fpc -> fpcList.add(fpcToMap(fpc, true, false)));

    return fpcList;
  }

  @GetMapping("/fpc-devices/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryFpc(@PathVariable @NotEmpty(message = "设备id不能为空") String id) {

    return fpcToMap(fpcService.queryFpcById(id), false, true);
  }

  @GetMapping("/fpc-devices/{serialNumber}/loginUrl")
  @Secured({"PERM_USER"})
  public String queryFpcLoginUrl(
      @PathVariable @NotEmpty(message = "设备序列号不能为空") String serialNumber) {
    String fpcLoginUrl = fpcService.queryFpcLoginUrl(serialNumber);

    // 刷新登录时间
    DeviceStatusDO deviceStatus = deviceStatusService
        .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_TFA, serialNumber);
    deviceStatus.setLastLoginTime(DateUtils.now());
    deviceStatusService.refreshDeviceStatus(deviceStatus);

    return fpcLoginUrl;
  }

  @DeleteMapping("/fpc-devices/{id}")
  @Secured({"PERM_USER"})
  public void deleteFpc(@PathVariable @NotEmpty(message = "设备id不能为空") String id) {

    String operatorId = LoggedUserContext.getCurrentUser().getId();
    FpcBO fpcVO = fpcService.deleteFpc(id, operatorId);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, fpcVO);
  }

  @GetMapping("/fpc-devices/as-export")
  @Secured({"PERM_USER"})
  public void exportFpcMessage(HttpServletRequest request, HttpServletResponse response) {

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "探针设备列表.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream()) {
      String content = fpcService.exportFpcMessage();
      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      out.write(content.getBytes(StandardCharsets.UTF_8));
      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.warn("export fpc file error ", e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException error) {
        LOGGER.warn("export fpc file, send error message fail.");
      }
    }
  }

  @GetMapping("/fpc-serial-numbers/as-export")
  @Secured({"PERM_USER"})
  public void exportFpcSerialNumber(HttpServletRequest request, HttpServletResponse response) {

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "探针设备序列号列表.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream()) {
      String content = fpcService.exportFpcSerialNumber();
      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      out.write(content.getBytes(StandardCharsets.UTF_8));
      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.warn("export fpc serialNumber file error ", e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException error) {
        LOGGER.warn("export fpc serialNumber file, send error message fail.");
      }
    }
  }

  @PostMapping("/fpc-devices/licenses")
  @Secured({"PERM_USER"})
  public void importFpcLicense(@RequestParam MultipartFile file, @RequestParam String id) {
    // 文件不能超过10KB, 后缀为txt/bin
    if (file.getSize() > 10 * 1024 || !StringUtils
        .endsWithAny(StringUtils.lowerCase(file.getOriginalFilename()), ".bin", ".txt")) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "License文件非法");
    }

    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID() + ".txt")
        .toFile();

    try {
      file.transferTo(tempFile);
    } catch (IllegalStateException | IOException e) {
      LOGGER.warn("failed to import license.", e);
      FileUtils.deleteQuietly(tempFile);
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "上传License文件失败");
    }

    Map<String, Object> importResult = fpcManagerInvoker.importFpcLicense(id, tempFile);
    FileUtils.deleteQuietly(tempFile);
    LOGGER.info("import fpc license, result: {}", importResult);

    if ((int) importResult.get("code") != WebappConstants.REST_RESULT_SUCCESS_CODE) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION,
          MapUtils.getString(importResult, "msg"));
    }

    LogHelper.auditOperate(JsonHelper.serialize(importResult.get("result")));
  }

  @PostMapping("/fpc-devices/sync-remote-servers")
  @Secured({"PERM_USER"})
  public void syncRemoteServers() {
    // 集群节点与当前实际管理的探针设备保持一致
    clusterService.queryAbnormalNodesAndRefresh();
  }

  private Map<String, Object> fpcToMap(FpcBO fpcBO, boolean isDetail, boolean isShowVerifyField) {
    Map<String, Object> fpcMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    fpcMap.put("id", fpcBO.getId());
    fpcMap.put("name", fpcBO.getName());
    fpcMap.put("ip", fpcBO.getIp());
    fpcMap.put("description", fpcBO.getDescription());

    if (isShowVerifyField) {
      fpcMap.put("appKey", fpcBO.getAppKey());
      fpcMap.put("appToken", fpcBO.getAppToken());
      fpcMap.put("cmsToken", fpcBO.getCmsToken());
    }

    if (isDetail) {
      fpcMap.put("cmsSerialNumber", fpcBO.getCmsSerialNumber());
      fpcMap.put("cmsName", fpcBO.getCmsName());
      fpcMap.put("createTime", fpcBO.getCreateTime());
      fpcMap.put("serialNumber", fpcBO.getSerialNumber());
      fpcMap.put("version", fpcBO.getVersion());
      fpcMap.put("licenseStatus", fpcBO.getLicenseStatus());
      fpcMap.put("licenseStatusText", fpcBO.getLicenseStatusText());
      fpcMap.put("connectStatus", fpcBO.getConnectStatus());
      fpcMap.put("connectStatusText", fpcBO.getConnectStatusText());
      fpcMap.put("lastLoginTime", fpcBO.getLastLoginTime());
      fpcMap.put("lastInteractiveTime", fpcBO.getLastInteractiveTime());
      fpcMap.put("lastInteractiveLatency", fpcBO.getLastInteractiveLatency());
      fpcMap.put("upTime", fpcBO.getUpTime());
      fpcMap.put("alarmCount", fpcBO.getAlarmCount());
      fpcMap.put("cpuMetric", fpcBO.getCpuMetric());
      fpcMap.put("memoryMetric", fpcBO.getMemoryMetric());
      fpcMap.put("systemFsMetric", fpcBO.getSystemFsMetric());
      fpcMap.put("indexFsMetric", fpcBO.getIndexFsMetric());
      fpcMap.put("metadataFsMetric", fpcBO.getMetadataFsMetric());
      fpcMap.put("metadataHotFsMetric", fpcBO.getMetadataHotFsMetric());
      fpcMap.put("packetFsMetric", fpcBO.getPacketFsMetric());
      fpcMap.put("raidList", fpcBO.getRaidList());
      fpcMap.put("netifList", fpcBO.getNetifList());
    }

    return fpcMap;
  }

}
