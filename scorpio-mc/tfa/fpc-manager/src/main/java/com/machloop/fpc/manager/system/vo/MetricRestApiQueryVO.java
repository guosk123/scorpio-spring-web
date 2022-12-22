package com.machloop.fpc.manager.system.vo;


import java.util.Date;

import org.hibernate.validator.constraints.Range;

public class MetricRestApiQueryVO {


  private String dsl;
  private String startTime;
  private String endTime;
  @Range(min = 10, max = 200, message = "topN的有效范围是[10,200]")
  private int count = 10;
  private Date startTimeDate;
  private Date endTimeDate;
  private int interval;
  // 查询范围是否包含开始时间
  private boolean includeStartTime = false;
  // 查询范围是否包含结束时间
  private boolean includeEndTime = true;
  private boolean hasAgingTime = false;
  private int timePrecision = 3;
  private String sourceType;// 数据源

  @Override
  public String toString() {
    return "MetricRestApiQueryVO{" + "dsl='" + dsl + '\'' + ", startTime='" + startTime + '\''
        + ", endTime='" + endTime + '\'' + ", count=" + count + ", startTimeDate=" + startTimeDate
        + ", endTimeDate=" + endTimeDate + ", interval=" + interval + ", includeStartTime="
        + includeStartTime + ", includeEndTime=" + includeEndTime + ", hasAgingTime=" + hasAgingTime
        + ", timePrecision=" + timePrecision + ", sourceType='" + sourceType + '\'' + '}';
  }

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
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

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public String getDsl() {
    return dsl;
  }

  public void setDsl(String dsl) {
    this.dsl = dsl;
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

  public boolean getHasAgingTime() {
    return hasAgingTime;
  }

  public void setHasAgingTime(boolean hasAgingTime) {
    this.hasAgingTime = hasAgingTime;
  }

  public int getTimePrecision() {
    return timePrecision;
  }

  public void setTimePrecision(int timePrecision) {
    this.timePrecision = timePrecision;
  }

  public String getSourceType() {
    return sourceType;
  }

  public void setSourceType(String sourceType) {
    this.sourceType = sourceType;
  }
}
