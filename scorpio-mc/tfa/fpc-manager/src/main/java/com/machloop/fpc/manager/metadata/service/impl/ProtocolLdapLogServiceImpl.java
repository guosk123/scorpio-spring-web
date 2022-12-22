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
import com.machloop.fpc.manager.metadata.data.ProtocolLdapLogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolLdapLogVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author chenshimiao
 * 
 * create at 2022/7/27 3:46 PM,cms
 * @version 1.0
 */
@Service("protocolLdapLogService")
public class ProtocolLdapLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolLdapLogVO, ProtocolLdapLogDO>
    implements LogRecordService<ProtocolLdapLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("opType", "请求类型");
    fields.put("resStatus", "回复状态");
    fields.put("reqContent", "请求内容");
    fields.put("resContent", "回复内容");
  }

  @Autowired
  @Qualifier("protocolLdapLogDao")
  private LogRecordDao<ProtocolLdapLogDO> logRecordDao;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private CounterDao counterDao;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private ServiceService serviceService;

  @Override
  protected LogRecordDao<ProtocolLdapLogDO> getLogRecordDao() {
    return logRecordDao;
  }

  @Override
  protected CounterDao getCounterDao() {
    return counterDao;
  }

  @Override
  protected ProtocolLdapLogVO convertLogDO2LogVO(ProtocolLdapLogDO logDO) {
    ProtocolLdapLogVO protocolLdapLogVO = new ProtocolLdapLogVO();
    BeanUtils.copyProperties(logDO, protocolLdapLogVO);

    return protocolLdapLogVO;
  }

  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolLdapLogDO> logDOList,
      String columns) {
    Map<String, String> policyLevelDict = dictManager.getBaseDict()
        .getItemMap("appliance_collect_policy_level");

    Map<String, String> opType = dictManager.getBaseDict().getItemMap("protocol_ldap_op_type");

    Map<String,
        String> resStatus = dictManager.getBaseDict().getItemMap("protocol_ldap_res_status");

    Map<String, String> networkDict = networkService.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkBO::getId, NetworkBO::getName));
    networkDict.putAll(logicalSubnetService.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetBO::getId, LogicalSubnetBO::getName)));
    Map<String, String> serviceDict = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getId, ServiceBO::getName));

    List<List<String>> lines = Lists.newArrayListWithCapacity(logDOList.size() + 1);

    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(columns, "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(columns);
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }
    lines.add(titles);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));

    for (ProtocolLdapLogDO protocolLdapLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolLdapLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "opType":

            value = StringUtils
                .isNotBlank(opType.get(String.valueOf(protocolLdapLogDO.getOpType())))
                    ? opType.get(String.valueOf(protocolLdapLogDO.getOpType()))
                    : "-";
            break;
          case "resStatus":
            value = StringUtils
                .isNotBlank(resStatus.get(String.valueOf(protocolLdapLogDO.getResStatus())))
                    ? resStatus.get(String.valueOf(protocolLdapLogDO.getResStatus()))
                    : "-";
            break;
          case "reqContent":
            value = protocolLdapLogDO.getReqContent().toString();
            break;
          case "resContent":
            value = protocolLdapLogDO.getResContent().toString();
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
