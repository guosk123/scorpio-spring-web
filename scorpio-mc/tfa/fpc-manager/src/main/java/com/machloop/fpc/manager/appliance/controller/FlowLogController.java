package com.machloop.fpc.manager.appliance.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService;
import com.machloop.fpc.manager.appliance.bo.HostGroupBO;
import com.machloop.fpc.manager.appliance.service.FlowLogService;
import com.machloop.fpc.manager.appliance.service.HostGroupService;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月19日, fpc-manager
 */
// @RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class FlowLogController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogController.class);
  @Autowired
  private FlowLogService flowLogService;

  @Autowired
  private HostGroupService hostGroupService;

  @Autowired
  private ScenarioTaskResultService scenarioTaskResultService;

  @GetMapping("/flow-logs")
  @Secured({"PERM_USER"})
  public String queryFlowLogs(FlowLogQueryVO queryVO, String queryTaskId, String startTime,
      String endTime, @RequestParam(required = false, defaultValue = "0") int terminateAfter,
      @RequestParam(required = false, defaultValue = "30") int timeout,
      @RequestParam(required = false, defaultValue = "1") double samplingRate,
      @RequestParam(required = false, defaultValue = "start_time") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      String searchAfter) {
    if (StringUtils.isNotBlank(startTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
    }
    if (StringUtils.isNotBlank(endTime)) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }
    enrichQuery(queryVO);
    String json = flowLogService.queryFlowLogs(queryVO, queryTaskId, terminateAfter, timeout,
        samplingRate, sortProperty, sortDirection, pageSize, searchAfter);

    return transJsonLongToStr(json);
  }

  @GetMapping("/flow-logs/{flowId}")
  public List<Map<String, Object>> queryFlowLogs(
      @PathVariable @NotEmpty(message = "flowId不能为空") String flowId, String inclusiveTime) {
    Date inclusiveTimeDate = DateUtils.parseISO8601Date(inclusiveTime);
    return flowLogService.queryFlowLogs(flowId, inclusiveTimeDate);
  }

  @GetMapping("/flow-logs/as-statistics")
  @Secured({"PERM_USER"})
  public String queryFlowLogsStatistics(FlowLogQueryVO queryVO, String queryTaskId,
      @RequestParam String startTime, @RequestParam String endTime,
      @RequestParam int histogramInterval, @RequestParam String termFieldName,
      @RequestParam(required = false, defaultValue = "10") int termSize,
      @RequestParam(required = false, defaultValue = "0") int terminateAfter,
      @RequestParam(required = false, defaultValue = "30") int timeout,
      @RequestParam(required = false, defaultValue = "1") double samplingRate) {
    if (StringUtils.isNotBlank(startTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
    }
    if (StringUtils.isNotBlank(endTime)) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }
    enrichQuery(queryVO);
    String json = flowLogService.queryFlowLogStatistics(queryVO, queryTaskId, histogramInterval,
        termFieldName, termSize, terminateAfter, timeout, samplingRate);
    return transJsonLongToStr(json);
  }

  @GetMapping("/flow-logs/as-statistics-groupby-ip")
  @Secured({"PERM_USER"})
  public String queryFlowLogsStatisticsGroupByIp(FlowLogQueryVO queryVO, String queryTaskId,
      @RequestParam String startTime, @RequestParam String endTime,
      @RequestParam(required = false, defaultValue = "1000") int termSize,
      @RequestParam(required = false, defaultValue = "30") int timeout,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(startTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
    }
    if (StringUtils.isNotBlank(endTime)) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }
    enrichQuery(queryVO);
    String json = flowLogService.queryFlowLogStatisticsGroupByIp(queryVO, queryTaskId, termSize,
        timeout, sortProperty, sortDirection);
    return transJsonLongToStr(json);
  }

  @GetMapping("/flow-logs/{flow-packet-id}/analysis")
  @Secured({"PERM_USER"})
  public void analyzeFlowPacket(@PathVariable(name = "flow-packet-id") String flowPacketId,
      @RequestParam String type, @RequestParam String parameter, @RequestParam long startTime,
      @RequestParam long endTime, HttpServletRequest request, HttpServletResponse response) {

    flowLogService.analyzeFlowPacket(flowPacketId, new Date(startTime), new Date(endTime), type,
        parameter, request, response);
  }

  @GetMapping("/flow-logs/as-export")
  @Secured({"PERM_USER"})
  public void exportFlowLogs(FlowLogQueryVO queryVO, String startTime, String endTime,
      @RequestParam(required = false, defaultValue = "0") int terminateAfter,
      @RequestParam(required = false, defaultValue = "30") int timeout,
      @RequestParam(required = false, defaultValue = "1") double samplingRate,
      @RequestParam(required = false, defaultValue = "start_time") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection, String cursor,
      HttpServletRequest request, HttpServletResponse response) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, "flow_logs.csv"));
    response.resetBuffer();

    if (StringUtils.isNotBlank(startTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
    }
    if (StringUtils.isNotBlank(endTime)) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }
    enrichQuery(queryVO);
    try (OutputStream out = response.getOutputStream();) {
      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

      flowLogService.exportFlowLogs(queryVO, terminateAfter, timeout, samplingRate, sortProperty,
          sortDirection, out);

      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export flow logs error ", e);
    }
  }

  @GetMapping("/flow-logs/{flow-packet-id}/file-urls")
  @Secured({"PERM_USER"})
  public String fetchFlowPacketFileUrls(@PathVariable(name = "flow-packet-id") String flowPacketId,
      @RequestParam long startTime, @RequestParam long endTime, HttpServletRequest request) {

    return flowLogService.fetchFlowLogPacketFileUrls(flowPacketId, new Date(startTime),
        new Date(endTime), request.getRemoteAddr());
  }

  @PostMapping("/flow-logs/cancel-query-task")
  @Secured({"PERM_USER"})
  public void cancelFlowLogsQueryTask(@RequestParam String queryTaskId) {
    flowLogService.cancelFlowLogsQueryTask(queryTaskId);
  }

  private void enrichQuery(final FlowLogQueryVO queryVO) {
    // 如果IP条件以hostgroup开头, 需要根据id查询hostgroup的所有地址
    if (StringUtils.startsWith(queryVO.getIpInitiator(),
        ManagerConstants.METADATA_CONDITION_IP_HOSTGROUP_PREFIX)) {
      HostGroupBO hostGroupBO = hostGroupService.queryHostGroup(StringUtils.substringAfter(
          queryVO.getIpInitiator(), ManagerConstants.METADATA_CONDITION_IP_HOSTGROUP_PREFIX));
      queryVO.setIpInitiator(hostGroupBO.getIpAddress());
    }
    if (StringUtils.startsWith(queryVO.getIpResponder(),
        ManagerConstants.METADATA_CONDITION_IP_HOSTGROUP_PREFIX)) {
      HostGroupBO hostGroupBO = hostGroupService.queryHostGroup(StringUtils.substringAfter(
          queryVO.getIpResponder(), ManagerConstants.METADATA_CONDITION_IP_HOSTGROUP_PREFIX));
      queryVO.setIpResponder(hostGroupBO.getIpAddress());
    }
    // 如果ID条件以analysis-result开头, 需要根据id查询分析任务结果对应的id
    if (StringUtils.startsWith(queryVO.getId(),
        ManagerConstants.METADATA_CONDITION_ID_ANALYSIS_RESULT_PREFIX)) {
      queryVO.setId(scenarioTaskResultService.queryScenarioTaskResultIds(queryVO.getId()));
    }
  }

  private String transJsonLongToStr(String json) {
    if (StringUtils.isBlank(json)) {
      return json;
    }
    // js处理大数会丢失精度, 将数值型改为字符串
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      SimpleModule simpleModule = new SimpleModule();
      simpleModule.addSerializer(BigInteger.class, ToStringSerializer.instance);
      simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
      simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
      objectMapper.registerModule(simpleModule);
      return objectMapper.writeValueAsString(JsonHelper.deserialize(json, Map.class));
    } catch (JsonProcessingException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "查询失败");
    }
  }
}
