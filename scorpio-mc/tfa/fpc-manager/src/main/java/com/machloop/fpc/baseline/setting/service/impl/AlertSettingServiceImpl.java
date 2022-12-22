package com.machloop.fpc.baseline.setting.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
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
import com.machloop.fpc.manager.appliance.bo.AlertRuleBO;
import com.machloop.fpc.manager.appliance.service.AlertRuleService;
import com.machloop.fpc.manager.appliance.service.CustomTimeService;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;

import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import com.alibaba.fastjson.JSONObject;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月3日, fpc-baseline
 */
@Service
public class AlertSettingServiceImpl implements BaselineSettingSyncService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertSettingServiceImpl.class);

  private Map<String,
      TrendAlertSetting> alertRuleMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  private static final String ALERT_SCOPE_ALL_NETWORK = "allNetwork";

  @Autowired
  private MissionService missionService;

  @Autowired
  private AlertRuleService alertRuleService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private CustomTimeService customTimeService;

  /**
   * @see com.machloop.fpc.baseline.setting.service.BaselineSettingSyncService#sync()
   */
  @Override
  public Tuple4<String, Integer, Integer, Integer> sync() {
    List<AlertRuleBO> trendAlerts = alertRuleService
        .queryAlertRulesByCategory(FpcConstants.ALERT_CATEGORY_TREND);
    Set<String> trendAlertIds = Sets.newHashSetWithExpectedSize(trendAlerts.size());

    // 获取网络集合
    List<String> allNetworkIds = networkService.queryNetworks().stream().map(NetworkBO::getId)
        .collect(Collectors.toList());
    allNetworkIds.addAll(logicalSubnetService.queryLogicalSubnets().stream()
        .map(LogicalSubnetBO::getId).collect(Collectors.toList()));

    int addCount = 0;
    int updateCount = 0;
    int removeCount = 0;
    for (AlertRuleBO trendAlert : trendAlerts) {
      // 获取最新的自定义时间
      String trend = MapUtils.getString(JSONObject.parseObject(trendAlert.getTrendSettings()),
          "trend");
      String customTimeId = MapUtils.getString(JSONObject.parseObject(trend), "customTimeId");
      String latestCustomTime = customTimeService.queryCustomTime(customTimeId)
          .getCustomTimeSetting();

      // 网络作用域
      CsvUtils.convertCSVToList(trendAlert.getNetworkIds()).forEach(networkId -> {
        List<String> validNetworkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (StringUtils.equals(networkId, ALERT_SCOPE_ALL_NETWORK)) {
          validNetworkIds.addAll(allNetworkIds);
        } else {
          validNetworkIds.add(networkId);
        }
        validNetworkIds.forEach(validNetworkId -> {
          TrendAlertSetting trendAlertSetting = new TrendAlertSetting();
          trendAlertSetting.setId(trendAlert.getId());
          trendAlertSetting.setNetworkId(validNetworkId);
          trendAlertSetting.setTrendSettings(trendAlert.getTrendSettings());
          trendAlertSetting.setCustomTime(latestCustomTime);

          String id = StringUtils.join(trendAlert.getId(), validNetworkId);
          updateMissionState(id, trendAlertSetting, addCount, updateCount);
          trendAlertIds.add(id);
        });
      });

      // 业务作用域
      CsvUtils.convertCSVToList(trendAlert.getServiceIds()).forEach(serviceNetworkId -> {
        String[] split = StringUtils.split(serviceNetworkId, "^");

        TrendAlertSetting trendAlertSetting = new TrendAlertSetting();
        trendAlertSetting.setId(trendAlert.getId());
        trendAlertSetting.setServiceId(split[0]);
        trendAlertSetting.setNetworkId(split[1]);
        trendAlertSetting.setTrendSettings(trendAlert.getTrendSettings());
        trendAlertSetting.setCustomTime(latestCustomTime);

        String id = StringUtils.join(trendAlert.getId(), split[0], split[1]);
        updateMissionState(id, trendAlertSetting, addCount, updateCount);
        trendAlertIds.add(id);
      });
    }

    Iterator<Entry<String, TrendAlertSetting>> iterator = alertRuleMap.entrySet().iterator();
    while (iterator.hasNext()) {
      String id = iterator.next().getKey();
      if (!trendAlertIds.contains(id)) {
        // 配置删除
        missionService.removeMission(id);
        iterator.remove();
        removeCount++;
        LOGGER.info("found alert setting removed, id: {}", id);
      }
    }

    return Tuples.of("alert", addCount, updateCount, removeCount);
  }

  private void updateMissionState(String id, TrendAlertSetting trendAlert, int addCount,
      int updateCount) {
    TrendAlertSetting exist = alertRuleMap.get(id);
    if (exist == null) {
      // 新增加的配置
      Mission newMission = createMission(id, trendAlert);
      if (newMission != null) {
        missionService.addMission(newMission);
        alertRuleMap.put(id, trendAlert);
        addCount++;
        LOGGER.info("found alert setting created, id: {}, setting: {}.", id, trendAlert);
      }
    } else if (!StringUtils.equals(exist.getNetworkId(), trendAlert.getNetworkId())
        || !StringUtils.equals(exist.getServiceId(), trendAlert.getServiceId())
        || !StringUtils.equals(exist.getTrendSettings(), trendAlert.getTrendSettings())
        || !StringUtils.equals(exist.getCustomTime(), trendAlert.getCustomTime())) {
      // 配置变更
      missionService.removeMission(id);
      Mission updateMission = createMission(id, trendAlert);
      if (updateMission != null) {
        missionService.addMission(updateMission);
        alertRuleMap.put(id, trendAlert);
        updateCount++;
        LOGGER.info("found alert setting changed, id: {}, setting: {}.", id, trendAlert);
      }
    }
  }

  /**
   * @param trendAlert
   * @param networkId
   * @return
   */
  private Mission createMission(String id, TrendAlertSetting trendAlert) {
    String trendSettingJson = trendAlert.getTrendSettings();
    LOGGER.debug("trend setting json: {}",
        StringUtils.remove(StringUtils.remove(trendSettingJson, "\r"), "\n"));
    JsonNode trendSettingJsonNode = JsonHelper.deserialize(trendSettingJson, JsonNode.class);

    // 获取告警条件窗口
    JsonNode fireCriteria = trendSettingJsonNode.get("fireCriteria");
    int windowSeconds = fireCriteria.get("windowSeconds").asInt();

    JsonNode trend = trendSettingJsonNode.get("trend");
    String weightingModel = trend.get("weightingModel").asText();
    String windowingModel = trend.get("windowingModel").asText();
    if (StringUtils.equals(windowingModel, WindowModelEnum.LAST_N_HOURS.getValue())
        && windowSeconds < Constants.ONE_HOUR_SECONDS) {
      // 告警的基线窗口如果是小时环比，并且告警条件时间窗口在一小时内，则需要进行调整窗口模型，保证采集准确
      windowingModel = WindowModelEnum.LAST_N_HOURS_ALERT.getValue();
    }

    int windowingCount = trend.get("windowingCount").asInt();

    JsonNode metricsProperties = trendSettingJsonNode.get("metrics");

    // 自定义时间
    JsonNode custom = trend.get("customTimeId");
    String customTime = null;
    if (custom != null) {
      String customTimeId = custom.asText();
      customTime = customTimeService.queryCustomTime(customTimeId).getCustomTimeSetting();
    }

    String SourceType = null;
    String SourceValue = null;
    String numeratorMetricField = null;
    String denominatorMetricField = null;
    boolean isRatio = false;

    numeratorMetricField = metricsProperties.get("numerator").get("metric").asText();

    JsonNode numeratorType = metricsProperties.get("numerator").get("sourceType");
    JsonNode numeratorValue = metricsProperties.get("numerator").get("sourceValue");

    // 分子分母数据源相同，所以只取分子的数据源做校验
    if (numeratorType != null && numeratorValue != null) {
      SourceType = numeratorType.asText();
      SourceValue = numeratorValue.asText();
    } else {
      SourceType = StringUtils.isNotBlank(trendAlert.getServiceId())
          ? FpcConstants.SOURCE_TYPE_SERVICE
          : FpcConstants.SOURCE_TYPE_NETWORK;
      SourceValue = StringUtils.joinWith(",", trendAlert.getNetworkId(), trendAlert.getServiceId());
    }

    isRatio = metricsProperties.get("isRatio").asBoolean();
    if (isRatio) {
      denominatorMetricField = metricsProperties.get("denominator").get("metric").asText();
    }

    Mission mission = null;
    try {
      Metric metric = new Metric(isRatio, numeratorMetricField, SourceType, SourceValue,
          denominatorMetricField, SourceType, SourceValue, null);
      Operation operation = OperationFactory
          .createOperation(AggsFunctionEnum.getEnumByValue(weightingModel));
      Window window = Window.createWindow(windowingCount,
          WindowModelEnum.getEnumByValue(windowingModel));
      mission = new Mission(id, FpcConstants.BASELINE_SETTING_SOURCE_ALERT, trendAlert.getId(),
          trendAlert.getNetworkId(), trendAlert.getServiceId(), metric, operation, window,
          customTime);
    } catch (IllegalArgumentException e) {
      LOGGER.warn("mission create failed, reason: {}", e.getMessage());
    }

    return mission;
  }

  public static class TrendAlertSetting {
    private String id;
    private String trendSettings;
    private String networkId;
    private String serviceId;
    private String customTime;

    @Override
    public String toString() {
      return "TrendAlertSetting [id=" + id + ", trendSettings=" + trendSettings + ", networkId="
          + networkId + ", serviceId=" + serviceId + ", customTime=" + customTime + "]";
    }

    public String getId() {
      return id;
    }

    public void setId(String id) {
      this.id = id;
    }

    public String getTrendSettings() {
      return trendSettings;
    }

    public void setTrendSettings(String trendSettings) {
      this.trendSettings = trendSettings;
    }

    public String getNetworkId() {
      return networkId;
    }

    public void setNetworkId(String networkId) {
      this.networkId = networkId;
    }

    public String getServiceId() {
      return serviceId;
    }

    public void setServiceId(String serviceId) {
      this.serviceId = serviceId;
    }

    public String getCustomTime() {
      return customTime;
    }

    public void setCustomTime(String customTime) {
      this.customTime = customTime;
    }
  }
}
