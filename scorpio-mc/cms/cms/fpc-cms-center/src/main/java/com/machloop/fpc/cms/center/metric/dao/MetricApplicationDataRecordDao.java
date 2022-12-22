package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricApplicationDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricApplicationDataRecordDao {

  List<Map<String, Object>> queryMetricApplicationRawdatas(MetricQueryVO queryVO);

  List<MetricApplicationDataRecordDO> queryMetricApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, int type);

  /**
   * 统计全部应用
   * @param queryVO
   * @param aggsField 聚合字段
   * @param sortProperty 排序字段
   * @param sortDirection 排序方式
   * @return
   */
  List<Map<String, Object>> countMetricApplications(MetricQueryVO queryVO, String aggsField,
      String sortProperty, String sortDirection);

  /**
   * 统计多个类型/子类型/应用的时间曲线
   * @param queryVO
   * @param termField 分组字段
   * @param aggsField 聚合字段
   * @param ids 需要统计的id集合
   * @return
   */
  List<Map<String, Object>> queryMetricApplicationHistograms(MetricQueryVO queryVO,
      String termField, String aggsField, List<String> ids);

  List<Map<String, Object>> queryMetricNetworkSegmentationApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection);
}
