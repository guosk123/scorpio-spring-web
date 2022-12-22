package com.machloop.fpc.npm.metric.service.impl;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.fpc.manager.global.service.SlowQueryService;
import com.machloop.fpc.npm.metric.dao.MetricIpDao;
import com.machloop.fpc.npm.metric.dao.MetricProtocolPortDao;
import com.machloop.fpc.npm.metric.dao.impl.MetricSessionRecordDaoImpl;
import com.machloop.fpc.npm.metric.service.MetricNetflowTableService;
import com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2021年9月26日, fpc-manager
 */
@Transactional
@Service
public class MetricNetflowTableServiceImpl implements MetricNetflowTableService {

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#queryMetricIp(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Autowired
  private MetricIpDao metricIpDao;

  @Autowired
  private MetricProtocolPortDao metricProtocolPortDao;

  @Autowired
  private SlowQueryService slowQueryService;

  @Autowired
  private MetricSessionRecordDaoImpl metricSessionRecordDaoImpl;

  @Override
  public List<Map<String, Object>> queryMetricIp(MetricNetflowQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<Map<String, Object>> recordList = metricIpDao.queryIpTable(queryVO, sortProperty,
        sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (Map<String, Object> tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) tmp.get("ipv4_address");
      Inet6Address ipv6 = (Inet6Address) tmp.get("ipv6_address");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("totalBytes", MapUtils.getString(tmp, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(tmp, "total_packets"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#queryMetricTransmitIp(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricTransmitIp(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> recordList = metricIpDao.queryTransmitIpTable(queryVO, sortProperty,
        sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (Map<String, Object> tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) tmp.get("ipv4_address");
      Inet6Address ipv6 = (Inet6Address) tmp.get("ipv6_address");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("transmitBytes", MapUtils.getString(tmp, "transmit_bytes"));
      item.put("transmitPackets", MapUtils.getString(tmp, "transmit_packets"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#queryMetricIngestIp(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricIngestIp(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> recordList = metricIpDao.queryIngestIpTable(queryVO, sortProperty,
        sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (Map<String, Object> tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) tmp.get("ipv4_address");
      Inet6Address ipv6 = (Inet6Address) tmp.get("ipv6_address");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("ingestBytes", MapUtils.getString(tmp, "ingest_bytes"));
      item.put("ingestPackets", MapUtils.getString(tmp, "ingest_packets"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#queryMetricProtocolPort(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricProtocolPort(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> recordList = metricProtocolPortDao.queryProtocolPortTable(queryVO,
        sortProperty, sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (Map<String, Object> tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("protocol", MapUtils.getString(tmp, "protocol"));
      item.put("port", MapUtils.getString(tmp, "port"));
      item.put("totalBytes", MapUtils.getString(tmp, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(tmp, "total_packets"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#querySessionRecords(java.lang.String, com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<Map<String, Object>> querySessionRecords(String queryId, MetricNetflowQueryVO queryVO,
      Pageable page) {
    List<String> sessionIds = StringUtils.isNotBlank(queryVO.getSessionId())
        ? CsvUtils.convertCSVToList(queryVO.getSessionId())
        : Lists.newArrayListWithCapacity(0);

    Page<Map<String, Object>> sessionRecordPage = metricSessionRecordDaoImpl
        .querySessionRecords(queryId, page, queryVO, sessionIds);

    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }

    for (Map<String, Object> sessionRecord : sessionRecordPage) {
      sessionRecord.put("startTime", MapUtils.getString(sessionRecord, "start_time"));
      sessionRecord.put("endTime", MapUtils.getString(sessionRecord, "end_time"));
      sessionRecord.put("reportTime", MapUtils.getString(sessionRecord, "report_time"));
      Inet4Address srcIpv4 = (Inet4Address) sessionRecord.get("src_ipv4");
      Inet6Address srcIpv6 = (Inet6Address) sessionRecord.get("src_ipv6");
      sessionRecord.put("src_ip",
          srcIpv4 == null ? srcIpv6.getHostAddress() : srcIpv4.getHostAddress());
      Inet4Address destIpv4 = (Inet4Address) sessionRecord.get("dest_ipv4");
      Inet6Address destIpv6 = (Inet6Address) sessionRecord.get("dest_ipv6");
      sessionRecord.put("dest_ip",
          destIpv4 == null ? destIpv6.getHostAddress() : destIpv4.getHostAddress());
    }
    return sessionRecordPage;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#querySessionRecordTotalElement(java.lang.String, com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO)
   */
  @Override
  public Map<String, Object> querySessionRecordTotalElement(String queryId,
      MetricNetflowQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("total", metricSessionRecordDaoImpl.countSessionRecords(queryId, queryVO));

    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#querySessionRecordIp(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordIp(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> ipSRList = metricSessionRecordDaoImpl.queryIpTable(queryVO,
        sortProperty, sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(ipSRList.size());
    for (Map<String, Object> tmp : ipSRList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) tmp.get("ipv4");
      Inet6Address ipv6 = (Inet6Address) tmp.get("ipv6");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("totalBytes", MapUtils.getString(tmp, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(tmp, "total_packets"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#querySessionRecordTransmitIp(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordTransmitIp(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> recordList = metricSessionRecordDaoImpl.queryTransmitIpTable(queryVO,
        sortProperty, sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (Map<String, Object> tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) tmp.get("ipv4");
      Inet6Address ipv6 = (Inet6Address) tmp.get("ipv6");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("transmitBytes", MapUtils.getString(tmp, "transmit_bytes"));
      item.put("transmitPackets", MapUtils.getString(tmp, "transmit_packets"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#querySessionRecordIngestIp(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordIngestIp(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> recordList = metricSessionRecordDaoImpl.queryIngestIpTable(queryVO,
        sortProperty, sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (Map<String, Object> tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Inet4Address ipv4 = (Inet4Address) tmp.get("ipv4");
      Inet6Address ipv6 = (Inet6Address) tmp.get("ipv6");
      item.put("ipAddress", ipv4 == null ? ipv6.getHostAddress() : ipv4.getHostAddress());
      item.put("ingestBytes", MapUtils.getString(tmp, "ingest_bytes"));
      item.put("ingestPackets", MapUtils.getString(tmp, "ingest_packets"));
      result.add(item);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#querySessionRecordSession(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordSession(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> recordList = metricSessionRecordDaoImpl.querySessionTable(queryVO,
        sortProperty, sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());
    for (Map<String, Object> tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("sessionId", MapUtils.getString(tmp, "session_id"));
      item.put("protocol", MapUtils.getString(tmp, "protocol"));
      item.put("ipInitiator", StringUtils.isBlank(sortDirection));
      Inet4Address srcIpv4 = (Inet4Address) tmp.get("src_ipv4");
      Inet6Address srcIpv6 = (Inet6Address) tmp.get("src_ipv6");
      item.put("srcIp", srcIpv4 == null ? srcIpv6.getHostAddress() : srcIpv4.getHostAddress());
      Inet4Address destIpv4 = (Inet4Address) tmp.get("dest_ipv4");
      Inet6Address destIpv6 = (Inet6Address) tmp.get("dest_ipv6");
      item.put("destIp", destIpv4 == null ? destIpv6.getHostAddress() : destIpv4.getHostAddress());
      item.put("srcPort", MapUtils.getString(tmp, "src_port"));
      item.put("destPort", MapUtils.getString(tmp, "dest_port"));
      item.put("totalBytes", MapUtils.getString(tmp, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(tmp, "total_packets"));
      result.add(item);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.metric.service.MetricNetflowTableService#querySessionRecordProtocolPort(com.machloop.fpc.npm.metric.vo.MetricNetflowQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> querySessionRecordProtocolPort(MetricNetflowQueryVO queryVO,
      String sortProperty, String sortDirection) {
    List<Map<String, Object>> recordList = metricSessionRecordDaoImpl
        .queryProtocolportTable(queryVO, sortProperty, sortDirection);
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(recordList.size());

    for (Map<String, Object> tmp : recordList) {
      Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      item.put("port", MapUtils.getString(tmp, "port"));
      item.put("protocol", MapUtils.getString(tmp, "protocol"));
      item.put("totalBytes", MapUtils.getString(tmp, "total_bytes"));
      item.put("totalPackets", MapUtils.getString(tmp, "total_packets"));
      result.add(item);
    }
    return result;
  }

}
