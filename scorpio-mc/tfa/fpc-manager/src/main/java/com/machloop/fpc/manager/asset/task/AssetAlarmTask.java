package com.machloop.fpc.manager.asset.task;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.asset.dao.AssetAlarmDao;
import com.machloop.fpc.manager.asset.dao.AssetBaselineDao;
import com.machloop.fpc.manager.asset.dao.AssetInformationDao;
import com.machloop.fpc.manager.asset.data.AssetAlarmDO;
import com.machloop.fpc.manager.asset.data.AssetBaselineDO;
import com.machloop.fpc.manager.asset.service.AssetConfigurationService;
import com.machloop.fpc.manager.asset.vo.AssetInformationQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年9月8日, fpc-manager
 */
@Component
public class AssetAlarmTask {

  private static final Map<String, Object> typeMap = Maps.newHashMapWithExpectedSize(4);
  static {
    typeMap.put("1", "deviceType");// 设备类型
    typeMap.put("2", "port");// 监听端口
    typeMap.put("3", "label");// 服务标签
    typeMap.put("4", "os");// 操作系统
  }
  @Autowired
  private AssetInformationDao assetInformationDao;

  @Autowired
  private AssetConfigurationService assetConfigurationService;

  @Autowired
  private AssetBaselineDao assetBaselineDao;

  @Autowired
  private AssetAlarmDao assetAlarmDao;

  @Scheduled(cron = "${task.system.asset.alarm.schedule.cron}")
  public void run() {

    List<AssetBaselineDO> assetBaselineDOList = assetBaselineDao.queryAssetBaselines(null);
    List<String> baselineIpList = assetBaselineDOList.stream().map(AssetBaselineDO::getIpAddress)
        .distinct().collect(Collectors.toList());
    if (CollectionUtils.isEmpty(baselineIpList)) {
      return;
    }
    List<Map<String, Object>> assetInformationList = assetInformationDao
        .queryAssetsWithValue(new AssetInformationQueryVO(), baselineIpList, null, null, 0);

    Map<Object,
        List<Map<String, Object>>> informationMap = assetInformationList.stream()
            .collect(Collectors.groupingBy(
                item -> MapUtils.getString(item, "ip") + "_" + MapUtils.getString(item, "type")));
    if (MapUtils.isEmpty(informationMap)) {
      return;
    }

    // 遍历基线，使用ip+type从Map<ip_type,information>中取值，判断value是否相同，相同则判断下一个，不相同则告警
    for (AssetBaselineDO assetBaselineDO : assetBaselineDOList) {
      String key = assetBaselineDO.getIpAddress() + "_" + assetBaselineDO.getType();
      Map<String,
          Object> temp = CollectionUtils.isEmpty(informationMap.get(key))
              ? Maps.newHashMapWithExpectedSize(0)
              : informationMap.get(key).get(0);
      if (MapUtils.isEmpty(temp)) {
        continue;
      } else {
        String value = MapUtils.getString(temp, "value1");
        if (StringUtils.equals(MapUtils.getString(temp, "type"), "os")) {
          value = assetConfigurationService.queryAssetOS(MapUtils.getString(temp, "value1")) + " "
              + MapUtils.getString(temp, "value2");
        }
        String baselineValue = MapUtils.getString(JsonHelper
            .deserialize(assetBaselineDO.getBaseline(), new TypeReference<Map<String, Object>>() {
            }), MapUtils.getString(typeMap, MapUtils.getString(temp, "type")));
        if (!StringUtils.equals(value, baselineValue)) {
          AssetAlarmDO assetAlarmDO = new AssetAlarmDO();
          assetAlarmDO.setIpAddress(assetBaselineDO.getIpAddress());
          assetAlarmDO.setBaseline(baselineValue);
          assetAlarmDO.setCurrent(value);
          assetAlarmDO.setType(assetBaselineDO.getType());
          assetAlarmDao.saveAssetAlarms(assetAlarmDO);

          // 资产下线告警
          long interval = assetBaselineDO.getUpdateTime().getTime() - DateUtils.parseISO8601Date(
              new StringBuilder(MapUtils.getString(temp, "timestamp")).insert(16, ":00").toString())
              .getTime();
          if (StringUtils.equals(assetBaselineDO.getType(), "5") && interval > 604800000) {
            assetAlarmDO.setType("5");
            assetAlarmDao.saveAssetAlarms(assetAlarmDO);
          }
        }
      }
    }
  }

}
