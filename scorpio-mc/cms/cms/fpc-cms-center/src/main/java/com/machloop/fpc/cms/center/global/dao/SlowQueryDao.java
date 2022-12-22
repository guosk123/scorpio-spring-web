package com.machloop.fpc.cms.center.global.dao;

import java.util.List;

/**
 * @author mazhiyuan
 *
 * create at 2020年10月20日, fpc-manager
 */
public interface SlowQueryDao {

  void cancelQueries(List<String> queryId);

}
