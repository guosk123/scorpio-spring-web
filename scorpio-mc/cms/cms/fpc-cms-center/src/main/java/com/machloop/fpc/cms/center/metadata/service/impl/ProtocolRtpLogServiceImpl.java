package com.machloop.fpc.cms.center.metadata.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.appliance.bo.ServiceBO;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.global.dao.CounterDao;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.dao.ProtocolRtpLogDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolRtpLogDO;
import com.machloop.fpc.cms.center.metadata.service.LogRecordService;
import com.machloop.fpc.cms.center.metadata.service.ProtocolRtpLogService;
import com.machloop.fpc.cms.center.metadata.vo.LogRecordQueryVO;
import com.machloop.fpc.cms.center.metadata.vo.ProtocolRtpLogVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;

/**
 * @author ChenXiao
 * create at 2022/12/9
 */
@Service("protocolRtpLogService")
public class ProtocolRtpLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolRtpLogVO, ProtocolRtpLogDO>
    implements LogRecordService<ProtocolRtpLogVO>, ProtocolRtpLogService {


  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.put("level", "采集策略等级");
    fields.put("policyName", "采集策略名称");
    fields.put("flowId", "RTP流ID");
    fields.put("networkId", "网络ID");
    fields.put("serviceId", "业务ID");
    fields.put("applicationId", "应用ID");
    fields.put("startTime", "通讯开始时间");
    fields.put("endTime", "通讯结束时间");
    fields.put("inviteTime", "通讯邀请时间");
    fields.put("from", "发送方设备编码");
    fields.put("srcIp", "发送方IP");
    fields.put("srcPort", "发送方端口");
    fields.put("to", "接收方设备编码");
    fields.put("destIp", "接收方IP");
    fields.put("destPort", "接收方端口");
    fields.put("ipProtocol", "rtp流传输层协议");
    fields.put("ssrc", "rtp流SSRC");
    fields.put("status", "视频流状态");
    fields.put("rtpTotalPackets", "此次上报的rtp总包数");
    fields.put("rtpLossPackets", "此次上报的rtp丢包数");
    fields.put("rtpLossPacketsRate", "此次上报的rtp丢包率");
    fields.put("jitterMax", "此次上报的rtp最大抖动 单位是微秒");
    fields.put("jitterMean", "此次上报的rtp平均抖动 单位是微秒");
    fields.put("payload", "rtp流负载类型");
    fields.put("inviteSrcIp", "控制通道源IP");
    fields.put("inviteSrcPort", "控制通道源端口");
    fields.put("inviteDestIp", "控制通道目的IP");
    fields.put("inviteDestPort", "控制通道目的端口");
    fields.put("inviteIpProtocol", "控制通道传输层协议");
    fields.put("sipFlowId", "控制通道流ID");
  }


  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolRtpLogDao")
  private LogRecordDao<ProtocolRtpLogDO> logRecordDao;

  @Autowired
  private CounterDao counterDao;

  @Autowired
  private SensorNetworkService networkService;

  @Autowired
  private SensorLogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;


  @Override
  protected LogRecordDao<ProtocolRtpLogDO> getLogRecordDao() {
    return logRecordDao;
  }

  @Override
  protected CounterDao getCounterDao() {
    return counterDao;
  }

  @Autowired
  private ProtocolRtpLogDao protocolRtpLogDao;

  @Override
  protected String columnMapping(String columns) {

    if (StringUtils.equals(columns, "*")) {
      return columns;
    }

    Set<String> columnSets = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    columnSets.add("network_id");
    columnSets.add("service_id");
    columnSets.add("rtp_total_packets");
    columnSets.add("rtp_loss_packets");


    CsvUtils.convertCSVToList(columns).forEach(item -> {
      columnSets.add(TextUtils.camelToUnderLine(item));
    });

    return CsvUtils.convertCollectionToCSV(columnSets);
  }

  @Override
  protected ProtocolRtpLogVO convertLogDO2LogVO(ProtocolRtpLogDO logDO) {
    ProtocolRtpLogVO protocolRtpLogVO = new ProtocolRtpLogVO();
    BeanUtils.copyProperties(logDO, protocolRtpLogVO);

    return protocolRtpLogVO;
  }

  @Override
  public Map<String, Object> queryLogRecordStatistics(LogRecordQueryVO queryVO) {
    List<String> flowIds = StringUtils.isNotBlank(queryVO.getId())
        ? CsvUtils.convertCSVToList(queryVO.getId())
        : Lists.newArrayListWithCapacity(0);

    long total = getLogRecordDao().countLogRecords(queryVO, flowIds);

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("total", total);
    return result;
  }

  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolRtpLogDO> logDOList,
      String columns) {
    Map<String, String> policyLevelDict = dictManager.getBaseDict()
        .getItemMap("appliance_collect_policy_level");
    Map<String, String> rtpStatusDict = dictManager.getBaseDict().getItemMap("protocol_rtp_status");
    Map<String, String> networkDict = networkService.querySensorNetworks().stream()
        .collect(Collectors.toMap(SensorNetworkBO::getNetworkInSensorId, SensorNetworkBO::getName));
    networkDict.putAll(logicalSubnetService.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetBO::getId, SensorLogicalSubnetBO::getName)));
    Map<String, String> serviceDict = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getId, ServiceBO::getName));

    List<List<String>> lines = Lists.newArrayListWithCapacity(logDOList.size() + 1);

    // title
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(columns, "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(columns);
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }
    lines.add(titles);

    // content
    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    for (ProtocolRtpLogDO protocolRtpLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolRtpLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "inviteTime":
            value = protocolRtpLogDO.getInviteTime();
            break;
          case "from":
            value = protocolRtpLogDO.getFrom();
            break;
          case "to":
            value = protocolRtpLogDO.getTo();
            break;
          case "ipProtocol":
            value = protocolRtpLogDO.getIpProtocol();
            break;
          case "ssrc":
            value = String.valueOf(protocolRtpLogDO.getSsrc());
            break;
          case "status":
            value = rtpStatusDict.getOrDefault(String.valueOf(protocolRtpLogDO.getStatus()), "其他");
            break;
          case "rtpTotalPackets":
            value = String.valueOf(protocolRtpLogDO.getRtpTotalPackets());
            break;
          case "rtpLossPackets":
            value = String.valueOf(protocolRtpLogDO.getRtpLossPackets());
            break;
          case "rtpLossPacketsRate":
            value = String.valueOf(protocolRtpLogDO.getRtpLossPacketsRate());
            break;
          case "jitterMax":
            value = String.valueOf(protocolRtpLogDO.getJitterMax());
            break;
          case "jitterMean":
            value = String.valueOf(protocolRtpLogDO.getJitterMean());
            break;
          case "payload":
            value = protocolRtpLogDO.getPayload();
            break;
          case "inviteSrcIp":
            value = String.valueOf(protocolRtpLogDO.getInviteSrcIp());
            break;
          case "inviteSrcPort":
            value = String.valueOf(protocolRtpLogDO.getInviteSrcPort());
            break;
          case "inviteDestIp":
            value = String.valueOf(protocolRtpLogDO.getInviteDestIp());
            break;
          case "inviteDestPort":
            value = String.valueOf(protocolRtpLogDO.getInviteDestPort());
            break;
          case "inviteIpProtocol":
            value = String.valueOf(protocolRtpLogDO.getInviteIpProtocol());
            break;
          case "sipFlowId":
            value = protocolRtpLogDO.getSipFlowId();
            break;
          default:
            value = "";
            break;
        }
        return value;
      }).collect(Collectors.toList());

      lines.add(values);
    }
    return lines;
  }

  @Override
  public Map<String, Map<String, Object>> queryRtpNetworkSegmentation(LogRecordQueryVO queryVO) {

    List<Map<String, Object>> list = protocolRtpLogDao.queryRtpNetworkSegmentation(queryVO);

    Map<String,
        Map<String, Object>> res = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    for (Map<String, Object> map : list) {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      long rtpLossPackets = MapUtils.getLongValue(map, "rtp_loss_packets");
      long rtpTotalPackets = MapUtils.getLongValue(map, "rtp_total_packets");
      String rtpLossPacketsRate = new BigDecimal((double) rtpLossPackets * 100 / rtpTotalPackets)
          .setScale(2, RoundingMode.HALF_UP).doubleValue() + "%";
      temp.put("rtpLossPacketsRate", rtpLossPacketsRate);
      temp.put("rtpTotalPackets", rtpTotalPackets);
      temp.put("rtpLossPackets", rtpLossPackets);
      temp.put("jitterMax", MapUtils.getLongValue(map, "jitter_max"));
      temp.put("jitterMean", MapUtils.getLongValue(map, "jitter_mean"));
      res.put(MapUtils.getString(map, "network_id"), temp);
    }
    return res;
  }
}
