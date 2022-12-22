package com.machloop.fpc.npm.graph.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.graph.dao.IpConversationDao;
import com.machloop.fpc.npm.graph.service.IpConversationService;
import com.machloop.fpc.npm.graph.vo.GraphQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年7月27日, fpc-manager
 */
// @Service
public class IpConversationServiceImpl implements IpConversationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpConversationServiceImpl.class);

  private static final int FIRST_NODE_EDGE_COUNTS = 100;

  @Autowired
  private IpConversationDao ipConversationDao;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  /**
   * @see com.machloop.fpc.npm.graph.service.IpConversationService#queryIpConversation(com.machloop.fpc.npm.graph.vo.GraphQueryVO, java.lang.String, java.lang.String, int, int)
   */
  @Override
  public Map<String, Object> queryIpConversation(GraphQueryVO queryVO, String sortProperty,
      String sortDirection, int centralNodeSize, int size) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    NetworkBO network = networkService.queryNetwork(queryVO.getNetworkId());
    LogicalSubnetBO logicalSubnet = logicalSubnetService.queryLogicalSubnet(queryVO.getNetworkId());
    if (StringUtils.isAllBlank(network.getId(), logicalSubnet.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "网络不存在");
    }

    // 检索起点
    List<String> vaildIpList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getIpAddress())) {
      vaildIpList.addAll(ipConversationDao.lookupVertexByTag(queryVO, centralNodeSize));
    } else {
      vaildIpList.addAll(ipConversationDao.lookupVertexByEdge(queryVO, sortProperty, sortDirection,
          centralNodeSize));
    }

    // 检索路径
    List<Map<String, Object>> ipConversations = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(vaildIpList)) {
      // 单IP作为起点时不限制返回数量
      int limit = vaildIpList.size() != 1 ? size : 0;
      ipConversations.addAll(ipConversationDao.goFromIpConversation(queryVO, vaildIpList,
          sortProperty, sortDirection, limit));
    }

    // 排除重复IP对
    deduplicationIpConversation(ipConversations);

    // 单IP需要过滤孤岛节点，多IP需要截取数据
    if (StringUtils.isNotBlank(queryVO.getIpAddress())
        && NetworkUtils.isInetAddress(queryVO.getIpAddress())) {
      Integer pathLength = queryVO.getPathLength();

      // 层深大于1时才需要过滤
      if (pathLength > 1) {
        List<Map<String, Object>> validIpConversations = Lists
            .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

        // 计算节点（非起点）的子节点数量(如果结果集小于限制返回数量则不限制子节点数量)
        int childNodeNumber = ipConversations.size() > size
            ? calculateChildNodeNumber(size, queryVO.getPathLength())
            : 0;
        LOGGER.debug("child node number is: {}", childNodeNumber);

        // 过滤
        int currentPathLevel = 1;
        Set<String> centralNodes = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
        centralNodes.add(queryVO.getIpAddress());
        while (currentPathLevel <= pathLength) {
          int nodeEdgeCounts = currentPathLevel == 1 ? FIRST_NODE_EDGE_COUNTS : childNodeNumber;
          // 过滤孤岛
          centralNodes = filterValidIp(ipConversations, validIpConversations, centralNodes,
              nodeEdgeCounts);
          currentPathLevel++;
        }

        ipConversations = validIpConversations;
      }
    } else {
      // 截取数据
      if (ipConversations.size() > size) {
        ipConversations = ipConversations.subList(0, size);
      }
    }

    // 获取点集合
    Set<String> ipList = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    ipConversations.forEach(ipConversation -> {
      ipList.add(MapUtils.getString(ipConversation, "srcIp"));
      ipList.add(MapUtils.getString(ipConversation, "destIp"));
    });

    result.put("nodes", ipList);
    result.put("edges", ipConversations);
    return result;
  }

  /**
   * 排除重复IP对
   * @param ipConversations
   * @return
   */
  private void deduplicationIpConversation(List<Map<String, Object>> ipConversations) {
    List<String> ipSessionPairs = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    Iterator<Map<String, Object>> iterator = ipConversations.iterator();
    while (iterator.hasNext()) {
      Map<String, Object> ipConversation = iterator.next();
      String srcIp = MapUtils.getString(ipConversation, "srcIp");
      String destIp = MapUtils.getString(ipConversation, "destIp");

      String ipPairA = StringUtils.join(srcIp, destIp);
      String ipPairB = StringUtils.join(destIp, srcIp);
      if (ipSessionPairs.contains(ipPairA) || ipSessionPairs.contains(ipPairB)) {
        iterator.remove();
      } else {
        ipSessionPairs.add(ipPairA);
      }
    }
  }

  /**
   * 过滤有效节点
   * @param ipConversations
   * @param validIpConversations
   * @param centralNodes
   * @param nodeEdgeCounts
   * @return
   */
  private Set<String> filterValidIp(List<Map<String, Object>> ipConversations,
      List<Map<String, Object>> validIpConversations, Set<String> centralNodes,
      int nodeEdgeCounts) {
    Iterator<Map<String, Object>> iterator = ipConversations.iterator();
    Map<String, Integer> validIpMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Set<String> validIpList = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    while (iterator.hasNext()) {
      Map<String, Object> ipConversation = iterator.next();
      String srcIp = MapUtils.getString(ipConversation, "srcIp");
      String destIp = MapUtils.getString(ipConversation, "destIp");
      if (centralNodes.contains(srcIp) || centralNodes.contains(destIp)) {
        Tuple2<String, String> ipPair = centralNodes.contains(srcIp) ? Tuples.of(srcIp, destIp)
            : Tuples.of(destIp, srcIp);

        Integer currentNodeEdgeCounts = validIpMap.getOrDefault(ipPair.getT1(), 0);
        // 限制单个中心节点最多包含的子节点数量
        if (nodeEdgeCounts == 0 || currentNodeEdgeCounts < nodeEdgeCounts) {
          validIpMap.put(ipPair.getT1(), ++currentNodeEdgeCounts);
          validIpList.add(ipPair.getT2());
          validIpConversations.add(ipConversation);
        }

        iterator.remove();
      }
    }

    return validIpList;
  }

  /**
   * 根据返回总边数和层深计算每个节点最多包含多少个子节点
   * @param size
   * @param pathLength
   * @return
   */
  private int calculateChildNodeNumber(int size, int pathLength) {
    int n = 0;
    if (pathLength == 1) {
      return n;
    }

    int resultSize = 0;
    while (resultSize < size) {
      n++;
      resultSize = 0;
      for (int i = 0; i < pathLength; i++) {
        resultSize += FIRST_NODE_EDGE_COUNTS * Math.pow(n, i);
      }
    }

    --n;
    // 子节点数量最少为1
    return n <= 0 ? 1 : n;
  }

}
