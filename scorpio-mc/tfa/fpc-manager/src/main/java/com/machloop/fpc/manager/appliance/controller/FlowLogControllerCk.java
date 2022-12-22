package com.machloop.fpc.manager.appliance.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService;
import com.machloop.fpc.manager.appliance.bo.HostGroupBO;
import com.machloop.fpc.manager.appliance.service.FlowLogServiceCk;
import com.machloop.fpc.manager.appliance.service.HostGroupService;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年2月19日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
public class FlowLogControllerCk {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogControllerCk.class);

  @Autowired
  private FlowLogServiceCk flowLogService;

  @Autowired
  private HostGroupService hostGroupService;

  @Autowired
  private ScenarioTaskResultService scenarioTaskResultService;

  @GetMapping("/flow-logs")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO,
      @Pattern(
          regexp = WebappConstants.TABLE_COLUMNS_PATTERN,
          message = "指定列名不合法,且长度需要大于一个字符") @RequestParam(
              required = false, defaultValue = "*") String columns,
      @RequestParam(required = false, defaultValue = "report_time") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQuery(queryVO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_QUERY, queryVO);

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order(Direction.DESC, "report_time"), new Order("flow_id"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    if (queryVO.getSid() != null || queryVO.getFlowId() != null) {
      List<Map<String, Object>> queryFlowLogsByFlowIds = flowLogService
          .queryFlowLogsByFlowIds(queryId, page, queryVO, columns);
      int maxPageSize = queryFlowLogsByFlowIds.size() < page.getPageSize()
          ? queryFlowLogsByFlowIds.size()
          : page.getPageSize();
      return new PageImpl<>(queryFlowLogsByFlowIds.subList(0, maxPageSize), page,
          queryFlowLogsByFlowIds.size());
    } else {
      return flowLogService.queryFlowLogs(queryId, page, queryVO, columns);
    }
  }

  @GetMapping("/flow-logs/{flowId}")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryFlowLogs(String queryId,
      @PathVariable @NotEmpty(message = "flowId不能为空") String flowId, FlowLogQueryVO queryVO,
      @Pattern(
          regexp = WebappConstants.TABLE_COLUMNS_PATTERN,
          message = "指定列名不合法,且长度需要大于一个字符") @RequestParam(
              required = false, defaultValue = "*") String columns) {
    queryVO.setFlowId(Long.parseLong(flowId));

    // 无实际意义，用来添加参数
    PageRequest pageRequest = new PageRequest(10, 0,
        new Sort(new Order(Direction.DESC, "flow_id")));
    return flowLogService.queryFlowLogsByFlowIds(queryId, pageRequest, queryVO, columns);
  }

  @GetMapping("/flow-logs/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryFlowLogsStatistics(String queryId, FlowLogQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQuery(queryVO);
    Map<String, Object> result = flowLogService.queryFlowLogStatistics(queryId, queryVO);
    return result;
  }

  @GetMapping("/flow-logs/as-statistics-groupby-ip")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryFlowLogsStatisticsGroupByIp(String queryId,
      FlowLogQueryVO queryVO, @RequestParam(required = false, defaultValue = "100") int termSize,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQuery(queryVO);
    Sort sort = new Sort(Direction.fromString(sortDirection), sortProperty);
    List<Map<String, Object>> result = flowLogService.queryFlowLogStatisticsGroupByIp(queryId,
        queryVO, termSize, sort);
    return result;
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
  public void exportFlowLogs(String queryId, FlowLogQueryVO queryVO,
      @Pattern(
          regexp = WebappConstants.TABLE_COLUMNS_PATTERN,
          message = "指定列名不合法,且长度需要大于一个字符") @RequestParam(
              required = false, defaultValue = "*") String columns,
      @RequestParam(required = false, defaultValue = "0") int terminateAfter,
      @RequestParam(required = false, defaultValue = "30") int timeout,
      @RequestParam(required = false, defaultValue = "1") double samplingRate,
      @RequestParam(required = false, defaultValue = "report_time") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      @RequestParam(required = false, defaultValue = "0") int count, String cursor,
      HttpServletRequest request, HttpServletResponse response) {

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order(Sort.Direction.DESC, "report_time"), new Order("flow_id"));

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV) ? "flow_logs.csv"
        : "flow_logs.xlsx";
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, fileName));
    response.resetBuffer();

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQuery(queryVO);
    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }

      flowLogService.exportFlowLogs(queryId, queryVO, columns, terminateAfter, timeout,
          samplingRate, sort, fileType, count, out);

      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/flow-logs/packets")
  @Secured({"PERM_USER"})
  public Map<String, Object> fetchFlowPacketFileUrls(String queryId, FlowLogQueryVO queryVO,
      @RequestParam(
          required = false, defaultValue = FpcConstants.PACKET_FILE_TYPE_PCAP) String fileType,
      @RequestParam(required = false, defaultValue = "report_time") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQuery(queryVO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_QUERY, queryVO);

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order(Sort.Direction.DESC, "report_time"), new Order("flow_id"));

    return flowLogService.fetchFlowLogPacketFileUrls(queryId, fileType, queryVO, sort);
  }

  @GetMapping("/flow-logs/{flow-packet-id}/file-urls")
  @Secured({"PERM_USER"})
  public String fetchFlowPacketFileUrls(@PathVariable(name = "flow-packet-id") String flowPacketId,
      @RequestParam long startTime, @RequestParam long endTime, HttpServletRequest request) {

    return flowLogService.fetchFlowLogPacketFileUrls(flowPacketId, new Date(startTime),
        new Date(endTime), request.getRemoteAddr());
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
}
