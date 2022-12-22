package com.machloop.fpc.manager.metadata.service.impl;

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
import com.machloop.fpc.manager.global.dao.CounterDao;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolIcmpLogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolIcmpLogVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author guosk
 *
 * create at 2020年11月27日, fpc-manager
 */
@Service("protocolIcmpLogService")
public class ProtocolIcmpLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolIcmpLogVO, ProtocolIcmpLogDO>
    implements LogRecordService<ProtocolIcmpLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("version", "ICMP版本");
    fields.put("result", "详细信息");
    fields.put("requestDataLen", "请求数据长度");
    fields.put("responseDataLen", "应答数据长度");
    fields.put("onlyRequest", "只有请求");
    fields.put("onlyResponse", "只有应答");
    fields.put("payloadHashInconsistent", "请求应答payload是否一致");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolIcmpLogDao")
  private LogRecordDao<ProtocolIcmpLogDO> logRecordDao;

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
  protected LogRecordDao<ProtocolIcmpLogDO> getLogRecordDao() {
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
  protected ProtocolIcmpLogVO convertLogDO2LogVO(ProtocolIcmpLogDO logDO) {
    ProtocolIcmpLogVO protocolIcmpLogVO = new ProtocolIcmpLogVO();
    BeanUtils.copyProperties(logDO, protocolIcmpLogVO);

    return protocolIcmpLogVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolIcmpLogDO> logDOList,
      String columns) {
    Map<String, String> policyLevelDict = dictManager.getBaseDict()
        .getItemMap("appliance_collect_policy_level");
    Map<String, String> versionDict = dictManager.getBaseDict().getItemMap("protocol_icmp_version");
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
    for (ProtocolIcmpLogDO protocolIcmpLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolIcmpLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "version":
            value = MapUtils.getString(versionDict, String.valueOf(protocolIcmpLogDO.getVersion()),
                "");
            break;
          case "result":
            value = protocolIcmpLogDO.getResult();
            break;
          case "requestDataLen":
            value = String.valueOf(protocolIcmpLogDO.getRequestDataLen());
            break;
          case "responseDataLen":
            value = String.valueOf(protocolIcmpLogDO.getResponseDataLen());
            break;
          case "onlyRequest":
            String onlyRequest = String.valueOf(protocolIcmpLogDO.getOnlyRequest());
            value = StringUtils.equals(onlyRequest, Constants.BOOL_YES) ? "是" : "否";
            break;
          case "onlyResponse":
            String onlyResponse = String.valueOf(protocolIcmpLogDO.getOnlyResponse());
            value = StringUtils.equals(onlyResponse, Constants.BOOL_YES) ? "是" : "否";
            break;
          case "payloadHashInconsistent":
            Integer payloadHashInconsistent = protocolIcmpLogDO.getPayloadHashInconsistent();
            if (payloadHashInconsistent == null) {
              value = "";
            } else {
              value = StringUtils.equals(String.valueOf(payloadHashInconsistent),
                  Constants.BOOL_YES) ? "是" : "否";
            }
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
