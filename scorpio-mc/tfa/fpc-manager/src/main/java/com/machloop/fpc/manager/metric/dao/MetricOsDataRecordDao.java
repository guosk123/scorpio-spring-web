package com.machloop.fpc.manager.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月21日, fpc-manager
 */
public interface MetricOsDataRecordDao {

  List<Map<String, Object>> queryOsMetric(MetricQueryVO queryVO, String sortProperty,
      String sortDirection);

}
