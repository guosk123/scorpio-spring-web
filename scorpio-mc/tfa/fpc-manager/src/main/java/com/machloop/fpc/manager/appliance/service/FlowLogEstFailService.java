package com.machloop.fpc.manager.appliance.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;

/**
 * @author guosk
 *
 * create at 2022年3月30日, fpc-manager
 */
public interface FlowLogEstFailService {

  Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page, FlowLogQueryVO queryVO,
      String columns);

  Map<String, Object> queryFlowLogStatistics(String queryId, FlowLogQueryVO queryVO);

  void exportFlowLogs(String queryId, FlowLogQueryVO queryVO, String columns, Sort sort,
      OutputStream out) throws IOException;

}
