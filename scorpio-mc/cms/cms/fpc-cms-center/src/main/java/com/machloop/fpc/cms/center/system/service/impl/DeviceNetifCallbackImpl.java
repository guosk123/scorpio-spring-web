package com.machloop.fpc.cms.center.system.service.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.webapp.system.service.DeviceNetifCallback;
import com.machloop.alpha.webapp.system.service.impl.SystemServerIpServiceImpl;
import com.machloop.fpc.cms.center.central.dao.CentralNetifDao;
import com.machloop.fpc.cms.center.central.data.CentralNetifDO;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2020年9月24日, fpc-manager
 */
@Service
public class DeviceNetifCallbackImpl implements DeviceNetifCallback {

  @Autowired
  private CentralNetifDao centralNetifDao;

  @Autowired
  private LicenseService licenseService;

  @PostConstruct
  public void initial() {
    SystemServerIpServiceImpl.register(this);
  }

  /**
   * @see com.machloop.alpha.webapp.system.service.DeviceNetifCallback#getManagementNetifName()
   */
  @Override
  public String getManagementNetifName() {
    // 查询管理接口名称
    CentralNetifDO centralNetifDO = centralNetifDao.queryCentralNetifById("1");

    String mgtIfname = "";
    if (StringUtils.isNotBlank(centralNetifDO.getId())) {
      mgtIfname = centralNetifDO.getNetifName();
    } else {
      List<CentralNetifDO> localNetifs = centralNetifDao.queryCentralNetifProfiles(
          FpcCmsConstants.DEVICE_TYPE_CMS, licenseService.queryDeviceSerialNumber(),
          Lists.newArrayList(FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT));
      if (localNetifs.size() > 0) {
        mgtIfname = localNetifs.get(0).getNetifName();
      }
    }

    return mgtIfname;
  }

}
