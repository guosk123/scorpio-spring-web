package com.machloop.fpc.npm.graph.controller;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.npm.graph.service.IpConversationService;
import com.machloop.fpc.npm.graph.vo.GraphQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月27日, fpc-manager
 */
@Validated
// @RestController
// @RequestMapping("/webapi/fpc-v1/graph")
public class IpConversationController {

  @Autowired
  private IpConversationService ipConversationService;

  @GetMapping("/ip-conversations")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryIpConversations(@Validated GraphQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "total_bytes") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false, defaultValue = "10") int centralNodeSize,
      @RequestParam(required = false, defaultValue = "100") int size) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    return ipConversationService.queryIpConversation(queryVO, sortProperty, sortDirection,
        centralNodeSize, size);
  }

}
