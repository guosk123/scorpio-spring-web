package com.machloop.fpc.manager.asset.service;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.asset.bo.AssetAlarmBO;
import com.machloop.fpc.manager.asset.bo.AssetBaselineBO;
import com.machloop.fpc.manager.asset.bo.OSNameBO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月6日, fpc-manager
 */
public interface AssetConfigurationService {

  Page<Map<String, Object>> qureyAssetBaselines(String ip, String sortProperty,
      String sortDirection, Pageable page);

  AssetBaselineBO saveOrUpdateAssetBaselines(AssetBaselineBO assetBaselineBO, String operatorId);

  Page<AssetAlarmBO> queryAssetAlarms(Pageable page, String ipAddress, String type);

  List<Map<String, Object>> queryAssetDevices();
  
  List<OSNameBO> queryAssetOS(String id);
  
  Map<String, Object> queryAssetOS();

  AssetBaselineBO deleteAssetBaseline(String id, String operatorId);
}
