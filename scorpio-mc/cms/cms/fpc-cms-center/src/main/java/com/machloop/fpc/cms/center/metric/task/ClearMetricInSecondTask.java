package com.machloop.fpc.cms.center.metric.task;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.metric.dao.MetricInSecondDao;

/**
 * @author guosk
 *
 * create at 2022年2月19日, fpc-cms-center
 */
@Component
public class ClearMetricInSecondTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ClearMetricInSecondTask.class);

  @Autowired
  private MetricInSecondDao metricInSecondDao;

  // @Scheduled(cron = "${task.clear.metric-in-second.cron}")
  public void run() {
    LOGGER.debug("start delete metric in second data.");

    // 删除一天前的数据
    Date expireDate = DateUtils.beforeDayDate(DateUtils.now(), Constants.ONE_DAYS);
    metricInSecondDao.deleteExpireMetricData(expireDate);

    LOGGER.debug("end delete metric in second data.");
  }

}
