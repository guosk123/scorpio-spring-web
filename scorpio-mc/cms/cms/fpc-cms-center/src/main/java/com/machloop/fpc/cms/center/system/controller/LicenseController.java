package com.machloop.fpc.cms.center.system.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.system.bo.LicenseBO;
import com.machloop.fpc.cms.center.system.bo.LicenseItemBO;
import com.machloop.fpc.cms.center.system.service.LicenseService;

/**
 * @author guosk
 *
 * create at 2021年11月4日, fpc-cms-center
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/system")
public class LicenseController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseController.class);

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private LicenseService licenseService;

  @GetMapping("/licenses")
  @Secured({"PERM_SYS_USER"})
  public Map<String, Object> queryLicense() {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    LicenseBO license = licenseService.queryLatestLicense();

    if (CollectionUtils.isNotEmpty(license.getLicenseItemList())) {
      List<Map<String, Object>> licenseItemList = Lists
          .newArrayListWithCapacity(license.getLicenseItemList().size());
      for (LicenseItemBO licenseItem : license.getLicenseItemList()) {
        Map<String,
            Object> licenseItemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        licenseItemMap.put("serialNo", licenseItem.getSerialNo());
        licenseItemMap.put("fpcId", licenseItem.getFpcId());
        licenseItemMap.put("fpcIp", licenseItem.getFpcIp());

        licenseItemList.add(licenseItemMap);
      }

      resultMap.put("licenseItemList", licenseItemList);
    }
    resultMap.put("collectTime", license.getCollectTime());
    resultMap.put("signTime", license.getSignTime());
    resultMap.put("expiryTime", license.getExpiryTime());
    resultMap.put("licenseType", license.getLicenseType());
    resultMap.put("version", license.getVersion());
    resultMap.put("fileName", license.getFileName());
    resultMap.put("localSerialNo", license.getLocalSerialNo());

    return resultMap;
  }

  @PostMapping("/licenses")
  @Secured({"PERM_SYS_USER"})
  public void importLicense(@RequestParam MultipartFile file) {
    // 文件不能超过10KB, 后缀为txt/bin
    if (file.getSize() > 10 * 1024 || !StringUtils
        .endsWithAny(StringUtils.lowerCase(file.getOriginalFilename()), ".bin", ".txt")) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "License文件非法");
    }

    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    Path tempFilePath = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID());

    try {
      file.transferTo(tempFilePath.toFile());
    } catch (IllegalStateException | IOException e) {
      LOGGER.warn("failed to import license.", e);
      FileUtils.deleteQuietly(tempFilePath.toFile());
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "上传License文件失败");
    }

    LicenseBO licenseBO = licenseService.importLicense(tempFilePath, file.getOriginalFilename(),
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, licenseBO);
  }
}
