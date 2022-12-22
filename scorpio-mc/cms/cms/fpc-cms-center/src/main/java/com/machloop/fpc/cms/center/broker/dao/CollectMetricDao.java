package com.machloop.fpc.cms.center.broker.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.broker.data.CollectMetricDO;

public interface CollectMetricDao {

  List<CollectMetricDO> queryCollectMetrics(String deviceType, String deviceSerialNumber,
      String type, Date startTime, Date endTime);

  CollectMetricDO queryCollectMetric(String deviceType, String deviceSerialNumber, String type,
      Date startTime);

  /**
   * 若更新将按照fpcId、类型、统计时间区间更新
   * @param collectMetricDO
   * @return
   */
  int saveOrUpdateCollectMetric(CollectMetricDO collectMetricDO);

  /**
   * 删除失效日期之前的所有数据
   * @param expireDate
   * @return
   */
  int deleteExpireCollectMetric(Date expireDate);

}
