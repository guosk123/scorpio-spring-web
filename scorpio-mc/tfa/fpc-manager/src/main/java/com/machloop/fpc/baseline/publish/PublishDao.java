package com.machloop.fpc.baseline.publish;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.npm.appliance.data.BaselineValueDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月4日, fpc-baseline
 */
public interface PublishDao {

  void publish(List<BaselineValueDO> values);

  int cleanAlertBefore(Date beforeTime);

  int cleanNpmBefore(List<String> ids, Date beforeTime);

  int cleanInvalidValue(List<String> validIds);
}
