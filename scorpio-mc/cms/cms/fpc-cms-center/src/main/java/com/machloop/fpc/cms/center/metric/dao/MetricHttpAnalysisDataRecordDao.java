package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
/**
 * @author guosk
 *
 * create at 2021年7月21日, fpc-manager
 */
public interface MetricHttpAnalysisDataRecordDao {

  List<Map<String, Object>> queryHttpAnalysis(MetricQueryVO queryVO, String type,
      String sortProperty, String sortDirection);

}
