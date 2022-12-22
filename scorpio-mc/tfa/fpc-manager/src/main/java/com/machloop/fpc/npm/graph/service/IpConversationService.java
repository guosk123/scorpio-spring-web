package com.machloop.fpc.npm.graph.service;

import java.util.Map;

import com.machloop.fpc.npm.graph.vo.GraphQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月27日, fpc-manager
 */
public interface IpConversationService {

  Map<String, Object> queryIpConversation(GraphQueryVO queryVO, String sortProperty,
      String sortDirection, int centralNodeSize, int size);

}
