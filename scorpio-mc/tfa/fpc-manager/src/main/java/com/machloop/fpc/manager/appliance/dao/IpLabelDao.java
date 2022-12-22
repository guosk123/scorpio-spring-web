package com.machloop.fpc.manager.appliance.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.appliance.data.IpLabelDO;

/**
 * @author chenshimiao
 *
 * create at 2022/9/6 10:46 AM,cms
 * @version 1.0
 */
public interface IpLabelDao {
  Page<IpLabelDO> queryIpLabels(PageRequest page, String name, String category);

  List<IpLabelDO> queryIpLabels();

  List<IpLabelDO> queryIdAndIp();

  IpLabelDO queryIpLabel(String id);

  IpLabelDO queryIpLabelByName(String name);

  List<Map<String, Object>> queryIpLabelByCategory();

  IpLabelDO saveIpLabel(IpLabelDO ipLabelDO);

  int updateIpLabel(IpLabelDO ipLabelDO);

  int deleteIpLabel(String id, String operatorId);
}
