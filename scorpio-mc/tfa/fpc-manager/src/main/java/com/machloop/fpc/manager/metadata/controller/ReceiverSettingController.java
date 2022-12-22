package com.machloop.fpc.manager.metadata.controller;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.metadata.service.ReceiverSettingService;
import com.machloop.fpc.manager.metadata.vo.ReceiverSettingVO;

@RestController
@RequestMapping("/webapi/fpc-v1/metadata")
public class ReceiverSettingController {

  @Value("${file.kafka.keytab.path}")
  private String keytabFilePath;

  @Autowired
  private ReceiverSettingService receiverSettingService;

  @GetMapping("/receiver-settings")
  @Secured({"PERM_USER"})
  public ReceiverSettingVO queryReceiverSetting() {
    return receiverSettingService.queryReceiverSetting();
  }

  @PutMapping("/receiver-settings")
  @Secured({"PERM_USER"})
  public void saveReceiverSetting(ReceiverSettingVO receiverSettingVO) {
    receiverSettingVO.setOperatorId(LoggedUserContext.getCurrentUser().getId());
    receiverSettingService.saveOrUpdateReceiverSetting(receiverSettingVO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, receiverSettingVO);
  }

  @PostMapping("/receiver-settings/keytab")
  @Secured({"PERM_USER"})
  public String importKeytabFile(@RequestParam MultipartFile file) {
    Path keytabFile = Paths.get(keytabFilePath);

    try {
      file.transferTo(keytabFile.toFile());
    } catch (IllegalStateException | IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "上传keytab文件失败");
    }

    return keytabFilePath;
  }

}
