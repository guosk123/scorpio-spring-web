package com.machloop.fpc.cms.center.metric.vo;

import java.util.Date;

/**
 * @author "Minjiajun"
 *
 * create at 2022年1月25日, fpc-cms-center
 */
public class MetricSensorQueryVO {

  private String startTime;
  private String endTime;
  private Date startTimeDate;
  private Date endTimeDate;
  private String deviceType;
  private String metric;
  private String partitionName;
  private String dsl;
  private String tfaSerialNumber;
  private int interval;
  private int topNumber = 10;
  private int timePrecision = 9;

  private boolean includeStartTime = false;
  private boolean includeEndTime = true;

  @Override
  public String toString() {
    return "MetricSensorQueryVO [startTime=" + startTime + ", endTime=" + endTime
        + ", startTimeDate=" + startTimeDate + ", endTimeDate=" + endTimeDate + ", deviceType="
        + deviceType + ", metric=" + metric + ", partitionName=" + partitionName + ", dsl=" + dsl
        + ", tfaSerialNumber=" + tfaSerialNumber + ", interval=" + interval + ", topNumber="
        + topNumber + ", timePrecision=" + timePrecision + ", includeStartTime=" + includeStartTime
        + ", includeEndTime=" + includeEndTime + "]";
  }

  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
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

  public String getDeviceType() {
    return deviceType;
  }

  public void setDeviceType(String deviceType) {
    this.deviceType = deviceType;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public String getPartitionName() {
    return partitionName;
  }

  public void setPartitionName(String partitionName) {
    this.partitionName = partitionName;
  }

  public String getDsl() {
    return dsl;
  }

  public void setDsl(String dsl) {
    this.dsl = dsl;
  }

  public String getTfaSerialNumber() {
    return tfaSerialNumber;
  }

  public void setTfaSerialNumber(String tfaSerialNumber) {
    this.tfaSerialNumber = tfaSerialNumber;
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
  }

  public int getTopNumber() {
    return topNumber;
  }

  public void setTopNumber(int topNumber) {
    this.topNumber = topNumber;
  }

  public int getTimePrecision() {
    return timePrecision;
  }

  public void setTimePrecision(int timePrecision) {
    this.timePrecision = timePrecision;
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
