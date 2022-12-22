package com.machloop.fpc.manager.asset.service.impl;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.manager.asset.bo.AssetAlarmBO;
import com.machloop.fpc.manager.asset.bo.AssetBaselineBO;
import com.machloop.fpc.manager.asset.bo.OSNameBO;
import com.machloop.fpc.manager.asset.dao.AssetAlarmDao;
import com.machloop.fpc.manager.asset.dao.AssetBaselineDao;
import com.machloop.fpc.manager.asset.dao.AssetDeviceDao;
import com.machloop.fpc.manager.asset.dao.AssetInformationDao;
import com.machloop.fpc.manager.asset.dao.AssetOSDao;
import com.machloop.fpc.manager.asset.data.AssetAlarmDO;
import com.machloop.fpc.manager.asset.data.AssetBaselineDO;
import com.machloop.fpc.manager.asset.data.OSNameDO;
import com.machloop.fpc.manager.asset.service.AssetConfigurationService;
import com.machloop.fpc.manager.asset.service.AssetInformationService;
import com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月6日, fpc-manager
 */
@Service
public class AssetConfigurationServiceImpl implements AssetConfigurationService {

  private static final Map<String, Object> typeMap = Maps.newHashMapWithExpectedSize(4);
  static {
    typeMap.put("1", "deviceType");// 设备类型
    typeMap.put("2", "port");// 监听端口
    typeMap.put("3", "label");// 服务标签
    typeMap.put("4", "os");// 操作系统
    typeMap.put("5", "assetOnline");// 资产是否在线感知
  }

  @Autowired
  private AssetBaselineDao assetBaselineDao;

  @Autowired
  private AssetInformationDao assetInformationDao;

  @Autowired
  private AssetInformationService assetInformationServcie;

  @Autowired
  private AssetAlarmDao assetAlarmDao;

  @Autowired
  private AssetDeviceDao assetDeviceDao;

