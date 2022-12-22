package com.machloop.fpc.manager.metadata.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.global.dao.CounterDao;
import com.machloop.fpc.manager.metadata.dao.LogRecordDao;
import com.machloop.fpc.manager.metadata.data.ProtocolDnsLogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolDnsLogVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author liyongjun
 *
 * create at 2019年9月24日, fpc-manager
 */
@Service("protocolDnsLogService")
public class ProtocolDnsLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolDnsLogVO, ProtocolDnsLogDO>
    implements LogRecordService<ProtocolDnsLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("answer", "DNS应答");
    fields.put("domain", "域名");
    fields.put("domainAddress", "域名解析地址");
    fields.put("dnsRcode", "DNS协议返回码");
    fields.put("dnsRcodeName", "DNS协议返回码名称");
    fields.put("dnsQueries", "DNS查询内容");
    fields.put("dnsType", "DNS类型");
    fields.put("subdomainCount", "子域名数量");
    fields.put("transactionId", "事务ID");
    fields.put("domainIntelligence", "DNS请求域名情报");
  }

  private static final int BLACKLIST = 0;
  private static final int WHITELIST = 1;
  private static final int UNKNOWN = 2;
  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolDnsLogDao")
  private LogRecordDao<ProtocolDnsLogDO> logRecordDao;

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
  protected LogRecordDao<ProtocolDnsLogDO> getLogRecordDao() {
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
  protected ProtocolDnsLogVO convertLogDO2LogVO(ProtocolDnsLogDO logDO) {
    ProtocolDnsLogVO protocolDnsLogVO = new ProtocolDnsLogVO();
    BeanUtils.copyProperties(logDO, protocolDnsLogVO);
    return protocolDnsLogVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolDnsLogDO> logDOList,
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
      Set<String> columnList = CsvUtils.convertCSVToSet(columns);
      if (columnList.contains("dnsQueries")) {
        columnList.add("dnsType");
      }

      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }
    lines.add(titles);

    // content
    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    for (ProtocolDnsLogDO protocolDnsLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolDnsLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "answer":
            value = protocolDnsLogDO.getAnswer();
            break;
          case "domain":
            value = protocolDnsLogDO.getDomain();
            break;
          case "domainAddress":
            value = JsonHelper.serialize(protocolDnsLogDO.getDomainAddress());
            break;
          case "domainIntelligence":
            switch (protocolDnsLogDO.getDomainIntelligence()) {
              case BLACKLIST:
                value = "黑名单";
                break;
              case WHITELIST:
                value = "白名单";
                break;
              case UNKNOWN:
                value = "未知";
                break;
            }
            break;
          case "dnsRcode":
            value = protocolDnsLogDO.getDnsRcode();
            break;
          case "dnsRcodeName":
            value = protocolDnsLogDO.getDnsRcodeName();
            break;
          case "dnsQueries":
            value = protocolDnsLogDO.getDnsQueries();
            break;
          case "dnsType":
            List<Map<String, Object>> dnsQueriesMapList = JsonHelper.deserialize(
                protocolDnsLogDO.getDnsQueries(), new TypeReference<List<Map<String, Object>>>() {
                });
            StringBuilder typeNameSb = new StringBuilder();
            for (Map<String, Object> dnsQueriesMap : dnsQueriesMapList) {
              typeNameSb.append(MapUtils.getString(dnsQueriesMap, "type_name", "")).append(" ");
            }
            if (typeNameSb.length() > 0) {
              typeNameSb.deleteCharAt(typeNameSb.length() - 1);
            }
            value = typeNameSb.toString();
            break;
          case "subdomainCount":
            value = String.valueOf(protocolDnsLogDO.getSubdomainCount());
            break;
          case "transactionId":
            value = protocolDnsLogDO.getTransactionId();
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
