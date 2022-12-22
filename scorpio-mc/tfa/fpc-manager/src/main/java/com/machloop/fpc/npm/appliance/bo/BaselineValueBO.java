package com.machloop.fpc.npm.appliance.bo;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2021年5月10日, fpc-manager
 */
public class BaselineValueBO {

  private String id;
  private String sourceType;
  private String sourceId;
  private double value;
  private Date calculateTime;
  private Date timestamp;

  @Override
  public String toString() {
    return "BaselineValueBO [id=" + id + ", sourceType=" + sourceType + ", sourceId=" + sourceId
        + ", value=" + value + ", calculateTime=" + calculateTime + ", timestamp=" + timestamp
        + "]";
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

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
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
