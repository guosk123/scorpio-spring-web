package com.machloop.fpc.cms.center.global.service;

import java.util.List;

/**
 * @author mazhiyuan
 *
 * create at 2020年10月20日, fpc-manager
 */
public interface SlowQueryService {

  void heartbeat(List<String> queryIds);

  void finish(String queryId);

  void cancelQueries(List<String> queryIds);

  void cancelDeathQueries();
}
