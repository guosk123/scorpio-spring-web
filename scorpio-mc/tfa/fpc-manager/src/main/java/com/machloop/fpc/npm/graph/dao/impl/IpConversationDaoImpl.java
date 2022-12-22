package com.machloop.fpc.npm.graph.dao.impl;

import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6Network;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.manager.boot.configuration.NebulaGraphClient;
import com.machloop.fpc.npm.graph.dao.IpConversationDao;
import com.machloop.fpc.npm.graph.vo.GraphQueryVO;
import com.vesoft.nebula.client.graph.data.Node;
import com.vesoft.nebula.client.graph.data.PathWrapper;
import com.vesoft.nebula.client.graph.data.Relationship;
import com.vesoft.nebula.client.graph.data.ResultSet;
import com.vesoft.nebula.client.graph.data.ValueWrapper;
import com.vesoft.nebula.client.graph.exception.IOErrorException;
import com.vesoft.nebula.client.graph.exception.InvalidValueException;
import com.vesoft.nebula.client.graph.net.Session;

import reactor.util.function.Tuple2;

/**
 * @author guosk
 *
 * create at 2021年7月27日, fpc-manager
 */
// @Repository
public class IpConversationDaoImpl implements IpConversationDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(IpConversationDaoImpl.class);

  private static final String FPC_GRAPH_IP_CONVERSATION_PREFIX = "ip_conversation";

  private static final String TAG_TYPE_IPV4 = "ipv4";
  private static final String TAG_TYPE_IPV6 = "ipv6";

  @Autowired
  private NebulaGraphClient nebulaGraphClient;

  /**
   * @see com.machloop.fpc.npm.graph.dao.IpConversationDao#lookupVertexByTag(com.machloop.fpc.npm.graph.vo.GraphQueryVO, int)
   */
  @Override
  public List<String> lookupVertexByTag(GraphQueryVO queryVO, int size) {
    StringBuilder gqlBuilder = new StringBuilder();
    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 指定查询的图空间名称
    gqlBuilder.append("use ").append(convertSpaceName(queryVO.getNetworkId())).append(";");

    String ipAddress = queryVO.getIpAddress();
    String[] split = StringUtils.split(ipAddress, "-");
    if (NetworkUtils.isInetAddress(ipAddress)) {
      return Lists.newArrayList(ipAddress);
    } else if (NetworkUtils.isCidr(ipAddress, IpVersion.V4)
        || (split.length == 2 && NetworkUtils.isInetAddress(split[0], IpVersion.V4)
            && NetworkUtils.isInetAddress(split[1], IpVersion.V4))) {
      gqlBuilder.append("lookup on ").append(TAG_TYPE_IPV4);
      gqlBuilder.append(
          " where ipv4.ipv4_addr_integer >= #ipStart and ipv4.ipv4_addr_integer <= #ipEnd ");
      gqlBuilder.append(" | limit ").append(size);

      Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ipAddress);
      params.put("#ipStart", String.valueOf(ip2Range.getT1()));
      params.put("#ipEnd", String.valueOf(ip2Range.getT2()));
    } else if (NetworkUtils.isCidr(ipAddress, IpVersion.V6)
        || (split.length == 2 && NetworkUtils.isInetAddress(split[0], IpVersion.V6)
            && NetworkUtils.isInetAddress(split[1], IpVersion.V6))) {
      gqlBuilder.append("lookup on ").append(TAG_TYPE_IPV6);
      gqlBuilder.append(
          " where ipv6.ipv6_addr_integer_high == #startIpHighBits and ipv6.ipv6_addr_integer_low >= #startIpLowBits ");
      gqlBuilder.append(" union ");
      gqlBuilder.append("lookup on ").append(TAG_TYPE_IPV6);
      gqlBuilder.append(
          " where ipv6.ipv6_addr_integer_high > #startIpHighBits and ipv6.ipv6_addr_integer_high < #endIpHighBits ");
      gqlBuilder.append(" union ");
      gqlBuilder.append("lookup on ").append(TAG_TYPE_IPV6);
      gqlBuilder.append(
          " where ipv6.ipv6_addr_integer_high == #endIpHighBits and ipv6.ipv6_addr_integer_low <= #endIpLowBits ");

      IPv6Network ipv6Network = split.length == 2
          ? IPv6Network.fromTwoAddresses(IPv6Address.fromString(split[0]),
              IPv6Address.fromString(split[1]))
          : IPv6Network.fromString(ipAddress);
      params.put("#startIpHighBits", String.valueOf(ipv6Network.getFirst().getHighBits()));
      params.put("#startIpLowBits", String.valueOf(ipv6Network.getFirst().getLowBits()));
      params.put("#endIpHighBits", String.valueOf(ipv6Network.getLast().getHighBits()));
      params.put("#endIpLowBits", String.valueOf(ipv6Network.getLast().getLowBits()));
    } else {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的IP");
    }

    List<String> vertexList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Session session = null;
    try {
      String gql = StringUtils.replaceEachRepeatedly(gqlBuilder.toString(),
          params.keySet().toArray(new String[params.size()]),
          params.values().toArray(new String[params.size()]));
      LOGGER.info("execute lookup tag gql: " + gql);

      session = nebulaGraphClient.getSession();
      ResultSet resultSet = session.execute(gql);
      vertexList.addAll(praseResult(resultSet).stream()
          .map(item -> MapUtils.getString(item, "VertexID")).collect(Collectors.toList()));
      if (vertexList.size() > size) {
        vertexList = vertexList.subList(0, size);
      }
    } catch (UnsupportedEncodingException | IOErrorException e) {
      LOGGER.warn("lookup tag failed.", e);
    } finally {
      if (session != null) {
        session.release();
      }
    }

    return vertexList;
  }

  /**
   * @see com.machloop.fpc.npm.graph.dao.IpConversationDao#lookupVertexByEdge(com.machloop.fpc.npm.graph.vo.GraphQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<String> lookupVertexByEdge(GraphQueryVO queryVO, String sortProperty,
      String sortDirection, int size) {
    StringBuilder gqlBuilder = new StringBuilder();
    // 指定查询的图空间名称
    gqlBuilder.append("use ").append(convertSpaceName(queryVO.getNetworkId())).append(";");
    gqlBuilder.append("lookup on conversation ");

    // 基础数据过滤
    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    gqlBuilder.append(" where conversation.insert_time > 0 ");
    enrichTimeWhereGql(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(), gqlBuilder, params);
    gqlBuilder.append(" and conversation.established_sessions >= 0 ");
    gqlBuilder.append(" and conversation.total_bytes >= 0 ");

    // 指定获取内容
    gqlBuilder.append(" yield conversation.total_bytes as total_bytes, ");
    gqlBuilder.append(" conversation.established_sessions as established_sessions ");
    gqlBuilder.append(" | return $-.VID as vid, $-.total_bytes as total_bytes, ");
    gqlBuilder.append(" $-.established_sessions as established_sessions ");

    // 聚合
    gqlBuilder.append(" | group by $-.vid yield $-.vid as vid, ");
    gqlBuilder.append(" sum($-.total_bytes) as total_bytes, ");
    gqlBuilder.append(" sum($-.established_sessions) as established_sessions ");

    // 排序
    gqlBuilder.append(" | order by $-.").append(sortProperty).append(" ").append(sortDirection);
    gqlBuilder.append(" | limit ").append(size);

    List<String> vertexList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Session session = null;
    try {
      String gql = StringUtils.replaceEachRepeatedly(gqlBuilder.toString(),
          params.keySet().toArray(new String[params.size()]),
          params.values().toArray(new String[params.size()]));
      LOGGER.info("execute lookup edge gql: " + gql);

      session = nebulaGraphClient.getSession();

      List<Map<String, Object>> list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      // SrcVID
      ResultSet srcResultSet = session.execute(gql.replace("$-.VID", "$-.SrcVID"));
      list.addAll(praseResult(srcResultSet));

      // DstVID
      ResultSet dstResultSet = session.execute(gql.replace("$-.VID", "$-.DstVID"));
      list.addAll(praseResult(dstResultSet));

      vertexList.addAll(list.stream().sorted(new Comparator<Map<String, Object>>() {

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
          Long o1Value = MapUtils.getLong(o1, TextUtils.underLineToCamel(sortProperty));
          Long o2Value = MapUtils.getLong(o2, TextUtils.underLineToCamel(sortProperty));
          return o2Value.compareTo(o1Value);
        }

      }).map(item -> MapUtils.getString(item, "vid")).limit(size).collect(Collectors.toList()));
    } catch (UnsupportedEncodingException | IOErrorException e) {
      LOGGER.warn("lookup edge failed.", e);
    } finally {
      if (session != null) {
        session.release();
      }
    }

    return vertexList;
  }

  /**
   * @see com.machloop.fpc.npm.graph.dao.IpConversationDao#goFromIpConversation(com.machloop.fpc.npm.graph.vo.GraphQueryVO, java.util.List, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> goFromIpConversation(GraphQueryVO queryVO, List<String> ipList,
      String sortProperty, String sortDirection, int size) {
    StringBuilder gqlBuilder = new StringBuilder();
    // 指定查询的图空间名称
    gqlBuilder.append("use ").append(convertSpaceName(queryVO.getNetworkId())).append(";");
    gqlBuilder.append("go ");
    if (queryVO.getPathLength() != null) {
      gqlBuilder.append("1 to ").append(queryVO.getPathLength()).append(" steps ");
    }
    ipList = ipList.stream().map(ip -> "'" + ip + "'").collect(Collectors.toList());
    gqlBuilder.append(" from ").append(StringUtils.join(ipList, ","));
    gqlBuilder.append(" over conversation BIDIRECT ");

    // 基础数据过滤
    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    gqlBuilder.append(" where 1==1 ");
    enrichTimeWhereGql(queryVO.getStartTimeDate(), queryVO.getEndTimeDate(), gqlBuilder, params);
    gqlBuilder.append(" and conversation.established_sessions >= 0 ");
    gqlBuilder.append(" and conversation.total_bytes >= 0 ");

    // 初步检索信息
    gqlBuilder.append(" yield DISTINCT conversation._src as srcId, conversation._dst as dstId, ");
    gqlBuilder.append(" conversation.established_sessions as established_sessions, ");
    gqlBuilder.append(" conversation.total_bytes as total_bytes ");

    // 聚合
    gqlBuilder.append(" | group by $-.srcId, $-.dstId ");
    gqlBuilder.append(" yield $-.srcId as srcIp, $-.dstId as destIp, ");
    gqlBuilder.append(" sum($-.established_sessions) as establishedSessions, ");
    gqlBuilder.append(" sum($-.total_bytes) as totalBytes ");

    // 聚合结果过滤
    gqlBuilder.append(" | yield $-.srcIp as srcIp, $-.destIp as destIp, ");
    gqlBuilder.append(" $-.establishedSessions as establishedSessions, ");
    gqlBuilder.append(" $-.totalBytes as totalBytes ");
    gqlBuilder.append(" where 1==1 ");
    if (queryVO.getMinTotalBytes() != null) {
      gqlBuilder.append(" and $-.totalBytes >= #totalBytes ");
      params.put("#totalBytes", String.valueOf(queryVO.getMinTotalBytes()));
    }
    if (queryVO.getMinEstablishedSessions() != null) {
      gqlBuilder.append(" and $-.establishedSessions >= #establishedSessions ");
      params.put("#establishedSessions", String.valueOf(queryVO.getMinEstablishedSessions()));
    }

    // 排序
    gqlBuilder.append(" | order by $-.").append(TextUtils.underLineToCamel(sortProperty))
        .append(" ").append(sortDirection);
    if (size > 0) {
      gqlBuilder.append(" | limit ").append(size);
    }

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Session session = null;
    try {
      String gql = StringUtils.replaceEachRepeatedly(gqlBuilder.toString(),
          params.keySet().toArray(new String[params.size()]),
          params.values().toArray(new String[params.size()]));
      LOGGER.info("execute go gql: " + gql);

      session = nebulaGraphClient.getSession();
      ResultSet resultSet = session.execute(gql);
      result = praseResult(resultSet);
    } catch (UnsupportedEncodingException | IOErrorException e) {
      LOGGER.warn("go from ip conversation failed.", e);
    } finally {
      if (session != null) {
        session.release();
      }
    }

    return result;
  }

  /**
   * 该方法慎用（未指定起点，匹配数据量无法预估）
   * @see com.machloop.fpc.npm.graph.dao.IpConversationDao#matchIpConversation(com.machloop.fpc.npm.graph.vo.GraphQueryVO, int)
   */
  @Override
  public List<Map<String, Object>> matchIpConversation(GraphQueryVO queryVO, int size) {
    StringBuilder gqlBuilder = new StringBuilder();
    gqlBuilder.append("use ").append(convertSpaceName(queryVO.getNetworkId())).append(";");

    // 合并标签ipv4 和 ipv6的结果
    String ipAddress = queryVO.getIpAddress();
    String[] split = StringUtils.split(ipAddress, "-");
    if (StringUtils.isBlank(ipAddress)) {
      gqlBuilder.append(matchIpConversationGql(queryVO, TAG_TYPE_IPV4, size));
      gqlBuilder.append(" union ");
      gqlBuilder.append(matchIpConversationGql(queryVO, TAG_TYPE_IPV6, size));
    } else if (NetworkUtils.isInetAddress(ipAddress, IpVersion.V4)
        || NetworkUtils.isCidr(ipAddress, IpVersion.V4)
        || (split.length == 2 && NetworkUtils.isInetAddress(split[0], IpVersion.V4)
            && NetworkUtils.isInetAddress(split[1], IpVersion.V4))) {
      gqlBuilder.append(matchIpConversationGql(queryVO, TAG_TYPE_IPV4, size));
    } else if (NetworkUtils.isInetAddress(ipAddress, IpVersion.V6)
        || NetworkUtils.isCidr(ipAddress, IpVersion.V6)
        || (split.length == 2 && NetworkUtils.isInetAddress(split[0], IpVersion.V6)
            && NetworkUtils.isInetAddress(split[1], IpVersion.V6))) {
      gqlBuilder.append(matchIpConversationGql(queryVO, TAG_TYPE_IPV6, size));
    }
    LOGGER.info("execute matchIpConversation gql: " + gqlBuilder.toString());

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Session session = null;
    try {
      session = nebulaGraphClient.getSession();
      ResultSet resultSet = session.execute(gqlBuilder.toString());
      result = praseResult(resultSet);
    } catch (UnsupportedEncodingException | IOErrorException e) {
      LOGGER.warn("match ip conversation failed.", e);
    } finally {
      if (session != null) {
        session.release();
      }
    }

    return result;
  }

  /**
   * 拼写match语句
   * @param queryVO
   * @param tagType
   * @param size
   * @return
   */
  private String matchIpConversationGql(GraphQueryVO queryVO, String tagType, int size) {
    StringBuilder gqlBuilder = new StringBuilder();
    gqlBuilder.append("match p=(v:#tagType)-[e:conversation*#steps]->(v2)");
    gqlBuilder.append(" where 1==1 ");

    // 过滤
    Map<String, String> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("#tagType", tagType);
    params.put("#steps",
        queryVO.getPathLength() != null ? String.valueOf(queryVO.getPathLength()) : "1");
    enrichVertexWhereGql(gqlBuilder, params, queryVO.getIpAddress());
    for (int i = 0; i < queryVO.getPathLength(); i++) {
      enrichEdgeWhereGql(gqlBuilder, params, queryVO, "e[" + i + "]");
    }

    // 检索路径和每个路径去重后的节点数量
    gqlBuilder.append(
        " with id(v) as vid, id(v2) as v2id, p, nodes(p) as allNodes unwind allNodes as unwindNodes ");
    gqlBuilder.append(
        " return vid, v2id, p, size(collect(distinct unwindNodes)) as deduplicationNodeSize ");

    // 去除环路
    gqlBuilder.append(
        " | with $-.vid as vid, $-.v2id as v2id, $-.p as p, $-.deduplicationNodeSize as deduplicationNodeSize ");
    gqlBuilder.append(" where deduplicationNodeSize == length(p) + 1 ");
    gqlBuilder.append(" return vid as srcIp, v2id as destIp, p limit ").append(size);

    return StringUtils.replaceEachRepeatedly(gqlBuilder.toString(),
        params.keySet().toArray(new String[params.size()]),
        params.values().toArray(new String[params.size()]));
  }

  /**
   * 获取图空间名称
   * @param networkId
   * @return
   */
  private String convertSpaceName(String networkId) {
    return StringUtils.joinWith("_", FPC_GRAPH_IP_CONVERSATION_PREFIX, networkId);
  }

  /**
   * 点过滤
   * @param gqlBuilder
   * @param params
   * @param ipAddress
   */
  private void enrichVertexWhereGql(StringBuilder gqlBuilder, Map<String, String> params,
      String ipAddress) {
    if (StringUtils.isNotBlank(ipAddress)) {
      String[] split = StringUtils.split(ipAddress, "-");

      if (NetworkUtils.isInetAddress(ipAddress)) {
        gqlBuilder.append(" and (id(v) == '#ipAddress' or id(v2) == '#ipAddress') ");
        params.put("#ipAddress", ipAddress);
      } else if (NetworkUtils.isCidr(ipAddress, IpVersion.V4)
          || (split.length == 2 && NetworkUtils.isInetAddress(split[0], IpVersion.V4)
              && NetworkUtils.isInetAddress(split[1], IpVersion.V4))) {
        gqlBuilder.append(" and (");
        gqlBuilder.append("(v.ipv4_addr_integer >= #ipStart and v.ipv4_addr_integer <= #ipEnd) ");
        gqlBuilder.append(" or ");
        gqlBuilder.append("(v2.ipv4_addr_integer >= #ipStart and v2.ipv4_addr_integer <= #ipEnd) ");
        gqlBuilder.append(") ");

        Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ipAddress);
        params.put("#ipStart", String.valueOf(ip2Range.getT1()));
        params.put("#ipEnd", String.valueOf(ip2Range.getT2()));
      } else if (NetworkUtils.isCidr(ipAddress, IpVersion.V6)
          || (split.length == 2 && NetworkUtils.isInetAddress(split[0], IpVersion.V6)
              && NetworkUtils.isInetAddress(split[1], IpVersion.V6))) {
        gqlBuilder.append("and (");
        gqlBuilder.append(
            "(v.ipv6_addr_integer_high == #startIpHighBits and v.ipv6_addr_integer_low >= #startIpLowBits)");
        gqlBuilder.append(
            " or (v.ipv6_addr_integer_high > #startIpHighBits and v.ipv6_addr_integer_high < #endIpHighBits)");
        gqlBuilder.append(
            " or (v.ipv6_addr_integer_high == #endIpHighBits and v.ipv6_addr_integer_low <= #endIpLowBits)");
        gqlBuilder.append(
            " or (v2.ipv6_addr_integer_high == #startIpHighBits and v2.ipv6_addr_integer_low >= #startIpLowBits)");
        gqlBuilder.append(
            " or (v2.ipv6_addr_integer_high > #startIpHighBits and v2.ipv6_addr_integer_high < #endIpHighBits)");
        gqlBuilder.append(
            " or (v2.ipv6_addr_integer_high == #endIpHighBits and v2.ipv6_addr_integer_low <= #endIpLowBits)");
        gqlBuilder.append(") ");

        IPv6Network ipv6Network = split.length == 2
            ? IPv6Network.fromTwoAddresses(IPv6Address.fromString(split[0]),
                IPv6Address.fromString(split[1]))
            : IPv6Network.fromString(ipAddress);
        params.put("#startIpHighBits", String.valueOf(ipv6Network.getFirst().getHighBits()));
        params.put("#startIpLowBits", String.valueOf(ipv6Network.getFirst().getLowBits()));
        params.put("#endIpHighBits", String.valueOf(ipv6Network.getLast().getHighBits()));
        params.put("#endIpLowBits", String.valueOf(ipv6Network.getLast().getLowBits()));
      } else {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的IP");
      }
    }
  }

  /**
   * 边属性过滤
   * @param gqlBuilder
   * @param params
   * @param queryVO
   * @param edge
   */
  private void enrichEdgeWhereGql(StringBuilder gqlBuilder, Map<String, String> params,
      GraphQueryVO queryVO, String edge) {
    if (queryVO.getStartTimeDate() == null || queryVO.getEndTimeDate() == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    gqlBuilder.append(String.format(" and %s.insert_time >= #startTime ", edge));
    gqlBuilder.append(String.format(" and %s.insert_time < #endTime ", edge));
    params.put("#startTime", String.valueOf(queryVO.getStartTimeDate().getTime() / 1000));
    params.put("#endTime", String.valueOf(queryVO.getEndTimeDate().getTime() / 1000));

    if (queryVO.getMinEstablishedSessions() != null) {
      gqlBuilder
          .append(String.format(" and %s.established_sessions >= #minEstablishedSessions ", edge));
      params.put("#minEstablishedSessions", String.valueOf(queryVO.getMinEstablishedSessions()));
    }

    if (queryVO.getMinTotalBytes() != null) {
      gqlBuilder.append(String.format(" and %s.total_bytes >= #minTotalBytes ", edge));
      params.put("#minTotalBytes", String.valueOf(queryVO.getMinTotalBytes()));
    }
  }

  /**
   * 过滤边插入时间
   * @param startTime
   * @param endTime
   * @param gqlBuilder
   * @param params
   */
  private void enrichTimeWhereGql(Date startTime, Date endTime, StringBuilder gqlBuilder,
      Map<String, String> params) {
    if (startTime == null || endTime == null) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "时间条件不能为空");
    }

    gqlBuilder.append(" and conversation.insert_time >= #startTime ");
    gqlBuilder.append(" and conversation.insert_time < #endTime ");
    params.put("#startTime", String.valueOf(startTime.getTime() / 1000));
    params.put("#endTime", String.valueOf(endTime.getTime() / 1000));
  }

  /**
   * 解析查询结果
   * @param resultSet
   * @return
   * @throws InvalidValueException
   * @throws UnsupportedEncodingException
   */
  private List<Map<String, Object>> praseResult(ResultSet resultSet)
      throws InvalidValueException, UnsupportedEncodingException {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> colNames = resultSet.keys();
    // 单行数据
    for (int i = 0; i < resultSet.rowsSize(); i++) {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      // 单列数据
      List<ValueWrapper> values = resultSet.rowValues(i).values();
      for (int j = 0; j < values.size(); j++) {
        praseValueWrapper(map, TextUtils.underLineToCamel(colNames.get(j)), values.get(j));
      }
      result.add(map);
    }

    return result;
  }

  /**
   * 解析查询结果的单列数据
   * @param map
   * @param column
   * @param value
   */
  private void praseValueWrapper(Map<String, Object> map, String column, ValueWrapper value) {
    try {
      if (value.isLong()) {
        map.put(column, value.asLong());
      }
      if (value.isBoolean()) {
        map.put(column, value.asBoolean());
      }
      if (value.isDouble()) {
        map.put(column, value.asDouble());
      }
      if (value.isString()) {
        map.put(column, value.asString());
      }
      if (value.isTime()) {
        map.put(column, value.asTime().toString());
      }
      if (value.isDate()) {
        map.put(column, value.asDate().toString());
      }
      if (value.isDateTime()) {
        map.put(column, value.asDateTime().toString());
      }
      if (value.isVertex()) {
        map.put(column, value.asNode().getId().asString());
      }
      if (value.isEdge()) {
        Relationship relationship = value.asRelationship();
        map.put("srcIp", relationship.srcId().asString());
        map.put("destIp", relationship.dstId().asString());
        relationship.properties().forEach((propertyKey, propertyValue) -> praseValueWrapper(map,
            TextUtils.underLineToCamel(propertyKey), propertyValue));
      }
      if (value.isPath()) {
        PathWrapper path = value.asPath();
        // nodes
        List<Node> nodes = path.getNodes();
        List<String> nodeIds = Lists.newArrayListWithCapacity(nodes.size());
        for (Node node : nodes) {
          nodeIds.add(node.getId().asString());
        }
        map.put("nodes", nodeIds);
        // relationships
        List<Relationship> relationships = path.getRelationships();
        List<Map<String, Object>> relationshipList = Lists
            .newArrayListWithCapacity(relationships.size());
        for (Relationship relationship : relationships) {
          Map<String,
              Object> relationshipMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          relationshipMap.put("srcIp", relationship.srcId().asString());
          relationshipMap.put("destIp", relationship.dstId().asString());
          relationship.properties()
              .forEach((propertyKey, propertyValue) -> praseValueWrapper(relationshipMap,
                  TextUtils.underLineToCamel(propertyKey), propertyValue));
          relationshipList.add(relationshipMap);
        }
        map.put("relationships", relationshipList);
      }
      if (value.isList()) {
        map.put(column, value.asList());
      }
      if (value.isSet()) {
        map.put(column, value.asSet());
      }
      if (value.isMap()) {
        map.put(column, value.asMap());
      }
    } catch (InvalidValueException | UnsupportedEncodingException e) {
      LOGGER.warn("prase valueWrapper failed.", e);
    }
  }

}
