package com.machloop.fpc.cms.center.appliance.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Pattern;

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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Direction;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.cms.center.appliance.service.FlowLogEstFailService;
import com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2022年3月31日, fpc-cms-center
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class FlowLogEstFailController {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogEstFailController.class);

  @Autowired
  private FlowLogEstFailService flowLogEstFailService;

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;

  @GetMapping("/flow-logs/establish-fail")
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

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_QUERY, queryVO);

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order(Sort.Direction.DESC, "report_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    enrichQuery(queryVO);
    return flowLogEstFailService.queryFlowLogs(queryId, page, queryVO, columns);
  }

  @GetMapping("/flow-logs/establish-fail/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryFlowLogsStatistics(String queryId, FlowLogQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);
    return flowLogEstFailService.queryFlowLogStatistics(queryId, queryVO);
  }

  @GetMapping("/flow-logs/establish-fail/as-export")
  @Secured({"PERM_USER"})
  public void exportFlowLogs(String queryId, FlowLogQueryVO queryVO,
      @Pattern(
          regexp = WebappConstants.TABLE_COLUMNS_PATTERN,
          message = "指定列名不合法,且长度需要大于一个字符") @RequestParam(
              required = false, defaultValue = "*") String columns,
      @RequestParam(required = false, defaultValue = "report_time") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      HttpServletRequest request, HttpServletResponse response) {
    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition", "attachment; filename="
        + FileDownloadUtils.fileNameToUtf8String(agent, "est_fail_flow_logs.csv"));
    response.resetBuffer();

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }
    enrichQuery(queryVO);

    try (OutputStream out = response.getOutputStream();) {
      // 设置BOM头为UTF-8
      out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

      Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
          new Order(Sort.Direction.DESC, "report_time"));
      flowLogEstFailService.exportFlowLogs(queryId, queryVO, columns, sort, out);

      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export establish failed flow logs error ", e);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_EXPORT, queryVO);
  }

  private void enrichQuery(final FlowLogQueryVO queryVO) {
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
      if (list.size() > 1) {
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
