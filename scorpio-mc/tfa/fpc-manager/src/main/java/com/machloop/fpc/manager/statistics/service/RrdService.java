package com.machloop.fpc.manager.statistics.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.Constants;
import com.machloop.fpc.manager.statistics.bo.TimeseriesBO;
import com.machloop.fpc.manager.statistics.data.TimeseriesDO;
import com.machloop.fpc.manager.system.data.MetricNetworkTraffic;

public interface RrdService {

  int POINT_SIZE = 60 * 60 / Constants.ONE_MINUTE_SECONDS; // 一小时放入一个cell（=60）
  int CELL_SIZE = 24; // 存储24小时，每小时一个cell，需要cell的数量为24
  int RRD_CAPACITY = CELL_SIZE * POINT_SIZE; // 容纳数据点的总数（=1440）

  Map<String, Long> queryTimeseriesDataPoint(List<String> rrdNameList, String interval,
      Date startTime, Date endTime);

  TimeseriesBO queryRrdHistogram(String rrdName, String interval, Date startTime, Date endTime);

  List<TimeseriesDO> queryTimeseries(String rrdName, int startPoint, int endPoint);

  int produceRrdData(MetricNetworkTraffic metricStatElement,
      MetricNetworkTraffic previousStatElement, long currentTime, String rrdName);

  MetricNetworkTraffic buildMetricStatElem(String rrdName);

}