  @Autowired
  private AssetOSDao assetOSDao;

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetConfigurationService#qureyAssetBaselines(java.lang.String, java.lang.String, java.lang.String, com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<Map<String, Object>> qureyAssetBaselines(String ips, String sortProperty,
      String sortDirection, Pageable page) {

    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    List<String> ipList = CsvUtils.convertCSVToList(ips);
    List<AssetBaselineDO> assetBaselineDOList = assetBaselineDao.queryAssetBaselines(ipList);
    Map<String, List<AssetBaselineDO>> ipAssetBaselineMap = assetBaselineDOList.stream()
        .collect(Collectors.groupingBy(AssetBaselineDO::getIpAddress));

    for (String ipAddress : ipAssetBaselineMap.keySet()) {
      List<AssetBaselineDO> rawAssetList = ipAssetBaselineMap.get(ipAddress);
      result.add(mergeAsset(rawAssetList));
    }

    // 排序
    result.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        String o1Value = MapUtils.getString(o1, TextUtils.underLineToCamel(sortProperty));
        String o2Value = MapUtils.getString(o2, TextUtils.underLineToCamel(sortProperty));

        return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
            ? o1Value.compareTo(o2Value)
            : o2Value.compareTo(o1Value);
      }
    });
    int total = result.size();

    // 分页
    result.stream().skip(page.getOffset()).limit(page.getPageSize()).collect(Collectors.toList());

    return new PageImpl<>(result, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetConfigurationService#saveOrUpdateAssetBaselines(com.machloop.fpc.manager.asset.bo.AssetBaselineBO, java.lang.String)
   */
  @Override
  public AssetBaselineBO saveOrUpdateAssetBaselines(AssetBaselineBO assetBaselineBO,
      String operatorId) {

    List<String> ipAddressList = CsvUtils.convertCSVToList(assetBaselineBO.getIpAddress());
    List<String> typeList = CsvUtils.convertCSVToList(assetBaselineBO.getType());

    int ipCount = ipAddressList.size();
    int typeCount = typeList.size();
    List<AssetBaselineDO> assetBaselineDOList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    // 将assetBaselineBO中的多个ip和多个type转换成一个ip和一个type的asset
    for (int i = 0; i < ipCount; i++) {
      AssetInformationQueryVO queryVO = new AssetInformationQueryVO();
      queryVO.setIpAddress(ipAddressList.get(i));
      // 获取最新的资产数据作为基线
      List<Map<String, Object>> assetInformationList = assetInformationDao
          .queryAssetsWithValue(queryVO, null, null, null, 0);
      List<Map<String, Object>> aggregatedAssetInformation = assetInformationServcie
          .aggregateAssetInformationList(queryVO, assetInformationList);
      for (int j = 0; j < typeCount; j++) {
        AssetBaselineDO asset = new AssetBaselineDO();
        asset.setIpAddress(ipAddressList.get(i));
        asset.setType(typeList.get(j));
        asset.setDescription(assetBaselineBO.getDescription());

        String type = MapUtils.getString(typeMap, asset.getType());
        Map<String, Object> aggregatedAsset = aggregatedAssetInformation.get(0);

        Map<String,
            Object> baselineMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        String value1 = "";

        if (StringUtils.equals(type, "assetOnline")) {
          baselineMap.put(type, "是");
        } else {
          value1 = MapUtils.getString(aggregatedAsset, type);
          baselineMap.put(type, StringUtils.isNotBlank(value1) ? value1 : "无");
        }
        asset.setBaseline(JsonHelper.serialize(baselineMap));
        assetBaselineDOList.add(asset);
      }
    }
    assetBaselineDao.saveOrUpdateAssetBaselines(assetBaselineDOList, operatorId);
    return assetBaselineBO;
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetConfigurationService#queryAssetAlarms(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String)
   */
  @Override
  public Page<AssetAlarmBO> queryAssetAlarms(Pageable page, String ipAddress, String type) {

    Page<AssetAlarmDO> assetAlarmPage = assetAlarmDao.queryAssetAlarms(page, ipAddress, type);
    long totalElem = assetAlarmPage.getTotalElements();

    List<AssetAlarmBO> assetAlarmBOList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (AssetAlarmDO assetAlarmDO : assetAlarmPage) {
      AssetAlarmBO assetAlarmBO = new AssetAlarmBO();
      BeanUtils.copyProperties(assetAlarmDO, assetAlarmBO);
      assetAlarmBOList.add(assetAlarmBO);
    }
    return new PageImpl<>(assetAlarmBOList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetConfigurationService#queryAssetDevices()
   */
  @Override
  public List<Map<String, Object>> queryAssetDevices() {

    return assetDeviceDao.queryAssetDevices();
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetConfigurationService#queryAssetOS(java.lang.String)
   */
  @Override
  public List<OSNameBO> queryAssetOS(String id) {

    List<OSNameDO> osNameDOList = assetOSDao.queryAssetOS(id);

    List<OSNameBO> osNameBOList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (OSNameDO osNameDO : osNameDOList) {
      OSNameBO osNameBO = new OSNameBO();
      BeanUtils.copyProperties(osNameDO, osNameBO);
      osNameBOList.add(osNameBO);
    }
    return osNameBOList;

  }

  @Override
  public Map<String, Object> queryAssetOS() {

    List<OSNameDO> osNameDOList = assetOSDao.queryAssetOS("");

    List<OSNameBO> osNameBOList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (OSNameDO osNameDO : osNameDOList) {
      OSNameBO osNameBO = new OSNameBO();
      BeanUtils.copyProperties(osNameDO, osNameBO);
      osNameBOList.add(osNameBO);
    }
    return OSNameBO2Map(osNameBOList);

  }

  private Map<String, Object> OSNameBO2Map(List<OSNameBO> osNameBOList) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    for (OSNameBO osName : osNameBOList) {
      map.put(osName.getId(), osName.getOs());
    }
    return map;
  }

  /**
   * @see com.machloop.fpc.manager.asset.service.AssetConfigurationService#deleteAssetBaseline(java.lang.String, java.lang.String)
   */
  @Override
  public AssetBaselineBO deleteAssetBaseline(String ipAddress, String operatorId) {

    List<String> ipList = CsvUtils.convertCSVToList(ipAddress);
    List<AssetBaselineDO> existAssetBaselines = assetBaselineDao.queryAssetBaselines(ipList);
    if (CollectionUtils.isEmpty(existAssetBaselines)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "资产基线不存在");
    }

    assetBaselineDao.deleteAssetBaseline(ipAddress, operatorId);

    AssetBaselineBO assetBaselineBO = new AssetBaselineBO();
    List<String> baselineList = existAssetBaselines.stream().map(e -> e.getBaseline())
        .collect(Collectors.toList());
    List<String> typeList = existAssetBaselines.stream().map(e -> e.getType())
        .collect(Collectors.toList());
    assetBaselineBO.setBaseline(CsvUtils.convertCollectionToCSV(baselineList));
    assetBaselineBO.setType(CsvUtils.convertCollectionToCSV(typeList));
    return assetBaselineBO;
  }

  private Map<String, Object> mergeAsset(List<AssetBaselineDO> rawAssetList) {

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> typeList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> baselineList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (AssetBaselineDO rawAsset : rawAssetList) {
      typeList.add(rawAsset.getType());
      baselineList.add(
          JsonHelper.deserialize(rawAsset.getBaseline(), new TypeReference<Map<String, Object>>() {
          }));
    }
    Date updateTime = rawAssetList.stream().map(e -> e.getUpdateTime())
        .max((x, y) -> x.compareTo(y)).get();
    result.put("ipAddress", rawAssetList.get(0).getIpAddress());
    result.put("type", typeList);
    result.put("baseline", baselineList);
    result.put("updateTime", DateUtils.toStringISO8601(updateTime));
    result.put("description", rawAssetList.get(0).getDescription());
    return result;
  }
}
