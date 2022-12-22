package com.machloop.fpc.npm.metric.service.impl;

import java.math.BigDecimal;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.fpc.npm.metric.dao.MetricIpDao;
import com.machloop.fpc.npm.metric.dao.MetricProtocolPortDao;
import com.machloop.fpc.npm.metric.dao.MetricSessionRecordDao;
import com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService;
import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
@Transactional
@Service
public class MetricNetflowHistogramServiceImpl implements MetricNetflowHistogramService {

  @Autowired
  private MetricIpDao metricIpDao;

  @Autowired
  private MetricProtocolPortDao metricProtocolPortDao;

  @Autowired
  private MetricSessionRecordDao metricSessionRecordDao;

  @Autowired
  private MetricNetflowHistogramService metricNetflowHistogramService;

  private static final int topNumber = 10;

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#queryMetricIpHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> ipTableDataList = metricIpDao.queryIpTable(queryVO, sortProperty,
        sortDirection);
    if (ipTableDataList.size() == 0) {
      return result;
    }
    if (ipTableDataList.size() > topNumber) {
      ipTableDataList = ipTableDataList.subList(0, topNumber);
    }
    List<Map<String, Object>> ipHistogramDataList = metricIpDao.queryIpTableHistogram(queryVO,
        ipTableDataList);
    for (Map<String, Object> ipHistogramData : ipHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) ipHistogramData.get("ipv4_address");
      Inet6Address ipv6 = (Inet6Address) ipHistogramData.get("ipv6_address");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("totalBytes", MapUtils.getString(ipHistogramData, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(ipHistogramData, "total_packets"));
      item.put("timeStamp", MapUtils.getString(ipHistogramData, "timeStamp"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#queryMetricTransmitIpHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricTransmitIpHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<Map<String, Object>> transmitIpTableDataList = metricIpDao.queryTransmitIpTable(queryVO,
        sortProperty, sortDirection);
    if (transmitIpTableDataList.size() == 0) {
      return result;
    }
    if (transmitIpTableDataList.size() > topNumber) {
      transmitIpTableDataList = transmitIpTableDataList.subList(0, topNumber);
    }
    List<Map<String, Object>> ipHistogramDataList = metricIpDao
        .queryTransmitIpTableHistogram(queryVO, transmitIpTableDataList);
    for (Map<String, Object> ipHistogramData : ipHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) ipHistogramData.get("ipv4_address");
      Inet6Address ipv6 = (Inet6Address) ipHistogramData.get("ipv6_address");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("transmitBytes", MapUtils.getString(ipHistogramData, "transmit_bytes"));
      item.put("transmitPackets", MapUtils.getString(ipHistogramData, "transmit_packets"));
      item.put("timeStamp", MapUtils.getString(ipHistogramData, "timeStamp"));

      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#queryMetricIngestIpHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricIngestIpHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> ingestIpTableDataList = metricIpDao.queryIngestIpTable(queryVO,
        sortProperty, sortDirection);
    if (ingestIpTableDataList.size() == 0) {
      return result;
    }
    if (ingestIpTableDataList.size() > topNumber) {
      ingestIpTableDataList = ingestIpTableDataList.subList(0, topNumber);
    }
    List<Map<String, Object>> ipHistogramDataList = metricIpDao.queryIngestIpTableHistogram(queryVO,
        ingestIpTableDataList);
    for (Map<String, Object> ipHistogramData : ipHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) ipHistogramData.get("ipv4_address");
      Inet6Address ipv6 = (Inet6Address) ipHistogramData.get("ipv6_address");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("ingestBytes", MapUtils.getString(ipHistogramData, "ingest_bytes"));
      item.put("ingestPackets", MapUtils.getString(ipHistogramData, "ingest_packets"));
      item.put("timeStamp", MapUtils.getString(ipHistogramData, "timeStamp"));

      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#queryMetricProtocolPortHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricProtocolPortHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> protocolPortTableDataList = metricProtocolPortDao
        .queryProtocolPortTable(queryVO, sortProperty, sortDirection);
    if (protocolPortTableDataList.size() == 0) {
      return result;
    }
    if (protocolPortTableDataList.size() > topNumber) {
      protocolPortTableDataList = protocolPortTableDataList.subList(0, topNumber);
    }
    List<String> protocolPortList = protocolPortTableDataList.stream()
        .map(e -> MapUtils.getString(e, "protocol_port")).collect(Collectors.toList());

    List<Map<String, Object>> protocolPortHistogramDataList = metricProtocolPortDao
        .queryProtocolPortTableHistogram(queryVO, protocolPortList);
    for (Map<String, Object> protocolPortHistogramData : protocolPortHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("protocol", MapUtils.getString(protocolPortHistogramData, "protocol"));
      item.put("port", MapUtils.getString(protocolPortHistogramData, "port"));
      item.put("totalBytes", MapUtils.getString(protocolPortHistogramData, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(protocolPortHistogramData, "total_packets"));
      item.put("timeStamp", MapUtils.getString(protocolPortHistogramData, "timeStamp"));

      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#querySessionRecordIpHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordIpHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> ipTableDataList = metricSessionRecordDao.queryIpTable(queryVO,
        sortProperty, sortDirection);
    if (ipTableDataList.size() == 0) {
      return result;
    }
    if (ipTableDataList.size() > topNumber) {
      ipTableDataList = ipTableDataList.subList(0, topNumber);
    }
    List<Map<String, Object>> ipHistogramDataList = metricSessionRecordDao
        .queryIpTableHistogram(queryVO, ipTableDataList);
    for (Map<String, Object> ipHistogramData : ipHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) ipHistogramData.get("ipv4");
      Inet6Address ipv6 = (Inet6Address) ipHistogramData.get("ipv6");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("totalBytes", MapUtils.getString(ipHistogramData, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(ipHistogramData, "total_packets"));
      item.put("timeStamp", MapUtils.getString(ipHistogramData, "timeStamp"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#querySessionRecordTransmitIpHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordTransmitIpHistogram(
      MetricNetflowQueryVO queryVO, String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> transmitIpTableDataList = metricSessionRecordDao
        .queryTransmitIpTable(queryVO, sortProperty, sortDirection);
    if (transmitIpTableDataList.size() == 0) {
      return result;
    }
    if (transmitIpTableDataList.size() > topNumber) {
      transmitIpTableDataList = transmitIpTableDataList.subList(0, topNumber);
    }

    List<Map<String, Object>> ipHistogramDataList = metricSessionRecordDao
        .queryTransmitIpTableHistogram(queryVO, transmitIpTableDataList);
    for (Map<String, Object> ipHistogramData : ipHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) ipHistogramData.get("ipv4");
      Inet6Address ipv6 = (Inet6Address) ipHistogramData.get("ipv6");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("transmitBytes", MapUtils.getString(ipHistogramData, "transmit_bytes"));
      item.put("transmitPackets", MapUtils.getString(ipHistogramData, "transmit_packets"));
      item.put("timeStamp", MapUtils.getString(ipHistogramData, "timeStamp"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#querySessionRecordIngestIpHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordIngestIpHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> ingestIpTableDataList = metricSessionRecordDao
        .queryIngestIpTable(queryVO, sortProperty, sortDirection);
    if (ingestIpTableDataList.size() == 0) {
      return result;
    }
    if (ingestIpTableDataList.size() > topNumber) {
      ingestIpTableDataList = ingestIpTableDataList.subList(0, topNumber);
    }

    List<Map<String, Object>> ipHistogramDataList = metricSessionRecordDao
        .queryIngestIpTableHistogram(queryVO, ingestIpTableDataList);
    for (Map<String, Object> ipHistogramData : ipHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) ipHistogramData.get("ipv4");
      Inet6Address ipv6 = (Inet6Address) ipHistogramData.get("ipv6");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("ingestBytes", MapUtils.getString(ipHistogramData, "ingest_bytes"));
      item.put("ingestPackets", MapUtils.getString(ipHistogramData, "ingest_packets"));
      item.put("timeStamp", MapUtils.getString(ipHistogramData, "timeStamp"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#querySessionRecordSessionHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordSessionHistogram(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> sessionTableDataList = metricSessionRecordDao
        .querySessionTable(queryVO, sortProperty, sortDirection);
    if (sessionTableDataList.size() == 0) {
      return result;
    }
    if (sessionTableDataList.size() > topNumber) {
      sessionTableDataList = sessionTableDataList.subList(0, topNumber);
    }
    List<String> sessionIdList = sessionTableDataList.stream()
        .map(e -> MapUtils.getString(e, "session_id")).collect(Collectors.toList());
    List<Map<String, Object>> sessionHistogramDataList = metricSessionRecordDao
        .querySessionTableHistogram(queryVO, sessionIdList);

    for (Map<String, Object> sessionHistogramData : sessionHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("sessionId", MapUtils.getString(sessionHistogramData, "session_id"));
      Inet4Address srcIpv4 = (Inet4Address) sessionHistogramData.get("src_ipv4");
      Inet6Address srcIpv6 = (Inet6Address) sessionHistogramData.get("src_ipv6");
      item.put("srcIp", srcIpv4 == null ? srcIpv6.getHostAddress() : srcIpv4.getHostAddress());
      Inet4Address destIpv4 = (Inet4Address) sessionHistogramData.get("dest_ipv4");
      Inet6Address destIpv6 = (Inet6Address) sessionHistogramData.get("dest_ipv6");
      item.put("destIp", destIpv4 == null ? destIpv6.getHostAddress() : destIpv4.getHostAddress());
      item.put("srcPort", MapUtils.getString(sessionHistogramData, "src_port"));
      item.put("destPort", MapUtils.getString(sessionHistogramData, "dest_port"));
      item.put("totalBytes", MapUtils.getString(sessionHistogramData, "total_bytes"));
      item.put("timeStamp", MapUtils.getString(sessionHistogramData, "timeStamp"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#querySessionRecordProtocolPortHistogram(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordProtocolPortHistogram(
      MetricNetflowQueryVO queryVO, String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> protocolPortTableDataList = metricSessionRecordDao
        .queryProtocolportTable(queryVO, sortProperty, sortDirection);
    if (protocolPortTableDataList.size() == 0) {
      return result;
    }
    if (protocolPortTableDataList.size() > topNumber) {
      protocolPortTableDataList = protocolPortTableDataList.subList(0, topNumber);
    }
    List<String> protocolPortList = protocolPortTableDataList.stream()
        .map(e -> (MapUtils.getString(e, "protocol") + "_" + MapUtils.getString(e, "port")))
        .collect(Collectors.toList());

    List<Map<String, Object>> protocolPortHistogramDataList = metricSessionRecordDao
        .queryProtocolportTableHistogram(queryVO, protocolPortList);
    for (Map<String, Object> protocolPortHistogramData : protocolPortHistogramDataList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("protocol", MapUtils.getString(protocolPortHistogramData, "protocol"));
      item.put("port", MapUtils.getString(protocolPortHistogramData, "port"));
      item.put("totalBytes", MapUtils.getString(protocolPortHistogramData, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(protocolPortHistogramData, "total_packets"));
      item.put("timeStamp", MapUtils.getString(protocolPortHistogramData, "timeStamp"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowHistogramService#queryNetflowDashboard(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, int)
   */
  @Override
  public Map<String, Object> queryNetflowDashboard(MetricNetflowQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<Map<String, Object>> metricNetflowList = metricIpDao.queryMetricNetflowIndex(queryVO);

    long timeInterval = (queryVO.getEndTimeDate().getTime() - queryVO.getStartTimeDate().getTime())
        / Constants.NUM_1000;
    if (timeInterval < Constants.FIVE_MINUTE_SECONDS) {
      timeInterval = Constants.FIVE_MINUTE_SECONDS;
    }

    // Mbps转成bps
    double netifSpeed = queryVO.getNetifSpeed() * Constants.NUM_1000 * Constants.NUM_1000;

    // 统计指标
    Map<String, Object> metricNetflowData = metricNetflowList.get(0);
    result.put("totalBytes", metricNetflowData.get("total_bytes"));
    result.put("totalPackets", metricNetflowData.get("total_packets"));
    result.put("totalBandwidth",
        calcuBandwidth(MapUtils.getLongValue(metricNetflowData, "total_bytes", 0L), timeInterval));
    result.put("totalPacketSpeed",
        MapUtils.getLongValue(metricNetflowData, "total_packets", 0L) / timeInterval);

    if (queryVO.getNetifSpeed() != 0) {
      result.put("transmitBandwidthRatio",
          calcuBandwidth(MapUtils.getLongValue(metricNetflowData, "transmit_bytes", 0L),
              timeInterval) / netifSpeed);
      result.put("ingestBandwidthRatio",
          calcuBandwidth(MapUtils.getLongValue(metricNetflowData, "ingest_bytes", 0L), timeInterval)
              / netifSpeed);
    }

    if (StringUtils.isNotBlank(queryVO.getNetifNo())) {
      // 接口出入方向平均带宽
      List<Map<String, Object>> transmitIngestBytesDataList = metricIpDao
          .querytransmitIngestBytesDataAggregate(queryVO);
      List<Map<String, Object>> inAndOutBandwidthList = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      for (Map<String, Object> transmitIngestBandwidthData : transmitIngestBytesDataList) {
        transmitIngestBandwidthData.put("transmitBandwidth",
            calcuBandwidth(MapUtils.getLongValue(transmitIngestBandwidthData, "transmit_bytes", 0L),
                (long) queryVO.getInterval()));
        transmitIngestBandwidthData.put("ingestBandwidth",
            calcuBandwidth(MapUtils.getLongValue(transmitIngestBandwidthData, "ingest_bytes", 0L),
                (long) queryVO.getInterval()));
        inAndOutBandwidthList.add(transmitIngestBandwidthData);
      }
      result.put("transmitIngestBandwidthHistogram", inAndOutBandwidthList);
    }

    // 平均发送带宽top10IP
    List<Map<String, Object>> transmitBandwidthHistogram = metricNetflowHistogramService
        .queryMetricTransmitIpHistogram(queryVO, "transmit_bytes", "desc");
    for (Map<String, Object> transmitData : transmitBandwidthHistogram) {
      transmitData.put("transmitBandwidth",
          calcuBandwidth(MapUtils.getLongValue(transmitData, "transmitBytes", 0L), timeInterval));
    }
    result.put("transmitBandwidthHistogram", transmitBandwidthHistogram);

    // 平均接收带宽top10IP
    List<Map<String, Object>> ingestBandwidthHistogram = metricNetflowHistogramService
        .queryMetricIngestIpHistogram(queryVO, "ingest_bytes", "desc");
    for (Map<String, Object> ingestData : ingestBandwidthHistogram) {
      ingestData.put("ingestBandwidth",
          calcuBandwidth(MapUtils.getLongValue(ingestData, "ingestBytes", 0L), timeInterval));
    }
    result.put("ingestBandwidthHistogram", ingestBandwidthHistogram);

    // 平均总带宽top10IP
    List<Map<String, Object>> totalBandwidthHistogram = metricNetflowHistogramService
        .queryMetricIpHistogram(queryVO, "total_bytes", "desc");
    for (Map<String, Object> totalData : totalBandwidthHistogram) {
      totalData.put("totalBandwidth",
          calcuBandwidth(MapUtils.getLongValue(totalData, "totalBytes", 0L), timeInterval));
    }
    result.put("totalBandwidthHistogram", totalBandwidthHistogram);

    // top10协议端口平均带宽
    List<Map<String, Object>> protocolPortBandwidthHistogram = metricNetflowHistogramService
        .queryMetricProtocolPortHistogram(queryVO, "total_bytes", "desc");
    for (Map<String, Object> protocolPortData : protocolPortBandwidthHistogram) {
      protocolPortData.put("totalBandwidth",
          calcuBandwidth(MapUtils.getLongValue(protocolPortData, "totalBytes", 0L), timeInterval));
    }
    result.put("protocolPortBandwidthHistogram", protocolPortBandwidthHistogram);

    // top10会话平均带宽
    List<Map<String, Object>> sessionBandwidthHistogram = metricNetflowHistogramService
        .querySessionRecordSessionHistogram(queryVO, "total_bytes", "desc");
    for (Map<String, Object> sessionData : sessionBandwidthHistogram) {
      sessionData.put("totalBandwidth",
          calcuBandwidth(MapUtils.getLongValue(sessionData, "totalBytes", 0L), timeInterval));
    }
    result.put("sessionBandwidthHistogram", sessionBandwidthHistogram);

    return result;
  }

  private static double calcuBandwidth(Long currentBytes, Long timeInterval) {
    BigDecimal bg = new BigDecimal(currentBytes * Constants.BYTE_BITS / timeInterval);
    return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
  }
}
