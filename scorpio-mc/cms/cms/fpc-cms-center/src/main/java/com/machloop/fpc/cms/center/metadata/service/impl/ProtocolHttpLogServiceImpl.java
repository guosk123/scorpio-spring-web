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
import com.machloop.fpc.cms.center.appliance.dao.HostGroupDao;
import com.machloop.fpc.cms.center.appliance.data.HostGroupDO;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.global.dao.CounterDao;
import com.machloop.fpc.cms.center.metadata.dao.LogRecordDao;
import com.machloop.fpc.cms.center.metadata.data.ProtocolHttpLogDO;
import com.machloop.fpc.cms.center.metadata.service.LogRecordService;
import com.machloop.fpc.cms.center.metadata.vo.ProtocolHttpLogVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorLogicalSubnetBO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.service.SensorLogicalSubnetService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;

@Service("protocolHttpLogService")
public class ProtocolHttpLogServiceImpl
    extends AbstractLogRecordServiceImpl<ProtocolHttpLogVO, ProtocolHttpLogDO>
    implements LogRecordService<ProtocolHttpLogVO> {

  public static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.putAll(kpiFields);
    fields.put("method", "请求方法");
    fields.put("host", "host");
    fields.put("uri", "URL");
    fields.put("origin", "origin");
    fields.put("cookie", "cookie");
    fields.put("userAgent", "User-Agent");
    fields.put("referer", "referer");
    fields.put("xff", "xff");
    fields.put("status", "status");
    fields.put("setCookie", "set_cookie");
    fields.put("contentType", "Content-Type");
    fields.put("acceptLanguage", "Accept-language");
    fields.put("acceptEncoding", "Accept-Encoding");
    fields.put("requestHeader", "HTTP请求头");
    fields.put("requestBody", "HTTP请求内容");
    fields.put("responseHeader", "HTTP响应头");
    fields.put("responseBody", "HTTP响应内容");
    fields.put("fileName", "传输文件名称");
    fields.put("fileType", "传输文件类型");
    fields.put("fileFlag", "文件上传下载标识");
    fields.put("authorization", "Authorizaiton");
    fields.put("authType", "认证方式");
    fields.put("osVersion", "操作系统");
    fields.put("location", "重定向地址");
    fields.put("decrypted", "加密方式");
    fields.put("channelState", "代理连接状态");
    fields.put("xffFirst", "头部xff");
    fields.put("xffLast", "尾部xff");
    fields.put("xffFirstAlias", "头部xff-地址组");
    fields.put("xffLastAlias", "尾部xff-地址组");
  }

  @Autowired
  private DictManager dictManager;

  @Autowired
  @Qualifier("protocolHttpLogDao")
  private LogRecordDao<ProtocolHttpLogDO> logRecordDao;

  @Autowired
  private CounterDao counterDao;

  @Autowired
  private HostGroupDao hostGroupDao;

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
  protected LogRecordDao<ProtocolHttpLogDO> getLogRecordDao() {
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
  protected ProtocolHttpLogVO convertLogDO2LogVO(ProtocolHttpLogDO logDO) {
    ProtocolHttpLogVO protocolHttpLogVO = new ProtocolHttpLogVO();
    BeanUtils.copyProperties(logDO, protocolHttpLogVO);

    return protocolHttpLogVO;
  }

  /**
   * @see com.machloop.fpc.cms.center.metadata.service.impl.AbstractLogRecordServiceImpl#convertLogDOList2LineList(java.util.List, java.lang.String)
   */
  @Override
  protected List<List<String>> convertLogDOList2LineList(List<ProtocolHttpLogDO> logDOList,
      String columns) {
    Map<String, String> policyLevelDict = dictManager.getBaseDict()
        .getItemMap("appliance_collect_policy_level");
    Map<String,
        String> authTypeDict = dictManager.getBaseDict().getItemMap("protocol_http_auth_type");
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

    Map<String, String> hostGroupIdAndName = hostGroupDao.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupDO::getId, HostGroupDO::getName));

    // content
    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Entry::getValue, Entry::getKey));
    for (ProtocolHttpLogDO protocolHttpLogDO : logDOList) {
      List<String> values = titles.stream().map(title -> {
        String field = columnNameMap.get(title);

        String value = getFieldValue(protocolHttpLogDO, field, policyLevelDict, networkDict,
            serviceDict);
        if (StringUtils.isNotBlank(value)) {
          return value;
        }

        switch (field) {
          case "method":
            value = protocolHttpLogDO.getMethod();
            break;
          case "host":
            value = protocolHttpLogDO.getHost();
            break;
          case "uri":
            value = protocolHttpLogDO.getUri();
            break;
          case "origin":
            value = protocolHttpLogDO.getOrigin();
            break;
          case "cookie":
            value = protocolHttpLogDO.getCookie();
            break;
          case "userAgent":
            value = protocolHttpLogDO.getUserAgent();
            break;
          case "referer":
            value = protocolHttpLogDO.getReferer();
            break;
          case "xff":
            value = protocolHttpLogDO.getXff();
            break;
          case "status":
            value = protocolHttpLogDO.getStatus();
            break;
          case "setCookie":
            value = protocolHttpLogDO.getSetCookie();
            break;
          case "contentType":
            value = protocolHttpLogDO.getContentType();
            break;
          case "acceptLanguage":
            value = protocolHttpLogDO.getAcceptLanguage();
            break;
          case "acceptEncoding":
            value = protocolHttpLogDO.getAcceptEncoding();
            break;

          case "requestHeader":
            value = protocolHttpLogDO.getRequestHeader();
            break;
          case "requestBody":
            value = protocolHttpLogDO.getRequestBody();
            break;
          case "responseHeader":
            value = protocolHttpLogDO.getResponseHeader();
            break;
          case "responseBody":
            value = protocolHttpLogDO.getResponseBody();
            break;
          case "fileName":
            value = protocolHttpLogDO.getFileName();
            break;
          case "fileType":
            value = protocolHttpLogDO.getFileType();
            break;
          case "fileFlag":
            value = protocolHttpLogDO.getFileFlag();
            break;
          case "authorization":
            value = protocolHttpLogDO.getAuthorization();
            break;
          case "authType":
            value = authTypeDict.getOrDefault(protocolHttpLogDO.getAuthType(), "");
            break;
          case "osVersion":
            value = protocolHttpLogDO.getOsVersion();
            break;
          case "location":
            value = protocolHttpLogDO.getLocation();
            break;
          case "decrypted":
            value = StringUtils.equals(protocolHttpLogDO.getDecrypted(), "0") ? "明文" : "密文";
            break;
          case "xffFirst":
            value = protocolHttpLogDO.getXffFirst();
            break;
          case "xffLast":
            value = protocolHttpLogDO.getXffLast();
            break;
          case "xffFirstAlias":
            value = StringUtils.isNotBlank(protocolHttpLogDO.getXffFirstAlias())
                ? hostGroupIdAndName.get(protocolHttpLogDO.getXffFirstAlias())
                : "";
            break;
          case "xffLastAlias":
            value = StringUtils.isNotBlank(protocolHttpLogDO.getXffLastAlias())
                ? hostGroupIdAndName.get(protocolHttpLogDO.getXffLastAlias())
                : "";
            break;
          case "channelState":
            Integer channelState = protocolHttpLogDO.getChannelState();
            if (channelState == null) {
              value = "";
            } else {
              value = StringUtils.equals(String.valueOf(channelState), Constants.BOOL_YES) ? "成功"
                  : "失败";
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
