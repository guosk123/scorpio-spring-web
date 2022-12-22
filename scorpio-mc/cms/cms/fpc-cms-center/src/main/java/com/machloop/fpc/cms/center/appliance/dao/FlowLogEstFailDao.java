package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.fpc.cms.center.appliance.vo.FlowLogQueryVO;

/**
 * @author guosk
 *
 * create at 2022年3月31日, fpc-cms-center
 */
public interface FlowLogEstFailDao {

  Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page, FlowLogQueryVO queryVO,
      String columns);

  List<Map<String, Object>> queryFlowLogs(String queryId, FlowLogQueryVO queryVO, String columns,
      Sort sort, int size);

  long countFlowLogs(String queryId, FlowLogQueryVO queryVO);

}
