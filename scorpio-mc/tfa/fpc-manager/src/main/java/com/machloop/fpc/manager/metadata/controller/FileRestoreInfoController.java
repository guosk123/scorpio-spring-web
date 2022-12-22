package com.machloop.fpc.manager.metadata.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.FileDownloadUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService;
import com.machloop.fpc.manager.metadata.service.FileRestoreInfoService;
import com.machloop.fpc.manager.metadata.vo.FileRestoreInfoVO;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/27
 */

@RestController
@RequestMapping("/webapi/fpc-v1/metadata")
public class FileRestoreInfoController {


  @Autowired
  private FileRestoreInfoService fileRestoreInfoService;


  @Autowired
  private ScenarioTaskResultService scenarioTaskResultService;

  private static final Logger LOGGER = LoggerFactory.getLogger(FileRestoreInfoController.class);


  @GetMapping("/file-restore-info")
  @Secured({"PERM_USER"})
  public Page<FileRestoreInfoVO> queryFileRestoreInfos(
      @RequestParam(required = false, defaultValue = "") String startTime,
      @RequestParam(required = false, defaultValue = "") String endTime, LogRecordQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(
          name = "sortProperty", required = false, defaultValue = "timestamp") String sortProperty,
      @RequestParam(
          name = "sortDirection", required = false, defaultValue = "desc") String sortDirection) {
    if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }
    // 如果ID条件以analysis-result开头, 需要根据id查询分析任务结果对应的id
    if (StringUtils.startsWith(queryVO.getId(),
        ManagerConstants.METADATA_CONDITION_ID_ANALYSIS_RESULT_PREFIX)) {
      queryVO.setId(scenarioTaskResultService.queryScenarioTaskResultIds(queryVO.getId()));
    }

    Sort sort = new Sort(new Sort.Order(Sort.Direction.fromString(sortDirection), sortProperty),
        new Sort.Order("timestamp"), new Sort.Order("flow_id"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    return fileRestoreInfoService.queryFileRestoreInfos(queryVO, page);
  }

  @GetMapping("/file-restore-info/as-statistics")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryFileRestoreInfoStatistics(
      @RequestParam(required = false, defaultValue = "") String startTime,
      @RequestParam(required = false, defaultValue = "") String endTime, LogRecordQueryVO queryVO) {
    if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }
    // 如果ID条件以analysis-result开头, 需要根据id查询分析任务结果对应的id
    if (StringUtils.startsWith(queryVO.getId(),
        ManagerConstants.METADATA_CONDITION_ID_ANALYSIS_RESULT_PREFIX)) {
      queryVO.setId(scenarioTaskResultService.queryScenarioTaskResultIds(queryVO.getId()));
    }
    return fileRestoreInfoService.queryFileRestoreInfoStatistics(queryVO);
  }

  @GetMapping("/file-restore-info/{id}")
  @Secured({"PERM_USER"})
  public FileRestoreInfoVO queryFileRestoreInfo(@PathVariable String id, LogRecordQueryVO queryVO) {

    return fileRestoreInfoService.queryFileRestoreInfo(queryVO, id);
  }

  @GetMapping("/file-restore-info/as-export")
  @Secured({"PERM_USER"})
  public void exportLogRecords(@RequestParam(required = false, defaultValue = "") String startTime,
      @RequestParam(required = false, defaultValue = "") String endTime, LogRecordQueryVO queryVO,
      @RequestParam(
          name = "sortProperty", required = false, defaultValue = "timestamp") String sortProperty,
      @RequestParam(
          name = "sortDirection", required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(
          required = false, defaultValue = Constants.EXPORT_FILE_TYPE_CSV) String fileType,
      @RequestParam(required = false, defaultValue = "0") int count, HttpServletRequest request,
      HttpServletResponse response) {

    Sort sort = new Sort(new Sort.Order(Sort.Direction.fromString(sortDirection), sortProperty),
        new Sort.Order("timestamp"), new Sort.Order("flow_id"));

    if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(startTime));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(endTime));
    }
    // 如果ID条件以analysis-result开头, 需要根据id查询分析任务结果对应的id
    if (StringUtils.startsWith(queryVO.getId(),
        ManagerConstants.METADATA_CONDITION_ID_ANALYSIS_RESULT_PREFIX)) {
      queryVO.setId(scenarioTaskResultService.queryScenarioTaskResultIds(queryVO.getId()));
    }

    // 设置导出文件格式
    String agent = request.getHeader("User-Agent");
    String fileName = StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)
        ? "file_restore_infos.csv"
        : "file_restore_infos.xlsx";
    response.setContentType("application/octet-stream;charset=utf-8");
    response.setHeader("Content-Disposition",
        "attachment; filename=" + FileDownloadUtils.fileNameToUtf8String(agent, fileName));
    response.resetBuffer();

    try (OutputStream out = response.getOutputStream();) {
      if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
        // 设置BOM头为UTF-8
        out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
      }

      fileRestoreInfoService.exportLogRecords(queryVO, sort, fileType, count, out);
      response.flushBuffer();
    } catch (IOException e) {
      LOGGER.warn("export file restore infos error ", e);
    }
  }


}
