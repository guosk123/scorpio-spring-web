package com.machloop.fpc.manager.asset.dao;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月5日, fpc-manager
 */
public interface AssetInformationDao {

  // 同时有过滤条件和上报时间时使用
  List<Map<String, Object>> queryAssetsWithValueAndFirstTime(String queryId,
      AssetInformationQueryVO queryVO, List<String> alarmIpList, Pageable page, int count);

  // 没有上报时间时使用
  List<Map<String, Object>> queryAssetsWithValue(AssetInformationQueryVO queryVO,
      List<String> baselineIpList, List<String> alarmIpList, Pageable page, int count);

  // 只有上报时间时使用
  List<Map<String, Object>> queryAssetsWithFirstTime(AssetInformationQueryVO queryVO,
      List<String> alarmIpList, Pageable page, int count);

  void dropExpiredData(String partitionName);

  long countAssetInformation(AssetInformationQueryVO queryVO, List<String> alarmIpList);
}
