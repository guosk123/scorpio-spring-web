package com.machloop.fpc.cms.center.central.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 集群状态
 * @author guosk
 *
 * create at 2022年4月28日, fpc-cms-center
 */
public interface ClusterDao {

  List<Map<String, Object>> queryClusterNodes(boolean exceptionNode, Date afterTime);

  /**
   * 注意：尽量减少使用该方法，该方法是通过远程查询的方式（remote函数），判断目标节点连接是否正常，该方式每次查询都会建立新的连接
   * @param nodeIp
   * @return
   */
  String queryNodeConnectState(String nodeIp);

}
