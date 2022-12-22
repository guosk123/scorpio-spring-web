package com.machloop.fpc.manager.knowledge.dao;

import java.util.List;
import java.util.Map;

/**
 * @author guosk
 *
 * create at 2020年12月5日, fpc-manager
 */
public interface SaProtocolDao {

  List<Map<String, String>> queryProtocols(String filePath);

  List<Map<String, String>> queryProtocolLabels(String filePath);

}
