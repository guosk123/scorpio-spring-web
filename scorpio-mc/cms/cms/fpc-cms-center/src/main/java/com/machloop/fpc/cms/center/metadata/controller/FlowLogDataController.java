package com.machloop.fpc.cms.center.metadata.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.metadata.service.FlowLogDataService;
import com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author liyongjun
 *
 * create at 2019年9月18日, fpc-manager
 */
@RestController
@RequestMapping("/webapi/fpc-cms-v1/metadata")
public class FlowLogDataController {

  @Autowired
  private FlowLogDataService flowLogDataService;

  @Autowired
  private SensorNetworkGroupService sensorNetworkGroupService;

  @GetMapping("/flow-logs/as-protocol-count")
  @Secured({"PERM_USER"})
  public Map<String, Long> countFlowLogDataGroupByProtocol(LogCountQueryVO queryVO) {

    if (StringUtils.isNotBlank(queryVO.getStartTime())) {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
    }
    if (StringUtils.isNotBlank(queryVO.getEndTime())) {
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    }

    enrichQuery(queryVO);

    return flowLogDataService.countFlowLogDataGroupByProtocol(queryVO);
  }

  private void enrichQuery(final LogCountQueryVO queryVO) {
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
