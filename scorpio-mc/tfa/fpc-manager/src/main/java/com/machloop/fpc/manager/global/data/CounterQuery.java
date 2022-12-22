package com.machloop.fpc.manager.global.data;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2022年3月28日, fpc-manager
 */
public class CounterQuery {

  private String dsl;
  private String sourceType;
  private String networkId;
  private String serviceId;
  private Date startTimeDate;
  private Date endTimeDate;
  private boolean includeStartTime;
  private boolean includeEndTime;

  @Override
  public String toString() {
    return "CounterQuery [dsl=" + dsl + ", sourceType=" + sourceType + ", networkId=" + networkId
        + ", serviceId=" + serviceId + ", startTimeDate=" + startTimeDate + ", endTimeDate="
        + endTimeDate + ", includeStartTime=" + includeStartTime + ", includeEndTime="
        + includeEndTime + "]";
  }

  public String getDsl() {
    return dsl;
  }

  public void setDsl(String dsl) {
    this.dsl = dsl;
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

  public Date getStartTimeDate() {
    return startTimeDate;
  }

  public void setStartTimeDate(Date startTimeDate) {
    this.startTimeDate = startTimeDate;
  }

  public Date getEndTimeDate() {
    return endTimeDate;
  }

  public void setEndTimeDate(Date endTimeDate) {
    this.endTimeDate = endTimeDate;
  }

  public boolean getIncludeStartTime() {
    return includeStartTime;
  }

  public void setIncludeStartTime(boolean includeStartTime) {
    this.includeStartTime = includeStartTime;
  }

  public boolean getIncludeEndTime() {
    return includeEndTime;
  }

  public void setIncludeEndTime(boolean includeEndTime) {
    this.includeEndTime = includeEndTime;
  }

}
