package com.machloop.fpc.cms.center.metadata.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.fpc.cms.center.appliance.bo.ServiceBO;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.global.dao.CounterDao;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolDhcpLogDO;
import com.machloop.fpc.cms.center.metadata.service.LogRecordService;
import com.machloop.fpc.cms.center.metadata.vo.ProtocolDhcpLogVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;

/**
 * @author guosk
 *
 * create at 2020年12月11日, fpc-manager
 */
@Service("protocolDhcpLogService")
public class ProtocolDhcpLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolDhcpLogVO, ProtocolDhcpLogDO>
    implements LogRecordService<ProtocolDhcpLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("version", "DHCP版本");
    fields.put("srcIpv4", "源IPv4");
    fields.put("destIpv4", "目的IPv4");
    fields.put("srcIpv6", "源IPv6");
    fields.put("destIpv6", "目的IPv6");
    fields.put("srcMac", "源MAC地址");
    fields.put("destMac", "目的MAC地址");
    fields.put("srcPort", "源端口");
    fields.put("destPort", "目的端口");
    fields.put("messageType", "消息类型");
    fields.put("transactionId", "事务ID");
    fields.put("parameters", "请求参数列表");
    fields.put("offeredIpv4Address", "分配的IPv4地址");
    fields.put("offeredIpv6Address", "分配的IPv6地址");
    fields.put("upstreamBytes", "请求字节数");
    fields.put("downstreamBytes", "应答字节数");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolDhcpLogDao")
  private LogRecordDao<ProtocolDhcpLogDO> logRecordDao;

  @Autowired
  private CounterDao counterDao;

  @Autowired
  private SensorNetworkService networkService;

  @Autowired
  private SensorLogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#getLogRecordDao()
   */
  @Override
  protected LogRecordDao<ProtocolDhcpLogDO> getLogRecordDao() {
    return logRecordDao;
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#getCounterDao()
   */
  @Override
  protected CounterDao getCounterDao() {
    return counterDao;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDO2LogVO(com.machloop.fpc.cms.center.metadata.data.AbstractLogRecordDO)
   */
  @Override
  protected ProtocolDhcpLogVO convertLogDO2LogVO(ProtocolDhcpLogDO logDO) {
    ProtocolDhcpLogVO protocolDhcpLogVO = new ProtocolDhcpLogVO();
    BeanUtils.copyProperties(logDO, protocolDhcpLogVO);
    return protocolDhcpLogVO;
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolDhcpLogDO> logDOList,
      String columns) {
    Map<String, String> versionDict = dictManager.getBaseDict().getItemMap("protocol_dhcp_version");

    Map<String,
        String> messageTypeDict = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, String> v4messageTypeDict = dictManager.getBaseDict()
        .getItemMap("protocol_dhcp_message_type");
    messageTypeDict.putAll(v4messageTypeDict.entrySet().stream().collect(
        Collectors.toMap(entry -> String.join("_", "0", entry.getKey()), Entry::getValue)));
    Map<String, String> v6MessageTypeDict = dictManager.getBaseDict()
        .getItemMap("protocol_dhcpv6_message_type");
    Map<String, String> networkDict = networkService.querySensorNetworks().stream()
        .collect(Collectors.toMap(SensorNetworkBO::getNetworkInSensorId, SensorNetworkBO::getName));
    networkDict.putAll(logicalSubnetService.querySensorLogicalSubnets().stream()
        .collect(Collectors.toMap(SensorLogicalSubnetBO::getId, SensorLogicalSubnetBO::getName)));
    Map<String, String> serviceDict = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getId, ServiceBO::getName));

    messageTypeDict.putAll(v6MessageTypeDict.entrySet().stream().collect(
        Collectors.toMap(entry -> String.join("_", "1", entry.getKey()), Entry::getValue)));

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
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    for (ProtocolDhcpLogDO protocolDhcpLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolDhcpLogDO, field, null, networkDict, serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "srcIpv4":
            value = protocolDhcpLogDO.getSrcIpv4();
            break;
          case "srcIpv6":
            value = protocolDhcpLogDO.getSrcIpv6();
            break;
          case "destIpv4":
            value = protocolDhcpLogDO.getDestIpv4();
            break;
          case "destIpv6":
            value = protocolDhcpLogDO.getDestIpv6();
            break;
          case "srcPort":
            value = String.valueOf(protocolDhcpLogDO.getSrcPort());
            break;
          case "destPort":
            value = String.valueOf(protocolDhcpLogDO.getDestPort());
            break;
          case "version":
            value = MapUtils.getString(versionDict, protocolDhcpLogDO.getVersion() + "", "");
            break;
          case "srcMac":
            value = protocolDhcpLogDO.getSrcMac();
            break;
          case "destMac":
            value = protocolDhcpLogDO.getDestMac();
            break;
          case "messageType":
            value = MapUtils.getString(messageTypeDict,
                protocolDhcpLogDO.getVersion() + "_" + protocolDhcpLogDO.getMessageType(), "");
            break;
          case "transactionId":
            value = protocolDhcpLogDO.getTransactionId();
            break;
          case "parameters":
            StringBuilder parameters = new StringBuilder();
            protocolDhcpLogDO.getParameters()
                .forEach(parameter -> parameters.append(parameter).append(" "));
            if (parameters.length() > 0) {
              parameters.deleteCharAt(parameters.length() - 1);
            }
            value = parameters.toString();
            break;
          case "offeredIpv4Address":
            value = protocolDhcpLogDO.getOfferedIpv4Address();
            break;
          case "offeredIpv6Address":
            value = protocolDhcpLogDO.getOfferedIpv6Address();
            break;
          case "upstreamBytes":
            value = String.valueOf(protocolDhcpLogDO.getUpstreamBytes());
            break;
          case "downstreamBytes":
            value = String.valueOf(protocolDhcpLogDO.getDownstreamBytes());
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

}
