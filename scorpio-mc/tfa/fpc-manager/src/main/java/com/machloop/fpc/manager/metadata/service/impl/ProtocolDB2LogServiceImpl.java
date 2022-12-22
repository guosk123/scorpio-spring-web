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
import com.machloop.fpc.manager.metadata.data.ProtocolDB2LogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolDB2LogVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author minjiajun
 *
 * create at 2022年8月19日, fpc-manager
 */
@Service("protocolDB2LogService")
public class ProtocolDB2LogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolDB2LogVO, ProtocolDB2LogDO>
    implements LogRecordService<ProtocolDB2LogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("codePoint", "DRDA用途");
    fields.put("data", "DRDA数据");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolDB2LogDao")
  private LogRecordDao<ProtocolDB2LogDO> logRecordDao;

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
  protected LogRecordDao<ProtocolDB2LogDO> getLogRecordDao() {
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
  protected ProtocolDB2LogVO convertLogDO2LogVO(ProtocolDB2LogDO logDO) {
    ProtocolDB2LogVO protocolDB2LogVO = new ProtocolDB2LogVO();
    BeanUtils.copyProperties(logDO, protocolDB2LogVO);

    return protocolDB2LogVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolDB2LogDO> logDOList,
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
    for (ProtocolDB2LogDO protocolDB2LogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolDB2LogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "codePoint":
            value = protocolDB2LogDO.getCodePoint();
            break;
          case "data":
            value = String.valueOf(protocolDB2LogDO.getData());
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
