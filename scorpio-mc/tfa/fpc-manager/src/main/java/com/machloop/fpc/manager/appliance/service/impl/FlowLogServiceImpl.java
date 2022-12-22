package com.machloop.fpc.manager.appliance.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.dao.FlowLogDao;
import com.machloop.fpc.manager.appliance.service.FlowLogService;
import com.machloop.fpc.manager.appliance.service.WebSharkService;
import com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO;
import com.machloop.fpc.manager.knowledge.bo.SaApplicationBO;
import com.machloop.fpc.manager.knowledge.bo.SaCategoryBO;
import com.machloop.fpc.manager.knowledge.bo.SaCustomApplicationBO;
import com.machloop.fpc.manager.knowledge.bo.SaSubCategoryBO;
import com.machloop.fpc.manager.knowledge.service.SaService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;


/**
 * @author mazhiyuan
 *
 *         create at 2020年2月19日, fpc-manager
 */
// @Service
public class FlowLogServiceImpl implements FlowLogService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FlowLogServiceImpl.class);

  private Map<String, Date> fileMap = Maps.newConcurrentMap();

  @Autowired
  private WebSharkService webSharkService;

  @Autowired
  private SaService saService;

  @Autowired
  private FlowLogDao flowLogDao;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${fpc.engine.rest.server.protocol}")
  private String fileServerProtocol;
  @Value("${fpc.engine.rest.server.host}")
  private String fileServerHost;
  @Value("${fpc.engine.rest.server.port}")
  private String fileServerPort;

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogService#queryFlowLogs(com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String, int, int, double, java.lang.String, java.lang.String, int, java.lang.String)
   */
  @Override
  public String queryFlowLogs(FlowLogQueryVO queryVO, String queryTaskId, int terminateAfter,
      int timeout, double samplingRate, String sortProperty, String sortDirection, int pageSize,
      String searchAfter) {

    return flowLogDao.queryFlowLogs(queryVO, queryTaskId, terminateAfter, timeout, samplingRate,
        sortProperty, sortDirection, pageSize, searchAfter);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogService#queryFlowLogs(java.lang.String, java.util.Date)
   */
  @Override
  public List<Map<String, Object>> queryFlowLogs(String flowId, Date inclusiveTime) {
    return flowLogDao.queryFlowLogs(flowId, inclusiveTime);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogService#queryFlowLogStatistics(com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String, int, java.lang.String, int, int, int, double)
   */
  @Override
  public String queryFlowLogStatistics(FlowLogQueryVO queryVO, String queryTaskId,
      int histogramInterval, String termFieldName, int termSize, int terminateAfter, int timeout,
      double samplingRate) {

    return flowLogDao.queryFlowLogStatistics(queryVO, queryTaskId, histogramInterval, termFieldName,
        termSize, terminateAfter, timeout, samplingRate);
  }


  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogService#queryFlowLogStatisticsGroupByIp(com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, java.lang.String, int, int, java.lang.String, java.lang.String)
   */
  @Override
  public String queryFlowLogStatisticsGroupByIp(FlowLogQueryVO queryVO, String queryTaskId,
      int termSize, int timeout, String sortProperty, String sortDirection) {
    return flowLogDao.queryFlowLogStatisticsGroupByIp(queryVO, queryTaskId, termSize, timeout,
        sortProperty, sortDirection);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogService#exportFlowLogs(com.machloop.fpc.manager.appliance.vo.FlowLogQueryVO, int, int, double, java.lang.String, java.lang.String, java.io.OutputStream)
   */
  @Override
  public void exportFlowLogs(FlowLogQueryVO queryVO, int terminateAfter, int timeout,
      double samplingRate, String sortProperty, String sortDirection, OutputStream out)
      throws IOException {

    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();

    String title = "`接口编号`,`开始时间`,`结束时间`,`持续时间(s)`,`上行字节数`,"
        + "`下行字节数`,`总字节数`,`上行包数`,`下行包数`,`总包数`,`上行payload`,`下行payload`,"
        + "`payload总字节数`,`源MAC`,`目的MAC`,`网络层协议`,`源IP`,`目的IP`,`VLANID`,`源端口`,"
        + "`目的端口`,`传输层协议`,`应用层协议`,`应用分类`,`应用子分类`,`应用名称`,`源IP国家`,"
        + "`源IP省份`,`源IP城市`,`目的IP国家`,`目的IP省份`,`目的IP城市`\n";
    out.write(title.getBytes(StandardCharsets.UTF_8));

    int maxCount = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.max.count"));
    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));
    String scrollId = null;
    int count = 0;
    while (count < maxCount) {
      Tuple2<String, List<Map<String, Object>>> result = flowLogDao.queryFlowLogs(queryVO,
          terminateAfter, timeout, samplingRate, sortProperty, sortDirection, batchSize, scrollId);
      scrollId = result.getT1();
      List<Map<String, Object>> list = result.getT2();

      for (Map<String, Object> item : list) {
        String line = transFlowLogMapToStr(item, saAppDict);
        out.write(line.getBytes(StandardCharsets.UTF_8));
        count += 1;
        if (count == maxCount) {
          break;
        }
      }
      // 返回结果数不足batchSize, 说明已全部返回
      if (list.size() < batchSize) {
        break;
      }
      if (StringUtils.isBlank(scrollId)) {
        break;
      }
    }

    // 导出完成清空scroll
    if (StringUtils.isNotBlank(scrollId)) {
      if (!flowLogDao.clearScroll(scrollId)) {
        LOGGER.warn("failed to clear scroll, scroll id: [{}]", scrollId);
      }
    }
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogService#analyzeFlowPacket(java.lang.String, java.util.Date, java.util.Date, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
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
        String serverIp = fileServerHost;
        String[] ipList = StringUtils.split(fileServerHost, ",");
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
          url.append(fileServerProtocol);
          url.append("://");
          if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
            url.append("[").append(serverIp).append("]");
          } else {
            url.append(serverIp);
          }
          url.append(":");
          url.append(fileServerPort);
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
   * @see com.machloop.fpc.manager.appliance.service.FlowLogService#fetchFlowLogPacketFileUrls(java.lang.String, java.util.Date, java.util.Date, java.lang.String)
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
    String serverIp = fileServerHost;
    String[] ipList = StringUtils.split(fileServerHost, ",");
    if (ipList.length > 1) {
      if (InetAddressUtils.isIPv4Address(remoteAddr)) {
        serverIp = InetAddressUtils.isIPv4Address(ipList[0]) ? ipList[0] : ipList[1];
      } else {
        serverIp = InetAddressUtils.isIPv6Address(ipList[0]) ? ipList[0] : ipList[1];
      }
    }

    // 拼接文件下载地址
    StringBuilder url = new StringBuilder();
    try {
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
        url.append("[").append(serverIp).append("]");
      } else {
        url.append(serverIp);
      }
      url.append(":");
      url.append(fileServerPort);
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

  /**
   * @see com.machloop.fpc.manager.appliance.service.FlowLogService#cancelFlowLogsQueryTask(java.lang.String)
   */
  @Override
  public void cancelFlowLogsQueryTask(String queryTaskId) {
    flowLogDao.cancelFlowLogsQueryTask(queryTaskId);
  }

  private String transFlowLogMapToStr(Map<String, Object> item,
      Map<String, Tuple3<String, String, String>> saAppDict) {
    Tuple3<String, String,
        String> app = saAppDict.get(MapUtils.getString(item, "application_id", ""));
    String appName = app != null ? app.getT1() : "";
    String appCategoryName = app != null ? app.getT2() : "";
    String appSubCategoryName = app != null ? app.getT3() : "";
    return CsvUtils.spliceRowData(MapUtils.getString(item, "interface", ""),
        MapUtils.getString(item, "start_time", ""), MapUtils.getString(item, "end_time", ""),
        Long.toString(Math.round(MapUtils.getLong(item, "duration", 0L) / 1000d)),
        MapUtils.getString(item, "upstream_bytes", ""),
        MapUtils.getString(item, "downstream_bytes", ""),
        MapUtils.getString(item, "total_bytes", ""),
        MapUtils.getString(item, "upstream_packets", ""),
        MapUtils.getString(item, "downstream_packets", ""),
        MapUtils.getString(item, "total_packets", ""),
        MapUtils.getString(item, "payload_upstream_bytes", ""),
        MapUtils.getString(item, "payload_downstream_bytes", ""),
        MapUtils.getString(item, "payload_total_bytes", ""),
        MapUtils.getString(item, "ethernet_initiator", ""),
        MapUtils.getString(item, "ethernet_responder", ""),
        MapUtils.getString(item, "ethernet_protocol", ""),
        MapUtils.getString(item,
            StringUtils.equals(MapUtils.getString(item, "ethernet_protocol", ""), "ipv4")
                ? "ipv4_initiator"
                : "ipv6_initiator",
            ""),
        MapUtils.getString(item,
            StringUtils.equals(MapUtils.getString(item, "ethernet_protocol", ""), "ipv4")
                ? "ipv4_responder"
                : "ipv6_responder",
            ""),
        MapUtils.getString(item, "vlan_id", ""), MapUtils.getString(item, "port_initiator", ""),
        MapUtils.getString(item, "port_responder", ""), MapUtils.getString(item, "ip_protocol", ""),
        MapUtils.getString(item, "l7_protocol", ""), appCategoryName, appSubCategoryName, appName,
        MapUtils.getString(item, "country_initiator", ""),
        MapUtils.getString(item, "province_initiator", ""),
        MapUtils.getString(item, "city_initiator", ""),
        MapUtils.getString(item, "country_responder", ""),
        MapUtils.getString(item, "province_responder", ""),
        MapUtils.getString(item, "city_responder", ""));
  }

  private Map<String, Tuple3<String, String, String>> querySaApplicationDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    Map<String,
        SaCategoryBO> categoryDict = Maps.newHashMapWithExpectedSize(knowledgeRules.getT1().size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category));

    // 小类
    Map<String, SaSubCategoryBO> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size());
    knowledgeRules.getT2()
        .forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(), subCategory));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, Tuple3<String, String, String>> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3().forEach(app -> {
      SaCategoryBO category = categoryDict.get(app.getCategoryId());
      SaSubCategoryBO subCategory = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getNameText(), category != null ? category.getNameText() : "",
              subCategory != null ? subCategory.getNameText() : ""));
    });
    customRules.forEach(app -> {
      SaCategoryBO category = categoryDict.get(app.getCategoryId());
      SaSubCategoryBO subCategory = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getName() + "（自定义）", category != null ? category.getNameText() : "",
              subCategory != null ? subCategory.getNameText() : ""));
    });
    return applicationDict;
  }
}
