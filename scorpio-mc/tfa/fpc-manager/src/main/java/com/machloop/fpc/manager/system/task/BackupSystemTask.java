package com.machloop.fpc.manager.system.task;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.service.BackupService;

@Component
public class BackupSystemTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackupSystemTask.class);

  private static final String ALARM_KEY_BACKUP = "backup_failed";

  @Value("${spring.datasource.url}")
  private String postgreUrl;

  @Value("${spring.datasource.username}")
  private String postgreUsername;

  @Value("${spring.datasource.password}")
  private String postgrePassword;

  @Value("${file.system.backup.path}")
  private String backupBaseFilePath;

  @Value("${file.system.backup.pgdump.path}")
  private String pgdumpFilePath;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private BackupService backupService;

  @Scheduled(cron = "${task.system.backup.schedule.cron}")
  public void run() {

    LOGGER.info("start execute system backup task.");

    // 系统默认部署在linux
    if (!SystemUtils.IS_OS_LINUX) {
      LOGGER.info("finish system backup because os is not linux.");
      return;
    }

    // 获取开关状态
    boolean isClose = StringUtils.equals(Constants.BOOL_NO,
        globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_BACKUP_SETTING_STATE));
    if (isClose) {
      LOGGER.info("finish system backup because system backup state is closed.");
      return;
    }

    // 获取备份限制文件个数
    int limitFileNum = 0;
    try {
      limitFileNum = Integer.parseInt(
          globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_BACKUP_SETTING_FILENUM));
    } catch (NumberFormatException e) {
      LOGGER.warn("finish system backup because system backup filenum is empty.");
      return;
    }

    // 拼接日备份目录路径
    Path path = Paths.get(backupBaseFilePath, "backup_"
        + DateUtils.toStringFormat(DateUtils.beforeDayDate(DateUtils.now(), 1), "yyyyMMdd"));

    // 执行pg_dump
    int postgreAttempts = 0;
    boolean postgreBackupStatus = false;

    while (postgreAttempts < WebappConstants.SYSTEM_BACKUP_RETRY_TIMES) {
      postgreBackupStatus = backupService.backupPostgreData(path, pgdumpFilePath, postgreUrl,
          postgreUsername, postgrePassword);

      if (postgreBackupStatus) {
        // 打印成功日志
        backupService.printLog("", LogHelper.LEVEL_NOTICE, "系统备份成功。");
        break;
      }
      // 打印失败日志
      backupService.printLog("", LogHelper.LEVEL_WARN, "系统备份失败。");

      // 打印失败告警
      backupService.printAlarm("", ALARM_KEY_BACKUP, "系统备份失败。");

      postgreAttempts++;
    }

    if (!postgreBackupStatus) {
      // 备份任务执行失败
      FileUtils.deleteQuietly(path.toFile());
      return;
    }

    // 打包压缩今日备份文件
    backupService.compressBackupFolder(path);

    // 只保留最近N个备份文件，删除最久的文件
    backupService.clearHisBackup(backupBaseFilePath, limitFileNum);

    LOGGER.info("end execute system backup task.");
  }

}
