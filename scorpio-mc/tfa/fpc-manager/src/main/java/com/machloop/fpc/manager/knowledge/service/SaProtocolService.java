package com.machloop.fpc.manager.knowledge.service;

import java.util.List;
import java.util.Map;

/**
 * @author guosk
 *
 * create at 2020年12月5日, fpc-manager
 */
public interface SaProtocolService {

  List<Map<String, Object>> queryProtocols(String protocolName, String standard, String label);

  List<Map<String, String>> queryProtocols();

  List<Map<String, String>> queryLabels();

}
