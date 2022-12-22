package com.machloop.fpc.manager.metric.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.manager.metric.data.MetricForwardPolicyDataRecordDO;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/12 10:16,IntelliJ IDEA
 *
 */

public interface MetricForwardDataRecordDao {

  Map<String, Object> queryBandWidthByPolicyId(String policyId);


  List<MetricForwardPolicyDataRecordDO> queryMetricForwardPolicyHistograms(MetricQueryVO queryVO, String policyId,  boolean extendedBound);
}
