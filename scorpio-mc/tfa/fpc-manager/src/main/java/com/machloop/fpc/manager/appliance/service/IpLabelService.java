package com.machloop.fpc.manager.appliance.service;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.manager.appliance.bo.IpLabelBO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/9/6 10:45 AM,cms
 * @version 1.0
 */
public interface IpLabelService {

  Page<IpLabelBO> queryIpLabels(PageRequest page, String name, String category);

  List<IpLabelBO> queryIpLabels();

  IpLabelBO queryIpLabel(String id);

  IpLabelBO queryIpLabelByIp(String ip);

  Map<String, Object> queryIpLabelCategory();

  IpLabelBO saveIpLabel(IpLabelBO iplabelBO, String operatorId);

  IpLabelBO updateIpLabel(String id, IpLabelBO ipLabelBO, String operatorId);

  IpLabelBO deleteIpLabel(String id, String operatorId, boolean forceDelete);
}
