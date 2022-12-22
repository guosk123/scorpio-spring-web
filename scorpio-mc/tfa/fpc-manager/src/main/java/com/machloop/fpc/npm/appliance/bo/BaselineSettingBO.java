package com.machloop.fpc.npm.appliance.bo;

/**
 * @author guosk
 *
 * create at 2021年4月21日, fpc-manager
 */
public class BaselineSettingBO {

  private String id;
  private String sourceType;
  private String networkId;
  private String serviceId;
  private String category;
  private String weightingModel;
  private String windowingModel;
  private int windowingCount;
  private String updateTime;

  @Override
  public String toString() {
    return "BaselineSettingBO [id=" + id + ", sourceType=" + sourceType + ", networkId=" + networkId
        + ", serviceId=" + serviceId + ", category=" + category + ", weightingModel="
        + weightingModel + ", windowingModel=" + windowingModel + ", windowingCount="
        + windowingCount + ", updateTime=" + updateTime + "]";
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
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

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getWeightingModel() {
    return weightingModel;
  }

  public void setWeightingModel(String weightingModel) {
    this.weightingModel = weightingModel;
  }

  public String getWindowingModel() {
    return windowingModel;
  }

  public void setWindowingModel(String windowingModel) {
    this.windowingModel = windowingModel;
  }

  public int getWindowingCount() {
    return windowingCount;
  }

  public void setWindowingCount(int windowingCount) {
    this.windowingCount = windowingCount;
  }

  public String getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(String updateTime) {
    this.updateTime = updateTime;
  }

}
