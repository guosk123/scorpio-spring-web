package com.machloop.fpc.manager.analysis.service;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.analysis.bo.StandardProtocolBO;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public interface StandardProtocolService {

  Page<StandardProtocolBO> queryStandardProtocols(Pageable page, StandardProtocolQueryVO queryVO);

  List<StandardProtocolBO> queryStandardProtocols(StandardProtocolQueryVO queryVO);
  
  List<StandardProtocolBO> queryStandardProtocols();

  StandardProtocolBO queryStandardProtocol(String id);

  StandardProtocolBO saveStandardProtocol(StandardProtocolBO standardProtocolBO, String operatorId);

  StandardProtocolBO updateStandardProtocol(String id, StandardProtocolBO standardProtocolBO,
      String operatorId);

  StandardProtocolBO deleteStandardProtocol(String id, String operatorId);

}
