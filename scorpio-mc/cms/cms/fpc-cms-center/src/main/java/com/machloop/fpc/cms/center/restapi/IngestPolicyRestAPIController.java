package com.machloop.fpc.cms.center.restapi;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.cms.center.appliance.bo.IngestPolicyBO;
import com.machloop.fpc.cms.center.appliance.service.IngestPolicyService;
import com.machloop.fpc.cms.center.restapi.vo.IngestPolicyVO;
import com.machloop.fpc.cms.center.restapi.vo.IngestPolicyVO.FilterTupleBO;
import com.machloop.fpc.cms.center.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author guosk
 *
 * create at 2021年6月26日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-cms-v1/appliance")
public class IngestPolicyRestAPIController {

  private static final int MAX_FILTER_TUPLE_NUMBER = 10;
  private static final Range<Integer> RANGE_FILTER_TUPLE_VLANID = Range.closed(0, 4094);

  private static final List<String> PROTOCOLS = Lists.newArrayList("TCP", "UDP");

  @Autowired
  private IngestPolicyService ingestPolicyService;
  @Autowired
  private UserService userService;

  @GetMapping("/ingest-policies")
  @RestApiSecured
  public RestAPIResultVO queryIngestPolicys() {
    List<IngestPolicyBO> ingestPolicys = ingestPolicyService.queryIngestPolicys();

    List<Map<String, Object>> resultList = ingestPolicys.stream()
        .map(ingestPolicy -> ingestPolicyBO2Map(ingestPolicy)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  @GetMapping("/ingest-policies/{id}")
  @RestApiSecured
  public RestAPIResultVO queryIngestPolicy(@PathVariable String id) {
    IngestPolicyBO ingestPolicy = ingestPolicyService.queryIngestPolicy(id);

    if (StringUtils.isBlank(ingestPolicy.getId())) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.OBJECT_NOT_FOUND_CODE).msg("捕获规则不存在")
          .build();
    }

    return RestAPIResultVO.resultSuccess(ingestPolicyBO2Map(ingestPolicy));
  }

