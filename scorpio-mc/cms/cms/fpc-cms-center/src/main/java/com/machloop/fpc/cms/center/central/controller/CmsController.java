package com.machloop.fpc.cms.center.central.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.central.bo.CmsBO;
import com.machloop.fpc.cms.center.central.service.CmsService;
import com.machloop.fpc.cms.center.central.vo.CmsQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/central")
public class CmsController {

  private static final Logger LOGGER = LoggerFactory.getLogger(CmsController.class);

  @Autowired
  private CmsService cmsService;

  @Autowired
  private DeviceStatusService deviceStatusService;

  @GetMapping("/cms-devices")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryCmss(CmsQueryVO cmsQueryVO) {
    List<CmsBO> cmsBOs = cmsService.queryCms(cmsQueryVO);

    List<Map<String, Object>> cmsList = Lists.newArrayListWithExpectedSize(cmsBOs.size());
    cmsBOs.forEach(cms -> cmsList.add(cmsToMap(cms, true, false)));

    return cmsList;
  }

  @GetMapping("/cms-devices/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryCms(@PathVariable @NotEmpty(message = "设备id不能为空") String id) {

    return cmsToMap(cmsService.queryCmsById(id), false, true);
  }

  @GetMapping("/cms-devices/{serialNumber}/loginUrl")
  @Secured({"PERM_USER"})
  public String queryCmsLoginUrl(
      @PathVariable @NotEmpty(message = "设备序列号不能为空") String serialNumber) {
    String cmsLoginUrl = cmsService.queryCmsLoginUrl(serialNumber);

    // 刷新登录时间
    DeviceStatusDO deviceStatus = deviceStatusService
        .queryDeviceStatus(FpcCmsConstants.DEVICE_TYPE_CMS, serialNumber);
    deviceStatus.setLastLoginTime(DateUtils.now());
    deviceStatusService.refreshDeviceStatus(deviceStatus);

    return cmsLoginUrl;
  }

  @DeleteMapping("/cms-devices/{id}")
  @Secured({"PERM_USER"})
  public void deleteCms(@PathVariable @NotEmpty(message = "设备id不能为空") String id) {

    String operatorId = LoggedUserContext.getCurrentUser().getId();
    CmsBO cmsBO = cmsService.deleteCms(id, operatorId);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, cmsBO);
  }

  @GetMapping("/cms-devices/as-export")
  @Secured({"PERM_USER"})
  public void exportCmsMessage(HttpServletRequest request, HttpServletResponse response) {

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "CMS设备列表.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream()) {
      String content = cmsService.exportCmsMessage();
      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      out.write(content.getBytes(StandardCharsets.UTF_8));
      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.warn("export cms file error ", e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException error) {
        LOGGER.warn("export cms file, send error message fail.");
      }
    }
  }

  @GetMapping("/cms-serial-numbers/as-export")
  @Secured({"PERM_USER"})
  public void exportCmsSerialNumber(HttpServletRequest request, HttpServletResponse response) {

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "CMS设备序列号列表.csv"));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream()) {
      String content = cmsService.exportCmsSerialNumber();
      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      out.write(content.getBytes(StandardCharsets.UTF_8));
      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.warn("export cms serialNumber file error ", e);
      try {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } catch (IOException error) {
        LOGGER.warn("export cms serialNumber file, send error message fail.");
      }
    }
  }

  private Map<String, Object> cmsToMap(CmsBO cmsBO, boolean isDetail, boolean isShowVerifyField) {
    Map<String, Object> cmsMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    cmsMap.put("id", cmsBO.getId());
    cmsMap.put("name", cmsBO.getName());
    cmsMap.put("ip", cmsBO.getIp());
    cmsMap.put("description", cmsBO.getDescription());

    if (isShowVerifyField) {
      cmsMap.put("appKey", cmsBO.getAppKey());
      cmsMap.put("appToken", cmsBO.getAppToken());
      cmsMap.put("cmsToken", cmsBO.getCmsToken());
    }

    if (isDetail) {
      cmsMap.put("parentCmsSerialNumber", cmsBO.getSuperiorCmsSerialNumber());
      cmsMap.put("createTime", cmsBO.getCreateTime());
      cmsMap.put("serialNumber", cmsBO.getSerialNumber());
      cmsMap.put("version", cmsBO.getVersion());
      cmsMap.put("licenseStatus", cmsBO.getLicenseStatus());
      cmsMap.put("licenseStatusText", cmsBO.getLicenseStatusText());
      cmsMap.put("connectStatus", cmsBO.getConnectStatus());
      cmsMap.put("connectStatusText", cmsBO.getConnectStatusText());
      cmsMap.put("lastLoginTime", cmsBO.getLastLoginTime());
      cmsMap.put("lastInteractiveTime", cmsBO.getLastInteractiveTime());
      cmsMap.put("lastInteractiveLatency", cmsBO.getLastInteractiveLatency());
      cmsMap.put("upTime", cmsBO.getUpTime());
      cmsMap.put("cpuMetric", cmsBO.getCpuMetric());
      cmsMap.put("memoryMetric", cmsBO.getMemoryMetric());
      cmsMap.put("systemFsMetric", cmsBO.getSystemFsMetric());
    }

    return cmsMap;
  }

}
