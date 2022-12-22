package com.machloop.fpc.manager.knowledge.controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.validation.constraints.NotEmpty;

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

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.knowledge.bo.DecryptSettingBO;
import com.machloop.fpc.manager.knowledge.service.DecryptService;
import com.machloop.fpc.manager.knowledge.vo.DecryptSettingCreationVO;
import com.machloop.fpc.manager.knowledge.vo.DecryptSettingModificationVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月20日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class DecryptController {

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private DecryptService decryptService;

  @GetMapping("/decrypt-settings")
  public List<Map<String, Object>> queryDecryptSettings(String ipAddress, String port,
      String protocol) {
    return decryptService.queryDecryptSettings(ipAddress, port, protocol).stream()
        .map(item -> decryptSettingBO2Map(item)).collect(Collectors.toList());
  }

  @GetMapping("/decrypt-settings/{id}")
  public Map<String, Object> queryDecryptSetting(@PathVariable String id) {
    return decryptSettingBO2Map(decryptService.queryDecryptSetting(id));
  }

  @PostMapping("/decrypt-settings")
  public void saveDecryptSetting(@Validated DecryptSettingCreationVO creationVO,
      @RequestParam MultipartFile file) {
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

    DecryptSettingBO decryptSettingBO = new DecryptSettingBO();
    BeanUtils.copyProperties(creationVO, decryptSettingBO);

    DecryptSettingBO decryptSetting = decryptService.saveDecryptSetting(decryptSettingBO, tempPath,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, decryptSetting);
  }

  @PutMapping("/decrypt-settings/{id}")
  public void updateDecryptSetting(@PathVariable @NotEmpty(message = "修改时传入的id不能为空") String id,
      @Validated DecryptSettingModificationVO modificationVO, MultipartFile file) {

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

    DecryptSettingBO decryptSettingBO = new DecryptSettingBO();
    BeanUtils.copyProperties(modificationVO, decryptSettingBO);

    DecryptSettingBO decryptSetting = decryptService.updateDecryptSetting(id, decryptSettingBO,
        tempPath, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, decryptSetting);
  }

  @DeleteMapping("/decrypt-settings/{id}")
  public void deleteDecryptSetting(@PathVariable @NotEmpty(message = "删除时传入的id不能为空") String id) {
    DecryptSettingBO decryptSetting = decryptService.deleteDecryptSetting(id,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, decryptSetting);
  }

  private static Map<String, Object> decryptSettingBO2Map(DecryptSettingBO decryptSetting) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
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
