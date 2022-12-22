package com.machloop.fpc.manager.system.service.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.webapp.system.service.DeviceNetifCallback;
import com.machloop.alpha.webapp.system.service.impl.SystemServerIpServiceImpl;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.system.dao.DeviceNetifDao;
import com.machloop.fpc.manager.system.data.DeviceNetifDO;

/**
 * @author guosk
 *
 * create at 2020年9月24日, fpc-manager
 */
@Service
public class DeviceNetifCallbackImpl implements DeviceNetifCallback {

  @Autowired
  private DeviceNetifDao deviceNetifDao;

  @PostConstruct
  public void initial() {
    SystemServerIpServiceImpl.register(this);
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.DeviceNetifCallback#getManagementNetifName()
   */
  @Override
  public String getManagementNetifName() {
    List<String> categoryList = Lists.newArrayList(FpcConstants.DEVICE_NETIF_CATEGORY_MGMT);
    List<DeviceNetifDO> netifDOList = deviceNetifDao.queryDeviceNetifs(categoryList);

    String mgtIfname = "";
    if (CollectionUtils.isNotEmpty(netifDOList)) {
      mgtIfname = netifDOList.get(0).getName();
    }

    return mgtIfname;
  }

}
