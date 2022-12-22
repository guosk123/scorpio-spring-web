package com.machloop.fpc.manager.restapi;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.knowledge.bo.DecryptSettingBO;
import com.machloop.fpc.manager.knowledge.service.DecryptService;
import com.machloop.fpc.manager.knowledge.vo.DecryptSettingModificationVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月17日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class DecryptRestAPIController {

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private UserService userService;

  @Autowired
  private DecryptService decryptService;

  private static final List<String> PROTOCOLS = Lists.newArrayList("http", "smtp", "pop3", "imap");

  @GetMapping("/decrypt-settings")
  @RestApiSecured
  public RestAPIResultVO queryDecryptSettings() {
    List<Map<String, Object>> decryptSetting = decryptService.queryDecryptSettings().stream()
        .map(item -> decryptSettingBO2Map(item)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(decryptSetting);

  }

  @PostMapping("/decrypt-settings")
  @RestApiSecured
  public RestAPIResultVO saveDecryptSetting(@RequestParam String ipAddress,
      @RequestParam String port, @RequestParam String protocol, @RequestParam MultipartFile file,
      HttpServletRequest request) {

    if (StringUtils.isNotBlank(ipAddress) && !NetworkUtils.isInetAddress(ipAddress)
        && !NetworkUtils.isCidr(ipAddress)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不合法的IP：" + ipAddress).build();
    }

    if (StringUtils.isNotBlank(protocol) && !PROTOCOLS.contains(protocol.toUpperCase())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不合法的协议：" + protocol).build();
    }

    if (StringUtils.isNotBlank(port) && !NetworkUtils.isInetAddressPort(port)) {
      String[] range = StringUtils.split(port, "-");
      if (range.length != 2 || !NetworkUtils.isInetAddressPort(range[0])
          || !NetworkUtils.isInetAddressPort(range[1])
          || (Integer.parseInt(range[0]) >= Integer.parseInt(range[1]))) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg("不合法的端口：" + port).build();
      }
    }

    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    Path tempPath = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID());
    try {
      // 文件最大1MB, 不能为空文件
      if (file.getSize() > 1 * 1024 * 1024 || file.getSize() <= 0) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件非法");
      }

      file.transferTo(tempPath);
    } catch (Exception e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "上传文件失败");
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    DecryptSettingBO decryptSettingBO = new DecryptSettingBO();
    DecryptSettingBO decryptSetting = new DecryptSettingBO();
    try {
      decryptSettingBO.setIpAddress(ipAddress);
      decryptSettingBO.setPort(port);
      decryptSettingBO.setProtocol(protocol);

      decryptSetting = decryptService.saveDecryptSetting(decryptSettingBO, tempPath,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, decryptSetting, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(decryptSetting.getId());
  }

  @PutMapping("/decrypt-settings/{id}")
  @RestApiSecured
  public RestAPIResultVO updateDecryptSetting(
      @PathVariable @NotEmpty(message = "修改时传入的id不能为空") String id,
      @Validated DecryptSettingModificationVO modificationVO, MultipartFile file,
      HttpServletRequest request) {

    Path tempPath = null;
    if (file != null) {
      File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
      tempPath = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID());
      // 文件最大1MB, 不能为空文件
      if (file.getSize() > 1 * 1024 * 1024 || file.getSize() <= 0) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "文件非法");
      }
      try {
        file.transferTo(tempPath);
      } catch (Exception e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "上传文件失败");
      }
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    DecryptSettingBO decryptSettingBO = new DecryptSettingBO();
    DecryptSettingBO decryptSetting = new DecryptSettingBO();

    try {
      BeanUtils.copyProperties(modificationVO, decryptSettingBO);

      decryptSetting = decryptService.updateDecryptSetting(id, decryptSettingBO, tempPath,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, decryptSetting, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(id);

  }

  @DeleteMapping("/decrypt-settings/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteDecryptSetting(
      @PathVariable @NotEmpty(message = "删除时传入的id不能为空") String id, HttpServletRequest request) {

    DecryptSettingBO decryptSetting = new DecryptSettingBO();

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    try {
      decryptSetting = decryptService.deleteDecryptSetting(id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, decryptSetting, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(id);
  }

  private static Map<String, Object> decryptSettingBO2Map(DecryptSettingBO decryptSetting) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", decryptSetting.getId());
    map.put("ipAddress", decryptSetting.getIpAddress());
    map.put("port", decryptSetting.getPort());
    map.put("protocol", decryptSetting.getProtocol());
    map.put("certHash", decryptSetting.getCertHash());
    map.put("createTime", decryptSetting.getCreateTime());
    map.put("updateTime", decryptSetting.getUpdateTime());

    return map;
  }

}
