package com.machloop.fpc.manager.metadata.service;

import java.util.Map;

import com.machloop.fpc.manager.metadata.vo.LogCountQueryVO;

public interface FlowLogDataService {

  Map<String, Long> countFlowLogDataGroupByProtocol(LogCountQueryVO queryVO);

}
