package com.machloop.fpc.manager.metadata.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.machloop.fpc.manager.global.dao.CounterDao;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolSipLogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolSipLogVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

@Service("protocolSipLogService")
public class ProtocolSipLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolSipLogVO, ProtocolSipLogDO>
    implements LogRecordService<ProtocolSipLogVO> {


  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("from", "主叫");
    fields.put("to", "被叫");
    fields.put("ipProtocol", "信令传输协议");
    fields.put("type", "请求类型");
    fields.put("seqNum", "序列号");
    fields.put("callId", "呼叫ID");
    fields.put("requestUri", "请求URI");
    fields.put("statusCode", "状态码");
    fields.put("sdp", "SDP");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolSipLogDao")
  private LogRecordDao<ProtocolSipLogDO> logRecordDao;

  @Autowired
  private CounterDao counterDao;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;

  @Override
  protected LogRecordDao<ProtocolSipLogDO> getLogRecordDao() {
    return logRecordDao;
  }


  @Override
  protected CounterDao getCounterDao() {
    return counterDao;
  }

  @Override
  protected String columnMapping(String columns) {
    if (StringUtils.equals(columns, "*")) {
      return columns;
    }

    Set<String> columnSets = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    columnSets.add("network_id");
    columnSets.add("service_id");

    CsvUtils.convertCSVToList(columns).forEach(item -> {
      columnSets.add(TextUtils.camelToUnderLine(item));
    });

    return CsvUtils.convertCollectionToCSV(columnSets);
  }

  @Override
  protected ProtocolSipLogVO convertLogDO2LogVO(ProtocolSipLogDO logDO) {
    ProtocolSipLogVO protocolSipLogVO = new ProtocolSipLogVO();
    BeanUtils.copyProperties(logDO, protocolSipLogVO);

    return protocolSipLogVO;
  }

  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolSipLogDO> logDOList,
      String columns) {
    Map<String, String> policyLevelDict = dictManager.getBaseDict()
        .getItemMap("appliance_collect_policy_level");
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
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    for (ProtocolSipLogDO protocolSipLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolSipLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "from":
            value = protocolSipLogDO.getFrom();
            break;
          case "to":
            value = protocolSipLogDO.getTo();
            break;
          case "ipProtocol":
            value = protocolSipLogDO.getIpProtocol();
            break;
          case "type":
            value = protocolSipLogDO.getType();
            break;
          case "seqNum":
            value = String.valueOf(protocolSipLogDO.getSeqNum());
            break;
          case "callId":
            value = protocolSipLogDO.getCallId();
            break;
          case "requestUri":
            value = protocolSipLogDO.getRequestUri();
            break;
          case "statusCode":
            value = protocolSipLogDO.getStatusCode();
            break;
          case "sdp":
            value = String.valueOf(protocolSipLogDO.getSdp());
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
