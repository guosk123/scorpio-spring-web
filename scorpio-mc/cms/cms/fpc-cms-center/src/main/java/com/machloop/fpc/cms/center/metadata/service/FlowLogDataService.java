package com.machloop.fpc.cms.center.metadata.service;

import java.util.Map;

import com.machloop.fpc.cms.center.metadata.vo.LogCountQueryVO;

public interface FlowLogDataService {

  Map<String, Long> countFlowLogDataGroupByProtocol(LogCountQueryVO queryVO);

}
