package com.machloop.fpc.cms.center.system.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import com.machloop.fpc.cms.center.appliance.bo.ServiceBO;
import com.machloop.fpc.cms.center.appliance.dao.AlertMessageDao;
import com.machloop.fpc.cms.center.appliance.data.AlertMessageDO;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.appliance.vo.AlertMessageQueryVO;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2021年2月3日, fpc-cms-center
 */
@Service
public class MailServiceImpl extends AbstractMailSendServiceImpl implements MailService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MailServiceImpl.class);

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
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private ServiceService serviceService;

  private String serverIp;
  private String hostName;

  private Map<String, String> componentDict;
  private Map<String, String> logCategotyDict;
  private Map<String, String> alarmCategotyDict;
  private Map<String, String> alertCategotyDict;

  @PostConstruct
  public void initial() {
    componentDict = dictManager.getBaseDict().getItemMap(Constants.DICT_SYSTEM_COMPONENT);
    logCategotyDict = dictManager.getBaseDict().getItemMap("system_log_category");
    alarmCategotyDict = dictManager.getBaseDict().getItemMap("alarm_category_001003");
    alertCategotyDict = dictManager.getBaseDict().getItemMap("appliance_alert_rule_category");
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getLogDao()
   */
  @Override
  protected LogDao getLogDao() {
    return logDao;
  }

  @Override
  public String collectSystemMessage(Date startTime, Date endTime, String systemAlarmContent,
      String systemLogContent) {
    StringBuilder result = new StringBuilder();
    StringBuilder alarmContentForCms = getSystemAlarmContentForCms(systemAlarmContent, startTime,
        endTime);
    StringBuilder logContentForCms = getSystemLogContentForCms(systemLogContent, startTime,
        endTime);
    if (StringUtils.isNoneBlank(alarmContentForCms, logContentForCms)) {
      result.append("系统告警内容").append("<br>").append(alarmContentForCms).append("<br>")
          .append("<br>");
      result.append("系统日志内容").append("<br>").append(logContentForCms).append("<br>").append("<br>");
    }
    return result.toString();
  }

  private StringBuilder getSystemLogContentForCms(String systemLogContent, Date startTime,
      Date endTime) {
    LogQueryVO logQueryVO = new LogQueryVO();
    logQueryVO.setCreateTimeBegin(DateUtils.toStringISO8601(startTime));
    logQueryVO.setCreateTimeEnd(DateUtils.toStringISO8601(endTime));
    List<LogDO> systemLogList = getLogDao().queryLogsWithoutPage(logQueryVO);
    systemLogList = systemLogList.stream()
        .filter(logDO -> StringUtils.equals(logDO.getComponent(), "001003"))
        .collect(Collectors.toList());

    List<String> logContentIndex = CsvUtils.convertCSVToList(systemLogContent);

    StringBuilder result = new StringBuilder();
    if (CollectionUtils.isNotEmpty(systemLogList)) {
      for (LogDO systemLog : systemLogList) {
        result.append(logDO2String(systemLog, logContentIndex)).append("<br>");
      }
    }
    return result;
  }

  private StringBuilder getSystemAlarmContentForCms(String systemAlarmContent, Date startTime,
      Date endTime) {

    AlarmQueryVO alarmQuery = new AlarmQueryVO();
    alarmQuery.setCreateTimeBegin(DateUtils.toStringISO8601(startTime));
    alarmQuery.setCreateTimeEnd(DateUtils.toStringISO8601(endTime));
    List<AlarmDO> systemAlarmList = getAlarmDao().queryAlarmsWithoutPage(alarmQuery);
    systemAlarmList = systemAlarmList.stream()
        .filter(logDO -> StringUtils.equals(logDO.getComponent(), "001003"))
        .collect(Collectors.toList());

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

  public Map<String, String> getAlertCategotyDict() {
    return alertCategotyDict;
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

  /**
   * @see com.machloop.alpha.webapp.system.service.MailService#collectAlertMessage(java.util.Date, java.util.Date, java.lang.String, java.lang.String)
   */
  @Override
  public String collectAlertMessage(Date startTime, Date endTime, String networkAlertContent,
      String serviceAlertContent) {

    AlertMessageQueryVO queryVO = new AlertMessageQueryVO();
    queryVO
        .setStartTime(DateUtils.toStringFormat(startTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    queryVO.setEndTime(DateUtils.toStringFormat(endTime, "yyyy-MM-dd HH:mm:ss", ZoneOffset.UTC));
    List<AlertMessageDO> alertContentList = alertMessageDao.queryAlertMessages(queryVO);

    StringBuilder serviceResult = new StringBuilder();
    StringBuilder networkResult = new StringBuilder();

    List<String> networkAlertIndexList = CsvUtils.convertCSVToList(networkAlertContent);
    List<String> serviceAlertIndexList = CsvUtils.convertCSVToList(serviceAlertContent);

    for (AlertMessageDO alertContent : alertContentList) {
      if (StringUtils.isNotBlank(alertContent.getServiceId())) {
        serviceResult.append(alertContent2String(alertContent, serviceAlertIndexList))
            .append("<br>");
      }
      networkResult.append(alertContent2String(alertContent, networkAlertIndexList)).append("<br>");
    }

    StringBuilder result = new StringBuilder();
    result.append("业务告警消息").append("<br>").append(serviceResult).append("<br>").append("<br>");
    result.append("网络告警消息").append("<br>").append(networkResult).append("<br>").append("<br>");

    return result.toString();
  }


  private String alertContent2String(AlertMessageDO alertContent, List<String> alertContentList) {

    if (CollectionUtils.isEmpty(alertContentList)) {
      return "";
    }

    StringBuilder content = new StringBuilder();
    Map<String, String> categoryDict = dictManager.getBaseDict()
        .getItemMap("appliance_alert_rule_category");
    Map<String, String> levelDict = dictManager.getBaseDict().getItemMap("system_alarm_level");

    for (int i = 0; i < alertContentList.size(); i++) {
      String alertContentIndex = alertContentList.get(i);

      if (StringUtils.equals(alertContentIndex, "arise_time")) {
        content.append("触发时间：").append(convertUTC2CST(alertContent.getAriseTime())).append("；");
      }
      if (StringUtils.equals(alertContentIndex, "network_name")) {
        SensorNetworkDO sensorNetwork = sensorNetworkDao
            .querySensorNetworkByNetworkInSensorId(alertContent.getNetworkId());
        content.append("网络名称：")
            .append(StringUtils.isBlank(sensorNetwork.getName())
                ? sensorNetwork.getNetworkInSensorName()
                : sensorNetwork.getName())
            .append("；");
      }
      if (StringUtils.equals(alertContentIndex, "service_name")) {
        ServiceBO service = serviceService.queryService(alertContent.getServiceId());
        content.append("业务名称：").append(service.getName()).append("；");
      }
      if (StringUtils.equals(alertContentIndex, "alert_content")) {
        content.append("告警详情：").append(component2content(alertContent)).append("；");

      }
      if (StringUtils.equals(alertContentIndex, "name")) {
        content.append("告警名称：").append(alertContent.getName()).append("；");

      }
      if (StringUtils.equals(alertContentIndex, "category")) {
        content.append("告警分类：").append(MapUtils.getString(categoryDict, alertContent.getCategory()))
            .append("；");
      }
      if (StringUtils.equals(alertContentIndex, "level")) {
        content.append("告警级别：").append(MapUtils.getString(levelDict, alertContent.getLevel()))
            .append("；");
      }
    }
    return content.toString();
  }

  private String convertUTC2CST(String utcTime) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    Date date = null;
    try {
      date = sdf.parse(utcTime);
    } catch (ParseException e) {
      LOGGER.warn("convert utc time to cst time error.", e);
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 8);
    return DateUtils.toStringFormat(calendar.getTime(), "yyyy-MM-dd HH:mm:ss");
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
    for (Map<String, Object> component : componentList) {
      StringBuilder content = new StringBuilder();

      Map<String, Object> alertFireContext = MapUtils.getMap(component, "alertFireContext");
      Map<String, Object> alertDefine = MapUtils.getMap(component, "alertDefine");
      String category = MapUtils.getString(alertDefine, "category");
      String name = MapUtils.getString(alertDefine, "name");

      String windowStartTime = DateUtils.toStringFormat(
          new Date(MapUtils.getLongValue(alertFireContext, "windowStartTime")),
          "yyyy-MM-dd HH:mm:ss");
      String windowEndTime = DateUtils.toStringFormat(
          new Date(MapUtils.getLongValue(alertFireContext, "windowEndTime")),
          "yyyy-MM-dd HH:mm:ss");

      Map<String,
          Object> settings = MapUtils.getMap(alertDefine, "thresholdSettings") == null
              ? MapUtils.getMap(alertDefine, "trendSettings")
              : MapUtils.getMap(alertDefine, "thresholdSettings");

      Map<String, Object> metrics = MapUtils.getMap(settings, "metrics");
      Map<String, Object> fireCriteria = MapUtils.getMap(settings, "fireCriteria");
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
              .append(thresholdResult).append("。 ");
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
            .append("为：").append(trendResult);
        if (StringUtils.isBlank(trendPercent + "")) {
          content.append("，趋势百分比为：").append(trendPercent).append("。 ");
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
