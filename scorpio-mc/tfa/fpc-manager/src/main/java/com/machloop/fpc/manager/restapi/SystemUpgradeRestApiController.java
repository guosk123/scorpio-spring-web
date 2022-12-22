package com.machloop.fpc.manager.restapi;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Min;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.system.service.SystemUpgradeService;

/**
 * @author chenshimiao
 * 
 * create at 2022/10/12 4:10 PM,cms
 * @version 1.0
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-v1/system")
public class SystemUpgradeRestApiController {

  private static final String FILESUFFIX = ".tar.gz";
  @Autowired
  private SystemUpgradeService systemUpgradeService;

  @Autowired
  private UserService userService;

  @GetMapping("/upgrade/infos")
  @RestApiSecured
  public Map<String, String> queryCurrentUpgradeVersion() {

    return systemUpgradeService.queryCurrentUpgradeVersion();
  }

  @GetMapping("/upgrade/logs")
  @RestApiSecured
  public Map<String, Object> queryUpgradeLogs(
      @RequestParam(defaultValue = "0") @Min(value = 0, message = "游标最小为0") long cursor) {

    return systemUpgradeService.queryUpgradeLogs(cursor);
  }

  @PostMapping("/upgrade")
  @RestApiSecured
  public RestAPIResultVO systemUpgrade(@RequestParam MultipartFile file,
      HttpServletRequest request) {
    try {
      String matchingIllegalCharacters = TextUtils
          .matchingIllegalCharacters(file.getOriginalFilename());
      if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            String.format("文件名存在非法字符： [%s]", matchingIllegalCharacters));
      }
      if (!file.getOriginalFilename().contains(FILESUFFIX)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            String.format("系统更新文件类型错误"));
      }
    } catch (BusinessException e) {
      return RestAPIResultVO.resultFailed(e);
    }


    try {
      systemUpgradeService.systemUpgrade(file);
    } catch (BusinessException e) {
      return RestAPIResultVO.resultFailed(e);
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(),
        "执行系统升级。升级时间为：" + DateUtils.now() + "。 升级版本为：" + file.getOriginalFilename());

    return RestAPIResultVO.resultSuccess("正在执行升级文件。");
  }
}
