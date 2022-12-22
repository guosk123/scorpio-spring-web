package com.machloop.fpc.baseline.mission;

import com.machloop.fpc.baseline.calculate.Operation;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月4日, fpc-baseline
 */
public class Mission {

  private String id;
  private String missionSourceType;// 告警 | 统计分析基线配置
  private String missionSourceId;
  private String networkId;
  private String serviceId;
  private Metric metric;
  private Operation weighting;
  private Window window;
  private String customTime;

  public Mission(String id, String missionSourceType, String missionSourceId, String networkId,
      String serviceId, Metric metric, Operation weighting, Window window, String customTime) {
    super();
    this.id = id;
    this.missionSourceType = missionSourceType;
    this.missionSourceId = missionSourceId;
    this.networkId = networkId;
    this.serviceId = serviceId;
    this.metric = metric;
    this.weighting = weighting;
    this.window = window;
    this.customTime = customTime;
  }

  @Override
  public String toString() {
    return "Mission [id=" + id + ", missionSourceType=" + missionSourceType + ", missionSourceId="
        + missionSourceId + ", networkId=" + networkId + ", serviceId=" + serviceId + ", metric="
        + metric + ", weighting=" + weighting + ", window=" + window + ", customTime=" + customTime
        + "]";
  }

  public String getId() {
    return id;
  }

  public String getMissionSourceType() {
    return missionSourceType;
  }

  public String getMissionSourceId() {
    return missionSourceId;
  }

  public String getNetworkId() {
    return networkId;
  }

  public String getServiceId() {
    return serviceId;
  }

  public Metric getMetric() {
    return metric;
  }

  public Operation getWeighting() {
    return weighting;
  }

  public Window getWindow() {
    return window;
  }

  public String getCustomTime() {
    return customTime;
  }
}
