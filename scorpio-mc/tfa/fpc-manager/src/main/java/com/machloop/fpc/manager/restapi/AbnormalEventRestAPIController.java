package com.machloop.fpc.manager.restapi;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.npm.analysis.bo.AbnormalEventBO;
import com.machloop.fpc.npm.analysis.service.AbnormalEventService;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventQueryVO;

/**
 * @author guosk
 *
 * create at 2021年8月16日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/analysis")
public class AbnormalEventRestAPIController {

  private static String serverAddress;

  @Autowired
  private AbnormalEventService abnormalEventService;

  @Autowired
  private SystemServerIpService systemServerIpService;

  @GetMapping("/abnormal-events")
  @RestApiSecured
  public RestAPIResultVO queryAbnormalEvents(AbnormalEventQueryVO queryVO) {
    if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间段不能为空")
          .build();
    }

    try {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("时间格式不合法")
          .build();
    }

    List<AbnormalEventBO> abnormalEventList = abnormalEventService.queryAbnormalEvents(queryVO);
    List<Map<String, Object>> resultList = abnormalEventList.stream()
        .map(abnormalEvent -> abnormalEventBO2Map(abnormalEvent)).collect(Collectors.toList());

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    resultMap.put("data", resultList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  private Map<String, Object> abnormalEventBO2Map(AbnormalEventBO abnormalEvent) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("timestamp", abnormalEvent.getStartTime());
    map.put("networkId", abnormalEvent.getNetworkId());
    map.put("type", abnormalEvent.getType());
    map.put("content", abnormalEvent.getContent());
    map.put("description", abnormalEvent.getDescription());
    map.put("srcIp", abnormalEvent.getSrcIp());
    map.put("destIp", abnormalEvent.getDestIp());
    map.put("destPort", abnormalEvent.getDestPort());
    map.put("l7ProtocolId", abnormalEvent.getL7ProtocolId());
    map.put("countryIdInitiator", abnormalEvent.getCountryIdInitiator());
    map.put("provinceIdInitiator", abnormalEvent.getProvinceIdInitiator());
    map.put("cityIdInitiator", abnormalEvent.getCityIdInitiator());
    map.put("countryIdResponder", abnormalEvent.getCountryIdResponder());
    map.put("provinceIdResponder", abnormalEvent.getProvinceIdResponder());
    map.put("cityIdResponder", abnormalEvent.getCityIdResponder());

    return map;
  }

}
