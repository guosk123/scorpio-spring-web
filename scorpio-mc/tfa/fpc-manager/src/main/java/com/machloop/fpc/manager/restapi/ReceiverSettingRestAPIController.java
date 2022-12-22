package com.machloop.fpc.manager.restapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.metadata.service.ReceiverSettingService;
import com.machloop.fpc.manager.metadata.vo.ReceiverSettingVO;
import com.machloop.fpc.manager.restapi.vo.ReceiverSettingRestAPIVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author fengtianyou
 * 
 * create at 2021年9月23日, fpc-manager
 */

@RestController
@RequestMapping("/restapi/fpc-v1/metadata")
public class ReceiverSettingRestAPIController {

  @Value("${file.kafka.keytab.path}")
  private String keytabFilePath;

  @Autowired
  private UserService userService;

  @Autowired
  private ReceiverSettingService receiverSettingService;

  @Autowired
  private SaProtocolService saProtocolService;

  @GetMapping("/receiver-settings")
  @RestApiSecured
  public RestAPIResultVO queryReceiverSetting() {
    ReceiverSettingVO receiverSettingVO = receiverSettingService.queryReceiverSetting();
    ReceiverSettingRestAPIVO receiverSettingRestAPIVO = covertVO(receiverSettingVO);
    return RestAPIResultVO.resultSuccess(receiverSettingRestAPIVO);
  }

  @PutMapping("receiver-settings")
  @RestApiSecured
  public RestAPIResultVO saveReceiverSetting(
      @RequestBody @Validated ReceiverSettingRestAPIVO receiverSettingVO,
      BindingResult bindingResult, HttpServletRequest request) {
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, receiverSettingVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }
    ReceiverSettingVO convertedReceiverSettingVO = covertRestAPIVO(receiverSettingVO);
    ReceiverSettingVO resultVO = new ReceiverSettingVO();
    try {
      resultVO = receiverSettingService.saveOrUpdateReceiverSetting(convertedReceiverSettingVO);
    } catch (BusinessException exception) {

      return RestAPIResultVO.resultFailed(exception);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, receiverSettingVO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(resultVO);
  }

  @PostMapping("/receiver-settings/keytab")
  @RestApiSecured
  public RestAPIResultVO importKeytabFile(@RequestParam MultipartFile file) {
    Path keytabFile = Paths.get(keytabFilePath);
    try {
      file.transferTo(keytabFile.toFile());
    } catch (IllegalStateException | IOException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_API_INVOKE_ERROR, "上传keytab文件失败");
    }

    return RestAPIResultVO.resultSuccess(keytabFilePath);
  }

  // 转换VO类型
  private ReceiverSettingVO covertRestAPIVO(ReceiverSettingRestAPIVO receiverSettingRestAPIVO) {
    ReceiverSettingVO receiverSettingVO = new ReceiverSettingVO();
    BeanUtils.copyProperties(receiverSettingRestAPIVO, receiverSettingVO);
    String protocolTopic = JsonHelper.serialize(receiverSettingRestAPIVO.getProtocolTopic(), false);
    receiverSettingVO.setProtocolTopic(protocolTopic);
    return receiverSettingVO;
  }

  private ReceiverSettingRestAPIVO covertVO(ReceiverSettingVO receiverSettingVO) {
    ReceiverSettingRestAPIVO receiverSettingRestAPIVO = new ReceiverSettingRestAPIVO();
    BeanUtils.copyProperties(receiverSettingVO, receiverSettingRestAPIVO);
    Map<String, String> protocolMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(receiverSettingVO.getProtocolTopic())) {
      protocolMap = JsonHelper.deserialize(receiverSettingVO.getProtocolTopic(),
          new TypeReference<Map<String, Object>>() {
          }, false);
    }

    receiverSettingRestAPIVO.setProtocolTopic(protocolMap);
    return receiverSettingRestAPIVO;
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult,
      ReceiverSettingRestAPIVO receiverSettingVO) {
    // 初步校验
    if (bindingResult.hasErrors()) {

      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 日志类型校验
    if (MapUtils.isEmpty(receiverSettingVO.getProtocolTopic())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(receiverSettingVO.getProtocolTopic() + "协议不能为空").build();
    }


    List<String> collect = saProtocolService.queryProtocols().stream()
        .filter(item -> MapUtils.getIntValue(item, "protocolId") <= 255)
        .map(item -> MapUtils.getString(item, "protocolId")).collect(Collectors.toList());
    collect.add("645");
    collect.add("flow_log");
    for (Map.Entry<String, String> entry : receiverSettingVO.getProtocolTopic().entrySet()) {
      if (!collect.contains(entry.getKey())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(entry.toString() + "格式非法, 请输入正确的应用层协议id").build();
      }
      if (StringUtils.isBlank(entry.getValue())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(entry.toString() + "Kafka Topic不能为空").build();
      }
    }
    // 节点地址校验
    String[] addressEntry = StringUtils.split(receiverSettingVO.getReceiverAddress(), ",");

    for (String item : addressEntry) {
      String[] addressMap = StringUtils.split(item, ":");
      if (!NetworkUtils.isInetAddress(addressMap[0], NetworkUtils.IpVersion.V4)) {

        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(addressMap[0] + "格式非法, 请输入正确的ip地址").build();
      }
      if (!(Integer.parseInt(addressMap[1]) > 0 && Integer.parseInt(addressMap[1]) <= 65535)) {

        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(addressMap[1] + "格式非法，请输入正确的端口号").build();
      }
      // KERBEROS认证校验
      if (receiverSettingVO.getKerberosCertification().equals("1")) {
        if (StringUtils.isBlank(receiverSettingVO.getSaslKerberosServiceName())) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(receiverSettingVO.getSaslKerberosServiceName() + "saslKerberosServiceName不能为空")
              .build();
        }
        if (StringUtils.isBlank(receiverSettingVO.getSaslKerberosPrincipal())) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(receiverSettingVO.getSaslKerberosPrincipal() + "saslKerberosPrincipal不能为空")
              .build();
        }
        if (!StringUtils.equals(receiverSettingVO.getAuthenticationMechanism(), "GSSAPI")) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(receiverSettingVO.getAuthenticationMechanism() + "authenticationMechanism格式非法")
              .build();
        }
        if (!StringUtils.equals(receiverSettingVO.getSecurityProtocol(), "sasl_plaintext")) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(receiverSettingVO.getSecurityProtocol() + "securityProtocol格式非法").build();
        }
        if (receiverSettingVO.getKeyRestoreTime() < 0) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(receiverSettingVO.getKeyRestoreTime() + "keyRestoreTime格式非法").build();
        }

      } else if (receiverSettingVO.getKerberosCertification().equals("0")) {

      } else {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(receiverSettingVO.getKerberosCertification() + "kerberosCertification格式非法")
            .build();
      }
    }
    return null;
  }
}
