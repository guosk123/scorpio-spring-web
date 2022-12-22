package com.machloop.fpc.manager.system.service.impl;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.bo.SystemServerIpBO;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;
import com.machloop.alpha.webapp.system.service.SyslogService;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl;

/**
 * @author guosk
 *
 * create at 2021年2月3日, fpc-manager
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
   * @see com.machloop.alpha.webapp.system.service.impl.AbstractSyslogServiceImpl#getHostName()
   */
  @Override
  protected String getHostName() {
    if (StringUtils.isBlank(hostName)) {
      hostName = globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME);
    }
    return hostName;
  }

}
