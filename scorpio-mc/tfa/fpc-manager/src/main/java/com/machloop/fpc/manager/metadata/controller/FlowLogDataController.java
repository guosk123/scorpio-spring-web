package com.machloop.fpc.manager.metadata.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.metadata.service.FlowLogDataService;
import com.machloop.fpc.manager.metadata.vo.LogCountQueryVO;

/**
 * @author liyongjun
 *
 * create at 2019年9月18日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-v1/metadata")
public class FlowLogDataController {

  @Autowired
  private FlowLogDataService flowLogDataService;

  @GetMapping("/flow-logs/as-protocol-count")
  @Secured({"PERM_USER"})
  public Map<String, Long> countFlowLogDataGroupByProtocol(LogCountQueryVO queryVO) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return flowLogDataService.countFlowLogDataGroupByProtocol(queryVO);
  }

}
