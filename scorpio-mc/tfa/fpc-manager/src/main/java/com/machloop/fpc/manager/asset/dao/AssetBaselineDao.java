package com.machloop.fpc.manager.asset.dao;

import java.util.List;

import com.machloop.fpc.manager.asset.data.AssetBaselineDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月6日, fpc-manager
 */
public interface AssetBaselineDao {

  List<AssetBaselineDO> queryAssetBaselines(List<String> ipList);

  List<AssetBaselineDO> queryAssetBaselineByIp(String ipAddress);

  void saveOrUpdateAssetBaselines(List<AssetBaselineDO> assetBaselineDOList, String operatorId);

  int deleteAssetBaseline(String ipAddress, String operatorId);

}
