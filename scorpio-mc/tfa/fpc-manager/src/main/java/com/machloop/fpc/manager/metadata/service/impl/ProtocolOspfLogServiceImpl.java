package com.machloop.fpc.manager.metadata.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
import com.machloop.fpc.manager.global.dao.CounterDao;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolOspfLogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolOspfLogVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author guosk
 *
 * create at 2021年5月12日, fpc-manager
 */
@Service("protocolOspfLogService")
public class ProtocolOspfLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolOspfLogVO, ProtocolOspfLogDO>
    implements LogRecordService<ProtocolOspfLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("version", "版本");
    fields.put("messageType", "消息类型");
    fields.put("packetLength", "包长");
    fields.put("sourceOspfRouter", "源路由");
    fields.put("areaId", "区域ID");
    fields.put("linkStateIpv4Address", "通告IPv4地址");
    fields.put("linkStateIpv6Address", "通告IPv6地址");
    fields.put("message", "消息");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolOspfLogDao")
  private LogRecordDao<ProtocolOspfLogDO> logRecordDao;

  @Autowired
  private CounterDao counterDao;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#getLogRecordDao()
   */
  @Override
  protected LogRecordDao<ProtocolOspfLogDO> getLogRecordDao() {
    return logRecordDao;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#getCounterDao()
   */
  @Override
  protected CounterDao getCounterDao() {
    return counterDao;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDO2LogVO(com.machloop.fpc.manager.appliance.data.protocol.AbstractProtocolLogDO)
   */
  @Override
  protected ProtocolOspfLogVO convertLogDO2LogVO(ProtocolOspfLogDO logDO) {
    ProtocolOspfLogVO protocolOspfLogVO = new ProtocolOspfLogVO();
    BeanUtils.copyProperties(logDO, protocolOspfLogVO);

    return protocolOspfLogVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolOspfLogDO> logDOList,
      String columns) {
    Map<String, String> policyLevelDict = dictManager.getBaseDict()
        .getItemMap("appliance_collect_policy_level");
    Map<String, String> messageTypeDict = dictManager.getBaseDict()
        .getItemMap("protocol_ospf_message_type");
    Map<String, String> networkDict = networkService.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkBO::getId, NetworkBO::getName));
    networkDict.putAll(logicalSubnetService.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetBO::getId, LogicalSubnetBO::getName)));
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
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    for (ProtocolOspfLogDO protocolOspfLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolOspfLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "version":
            value = String.valueOf(protocolOspfLogDO.getVersion());
            break;
          case "messageType":
            value = messageTypeDict.getOrDefault(String.valueOf(protocolOspfLogDO.getMessageType()),
                "");
            break;
          case "packetLength":
            value = String.valueOf(protocolOspfLogDO.getPacketLength());
            break;
          case "sourceOspfRouter":
            value = String.valueOf(protocolOspfLogDO.getSourceOspfRouter());
            break;
          case "areaId":
            value = String.valueOf(protocolOspfLogDO.getAreaId());
            break;
          case "linkStateIpv4Address":
            StringBuilder ipv4Builder = new StringBuilder();
            protocolOspfLogDO.getLinkStateIpv4Address()
                .forEach(ipv4 -> ipv4Builder.append(ipv4).append(" "));
            if (ipv4Builder.length() > 0) {
              ipv4Builder.deleteCharAt(ipv4Builder.length() - 1);
            }
            value = ipv4Builder.toString();
            break;
          case "linkStateIpv6Address":
            StringBuilder ipv6Builder = new StringBuilder();
            protocolOspfLogDO.getLinkStateIpv6Address()
                .forEach(ipv6 -> ipv6Builder.append(ipv6).append(" "));
            if (ipv6Builder.length() > 0) {
              ipv6Builder.deleteCharAt(ipv6Builder.length() - 1);
            }
            value = ipv6Builder.toString();
            break;
          case "message":
            value = protocolOspfLogDO.getMessage();
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
