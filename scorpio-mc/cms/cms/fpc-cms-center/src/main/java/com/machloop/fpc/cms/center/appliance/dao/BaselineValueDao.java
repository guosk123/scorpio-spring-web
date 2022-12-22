package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.BaselineValueDO;

/**
 * @author guosk
 *
 * create at 2021年4月21日, fpc-manager
 */
public interface BaselineValueDao {

  List<BaselineValueDO> queryBaselineValues(String sourceType, String sourceId, Date startTime,
      Date endTime);

}
