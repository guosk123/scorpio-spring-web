package com.machloop.fpc.manager.appliance.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.dao.FlowLogEstFailDao;
import com.machloop.fpc.manager.appliance.service.FlowLogEstFailService;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.manager.global.service.SlowQueryService;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author guosk
 *
 * create at 2022年3月30日, fpc-manager
 */
@Service
public class FlowLogEstFailServiceImpl implements FlowLogEstFailService {

  private static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.put("network_id", "所属网络");
    fields.put("service_id", "所属业务");
    fields.put("start_time", "开始时间");
    fields.put("report_time", "记录时间");
    fields.put("tcp_session_state", "tcp会话状态");
    fields.put("ip_initiator", "源IP");
    fields.put("ip_locality_initiator", "源IP位置");
    fields.put("ip_responder", "目的IP");
    fields.put("ip_locality_responder", "目的IP位置");
    fields.put("port_initiator", "源端口");
    fields.put("port_responder", "目的端口");
  }

  @Autowired
  private FlowLogEstFailDao flowLogEstFailDao;

  @Autowired
  private SlowQueryService slowQueryService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogEstFailService#queryFlowLogs(java.lang.String, com.machloop.alpha.common.base.page.Pageable, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String)
   */
  @Override
  public Page<Map<String, Object>> queryFlowLogs(String queryId, Pageable page,
      FlowLogQueryVO queryVO, String columns) {
    Page<Map<String, Object>> flowLogPage = flowLogEstFailDao.queryFlowLogs(queryId, page, queryVO,
        columnMapping(columns));

    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }

    return flowLogPage;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogEstFailService#queryFlowLogStatistics(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO)
   */
  @Override
  public Map<String, Object> queryFlowLogStatistics(String queryId, FlowLogQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("total", flowLogEstFailDao.countFlowLogs(queryId, queryVO));

    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogEstFailService#exportFlowLogs(java.lang.String, com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, com.machloop.alpha.common.base.page.Sort, java.io.OutputStream)
   */
  @Override
  public void exportFlowLogs(String queryId, FlowLogQueryVO queryVO, String columns, Sort sort,
      OutputStream out) throws IOException {
    // 网络名称
    Map<String, String> networkDict = networkService.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkBO::getId, NetworkBO::getName));
    networkDict.putAll(logicalSubnetService.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetBO::getId, LogicalSubnetBO::getName)));
    // 业务名称
    Map<String, String> serviceDict = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getId, ServiceBO::getName));
    // tcp会话字典
    Map<String, String> tcpSessionStateDict = dictManager.getBaseDict()
        .getItemMap("flow_log_tcp_session_state");
    // ip内外网位置字典
    Map<String, String> ipLocalityDict = dictManager.getBaseDict()
        .getItemMap("flow_log_ip_address_locality");
    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));

    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(columns, "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(columns);
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    String title = CsvUtils.spliceRowData(titles.toArray(new String[titles.size()]));
    out.write(title.getBytes(StandardCharsets.UTF_8));

    List<Map<String, Object>> result = flowLogEstFailDao.queryFlowLogs(queryId, queryVO,
        columnMapping(columns), sort,
        Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.max.count")));

    // 完成慢查询
    if (StringUtils.isNotBlank(queryId)) {
      slowQueryService.finish(queryId);
    }

    for (Map<String, Object> item : result) {
      String line = transFlowLogMapToStr(item, titles, columnNameMap, networkDict, serviceDict,
          tcpSessionStateDict, ipLocalityDict);
      out.write(line.getBytes(StandardCharsets.UTF_8));
    }
  }

  private String columnMapping(String columns) {
    if (StringUtils.equals(columns, "*")) {
      return columns;
    }

    Set<String> columnSets = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    CsvUtils.convertCSVToList(columns).forEach(item -> {
      switch (item) {
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

  @SuppressWarnings("unchecked")
  private String transFlowLogMapToStr(Map<String, Object> item, List<String> titles,
      Map<String, String> columnNameMap, Map<String, String> networkDict,
      Map<String, String> serviceDict, Map<String, String> tcpSessionStateDict,
      Map<String, String> ipLocalityDict) {
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
        case "tcp_session_state":
          value = MapUtils.getString(tcpSessionStateDict,
              MapUtils.getString(item, "tcp_session_state", ""), "");
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

    return CsvUtils.spliceRowData(values.toArray(new String[values.size()]));
  }

}
