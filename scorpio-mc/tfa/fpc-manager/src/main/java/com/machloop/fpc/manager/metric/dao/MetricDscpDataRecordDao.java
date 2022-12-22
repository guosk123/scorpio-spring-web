package com.machloop.fpc.manager.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metric.data.MetricDscpDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年4月23日, fpc-manager
 */
public interface MetricDscpDataRecordDao {

  List<MetricDscpDataRecordDO> queryMetricDscps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计IP通讯对的时间曲线
   * @param queryVO
   * @param aggsField
   * @param dscpTypes 需要统计的DSCP类型集合
   * @return
   */
  List<Map<String, Object>> queryMetricDscpHistograms(MetricQueryVO queryVO, String aggsField,
      List<String> dscpTypes);

}
