package com.machloop.fpc.cms.center.metadata.service.impl;

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
import com.machloop.fpc.cms.center.appliance.bo.ServiceBO;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.global.dao.CounterDao;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolSocks5LogDO;
import com.machloop.fpc.cms.center.metadata.service.LogRecordService;
import com.machloop.fpc.cms.center.metadata.vo.ProtocolSocks5LogVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
@Service("protocolSocks5LogService")
public class ProtocolSocks5LogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolSocks5LogVO, ProtocolSocks5LogDO>
    implements LogRecordService<ProtocolSocks5LogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("username", "用户名");
    fields.put("authMethod", "验证方式");
    fields.put("authResult", "验证结果");
    fields.put("cmd", "操作命令");
    fields.put("atyp", "地址类型");
    fields.put("bindAddr", "服务器绑定地址");
    fields.put("bindPort", "服务器绑定端口");
    fields.put("cmdResult", "命令执行结果");
    fields.put("channelState", "连接状态");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolSocks5LogDao")
  private LogRecordDao<ProtocolSocks5LogDO> logRecordDao;

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
  protected LogRecordDao<ProtocolSocks5LogDO> getLogRecordDao() {
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
  protected ProtocolSocks5LogVO convertLogDO2LogVO(ProtocolSocks5LogDO logDO) {
    ProtocolSocks5LogVO protocolSocks5LogVO = new ProtocolSocks5LogVO();
    BeanUtils.copyProperties(logDO, protocolSocks5LogVO);

    return protocolSocks5LogVO;
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolSocks5LogDO> logDOList,
      String columns) {
    Map<String, String> policyLevelDict = dictManager.getBaseDict()
        .getItemMap("appliance_collect_policy_level");
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
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    for (ProtocolSocks5LogDO protocolSocks5LogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolSocks5LogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "username":
            value = protocolSocks5LogDO.getUsername();
            break;
          case "authMethod":
            value = protocolSocks5LogDO.getAuthMethod();
            break;
          case "authResult":
            value = protocolSocks5LogDO.getAuthResult();
            break;
          case "cmd":
            value = protocolSocks5LogDO.getCmd();
            break;
          case "atyp":
            value = protocolSocks5LogDO.getAtyp();
            break;
          case "bindAddr":
            value = protocolSocks5LogDO.getBindAddr();
            break;
          case "bindPort":
            value = String.valueOf(protocolSocks5LogDO.getBindPort());
            break;
          case "cmdResult":
            value = protocolSocks5LogDO.getCmdResult();
            break;
          case "channelState":
            String channelState = String.valueOf(protocolSocks5LogDO.getChannelState());
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

}
