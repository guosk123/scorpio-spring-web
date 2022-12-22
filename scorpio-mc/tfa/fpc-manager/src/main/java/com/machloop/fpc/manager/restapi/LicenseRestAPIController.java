package com.machloop.fpc.manager.restapi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.system.bo.LicenseBO;
import com.machloop.fpc.manager.system.service.LicenseService;

/**
 * @author guosk
 *
 * create at 2021年12月24日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class LicenseRestAPIController {

  private static final Logger LOGGER = LoggerFactory.getLogger(LicenseRestAPIController.class);

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private UserService userService;

  @PostMapping("/licenses")
  @RestApiSecured
  public RestAPIResultVO importLicense(@RequestParam MultipartFile file,
      HttpServletRequest request) {
    // 文件不能超过10KB, 后缀为txt/bin
    if (file.getSize() > 10 * 1024 || !StringUtils
        .endsWithAny(StringUtils.lowerCase(file.getOriginalFilename()), ".bin", ".txt")) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("License文件非法")
          .build();
    }

    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    Path tempFilePath = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID());

    try {
      file.transferTo(tempFilePath.toFile());
    } catch (IllegalStateException | IOException e) {
      LOGGER.warn("failed to import license.", e);
      FileUtils.deleteQuietly(tempFilePath.toFile());
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("文件写入失败，License导入失败").build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    LicenseBO licenseBO = null;
    try {
      licenseBO = licenseService.importLicense(tempFilePath, file.getOriginalFilename(),
          userBO.getId());

      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, licenseBO, userBO.getFullname(),
          userBO.getName());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    return RestAPIResultVO.resultSuccess(licenseBO);
  }

}
