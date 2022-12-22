package com.machloop.fpc.manager.system.task;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.bo.ArchiveTimeBO;
import com.machloop.alpha.webapp.system.service.ArchiveService;

@Component
public class ArchiveLogAlarmTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveLogAlarmTask.class);

  @Value("${file.archive.log.path}")
  private String logArchivePath;

  @Value("${file.archive.alarm.path}")
  private String alarmArchivePath;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private ArchiveService archiveService;

  @Scheduled(cron = "${task.system.archive.schedule.cron}")
  public void run() {

    LOGGER.info("start execute log and alarm archive task.");

    // 获取开关状态
    boolean isClose = StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_ARCHIVE_SETTING_STATE));
    if (isClose) {
      LOGGER.info("finish log and alarm archive because state is closed.");
      return;
    }

    // 获取归档时间范围
    ArchiveTimeBO archiveTimeBO = archiveService
        .getArchiveTimeHorizon(WebappConstants.GLOBAL_SETTING_ARCHIVE_TIME);

    if (!archiveTimeBO.getResult()) {
      LOGGER.warn("finish log and alarm archive because get time horizon failed.");
      return;
    }

    // 创建归档根目录
    if (!archiveService.createRootDirectory(logArchivePath, alarmArchivePath)) {
      LOGGER.warn("finish log and alarm archive because create root directory failed.");
      return;
    }

    // 对日志和告警进行归档
    Map<String, Integer> result = archiveService.archiveData(archiveTimeBO.getBeginTime(),
        archiveTimeBO.getEndTime(), logArchivePath, alarmArchivePath, "");

    // 对文件夹进行压缩
    archiveService.compressArchiveFolder(WebappConstants.GLOBAL_SETTING_ARCHIVE_TIME,
        logArchivePath, alarmArchivePath);

    LOGGER.info(
        "end execute log and alarm archive task, this time is total archive {} log, {} alarm.",
        result.get("archiveLogCount"), result.get("archiveAlarmCount"));
  }

}
