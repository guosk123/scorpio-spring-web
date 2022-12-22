package com.machloop.fpc.cms.center.metadata.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.fpc.cms.center.metadata.service.LogRecordService;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.cms.center.metadata.vo.ProtocolTelnetLogVO;

/**
 * @author liyongjun
 *
 * create at 2019年9月24日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/metadata")
public class ProtocolTelnetLogController {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProtocolTelnetLogController.class);

  @Autowired
  @Qualifier("protocolTelnetLogService")
  private LogRecordService<ProtocolTelnetLogVO> logRecordService;

  @GetMapping("/protocol-telnet-logs")
  @Secured({"PERM_USER"})
  public Page<ProtocolTelnetLogVO> queryLogRecords(
      @RequestParam(required = false, defaultValue = "") String startTime,
      @RequestParam(required = false, defaultValue = "") String endTime,
      @Validated LogRecordQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(
          name = "sortProperty", required = false, defaultValue = "start_time") String sortProperty,
      @RequestParam(
          name = "sortDirection", required = false, defaultValue = "desc") String sortDirection) {

    if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order("start_time"), new Order("flow_id"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    return logRecordService.queryLogRecords(queryVO, page);
  }

  @GetMapping("/protocol-telnet-logs/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryLogRecordStatistics(
      @RequestParam(required = false, defaultValue = "") String startTime,
      @RequestParam(required = false, defaultValue = "") String endTime, LogRecordQueryVO queryVO) {
    if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }

    return logRecordService.queryLogRecordStatistics(queryVO);
  }

  @GetMapping("/protocol-telnet-logs/via-dsl")
  @Secured({"PERM_USER"})
  public String queryLogRecordViaDsl(@RequestParam String dsl) {
    return logRecordService.queryLogRecordsViaDsl(dsl);
  }

  @GetMapping("/protocol-telnet-logs/{id}")
  @Secured({"PERM_USER"})
  public ProtocolTelnetLogVO queryLogRecord(@PathVariable String id, LogRecordQueryVO queryVO) {

    return logRecordService.queryLogRecord(queryVO, id);
  }


  @GetMapping("/protocol-telnet-logs/as-export")
  @Secured({"PERM_USER"})
  public void exportLogRecords(@RequestParam(required = false, defaultValue = "") String startTime,
      @RequestParam(required = false, defaultValue = "") String endTime,
      @Validated LogRecordQueryVO queryVO,

      @RequestParam(
          name = "sortProperty", required = false, defaultValue = "start_time") String sortProperty,
      @RequestParam(
          name = "sortDirection", required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      @RequestParam(required = false, defaultValue = "0") int count, HttpServletRequest request,
      HttpServletResponse response) {

    Sort sort = new Sort(new Order(Direction.fromString(sortDirection), sortProperty),
        new Order("start_time"), new Order("flow_id"));

    if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }


    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "telnet_logs.csv"
        : "telnet_logs.xlsx";
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, fileName));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }

      logRecordService.exportLogRecords(queryVO, sort, fileType, count, out);
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export telnet logs error ", e);
    }
  }
}
