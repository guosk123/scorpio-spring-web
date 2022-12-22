package com.machloop.fpc.cms.center.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年5月10日, fpc-manager
 */
public class BaselineValueDO extends BaseDO {

  private String sourceType;
  private String sourceId;
  private String alertNetworkId;
  private String alertNetworkGroupId;
  private String alertServiceId;
  private double value;
  private Date calculateTime;
  private Date timestamp;

  @Override
  public String toString() {
    return "BaselineValueDO [sourceType=" + sourceType + ", sourceId=" + sourceId
        + ", alertNetworkId=" + alertNetworkId + ", alertNetworkGroupId=" + alertNetworkGroupId
        + ", alertServiceId=" + alertServiceId + ", value=" + value + ", calculateTime="
        + calculateTime + ", timestamp=" + timestamp + "]";
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

  public String getAlertNetworkGroupId() {
    return alertNetworkGroupId;
  }

  public void setAlertNetworkGroupId(String alertNetworkGroupId) {
    this.alertNetworkGroupId = alertNetworkGroupId;
  }

  public String getAlertServiceId() {
    return alertServiceId;
  }

  public void setAlertServiceId(String alertServiceId) {
    this.alertServiceId = alertServiceId;
  }

  public double getValue() {
    return value;
  }

  public void setValue(double value) {
    this.value = value;
  }

  public Date getCalculateTime() {
    return calculateTime;
  }

  public void setCalculateTime(Date calculateTime) {
    this.calculateTime = calculateTime;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

}
