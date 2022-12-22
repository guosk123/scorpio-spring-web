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
import com.machloop.fpc.manager.metadata.data.ProtocolMailLogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolMailLogVO;
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
@Service("protocolMailLogService")
public class ProtocolMailLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolMailLogVO, ProtocolMailLogDO>
    implements LogRecordService<ProtocolMailLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("mailAddress", "用户名");
    fields.put("messageId", "邮件ID");
    fields.put("protocol", "协议");
    fields.put("date", "发送日期");
    fields.put("subject", "邮件主题");
    fields.put("from", "发件人");
    fields.put("to", "收件人");
    fields.put("cc", "抄送");
    fields.put("bcc", "密送");
    fields.put("decrypted", "加密方式");
    fields.put("attachment", "附件名称");
    fields.put("urlList", "正文内链接");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolMailLogDao")
  private LogRecordDao<ProtocolMailLogDO> logRecordDao;

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
  protected LogRecordDao<ProtocolMailLogDO> getLogRecordDao() {
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
  protected ProtocolMailLogVO convertLogDO2LogVO(ProtocolMailLogDO logDO) {
    ProtocolMailLogVO protocolMailLogVO = new ProtocolMailLogVO();
    BeanUtils.copyProperties(logDO, protocolMailLogVO);

    return protocolMailLogVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolMailLogDO> logDOList,
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
    for (ProtocolMailLogDO protocolMailLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolMailLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "mailAddress":
            value = protocolMailLogDO.getMailAddress();
            break;
          case "messageId":
            value = protocolMailLogDO.getMessageId();
            break;
          case "protocol":
            value = protocolMailLogDO.getProtocol();
            break;
          case "date":
            value = protocolMailLogDO.getDate();
            break;
          case "subject":
            value = protocolMailLogDO.getSubject();
            break;
          case "from":
            value = protocolMailLogDO.getFrom();
            break;
          case "to":
            value = protocolMailLogDO.getTo();
            break;
          case "cc":
            value = protocolMailLogDO.getCc();
            break;
          case "bcc":
            value = protocolMailLogDO.getBcc();
            break;
          case "decrypted":
            value = StringUtils.equals(protocolMailLogDO.getDecrypted(), "0") ? "明文" : "密文";
            break;
          case "attachment":
            value = protocolMailLogDO.getAttachment();
            break;
          case "urlList":
            value = StringUtils.join(protocolMailLogDO.getUrlList(), "|");
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
