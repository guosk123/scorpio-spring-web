package com.machloop.fpc.manager.statistics.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author liumeng
 *
 * create at 2018年12月19日, fpc-manager
 */
public class RrdDO extends BaseDO {

  private String name;
  private Date lastTime;
  private int lastPosition;

  @Override
  public String toString() {
    return "RrdDO [name=" + name + ", lastTime=" + lastTime + ", lastPosition=" + lastPosition
        + ", toString()=" + super.toString() + "]";
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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
