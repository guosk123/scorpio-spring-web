package com.machloop.fpc.manager.asset.dao;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.asset.data.AssetAlarmDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月7日, fpc-manager
 */
public interface AssetAlarmDao {

  Page<AssetAlarmDO> queryAssetAlarms(Pageable page, String ipAddress, String type);

  List<AssetAlarmDO> queryAssetAlarms(String ipAddress, int type);

  void saveAssetAlarms(AssetAlarmDO assetAlarmBO);
}
