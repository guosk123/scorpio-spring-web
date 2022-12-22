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
import com.machloop.fpc.cms.center.metadata.data.ProtocolTdsLogDO;
import com.machloop.fpc.cms.center.metadata.service.LogRecordService;
import com.machloop.fpc.cms.center.metadata.vo.ProtocolTdsLogVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;

/**
 * @author guosk
 * <p>
 * create at 2020年12月11日, fpc-manager
 */
@Service("protocolTdsLogService")
public class ProtocolTdsLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolTdsLogVO, ProtocolTdsLogDO>
    implements LogRecordService<ProtocolTdsLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("cmd", "sql命令");
    fields.put("error", "错误信息");
    fields.put("delaytime", "响应时间");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolTdsLogDao")
  private LogRecordDao<ProtocolTdsLogDO> logRecordDao;

  @Autowired
  private CounterDao counterDao;

  @Autowired
  private SensorNetworkService networkService;

  @Autowired
  private SensorLogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;

  /**
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#getLogRecordDao()
   */
  @Override
  protected LogRecordDao<ProtocolTdsLogDO> getLogRecordDao() {
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
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDO2LogVO(com.machloop.fpc.cms.center.metadata.data.AbstractLogRecordDO)
   */
  @Override
  protected ProtocolTdsLogVO convertLogDO2LogVO(ProtocolTdsLogDO logDO) {
    ProtocolTdsLogVO protocolTdsLogVO = new ProtocolTdsLogVO();
    BeanUtils.copyProperties(logDO, protocolTdsLogVO);
    return protocolTdsLogVO;
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolTdsLogDO> logDOList,
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
    for (ProtocolTdsLogDO protocolTdsLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolTdsLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "cmd":
            value = protocolTdsLogDO.getCmd();
            break;
          case "error":
            value = protocolTdsLogDO.getError();
            break;
          case "delaytime":
            value = String.valueOf(protocolTdsLogDO.getDelaytime());
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
