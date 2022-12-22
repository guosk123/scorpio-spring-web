package com.machloop.fpc.manager.system.service.impl;

import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.bo.SystemServerIpBO;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.alpha.webapp.system.data.AlarmDO;
import com.machloop.alpha.webapp.system.data.LogDO;
import com.machloop.alpha.webapp.system.service.MailService;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.alpha.webapp.system.service.impl.AbstractMailSendServiceImpl;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.alpha.webapp.system.vo.LogQueryVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.manager.appliance.dao.AlertMessageDao;
import com.machloop.fpc.manager.appliance.data.AlertMessageDO;
import com.machloop.fpc.manager.appliance.vo.AlertMessageQueryVO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author minjiajun
 *
 * create at 2022年8月12日, fpc-manager
 */
@Service
public class MailServiceImpl extends AbstractMailSendServiceImpl implements MailService {

  @Autowired
  private DictManager dictManager;

  @Autowired
  private LogDao logDao;

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private SystemServerIpService systemServerIpService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private AlertMessageDao alertMessageDao;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private ServiceService serviceService;

  private String serverIp;
  private String hostName;

  private Map<String, String> componentDict;
  private Map<String, String> logCategotyDict;
  private Map<String, String> alarmCategotyDict;

  @PostConstruct
  public void initial() {
    componentDict = dictManager.getBaseDict().getItemMap(Constants.DICT_SYSTEM_COMPONENT);
    logCategotyDict = dictManager.getBaseDict().getItemMap("system_log_category");
    alarmCategotyDict = dictManager.getBaseDict().getItemMap("alarm_category_001001");
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getLogDao()
   */
  @Override
  protected LogDao getLogDao() {
    return logDao;
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getAlarmDao()
   */
  @Override
  protected AlarmDao getAlarmDao() {
    return alarmDao;
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getComponentDict()
   */
  @Override
  protected Map<String, String> getComponentDict() {
    return componentDict;
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getLogCategotyDict()
   */
  @Override
  protected Map<String, String> getLogCategotyDict() {
    return logCategotyDict;
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getAlarmCategotyDict()
   */
  @Override
  protected Map<String, String> getAlarmCategotyDict() {
    return alarmCategotyDict;
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getServerIP()
   */
  @Override
  protected String getServerIP() {
    if (StringUtils.isBlank(serverIp)) {
      SystemServerIpBO systemServerIp = systemServerIpService.getSystemServerIp();
      serverIp = StringUtils.isNotBlank(systemServerIp.getIpv4Address())
          ? StringUtils.substringBefore(systemServerIp.getIpv4Address(), "/")
          : StringUtils.substringBefore(systemServerIp.getIpv6Address(), "/");
    }
    return serverIp;
  }

  @Override
  protected String getHostName() {
    if (StringUtils.isBlank(hostName)) {
      hostName = globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME);
    }
    return hostName;
  }

  @Override
  public String collectSystemMessage(Date startTime, Date endTime, String systemAlarmContent,
      String systemLogContent) {
    StringBuilder result = new StringBuilder();
    StringBuilder alarmContent = getSystemAlarmContent(systemAlarmContent, startTime, endTime);
    StringBuilder logContent = getSystemLogContent(systemLogContent, startTime, endTime);
    if (StringUtils.isNoneBlank(alarmContent, logContent)) {
      result.append("系统告警内容").append("<br>").append(alarmContent).append("<br>").append("<br>");
      result.append("系统日志内容").append("<br>").append(logContent).append("<br>").append("<br>");
    }
    return result.toString();
  }

  private StringBuilder getSystemLogContent(String systemLogContent, Date startTime, Date endTime) {

    LogQueryVO logQueryVO = new LogQueryVO();
    logQueryVO.setCreateTimeBegin(DateUtils.toStringISO8601(startTime));
    logQueryVO.setCreateTimeEnd(DateUtils.toStringISO8601(endTime));
    List<LogDO> systemLogList = getLogDao().queryLogsWithoutPage(logQueryVO);

    List<String> logContentIndex = CsvUtils.convertCSVToList(systemLogContent);

    StringBuilder result = new StringBuilder();
    if (CollectionUtils.isNotEmpty(systemLogList)) {
      for (LogDO systemLog : systemLogList) {
        result.append(logDO2String(systemLog, logContentIndex)).append("<br>");
      }
    }
    return result;
  }

  private StringBuilder getSystemAlarmContent(String systemAlarmContent, Date startTime,
      Date endTime) {

    AlarmQueryVO alarmQuery = new AlarmQueryVO();
    alarmQuery.setCreateTimeBegin(DateUtils.toStringISO8601(startTime));
    alarmQuery.setCreateTimeEnd(DateUtils.toStringISO8601(endTime));
    List<AlarmDO> systemAlarmList = getAlarmDao().queryAlarmsWithoutPage(alarmQuery);

    List<String> alarmContentIndex = CsvUtils.convertCSVToList(systemAlarmContent);

    StringBuilder result = new StringBuilder();
    if (CollectionUtils.isNotEmpty(systemAlarmList)) {
      for (AlarmDO systemAlarm : systemAlarmList) {
        result.append(alarmDO2String(systemAlarm, alarmContentIndex)).append("<br>");
      }
    }
    return result;
  }


  /**
   * @see com.machloop.alpha.webapp.system.service.MailService#collectAlertMessage(java.util.Date, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public String collectAlertMessage(Date startTime, Date endTime, String networkAlertContent,
      String serviceAlertContent) {
    StringBuilder result = new StringBuilder();

    result.append("业务告警消息").append("<br>")
        .append(getAlertContent(serviceAlertContent, startTime, endTime)).append("<br>")
        .append("<br>");

    result.append("网络告警消息").append("<br>")
        .append(getAlertContent(networkAlertContent, startTime, endTime)).append("<br>")
        .append("<br>");
    return result.toString();
  }

  private StringBuilder getAlertContent(String alertContentIndex, Date startTime, Date endTime) {
    AlertMessageQueryVO queryVO = new AlertMessageQueryVO();
    queryVO
        .setStartTime(DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    queryVO.setEndTime(DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    List<AlertMessageDO> alertContentList = alertMessageDao.queryAlertMessages(queryVO);

    StringBuilder result = new StringBuilder();

    if (StringUtils.isNotBlank(alertContentIndex)) {
      List<String> alertContentIndexList = CsvUtils.convertCSVToList(alertContentIndex);

      for (AlertMessageDO alertContent : alertContentList) {
        result.append(alertContent2String(alertContent, alertContentIndexList)).append("<br>");
      }
    }
    return result;
  }

  private String alertContent2String(AlertMessageDO alertContent, List<String> alertContentList) {
    StringBuilder content = new StringBuilder();

    Map<String, String> categoryDict = dictManager.getBaseDict()
        .getItemMap("appliance_alert_rule_category");
    Map<String, String> levelDict = dictManager.getBaseDict().getItemMap("system_alarm_level");
    if (StringUtils.isNotBlank(alertContent.getAriseTime())) {
      content.append("发生时间：").append(alertContent.getAriseTime()).append("；");
    }
    if (alertContentList.contains("network_name")) {
      NetworkBO sensorNetwork = networkService.queryNetwork(alertContent.getNetworkId());
      content.append("网络名称：").append(sensorNetwork.getName()).append("；");
    }
    if (alertContentList.contains("service_name")) {
      ServiceBO service = serviceService.queryService(alertContent.getServiceId());
      content.append("业务名称：").append(service.getName()).append("；");
    }
    if (alertContentList.contains("alert_content")) {
      content.append("告警详情：").append(component2content(alertContent)).append("；");
    }
    if (StringUtils.isNotBlank(alertContent.getName())) {
      content.append("告警名称：").append(alertContent.getName()).append("；");
    }
    if (StringUtils.isNotBlank(alertContent.getCategory())) {
      content.append("告警分类：").append(MapUtils.getString(categoryDict, alertContent.getCategory()))
          .append("；");
    }
    if (StringUtils.isNotBlank(alertContent.getLevel())) {
      content.append("告警级别：").append(MapUtils.getString(levelDict, alertContent.getLevel()))
          .append("；");
    }
    return content.toString();
  }

  @SuppressWarnings("unchecked")
  private String component2content(AlertMessageDO alertContent) {

    StringBuilder result = new StringBuilder();

    Map<String, String> alertMetricDict = dictManager.getBaseDict()
        .getItemMap("appliance_alert_rule_metric");
    Map<String, String> calculationDict = dictManager.getBaseDict()
        .getItemMap("appliance_alert_rule_calculation_type");

    List<Map<String, Object>> componentList = JsonHelper.deserialize(alertContent.getComponents(),
        new TypeReference<List<Map<String, Object>>>() {
        }, false);
    StringBuilder advancedContent = new StringBuilder();
    for (int i = 0; i < componentList.size(); i++) {
      StringBuilder content = new StringBuilder();

      Map<String,
          Object> alertFireContext = MapUtils.getMap(componentList.get(i), "alertFireContext");
      Map<String, Object> alertDefine = MapUtils.getMap(componentList.get(i), "alertDefine");
      String category = MapUtils.getString(alertDefine, "category");
      String name = MapUtils.getString(alertDefine, "name");

      String windowStartTime = DateUtils.toStringFormat(
          new Date(MapUtils.getLongValue(alertFireContext, "windowStartTime")),
          "yyyy-MM-dd HH:mm:ss");
      String windowEndTime = DateUtils.toStringFormat(
          new Date(MapUtils.getLongValue(alertFireContext, "windowEndTime")),
          "yyyy-MM-dd HH:mm:ss");

      Map<String, Object> thresholdSettings = MapUtils.getMap(alertDefine, "thresholdSettings");

      Map<String, Object> metrics = MapUtils.getMap(thresholdSettings, "metrics");
      Map<String, Object> fireCriteria = MapUtils.getMap(thresholdSettings, "fireCriteria");
      // 分子指标
      String numerator = MapUtils.getString(MapUtils.getMap(metrics, "numerator"), "metric");
      String numeratorText = MapUtils.getString(alertMetricDict, numerator, "");
      // 分母指标
      String denominator = MapUtils.getString(MapUtils.getMap(metrics, "denominator"), "metric");
      String denominatorText = MapUtils.getString(alertMetricDict, denominator, "");
      // 计算指标
      String calculation = MapUtils.getString(fireCriteria, "calculation");
      String calculationText = MapUtils.getString(calculationDict, calculation);
      // 阈值计算结果
      String thresholdResult = MapUtils.getString(alertFireContext, "thresholdResult");
      // 基线计算结果
      int trendResult = MapUtils.getIntValue(alertFireContext, "trendResult");
      // 基线计算百分比
      double trendPercent = MapUtils.getDoubleValue(alertFireContext, "trendPercent");
      // 基线值
      int trendBaseline = MapUtils.getIntValue(alertFireContext, "trendBaseline");

      if (StringUtils.equals(category, FpcCmsConstants.ALERT_CATEGORY_THRESHOLD)) {
        content.append("在").append(windowStartTime).append("~").append(windowEndTime).append("内，");
        if (MapUtils.getBooleanValue(metrics, "isRatio")) {
          content.append(numeratorText).append("指标与").append(denominatorText)
              .append(calculationText).append("比率为：").append(thresholdResult);
        } else {
          content.append(numeratorText).append("指标数据").append(calculationText).append("为：")
              .append(thresholdResult);
        }
        if (StringUtils.equals(alertContent.getCategory(),
            FpcCmsConstants.ALERT_CATEGORY_ADVANCED)) {
          advancedContent.append("子告警").append("：").append(name).append("，告警类型：").append(category)
              .append("，告警详情：").append(content);
        }
      }
      if (StringUtils.equals(category, FpcCmsConstants.ALERT_CATEGORY_TREND)) {
        content.append("在").append(windowStartTime).append("~").append(windowEndTime).append("内，");
        if (MapUtils.getBooleanValue(metrics, "isRatio")) {
          content.append(numeratorText).append("指标与").append(denominatorText)
              .append(calculationText).append("比率的：");
        } else {
          content.append(numeratorText).append("指标数据").append(calculation).append("的：");
        }
        content.append("基线值为：").append(trendBaseline).append("，实际值").append(calculationText)
            .append(trendResult);
        if (StringUtils.isBlank(trendPercent + "")) {
          content.append("，趋势百分比为：").append(trendPercent);
        }
        if (StringUtils.equals(alertContent.getCategory(),
            FpcCmsConstants.ALERT_CATEGORY_ADVANCED)) {
          advancedContent.append("子告警").append("：").append(name).append("，告警类型：").append(category)
              .append("，告警详情：").append(content);
        }
      }
      result.append(content).append(advancedContent);
    }

    return result.toString();
  }

}
