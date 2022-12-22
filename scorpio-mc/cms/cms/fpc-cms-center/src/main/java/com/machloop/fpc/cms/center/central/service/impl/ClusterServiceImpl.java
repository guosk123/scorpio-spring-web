package com.machloop.fpc.cms.center.central.service.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.dao.ClusterDao;
import com.machloop.fpc.cms.center.central.service.ClusterService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.helper.ClickhouseRemoteServerHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2022年4月29日, fpc-cms-center
 */
@Service
public class ClusterServiceImpl implements ClusterService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClusterServiceImpl.class);

  private static final int CACHE_TIMEOUT_MS = 10000;

  private Map<String, String> abnormalNodesCache;

  private long lastRefreshTime;

  @Autowired
  private ClusterDao clusterDao;

  @Autowired
  private FpcService fpcService;

  /**
   * @see com.machloop.fpc.cms.center.central.service.ClusterService#queryAbnormalNodesAndRefresh()
   */
  @Override
  public synchronized Map<String, String> queryAbnormalNodesAndRefresh() {
    if (GracefulShutdownHelper.isShutdownNow()) {
      return Maps.newHashMapWithExpectedSize(0);
    }

    long now = DateUtils.now().getTime();
    if (now - this.lastRefreshTime <= CACHE_TIMEOUT_MS && this.abnormalNodesCache != null) {
      return this.abnormalNodesCache;
    }

    List<FpcBO> allFpc = fpcService.queryAllFpc();

    // 当前设备心跳正常的探针
    List<String> currentFpcs = allFpc.stream().filter(
        fpc -> StringUtils.equals(fpc.getConnectStatus(), FpcCmsConstants.CONNECT_STATUS_NORMAL))
        .map(FpcBO::getIp).collect(Collectors.toList());
    // 当前clickhouse集群中连接异常的节点
    List<String> exceptionNodes = clusterDao
        .queryClusterNodes(true,
            DateUtils.beforeSecondDate(DateUtils.now(), Constants.HALF_MINUTE_SECONDS))
        .stream().map(node -> MapUtils.getString(node, "hostName")).collect(Collectors.toList());
    // 汇总前两种情况，获取实际有效数据节点
    currentFpcs.removeAll(exceptionNodes);
    List<String> validNodes = Lists.newArrayList(currentFpcs);

    // 获取当前集群已有的数据节点(不包含localhost)
    List<String> currentNodes = ClickhouseRemoteServerHelper.queryNodes().stream()
        .filter(item -> !StringUtils.equals(item, "localhost")).collect(Collectors.toList());

    // 集群节点与实际有效节点不一致，变更集群
    if (!CollectionUtils.isEqualCollection(currentFpcs, currentNodes)) {
      // 新增|恢复节点
      currentFpcs.removeAll(currentNodes);
      // 将要新增的节点中存在连接异常的节点（节点不在当前集群内，但该节点处于异常，无法连接的状态）
      List<String> abnormalFpcs = currentFpcs.stream().filter(item -> StringUtils
          .equals(FpcCmsConstants.CONNECT_STATUS_ABNORMAL, clusterDao.queryNodeConnectState(item)))
          .collect(Collectors.toList());
      currentFpcs.removeAll(abnormalFpcs);
      currentFpcs.forEach(fpcIp -> ClickhouseRemoteServerHelper.addNode(fpcIp));

      // 移除异常节点
      currentNodes.removeAll(validNodes);
      currentNodes.forEach(fpcIp -> ClickhouseRemoteServerHelper.deleteNode(fpcIp));

      // 当前实际有效的节点
      validNodes.removeAll(abnormalFpcs);

      // 节点状态发生变化时，记录日志
      if (CollectionUtils.isNotEmpty(currentFpcs) || CollectionUtils.isNotEmpty(currentNodes)) {
        LOGGER.info("this time sync remote server, add node:[{}], delete node: [{}].",
            CsvUtils.convertCollectionToCSV(currentFpcs),
            CsvUtils.convertCollectionToCSV(currentNodes));

        // 集群内节点发生变化，记录系统日志
        StringBuilder content = new StringBuilder();
        content.append("检测到下级探针设备连接状态发生变更，");
        if (CollectionUtils.isNotEmpty(currentFpcs)) {
          content.append("数据集群新增探针节点：[");
          content.append(CsvUtils.convertCollectionToCSV(currentFpcs)).append("]；");
        }
        if (CollectionUtils.isNotEmpty(currentNodes)) {
          content.append("数据集群移除探针节点：[");
          content.append(CsvUtils.convertCollectionToCSV(currentNodes)).append("]。");
        }
        LogHelper.systemRuning(LogHelper.LEVEL_NOTICE, content.toString(), "system");
      }

      // 出现异常节点时，产生告警
      if (CollectionUtils.isNotEmpty(currentNodes)) {
        LOGGER.warn(
            "some abnormal node is detected and kicked out of the cluster, abnormal node: [{}]",
            CsvUtils.convertCollectionToCSV(currentNodes));

        AlarmHelper.alarm(AlarmHelper.LEVEL_IMPORTANT, AlarmHelper.CATEGORY_SERVER_RESOURCE,
            "cluster", String.format("检测到数据异常节点[%s]，已将该节点踢出数据集群",
                CsvUtils.convertCollectionToCSV(currentNodes)));
      }
    }

    Map<String,
        String> abnormalNodes = allFpc.stream().filter(fpc -> !validNodes.contains(fpc.getIp()))
            .collect(Collectors.toMap(FpcBO::getSerialNumber, FpcBO::getIp));
    this.lastRefreshTime = now;
    this.abnormalNodesCache = abnormalNodes;

    return abnormalNodes;
  }

}
