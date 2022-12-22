package com.machloop.fpc.cms.center.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月20日, fpc-manager
 */
public interface MetricHttpRequestDataRecordDao {

  List<Map<String, Object>> queryHttpRequestHistograms(MetricQueryVO queryVO);

}
