package com.machloop.fpc.cms.center.broker.task.subordinate;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.broker.dao.CollectMetricDao;

@Component
public class CollectMetricClearTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(CollectMetricClearTask.class);

  @Autowired
  private CollectMetricDao collectMetricDao;

  /**
   * 清理超期的采集信息统计
   */
  @Scheduled(cron = "${task.clear.collect.metric.cron}")
  public void run() {
    LOGGER.debug("start delete expire collect metric.");

    // 删除一天前的数据
    Date expireDate = DateUtils.beforeDayDate(DateUtils.now(), Constants.ONE_DAYS);
    collectMetricDao.deleteExpireCollectMetric(expireDate);

    LOGGER.debug("end delete expire collect metric.");
  }
}
