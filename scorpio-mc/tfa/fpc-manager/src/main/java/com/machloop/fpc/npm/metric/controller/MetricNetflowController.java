package com.machloop.fpc.npm.metric.controller;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService;
import com.machloop.fpc.npm.metric.service.MetricNetflowTableService;
import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年8月17日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/metric")
public class MetricNetflowController {
    
  @Autowired
  private MetricNetflowHistogramService metricNetflowHistogramService;
  
  @Autowired
  private MetricNetflowTableService metricNetflowTableService;

  @GetMapping("/netflows/dashboard")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryNetflowDashboard(MetricNetflowQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricNetflowHistogramService.queryNetflowDashboard(queryVO);
  }

  @GetMapping("/netflows/ip")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowIp(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      return metricNetflowTableService.querySessionRecordIp(queryVO, sortProperty, sortDirection);
    } else {
      return metricNetflowTableService.queryMetricIp(queryVO, sortProperty, sortDirection);
    }
  }

  @GetMapping("/netflows/ip/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowIpHistogram(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      return metricNetflowHistogramService.querySessionRecordIpHistogram(queryVO, sortProperty, sortDirection);
    } else {
      return metricNetflowHistogramService.queryMetricIpHistogram(queryVO, sortProperty, sortDirection);
    }
  }
  
  @GetMapping("/netflows/session")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowSession(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    return metricNetflowTableService.querySessionRecordSession(queryVO, sortProperty, sortDirection);

  }

  @GetMapping("/netflows/session/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowSessionHistogram(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    return metricNetflowHistogramService.querySessionRecordSessionHistogram(queryVO, sortProperty, sortDirection);

  }
  
  @GetMapping("/netflows/protocol-port")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowProtocolPort(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      return metricNetflowTableService.querySessionRecordProtocolPort(queryVO, sortProperty, sortDirection);
    } else {
      return metricNetflowTableService.queryMetricProtocolPort(queryVO, sortProperty, sortDirection);
    }
  }

  @GetMapping("/netflows/protocol-port/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowProtocolPortHistogram(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      return metricNetflowHistogramService.querySessionRecordProtocolPortHistogram(queryVO, sortProperty, sortDirection);
    } else {
      return metricNetflowHistogramService.queryMetricProtocolPortHistogram(queryVO, sortProperty, sortDirection);
    }
  }
  
  @GetMapping("/netflows/transmit-ip")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowTransmitIp(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "transmit_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      return metricNetflowTableService.querySessionRecordTransmitIp(queryVO, sortProperty, sortDirection);
    } else {
      return metricNetflowTableService.queryMetricTransmitIp(queryVO, sortProperty, sortDirection);
    }
  }

  @GetMapping("/netflows/transmit-ip/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowTransmitIpHistogram(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "transmit_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      return metricNetflowHistogramService.querySessionRecordTransmitIpHistogram(queryVO, sortProperty, sortDirection);
    } else {
      return metricNetflowHistogramService.queryMetricTransmitIpHistogram(queryVO, sortProperty, sortDirection);
    }
  }
  
  @GetMapping("/netflows/ingest-ip")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowIngestIp(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "ingest_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      return metricNetflowTableService.querySessionRecordIngestIp(queryVO, sortProperty, sortDirection);
    } else {
      return metricNetflowTableService.queryMetricIngestIp(queryVO, sortProperty, sortDirection);
    }
  }

  @GetMapping("/netflows/ingest-ip/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryNetflowIngestIpHistogram(MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "ingest_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    if (StringUtils.equals(queryVO.getDrilldown(), Constants.BOOL_YES)) {
      return metricNetflowHistogramService.querySessionRecordIngestIpHistogram(queryVO, sortProperty, sortDirection);
    } else {
      return metricNetflowHistogramService.queryMetricIngestIpHistogram(queryVO, sortProperty, sortDirection);
    }
  }
  
  @GetMapping("/netflows/session-records")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryNetflowSessionRecord(String queryId,
      MetricNetflowQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "report_time") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "pageNumber", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order(Sort.Direction.DESC, "report_time"), new Order("session_id"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    return metricNetflowTableService.querySessionRecords(queryId, queryVO, page);
  }

  @GetMapping("/netflows/session-total-element")
  @Secured({"PERM_USER"})
  public Map<String, Object> querySessionRecordTotalElement(String queryId,
      MetricNetflowQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return metricNetflowTableService.querySessionRecordTotalElement(queryId, queryVO);
  }
  
}

