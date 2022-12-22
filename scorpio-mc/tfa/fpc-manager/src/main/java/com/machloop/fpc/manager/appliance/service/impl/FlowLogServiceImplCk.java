package com.machloop.fpc.manager.appliance.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.googlecode.ipv6.IPv6Address;
import com.googlecode.ipv6.IPv6AddressRange;
import com.googlecode.ipv6.IPv6Network;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.*;
import com.machloop.alpha.common.util.ExportUtils.FetchData;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.HostGroupBO;
import com.machloop.fpc.manager.appliance.dao.FlowLogDaoCk;
import com.machloop.fpc.manager.appliance.dao.IpLabelDao;
import com.machloop.fpc.manager.appliance.data.IpLabelDO;
import com.machloop.fpc.manager.appliance.service.FlowLogServiceCk;
import com.machloop.fpc.manager.appliance.service.HostGroupService;
import com.machloop.fpc.manager.appliance.service.WebSharkService;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.manager.global.dao.CounterDao;
import com.machloop.fpc.manager.global.data.CounterQuery;
import com.machloop.fpc.manager.global.service.SlowQueryService;
import com.machloop.fpc.manager.knowledge.bo.*;
import com.machloop.fpc.manager.knowledge.service.GeoService;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.npm.analysis.service.SuricataAlertMessageService;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;


/**
 * @author mazhiyuan
 * <p>
 * create at 2020年2月19日, fpc-manager
 */
