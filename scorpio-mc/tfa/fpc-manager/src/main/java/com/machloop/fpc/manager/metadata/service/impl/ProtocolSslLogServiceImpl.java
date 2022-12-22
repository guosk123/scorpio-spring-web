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
import com.machloop.fpc.manager.metadata.data.ProtocolSslLogDO;
import com.machloop.fpc.manager.metadata.service.LogRecordService;
import com.machloop.fpc.manager.metadata.vo.ProtocolSslLogVO;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

@Service("protocolSslLogService")
public class ProtocolSslLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolSslLogVO, ProtocolSslLogDO>
    implements LogRecordService<ProtocolSslLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("serverName", "服务器名称");
    fields.put("serverCertsSha1", "服务器证书SHA1值");
    fields.put("ja3Client", "客户端指纹");
    fields.put("ja3Server", "服务端指纹");
    fields.put("version", "SSL版本");
    fields.put("cipherSuite", "SSL加密套件");
    fields.put("signatureAlgorithm", "证书签名算法");
    fields.put("issuer", "证书发布者");
    fields.put("issuerUrls", "发布者证书链接");
    fields.put("ocspUrls", "证书状态服务器");
    fields.put("crlUrls", "证书吊销列表链接");
    fields.put("commonName", "证书使用者");
    fields.put("validity", "证书有效期");
    fields.put("authType", "认证方式");
    fields.put("isReuse", "会话复用");
    fields.put("secProto", "DTLS标识");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolSslLogDao")
  private LogRecordDao<ProtocolSslLogDO> logRecordDao;

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
  protected LogRecordDao<ProtocolSslLogDO> getLogRecordDao() {
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
  protected ProtocolSslLogVO convertLogDO2LogVO(ProtocolSslLogDO logDO) {
    ProtocolSslLogVO protocolSslLogVO = new ProtocolSslLogVO();
    BeanUtils.copyProperties(logDO, protocolSslLogVO);

    return protocolSslLogVO;
  }

  /**
   * @see com.machloop.fpc.manager.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolSslLogDO> logDOList,
      String columns) {
    Map<String, String> policyLevelDict = dictManager.getBaseDict()
        .getItemMap("appliance_collect_policy_level");
    Map<String,
        String> authTypeDict = dictManager.getBaseDict().getItemMap("protocol_ssl_auth_type");
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
    for (ProtocolSslLogDO protocolSslLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolSslLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "serverName":
            value = protocolSslLogDO.getServerName();
            break;
          case "serverCertsSha1":
            value = StringUtils.join(protocolSslLogDO.getServerCertsSha1(), "|");
            break;
          case "ja3Client":
            value = protocolSslLogDO.getJa3Client();
            break;
          case "ja3Server":
            value = protocolSslLogDO.getJa3Server();
            break;
          case "version":
            value = protocolSslLogDO.getVersion();
            break;
          case "cipherSuite":
            value = protocolSslLogDO.getCipherSuite();
            break;
          case "signatureAlgorithm":
            value = protocolSslLogDO.getSignatureAlgorithm();
            break;
          case "issuer":
            value = protocolSslLogDO.getIssuer();
            break;
          case "issuerUrls":
            value = protocolSslLogDO.getIssuerUrls();
            break;
          case "ocspUrls":
            value = protocolSslLogDO.getOcspUrls();
            break;
          case "crlUrls":
            value = protocolSslLogDO.getCrlUrls();
            break;
          case "commonName":
            value = protocolSslLogDO.getCommonName();
            break;
          case "validity":
            value = protocolSslLogDO.getValidity();
            break;
          case "authType":
            value = authTypeDict.getOrDefault(protocolSslLogDO.getAuthType(), "");
            break;
          case "isReuse":
            value = protocolSslLogDO.getIsReuse() == 0 ? "否 " : "是";
            break;
          case "secProto":
            value = protocolSslLogDO.getSecProto() == 0 ? "false" : "true";
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
