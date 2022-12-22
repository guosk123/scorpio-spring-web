package com.machloop.fpc.cms.center.metric.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.cms.center.global.service.SlowQueryService;
import com.machloop.fpc.cms.center.metric.service.MetricFlowlogService;
import com.machloop.fpc.cms.center.metric.service.MetricService;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月27日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/metric")
public class MetricController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricController.class);

  @Autowired
  private MetricService metricService;

  @Autowired
  private MetricFlowlogService metricFlowlogService;

  @Autowired
  private SlowQueryService slowQueryService;

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;

  @GetMapping("/netifs/as-histogram")
  @Secured({"PERM_USER", "PERM_SYS_USER"})
  public List<Map<String, Object>> queryMetricNetifHistograms(@Validated MetricQueryVO queryVO,
      String netifName) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricService.queryMetricNetifHistograms(queryVO, netifName, false);
  }

  @GetMapping("/locations")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricLocations(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricLocations(queryVO, sortProperty, sortDirection);
    } else {
      result = metricService.queryMetricLocations(queryVO, sortProperty, sortDirection);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/locations/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricLocationHistograms(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String countryId,
      @RequestParam(required = false) String provinceId,
      @RequestParam(required = false) String cityId) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricLocationHistograms(queryVO, sortProperty,
          sortDirection, countryId, provinceId, cityId);
    } else {
      result = metricService.queryMetricLocationHistograms(queryVO, sortProperty, sortDirection,
          countryId, provinceId, cityId);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/locations/as-export")
  @Secured({"PERM_USER"})
  public void exportLocations(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      HttpServletRequest request, HttpServletResponse response) {

    // 设置文件导出格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "location_logs.csv"
        : "location_logs.xlsx";
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

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
        metricFlowlogService.exportLocations(queryVO, sortProperty, sortDirection, fileType, out);
      } else {
        metricService.exportLocations(queryVO, sortProperty, sortDirection, fileType, out);
      }

      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/applications")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricApplications(@Validated MetricQueryVO queryVO,
      @RequestParam(
          required = false,
          defaultValue = FpcCmsConstants.METRIC_TYPE_APPLICATION_APP + "") int type,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricApplications(queryVO, sortProperty, sortDirection,
          type);
    } else {
      result = metricService.queryMetricApplications(queryVO, sortProperty, sortDirection, type);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/applications/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricApplicationHistograms(
      @Validated MetricQueryVO queryVO,
      @RequestParam(
          required = false,
          defaultValue = FpcCmsConstants.METRIC_TYPE_APPLICATION_APP + "") int type,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String id,
      @RequestParam(required = false, defaultValue = Constants.BOOL_NO) String isDetail) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricApplicationHistograms(queryVO, type, sortProperty,
          sortDirection, id);
    } else {
      result = metricService.queryMetricApplicationHistograms(queryVO, type, sortProperty,
          sortDirection, id, StringUtils.equals(isDetail, Constants.BOOL_YES));
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/applications/as-count")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> countMetricApplications(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricService.countMetricApplications(queryVO, sortProperty, sortDirection);
  }

  @GetMapping("/applications/as-export")
  @Secured({"PERM_USER"})
  public void exportApplications(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false,
          defaultValue = FpcCmsConstants.METRIC_TYPE_APPLICATION_APP + "") int type,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      HttpServletRequest request, HttpServletResponse response) {

    // 设置文件导出格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "application_logs.csv"
        : "application_logs.xlsx";
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

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
        metricFlowlogService.exportApplications(queryVO, sortProperty, sortDirection, type,
            fileType, out);
      } else {
        metricService.exportApplications(queryVO, sortProperty, sortDirection, type, fileType, out);
      }


      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/l7-protocols")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricL7Protocols(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricL7Protocols(queryVO, sortProperty, sortDirection);
    } else {
      result = metricService.queryMetricL7Protocols(queryVO, sortProperty, sortDirection);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/l7-protocols/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricL7ProtocolHistograms(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String l7ProtocolId) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricL7ProtocolHistograms(queryVO, sortProperty,
          sortDirection, l7ProtocolId);
    } else {
      result = metricService.queryMetricL7ProtocolHistograms(queryVO, sortProperty, sortDirection,
          l7ProtocolId);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/l7-protocols/as-count")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> countMetricL7Protocols(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricService.countMetricL7Protocols(queryVO, sortProperty, sortDirection);
  }

  @GetMapping("/l7-protocols/as-export")
  @Secured({"PERM_USER"})
  public void exportL7Protocols(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String l7ProtocolId,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      HttpServletRequest request, HttpServletResponse response) {
    // 设置文件导出格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "l7-protocol_logs.csv"
        : "l7-protocol_logs.xlsx";
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

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
        metricFlowlogService.exportL7Protocols(queryVO, sortProperty, sortDirection, l7ProtocolId,
            fileType, out);
      } else {
        metricService.exportL7Protocols(queryVO, sortProperty, sortDirection, fileType, out);
      }

      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/ports")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricPorts(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricPorts(queryVO, sortProperty, sortDirection);
    } else {
      result = metricService.queryMetricPorts(queryVO, sortProperty, sortDirection);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/ports/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricPortHistograms(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String port) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricPortHistograms(queryVO, sortProperty, sortDirection,
          port);
    } else {
      result = metricService.queryMetricPortHistograms(queryVO, sortProperty, sortDirection, port);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/ports/as-export")
  @Secured({"PERM_USER"})
  public void exportPorts(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      HttpServletRequest request, HttpServletResponse response) {

    // 设置文件导出格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV) ? "port_logs.csv"
        : "port_logs.xlsx";
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

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
        metricFlowlogService.exportPorts(queryVO, sortProperty, sortDirection, fileType, out);
      } else {
        metricService.exportPorts(queryVO, sortProperty, sortDirection, fileType, out);
      }


      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/host-groups")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricHostGroups(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricHostGroups(queryVO, sortProperty, sortDirection);
    } else {
      result = metricService.queryMetricHostGroups(queryVO, sortProperty, sortDirection);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/host-groups/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricHostgroupHistograms(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String hostgroupId) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricHostGroupHistograms(queryVO, sortProperty,
          sortDirection, hostgroupId);
    } else {
      result = metricService.queryMetricHostGroupHistograms(queryVO, sortProperty, sortDirection,
          hostgroupId);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/host-groups/as-export")
  @Secured({"PERM_USER"})
  public void exportHostGroups(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      HttpServletRequest request, HttpServletResponse response) {

    // 设置文件导出格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "host-group_logs.csv"
        : "host-group_logs.xlsx";
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

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
        metricFlowlogService.exportHostGroups(queryVO, sortProperty, sortDirection, fileType, out);
      } else {
        metricService.exportHostGroups(queryVO, sortProperty, sortDirection, fileType, out);
      }

      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/l2-devices")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricL2Devices(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricL2Devices(queryVO, sortProperty, sortDirection);
    } else {
      result = metricService.queryMetricL2Devices(queryVO, sortProperty, sortDirection);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/l2-devices/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricL2DeviceHistograms(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String macAddress) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricL2DeviceHistograms(queryVO, sortProperty,
          sortDirection, macAddress);
    } else {
      result = metricService.queryMetricL2DeviceHistograms(queryVO, sortProperty, sortDirection,
          macAddress);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/l2-devices/as-export")
  @Secured({"PERM_USER"})
  public void exportL2Devices(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      HttpServletRequest request, HttpServletResponse response) {

    // 设置文件导出格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "l2-device_logs.csv"
        : "l2-device_logs.xlsx";
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

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
        metricFlowlogService.exportL2Devices(queryVO, sortProperty, sortDirection, fileType, out);
      } else {
        metricService.exportL2Devices(queryVO, sortProperty, sortDirection, fileType, out);
      }


      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/l3-devices")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricL3Devices(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricL3Devices(queryVO, sortProperty, sortDirection);
    } else {
      result = metricService.queryMetricL3Devices(queryVO, sortProperty, sortDirection);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  // 新增接口方法实现建连分析页面表格展示
  @GetMapping("/l3-devices/established-fail")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricL3DevicesEstablishedFail(
      @Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);

    enrichQuery(queryVO);

    result = metricService.queryMetricL3DevicesEstablishedFail(queryVO, sortProperty,
        sortDirection);

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/l3-devices/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricL3DeviceHistograms(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String ipAddress,
      @RequestParam(required = false) String ipLocality) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricL3DeviceHistograms(queryVO, sortProperty,
          sortDirection, ipAddress, ipLocality);
    } else {
      result = metricService.queryMetricL3DeviceHistograms(queryVO, sortProperty, sortDirection,
          ipAddress, ipLocality);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/l3-devices/as-count")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> countMetricL3Devices(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false, defaultValue = "") String compareProperty) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQuery(queryVO);

    return metricService.countMetricL3Devices(queryVO, sortProperty, sortDirection, compareProperty,
        queryVO.getCount());
  }

  @GetMapping("/l3-devices/as-export")
  @Secured({"PERM_USER"})
  public void exportL3Devices(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      HttpServletRequest request, HttpServletResponse response) {

    // 设置文件导出格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "l3-device_logs.csv"
        : "l3-devices_logs.xlsx";
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

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
        metricFlowlogService.exportL3Devices(queryVO, sortProperty, sortDirection, fileType, out);
      } else {
        metricService.exportL3Devices(queryVO, sortProperty, sortDirection, fileType, out);
      }


      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/ip-conversations")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricIpConversations(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricIpConversations(queryVO, sortProperty,
          sortDirection);
    } else {
      result = metricService.queryMetricIpConversations(queryVO, sortProperty, sortDirection);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/ip-conversations/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricIpConversationHistograms(
      @Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false) String ipAAddress,
      @RequestParam(required = false) String ipBAddress) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    enrichQuery(queryVO);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.queryMetricIpConversationHistograms(queryVO, sortProperty,
          sortDirection, ipAAddress, ipBAddress);
    } else {
      result = metricService.queryMetricIpConversationHistograms(queryVO, sortProperty,
          sortDirection, ipAAddress, ipBAddress);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/ip-conversations/as-count")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> countMetricIpConversations(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricService.countMetricIpConversations(queryVO, sortProperty, sortDirection);
  }

  @GetMapping("/ip-conversations/as-export")
  @Secured({"PERM_USER"})
  public void exportIpConversations(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      HttpServletRequest request, HttpServletResponse response) {

    // 设置文件导出格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "ip-conversation_logs.csv"
        : "ip-conversation_logs.xlsx";
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

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }
      if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
        metricFlowlogService.exportIpConversations(queryVO, sortProperty, sortDirection, fileType,
            out);
      } else {
        metricService.exportIpConversations(queryVO, sortProperty, sortDirection, fileType, out);
      }


      response.flushBuffer();
    } catch (Exception e) {
      LOGGER.error("export flow logs error ", e);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  @GetMapping("/dhcps")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricDhcps(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection, String type) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricService.queryMetricDhcps(queryVO, sortProperty, sortDirection, type);
  }

  @GetMapping("/dhcps/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryMetricDhcpHistograms(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection, String type,
      @RequestParam(required = false) String id) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricService.queryMetricDhcpHistograms(queryVO, sortProperty, sortDirection, type, id);
  }

  @GetMapping("/https")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryMetricHttps(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "count") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return metricService.queryMetricHttps(queryVO, sortProperty, sortDirection);
  }

  @GetMapping("/ip-conversations/as-graph")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> graphMetricIpConversations(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "0") Integer minEstablishedSessions,
      @RequestParam(required = false, defaultValue = "0") Integer minTotalBytes,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    // 默认心跳，避免用户在页面首次心跳前切换页面
    slowQueryService.heartbeat(Lists.newArrayList(queryVO.getQueryId()));

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(0);
    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      result = metricFlowlogService.graphMetricIpConversations(queryVO, minEstablishedSessions,
          minTotalBytes, sortProperty, sortDirection);
    } else {
      result = metricService.graphMetricIpConversations(queryVO, minEstablishedSessions,
          minTotalBytes, sortProperty, sortDirection);
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryVO.getQueryId())) {
      slowQueryService.finish(queryVO.getQueryId());
    }

    return result;
  }

  @GetMapping("/ip-conversations/as-graph/as-export")
  @Secured({"PERM_USER"})
  public void exportGraphMetricIpConversations(@Validated MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "0") Integer minEstablishedSessions,
      @RequestParam(required = false, defaultValue = "0") Integer minTotalBytes,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      HttpServletRequest request, HttpServletResponse response, @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "graph_ipconversation.csv"
        : "graph_ipconversation.xlsx";
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, fileName));
    response.resetBuffer();
    try (ServletOutputStream outputStream = response.getOutputStream();) {

      metricService.exportGraphMetricIpConversations(outputStream, queryVO, fileType, sortProperty,
          sortDirection, minEstablishedSessions, minTotalBytes);

      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export graphIpConversation error", e);
    }
  }

  private void enrichQuery(final MetricQueryVO queryVO) {
    // 查询对象为多个网络，单个或多个网络组时，需要在改方法内解析实际查询的对象(多维检索可 选多个网络和网络组)
    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      List<String> list = CsvUtils.convertCSVToList(queryVO.getNetworkGroupId());

      if (StringUtils.isNotBlank(queryVO.getServiceId())) {
        List<Tuple2<String, String>> serviceNetworkIds = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        list.forEach(networkGroupId -> {
          serviceNetworkIds.addAll(CsvUtils
              .convertCSVToList(sensorNetworkGroupService.querySensorNetworkGroup(networkGroupId)
                  .getNetworkInSensorIds())
              .stream().map(networkId -> Tuples.of(queryVO.getServiceId(), networkId))
              .collect(Collectors.toList()));
        });

        queryVO.setServiceNetworkIds(serviceNetworkIds);
      } else {
        List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        list.forEach(networkGroupId -> {
          networkIds.addAll(CsvUtils.convertCSVToList(sensorNetworkGroupService
              .querySensorNetworkGroup(networkGroupId).getNetworkInSensorIds()));
        });

        queryVO.setNetworkIds(networkIds);
      }
    } else {
      List<String> list = CsvUtils.convertCSVToList(queryVO.getNetworkId());
      if (CollectionUtils.isNotEmpty(list)) {
        if (StringUtils.isNotBlank(queryVO.getServiceId())) {
          List<Tuple2<String, String>> serviceNetworkIds = list.stream()
              .map(networkId -> Tuples.of(queryVO.getServiceId(), networkId))
              .collect(Collectors.toList());
          queryVO.setServiceNetworkIds(serviceNetworkIds);
        } else {
          queryVO.setNetworkIds(list);
        }
      }
    }
  }
}
