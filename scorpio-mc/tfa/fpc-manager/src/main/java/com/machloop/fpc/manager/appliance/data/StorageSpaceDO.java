package com.machloop.fpc.manager.appliance.data;

import java.util.Date;

import com.machloop.alpha.common.base.BaseDO;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
public class StorageSpaceDO extends BaseDO {

  private String spaceType;
  private long capacity;
  private Date updateTime;
  private String operatorId;

  @Override
  public String toString() {
    return "StorageSpaceDO [spaceType=" + spaceType + ", capacity=" + capacity + ", updateTime="
        + updateTime + ", operatorId=" + operatorId + "]";
  }

  public String getSpaceType() {
    return spaceType;
  }

  public void setSpaceType(String spaceType) {
    this.spaceType = spaceType;
  }

  public long getCapacity() {
    return capacity;
  }

  public void setCapacity(long capacity) {
    this.capacity = capacity;
  }

  public Date getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(Date updateTime) {
    this.updateTime = updateTime;
  }

  public String getOperatorId() {
    return operatorId;
  }

  public void setOperatorId(String operatorId) {
    this.operatorId = operatorId;
  }

}
