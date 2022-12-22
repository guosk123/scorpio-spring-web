package com.machloop.fpc.cms.center.restapi;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.cms.center.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.cms.npm.analysis.service.SuricataRuleClasstypeService;
import com.machloop.fpc.cms.npm.analysis.service.SuricataRuleService;

/**
 * @author chenshimiao
 * 
 * create at 2022/10/26 11:36 AM,cms
 * @version 1.0
 * 
 * 用来接收安全分析文件使用
 */

@Validated
@RestController
@RequestMapping("/restapi/fpc-cms-v1/appliance")
public class SuricataRuleRestApiController {

  @Autowired
  private UserService userService;

  @Autowired
  private SuricataRuleService suricataRuleService;

  @Autowired
  private SuricataRuleClasstypeService suricataRuleClasstypeService;

  /**
   * 下发使用restapi
   * @param file
   * @param classtypeId
   * @param source
   * @param request
   * @return
   */
  @PostMapping("/suricata-rules/as-import/issued")
  @RestApiSecured
  public RestAPIResultVO importIssuedSuricataRules(@RequestParam MultipartFile file,
      String classtypeId, String source, HttpServletRequest request) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名称存在非法字符：[%s]", matchingIllegalCharacters));
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    int importSuricataRules = 0;
    try {
      importSuricataRules = suricataRuleService.importIssuedSuricataRules(file, classtypeId, source,
          userBO.getId(), false);
    } catch (BusinessException e) {
      return RestAPIResultVO.resultFailed(e);
    }


    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(),
        "导入" + importSuricataRules + "条安全分析规则");

    return RestAPIResultVO.resultSuccess("导入成功。");
  }

  /**
   * 提供引擎端导入suricata使用
   * @param file
   * @param classtypeId
   * @param source
   * @param request
   * @return
   */
  @PostMapping("/suricata-rules/as-engine-import")
  @RestApiSecured
  public RestAPIResultVO importSuricataRules(@RequestParam MultipartFile file, String classtypeId,
      String source, HttpServletRequest request) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名称存在非法字符：[%s]", matchingIllegalCharacters));
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    int importSuricataRules = 0;
    try {
      /**
       * file        : 文件
       * classtypeId ：规则分类Id
       * source      ：来源
       * userId      ：导入者Id
       * isEngine    ：是否为引擎端写入
       */
      importSuricataRules = suricataRuleService.importIssuedSuricataRules(file, classtypeId, source,
          userBO.getId(), true);
    } catch (BusinessException e) {
      return RestAPIResultVO.resultFailed(e);
    }

    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(),
        "导入" + importSuricataRules + "条安全分析规则");

    return RestAPIResultVO.resultSuccess("导入成功。");
  }

  /**
   * 提供引擎端来导入规则分类使用
   * @param file
   * @param request
   * @return
   */
  @PostMapping("/classtype/as-import")
  @RestApiSecured
  public RestAPIResultVO importClasstype(@RequestParam MultipartFile file,
      HttpServletRequest request) {
    String matchingIllegalCharacters = TextUtils
        .matchingIllegalCharacters(file.getOriginalFilename());
    if (StringUtils.isNotBlank(matchingIllegalCharacters)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          String.format("文件名称存在非法字符：[%s]", matchingIllegalCharacters));
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    int importClasstypeIds = 0;
    try {
      importClasstypeIds = suricataRuleClasstypeService.importClasstypes(file, userBO.getId());
    } catch (BusinessException e) {
      return RestAPIResultVO.resultFailed(e);
    }

    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(),
        "导入" + importClasstypeIds + "条规则分类");

    return RestAPIResultVO.resultSuccess("导入成功。");
  }
}
