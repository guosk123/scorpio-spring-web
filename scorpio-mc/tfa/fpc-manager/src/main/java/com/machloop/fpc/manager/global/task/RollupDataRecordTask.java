package com.machloop.fpc.manager.global.task;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.global.dao.DataRecordDaoCk;

@Component
public class RollupDataRecordTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(RollupDataRecordTask.class);

  // 聚合默认延迟时间
  private static final int DEFAULT_TASK_ROLLUP_DELAY_MS = Constants.ONE_MINUTE_SECONDS * 1000;

  @Value("${task.rollup.delay.ms}")
  private int delayMs;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private List<DataRecordDaoCk> dataRecordDaoList;

  @Scheduled(fixedRateString = "${task.rollup.schedule.fixedrate.ms}")
  public void run() {

    LocalDateTime localDateTime = LocalDateTime.now();

    // 分别对5分钟、1小时的统计进行Rollup
    doRollup(localDateTime, ManagerConstants.GLOBAL_SETTING_DR_ROLLUP_LATEST_5MIN,
        Constants.FIVE_MINUTE_SECONDS);

    // 等待10s，防止es未refresh完成导致刚完成的5min汇总无法查出
    try {
      Thread.sleep(Constants.FIVE_SECONDS * 2 * 1000);
    } catch (InterruptedException e) {
      LOGGER.info("rollup task sleep has been interrupt.");
      Thread.currentThread().interrupt();
    }

    doRollup(localDateTime, ManagerConstants.GLOBAL_SETTING_DR_ROLLUP_LATEST_1HOUR,
        Constants.ONE_HOUR_SECONDS);
  }

  /**
   * @param localDateTime
   * @param latestRollupSetting
   * @param intervalSecond
   */
  private void doRollup(LocalDateTime localDateTime, String latestRollupSetting,
      long intervalSecond) {
    LocalDateTime currentIntervalDateTime = computeCurrentIntervalDateTime(localDateTime,
        intervalSecond);
    Date thisTime = Date.from(currentIntervalDateTime.atZone(ZoneId.systemDefault()).toInstant());
    Date nowTime = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());

    // 延迟指定时间，最长不超过1分钟
    if (nowTime.getTime()
        - thisTime.getTime() < (delayMs < 0 || delayMs > Constants.ONE_MINUTE_SECONDS * 1000
            ? DEFAULT_TASK_ROLLUP_DELAY_MS
            : delayMs)) {
      return;
    }

    String latestTimeStr = globalSettingService.getValue(latestRollupSetting);
    // TODO 发现当前的时间超过统计时间, 是否需要重置统计时间到上一次
    if (StringUtils.isNotBlank(latestTimeStr) && thisTime.getTime()
        - DateUtils.parseISO8601Date(latestTimeStr).getTime() >= intervalSecond * 1000) {
      Date latestTime = DateUtils.parseISO8601Date(latestTimeStr);
      while (latestTime.before(thisTime)) {

        Date startTime = new Date(latestTime.getTime());
        Date endTime = new Date(startTime.getTime() + intervalSecond * 1000);

        // 汇总数据
        for (DataRecordDaoCk dataRecordDao : dataRecordDaoList) {
          try {
            dataRecordDao.rollup(startTime, endTime);
          } catch (IOException e) {
            LOGGER.warn("failed to roll up {}, continue.", dataRecordDao.getClass().getName(), e);
          }
        }

        // TODO 如果rollup过程中发生异常(es故障)是否忽略本次未成功的统计值, 发生异常不记录时间, 等恢复正常重新计算
        // 记录统计时间
        globalSettingService.setValue(latestRollupSetting, DateUtils.toStringISO8601(endTime));
        latestTime = endTime;
      }
    } else if (StringUtils.isBlank(latestTimeStr)) {
      // 第一次执行，只统计当前开始时间
      Date startTime = Date.from(currentIntervalDateTime.minusSeconds(intervalSecond)
          .atZone(ZoneId.systemDefault()).toInstant());

      // 汇总数据
      for (DataRecordDaoCk dataRecordDao : dataRecordDaoList) {
        try {
          dataRecordDao.rollup(startTime, thisTime);
        } catch (IOException e) {
          LOGGER.warn("failed to roll up {}, continue.", dataRecordDao.getClass().getName(), e);
        }
      }

      // 记录统计时间
      globalSettingService.setValue(latestRollupSetting, DateUtils.toStringISO8601(thisTime));
    }
  }

  /**
   * @param localDateTime
   * @param intervalSecond
   * @return
   */
  private LocalDateTime computeCurrentIntervalDateTime(LocalDateTime localDateTime,
      long intervalSecond) {
    // 根据间隔时间
    LocalTime localTime = LocalTime.ofSecondOfDay(
        localDateTime.toLocalTime().toSecondOfDay() / intervalSecond * intervalSecond);
    return LocalDateTime.of(localDateTime.toLocalDate(), localTime);
  }

}
