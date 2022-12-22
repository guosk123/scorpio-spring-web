package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricHostgroupDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricHostgroupDataRecordDao {

  List<Map<String, Object>> queryMetricHostGroupRawdatas(MetricQueryVO queryVO);

  List<MetricHostgroupDataRecordDO> queryMetricHostgroups(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);

  /**
   * 统计地址组的时间曲线
   * @param queryVO
   * @param aggsField
   * @param hostgroupIds 需要统计地址组集合
   * @return
   */
  List<Map<String, Object>> queryMetricHostgroupHistograms(MetricQueryVO queryVO, String aggsField,
      List<String> hostgroupIds);
}
