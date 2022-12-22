package com.machloop.fpc.manager.asset.dao;

import java.util.List;

import com.machloop.fpc.manager.asset.data.OSNameDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月9日, fpc-manager
 */
public interface AssetOSDao {
  List<OSNameDO> queryAssetOS(String id);
}
