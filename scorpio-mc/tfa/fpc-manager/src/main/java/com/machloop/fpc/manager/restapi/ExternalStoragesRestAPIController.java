package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.ExternalStorageBO;
import com.machloop.fpc.manager.appliance.service.ExternalStorageService;
import com.machloop.fpc.manager.appliance.vo.ExternalStorageCreationVO;
import com.machloop.fpc.manager.appliance.vo.ExternalStorageModificationVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author fengtianyou
 *
 * create at 2021年9月22日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class ExternalStoragesRestAPIController {

  @Autowired
  private ExternalStorageService externalStorageService;

  @Autowired
  private UserService userService;

  @Autowired
  private DictManager dictManager;

  @GetMapping("/external-storages")
  @RestApiSecured
  public RestAPIResultVO queryExternalStorage() {
    List<ExternalStorageBO> list = externalStorageService.queryExternalStorages(null, null);
    return RestAPIResultVO.resultSuccess(list);
  }

  @PostMapping("/external-storages")
  @RestApiSecured
  public RestAPIResultVO saveExternalStorage(
      @RequestBody @Validated ExternalStorageCreationVO externalStorageVO,
      BindingResult bindingResult, HttpServletRequest request) {
    ExternalStorageBO externalStorageBO = new ExternalStorageBO();
    BeanUtils.copyProperties(externalStorageVO, externalStorageBO);
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, externalStorageBO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    try {
      externalStorageBO = externalStorageService.saveExternalStorage(externalStorageBO,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, externalStorageBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(externalStorageBO);
  }

  @PutMapping("/external-storages/{id}")
  @RestApiSecured
  public RestAPIResultVO updateExternalStorage(@PathVariable String id,
      @RequestBody @Validated ExternalStorageModificationVO externalStorageVO,
      BindingResult bindingResult, HttpServletRequest request) {
    ExternalStorageBO externalStorageBO = new ExternalStorageBO();
    BeanUtils.copyProperties(externalStorageVO, externalStorageBO);
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, externalStorageBO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    try {
      externalStorageBO = externalStorageService.updateExternalStorage(id, externalStorageBO,
          userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, externalStorageBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/external-storages/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteExternalStorage(@PathVariable String id,
      HttpServletRequest request) {
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    ExternalStorageBO externalStorageBO = new ExternalStorageBO();
    try {
      externalStorageBO = externalStorageService.deleteExternalStorage(id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, externalStorageBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult,
      ExternalStorageBO externalStorageBO) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 校验用途
    Map<String, String> usageMap = dictManager.getBaseDict().getItemMap("external_storage_usage");
    if (!usageMap.containsKey(externalStorageBO.getUsage())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(externalStorageBO.getType() + "格式非法, 请输入服务器用途").build();
    }

    // 校验服务器类型
    Map<String, String> typeMap = dictManager.getBaseDict().getItemMap("external_storage_type");
    if (StringUtils.equals(externalStorageBO.getUsage(), "transmit_task")) {
      if (!StringUtils.equals(externalStorageBO.getType(), "SMB")) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(externalStorageBO.getType() + "格式非法, 请输入服务器类型").build();
      }
    } else {
      if (!typeMap.containsKey(externalStorageBO.getType())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(externalStorageBO.getType() + "格式非法, 请输入服务器类型").build();
      }
    }

    // 校验ip地址
    if (!NetworkUtils.isInetAddress(externalStorageBO.getIpAddress(), NetworkUtils.IpVersion.V4)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(externalStorageBO.getIpAddress() + "格式非法, 请输入正确的IP地址").build();
    }

    if (!(externalStorageBO.getPort() <= 65535 && externalStorageBO.getPort() > 0)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(externalStorageBO.getPort() + "格式非法, 请输入正确的端口").build();
    }

    return null;
  }
}
