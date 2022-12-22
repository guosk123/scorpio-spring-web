package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricNetifDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2020年12月4日, fpc-manager
 */
public interface MetricNetifDataRecordDao {

  List<MetricNetifDataRecordDO> queryMetricNetifHistograms(MetricQueryVO queryVO, String netifName,
      boolean extendedBound);

  Map<String, Object> queryNetifLatestState(String netifName);

}
