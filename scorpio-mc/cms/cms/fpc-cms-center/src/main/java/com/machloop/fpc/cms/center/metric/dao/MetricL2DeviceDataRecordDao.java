package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.data.MetricL2DeviceDataRecordDO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月26日, fpc-manager
 */
public interface MetricL2DeviceDataRecordDao {

  List<Map<String, Object>> queryMetricL2DeviceRawdatas(MetricQueryVO queryVO);

  List<MetricL2DeviceDataRecordDO> queryMetricL2Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

  /**
   * 统计二层主机的时间曲线
   * @param queryVO
   * @param aggsField
   * @param macAddress 需要统计的二层主机集合
   * @return
   */
  List<Map<String, Object>> queryMetricL2DeviceHistograms(MetricQueryVO queryVO, String aggsField,
      List<String> macAddress);
}
