package com.machloop.fpc.manager.metric.dao.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.global.dao.ElasticIndexDao;
import com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl;
import com.machloop.fpc.manager.helper.Spl2DslHelper;
import com.machloop.fpc.manager.metric.dao.MetricDhcpDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricDhcpDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2020年12月10日, fpc-manager
 */
// @Repository
public class MetricDhcpDataRecordDaoImpl extends AbstractDataRecordDaoImpl
    implements MetricDhcpDataRecordDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricDhcpDataRecordDaoImpl.class);

  private static final List<Tuple2<String, Boolean>> SERVER_TERM_FIELD = Lists.newArrayList(
      Tuples.of("dhcp_version", false), Tuples.of("server_ip_address", false),
      Tuples.of("server_mac_address", false));

  private static final List<Tuple2<String, Boolean>> CLIENT_TERM_FIELD = Lists.newArrayList(
      Tuples.of("dhcp_version", false), Tuples.of("client_ip_address", false),
      Tuples.of("client_mac_address", false));

  private static final List<Tuple2<String, Boolean>> MESSAGE_TYPE_TERM_FIELD = Lists
      .newArrayList(Tuples.of("dhcp_version", false), Tuples.of("message_type", false));

  private static final List<
      Tuple2<String, Boolean>> AGGS_TERM_FIELD = Lists.newArrayList(Tuples.of("network_id", false),
          Tuples.of("client_ip_address", false), Tuples.of("server_ip_address", false),
          Tuples.of("client_mac_address", false), Tuples.of("server_mac_address", false),
          Tuples.of("message_type", false), Tuples.of("dhcp_version", false));

  @Autowired
  private RestHighLevelClient restHighLevelClient;

  @Autowired
  private ElasticIndexDao elasticIndexDao;

  @Autowired
  private Spl2DslHelper spl2DslHelper;

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDhcpDataRecordDao#queryMetricDhcpRawdatas(com.machloop.fpc.manager.metric.vo.MetricQueryVO)
   */
  public List<Map<String, Object>> queryMetricDhcpRawdatas(MetricQueryVO queryVO) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      result = queryMetricDataRecord(queryVO, convertIndexAlias(queryVO.getSourceType(),
          queryVO.getInterval(), queryVO.getPacketFileId()));
    } catch (IOException e) {
      LOGGER.warn("failed to query dhcp metric.", e);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDhcpDataRecordDao#queryMetricDhcps(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricDhcpDataRecordDO> queryMetricDhcps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String type) {
    Map<String, String> aggsFields = getAggsFields();

    List<
        MetricDhcpDataRecordDO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    try {
      List<Tuple2<String, Boolean>> termFields = Lists.newArrayList();
      switch (type) {
        case FpcConstants.METRIC_TYPE_DHCP_SERVER:
          termFields.addAll(SERVER_TERM_FIELD);
          break;
        case FpcConstants.METRIC_TYPE_DHCP_CLIENT:
          termFields.addAll(CLIENT_TERM_FIELD);
          break;
        case FpcConstants.METRIC_TYPE_DHCP_MESSAGE_TYPE:
          termFields.addAll(MESSAGE_TYPE_TERM_FIELD);
          break;
        default:
          return result;
      }

      Map<String, Object> after = null;
      int currentBatchSize = 0;
      List<Map<String, Object>> tempResult = Lists.newArrayListWithCapacity(0);
      do {
        Tuple2<Map<String, Object>,
            List<Map<String, Object>>> batchResult = compositeTermMetricAggregate(
                Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
                Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()),
                convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
                    queryVO.getPacketFileId()),
                termFields, queryVO.getDsl(), null, aggsFields, Lists.newArrayList(), sortProperty,
                sortDirection, COMPOSITE_BATCH_SIZE, after);

        after = batchResult.getT1();
        currentBatchSize = batchResult.getT2().size();
        List<Map<String, Object>> temp = batchResult.getT2();
        if (temp.size() > TOP_SIZE) {
          temp = temp.subList(0, TOP_SIZE);
        }
        tempResult.addAll(temp);
        tempResult = sortMetricResult(tempResult, sortProperty, sortDirection);
      } while (currentBatchSize == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());

      tempResult.forEach(item -> result.add(tranResultMapToDateRecord(item)));
    } catch (IOException e) {
      LOGGER.warn("failed to query dhcp metric.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.dao.MetricDhcpDataRecordDao#queryMetricDhcpHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, java.util.List, java.lang.String, java.util.List)
   */
  @Override
  public List<Map<String, Object>> queryMetricDhcpHistograms(MetricQueryVO queryVO,
      List<Tuple2<String, Boolean>> termFields, String aggsField,
      List<Map<String, Object>> combinationConditions) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    try {
      Map<String, String> allAggsFields = getAggsFields();
      if (!allAggsFields.containsKey(aggsField)) {
        return result;
      }
      Map<String, String> aggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      aggsFields.put(aggsField, allAggsFields.get(aggsField));

      // 生成附加条件
      BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
      combinationConditions.forEach(combinationCondition -> {
        BoolQueryBuilder itemQuery = QueryBuilders.boolQuery();
        combinationCondition.entrySet().forEach(entry -> {
          if (entry.getValue() != null) {
            itemQuery.must(QueryBuilders.termQuery(entry.getKey(), entry.getValue()));
          } else {
            itemQuery.mustNot(QueryBuilders.existsQuery(entry.getKey()));
          }
        });

        boolQuery.should(itemQuery);
      });

      Map<String, Object> after = null;
      List<Map<String, Object>> batchList = null;
      do {
        Tuple2<Map<String, Object>,
            List<Map<String, Object>>> batchResult = compositeDateHistogramTermMetricAggregate(
                Tuples.of(queryVO.getStartTimeDate(), queryVO.getIncludeStartTime()),
                Tuples.of(queryVO.getEndTimeDate(), queryVO.getIncludeEndTime()),
                queryVO.getInterval(),
                convertIndexAlias(queryVO.getSourceType(), queryVO.getInterval(),
                    queryVO.getPacketFileId()),
                termFields, queryVO.getDsl(), boolQuery, aggsFields, Lists.newArrayList(),
                COMPOSITE_BATCH_SIZE, after);
        after = batchResult.getT1();
        batchList = batchResult.getT2();

        result.addAll(batchList);
      } while (batchList.size() == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());
    } catch (IOException e) {
      LOGGER.warn("failed to query dhcp datehistogram.", e);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getElasticIndexDao()
   */
  @Override
  protected ElasticIndexDao getElasticIndexDao() {
    return elasticIndexDao;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getRestHighLevelClient()
   */
  @Override
  protected RestHighLevelClient getRestHighLevelClient() {
    return restHighLevelClient;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getSpl2DslHelper()
   */
  @Override
  protected Spl2DslHelper getSpl2DslHelper() {
    return spl2DslHelper;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexName()
   */
  @Override
  protected String getIndexName() {
    return ManagerConstants.INDEX_METRIC_DHCP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#getIndexAliasName()
   */
  @Override
  protected String getIndexAliasName() {
    return ManagerConstants.ALIAS_METRIC_DHCP_DATA_RECORD;
  }

  /**
   * @see com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl#aggregate(java.util.Date, java.util.Date, int, java.lang.String, com.machloop.fpc.manager.global.dao.impl.AbstractDataRecordDaoImpl.WriteCallback)
   */
  @Override
  protected int aggregate(Date startTime, Date endTime, int interval, String indexName,
      WriteCallback writeCallback) throws IOException {
    int success = 0;
    Map<String, String> aggsFields = getAggsFields();

    Map<String, Object> after = null;
    List<Map<String, Object>> batchList = null;
    do {
      List<String> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      Tuple2<Map<String, Object>,
          List<Map<String, Object>>> batchResult = compositeTermMetricAggregate(startTime, endTime,
              indexName, AGGS_TERM_FIELD, aggsFields, COMPOSITE_BATCH_SIZE, after);

      after = batchResult.getT1();
      batchList = batchResult.getT2();

      batchList.forEach(item -> {
        item.put("timestamp", endTime);
        result.add(JsonHelper.serialize(tranResultMapToDateRecord(item)));
      });
      success += writeCallback.process(result);
    } while (batchList.size() == COMPOSITE_BATCH_SIZE && !GracefulShutdownHelper.isShutdownNow());

    return success;
  }

  private Map<String, String> getAggsFields() {
    Map<String, String> aggsFields = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggsFields.put("total_bytes", "sum");
    aggsFields.put("total_packets", "sum");
    aggsFields.put("send_bytes", "sum");
    aggsFields.put("send_packets", "sum");
    aggsFields.put("receive_bytes", "sum");
    aggsFields.put("receive_packets", "sum");
    return aggsFields;
  }

  private MetricDhcpDataRecordDO tranResultMapToDateRecord(Map<String, Object> item) {
    MetricDhcpDataRecordDO recordDO = new MetricDhcpDataRecordDO();
    recordDO.setTimestamp((Date) item.get("timestamp"));
    recordDO.setNetworkId(MapUtils.getString(item, "network_id"));
    recordDO.setClientIpAddress(MapUtils.getString(item, "client_ip_address"));
    recordDO.setServerIpAddress(MapUtils.getString(item, "server_ip_address"));
    recordDO.setClientMacAddress(MapUtils.getString(item, "client_mac_address"));
    recordDO.setServerMacAddress(MapUtils.getString(item, "server_mac_address"));
    recordDO.setMessageType(MapUtils.getIntValue(item, "message_type"));
    recordDO.setDhcpVersion(MapUtils.getIntValue(item, "dhcp_version"));

    recordDO.setTotalBytes(MapUtils.getLongValue(item, "total_bytes"));
    recordDO.setTotalPackets(MapUtils.getLongValue(item, "total_packets"));
    recordDO.setSendBytes(MapUtils.getLongValue(item, "send_bytes"));
    recordDO.setSendPackets(MapUtils.getLongValue(item, "send_packets"));
    recordDO.setReceiveBytes(MapUtils.getLongValue(item, "receive_bytes"));
    recordDO.setReceivePackets(MapUtils.getLongValue(item, "receive_packets"));
    return recordDO;
  }

}
