package com.machloop.fpc.baseline.calculate;

import java.util.Date;

/**
 * @author mazhiyuan
 *
 * create at 2020年11月10日, fpc-baseline
 */
public class CalculateResult {

  private String missionId;
  private String sourceType;
  private String sourceId;
  private String alertNetworkId;
  private String alertServiceId;
  private Date calculateTime;
  private double value;
  private boolean success;

  @Override
  public String toString() {
    return "CalculateResult [missionId=" + missionId + ", sourceType=" + sourceType + ", sourceId="
        + sourceId + ", alertNetworkId=" + alertNetworkId + ", alertServiceId=" + alertServiceId
        + ", calculateTime=" + calculateTime + ", value=" + value + ", success=" + success + "]";
  }

  public String getMissionId() {
    return missionId;
  }

  public void setMissionId(String missionId) {
    this.missionId = missionId;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }

  public String getAlertNetworkId() {
    return alertNetworkId;
  }

  public void setAlertNetworkId(String alertNetworkId) {
    this.alertNetworkId = alertNetworkId;
  }

  public String getAlertServiceId() {
    return alertServiceId;
  }

  public void setAlertServiceId(String alertServiceId) {
    this.alertServiceId = alertServiceId;
  }

  public Date getCalculateTime() {
    return calculateTime;
  }

  public void setCalculateTime(Date calculateTime) {
    this.calculateTime = calculateTime;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

}
