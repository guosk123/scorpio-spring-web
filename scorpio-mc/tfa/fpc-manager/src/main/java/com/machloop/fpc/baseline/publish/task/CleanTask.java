package com.machloop.fpc.baseline.publish.task;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.fpc.baseline.publish.PublishDao;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.AlertRuleBO;
import com.machloop.fpc.manager.appliance.service.AlertRuleService;
import com.machloop.fpc.npm.appliance.dao.BaselineSettingDao;
import com.machloop.fpc.npm.appliance.data.BaselineSettingDO;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年6月8日, fpc-manager
 */
@Component
public class CleanTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(CleanTask.class);

  @Autowired
  private PublishDao publishDao;
  @Autowired
  private BaselineSettingDao baselineSettingDao;
  @Autowired
  private AlertRuleService alertRuleService;

  @Scheduled(cron = "${task.publish.clean.cron}")
  public void run() {
    // 获取有效的NPM基线
    List<BaselineSettingDO> npmBaselineSettings = baselineSettingDao.queryBaselineSettings();
    Tuple3<List<String>, List<String>,
        List<String>> validNpmBaselines = getCurrentValidBaseline(npmBaselineSettings);

    int cleanCount = 0;

    // 清除1小时以前的分钟基线值
    Date beforeOneHour = DateUtils.truncate(DateUtils.addHours(new Date(), -1),
        Calendar.HOUR_OF_DAY);
    cleanCount += publishDao.cleanNpmBefore(validNpmBaselines.getT1(), beforeOneHour);

    // 清除1天以前的5分钟基线值
    Date beforeOneDay = DateUtils.truncate(DateUtils.addDays(new Date(), -1), Calendar.HOUR_OF_DAY);
    cleanCount += publishDao.cleanNpmBefore(validNpmBaselines.getT2(), beforeOneDay);

    // 清除7天以前的小时基线值
    Date beforeSevenDay = DateUtils.truncate(DateUtils.addDays(new Date(), -7),
        Calendar.HOUR_OF_DAY);
    cleanCount += publishDao.cleanNpmBefore(validNpmBaselines.getT3(), beforeSevenDay);

    // 清除告警基线值
    int retainHour = Integer
        .parseInt(HotPropertiesHelper.getProperty("baseline.result.retention.hour"));
    Date alertCleanBefore = DateUtils.truncate(DateUtils.addHours(new Date(), -retainHour),
        Calendar.HOUR_OF_DAY);
    cleanCount += publishDao.cleanAlertBefore(alertCleanBefore);

    LOGGER.info(
        "success to clean published baseline, minute npm baseline before [{}], "
            + "five minute npm baseline before [{}], hour npm baseline before [{}], "
            + "alert baseline before [{}], total clean count: [{}]",
        beforeOneHour, beforeOneDay, beforeSevenDay, alertCleanBefore, cleanCount);

    // 清除已移除的基线值
    List<String> validBaselineIds = alertRuleService
        .queryAlertRulesByCategory(FpcConstants.ALERT_CATEGORY_TREND).stream()
        .map(AlertRuleBO::getId).collect(Collectors.toList());
    validBaselineIds.addAll(
        npmBaselineSettings.stream().map(BaselineSettingDO::getId).collect(Collectors.toList()));
    int invalidCount = publishDao.cleanInvalidValue(validBaselineIds);
    LOGGER.info("success to clean invalid baseline : [{}]", invalidCount);
  }

  private Tuple3<List<String>, List<String>, List<String>> getCurrentValidBaseline(
      List<BaselineSettingDO> baselineSettings) {
    List<String> minuteBaselines = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> fiveMinuteBaselines = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> hourBaselines = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    baselineSettings.forEach(baselineSetting -> {
      switch (baselineSetting.getWindowingModel()) {
        case FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_DAY:
        case FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_WEEK:
        case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_MINUTES:
          minuteBaselines.add(baselineSetting.getId());
          break;
        case FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_DAY:
        case FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_WEEK:
        case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_FIVE_MINUTES:
          fiveMinuteBaselines.add(baselineSetting.getId());
          break;
        case FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_DAY:
        case FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_WEEK:
        case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_HOURS:
          hourBaselines.add(baselineSetting.getId());
          break;
        default:
          break;
      }
    });

    return Tuples.of(minuteBaselines, fiveMinuteBaselines, hourBaselines);
  }

}
