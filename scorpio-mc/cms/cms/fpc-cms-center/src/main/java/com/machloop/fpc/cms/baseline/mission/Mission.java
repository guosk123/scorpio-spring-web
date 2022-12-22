package com.machloop.fpc.cms.baseline.mission;

import com.machloop.fpc.cms.baseline.calculate.Operation;

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
  private String networkGroupId;
  private String serviceId;
  private Metric metric;
  private Operation weighting;
  private Window window;

  public Mission(String id, String missionSourceType, String missionSourceId, String networkId,
      String networkGroupId, String serviceId, Metric metric, Operation weighting, Window window) {
    super();
    this.id = id;
    this.missionSourceType = missionSourceType;
    this.missionSourceId = missionSourceId;
    this.networkId = networkId;
    this.networkGroupId = networkGroupId;
    this.serviceId = serviceId;
    this.metric = metric;
    this.weighting = weighting;
    this.window = window;
  }

  @Override
  public String toString() {
    return "Mission [id=" + id + ", missionSourceType=" + missionSourceType + ", missionSourceId="
        + missionSourceId + ", networkId=" + networkId + ", networkGroupId=" + networkGroupId
        + ", serviceId=" + serviceId + ", metric=" + metric + ", weighting=" + weighting
        + ", window=" + window + "]";
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

  public String getNetworkGroupId() {
    return networkGroupId;
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
}
