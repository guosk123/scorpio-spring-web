package com.machloop.fpc.cms.baseline.publish.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.BaselineValueDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月4日, fpc-baseline
 */
public interface PublishDao {

  void publish(List<BaselineValueDO> values);

  int cleanNpmBefore(List<String> ids, Date beforeTime);

  int cleanInvalidValue(List<String> validIds);
}