@Service
public class FlowLogServiceImplCk implements FlowLogServiceCk {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogServiceImplCk.class);

  private Map<String, Date> fileMap = Maps.newConcurrentMap();

  private Map<String, Tuple4<String, String, Integer, Date>> packetMap = Maps.newConcurrentMap();

  private static final int SCALE_COUNTS = 4;

  private static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.put("interface", "接口编号");
    fields.put("network_id", "所属网络");
    fields.put("service_id", "所属业务");
    fields.put("start_time", "开始时间");
    fields.put("report_time", "记录时间");
    fields.put("duration", "持续时间(s)");
    fields.put("packet_sigseq", "前100个包的特征序列");
    fields.put("upstream_bytes", "正向字节数");
    fields.put("downstream_bytes", "反向字节数");
    fields.put("total_bytes", "总字节数");
    fields.put("upstream_packets", "正向包数");
    fields.put("downstream_packets", "反向包数");
    fields.put("total_packets", "总包数");
    fields.put("upstream_payload_bytes", "正向payload字节数");
    fields.put("downstream_payload_bytes", "反向payload字节数");
    fields.put("total_payload_bytes", "payload总字节数");
    fields.put("upstream_payload_packets", "正向payload包数");
    fields.put("downstream_payload_packets", "反向payload包数");
    fields.put("total_payload_packets", "payload总包数");
    fields.put("tcp_client_network_latency", "客户端网络时延");
    fields.put("tcp_server_network_latency", "服务器网络时延");
    fields.put("server_response_latency", "服务器响应时延");
    fields.put("tcp_client_loss_bytes", "TCP客户端丢包字节数");
    fields.put("tcp_server_loss_bytes", "TCP服务端丢包字节数");
    fields.put("tcp_client_zero_window_packets", "客户端TCP零窗口包数");
    fields.put("tcp_server_zero_window_packets", "服务器TCP零窗口包数");
    fields.put("tcp_session_state", "tcp会话状态");
    fields.put("tcp_established_success_flag", "TCP建连成功次数");
    fields.put("tcp_established_fail_flag", "TCP建连失败次数");
    fields.put("established_sessions", "新建会话数");
    fields.put("tcp_syn_packets", "TCP同步数据包数");
    fields.put("tcp_syn_ack_packets", "TCP同步确认数据包数");
    fields.put("tcp_syn_rst_packets", "TCP同步重置数据包");
    fields.put("tcp_client_retransmission_packets", "TCP客户端重传包数");
    fields.put("tcp_client_retransmission_rate", "客户端重传率");
    fields.put("tcp_server_retransmission_packets", "TCP服务端重传包数");
    fields.put("tcp_server_retransmission_rate", "服务端重传率");
    fields.put("ethernet_type", "MAC类型");
    fields.put("ethernet_initiator", "源MAC");
    fields.put("ethernet_responder", "目的MAC");
    fields.put("ethernet_protocol", "网络层协议");
    fields.put("hostgroup_id_initiator", "源IP所属地址组");
    fields.put("hostgroup_id_responder", "目的IP所属地址组");
    fields.put("ip_initiator", "源IP");
    fields.put("ip_nat_initiator", "NAT源IP");
    fields.put("ip_locality_initiator", "源IP位置");
    fields.put("ip_responder", "目的IP");
    fields.put("ip_nat_responder", "NAT目的IP");
    fields.put("ip_locality_responder", "目的IP位置");
    fields.put("vlan_id", "VLANID");
    fields.put("port_initiator", "源端口");
    fields.put("port_nat_initiator", "NAT源端口");
    fields.put("port_responder", "目的端口");
    fields.put("port_nat_responder", "NAT目的端口");
    fields.put("ip_protocol", "传输层协议");
    fields.put("l7_protocol_id", "应用层协议");
    fields.put("application_category_id", "应用分类");
    fields.put("application_subcategory_id", "应用子分类");
    fields.put("application_id", "应用名称");
    fields.put("country_id_initiator", "源IP国家");
    fields.put("province_id_initiator", "源IP省份");
    fields.put("city_id_initiator", "源IP城市");
    fields.put("country_id_responder", "目的IP国家");
    fields.put("province_id_responder", "目的IP省份");
    fields.put("city_id_responder", "目的IP城市");
  }

  @Autowired
  private ServletContext servletContext;

  @Autowired
  private WebSharkService webSharkService;

  @Autowired
  private SaService saService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;

  @Autowired
  private HostGroupService hostGroupService;

  @Autowired
  private SaProtocolService saProtocolService;

  @Autowired
  private GeoService geoService;

  @Autowired
  private SlowQueryService slowQueryService;

  @Autowired
  private FlowLogDaoCk flowLogDao;

  @Autowired
  private CounterDao counterDao;

  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private IpLabelDao ipLabelDao;

  @Value("${fpc.engine.rest.server.protocol}")
  private String engineServerProtocol;
  @Value("${fpc.engine.rest.server.host}")
  private String engineServerHost;
  @Value("${fpc.engine.rest.server.port}")
  private String engineServerPort;

  @Autowired
  private SuricataAlertMessageService suricataAlertMessageService;

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogServiceCk#queryFlowLogs(java.lang.String, com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String)
   */
  @Override
  public Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page,
      FlowLogQueryVO queryVO, String columns) {
    List<String> ids = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    Page<Map<String, Object>> flowLogPage = flowLogDao.queryFlowLogs(queryId, page, queryVO,
        columnMapping(columns), ids);

    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }

    // IP标签集合
    List<IpLabelDO> ipLabelDOList = ipLabelDao.queryIpLabels();
    List<Tuple2<String, Range<Long>>> existIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> existIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    ipLabelDOList.forEach(ipLabel -> {
      CsvUtils.convertCSVToList(ipLabel.getIpAddress()).forEach(ip -> {
        if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"), IpVersion.V4)
            || NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "-"), IpVersion.V4)) {
          Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
          existIpv4RangeList
              .add(Tuples.of(ipLabel.getName(), Range.closed(ip2Range.getT1(), ip2Range.getT2())));
        } else {
          existIpv6RangeList.add(Tuples.of(ipLabel.getName(), ipv6ToRange(ip)));
        }
      });
    });
    // sa应用 大类 小类 名称字典
    Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> saAppDict = querySaDict();
    // 网络名称
    Map<String, String> networkDict = networkService.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkBO::getId, NetworkBO::getName));
    networkDict.putAll(logicalSubnetService.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetBO::getId, LogicalSubnetBO::getName)));
    // 业务名称
    Map<String, String> serviceDict = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getId, ServiceBO::getName));
    // 地址组名称
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));
    // 获取l7protocol
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();

    for (Map<String, Object> flowLog : flowLogPage) {
      flowLog.put("flow_id", MapUtils.getString(flowLog, "flow_id", ""));

      Long tcpClientRetransmissionPackets = MapUtils.getLong(flowLog,
          "tcp_client_retransmission_packets");
      Long tcpClientPackets = MapUtils.getLong(flowLog, "tcp_client_packets");
      if (tcpClientRetransmissionPackets != null && tcpClientPackets != null) {
        double tcpClientRetransmissionRate = tcpClientPackets == 0 ? 0
            : tcpClientRetransmissionPackets / (double) tcpClientPackets;
        flowLog.put("tcp_client_retransmission_rate", new BigDecimal(tcpClientRetransmissionRate)
            .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }

      Long tcpServerRetransmissionPackets = MapUtils.getLong(flowLog,
          "tcp_server_retransmission_packets");
      Long tcpServerPackets = MapUtils.getLong(flowLog, "tcp_server_packets");
      if (tcpServerRetransmissionPackets != null && tcpServerPackets != null) {
        double tcpServerRetransmissionRate = tcpServerPackets == 0 ? 0
            : tcpServerRetransmissionPackets / (double) tcpServerPackets;
        flowLog.put("tcp_server_retransmission_rate", new BigDecimal(tcpServerRetransmissionRate)
            .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }

      if (flowLog.containsKey("ip_nat_initiator")) {
        String ipNatInitiator = MapUtils.getString(flowLog, "ip_nat_initiator");
        if (StringUtils.isNotBlank(ipNatInitiator)
            && StringUtils.startsWith(ipNatInitiator, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
          ipNatInitiator = StringUtils.substringAfter(ipNatInitiator,
              ManagerConstants.IPV4_TO_IPV6_PREFIX);
        }
        flowLog.put("ip_nat_initiator", ipNatInitiator);
      }
      if (flowLog.containsKey("ip_nat_responder")) {
        String ipNatResponder = MapUtils.getString(flowLog, "ip_nat_responder");
        if (StringUtils.isNotBlank(ipNatResponder)
            && StringUtils.startsWith(ipNatResponder, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
          ipNatResponder = StringUtils.substringAfter(ipNatResponder,
              ManagerConstants.IPV4_TO_IPV6_PREFIX);
        }
        flowLog.put("ip_nat_responder", ipNatResponder);
      }

      addIpCategory(flowLog, existIpv4RangeList, existIpv6RangeList);

      addDict(flowLog, saAppDict, networkDict, serviceDict, hostGroupDict, protocolDict,
          locationDict);
    }

    return flowLogPage;
  }

  @Override
  public List<Map<String, Object>> queryFlowLogsByFlowIds(String queryId, PageRequest page,
      FlowLogQueryVO queryVO, String columns) {

    List<Map<String, Object>> result = null;
    // 判断为根据flow_id还是sid
    if (queryVO.getSid() != null) {
      // 获取触发规则的前100条流ID
      List<String> flowIds = suricataAlertMessageService
          .queryTopHundredSuricataFlowId(queryVO.getSid(), queryVO.getStartTimeDate(),
              queryVO.getEndTimeDate())
          .stream().map(flowId -> String.valueOf(flowId)).collect(Collectors.toList());
      result = flowLogDao.queryFlowLogs(queryId, queryVO, flowIds, page, columnMapping(columns));
    } else {
      result = flowLogDao.queryFlowLogByFlowId(queryId, queryVO, columnMapping(columns));
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }

    if (CollectionUtils.isEmpty(result)) {
      return result;
    }

    // IP标签
    List<IpLabelDO> ipLabelDOList = ipLabelDao.queryIpLabels();
    List<Tuple2<String, Range<Long>>> existIpv4RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Tuple2<String, IPv6AddressRange>> existIpv6RangeList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    ipLabelDOList.forEach(ipLabel -> {
      CsvUtils.convertCSVToList(ipLabel.getIpAddress()).forEach(ip -> {
        if (NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "/"), IpVersion.V4)
            || NetworkUtils.isInetAddress(StringUtils.substringBeforeLast(ip, "-"), IpVersion.V6)) {
          Tuple2<Long, Long> ip2Range = NetworkUtils.ip2Range(ip);
          existIpv4RangeList
              .add(Tuples.of(ipLabel.getName(), Range.closed(ip2Range.getT1(), ip2Range.getT2())));
        } else {
          existIpv6RangeList.add(Tuples.of(ipLabel.getName(), ipv6ToRange(ip)));
        }
      });
    });
    // sa应用 大类 小类 名称字典
    Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> saAppDict = querySaDict();
    // 网络名称
    Map<String, String> networkDict = networkService.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkBO::getId, NetworkBO::getName));
    networkDict.putAll(logicalSubnetService.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetBO::getId, LogicalSubnetBO::getName)));
    // 业务名称
    Map<String, String> serviceDict = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getId, ServiceBO::getName));
    // 地址组名称
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));
    // 获取l7protocol
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();

    List<Map<String, Object>> mergeResult = mergeFlowLog(result);

    for (Map<String, Object> flowLog : mergeResult) {
      Long tcpClientRetransmissionPackets = MapUtils.getLong(flowLog,
          "tcp_client_retransmission_packets");
      Long tcpClientPackets = MapUtils.getLong(flowLog, "tcp_client_packets");
      if (tcpClientRetransmissionPackets != null && tcpClientPackets != null) {
        double tcpClientRetransmissionRate = tcpClientPackets == 0 ? 0
            : tcpClientRetransmissionPackets / (double) tcpClientPackets;
        flowLog.put("tcp_client_retransmission_rate", new BigDecimal(tcpClientRetransmissionRate)
            .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }

      Long tcpServerRetransmissionPackets = MapUtils.getLong(flowLog,
          "tcp_server_retransmission_packets");
      Long tcpServerPackets = MapUtils.getLong(flowLog, "tcp_server_packets");
      if (tcpServerRetransmissionPackets != null && tcpServerPackets != null) {
        double tcpServerRetransmissionRate = tcpServerPackets == 0 ? 0
            : tcpServerRetransmissionPackets / (double) tcpServerPackets;
        flowLog.put("tcp_server_retransmission_rate", new BigDecimal(tcpServerRetransmissionRate)
            .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      }

      if (flowLog.containsKey("ip_nat_initiator")) {
        String ipNatInitiator = MapUtils.getString(flowLog, "ip_nat_initiator");
        if (StringUtils.isNotBlank(ipNatInitiator)
            && StringUtils.startsWith(ipNatInitiator, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
          ipNatInitiator = StringUtils.substringAfter(ipNatInitiator,
              ManagerConstants.IPV4_TO_IPV6_PREFIX);
        }
        flowLog.put("ip_nat_initiator", ipNatInitiator);
      }
      if (flowLog.containsKey("ip_nat_responder")) {
        String ipNatResponder = MapUtils.getString(flowLog, "ip_nat_responder");
        if (StringUtils.isNotBlank(ipNatResponder)
            && StringUtils.startsWith(ipNatResponder, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
          ipNatResponder = StringUtils.substringAfter(ipNatResponder,
              ManagerConstants.IPV4_TO_IPV6_PREFIX);
        }
        flowLog.put("ip_nat_responder", ipNatResponder);
      }

      addIpCategory(flowLog, existIpv4RangeList, existIpv6RangeList);

      addDict(flowLog, saAppDict, networkDict, serviceDict, hostGroupDict, protocolDict,
          locationDict);
    }

    return mergeResult;
  }

  private Map<String, Object> addIpCategory(Map<String, Object> item,
      List<Tuple2<String, Range<Long>>> existIpv4RangeList,
      List<Tuple2<String, IPv6AddressRange>> existIpv6RangeList) {

    if (item.get("ipv4_initiator") != null) {
      Range<Long> ipv4Initiator = Range.closed(
          NetworkUtils.ip2Range(item.get("ipv4_initiator").toString().replace("/", "")).getT1(),
          NetworkUtils.ip2Range(item.get("ipv4_initiator").toString().replace("/", "")).getT2());
      List<String> ipLabelInitiator = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      for (Tuple2<String, Range<Long>> existIpRange : existIpv4RangeList) {
        if (ipv4Initiator.isConnected(existIpRange.getT2())) {
          ipLabelInitiator.add(existIpRange.getT1());
        }
      }
      item.put("ip_label_initiator", StringUtils.join(ipLabelInitiator, ","));
    }
    if (item.get("ipv4_responder") != null) {
      Range<Long> ipv4Responder = Range.closed(
          NetworkUtils.ip2Range(item.get("ipv4_responder").toString().replace("/", "")).getT1(),
          NetworkUtils.ip2Range(item.get("ipv4_responder").toString().replace("/", "")).getT2());
      List<String> ipLabelResponder = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      for (Tuple2<String, Range<Long>> existIpRange : existIpv4RangeList) {
        if (ipv4Responder.isConnected(existIpRange.getT2())) {
          ipLabelResponder.add(existIpRange.getT1());
        }
      }
      item.put("ip_label_responder", StringUtils.join(ipLabelResponder, ","));
    }
    if (item.get("ipv6_initiator") != null) {
      IPv6AddressRange ipv6Initiator = ipv6ToRange(
          item.get("ipv6_initiator").toString().replace("/", ""));
      List<String> ipLabelInitiator = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      for (Tuple2<String, IPv6AddressRange> existIpRange : existIpv6RangeList) {
        if (existIpRange.getT2().contains(ipv6Initiator)
            || ipv6Initiator.contains(existIpRange.getT2())) {
          ipLabelInitiator.add(existIpRange.getT1());
        }
      }
      item.put("ip_label_initiator", StringUtils.join(ipLabelInitiator, ","));
    }
    if (item.get("ipv6_responder") != null) {
      IPv6AddressRange ipv6Responder = ipv6ToRange(
          item.get("ipv6_responder").toString().replace("/", ""));
      List<String> ipLabelResponder = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      for (Tuple2<String, IPv6AddressRange> existIpRange : existIpv6RangeList) {
        if (existIpRange.getT2().contains(ipv6Responder)) {
          ipLabelResponder.add(existIpRange.getT1());
        }
      }
      item.put("ip_label_responder", StringUtils.join(ipLabelResponder, ","));
    }

    return item;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> addDict(Map<String, Object> item,
      Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> saAppDict,
      Map<String, String> networkDict, Map<String, String> serviceDict,
      Map<String, String> hostGroupDict, Map<String, String> protocolDict,
      Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> locationDict) {
    if (item.containsKey("network_id")) {
      List<String> networkIds = (List<String>) item.get("network_id");
      List<String> networkNames = networkIds.stream()
          .map(networkId -> MapUtils.getString(networkDict, networkId, ""))
          .collect(Collectors.toList());
      item.put("network_text", networkNames);
    }
    if (item.containsKey("service_id")) {
      List<String> serviceIds = (List<String>) item.get("service_id");
      List<String> serviceNames = serviceIds.stream()
          .map(serviceId -> MapUtils.getString(serviceDict, serviceId, ""))
          .collect(Collectors.toList());
      item.put("service_text", serviceNames);
    }
    if (item.containsKey("application_category_id")) {
      String application_category_name = MapUtils.getString(saAppDict.getT1(),
          MapUtils.getString(item, "application_category_id", ""), "");
      item.put("application_category_text", application_category_name);
    }
    if (item.containsKey("application_subcategory_id")) {
      String application_subcategory_name = MapUtils.getString(saAppDict.getT2(),
          MapUtils.getString(item, "application_subcategory_id", ""), "");
      item.put("application_subcategory_text", application_subcategory_name);
    }
    if (item.containsKey("application_id")) {
      String application_name = MapUtils.getString(saAppDict.getT3(),
          MapUtils.getString(item, "application_id", ""), "");
      item.put("application_text", application_name);
    }
    if (item.containsKey("l7_protocol_id")) {
      String l7ProtocolName = MapUtils.getString(protocolDict,
          MapUtils.getString(item, "l7_protocol_id", ""), "");
      item.put("l7_protocol_text", l7ProtocolName);
    }
    if (item.containsKey("hostgroup_id_initiator")) {
      String hostgroup_name_initiator = MapUtils.getString(hostGroupDict,
          MapUtils.getString(item, "hostgroup_id_initiator", ""), "");
      item.put("hostgroup_id_initiator_text", hostgroup_name_initiator);
    }
    if (item.containsKey("hostgroup_id_responder")) {
      String hostgroup_name_responder = MapUtils.getString(hostGroupDict,
          MapUtils.getString(item, "hostgroup_id_responder", ""), "");
      item.put("hostgroup_id_responder_text", hostgroup_name_responder);
    }
    if (item.containsKey("country_id_initiator")) {
      String country_name_initiator = MapUtils.getString(locationDict.getT1(),
          MapUtils.getString(item, "country_id_initiator", ""), "");
      item.put("country_id_initiator_text", country_name_initiator);
    }
    if (item.containsKey("country_id_responder")) {
      String country_name_responder = MapUtils.getString(locationDict.getT1(),
          MapUtils.getString(item, "country_id_responder", ""), "");
      item.put("country_id_responder_text", country_name_responder);
    }
    if (item.containsKey("province_id_initiator")) {
      String province_name_initiator = MapUtils.getString(locationDict.getT2(),
          MapUtils.getString(item, "province_id_initiator", ""), "");
      item.put("province_id_initiator_text", province_name_initiator);
    }
    if (item.containsKey("province_id_responder")) {
      String province_name_responder = MapUtils.getString(locationDict.getT2(),
          MapUtils.getString(item, "province_id_responder", ""), "");
      item.put("province_id_responder_text", province_name_responder);
    }
    if (item.containsKey("city_id_initiator")) {
      String city_name_initiator = MapUtils.getString(locationDict.getT3(),
          MapUtils.getString(item, "city_id_initiator", ""), "");
      item.put("city_id_initiator_text", city_name_initiator);
    }
    if (item.containsKey("city_id_responder")) {
      String city_name_responder = MapUtils.getString(locationDict.getT3(),
          MapUtils.getString(item, "city_id_responder", ""), "");
      item.put("city_id_responder_text", city_name_responder);
    }

    return item;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogServiceCk#queryFlowLogsGroupByFlow(java.lang.String, java.lang.String, java.lang.String, java.util.List, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogsGroupByFlow(FlowLogQueryVO queryVO,
      String l7ProtocolName, List<String> flowIds) {
    // 校验应用层协议
    Map<String,
        String> protocolNameIdDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(
                protocolMap -> StringUtils.lowerCase(MapUtils.getString(protocolMap, "nameText")),
                protocolMap -> MapUtils.getString(protocolMap, "protocolId")));
    String l7ProtocolId = null;
    if (StringUtils.isNotBlank(l7ProtocolName)) {
      l7ProtocolId = protocolNameIdDict.get(StringUtils.lowerCase(l7ProtocolName));
      if (StringUtils.isBlank(l7ProtocolId)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的应用层协议");
      }
    }

    // 查询流日志（限制查询条数）
    String queryId = IdGenerator.generateUUID();
    List<Map<String, Object>> result = flowLogDao.queryFlowLogs(queryId, queryVO, l7ProtocolId,
        flowIds,
        Integer.parseInt(HotPropertiesHelper.getProperty("rest.flowlog.result.query.max.count")));
    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }

    if (CollectionUtils.isEmpty(result)) {
      return result;
    }

    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();
    // 应用层协议字典
    Map<String, String> protocolIdNameDict = protocolNameIdDict.entrySet().stream()
        .collect(Collectors.toMap(entry -> entry.getValue(), entry -> entry.getKey()));
    // 地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();
    // tcp会话字典
    Map<String, String> tcpSessionStateDict = dictManager.getBaseDict()
        .getItemMap("flow_log_tcp_session_state");
    // eth类型字典
    Map<String,
        String> ethernetTypeDict = dictManager.getBaseDict().getItemMap("flow_log_ethernet_type");
    // ip内外网位置字典
    Map<String, String> ipLocalityDict = dictManager.getBaseDict()
        .getItemMap("flow_log_ip_address_locality");

    Map<String,
        Map<String, Object>> merge = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<OffsetDateTime>> flowReportTime = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.forEach(flowlog -> {
      String flowId = MapUtils.getString(flowlog, "flow_id");

      List<OffsetDateTime> list = flowReportTime.getOrDefault(flowId,
          Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE));
      list.add((OffsetDateTime) flowlog.get("report_time"));
      flowReportTime.put(flowId, list);

      Map<String, Object> flowlogRecord = merge.get(flowId);
      if (MapUtils.isEmpty(flowlogRecord)) {
        flowlogRecord = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        flowlogRecord.put("interface", flowlog.get("interface"));
        flowlogRecord.put("flow_id", flowlog.get("flow_id"));
        flowlogRecord.put("network_id", flowlog.get("network_id"));
        flowlogRecord.put("service_id", flowlog.get("service_id"));
        flowlogRecord.put("start_time", DateUtils.toStringNanoISO8601(
            (OffsetDateTime) flowlog.get("start_time"), ZoneId.systemDefault()));
        flowlogRecord.put("duration", flowlog.get("duration"));
        flowlogRecord.put("flow_continued", flowlog.get("flow_continued"));
        flowlogRecord.put("packet_sigseq", flowlog.get("packet_sigseq"));
        flowlogRecord.put("tcp_session_state", MapUtils.getString(tcpSessionStateDict,
            MapUtils.getString(flowlog, "tcp_session_state", ""), ""));
        flowlogRecord.put("ethernet_type", MapUtils.getString(ethernetTypeDict,
            MapUtils.getString(flowlog, "ethernet_type", ""), ""));
        flowlogRecord.put("ethernet_initiator", flowlog.get("ethernet_initiator"));
        flowlogRecord.put("ethernet_responder", flowlog.get("ethernet_responder"));
        flowlogRecord.put("ethernet_protocol", flowlog.get("ethernet_protocol"));
        flowlogRecord.put("vlan_id", flowlog.get("vlan_id"));
        flowlogRecord.put("hostgroup_id_initiator", flowlog.get("hostgroup_id_initiator"));
        flowlogRecord.put("hostgroup_id_responder", flowlog.get("hostgroup_id_responder"));
        flowlogRecord.put("ip_locality_initiator", MapUtils.getString(ipLocalityDict,
            MapUtils.getString(flowlog, "ip_locality_initiator", "0"), ""));
        flowlogRecord.put("ip_locality_responder", MapUtils.getString(ipLocalityDict,
            MapUtils.getString(flowlog, "ip_locality_responder", "0"), ""));
        flowlogRecord.put("ipv4_initiator", flowlog.get("ipv4_initiator"));
        flowlogRecord.put("ipv4_responder", flowlog.get("ipv4_responder"));
        flowlogRecord.put("ipv6_initiator", flowlog.get("ipv6_initiator"));
        flowlogRecord.put("ipv6_responder", flowlog.get("ipv6_responder"));
        flowlogRecord.put("ip_protocol", flowlog.get("ip_protocol"));
        flowlogRecord.put("port_initiator", flowlog.get("port_initiator"));
        flowlogRecord.put("port_responder", flowlog.get("port_responder"));
        flowlogRecord.put("l7_protocol", MapUtils.getString(protocolIdNameDict,
            MapUtils.getString(flowlog, "l7_protocol_id", ""), ""));
        Tuple3<String, String,
            String> app = saAppDict.get(MapUtils.getString(flowlog, "application_id", ""));
        String appName = app != null ? app.getT1() : "";
        String appCategoryName = app != null ? app.getT2() : "";
        String appSubCategoryName = app != null ? app.getT3() : "";
        flowlogRecord.put("application_category", appCategoryName);
        flowlogRecord.put("application_subcategory", appSubCategoryName);
        flowlogRecord.put("application_name", appName);
        flowlogRecord.put("malicious_application",
            saAppDict.get(MapUtils.getString(flowlog, "malicious_application_id", "")));
        flowlogRecord.put("country_initiator", MapUtils.getString(locationDict.getT1(),
            MapUtils.getString(flowlog, "country_id_initiator", ""), ""));
        flowlogRecord.put("province_initiator", MapUtils.getString(locationDict.getT2(),
            MapUtils.getString(flowlog, "province_id_initiator", ""), ""));
        flowlogRecord.put("city_initiator", MapUtils.getString(locationDict.getT3(),
            MapUtils.getString(flowlog, "city_id_initiator", ""), ""));
        flowlogRecord.put("district_initiator", flowlog.get("district_initiator"));
        flowlogRecord.put("aoi_type_initiator", flowlog.get("aoi_type_initiator"));
        flowlogRecord.put("aoi_name_initiator", flowlog.get("aoi_name_initiator"));
        flowlogRecord.put("country_responder", MapUtils.getString(locationDict.getT1(),
            MapUtils.getString(flowlog, "country_id_responder", ""), ""));
        flowlogRecord.put("province_responder", MapUtils.getString(locationDict.getT2(),
            MapUtils.getString(flowlog, "province_id_responder", ""), ""));
        flowlogRecord.put("city_responder", MapUtils.getString(locationDict.getT3(),
            MapUtils.getString(flowlog, "city_id_responder", ""), ""));
        flowlogRecord.put("district_responder", flowlog.get("district_responder"));
        flowlogRecord.put("aoi_type_responder", flowlog.get("aoi_type_responder"));
        flowlogRecord.put("aoi_name_responder", flowlog.get("aoi_name_responder"));
        if (flowlogRecord.containsKey("ip_nat_initiator")) {
          String ipNatInitiator = MapUtils.getString(flowlogRecord, "ip_nat_initiator");
          if (StringUtils.isNotBlank(ipNatInitiator)
              && StringUtils.startsWith(ipNatInitiator, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
            ipNatInitiator = StringUtils.substringAfter(ipNatInitiator,
                ManagerConstants.IPV4_TO_IPV6_PREFIX);
          }
          flowlogRecord.put("ip_nat_initiator", ipNatInitiator);
        }
        if (flowlogRecord.containsKey("ip_nat_responder")) {
          String ipNatResponder = MapUtils.getString(flowlogRecord, "ip_nat_responder");
          if (StringUtils.isNotBlank(ipNatResponder)
              && StringUtils.startsWith(ipNatResponder, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
            ipNatResponder = StringUtils.substringAfter(ipNatResponder,
                ManagerConstants.IPV4_TO_IPV6_PREFIX);
          }
          flowlogRecord.put("ip_nat_responder", ipNatResponder);
        }
      }

      String packet_sigseq = MapUtils.getString(flowlogRecord, "packet_sigseq");
      if (StringUtils.isNotBlank(packet_sigseq)) {
        flowlogRecord.put("packet_sigseq", packet_sigseq);
      }

      // 结果累加
      flowlogRecord.put("upstream_bytes", MapUtils.getLongValue(flowlogRecord, "upstream_bytes", 0)
          + MapUtils.getLongValue(flowlog, "upstream_bytes", 0));
      flowlogRecord.put("downstream_bytes",
          MapUtils.getLongValue(flowlogRecord, "downstream_bytes", 0)
              + MapUtils.getLongValue(flowlog, "downstream_bytes", 0));
      flowlogRecord.put("total_bytes", MapUtils.getLongValue(flowlogRecord, "total_bytes", 0)
          + MapUtils.getLongValue(flowlog, "total_bytes", 0));
      flowlogRecord.put("upstream_packets",
          MapUtils.getLongValue(flowlogRecord, "upstream_packets", 0)
              + MapUtils.getLongValue(flowlog, "upstream_packets", 0));
      flowlogRecord.put("downstream_packets",
          MapUtils.getLongValue(flowlogRecord, "downstream_packets", 0)
              + MapUtils.getLongValue(flowlog, "downstream_packets", 0));
      flowlogRecord.put("total_packets", MapUtils.getLongValue(flowlogRecord, "total_packets", 0)
          + MapUtils.getLongValue(flowlog, "total_packets", 0));
      flowlogRecord.put("upstream_payload_bytes",
          MapUtils.getLongValue(flowlogRecord, "upstream_payload_bytes", 0)
              + MapUtils.getLongValue(flowlog, "upstream_payload_bytes", 0));
      flowlogRecord.put("downstream_payload_bytes",
          MapUtils.getLongValue(flowlogRecord, "downstream_payload_bytes", 0)
              + MapUtils.getLongValue(flowlog, "downstream_payload_bytes", 0));
      flowlogRecord.put("total_payload_bytes",
          MapUtils.getLongValue(flowlogRecord, "total_payload_bytes", 0)
              + MapUtils.getLongValue(flowlog, "total_payload_bytes", 0));
      flowlogRecord.put("upstream_payload_packets",
          MapUtils.getLongValue(flowlogRecord, "upstream_payload_packets", 0)
              + MapUtils.getLongValue(flowlog, "upstream_payload_packets", 0));
      flowlogRecord.put("downstream_payload_packets",
          MapUtils.getLongValue(flowlogRecord, "downstream_payload_packets", 0)
              + MapUtils.getLongValue(flowlog, "downstream_payload_packets", 0));
      flowlogRecord.put("total_payload_packets",
          MapUtils.getLongValue(flowlogRecord, "total_payload_packets", 0)
              + MapUtils.getLongValue(flowlog, "total_payload_packets", 0));
      flowlogRecord.put("tcp_client_network_latency",
          MapUtils.getLongValue(flowlogRecord, "tcp_client_network_latency", 0)
              + MapUtils.getLongValue(flowlog, "tcp_client_network_latency", 0));
      flowlogRecord.put("tcp_server_network_latency",
          MapUtils.getLongValue(flowlogRecord, "tcp_server_network_latency", 0)
              + MapUtils.getLongValue(flowlog, "tcp_server_network_latency", 0));
      flowlogRecord.put("server_response_latency",
          MapUtils.getLongValue(flowlogRecord, "server_response_latency", 0)
              + MapUtils.getLongValue(flowlog, "server_response_latency", 0));
      flowlogRecord.put("tcp_client_loss_bytes",
          MapUtils.getLongValue(flowlogRecord, "tcp_client_loss_bytes", 0)
              + MapUtils.getLongValue(flowlog, "tcp_client_loss_bytes", 0));
      flowlogRecord.put("tcp_server_loss_bytes",
          MapUtils.getLongValue(flowlogRecord, "tcp_server_loss_bytes", 0)
              + MapUtils.getLongValue(flowlog, "tcp_server_loss_bytes", 0));
      flowlogRecord.put("tcp_client_zero_window_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_client_zero_window_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_client_zero_window_packets", 0));
      flowlogRecord.put("tcp_server_zero_window_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_server_zero_window_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_server_zero_window_packets", 0));
      flowlogRecord.put("tcp_established_success_flag",
          MapUtils.getLongValue(flowlogRecord, "tcp_established_success_flag", 0)
              + MapUtils.getLongValue(flowlog, "tcp_established_success_flag", 0));
      flowlogRecord.put("tcp_established_fail_flag",
          MapUtils.getLongValue(flowlogRecord, "tcp_established_fail_flag", 0)
              + MapUtils.getLongValue(flowlog, "tcp_established_fail_flag", 0));
      flowlogRecord.put("established_sessions",
          MapUtils.getLongValue(flowlogRecord, "established_sessions", 0)
              + MapUtils.getLongValue(flowlog, "established_sessions", 0));
      flowlogRecord.put("tcp_syn_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_syn_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_syn_packets", 0));
      flowlogRecord.put("tcp_syn_ack_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_syn_ack_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_syn_ack_packets", 0));
      flowlogRecord.put("tcp_syn_rst_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_syn_rst_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_syn_rst_packets", 0));
      flowlogRecord.put("tcp_client_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_client_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_client_packets", 0));
      flowlogRecord.put("tcp_server_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_server_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_server_packets", 0));
      flowlogRecord.put("tcp_client_retransmission_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_client_retransmission_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_client_retransmission_packets", 0));
      flowlogRecord.put("tcp_server_retransmission_packets",
          MapUtils.getLongValue(flowlogRecord, "tcp_server_retransmission_packets", 0)
              + MapUtils.getLongValue(flowlog, "tcp_server_retransmission_packets", 0));
      merge.put(MapUtils.getString(flowlog, "flow_id"), flowlogRecord);
    });

    Map<String, Map<String, Object>> resultMap = merge.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> {
          Map<String, Object> value = entry.getValue();

          List<OffsetDateTime> list = flowReportTime.get(entry.getKey());
          list.sort((o1, o2) -> o1.compareTo(o2));
          value.put("record_start_time",
              DateUtils.toStringNanoISO8601((OffsetDateTime) list.get(0), ZoneId.systemDefault()));
          value.put("record_end_time", DateUtils.toStringNanoISO8601(
              (OffsetDateTime) list.get(list.size() - 1), ZoneId.systemDefault()));
          value.put("report_time", value.get("record_end_time"));

          return value;
        }));

    return Lists.newArrayList(resultMap.values());
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogServiceCk#queryFlowLogStatistics(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, int, java.lang.String, int)
   */
  @Override
  public Map<String, Object> queryFlowLogStatistics(String queryId, FlowLogQueryVO queryVO) {
    List<String> ids = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (ids.isEmpty() && counterDao.onlyBaseFilter(queryVO.getSourceType(), queryVO.getDsl(),
        CounterDao.FLOW_LOG)) {
      CounterQuery counterQuery = new CounterQuery();
      BeanUtils.copyProperties(queryVO, counterQuery);
      result.put("total", counterDao.countFlowLogs(queryId, counterQuery));
    } else {
      result.put("total", flowLogDao.countFlowLogs(queryId, queryVO, ids));
    }

    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogServiceCk#queryFlowLogStatisticsGroupByIp(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, int, com.machloop.alpha.common.base.page.Sort)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogStatisticsGroupByIp(String queryId,
      FlowLogQueryVO queryVO, int termSize, Sort sort) {
    List<Map<String, Object>> l = flowLogDao.queryFlowLogStatisticsGroupByIp(queryId, queryVO,
        termSize, sort);
    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(l.size());
    for (Map<String, Object> item : l) {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(item.size());
      map.put("ipInitiator", item.get("ipv4_initiator") != null ? item.get("ipv4_initiator")
          : item.get("ipv6_initiator"));
      map.put("ipResponder", item.get("ipv4_responder") != null ? item.get("ipv4_responder")
          : item.get("ipv6_responder"));
      map.put("count", item.get("count"));
      map.put("upstreamBytes", item.get("upstream_bytes"));
      map.put("downstreamBytes", item.get("downstream_bytes"));
      map.put("totalBytes", item.get("total_bytes"));
      map.put("upstreamPackets", item.get("upstream_packets"));
      map.put("downstreamPackets", item.get("downstream_packets"));
      map.put("totalPackets", item.get("total_packets"));
      result.add(map);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogServiceCk#exportFlowLogs(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String, int, int, double, com.machloop.alpha.common.base.page.Sort, java.lang.String, int, java.io.OutputStream)
   */
  @Override
  public void exportFlowLogs(String queryId, FlowLogQueryVO queryVO, String columns,
      int terminateAfter, int timeout, double samplingRate, Sort sort, String fileType, int count,
      OutputStream out) throws IOException {
    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();
    // 网络名称
    Map<String, String> networkDict = networkService.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkBO::getId, NetworkBO::getName));
    networkDict.putAll(logicalSubnetService.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetBO::getId, LogicalSubnetBO::getName)));
    // 业务名称
    Map<String, String> serviceDict = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getId, ServiceBO::getName));
    // 地址组名称
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));
    // 获取l7protocol
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();
    // tcp会话字典
    Map<String, String> tcpSessionStateDict = dictManager.getBaseDict()
        .getItemMap("flow_log_tcp_session_state");
    // eth类型字典
    Map<String,
        String> ethernetTypeDict = dictManager.getBaseDict().getItemMap("flow_log_ethernet_type");
    // ip内外网位置字典
    Map<String, String> ipLocalityDict = dictManager.getBaseDict()
        .getItemMap("flow_log_ip_address_locality");
    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));

    // 标题
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(columns, "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(columns);
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), queryId).toFile();
    FileUtils.touch(tempFile);

    // 本次导出总量
    int maxCount = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.max.count"));
    count = (count <= 0 || count > maxCount) ? maxCount : count;

    // 单次查询数量
    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    // 流日志主键直查ID集合
    List<String> analysisResultIds = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    // 未指定flowId时，需要先获取主键，再去查询所有指定的列
    String tableName = "";
    List<String> ids = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (queryVO.getSid() != null) {
      List<String> flowIds = suricataAlertMessageService
          .queryTopHundredSuricataFlowId(queryVO.getSid(), queryVO.getStartTimeDate(),
              queryVO.getEndTimeDate())
          .stream().map(flowId -> String.valueOf(flowId)).collect(Collectors.toList());
      Tuple2<String, List<String>> idsTuple = flowLogDao.queryFlowLogsByFlowIds(queryId, queryVO,
          flowIds, sort, count);
      tableName = idsTuple.getT1();
      ids.addAll(idsTuple.getT2());
    } else if (queryVO.getFlowId() == null) {
      Tuple2<String, List<String>> idsTuple = flowLogDao.queryFlowLogs(queryId, queryVO,
          analysisResultIds, sort, count);
      tableName = idsTuple.getT1();
      ids.addAll(idsTuple.getT2());
    }

    // 创建数据迭代器
    final String ftableName = tableName;
    FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;
      private String dataColumns = columnMapping(columns);

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        List<Map<String, Object>> result = Lists.newArrayListWithCapacity(batchSize);

        // 获取数据
        if (queryVO.getFlowId() != null) {
          result = flowLogDao.queryFlowLogByFlowId(queryId, queryVO, dataColumns);
          result = mergeFlowLog(result);
        } else {
          List<String> tmpIds = ids.stream().skip(offset).limit(batchSize)
              .collect(Collectors.toList());
          if (CollectionUtils.isNotEmpty(tmpIds)) {
            result = flowLogDao.queryFlowLogsByIds(queryId, ftableName, dataColumns, sort, tmpIds);
          }
        }

        // 避免死循环
        if (result.size() == 0) {
          offset = -1;
          return Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataset = result.stream().map(item -> {
          return transFlowLogMapToStr(item, titles, columnNameMap, saAppDict, networkDict,
              serviceDict, hostGroupDict, protocolDict, locationDict, tcpSessionStateDict,
              ethernetTypeDict, ipLocalityDict);
        }).collect(Collectors.toList());

        offset += dataset.size();

        return dataset;
      }

    };

    // 导出数据
    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogServiceCk#analyzeFlowPacket(java.lang.String, java.util.Date, java.util.Date, java.lang.String, java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void analyzeFlowPacket(String flowPacketId, Date startTime, Date endTime, String type,
      String parameter, HttpServletRequest request, HttpServletResponse response) {

    try {
      String[] flowPacketIds = StringUtils.split(flowPacketId, ":");
      List<String> flowPacketIdHexList = Lists.newArrayListWithCapacity(flowPacketIds.length);
      for (String tmp : flowPacketIds) {
        flowPacketIdHexList.add(Long.toHexString(Long.parseLong(tmp)));
      }
      // flow_packet_id, startTime, endTime确定一个pcap包
      String id = "flow_packet_" + flowPacketId + startTime.getTime() + endTime.getTime();
      // 第一次加载需要传文件路径，之前已成功加载则无需再次获取文件地址
      String filePath = null;
      if (fileMap.size() >= Constants.BLOCK_DEFAULT_SIZE) {
        fileMap = Maps.newConcurrentMap();
      }

      if (fileMap.get(id) == null || fileMap.get(id)
          .before(DateUtils.beforeSecondDate(DateUtils.now(), Constants.HALF_MINUTE_SECONDS))) {

        // 使用UUID作为凭证，并取token进行签名
        String credential = IdGenerator.generateUUID();
        String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);

        String date = DateUtils.toStringISO8601(DateUtils.now());
        String path = String.format(ManagerConstants.REST_ENGINE_FLOW_PACKET_QUERY,
            StringUtils.join(flowPacketIdHexList, ":"));
        String serverIp = engineServerHost;
        String[] ipList = StringUtils.split(engineServerHost, ",");
        if (ipList.length > 1) {
          if (InetAddressUtils.isIPv4Address(request.getRemoteAddr())) {
            serverIp = InetAddressUtils.isIPv4Address(ipList[0]) ? ipList[0] : ipList[1];
          } else {
            serverIp = InetAddressUtils.isIPv6Address(ipList[0]) ? ipList[0] : ipList[1];
          }
        }

        // 拼接文件下载地址
        StringBuilder url = new StringBuilder();
        try {
          url.append(engineServerProtocol);
          url.append("://");
          if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
            url.append("[").append(serverIp).append("]");
          } else {
            url.append(serverIp);
          }
          url.append(":");
          url.append(engineServerPort);
          url.append(path);
          url.append(
              String.format("?start_time=%s&end_time=%s", startTime.getTime(), endTime.getTime()));
          url.append("&X-Machloop-Date=");
          url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
          url.append("&X-Machloop-Credential=");
          url.append(credential);
          url.append("&X-Machloop-Signature=");
          url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
        } catch (UnsupportedEncodingException e) {
          LOGGER.warn("failed to fetch flow log packet file urls", e);
          throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "下载失败");
        }
        LOGGER.debug("invoke rest api:{}", url);
        filePath = restTemplate.getForObject(url.toString(), String.class);
        LOGGER.debug("invoke rest api success, get file path:[{}]", filePath);
        if (StringUtils.isBlank(filePath)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "加载数据包文件失败");
        }
        fileMap.put(id, DateUtils.now());
      }
      LOGGER.debug("start to analyze packet from webshark");
      webSharkService.analyzeNetworkPacketFile(id, filePath, type, parameter, request, response);
    } catch (Exception e) {
      LOGGER.warn("failed to analyze flow packet.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "分析数据包文件失败");
    }
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogServiceCk#fetchFlowLogPacketFileUrls(java.lang.String, java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, com.machloop.alpha.common.base.page.Sort)
   */
  @Override
  public Map<String, Object> fetchFlowLogPacketFileUrls(String queryId, String fileType,
      FlowLogQueryVO queryVO, Sort sort) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (packetMap.size() >= Constants.BLOCK_DEFAULT_SIZE) {
      packetMap = Maps.newConcurrentMap();
    }

    String id = Base64Utils.encode(fileType + queryVO.getDsl());

    // 近期相同过滤条件已有数据包文件，可直接下载
    if (packetMap.containsKey(id) && !StringUtils.equals(packetMap.get(id).getT1(), queryId)
        && StringUtils.equals(packetMap.get(id).getT2(), "success") && packetMap.get(id).getT4()
            .after(DateUtils.beforeSecondDate(DateUtils.now(), Constants.ONE_MINUTE_SECONDS))) {
      // 构建下载链接
      result.put("queryId", queryId);
      result.put("status", packetMap.get(id).getT2());
      result.put("progress", 100);
      result.put("truncate", packetMap.get(id).getT3());
      result.put("result", packetDownloadUrl(packetMap.get(id).getT1()));

      return result;
    }

    // 第一次心跳，查询流日志，获取本次下载包含的flowId
    List<Object> flowIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (!packetMap.containsKey(id) || !StringUtils.equals(packetMap.get(id).getT1(), queryId)) {
      if (queryVO.getSid() != null) {
        flowIds = suricataAlertMessageService.queryTopHundredSuricataFlowId(queryVO.getSid(),
            queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
      } else if (queryVO.getFlowId() != null) {
        flowIds.add(queryVO.getFlowId());
      } else {
        List<String> ids = StringUtils.isNotBlank(queryVO.getId())
            ? CsvUtils.convertCSVToList(queryVO.getId())
            : Lists.newArrayListWithCapacity(0);

        String maxSize = HotPropertiesHelper.getProperty("flow.packet.download.max.session");
        int size = StringUtils.isBlank(maxSize) ? ManagerConstants.REST_ENGINE_DOWNLOAD_MAX_SESSIONS
            : Integer.parseInt(maxSize);
        flowIds = flowLogDao.queryFlowIds(queryId, queryVO, ids, sort, size);
      }

      if (CollectionUtils.isEmpty(flowIds)) {
        // 未查询到流
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "未查询到数据");
      }
    }

    // 调用数据包生成接口
    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String path = ManagerConstants.REST_ENGINE_MULTIFLOW_PACKET_QUERY;

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(engineServerProtocol);
      url.append("://");
      url.append(engineServerHost);
      url.append(":");
      url.append(engineServerPort);
      url.append(path);
      url.append("?X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "POST", date, path));
      requestUrl = url.toString();

      // 消息体
      Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      params.put("queryId", queryId);
      params.put("startTime", queryVO.getStartTimeDate().getTime());
      params.put("endTime", queryVO.getEndTimeDate().getTime());
      String networkId = StringUtils.defaultIfBlank(queryVO.getNetworkId(), "");
      if (StringUtils.isAllBlank(queryVO.getNetworkId(), queryVO.getPacketFileId())) {
        networkId = "ALL";
      }
      params.put("networkId", networkId);
      params.put("packetFileId", StringUtils.defaultIfBlank(queryVO.getPacketFileId(), ""));
      params.put("fileType", fileType);
      params.put("flowIds", flowIds);

      LOGGER.info("invoke rest api:{}, params: {}.", requestUrl, JsonHelper.serialize(params));

      HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(params);
      ResponseEntity<
          String> resultStr = restTemplate.postForEntity(url.toString(), httpEntity, String.class);

      if (StringUtils.isBlank(resultStr.getBody())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "引擎异常，未获取到数据包文件");
      }

      LOGGER.info("invoke rest api queryId:{}, result:{}", queryId, resultStr.getBody());

      Map<String, Object> response = JsonHelper.deserialize(resultStr.getBody(),
          new TypeReference<Map<String, Object>>() {
          }, false);

      result.put("queryId", queryId);
      String status = "";
      int truncate = 0;
      if (response.containsKey("code")) {
        status = "fail";
        result.put("status", status);
        result.put("progress", 0);
        result.put("result", MapUtils.getString(response, "msg", ""));
      } else {
        status = MapUtils.getString(response, "status");
        result.put("status", status);
        result.put("progress", MapUtils.getIntValue(response, "progress", 0));
        truncate = MapUtils.getIntValue(response, "truncate", 0);
        result.put("truncate", truncate);
        result.put("result",
            StringUtils.equals(MapUtils.getString(response, "status"), "success")
                ? packetDownloadUrl(queryId)
                : "");
      }

      packetMap.put(id, Tuples.of(queryId, status, truncate, DateUtils.now()));
    } catch (Exception e) {
      LOGGER.warn("failed to download flow packets [" + requestUrl + "].", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "获取数据包异常");
    }

    return result;
  }

  private String packetDownloadUrl(String queryId) {
    // 使用UUID作为凭证，并取token进行签名
    String credential = IdGenerator.generateUUID();
    String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
    String date = DateUtils.toStringISO8601(DateUtils.now());
    String path = ManagerConstants.REST_ENGINE_MULTIFLOW_PACKET_DOWNLOAD;

    StringBuilder url = new StringBuilder();
    try {
      url.append(path);
      url.append("?queryId=").append(queryId);
      url.append("&X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
    } catch (UnsupportedEncodingException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统异常，数据包下载异常");
    }

    return url.toString();
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogServiceCk#fetchFlowLogPacketFileUrls(java.lang.String, java.util.Date, java.util.Date, java.lang.String)
   */
  @Override
  public String fetchFlowLogPacketFileUrls(String flowPacketId, Date startTime, Date endTime,
      String remoteAddr) {

    String[] flowPacketIds = StringUtils.split(flowPacketId, ":");
    List<String> flowPacketIdHexList = Lists.newArrayListWithCapacity(flowPacketIds.length);
    for (String tmp : flowPacketIds) {
      flowPacketIdHexList.add(Long.toHexString(Long.parseLong(tmp)));
    }

    // 使用UUID作为凭证，并取token进行签名
    String credential = IdGenerator.generateUUID();
    String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
    String date = DateUtils.toStringISO8601(DateUtils.now());
    String path = String.format(ManagerConstants.REST_ENGINE_FLOW_PACKET_DOWNLOAD,
        StringUtils.join(flowPacketIdHexList, ":"));

    // 拼接文件下载地址
    StringBuilder url = new StringBuilder();
    try {
      url.append(path);
      url.append(
          String.format("?start_time=%s&end_time=%s", startTime.getTime(), endTime.getTime()));
      url.append("&X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
    } catch (UnsupportedEncodingException e) {
      LOGGER.warn("failed to fetch flow log packet file urls", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "下载失败");
    }

    LOGGER.debug("fetch flow log packet file url, flowPacketId:[{}], rest api:[{}]", flowPacketId,
        url.toString());
    return url.toString();
  }

  private String columnMapping(String columns) {
    if (StringUtils.equals(columns, "*")) {
      return columns;
    }

    Set<String> columnSets = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    columnSets.add("network_id");
    columnSets.add("service_id");
    columnSets.add("duration");
    columnSets.add("start_time");

    CsvUtils.convertCSVToList(columns).forEach(item -> {
      switch (item) {
        case "tcp_client_retransmission_rate":
          columnSets.add("tcp_client_retransmission_packets");
          columnSets.add("tcp_client_packets");
          break;
        case "tcp_server_retransmission_rate":
          columnSets.add("tcp_server_retransmission_packets");
          columnSets.add("tcp_server_packets");
          break;
        case "ip_initiator":
          columnSets.add("ipv4_initiator");
          columnSets.add("ipv6_initiator");
          break;
        case "ip_responder":
          columnSets.add("ipv4_responder");
          columnSets.add("ipv6_responder");
          break;
        default:
          columnSets.add(item);
          break;
      }
    });

    return CsvUtils.convertCollectionToCSV(columnSets);
  }

  private List<Map<String, Object>> mergeFlowLog(List<Map<String, Object>> queryResult) {
    LinkedHashMap<String, Map<String, Object>> merge = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    queryResult.forEach(flowlog -> {
      Map<String, Object> flowlogRecord = merge.getOrDefault(flowlog.get("flow_id"),
          Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE));
      if (MapUtils.isEmpty(flowlogRecord)) {
        flowlogRecord.putAll(flowlog);
      } else {
        // 结果累加
        flowlogRecord.put("upstream_bytes",
            MapUtils.getLongValue(flowlogRecord, "upstream_bytes", 0)
                + MapUtils.getLongValue(flowlog, "upstream_bytes", 0));
        flowlogRecord.put("downstream_bytes",
            MapUtils.getLongValue(flowlogRecord, "downstream_bytes", 0)
                + MapUtils.getLongValue(flowlog, "downstream_bytes", 0));
        flowlogRecord.put("total_bytes", MapUtils.getLongValue(flowlogRecord, "total_bytes", 0)
            + MapUtils.getLongValue(flowlog, "total_bytes", 0));
        flowlogRecord.put("upstream_packets",
            MapUtils.getLongValue(flowlogRecord, "upstream_packets", 0)
                + MapUtils.getLongValue(flowlog, "upstream_packets", 0));
        flowlogRecord.put("downstream_packets",
            MapUtils.getLongValue(flowlogRecord, "downstream_packets", 0)
                + MapUtils.getLongValue(flowlog, "downstream_packets", 0));
        flowlogRecord.put("total_packets", MapUtils.getLongValue(flowlogRecord, "total_packets", 0)
            + MapUtils.getLongValue(flowlog, "total_packets", 0));
        flowlogRecord.put("upstream_payload_bytes",
            MapUtils.getLongValue(flowlogRecord, "upstream_payload_bytes", 0)
                + MapUtils.getLongValue(flowlog, "upstream_payload_bytes", 0));
        flowlogRecord.put("downstream_payload_bytes",
            MapUtils.getLongValue(flowlogRecord, "downstream_payload_bytes", 0)
                + MapUtils.getLongValue(flowlog, "downstream_payload_bytes", 0));
        flowlogRecord.put("total_payload_bytes",
            MapUtils.getLongValue(flowlogRecord, "total_payload_bytes", 0)
                + MapUtils.getLongValue(flowlog, "total_payload_bytes", 0));
        flowlogRecord.put("upstream_payload_packets",
            MapUtils.getLongValue(flowlogRecord, "upstream_payload_packets", 0)
                + MapUtils.getLongValue(flowlog, "upstream_payload_packets", 0));
        flowlogRecord.put("downstream_payload_packets",
            MapUtils.getLongValue(flowlogRecord, "downstream_payload_packets", 0)
                + MapUtils.getLongValue(flowlog, "downstream_payload_packets", 0));
        flowlogRecord.put("total_payload_packets",
            MapUtils.getLongValue(flowlogRecord, "total_payload_packets", 0)
                + MapUtils.getLongValue(flowlog, "total_payload_packets", 0));
        flowlogRecord.put("tcp_client_network_latency",
            MapUtils.getLongValue(flowlogRecord, "tcp_client_network_latency", 0)
                + MapUtils.getLongValue(flowlog, "tcp_client_network_latency", 0));
        flowlogRecord.put("tcp_server_network_latency",
            MapUtils.getLongValue(flowlogRecord, "tcp_server_network_latency", 0)
                + MapUtils.getLongValue(flowlog, "tcp_server_network_latency", 0));
        flowlogRecord.put("server_response_latency",
            MapUtils.getLongValue(flowlogRecord, "server_response_latency", 0)
                + MapUtils.getLongValue(flowlog, "server_response_latency", 0));
        flowlogRecord.put("tcp_client_loss_bytes",
            MapUtils.getLongValue(flowlogRecord, "tcp_client_loss_bytes", 0)
                + MapUtils.getLongValue(flowlog, "tcp_client_loss_bytes", 0));
        flowlogRecord.put("tcp_server_loss_bytes",
            MapUtils.getLongValue(flowlogRecord, "tcp_server_loss_bytes", 0)
                + MapUtils.getLongValue(flowlog, "tcp_server_loss_bytes", 0));
        flowlogRecord.put("tcp_client_zero_window_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_client_zero_window_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_client_zero_window_packets", 0));
        flowlogRecord.put("tcp_server_zero_window_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_server_zero_window_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_server_zero_window_packets", 0));
        flowlogRecord.put("tcp_established_success_flag",
            MapUtils.getLongValue(flowlogRecord, "tcp_established_success_flag", 0)
                + MapUtils.getLongValue(flowlog, "tcp_established_success_flag", 0));
        flowlogRecord.put("tcp_established_fail_flag",
            MapUtils.getLongValue(flowlogRecord, "tcp_established_fail_flag", 0)
                + MapUtils.getLongValue(flowlog, "tcp_established_fail_flag", 0));
        flowlogRecord.put("established_sessions",
            MapUtils.getLongValue(flowlogRecord, "established_sessions", 0)
                + MapUtils.getLongValue(flowlog, "established_sessions", 0));
        flowlogRecord.put("tcp_syn_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_syn_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_syn_packets", 0));
        flowlogRecord.put("tcp_syn_ack_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_syn_ack_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_syn_ack_packets", 0));
        flowlogRecord.put("tcp_syn_rst_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_syn_rst_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_syn_rst_packets", 0));
        flowlogRecord.put("tcp_client_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_client_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_client_packets", 0));
        flowlogRecord.put("tcp_server_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_server_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_server_packets", 0));
        flowlogRecord.put("tcp_client_retransmission_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_client_retransmission_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_client_retransmission_packets", 0));
        flowlogRecord.put("tcp_server_retransmission_packets",
            MapUtils.getLongValue(flowlogRecord, "tcp_server_retransmission_packets", 0)
                + MapUtils.getLongValue(flowlog, "tcp_server_retransmission_packets", 0));
      }

      merge.put(MapUtils.getString(flowlog, "flow_id"), flowlogRecord);
    });

    return Lists.newArrayList(merge.values());
  }

  @SuppressWarnings("unchecked")
  private List<String> transFlowLogMapToStr(Map<String, Object> item, List<String> titles,
      Map<String, String> columnNameMap, Map<String, Tuple3<String, String, String>> saAppDict,
      Map<String, String> networkDict, Map<String, String> serviceDict,
      Map<String, String> hostGroupDict, Map<String, String> protocolDict,
      Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> locationDict,
      Map<String, String> tcpSessionStateDict, Map<String, String> ethernetTypeDict,
      Map<String, String> ipLocalityDict) {
    Tuple3<String, String,
        String> app = saAppDict.get(MapUtils.getString(item, "application_id", ""));
    String appName = app != null ? app.getT1() : "";
    String appCategoryName = app != null ? app.getT2() : "";
    String appSubCategoryName = app != null ? app.getT3() : "";

    List<String> values = titles.stream().map(title -> {
      String field = columnNameMap.get(title);

      String value = "";
      switch (field) {
        case "start_time":
        case "report_time":
          value = DateUtils.toStringNanoISO8601((OffsetDateTime) item.get(field),
              ZoneId.systemDefault());
          break;
        case "network_id":
          List<String> networkIds = (List<String>) item.get("network_id");
          value = StringUtils.join(
              networkIds.stream().map(networkId -> MapUtils.getString(networkDict, networkId, ""))
                  .collect(Collectors.toList()),
              "|");
          break;
        case "service_id":
          List<String> serviceIds = (List<String>) item.get("service_id");
          value = StringUtils.join(
              serviceIds.stream().map(serviceId -> MapUtils.getString(serviceDict, serviceId, ""))
                  .collect(Collectors.toList()),
              "|");
          break;
        case "tcp_client_retransmission_rate":
          Long tcpClientRetransmissionPackets = MapUtils.getLong(item,
              "tcp_client_retransmission_packets");
          Long tcpClientPackets = MapUtils.getLong(item, "tcp_client_packets");
          double tcpClientRetransmissionRate = tcpClientPackets == 0 ? 0
              : tcpClientRetransmissionPackets * 100 / (double) tcpClientPackets;
          BigDecimal tcp_client_retransmission_rate = new BigDecimal(tcpClientRetransmissionRate)
              .setScale(2, RoundingMode.HALF_UP);
          value = tcp_client_retransmission_rate.toString() + "%";
          break;
        case "tcp_server_retransmission_rate":
          Long tcpServerRetransmissionPackets = MapUtils.getLong(item,
              "tcp_server_retransmission_packets");
          Long tcpServerPackets = MapUtils.getLong(item, "tcp_server_packets");
          double tcpServerRetransmissionRate = tcpServerPackets == 0 ? 0
              : tcpServerRetransmissionPackets * 100 / (double) tcpServerPackets;
          BigDecimal tcp_server_retransmission_rate = new BigDecimal(tcpServerRetransmissionRate)
              .setScale(2, RoundingMode.HALF_UP);
          value = tcp_server_retransmission_rate.toString() + "%";
          break;
        case "duration":
          value = Long.toString(Math.round(MapUtils.getLong(item, "duration", 0L) / 1000d));
          break;
        case "application_category_id":
          value = appCategoryName;
          break;
        case "application_subcategory_id":
          value = appSubCategoryName;
          break;
        case "application_id":
          value = appName;
          break;
        case "l7_protocol_id":
          value = MapUtils.getString(protocolDict, MapUtils.getString(item, "l7_protocol_id", ""),
              "");
          break;
        case "tcp_session_state":
          value = MapUtils.getString(tcpSessionStateDict,
              MapUtils.getString(item, "tcp_session_state", ""), "");
          break;
        case "ethernet_type":
          value = MapUtils.getString(ethernetTypeDict,
              MapUtils.getString(item, "ethernet_type", ""), "");
          break;
        case "hostgroup_id_initiator":
        case "hostgroup_id_responder":
          value = MapUtils.getString(hostGroupDict, MapUtils.getString(item, field, ""), "");
          break;
        case "country_id_initiator":
        case "country_id_responder":
          value = MapUtils.getString(locationDict.getT1(), MapUtils.getString(item, field, ""), "");
          break;
        case "province_id_initiator":
        case "province_id_responder":
          value = MapUtils.getString(locationDict.getT2(), MapUtils.getString(item, field, ""), "");
          break;
        case "city_id_initiator":
        case "city_id_responder":
          value = MapUtils.getString(locationDict.getT3(), MapUtils.getString(item, field, ""), "");
          break;
        case "ip_initiator":
          Inet4Address ipv4Initiator = (Inet4Address) item.get("ipv4_initiator");
          Inet6Address ipv6Initiator = (Inet6Address) item.get("ipv6_initiator");
          value = ipv4Initiator != null ? ipv4Initiator.getHostAddress()
              : ipv6Initiator.getHostAddress();
          break;
        case "ip_responder":
          Inet4Address ipv4Responder = (Inet4Address) item.get("ipv4_responder");
          Inet6Address ipv6Responder = (Inet6Address) item.get("ipv6_responder");
          value = ipv4Responder != null ? ipv4Responder.getHostAddress()
              : ipv6Responder.getHostAddress();
          break;
        case "ip_nat_initiator":
          String ipNatInitiator = MapUtils.getString(item, "ip_nat_initiator");
          if (StringUtils.isNotBlank(ipNatInitiator)
              && StringUtils.startsWith(ipNatInitiator, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
            ipNatInitiator = StringUtils.substringAfter(ipNatInitiator,
                ManagerConstants.IPV4_TO_IPV6_PREFIX);
          }
          value = ipNatInitiator;
          break;
        case "ip_nat_responder":
          String ipNatResponder = MapUtils.getString(item, "ip_nat_responder");
          if (StringUtils.isNotBlank(ipNatResponder)
              && StringUtils.startsWith(ipNatResponder, ManagerConstants.IPV4_TO_IPV6_PREFIX)) {
            ipNatResponder = StringUtils.substringAfter(ipNatResponder,
                ManagerConstants.IPV4_TO_IPV6_PREFIX);
          }
          value = ipNatResponder;
          break;
        case "port_nat_initiator":
          value = String.valueOf(MapUtils.getIntValue(item, "port_nat_initiator"));
          break;
        case "port_nat_responder":
          value = String.valueOf(MapUtils.getIntValue(item, "port_nat_responder"));
          break;
        case "ip_locality_initiator":
        case "ip_locality_responder":
          value = MapUtils.getString(ipLocalityDict, MapUtils.getString(item, field, "0"), "");
          break;
        default:
          value = MapUtils.getString(item, field, "");
          break;
      }

      return value;
    }).collect(Collectors.toList());

    return values;
  }

  private Map<String, Tuple3<String, String, String>> querySaApplicationDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    Map<String, String> categoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT1().size() + customCategorys.size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category.getNameText()));
    customCategorys.forEach(
        category -> categoryDict.put(category.getCategoryId(), category.getName() + "（自定义）"));

    // 小类
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    Map<String, String> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size() + customSubCategorys.size());
    knowledgeRules.getT2().forEach(subCategory -> subCategoryDict
        .put(subCategory.getSubCategoryId(), subCategory.getNameText()));
    customSubCategorys.forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(),
        subCategory.getName() + "（自定义）"));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, Tuple3<String, String, String>> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3().forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getNameText(), StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    customRules.forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getName() + "（自定义）",
              StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    return applicationDict;
  }

  private Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> querySaDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    Map<String, String> categoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT1().size() + customCategorys.size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category.getNameText()));
    customCategorys.forEach(
        category -> categoryDict.put(category.getCategoryId(), category.getName() + "（自定义）"));

    // 小类
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    Map<String, String> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size() + customSubCategorys.size());
    knowledgeRules.getT2().forEach(subCategory -> subCategoryDict
        .put(subCategory.getSubCategoryId(), subCategory.getNameText()));
    customSubCategorys.forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(),
        subCategory.getName() + "（自定义）"));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, String> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3()
        .forEach(app -> applicationDict.put(app.getApplicationId(), app.getNameText()));
    customRules
        .forEach(app -> applicationDict.put(app.getApplicationId(), app.getName() + "（自定义）"));

    return Tuples.of(categoryDict, subCategoryDict, applicationDict);
  }

  private Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> queryGeoIpDict() {
    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();

    Map<String, String> countryDict = geolocations.getT1().stream()
        .collect(Collectors.toMap(GeoCountryBO::getCountryId, GeoCountryBO::getNameText));
    countryDict.putAll(geoService.queryCustomCountrys().stream()
        .collect(Collectors.toMap(GeoCustomCountryBO::getCountryId, GeoCustomCountryBO::getName)));

    Map<String, String> provinceDict = geolocations.getT2().stream()
        .collect(Collectors.toMap(GeoProvinceBO::getProvinceId, GeoProvinceBO::getNameText));

    Map<String, String> cityDict = geolocations.getT3().stream()
        .collect(Collectors.toMap(GeoCityBO::getCityId, GeoCityBO::getNameText));

    return Tuples.of(countryDict, provinceDict, cityDict);
  }

  private static IPv6AddressRange ipv6ToRange(String ipv6) {
    if (StringUtils.contains(ipv6, "-")) {
      String[] ipRange = StringUtils.split(ipv6, "-");
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipRange[0]),
          IPv6Address.fromString(ipRange[1]));
    } else if (StringUtils.contains(ipv6, "/")) {
      return IPv6Network.fromString(ipv6);
    } else {
      return IPv6AddressRange.fromFirstAndLast(IPv6Address.fromString(ipv6),
          IPv6Address.fromString(ipv6));
    }
  }
}
