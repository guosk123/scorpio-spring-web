package com.machloop.fpc.manager.analysis.dao;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.analysis.data.StandardProtocolDO;
import com.machloop.fpc.manager.analysis.vo.StandardProtocolQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月15日, fpc-manager
 */
public interface StandardProtocolDao {

  Page<StandardProtocolDO> queryStandardProtocols(Pageable page, StandardProtocolQueryVO queryVO);

  List<StandardProtocolDO> queryStandardProtocols(StandardProtocolQueryVO queryVO);
  
  List<StandardProtocolDO> queryStandardProtocols();

  StandardProtocolDO queryStandardProtocol(String id);

  StandardProtocolDO queryStandardProtocol(String l7ProtocolId, String port);

  StandardProtocolDO saveStandardProtocol(StandardProtocolDO protocolConfigDO);

  int updateStandardProtocol(StandardProtocolDO protocolConfigDO);

  int deleteStandardProtocol(String id, String operatorId);

}
