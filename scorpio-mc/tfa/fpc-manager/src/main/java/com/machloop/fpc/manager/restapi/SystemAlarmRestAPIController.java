package com.machloop.fpc.manager.restapi;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.clickhouse.client.internal.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.AlarmBO;
import com.machloop.alpha.webapp.system.service.AlarmService;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年11月16日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class SystemAlarmRestAPIController {

  @Autowired
  private AlarmService alarmService;

  @GetMapping("/alarm")
  @RestApiSecured
  public RestAPIResultVO querySystemAlarm(AlarmQueryVO queryVO) {

    Date timeBegin = DateUtils.parseISO8601Date(queryVO.getTimeBegin());
    Date timeEnd = DateUtils.parseISO8601Date(queryVO.getTimeEnd());
    List<AlarmBO> alarmBOList = alarmService.queryAlarmsByTimePeriod(timeBegin, timeEnd);
    List<Map<String, Object>> resultList = alarmBOList.stream().map(item -> alarmBO2Map(item))
        .collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(resultList);
  }

  private Map<String, Object> alarmBO2Map(AlarmBO alarmBO) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("nodeId", alarmBO.getNodeId());
    map.put("level", alarmBO.getLevel());
    map.put("category", alarmBO.getCategory());
    map.put("keyword", alarmBO.getKeyword());
    map.put("solver", alarmBO.getSolver());
    map.put("status", alarmBO.getStatus());
    map.put("component", alarmBO.getComponent());
    map.put("content", alarmBO.getContent());
    map.put("reason", alarmBO.getReason());
    map.put("solveTime", alarmBO.getSolveTime());
    map.put("ariseTime", alarmBO.getAriseTime());

    return map;
  }
}
