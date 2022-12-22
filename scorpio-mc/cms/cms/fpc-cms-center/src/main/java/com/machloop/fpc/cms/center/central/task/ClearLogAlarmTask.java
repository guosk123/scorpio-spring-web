package com.machloop.fpc.cms.center.central.task;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.dao.LogDao;

@Component
public class ClearLogAlarmTask {

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private LogDao logDao;

  @Scheduled(cron = "${task.clear.log.alarm.cron}")
  public void run() {
    Date expirationTime = DateUtils.beforeDayDate(DateUtils.now(), 3);
    String nodeId = HotPropertiesHelper.getProperty("fpc.cms.node.id");

    // 清除3天前的下级设备日志
    logDao.deleteLogs(expirationTime, nodeId);

    // 清除3天前的下级设备告警
    alarmDao.deleteAlarms(expirationTime, nodeId);
  }

}
