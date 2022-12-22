package com.machloop.fpc.manager.statistics.bo;

import java.util.Arrays;
import java.util.Date;

/**
 * @author liyongjun
 *
 * create at 2020年3月4日, fpc-manager
 */
public class TimeseriesBO {

  private Date startTime;
  private Date endTime;
  private int startPoint;
  private int endPoint;
  private double[] dataPoint;

  public int getDataPointNum(int capacity) {
    if (startPoint < endPoint) {
      return endPoint - startPoint + 1;
    } else if (startPoint == endPoint) {
      return 1;
    } else {
      return capacity - startPoint + 1 + endPoint;
    }
  }

  @Override
  public String toString() {
    return "TimeseriesVO [startTime=" + startTime + ", endTime=" + endTime + ", startPoint="
        + startPoint + ", endPoint=" + endPoint + ", dataPoint=" + Arrays.toString(dataPoint) + "]";
  }

  public Date getStartTime() {
    return startTime;
  }

  public void setStartTime(Date startTime) {
    this.startTime = startTime;
  }

  public Date getEndTime() {
    return endTime;
  }

  public void setEndTime(Date endTime) {
    this.endTime = endTime;
  }

  public int getStartPoint() {
    return startPoint;
  }

  public void setStartPoint(int startPoint) {
    this.startPoint = startPoint;
  }

  public int getEndPoint() {
    return endPoint;
  }

  public void setEndPoint(int endPoint) {
    this.endPoint = endPoint;
  }

  public double[] getDataPoint() {
    return dataPoint;
  }

  public void setDataPoint(double[] dataPoint) {
    this.dataPoint = dataPoint;
  }


}
