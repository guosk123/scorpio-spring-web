package com.machloop.fpc.cms.center.appliance.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.appliance.bo.AbnormalEventBO;
import com.machloop.fpc.cms.center.appliance.service.AbnormalEventService;
import com.machloop.fpc.cms.center.appliance.vo.AbnormalEventQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月15日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/analysis")
public class AbnormalEventController {

  @Autowired
  private AbnormalEventService abnormalEventService;

  @GetMapping("/abnormal-events")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryAbnormalEvents(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      AbnormalEventQueryVO queryVO) {
    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    Sort sort = new Sort(new Order(Sort.Direction.DESC, "start_time"),
        new Order(Sort.Direction.ASC, "type"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<AbnormalEventBO> abnormalEventPage = abnormalEventService.queryAbnormalEvents(page,
        queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(abnormalEventPage.getSize());
    for (AbnormalEventBO abnormalEvent : abnormalEventPage) {
      resultList.add(abnormalEventBO2Map(abnormalEvent));
    }

    return new PageImpl<>(resultList, page, abnormalEventPage.getTotalElements());
  }

  @GetMapping("/abnormal-events/as-count")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> countAbnormalEvent(String startTime, String endTime,
      String metricType, @RequestParam(required = false, defaultValue = "10") int count) {
    Date startTimeDate = null;
    if (StringUtils.isNotBlank(startTime)) {
      startTimeDate = DateUtils.parseISO8601Date(startTime);
    }
    Date endTimeDate = null;
    if (StringUtils.isNotBlank(endTime)) {
      endTimeDate = DateUtils.parseISO8601Date(endTime);
    }

    return abnormalEventService.countAbnormalEvent(startTimeDate, endTimeDate, metricType, count);
  }

  private Map<String, Object> abnormalEventBO2Map(AbnormalEventBO abnormalEvent) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", abnormalEvent.getId());
    map.put("startTime", abnormalEvent.getStartTime());
    map.put("networkId", abnormalEvent.getNetworkId());
    map.put("type", abnormalEvent.getType());
    map.put("content", abnormalEvent.getContent());
    map.put("description", abnormalEvent.getDescription());
    map.put("srcIp", abnormalEvent.getSrcIp());
    map.put("destIp", abnormalEvent.getDestIp());
    map.put("destPort", abnormalEvent.getDestPort());
    map.put("l7ProtocolId", abnormalEvent.getL7ProtocolId());
    map.put("countryIdInitiator", abnormalEvent.getCountryIdInitiator());
    map.put("provinceIdInitiator", abnormalEvent.getProvinceIdInitiator());
    map.put("cityIdInitiator", abnormalEvent.getCityIdInitiator());
    map.put("countryIdResponder", abnormalEvent.getCountryIdResponder());
    map.put("provinceIdResponder", abnormalEvent.getProvinceIdResponder());
    map.put("cityIdResponder", abnormalEvent.getCityIdResponder());

    return map;
  }

}
