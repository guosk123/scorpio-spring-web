package com.machloop.fpc.manager.metadata.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.clickhouse.client.internal.google.common.collect.Sets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.manager.global.dao.CounterDao;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolSocks4LogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolSocks4LogVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author minjiajun
 *
 * create at 2022年5月30日, fpc-manager
 */
@Service("protocolSocks4LogService")
public class ProtocolSocks4LogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolSocks4LogVO, ProtocolSocks4LogDO>
    implements LogRecordService<ProtocolSocks4LogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("cmd", "操作命令");
    fields.put("requestRemotePort", "远端端口");
    fields.put("requestRemoteIp", "远端IP");
    fields.put("userId", "用户ID");
    fields.put("domainName", "远端域名");
    fields.put("cmdResult", "结果");
    fields.put("responseRemoteIp", "服务器返回的远端IP");
    fields.put("responseRemotePort", "服务器返回的远端端口");
    fields.put("channelState", "连接状态");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolSocks4LogDao")
  private LogRecordDao<ProtocolSocks4LogDO> logRecordDao;

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
  protected LogRecordDao<ProtocolSocks4LogDO> getLogRecordDao() {
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
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDO2LogVO(com.machloop.fpc.manager.metadata.data.AbstractLogRecordDO)
   */
  @Override
  protected ProtocolSocks4LogVO convertLogDO2LogVO(ProtocolSocks4LogDO logDO) {
    ProtocolSocks4LogVO protocolSocks4LogVO = new ProtocolSocks4LogVO();
    BeanUtils.copyProperties(logDO, protocolSocks4LogVO);

    return protocolSocks4LogVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolSocks4LogDO> logDOList,
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
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    for (ProtocolSocks4LogDO protocolSocks4LogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolSocks4LogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "cmd":
            value = protocolSocks4LogDO.getCmd();
            break;
          case "requestRemotePort":
            value = protocolSocks4LogDO.getRequestRemotePort();
            break;
          case "requestRemoteIp":
            value = protocolSocks4LogDO.getRequestRemoteIp();
            break;
          case "userId":
            value = protocolSocks4LogDO.getUserId();
            break;
          case "domainName":
            value = protocolSocks4LogDO.getDomainName();
            break;
          case "cmdResult":
            value = protocolSocks4LogDO.getCmdResult();
            break;
          case "responseRemoteIp":
            value = String.valueOf(protocolSocks4LogDO.getResponseRemoteIp());
            break;
          case "responseRemotePort":
            value = protocolSocks4LogDO.getResponseRemotePort();
            break;
          case "channelState":
            String channelState = String.valueOf(protocolSocks4LogDO.getChannelState());
            value = StringUtils.equals(channelState, Constants.BOOL_YES) ? "成功" : "失败";
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

}
