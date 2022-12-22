package com.machloop.fpc.cms.center.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cloudbees.syslog.Facility;
import com.cloudbees.syslog.Severity;
import com.cloudbees.syslog.SyslogMessage;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.bo.SystemServerIpBO;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.alpha.webapp.system.data.AlarmDO;
import com.machloop.alpha.webapp.system.data.LogDO;
import com.machloop.alpha.webapp.system.service.SyslogService;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.alpha.webapp.system.vo.LogQueryVO;

/**
 * @author guosk
 *
 * create at 2021年2月3日, fpc-cms-center
 */
@Service
public class SyslogServiceImpl extends AbstractSyslogServiceImpl implements SyslogService {

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

  private String serverIp;
  private String hostName;

  private Map<String, String> componentDict;
  private Map<String, String> logCategotyDict;
  private Map<String, String> alarmCategotyDict;

  private String systemLogPri = "";
  private String auditLogPri = "";
  private String alarmPri = "";

  @PostConstruct
  public void initial() {
    componentDict = dictManager.getBaseDict().getItemMap(Constants.DICT_SYSTEM_COMPONENT);
    logCategotyDict = dictManager.getBaseDict().getItemMap("system_log_category");
    alarmCategotyDict = dictManager.getBaseDict().getItemMap("alarm_category_001003");
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

  @Override
  public List<SyslogMessage> collectMessage(Date startTime, Date endTime, String facility,
      String severity, String separator, String systemAlarmContent, String systemLogContent) {
    List<SyslogMessage> messageList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    if (StringUtils.isNotBlank(systemLogContent)) {
      messageList.addAll(collectSystemLogForCms(startTime, endTime, facility, severity, separator,
          systemLogContent));
    }

    if (StringUtils.isNotBlank(systemAlarmContent)) {
      messageList.addAll(collectSystemAlarmForCms(startTime, endTime, facility, severity, separator,
          systemAlarmContent));
    }

    return messageList;
  }

  private List<SyslogMessage> collectSystemAlarmForCms(Date startTime, Date endTime,
      String facility, String severity, String separator, String systemAlarmContent) {
    List<SyslogMessage> result = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    AlarmQueryVO alarmQuery = new AlarmQueryVO();
    alarmQuery.setCreateTimeBegin(DateUtils.toStringISO8601(startTime));
    alarmQuery.setCreateTimeEnd(DateUtils.toStringISO8601(endTime));

    List<AlarmDO> systemAlarmList = getAlarmDao().queryAlarmsWithoutPage(alarmQuery);
    systemAlarmList = systemAlarmList.stream()
        .filter(logDO -> StringUtils.equals(logDO.getComponent(), "001003"))
        .collect(Collectors.toList());

    for (AlarmDO alarmDO : systemAlarmList) {

      String componentText = MapUtils.getString(getComponentDict(), alarmDO.getComponent());

      SyslogMessage message = new SyslogMessage().withHostname(getServerIP())
          .withAppName(componentText)
          .withFacility(Facility.fromNumericalCode(Integer.parseInt(facility)))
          .withSeverity(Severity.fromNumericalCode(Integer.parseInt(severity)))
          .withProcId(alarmDO.getComponent()).withTimestamp(convertUTC2CST(alarmDO.getAriseTime()))
          .withMsg(alarmDO2String(alarmDO, systemAlarmContent, separator));

      result.add(message);
    }
    return result;
  }

  private List<SyslogMessage> collectSystemLogForCms(Date startTime, Date endTime, String facility,
      String severity, String separator, String systemLogContent) {
    List<SyslogMessage> result = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    LogQueryVO logQuery = new LogQueryVO();
    logQuery.setCreateTimeBegin(DateUtils.toStringISO8601(startTime));
    logQuery.setCreateTimeEnd(DateUtils.toStringISO8601(endTime));

    List<LogDO> systemLogList = getLogDao().queryLogsWithoutPage(logQuery);
    systemLogList = systemLogList.stream()
        .filter(logDO -> StringUtils.equals(logDO.getComponent(), "001003"))
        .collect(Collectors.toList());

    for (LogDO logDO : systemLogList) {

      String componentText = MapUtils.getString(getComponentDict(), logDO.getComponent());

      SyslogMessage message = new SyslogMessage().withHostname(getServerIP())
          .withAppName(componentText)
          .withFacility(Facility.fromNumericalCode(Integer.parseInt(facility)))
          .withSeverity(Severity.fromNumericalCode(Integer.parseInt(severity)))
          .withProcId(logDO.getComponent())
          .withMsg(logDO2String(logDO, systemLogContent, separator))
          .withTimestamp(convertUTC2CST(logDO.getAriseTime()));

      result.add(message);
    }
    return result;
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getSystemLogPri()
   */
  @Override
  protected String getSystemLogPri() {
    return systemLogPri;
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getAuditLogPri()
   */
  @Override
  protected String getAuditLogPri() {
    return auditLogPri;
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getAlarmPri()
   */
  @Override
  protected String getAlarmPri() {
    return alarmPri;
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

}
