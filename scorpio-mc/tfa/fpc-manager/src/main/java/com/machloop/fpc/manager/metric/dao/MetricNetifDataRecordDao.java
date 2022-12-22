package com.machloop.fpc.manager.metric.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metric.data.MetricNetifDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2020年12月4日, fpc-manager
 */
public interface MetricNetifDataRecordDao {

  List<MetricNetifDataRecordDO> queryMetricNetifHistograms(MetricQueryVO queryVO, String netifName,
      boolean extendedBound);

  List<MetricNetifDataRecordDO> queryMetricNetifs(Date afterTime);

  Map<String, Object> queryNetifLatestState(String netifName);

}
