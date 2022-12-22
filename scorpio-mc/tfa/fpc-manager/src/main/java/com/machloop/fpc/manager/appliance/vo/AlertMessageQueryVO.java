package com.machloop.fpc.manager.appliance.vo;

import java.util.Date;

/**
 * @author guosk
 *
 * create at 2020年11月3日, fpc-manager
 */
public class AlertMessageQueryVO {

  private String networkId;
  private String serviceId;
  private String startTime;
  private String endTime;
  private String name;
  private String category;
  private String level;
  private String status;
  private String solver;
  private String solveTimeBegin;
  private String solveTimeEnd;

  private Date startTimeDate;
  private Date endTimeDate;
  private int interval;

  @Override
  public String toString() {
    return "AlertMessageQueryVO{" + "networkId='" + networkId + '\'' + ", serviceId='" + serviceId
        + '\'' + ", startTime='" + startTime + '\'' + ", endTime='" + endTime + '\'' + ", name='"
        + name + '\'' + ", category='" + category + '\'' + ", level='" + level + '\'' + ", status='"
        + status + '\'' + ", solver='" + solver + '\'' + ", solveTimeBegin='" + solveTimeBegin
        + '\'' + ", solveTimeEnd='" + solveTimeEnd + '\'' + ", startTimeDate=" + startTimeDate
        + ", endTimeDate=" + endTimeDate + ", interval=" + interval + '}';
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

  public int getInterval() {
    return interval;
  }

  public void setInterval(int interval) {
    this.interval = interval;
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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSolver() {
    return solver;
  }

  public void setSolver(String solver) {
    this.solver = solver;
  }

  public String getSolveTimeBegin() {
    return solveTimeBegin;
  }

  public void setSolveTimeBegin(String solveTimeBegin) {
    this.solveTimeBegin = solveTimeBegin;
  }

  public String getSolveTimeEnd() {
    return solveTimeEnd;
  }

  public void setSolveTimeEnd(String solveTimeEnd) {
    this.solveTimeEnd = solveTimeEnd;
  }

}
