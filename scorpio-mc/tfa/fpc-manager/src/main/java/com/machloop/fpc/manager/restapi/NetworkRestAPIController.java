package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.machloop.fpc.manager.appliance.bo.FilterRuleBO;
import com.machloop.fpc.manager.appliance.service.FilterRuleService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.IngestPolicyBO;
import com.machloop.fpc.manager.appliance.service.IngestPolicyService;
import com.machloop.fpc.manager.restapi.vo.NetworkVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.data.NetworkNetifDO;
import com.machloop.fpc.npm.appliance.service.NetworkService;

/**
 * @author guosk
 *
 * create at 2021年6月23日, fpc-manager
 *
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class NetworkRestAPIController {

  private static final List<
      String> FLOWLOG_EXCEPT_STATISTICS = Lists.newArrayList("interframe", "preamble");
  private static final List<String> FLOWLOG_EXCEPT_STATUS = Lists.newArrayList("syn_sent");

  private static final String NETWORK_POLICY_TYPE_INGEST = "ingest";
  private static final String NETWORK_POLICY_TYPE_STORAGE = "storage";

  @Autowired
  private NetworkService networkService;

  @Autowired
  private UserService userService;

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private IngestPolicyService ingestPolicyService;

  @Autowired
  private FilterRuleService filterRuleService;

  @GetMapping("/networks")
  @RestApiSecured
  public RestAPIResultVO queryNetworks() {
    List<NetworkBO> networks = networkService.queryNetworksWithDetail();
    List<Map<String, Object>> networkMaps = networks.stream().map(network -> networkBO2Map(network))
        .collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(networkMaps);
  }

  @GetMapping("/networks/{id}")
  @RestApiSecured
  public RestAPIResultVO queryNetwork(@PathVariable String id) {
    NetworkBO networkBO = networkService.queryNetwork(id);

    if (StringUtils.isBlank(networkBO.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("网络不存在").build();
    }

    return RestAPIResultVO.resultSuccess(networkBO2Map(networkBO));
  }

  @PostMapping("/networks")
  @RestApiSecured
  public RestAPIResultVO saveNetwork(@RequestBody @Validated NetworkVO networkVO,
      BindingResult bindingResult, HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, networkVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    NetworkBO networkBO = new NetworkBO();
    try {
      BeanUtils.copyProperties(networkVO, networkBO);
      networkBO.setSendPolicyIds(CollectionUtils.isEmpty(networkVO.getSendPolicyIds()) ? ""
          : CsvUtils.convertCollectionToCSV(networkVO.getSendPolicyIds()));
      ExtraSetting extraSetting = new ExtraSetting();
      BeanUtils.copyProperties(networkVO, extraSetting);
      networkBO.setExtraSettings(JsonHelper.serialize(extraSetting, false));
      networkBO = networkService.saveNetwork(networkBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, networkBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(networkBO.getId());
  }

  @PutMapping("/networks/{id}")
  @RestApiSecured
  public RestAPIResultVO updateNetwork(@PathVariable String id,
      @RequestBody @Validated NetworkVO networkVO, BindingResult bindingResult,
      HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, networkVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    NetworkBO networkBO = new NetworkBO();
    try {
      BeanUtils.copyProperties(networkVO, networkBO);
      networkBO.setSendPolicyIds(CollectionUtils.isEmpty(networkVO.getSendPolicyIds()) ? ""
          : CsvUtils.convertCollectionToCSV(networkVO.getSendPolicyIds()));
      ExtraSetting extraSetting = new ExtraSetting();
      BeanUtils.copyProperties(networkVO, extraSetting);
      networkBO.setExtraSettings(JsonHelper.serialize(extraSetting, false));
      networkBO = networkService.updateNetwork(id, networkBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, networkBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @PutMapping("/networks/{id}/policies")
  @RestApiSecured
  public RestAPIResultVO updateNetworkPolicy(@PathVariable String id,
      @RequestBody(required = false) Map<String, String> param, HttpServletRequest request) {
    NetworkBO networkBO = networkService.queryNetwork(id);
    if (StringUtils.isBlank(networkBO.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("网络不存在").build();
    }

    if (MapUtils.isEmpty(param)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("请求参数为空").build();
    }

    String policyType = param.get("policyType");
    String policyId = param.get("policyId");
    if (StringUtils.equals(policyType, NETWORK_POLICY_TYPE_INGEST)) {
      List<String> ingestPolicyList = ingestPolicyService.queryIngestPolicys().stream()
          .map(IngestPolicyBO::getId).collect(Collectors.toList());
      if (!ingestPolicyList.contains(policyId)) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg("不存在的捕获规则:" + policyId).build();
      }
    } else if (StringUtils.equals(policyType, NETWORK_POLICY_TYPE_STORAGE)) {
      List<String> filterRules = filterRuleService.queryFilterRule().stream()
          .map(FilterRuleBO::getId).collect(Collectors.toList());
      if (!filterRules.contains(policyId)) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg("不存在的存储过滤规则:" + policyId).build();
      }
    } else {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的规则类型")
          .build();
    }

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    // 编辑网络策略
    networkService.updateNetworkPolicy(id, policyId, policyType, userBO.getId());

    LogHelper.auditOperate(userBO.getFullname(), userBO.getName(),
        "修改网络策略：网络ID=" + id + ";策略类型=" + policyType + ";策略ID=" + policyId);

    return RestAPIResultVO.resultSuccess(null);
  }

  @DeleteMapping("/networks/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteNetwork(@PathVariable String id, HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    NetworkBO networkBO = null;
    try {
      networkBO = networkService.deleteNetwork(id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, networkBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult, NetworkVO networkVO) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    if (CollectionUtils.isEmpty(networkVO.getNetif())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("业务接口配置不能为空")
          .build();
    }

    // 接口配置
    List<String> vaildNetifDirection = StringUtils.equals(networkVO.getNetifType(),
        FpcConstants.APPLIANCE_NETWORK_UNIDIRECTION_FLOW)
            ? Lists.newArrayList(FpcConstants.APPLIANCE_NETWORK_NETIF_DIRECTION_UPSTREAM,
                FpcConstants.APPLIANCE_NETWORK_NETIF_DIRECTION_DOWNSTREAM)
            : Lists.newArrayList(FpcConstants.APPLIANCE_NETWORK_NETIF_DIRECTION_HYBRID);
    Map<String, Integer> vaildNetifSpecification = deviceNetifService.queryDeviceNetifs().stream()
        .collect(Collectors.toMap(DeviceNetifBO::getName, DeviceNetifBO::getSpecification));
    for (NetworkNetifDO netif : networkVO.getNetif()) {
      if (!vaildNetifDirection.contains(netif.getDirection())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的业务接口方向")
            .build();
      }
      Integer specification = vaildNetifSpecification.get(netif.getNetifName());
      if (specification == null) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("业务接口不存在")
            .build();
      }
      if (specification > 0 && !Range.closed(1, specification).contains(netif.getSpecification())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("业务接口实际带宽非有效范围")
            .build();
      }
    }

    // 流量统计包含帧间隙和前导码
    String flowlogExceptStatistics = networkVO.getFlowlogExceptStatistics();
    if (StringUtils.isNotBlank(flowlogExceptStatistics) && !FLOWLOG_EXCEPT_STATISTICS
        .containsAll(CsvUtils.convertCSVToList(flowlogExceptStatistics))) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的流量统计配置")
          .build();
    }

    // SYN_SENT状态生成会话详单
    String flowlogExceptStatus = networkVO.getFlowlogExceptStatus();
    if (StringUtils.isNotBlank(flowlogExceptStatus)
        && !FLOWLOG_EXCEPT_STATUS.containsAll(CsvUtils.convertCSVToList(flowlogExceptStatus))) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不合法的SYN_SENT状态会话配置").build();
    }

    // 内网IP
    String insideIpAddress = networkVO.getInsideIpAddress();
    if (StringUtils.isNotBlank(insideIpAddress)) {
      List<String> ipList = CsvUtils.convertCSVToList(insideIpAddress);
      for (String ip : ipList) {
        if (StringUtils.contains(ip, "-")) {
          String[] ipRange = StringUtils.split(ip, "-");
          // 起止都是正确的ip
          if (ipRange.length != 2 || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]))
              || !NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]))) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg("不合法的IP格式:" + ip).build();
          }
          if (!(NetworkUtils.isInetAddress(StringUtils.trim(ipRange[0]), IpVersion.V4)
              && NetworkUtils.isInetAddress(StringUtils.trim(ipRange[1]), IpVersion.V4))) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg("不合法的IP格式:" + ip).build();
          }
        } else if (!NetworkUtils.isInetAddress(ip) && !NetworkUtils.isCidr(ip)) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg("不合法的IP格式:" + ip).build();
        }
      }
    }

    // 捕获规则
    List<String> ingestPolicyList = ingestPolicyService.queryIngestPolicys().stream()
        .map(IngestPolicyBO::getId).collect(Collectors.toList());
    if (!ingestPolicyList.contains(networkVO.getIngestPolicyId())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不存在的捕获规则:" + networkVO.getIngestPolicyId()).build();
    }

    // 过滤策略
    List<String> filterRuleIds = filterRuleService.queryFilterRule().stream()
        .map(FilterRuleBO::getId).collect(Collectors.toList());
    if (!filterRuleIds.contains(networkVO.getFilterRuleId())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不存在的存储过滤规则:" + networkVO.getFilterRuleId()).build();
    }

    return null;
  }

  private static Map<String, Object> networkBO2Map(NetworkBO networkBO) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", networkBO.getId());
    map.put("name", networkBO.getName());
    map.put("netifType", networkBO.getNetifType());
    map.put("netif", networkBO.getNetif());
    ExtraSetting extraSetting = JsonHelper.deserialize(networkBO.getExtraSettings(),
        ExtraSetting.class, false);
    map.put("flowlogDefaultAction", extraSetting.getFlowlogDefaultAction());
    map.put("flowlogExceptStatistics", extraSetting.getFlowlogExceptStatistics());
    map.put("flowlogExceptStatus", extraSetting.getFlowlogExceptStatus());
    map.put("metadataDefaultAction", extraSetting.getMetadataDefaultAction());
    map.put("sessionVlanAction", extraSetting.getSessionVlanAction());
    map.put("insideIpAddress", networkBO.getInsideIpAddress());
    map.put("ingestPolicyId", networkBO.getIngestPolicyId());
    map.put("filterRuleId", networkBO.getFilterRuleIds());
    map.put("createTime", networkBO.getCreateTime());

    return map;
  }

  public static class ExtraSetting {

    private String flowlogDefaultAction;
    private String flowlogExceptStatistics;
    private String flowlogExceptStatus;
    private String metadataDefaultAction;
    private String sessionVlanAction;

    @Override
    public String toString() {
      return "ExtraSetting [flowlogDefaultAction=" + flowlogDefaultAction
          + ", flowlogExceptStatistics=" + flowlogExceptStatistics + ", flowlogExceptStatus="
          + flowlogExceptStatus + ", metadataDefaultAction=" + metadataDefaultAction
          + ", sessionVlanAction=" + sessionVlanAction + "]";
    }

    public String getFlowlogDefaultAction() {
      return flowlogDefaultAction;
    }

    public void setFlowlogDefaultAction(String flowlogDefaultAction) {
      this.flowlogDefaultAction = flowlogDefaultAction;
    }

    public String getFlowlogExceptStatistics() {
      return flowlogExceptStatistics;
    }

    public void setFlowlogExceptStatistics(String flowlogExceptStatistics) {
      this.flowlogExceptStatistics = flowlogExceptStatistics;
    }

    public String getFlowlogExceptStatus() {
      return flowlogExceptStatus;
    }

    public void setFlowlogExceptStatus(String flowlogExceptStatus) {
      this.flowlogExceptStatus = flowlogExceptStatus;
    }

    public String getMetadataDefaultAction() {
      return metadataDefaultAction;
    }

    public void setMetadataDefaultAction(String metadataDefaultAction) {
      this.metadataDefaultAction = metadataDefaultAction;
    }

    public String getSessionVlanAction() {
      return sessionVlanAction;
    }

    public void setSessionVlanAction(String sessionVlanAction) {
      this.sessionVlanAction = sessionVlanAction;
    }
  }

}
