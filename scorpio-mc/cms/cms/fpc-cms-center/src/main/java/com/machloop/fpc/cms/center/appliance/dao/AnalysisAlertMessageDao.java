package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author guosk
 *
 * create at 2021年9月17日, fpc-manager
 */
public interface AnalysisAlertMessageDao {

  List<Map<String, Object>> analysisAlertMessage(Date startTime, Date endTime, int interval,
      Map<String, Object> params, String tableName, List<String> metrics);

}
