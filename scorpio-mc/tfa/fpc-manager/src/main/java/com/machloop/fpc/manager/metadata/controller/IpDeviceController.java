package com.machloop.fpc.manager.metadata.controller;

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
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService;
import com.machloop.fpc.manager.metadata.service.IpDeviceService;
import com.machloop.fpc.manager.metadata.vo.LogRecordQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/21
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/metadata")
public class IpDeviceController {


  @Autowired
  private IpDeviceService ipDeviceService;

  @Autowired
  private ScenarioTaskResultService scenarioTaskResultService;


  @GetMapping("/ip-device")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryIpDeviceList(
      @RequestParam(required = false, defaultValue = "") String startTime,
      @RequestParam(required = false, defaultValue = "") String endTime,
      @Validated LogRecordQueryVO queryVO,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam(
          name = "sortProperty", required = false,
          defaultValue = "report_time") String sortProperty,
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

    Sort sort = new Sort(new Sort.Order(Sort.Direction.fromString(sortDirection), sortProperty));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);

    return ipDeviceService.queryIpDeviceList(queryVO, page);

  }

  @GetMapping("/network-segmentation/as-histogram")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryRtpNetworkSegmentationHistograms(
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

    return ipDeviceService.queryRtpNetworkSegmentationHistograms(queryVO);
  }


}