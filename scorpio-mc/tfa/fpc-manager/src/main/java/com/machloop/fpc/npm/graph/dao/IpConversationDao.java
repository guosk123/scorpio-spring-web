package com.machloop.fpc.npm.graph.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.npm.graph.vo.GraphQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月27日, fpc-manager
 */
public interface IpConversationDao {

  List<String> lookupVertexByTag(GraphQueryVO queryVO, int size);

  List<String> lookupVertexByEdge(GraphQueryVO queryVO, String sortProperty, String sortDirection,
      int size);

  List<Map<String, Object>> goFromIpConversation(GraphQueryVO queryVO, List<String> ipList,
      String sortProperty, String sortDirection, int size);

  List<Map<String, Object>> matchIpConversation(GraphQueryVO queryVO, int size);

}
