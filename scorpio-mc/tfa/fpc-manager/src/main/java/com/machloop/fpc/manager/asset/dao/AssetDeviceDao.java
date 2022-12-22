package com.machloop.fpc.manager.asset.dao;

import java.util.List;
import java.util.Map;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月9日, fpc-manager
 */
public interface AssetDeviceDao {
  List<Map<String, Object>> queryAssetDevices();
}
