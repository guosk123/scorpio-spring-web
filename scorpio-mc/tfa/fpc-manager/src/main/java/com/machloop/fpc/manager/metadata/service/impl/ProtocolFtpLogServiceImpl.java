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
import com.machloop.fpc.manager.metadata.data.ProtocolFtpLogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolFtpLogVO;
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
@Service("protocolFtpLogService")
public class ProtocolFtpLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolFtpLogVO, ProtocolFtpLogDO>
    implements LogRecordService<ProtocolFtpLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("user", "登录用户");
    fields.put("cmdSeq", "操作序号");
    fields.put("cmd", "操作命令");
    fields.put("reply", "操作结果");
    fields.put("filename", "文件名称");
    fields.put("dataChannelIp", "数据通道IP");
    fields.put("dataChannelPort", "数据通道端口");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolFtpLogDao")
  private LogRecordDao<ProtocolFtpLogDO> logRecordDao;

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
  protected LogRecordDao<ProtocolFtpLogDO> getLogRecordDao() {
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
  protected ProtocolFtpLogVO convertLogDO2LogVO(ProtocolFtpLogDO logDO) {
    ProtocolFtpLogVO protocolFtpLogVO = new ProtocolFtpLogVO();
    BeanUtils.copyProperties(logDO, protocolFtpLogVO);

    return protocolFtpLogVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolFtpLogDO> logDOList,
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
    for (ProtocolFtpLogDO protocolFtpLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolFtpLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "user":
            value = protocolFtpLogDO.getUser();
            break;
          case "cmdSeq":
            value = protocolFtpLogDO.getCmdSeq();
            break;
          case "cmd":
            value = protocolFtpLogDO.getCmd();
            break;
          case "reply":
            value = protocolFtpLogDO.getReply();
            break;
          case "filename":
            value = protocolFtpLogDO.getFilename();
            break;
          case "dataChannelIp":
            value = protocolFtpLogDO.getDataChannelIp();
            break;
          case "dataChannelPort":
            Integer port = protocolFtpLogDO.getDataChannelPort();
            value = port != null ? String.valueOf(port) : "";
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