  @PostMapping("/ingest-policies")
  @RestApiSecured
  public RestAPIResultVO saveIngestPolicy(@RequestBody @Validated IngestPolicyVO ingestPolicyVO,
      BindingResult bindingResult, HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, ingestPolicyVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
    try {
      BeanUtils.copyProperties(ingestPolicyVO, ingestPolicyBO);
      ingestPolicyBO.setExceptBpf(StringUtils.defaultIfBlank(ingestPolicyVO.getExceptBpf(), ""));
      ingestPolicyBO.setExceptTuple(JsonHelper.serialize(ingestPolicyVO.getExceptTuple(), false));
      ingestPolicyBO
          .setDescription(StringUtils.defaultIfBlank(ingestPolicyBO.getDescription(), ""));
      ingestPolicyBO = ingestPolicyService.saveIngestPolicy(ingestPolicyBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, ingestPolicyBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(ingestPolicyBO.getId());
  }

  @PutMapping("/ingest-policies/{id}")
  @RestApiSecured
  public RestAPIResultVO updateIngestPolicy(@PathVariable String id,
      @RequestBody @Validated IngestPolicyVO ingestPolicyVO, BindingResult bindingResult,
      HttpServletRequest request) {
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, ingestPolicyVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
    try {
      BeanUtils.copyProperties(ingestPolicyVO, ingestPolicyBO);
      ingestPolicyBO.setExceptTuple(JsonHelper.serialize(ingestPolicyVO.getExceptTuple(), false));
      ingestPolicyBO = ingestPolicyService.updateIngestPolicy(id, ingestPolicyBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, ingestPolicyBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/ingest-policies/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteIngestPolicy(@PathVariable String id, HttpServletRequest request) {
    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    IngestPolicyBO ingestPolicyBO = null;
    try {
      ingestPolicyBO = ingestPolicyService.deleteIngestPolicy(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, ingestPolicyBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult,
      IngestPolicyVO ingestPolicyVO) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    List<FilterTupleBO> exceptTuple = ingestPolicyVO.getExceptTuple();
    if (CollectionUtils.isEmpty(exceptTuple)) {
      return null;
    }

    if (exceptTuple.size() > MAX_FILTER_TUPLE_NUMBER) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
          .msg("流过滤条件数量超过" + MAX_FILTER_TUPLE_NUMBER + "组").build();
    }

    HashSet<FilterTupleBO> removal = Sets.newHashSet(exceptTuple);
    if (removal.size() < exceptTuple.size()) {
      return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE).msg("存在重复的流过滤组合条件")
          .build();
    }

    for (FilterTupleBO tuple : exceptTuple) {

      if (tuple.isEmpty()) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg("单组流过滤条件内容不能为空").build();
      }

      String sourceIp = tuple.getSourceIp();
      if (StringUtils.isNotBlank(sourceIp) && !NetworkUtils.isInetAddress(sourceIp)
          && !NetworkUtils.isCidr(sourceIp)) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg("不合法的源IP：" + sourceIp).build();
      }
      tuple.setSourceIp(StringUtils.defaultIfBlank(sourceIp, ""));

      String destIp = tuple.getDestIp();
      if (StringUtils.isNotBlank(destIp) && !NetworkUtils.isInetAddress(destIp)
          && !NetworkUtils.isCidr(destIp)) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg("不合法的目的IP：" + sourceIp).build();
      }
      tuple.setDestIp(StringUtils.defaultIfBlank(destIp, ""));

      String sourcePort = tuple.getSourcePort();
      if (StringUtils.isNotBlank(sourcePort) && !NetworkUtils.isInetAddressPort(sourcePort)) {
        String[] range = StringUtils.split(sourcePort, "-");
        if (range.length != 2 || !NetworkUtils.isInetAddressPort(range[0])
            || !NetworkUtils.isInetAddressPort(range[1])
            || (Integer.parseInt(range[0]) >= Integer.parseInt(range[1]))) {
          return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
              .msg("不合法的源端口：" + sourcePort).build();
        }
      }
      tuple.setSourcePort(StringUtils.defaultIfBlank(sourcePort, ""));

      String destPort = tuple.getDestPort();
      if (StringUtils.isNotBlank(destPort) && !NetworkUtils.isInetAddressPort(destPort)) {
        String[] range = StringUtils.split(destPort, "-");
        if (range.length != 2 || !NetworkUtils.isInetAddressPort(range[0])
            || !NetworkUtils.isInetAddressPort(range[1])
            || (Integer.parseInt(range[0]) >= Integer.parseInt(range[1]))) {
          return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
              .msg("不合法的目的端口：" + sourcePort).build();
        }
      }
      tuple.setDestPort(StringUtils.defaultIfBlank(destPort, ""));

      String protocol = tuple.getProtocol();
      if (StringUtils.isNotBlank(protocol) && !PROTOCOLS.contains(protocol.toUpperCase())) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg("不合法的传输层协议号：" + protocol).build();
      }
      tuple.setProtocol(StringUtils.defaultIfBlank(protocol, ""));

      boolean isOk = true;
      String vlanIdStr = tuple.getVlanId();
      if (StringUtils.isNotBlank(vlanIdStr)) {
        try {
          int vlanId = Integer.parseInt(vlanIdStr);
          if (!RANGE_FILTER_TUPLE_VLANID.contains(vlanId)) {
            isOk = false;
          }
        } catch (NumberFormatException e) {
          String[] range = StringUtils.split(vlanIdStr, "-");
          try {
            int vlanId1 = Integer.parseInt(range[0]);
            int vlanId2 = Integer.parseInt(range[1]);
            if (!RANGE_FILTER_TUPLE_VLANID.contains(vlanId1)
                || !RANGE_FILTER_TUPLE_VLANID.contains(vlanId2)) {
              isOk = false;
            }
            if (vlanId1 >= vlanId2) {
              isOk = false;
            }
          } catch (NumberFormatException nfException) {
            isOk = false;
          } catch (IndexOutOfBoundsException ioobException) {
            isOk = false;
          }
        }
      }
      if (!isOk) {
        return new RestAPIResultVO.Builder(FpcCmsConstants.ILLEGAL_PARAMETER_CODE)
            .msg("不合法的vlanid：" + vlanIdStr).build();
      }
      tuple.setVlanId(StringUtils.defaultIfBlank(vlanIdStr, ""));
    }

    ingestPolicyVO.setExceptTuple(exceptTuple);
    return null;
  }

  private Map<String, Object> ingestPolicyBO2Map(IngestPolicyBO ingestPolicyBO) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", ingestPolicyBO.getId());
    map.put("name", ingestPolicyBO.getName());
    map.put("defaultAction", ingestPolicyBO.getDefaultAction());
    map.put("deduplication", ingestPolicyBO.getDeduplication());
    map.put("exceptBpf", ingestPolicyBO.getExceptBpf());
    if (StringUtils.isNotBlank(ingestPolicyBO.getExceptTuple())) {
      List<FilterTupleBO> exceptTuple = JsonHelper.deserialize(ingestPolicyBO.getExceptTuple(),
          new TypeReference<List<FilterTupleBO>>() {
          }, false);
      map.put("exceptTuple", exceptTuple);
    } else {
      map.put("exceptTuple", Lists.newArrayList());
    }
    map.put("description", ingestPolicyBO.getDescription());
    map.put("createTime", ingestPolicyBO.getCreateTime());

    return map;
  }

}
