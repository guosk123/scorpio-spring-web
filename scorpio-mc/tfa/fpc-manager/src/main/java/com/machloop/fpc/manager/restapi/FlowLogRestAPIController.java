package com.machloop.fpc.manager.restapi;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.service.FlowLogServiceCk;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author guosk
 *
 * create at 2021年6月28日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class FlowLogRestAPIController {

  private static final String TIME_OLD_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX";
  private static final String TIME_NEW_PATTERN = "yyyy-MM-dd HH:mm:ss.SSSSSSSSS";

  @Autowired
  private FlowLogServiceCk flowLogService;

  @GetMapping("/flow-logs")
  @RestApiSecured
  public RestAPIResultVO queryFlowLogs(FlowLogQueryVO queryVO,
      @RequestParam(name = "protocol", required = false) String l7ProtocolName,
      @RequestParam(name = "sessionIds", required = false) String flowIds) {

    try {
      if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间不能为空")
            .build();
      }
      // 判断时间范围
      Date startTimeDate = DateUtils.parseNanoISO8601Date(queryVO.getStartTime());
      Date endTimeDate = DateUtils.parseNanoISO8601Date(queryVO.getEndTime());
      if (startTimeDate.compareTo(endTimeDate) > 0 || DateUtils
          .afterSecondDate(startTimeDate, Constants.ONE_HOUR_SECONDS).compareTo(endTimeDate) < 0) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg("查询时间范围不合法，有效范围在一小时内").build();
      }
      queryVO.setStartTime(transformDateStringToZeroTimeZone(queryVO.getStartTime(),
          StringUtils.isBlank(flowIds) ? 0 : -1));
      queryVO.setEndTime(transformDateStringToZeroTimeZone(queryVO.getEndTime(),
          StringUtils.isBlank(flowIds) ? 0 : 1));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的查询时间格式")
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
        && StringUtils.contains(queryVO.getDsl(), "report_time")) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("dsl内请勿包含时间过滤")
          .build();
    }

    List<Map<String, Object>> flowLogs = null;
    try {
      if (StringUtils.isNotBlank(queryVO.getPacketFileId())) {
        queryVO.setSourceType(FpcConstants.SOURCE_TYPE_PACKET_FILE);
      }
      flowLogs = flowLogService.queryFlowLogsGroupByFlow(queryVO, l7ProtocolName, flowIdList);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    return RestAPIResultVO.resultSuccess(flowLogs);
  }

  private String transformDateStringToZeroTimeZone(String time, int hour) {
    if (StringUtils.isBlank(time)) {
      return "";
    }
    ZonedDateTime oldZonedDateTime = ZonedDateTime.parse(time,
        DateTimeFormatter.ofPattern(TIME_OLD_PATTERN));
    ZonedDateTime newZonedDateTime = ZonedDateTime.ofInstant(oldZonedDateTime.toInstant(),
        ZoneId.of("UTC"));
    newZonedDateTime = newZonedDateTime.plusHours(hour);
    return newZonedDateTime.format(DateTimeFormatter.ofPattern(TIME_NEW_PATTERN));
  }

}
