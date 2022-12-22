package com.machloop.fpc.manager.system.data;

import java.util.Date;

/**
 * @author liumeng
 *
 * create at 2018年12月19日, fpc-manager
 */
public class MetricNetworkTraffic {

  private long value;
  private Date lastTime;
  private int lastPosition;

  public MetricNetworkTraffic(long value, Date lastTime, int lastPosition) {
    super();
    this.value = value;
    this.lastTime = lastTime;
    this.lastPosition = lastPosition;
  }

  @Override
  public String toString() {
    return "MetricNetworkTraffic [value=" + value + ", lastTime=" + lastTime + ", lastPosition="
        + lastPosition + "]";
  }

  public long getValue() {
    return value;
  }

  public void setValue(long value) {
    this.value = value;
  }

  public Date getLastTime() {
    return lastTime;
  }

  public void setLastTime(Date lastTime) {
    this.lastTime = lastTime;
  }

  public int getLastPosition() {
    return lastPosition;
  }

  public void setLastPosition(int lastPosition) {
    this.lastPosition = lastPosition;
  }


}
