package com.machloop.fpc.baseline.setting.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.baseline.calculate.Operation;
import com.machloop.fpc.baseline.calculate.OperationFactory;
import com.machloop.fpc.baseline.mission.Metric;
import com.machloop.fpc.baseline.mission.Mission;
import com.machloop.fpc.baseline.mission.MissionService;
import com.machloop.fpc.baseline.mission.Window;
import com.machloop.fpc.baseline.mission.WindowModelEnum;
import com.machloop.fpc.baseline.setting.service.BaselineSettingSyncService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.common.helper.AggsFunctionEnum;
import com.machloop.fpc.npm.appliance.dao.BaselineSettingDao;
import com.machloop.fpc.npm.appliance.data.BaselineSettingDO;

import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年5月7日, fpc-manager
 */
@Service
public class NpmBaselineSettingServiceImpl implements BaselineSettingSyncService {

  private static final Logger LOGGER = LoggerFactory.getLogger(NpmBaselineSettingServiceImpl.class);

  private Map<String, BaselineSettingDO> baselineSettingMap = Maps
      .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  @Autowired
  private MissionService missionService;

  @Autowired
  private BaselineSettingDao baselineSettingDao;

  /**
   * @see com.machloop.fpc.baseline.setting.service.BaselineSettingSyncService#sync()
   */
  @Override
  public Tuple4<String, Integer, Integer, Integer> sync() {
    List<BaselineSettingDO> settings = baselineSettingDao.queryBaselineSettings();
    Set<String> settingIds = Sets.newHashSetWithExpectedSize(settings.size());

    int addCount = 0;
    int updateCount = 0;
    int removeCount = 0;
    for (BaselineSettingDO setting : settings) {
      String id = setting.getId();
      settingIds.add(id);

      BaselineSettingDO exist = baselineSettingMap.get(id);
      if (exist == null) {
        // 新增加的配置
        Mission newMission = createMission(setting);
        if (newMission != null) {
          missionService.addMission(newMission);
          baselineSettingMap.put(id, setting);
          addCount++;
          LOGGER.info("found npm baseline setting created, id: {}", id);
        }
      } else if (!StringUtils.equals(exist.getWeightingModel(), setting.getWeightingModel())
          || !StringUtils.equals(exist.getWindowingModel(), setting.getWindowingModel())
          || exist.getWindowingCount() != setting.getWindowingCount()) {
        // 配置变更
        missionService.removeMission(id);
        Mission updateMission = createMission(setting);
        if (updateMission != null) {
          missionService.addMission(updateMission);
          baselineSettingMap.put(id, setting);
          updateCount++;
          LOGGER.info("found npm baseline setting changed, id: {}", id);
        }
      }
    }

    Iterator<Entry<String, BaselineSettingDO>> iterator = baselineSettingMap.entrySet().iterator();
    while (iterator.hasNext()) {
      String id = iterator.next().getKey();
      if (!settingIds.contains(id)) {
        // 配置删除
        missionService.removeMission(id);
        iterator.remove();
        removeCount++;
        LOGGER.info("found npm baseline setting removed, id: {}", id);
      }
    }

    return Tuples.of("npm metric", addCount, updateCount, removeCount);
  }

  private Mission createMission(BaselineSettingDO setting) {
    String id = setting.getId();
    String sourceType = setting.getSourceType();

    LOGGER.debug("baseline setting json: {}", JsonHelper.serialize(setting, false));

    String numeratorMetric = "";
    try {
      numeratorMetric = getMetricField(setting.getCategory());
    } catch (IllegalArgumentException e) {
      LOGGER.warn("baseline setting error, {}, setting id: {}." + e.getMessage(), id);
      return null;
    }

    boolean isRatio = false;
    Integer denominatorConstants = null;
    if (StringUtils.equals(setting.getCategory(), FpcConstants.BASELINE_CATEGORY_BANDWIDTH)) {
      isRatio = true;
      try {
        denominatorConstants = getMetadataInterval(setting.getWindowingModel());
      } catch (IllegalArgumentException e) {
        LOGGER.warn("baseline setting error, {}, setting id: {}." + e.getMessage(), id);
        return null;
      }
    }

    Mission mission = null;
    try {
      Metric metric = new Metric(isRatio, numeratorMetric, sourceType,
          StringUtils.joinWith(",", setting.getNetworkId(), setting.getServiceId()), null, null,
          null, denominatorConstants);
      Operation operation = OperationFactory
          .createOperation(AggsFunctionEnum.getEnumByValue(setting.getWeightingModel()));
      Window window = Window.createWindow(setting.getWindowingCount(),
          WindowModelEnum.getEnumByValue(setting.getWindowingModel()));
      mission = new Mission(id, FpcConstants.BASELINE_SETTING_SOURCE_NPM, id,
          setting.getNetworkId(), setting.getServiceId(), metric, operation, window, null);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("mission create failed, reason: {}", e.getMessage());
    }

    return mission;
  }

  private int getMetadataInterval(String windowingModel) {
    int interval = Constants.ONE_MINUTE_SECONDS;

    switch (windowingModel) {
      case FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_DAY:
      case FpcConstants.BASELINE_WINDOWING_MODEL_MINUTE_OF_WEEK:
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_MINUTES:
        interval = Constants.ONE_MINUTE_SECONDS;
        break;
      case FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_DAY:
      case FpcConstants.BASELINE_WINDOWING_MODEL_FIVE_MINUTE_OF_WEEK:
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_FIVE_MINUTES:
        interval = Constants.FIVE_MINUTE_SECONDS;
        break;
      case FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_DAY:
      case FpcConstants.BASELINE_WINDOWING_MODEL_HOUR_OF_WEEK:
      case FpcConstants.BASELINE_WINDOWING_MODEL_LAST_N_HOURS:
        interval = Constants.ONE_HOUR_SECONDS;
        break;
      default:
        throw new IllegalArgumentException("unsupport windowing model.");
    }

    return interval;
  }

  private String getMetricField(String category) {
    String metricField = "";
    switch (category) {
      case FpcConstants.BASELINE_CATEGORY_BANDWIDTH:
        metricField = "total_bytes";
        break;
      case FpcConstants.BASELINE_CATEGORY_FLOW:
        metricField = "total_bytes";
        break;
      case FpcConstants.BASELINE_CATEGORY_PACKET:
        metricField = "total_packets";
        break;
      case FpcConstants.BASELINE_CATEGORY_RESPONSELATENCY:
        metricField = "server_response_latency_avg";
        break;
      default:
        throw new IllegalArgumentException("unsupport baseline category.");
    }

    return metricField;
  }

}
