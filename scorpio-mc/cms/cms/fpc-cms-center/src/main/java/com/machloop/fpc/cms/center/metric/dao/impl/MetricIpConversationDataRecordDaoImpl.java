package com.machloop.fpc.cms.center.metric.dao.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.Base64Utils;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.boot.configuration.ClickHouseJdbcTemplate;
import com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.cms.center.helper.Spl2SqlHelper;
import com.machloop.fpc.cms.center.metric.dao.MetricIpConversationDataRecordDao;
import com.machloop.fpc.cms.center.metric.data.MetricIpConversationDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.common.helper.AggsFunctionEnum;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author fengtianyou
 *
 * create at 2021年8月17日, fpc-manager
 */
@Repository
public class MetricIpConversationDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricIpConversationDataRecordDao {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MetricIpConversationDataRecordDaoImpl.class);

  private static final Map<String, String> PRIMARY_TERM_KEY;
  private static final Map<String, String> AGGS_TERM_KEY;
  static {
    PRIMARY_TERM_KEY = new HashMap<String, String>();
    AGGS_TERM_KEY = new HashMap<String, String>();
    PRIMARY_TERM_KEY.put("ip_a_address", "ipAAddress");
    PRIMARY_TERM_KEY.put("ip_b_address", "ipBAddress");
    AGGS_TERM_KEY.put("network_id", "networkId");
    AGGS_TERM_KEY.put("service_id", "serviceId");
    AGGS_TERM_KEY.put("ip_a_address", "ipAAddress");
    AGGS_TERM_KEY.put("ip_b_address", "ipBAddress");
  }

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  @Autowired
  private ClickHouseJdbcTemplate jdbcTemplate;

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getSpl2SqlHelper()
   */
  @Override
  protected Spl2SqlHelper getSpl2SqlHelper() {
    return spl2SqlHelper;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getClickHouseJdbcTemplate()
   */
  @Override
  protected ClickHouseJdbcTemplate getClickHouseJdbcTemplate() {
    return jdbcTemplate;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.global.dao.impl.AbstractDataRecordDaoImpl#getTableName()
   */
  @Override
  protected String getTableName() {
    return CenterConstants.TABLE_METRIC_IP_CONVERSATION_DATA_RECORD;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricIpConversationDataRecordDao#queryMetricIpConversationRawdatas(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversationRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      result = queryMetricDataRecord(queryVO);
    } catch (IOException e) {
      LOGGER.warn("failed to query ipConversation metric.", e);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricIpConversationDataRecordDao#queryMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricIpConversationDataRecordDO> queryMetricIpConversations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<MetricIpConversationDataRecordDO> result = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(aggsFields);
    setSpecialKpiAggs(aggsFields);

    try {
      List<Map<String, Object>> batchResult = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, null, PRIMARY_TERM_KEY, aggsFields, sortProperty, sortDirection);
      batchResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query ipConversation metric.", e);
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> graphMetricIpConversations(MetricQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      // 标识查询，用于取消查询
      String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
          ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
          : "";

      // 构造过滤条件
      StringBuilder whereSql = new StringBuilder();
      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      enrichWhereSql(queryVO, whereSql, params);
      fillAdditionalConditions(parseQuerySource(queryVO), whereSql, params);

      // 聚合字段
      Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      aggsFields.put("totalBytes", Tuples.of(AggsFunctionEnum.SUM, "total_bytes"));
      aggsFields.put("establishedSessions",
          Tuples.of(AggsFunctionEnum.SUM, "established_sessions"));

      // 构造查询语句
      StringBuilder sql = new StringBuilder(securityQueryId);
      // 分组字段
      List<String> terms = PRIMARY_TERM_KEY.entrySet().stream()
          .map(entry -> entry.getKey() + " AS " + entry.getValue()).collect(Collectors.toList());
      sql.append(" select ").append(StringUtils.join(terms, ","));
      // 聚合字段
      aggsFields.entrySet().forEach(entry -> {
        sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
            .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
      });
      sql.append(" from ").append(convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
          queryVO.getPacketFileId()));
      sql.append(whereSql.toString());
      sql.append(" group by ").append(StringUtils.join(PRIMARY_TERM_KEY.values(), ","));
      sql.append(" having 1=1 ");
      if (minEstablishedSessions != null) {
        sql.append(" and establishedSessions >= :minEstablishedSessions ");
        params.put("minEstablishedSessions", minEstablishedSessions);
      }
      if (minTotalBytes != null) {
        sql.append(" and totalBytes >= :minTotalBytes ");
        params.put("minTotalBytes", minTotalBytes);
      }
      sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
          .append(sortDirection);
      if (queryVO.getCount() > 0) {
        sql.append(" limit ").append(queryVO.getCount());
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("graphMetricIpConversations sql : [{}], param: [{}] ", sql.toString(), params);
      }

      result = queryWithExceptionHandle(sql.toString(), params, new ColumnMapRowMapper());
    } catch (RuntimeException e) {
      LOGGER.warn("failed to graph ipConversation metric.", e);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricIpConversationDataRecordDao#countMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> countMetricIpConversations(MetricQueryVO queryVO,
      String aggsField, String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(allAggsFields);
    setSpecialKpiAggs(allAggsFields);
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    getCombinationAggsFields(aggsField).forEach(item -> {
      if (allAggsFields.containsKey(item)) {
        aggsFields.put(item, allAggsFields.get(item));
      }
    });
    aggsFields.put("upstreamBytes", allAggsFields.get("upstreamBytes"));
    aggsFields.put("downstreamBytes", allAggsFields.get("downstreamBytes"));

    // 附加过滤条件
    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      for (String item : queryVO.getNetworkIds()) {
        Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        itemMap.put("network_id", item);
        itemMap.put("service_id", "");
        combinationConditions.add(itemMap);
      }
      // 已过滤，避免重复过滤
      queryVO.setNetworkIds(null);
    } else if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      for (Tuple2<String, String> item : queryVO.getServiceNetworkIds()) {
        Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        itemMap.put("network_id", item.getT1());
        itemMap.put("service_id", item.getT2());
        combinationConditions.add(itemMap);
      }
      // 已过滤，避免重复过滤
      queryVO.setServiceNetworkIds(null);
    } else {
      Map<String, Object> itemMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      String networkId = StringUtils.equals(queryVO.getSourceType(),
          FpcCmsConstants.SOURCE_TYPE_PACKET_FILE) ? queryVO.getPacketFileId()
              : queryVO.getNetworkId();
      itemMap.put("network_id", networkId);
      itemMap.put("service_id", StringUtils.defaultIfBlank(queryVO.getServiceId(), ""));
      combinationConditions.add(itemMap);
      // 已过滤，避免重复过滤
      queryVO.setNetworkId(null);
      queryVO.setServiceId(null);
    }

    try {
      result = termMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, PRIMARY_TERM_KEY, aggsFields, sortProperty,
          sortDirection);
    } catch (IOException e) {
      LOGGER.warn("failed to count ipConversation metric.", e);
    }

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.dao.MetricIpConversationDataRecordDao#queryMetricIpConversationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversationHistograms(MetricQueryVO queryVO,
      String aggsField, List<Map<String, Object>> combinationConditions) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> allAggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    setAggsFields(allAggsFields);
    setSpecialKpiAggs(allAggsFields);
    final Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (aggsField.contains(CenterConstants.METRIC_NPM_ALL_AGGSFILED)) {
      aggsFields.putAll(allAggsFields);
    } else {
      getCombinationAggsFields(aggsField).forEach(item -> {
        if (allAggsFields.containsKey(item)) {
          aggsFields.put(item, allAggsFields.get(item));
        }
      });
    }
    if (MapUtils.isEmpty(aggsFields)) {
      return result;
    }

    try {
      List<Map<String, Object>> batchResult = dateHistogramTermMetricAggregate(
          convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
              queryVO.getPacketFileId()),
          queryVO, combinationConditions, PRIMARY_TERM_KEY, aggsFields);
      result.addAll(batchResult);
    } catch (IOException e) {
      LOGGER.warn("failed to query ipConversation histograms.", e);
    }

    return result;
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationIpConversations(
      MetricQueryVO queryVO, String sortProperty, String sortDirection) {
    // 标识查询，用于取消查询
    String securityQueryId = StringUtils.isNotBlank(queryVO.getQueryId())
        ? String.format("/*%s*/", Base64Utils.encode(queryVO.getQueryId()))
        : "";

    // 构造查询语句
    StringBuilder sql = new StringBuilder(securityQueryId);
    sql.append(" select network_id as networkId ");

    // 聚合字段
    Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    kpiAggs(aggsFields);
    if (!StringUtils.equals(queryVO.getColumns(), "*")) {
      // 过滤要查询的列
      Set<String> fields = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      CsvUtils.convertCSVToList(queryVO.getColumns()).forEach(
          field -> fields.addAll(getCombinationAggsFields(TextUtils.camelToUnderLine(field))));
      fields.addAll(getCombinationAggsFields(sortProperty));

      aggsFields = aggsFields.entrySet().stream()
          .filter(aggsField -> fields.contains(aggsField.getKey()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    aggsFields.entrySet().forEach(entry -> {
      sql.append(", ").append(entry.getValue().getT1().getOperation()).append("(")
          .append(entry.getValue().getT2()).append(") AS ").append(entry.getKey());
    });
    sql.append(" from ").append(convertTableName(queryVO.getSourceType(), queryVO.getInterval(),
        queryVO.getPacketFileId()));
    // 构造过滤条件
    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    enrichWhereSql(queryVO, whereSql, params);
    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      whereSql.append(" and network_id in (:networkIds)");
      params.put("networkIds", queryVO.getNetworkIds());
    }
    sql.append(whereSql);
    sql.append(" group by networkId ");
    sql.append(" order by ").append(TextUtils.underLineToCamel(sortProperty)).append(" ")
        .append(sortDirection);
    List<Map<String, Object>> result = queryWithExceptionHandle(sql.toString(), params,
        new ColumnMapRowMapper());
    sortMetricResult(result, sortProperty, sortDirection);

    return result;
  }

  private void setAggsFields(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    kpiAggs(aggsFields);
    aggsFields.put("downstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "downstream_bytes"));
    aggsFields.put("downstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "downstream_packets"));
    aggsFields.put("upstreamBytes", Tuples.of(AggsFunctionEnum.SUM, "upstream_bytes"));
    aggsFields.put("upstreamPackets", Tuples.of(AggsFunctionEnum.SUM, "upstream_packets"));
    aggsFields.put("activeEstablishedSessions",
        Tuples.of(AggsFunctionEnum.SUM, "active_established_sessions"));
    aggsFields.put("passiveEstablishedSessions",
        Tuples.of(AggsFunctionEnum.SUM, "passive_established_sessions"));
  }

  private void setSpecialKpiAggs(Map<String, Tuple2<AggsFunctionEnum, String>> aggsFields) {
    specialKpiAggs(aggsFields);
  }

  private MetricIpConversationDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricIpConversationDataRecordDO recordDO = new MetricIpConversationDataRecordDO();
    String IpAAddress = MapUtils.getString(item, "ipAAddress");
    recordDO.setIpAAddress(StringUtils.equals(IpAAddress, "null") ? null : IpAAddress);
    String IpBAddress = MapUtils.getString(item, "ipBAddress");
    recordDO.setIpBAddress(StringUtils.equals(IpBAddress, "null") ? null : IpBAddress);

    tranKPIMapToDateRecord(item, recordDO);
    recordDO.setDownstreamBytes(MapUtils.getLongValue(item, "downstreamBytes"));
    recordDO.setDownstreamPackets(MapUtils.getLongValue(item, "downstreamPackets"));
    recordDO.setUpstreamBytes(MapUtils.getLongValue(item, "upstreamBytes"));
    recordDO.setUpstreamPackets(MapUtils.getLongValue(item, "upstreamPackets"));
    recordDO.setActiveEstablishedSessions(MapUtils.getLongValue(item, "activeEstablishedSessions"));
    recordDO
        .setPassiveEstablishedSessions(MapUtils.getLongValue(item, "passiveEstablishedSessions"));

    return recordDO;
  }

  /**
   * 解析实际查询的网络业务
   * @param queryVO
   * @return
   */
  private List<Map<String, Object>> parseQuerySource(MetricQueryVO queryVO) {
    List<Map<String, Object>> sourceConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    if (CollectionUtils.isNotEmpty(queryVO.getNetworkIds())) {
      sourceConditions = queryVO.getNetworkIds().stream().map(networkId -> {
        Map<String, Object> termKey = Maps.newHashMap();
        termKey.put("network_id", networkId);
        return termKey;
      }).collect(Collectors.toList());
    } else if (CollectionUtils.isNotEmpty(queryVO.getServiceNetworkIds())) {
      sourceConditions = queryVO.getServiceNetworkIds().stream().map(serviceNetworkId -> {
        Map<String, Object> termKey = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        termKey.put("service_id", serviceNetworkId.getT1());
        termKey.put("network_id", serviceNetworkId.getT2());

        return termKey;
      }).collect(Collectors.toList());
    }

    return sourceConditions;
  }

}
