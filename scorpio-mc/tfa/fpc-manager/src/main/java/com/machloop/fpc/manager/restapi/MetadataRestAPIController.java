package com.machloop.fpc.manager.restapi;

import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clickhouse.client.internal.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.service.ProtocolMapKeysService;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author guosk
 *
 * create at 2021年6月28日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/metadata")
public class MetadataRestAPIController {

  private static final List<String> CONTAIN_MAP_METADATA = Lists.newArrayList("sip", "ldap", "db2");

  @Autowired
  private Map<String, LogRecordService<?>> logRecordServiceMap;

  @Autowired
  private ProtocolMapKeysService protocolMapKeysService;

  @Autowired
  private SaService saService;

  @GetMapping("/protocol-map-keys")
  @RestApiSecured
  public RestAPIResultVO queryProtocolMapKeys(@RequestParam String protocol) {

    if (StringUtils.isBlank(protocol)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("元数据协议不能为空")
          .build();
    }
    
    if (!CONTAIN_MAP_METADATA.contains(protocol)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("输入的元数据协议不存在map类型的字段").build();
    }
    Map<String, Set<String>> resultMap = protocolMapKeysService.queryProtocolMapKeys(protocol);
    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/protocol-logs")
  @RestApiSecured
  public RestAPIResultVO queryFlowLogs(LogRecordQueryVO queryVO,
      @RequestParam(name = "protocol", required = false) String l7ProtocolName,
      @RequestParam(name = "sessionIds", required = false) String flowIds) {
    Date startTimeDate = null;
    Date endTimeDate = null;
    try {
      if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间不能为空")
            .build();
      }
      // 判断时间范围
      startTimeDate = DateUtils.parseNanoISO8601Date(queryVO.getStartTime());
      endTimeDate = DateUtils.parseNanoISO8601Date(queryVO.getEndTime());
      if (startTimeDate.compareTo(endTimeDate) > 0 || DateUtils
          .afterSecondDate(startTimeDate, Constants.ONE_HOUR_SECONDS).compareTo(endTimeDate) < 0) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg("查询时间范围不合法，有效范围在一小时内").build();
      }
      queryVO.setStartTime(DateUtils.transformDateString(queryVO.getStartTime(),
          "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX", "yyyy-MM-dd HH:mm:ss.SSSSSSSSS", ZoneId.of("UTC")));
      queryVO.setEndTime(DateUtils.transformDateString(queryVO.getEndTime(),
          "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX", "yyyy-MM-dd HH:mm:ss.SSSSSSSSS", ZoneId.of("UTC")));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的查询时间格式")
          .build();
    }
    if (StringUtils.isBlank(l7ProtocolName)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("应用层协议不能为空")
          .build();
    }

    List<String> flowIdList = null;
    if (StringUtils.isNotBlank(flowIds)) {
      flowIdList = CsvUtils.convertCSVToList(flowIds);
      for (String flowId : flowIdList) {
        if (!StringUtils.isNumeric(flowId)) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的会话ID")
              .build();
        }
      }
    }

    if (StringUtils.isNotBlank(queryVO.getDsl())
        && StringUtils.contains(queryVO.getDsl(), "start_time")) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("dsl内请勿包含时间过滤")
          .build();
    }

    // 根据协议获取service方法
    Map<String,
        LogRecordService<?>> serviceMap = logRecordServiceMap.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> StringUtils.lowerCase(
                    StringUtils.substringBetween(entry.getKey(), "protocol", "LogService")),
                Entry::getValue));
    LogRecordService<?> logRecordService = serviceMap.get(StringUtils.lowerCase(l7ProtocolName));
    if (logRecordService == null) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的应用层协议")
          .build();
    }

    List<Map<String, Object>> logRecords = null;
    try {
      if (StringUtils.isNotBlank(queryVO.getPacketFileId())) {
        queryVO.setSourceType(FpcConstants.SOURCE_TYPE_PACKET_FILE);
      }
      logRecords = logRecordService.queryLogRecords(queryVO, flowIdList);

      Map<String, String> appDict = saService.queryAllAppsIdNameMapping().entrySet().stream()
          .collect(Collectors.toMap(entry -> String.valueOf(entry.getKey()), Entry::getValue));
      logRecords.forEach(item -> {
        item.put("application", MapUtils.getString(appDict, item.get("applicationId")));
        item.remove("applicationId");
      });
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    return RestAPIResultVO.resultSuccess(logRecords);
  }

}
